package tenis.gestor;

import tenis.dao.JugadorDAO;
import tenis.dao.ReservaDAO;
import tenis.enums.EstadoReserva;
import tenis.enums.TipoJuego;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.CanchaBloqueadaException;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.excepciones.MenorSinResponsableException;
import tenis.excepciones.TurnoNoDisponibleException;
import tenis.excepciones.UsuarioSinPermisoException;
import tenis.interfaces.DAO;
import tenis.modelo.Bloqueo;
import tenis.modelo.Cancha;
import tenis.modelo.Club;
import tenis.modelo.Extra;
import tenis.modelo.Jugador;
import tenis.modelo.Reserva;
import tenis.modelo.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones sobre las reservas y las reglas de negocio de un turno.
 * Las operaciones que dependen de quién las pide reciben al usuario que inició sesión
 * y le preguntan si tiene permiso. Cada tipo de usuario responde distinto
 * (polimorfismo), así que este gestor no necesita saber de qué tipo es.
 */
public class GestorReserva {

    private final DAO<Reserva> dao = new ReservaDAO();
    private final DAO<Jugador> jugadorDAO = new JugadorDAO();

    /**
     * Valida todas las reglas de un turno y, si se cumplen, crea la reserva sin guardarla,
     * para que todavía se le puedan agregar extras.
     * El titular es el propio jugador si es adulto, o su responsable si es menor.
     *
     * @param solicitante usuario que pide la reserva
     * @param cancha cancha a reservar
     * @param jugador quien va a jugar
     * @param inicio comienzo del turno
     * @param fin final del turno
     * @param tipoJuego singles o dobles
     * @return la reserva creada, activa y todavía sin guardar
     * @throws UsuarioSinPermisoException si el solicitante no puede reservar ese turno
     * @throws TurnoNoDisponibleException si el club no atiende en ese horario o la cancha ya está reservada
     * @throws CanchaBloqueadaException si la cancha tiene un bloqueo en ese período
     * @throws MenorSinResponsableException si el jugador es menor y no tiene responsable
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws DatosInvalidosException si el turno termina antes de empezar o ya pasó
     */
    public Reserva prepararReserva(Usuario solicitante, Cancha cancha, Jugador jugador,
                                   LocalDateTime inicio, LocalDateTime fin, TipoJuego tipoJuego)
            throws UsuarioSinPermisoException, TurnoNoDisponibleException, CanchaBloqueadaException,
            MenorSinResponsableException, AccesoDatosException {

        // 1. Permiso: lo responde cada tipo de usuario
        if (!solicitante.puedeGestionarReserva(cancha, jugador)) {
            throw new UsuarioSinPermisoException("No podés reservar para ese jugador en esa cancha");
        }

        // 2. El turno tiene que ser coherente y futuro
        if (!fin.isAfter(inicio)) {
            throw new DatosInvalidosException("El turno debe terminar después de empezar");
        }
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new DatosInvalidosException("No se puede reservar un turno que ya empezó");
        }

        // 3. El club tiene que atender durante todo el turno
        Club club = cancha.getClub();
        if (!club.admiteTurno(inicio, fin)) {
            throw new TurnoNoDisponibleException("El club no atiende en ese horario: abre de "
                    + club.getHoraApertura() + " a " + club.getHoraCierre() + " los días " + club.diasAtencionTexto());
        }

        // 4. La cancha no puede estar bloqueada
        Bloqueo bloqueo = cancha.bloqueoEn(inicio, fin);
        if (bloqueo != null) {
            throw new CanchaBloqueadaException("La cancha está bloqueada por " + bloqueo.getMotivo()
                    + " de " + bloqueo.getDesde() + " a " + bloqueo.getHasta());
        }

        // 5. Un menor necesita responsable, que pasa a ser el titular
        Jugador titular = jugador;
        if (jugador.esMenor()) {
            if (jugador.getResponsable() == null) {
                throw new MenorSinResponsableException("El jugador menor de edad no tiene un responsable asociado.");
            }
            titular = jugador.getResponsable();
        }

        // 6. La cancha tiene que estar libre
        verificarDisponibilidad(cancha, inicio, fin);

        return new Reserva(0, cancha, jugador, titular, inicio, fin, tipoJuego);
    }

    /**
     * Cancela una reserva y calcula el cargo que corresponde.
     * Si la cancelación es tardía, guarda la cancelación tardía en el titular.
     *
     * @param solicitante usuario que pide la cancelación
     * @param idReserva id de la reserva a cancelar
     * @return el cargo a pagar; 0 si se canceló a tiempo
     * @throws UsuarioSinPermisoException si el solicitante no puede cancelar esa reserva
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     * @throws DatosInvalidosException si la reserva no está activa
     */
    public double cancelar(Usuario solicitante, int idReserva)
            throws UsuarioSinPermisoException, AccesoDatosException, ElementoNoEncontradoException {
        Reserva r = dao.buscarPorId(idReserva);
        if (!solicitante.puedeGestionarReserva(r.getCancha(), r.getJugador())) {
            throw new UsuarioSinPermisoException("No podés cancelar esa reserva");
        }
        double cargo = r.cancelar(LocalDateTime.now());
        dao.actualizar(r);
        if (cargo > 0 && r.getTitular() != null) {
            // cancelar() ya sumó la cancelación tardía al titular; acá se guarda
            jugadorDAO.actualizar(r.getTitular());
        }
        return cargo;
    }

    /**
     * Marca una reserva activa como completada y registra que el jugador asistió.
     *
     * @param solicitante usuario que lo pide; tiene que poder administrar la cancha
     * @param idReserva id de la reserva
     * @throws UsuarioSinPermisoException si el solicitante no puede administrar esa cancha
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     * @throws DatosInvalidosException si la reserva no está activa
     */
    public void completar(Usuario solicitante, int idReserva)
            throws UsuarioSinPermisoException, AccesoDatosException, ElementoNoEncontradoException {
        Reserva r = dao.buscarPorId(idReserva);
        if (!solicitante.puedeGestionarCancha(r.getCancha())) {
            throw new UsuarioSinPermisoException("No podés modificar reservas de esa cancha");
        }
        if (r.getEstado() != EstadoReserva.ACTIVA) {
            throw new DatosInvalidosException("Solo se puede completar una reserva activa; esta está " + r.getEstado());
        }
        r.setEstado(EstadoReserva.COMPLETADA);
        r.setAsistio(true);
        dao.actualizar(r);
    }

    /**
     * Lista las reservas que el usuario puede ver: el jugador las suyas y las de sus menores,
     * el recepcionista las de su club y el administrador todas.
     *
     * @param solicitante usuario que inició sesión
     * @return las reservas que puede gestionar
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public List<Reserva> listarVisibles(Usuario solicitante) throws AccesoDatosException {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : dao.listarTodos()) {
            if (solicitante.puedeGestionarReserva(r.getCancha(), r.getJugador())) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * Calcula los turnos de una hora que una cancha tiene libres en una fecha.
     * Recorre el horario del club de hora en hora y descarta los turnos que ya pasaron,
     * los que se superponen con una reserva y los que caen en un bloqueo.
     *
     * @param cancha cancha a consultar
     * @param fecha día a consultar
     * @return la hora de inicio de cada turno libre; lista vacía si el club no atiende ese día
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public List<LocalTime> horariosLibres(Cancha cancha, LocalDate fecha) throws AccesoDatosException {
        List<LocalTime> libres = new ArrayList<>();
        Club club = cancha.getClub();
        if (!club.getDiasAtencion().contains(fecha.getDayOfWeek())) {
            return libres;
        }

        List<Reserva> reservasDeLaCancha = reservasDe(cancha);
        LocalDateTime turno = fecha.atTime(club.getHoraApertura());
        LocalDateTime cierre = fecha.atTime(club.getHoraCierre());

        while (!turno.plusHours(1).isAfter(cierre)) {
            LocalDateTime finTurno = turno.plusHours(1);
            if (!turno.isBefore(LocalDateTime.now())
                    && !cancha.estaBloqueada(turno, finTurno)
                    && !estaOcupada(reservasDeLaCancha, turno, finTurno)) {
                libres.add(turno.toLocalTime());
            }
            turno = finTurno;
        }
        return libres;
    }

    /**
     * Busca una reserva por su id.
     *
     * @param id identificador de la reserva
     * @return la reserva encontrada
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */
    public Reserva buscarEnBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        // Al buscar por ID, el DAO ya se encarga de instanciar la reserva y cargarle sus extras
        return dao.buscarPorId(id);
    }

    /**
     * @return todas las reservas guardadas
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public List<Reserva> listarDesdeBD() throws AccesoDatosException {
        // Retorna todas las reservas, cada una con su respectiva lista de extras cargada
        return dao.listarTodos();
    }

    /**
     * Guarda una reserva nueva, siempre que la cancha siga libre en ese horario.
     * La verificación se repite acá aunque ya la haga {@link #prepararReserva}, porque
     * entre preparar y guardar otro usuario pudo haber reservado el mismo turno.
     *
     * @param r reserva a guardar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws TurnoNoDisponibleException si otra reserva activa de la misma cancha se superpone
     */
    public void guardarEnBD(Reserva r) throws AccesoDatosException, TurnoNoDisponibleException {
        verificarDisponibilidad(r.getCancha(), r.getInicio(), r.getFin());
        dao.insertar(r);
    }

    /**
     * Guarda los cambios de una reserva que ya existe.
     *
     * @param r reserva con los datos nuevos
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */
    public void actualizarEnBD(Reserva r) throws AccesoDatosException, ElementoNoEncontradoException {
        // Actualiza los datos de la reserva y sincroniza (borra e reinserta) sus extras
        dao.actualizar(r);
    }

    /**
     * Elimina una reserva.
     *
     * @param id identificador de la reserva a eliminar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */
    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        // Borra los extras asociados y luego elimina la reserva principal
        dao.eliminar(id);
    }

    /**
     * Agrega un extra a una reserva ya guardada.
     * Lee la reserva de la base, le suma el extra y guarda el cambio.
     *
     * @param reservaId identificador de la reserva
     * @param extra extra a agregar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */
    public void agregarExtraAReserva(int reservaId, Extra extra) throws AccesoDatosException, ElementoNoEncontradoException {
        // Traemos la reserva de la base de datos
        Reserva reserva = buscarEnBD(reservaId);

        // Sumamos a la lista en memoria
        reserva.agregarExtra(extra);

        // Persistimos el cambio (el DAO actualizará la tabla secundaria)
        actualizarEnBD(reserva);
    }

    /**
     * @param cancha cancha a verificar
     * @param inicio comienzo del turno
     * @param fin final del turno
     * @throws TurnoNoDisponibleException si otra reserva de la cancha se superpone con el turno
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    private void verificarDisponibilidad(Cancha cancha, LocalDateTime inicio, LocalDateTime fin)
            throws TurnoNoDisponibleException, AccesoDatosException {
        if (estaOcupada(reservasDe(cancha), inicio, fin)) {
            throw new TurnoNoDisponibleException("La cancha ya está reservada en ese horario");
        }
    }

    /**
     * @param reservas reservas de una cancha
     * @param inicio comienzo del turno
     * @param fin final del turno
     * @return true si alguna reserva no cancelada se superpone con el turno
     */
    private boolean estaOcupada(List<Reserva> reservas, LocalDateTime inicio, LocalDateTime fin) {
        for (Reserva r : reservas) {
            if (r.seSuperpone(inicio, fin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param cancha cancha a consultar
     * @return todas las reservas de esa cancha, de cualquier estado
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    private List<Reserva> reservasDe(Cancha cancha) throws AccesoDatosException {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : dao.listarTodos()) {
            if (r.getCancha().getId() == cancha.getId()) {
                resultado.add(r);
            }
        }
        return resultado;
    }
}
