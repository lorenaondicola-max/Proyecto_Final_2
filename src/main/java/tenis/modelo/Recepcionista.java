package tenis.modelo;

import tenis.enums.MotivoBloqueo;
import tenis.enums.TipoJuego;
import tenis.excepciones.CanchaBloqueadaException;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.MenorSinResponsableException;
import tenis.excepciones.TurnoNoDisponibleException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Usuario que atiende a los jugadores en un club.
 * Registra y cancela reservas y bloquea canchas. Pertenece a un único club,
 * al que consulta para validar los horarios.
 */
public class Recepcionista extends Usuario {
    private Club club;
    private static int contadorIdReserva = 1;

    /**
     * @param club club en el que trabaja el recepcionista
     */
    public Recepcionista(int id, String nombre, String dni, LocalDate fechaNacimiento,
                         String domicilio, String telefono, String email, String username,
                         String passwordHash, Club club) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
        this.club = club;
    }

    /**
     * @return los permisos operativos: gestionar y cancelar reservas,
     *         bloquear canchas y cargar jugadores
     */
    @Override
    public List<String> getPermisos() {
        // El recepcionista tiene permisos operativos sobre su club asignado
        return Arrays.asList("GESTIONAR_RESERVAS", "CANCELAR_RESERVAS", "BLOQUEAR_CANCHAS", "CARGAR_JUGADORES");
    }

    /**
     * Valida las reglas de una reserva y, si se cumplen, la crea.
     * Verifica que el turno termine después de empezar, que el club esté abierto
     * a la hora de inicio, que la cancha no esté bloqueada y, si el jugador es menor,
     * que tenga un responsable. El titular es el propio jugador si es adulto,
     * o su responsable si es menor.
     * No guarda la reserva ni comprueba si la cancha ya tiene otro turno en ese horario:
     * de eso se ocupa {@code GestorReserva.guardarEnBD()}.
     *
     * @param cancha cancha a reservar
     * @param jugador quien va a jugar
     * @param inicio comienzo del turno
     * @param fin final del turno
     * @param tipoJuego singles o dobles
     * @return la reserva creada, activa y todavía sin guardar
     * @throws TurnoNoDisponibleException si el club está cerrado a la hora de inicio
     * @throws CanchaBloqueadaException si la cancha tiene un bloqueo en ese periodo
     * @throws MenorSinResponsableException si el jugador es menor y no tiene responsable
     * @throws DatosInvalidosException si el fin no es posterior al inicio
     */
    public Reserva registrarReserva(Cancha cancha, Jugador jugador, LocalDateTime inicio,
                                    LocalDateTime fin, TipoJuego tipoJuego)
            throws TurnoNoDisponibleException, CanchaBloqueadaException, MenorSinResponsableException {
        //1. Validar que el turno termine antes de empezar
        if (!fin.isAfter(inicio)) {
            throw new DatosInvalidosException("El turno debe terminar después de empezar");
        }

        // 2. Validar que el club esté abierto en ese momento
        if (!this.club.estaAbierto(inicio)) {
            throw new TurnoNoDisponibleException("El club se encuentra cerrado en el horario seleccionado.");
        }

        // 3. Validar que la cancha no esté bloqueada por mantenimiento, torneo o clases
        if (cancha.estaBloqueada(inicio, fin)) {
            throw new CanchaBloqueadaException("La cancha se encuentra bloqueada en el período seleccionado.");
        }

        // 4. Validar las reglas específicas para jugadores menores de edad
        Jugador titularFinal = jugador;
        if (jugador.esMenor()) {
            if (jugador.getResponsable() == null) {
                throw new MenorSinResponsableException("El jugador menor de edad no tiene un responsable asociado.");
            }
            // El titular y responsable que responde por la reserva es el adulto asignado
            titularFinal = jugador.getResponsable();
        }

        // 5. Crear y retornar la nueva reserva (el ID luego será manejado por la BD)
        int idReserva = contadorIdReserva++;
        Reserva nuevaReserva = new Reserva(idReserva, cancha, jugador, titularFinal, inicio, fin, tipoJuego);

        return nuevaReserva;
    }

    /**
     * Cancela una reserva. El cálculo del cargo lo hace la propia reserva.
     *
     * @param reserva reserva a cancelar
     * @param ahora momento en que se pide la cancelación
     * @return el cargo a pagar; 0 si se canceló con la anticipación que exige el club
     * @throws DatosInvalidosException si la reserva no está activa
     */
    public double cancelarReserva(Reserva reserva, LocalDateTime ahora) {
        // Lógica para cancelar la reserva y calcular posibles cargos tardíos
        return reserva.cancelar(ahora);
    }

    /**
     * Bloquea una cancha durante un período, para que no pueda reservarse.
     * El bloqueo se guarda en memoria, dentro de la cancha.
     *
     * @param motivo mantenimiento, torneo o clases
     * @param desde inicio del bloqueo
     * @param hasta fin del bloqueo
     * @param detalle texto libre con la causa
     */
    public void bloquearCancha(Cancha cancha, MotivoBloqueo motivo, LocalDateTime desde, LocalDateTime hasta, String detalle) {
        int idBloqueo = cancha.getBloqueos().size() + 1; // Generación simple de ID en memoria
        Bloqueo nuevoBloqueo = new Bloqueo(idBloqueo, motivo, desde, hasta, detalle);
        cancha.agregarBloqueo(nuevoBloqueo);
    }

    // Getters y Setters
    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }
}
