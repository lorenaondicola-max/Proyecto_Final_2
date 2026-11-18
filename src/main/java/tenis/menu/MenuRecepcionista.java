package tenis.menu;

import tenis.enums.Categoria;
import tenis.enums.MotivoBloqueo;
import tenis.enums.Nivel;
import tenis.enums.Superficie;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.CanchaBloqueadaException;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.excepciones.MenorSinResponsableException;
import tenis.excepciones.TurnoNoDisponibleException;
import tenis.excepciones.UsuarioSinPermisoException;
import tenis.gestor.GestorUsuarios;
import tenis.modelo.Bloqueo;
import tenis.modelo.Cancha;
import tenis.modelo.Club;
import tenis.modelo.Jugador;
import tenis.modelo.Recepcionista;
import tenis.modelo.Reserva;
import tenis.modelo.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Menú de un recepcionista: las opciones comunes más la administración de jugadores,
 * canchas, bloqueos y reservas de su club.
 * El club sobre el que opera lo da {@link #elegirClub()} y las canchas que administra
 * las da {@link #canchasGestionables()}; {@link MenuAdministrador} sobrescribe los dos
 * para trabajar sobre todos los clubes.
 */
public class MenuRecepcionista extends Menu {

    protected final GestorUsuarios gestorUsuarios = new GestorUsuarios();
    private final Club club;

    /**
     * @param recepcionista recepcionista que inició sesión
     */
    public MenuRecepcionista(Recepcionista recepcionista) {
        super(recepcionista);
        this.club = recepcionista.getClub();
    }

    /**
     * Constructor para las subclases que no trabajan sobre un único club.
     *
     * @param usuario usuario que inició sesión
     */
    protected MenuRecepcionista(Usuario usuario) {
        super(usuario);
        this.club = null;
    }

    @Override
    protected String titulo() {
        return "MENÚ DEL RECEPCIONISTA - " + club.getNombre();
    }

    /**
     * @return el club del recepcionista
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no hay clubes para elegir
     */
    protected Club elegirClub() throws AccesoDatosException, ElementoNoEncontradoException {
        return club;
    }

    /**
     * @return las canchas del club del recepcionista
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    protected List<Cancha> canchasGestionables() throws AccesoDatosException {
        return gestorCancha.listarPorClub(club.getId());
    }

    /**
     * @return todos los jugadores: el recepcionista reserva para cualquiera
     */
    @Override
    protected List<Jugador> jugadoresHabilitados() throws AccesoDatosException {
        return gestorJugador.listarTodos();
    }

    /**
     * @return las canchas que administra
     */
    @Override
    protected List<Cancha> canchasParaReservar() throws AccesoDatosException {
        return canchasGestionables();
    }

    @Override
    protected void mostrarOpciones() {
        super.mostrarOpciones();
        System.out.println("5. Registrar jugador");
        System.out.println("6. Listar jugadores");
        System.out.println("7. Registrar cancha");
        System.out.println("8. Bloquear cancha");
        System.out.println("9. Desbloquear cancha");
        System.out.println("10. Marcar reserva como completada");
    }

    @Override
    protected boolean ejecutarOpcion(int opcion)
            throws AccesoDatosException, ElementoNoEncontradoException, TurnoNoDisponibleException,
            CanchaBloqueadaException, MenorSinResponsableException, UsuarioSinPermisoException {
        switch (opcion) {
            case 5 -> registrarJugador();
            case 6 -> listarJugadores();
            case 7 -> registrarCancha();
            case 8 -> bloquearCancha();
            case 9 -> desbloquearCancha();
            case 10 -> completarReserva();
            default -> {
                return super.ejecutarOpcion(opcion);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ opciones

    /**
     * Da de alta un jugador. Si es menor, le asigna un responsable adulto y no le pide
     * credenciales; si es adulto, le pide un nombre de usuario libre y una clave.
     */
    private void registrarJugador() throws AccesoDatosException, ElementoNoEncontradoException,
            MenorSinResponsableException {

        String nombre = Consola.leerTexto("Nombre y apellido: ", "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s']+$",
                "Error: El nombre solo debe contener letras, espacios y apóstrofes. Intente nuevamente.");

        String dni;
        while (true) {
            dni = leerDni();
            if (dniLibre(dni)) {
                break;
            }
            System.out.println("Error: Ya existe un jugador registrado con el DNI " + dni + ". Ingrese otro.");
        }

        LocalDate nacimiento = leerFechaNacimiento();
        String domicilio = Consola.leerTexto("Domicilio: ", "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s°]+$",
                "Error: El domicilio solo puede contener letras y números. Intente nuevamente.");
        String telefono = Consola.leerTexto("Teléfono: ", "^\\d+$",
                "Error: El teléfono solo debe contener números. Intente nuevamente.");
        String email = leerEmail();
        boolean esSocio = Consola.leerSiNo("¿Es socio?");
        Nivel nivel = Consola.elegirEnum("Nivel:", Nivel.values());
        Categoria categoria = Consola.elegirEnum("Categoría:", Categoria.values());

        Jugador nuevo;
        if (Period.between(nacimiento, LocalDate.now()).getYears() < 18) {
            System.out.println("El jugador es menor de edad: hay que asignarle un responsable.");
            Jugador responsable = Consola.elegirDeLista("Jugadores mayores de edad:", jugadoresAdultos());
            nuevo = new Jugador(nombre, dni, nacimiento, domicilio, telefono, email,
                    esSocio, nivel, categoria, responsable);
        } else {
            String usuarioNuevo = leerUsernameLibre();
            String clave = Consola.leerTexto("Clave: ");
            nuevo = new Jugador(nombre, dni, nacimiento, domicilio, telefono, email,
                    usuarioNuevo, clave, esSocio, nivel, categoria);
        }

        gestorJugador.guardar(nuevo);
        System.out.println("Jugador registrado con éxito: " + nuevo);
    }

    private void listarJugadores() throws AccesoDatosException {
        List<Jugador> jugadores = gestorJugador.listarTodos();
        if (jugadores.isEmpty()) {
            System.out.println("No hay jugadores cargados.");
            return;
        }
        for (Jugador j : jugadores) {
            System.out.println(j);
        }
    }

    /**
     * Da de alta una cancha en el club que corresponda, con un número que el club no use.
     */
    private void registrarCancha() throws AccesoDatosException, ElementoNoEncontradoException {
        Club destino = elegirClub();
        int numero = Consola.leerEnteroPositivo("Número de cancha: ");
        for (Cancha c : gestorCancha.listarPorClub(destino.getId())) {
            if (c.getNumero() == numero) {
                throw new DatosInvalidosException(destino.getNombre() + " ya tiene una cancha número " + numero);
            }
        }
        Superficie superficie = Consola.elegirEnum("Superficie:", Superficie.values());
        boolean tieneLuz = Consola.leerSiNo("¿Tiene iluminación?");

        Cancha nueva = new Cancha(0, numero, superficie, tieneLuz, destino);
        gestorCancha.guardar(nueva);
        System.out.println("Cancha registrada: " + nueva);
    }

    /**
     * Bloquea una cancha durante un período. Las reservas que ya existían en ese período
     * no se cancelan: se informan para que se resuelvan con los jugadores.
     */
    private void bloquearCancha() throws AccesoDatosException, ElementoNoEncontradoException {
        Cancha cancha = Consola.elegirDeLista("Canchas:", canchasGestionables());
        MotivoBloqueo motivo = Consola.elegirEnum("Motivo:", MotivoBloqueo.values());
        LocalDateTime desde = LocalDateTime.of(leerFechaFutura("Desde, fecha (AAAA-MM-DD): "),
                Consola.leerHora("Desde, hora (HH:MM): "));
        LocalDateTime hasta = LocalDateTime.of(Consola.leerFecha("Hasta, fecha (AAAA-MM-DD): "),
                Consola.leerHora("Hasta, hora (HH:MM): "));
        String detalle = Consola.leerTexto("Detalle: ");

        Bloqueo bloqueo = gestorCancha.bloquear(cancha, motivo, desde, hasta, detalle);
        System.out.println("Cancha bloqueada: " + bloqueo);

        for (Reserva r : reservasActivas()) {
            if (r.getCancha().getId() == cancha.getId() && bloqueo.seSuperpone(r.getInicio(), r.getFin())) {
                System.out.println("  Atención, reserva afectada: " + r);
            }
        }
    }

    /**
     * Quita uno de los bloqueos de una cancha.
     */
    private void desbloquearCancha() throws AccesoDatosException, ElementoNoEncontradoException {
        Cancha cancha = Consola.elegirDeLista("Canchas:", canchasGestionables());
        Bloqueo bloqueo = Consola.elegirDeLista("Bloqueos de la cancha:", cancha.getBloqueos());
        gestorCancha.desbloquear(cancha, bloqueo.getId());
        System.out.println("Bloqueo eliminado.");
    }

    /**
     * Marca como completada una reserva activa de las canchas que administra.
     */
    private void completarReserva() throws AccesoDatosException, ElementoNoEncontradoException,
            UsuarioSinPermisoException {
        Reserva r = Consola.elegirDeLista("Reservas activas:", reservasActivas());
        gestorReserva.completar(usuario, r.getId());
        System.out.println("¡La reserva #" + r.getId() + " ha sido actualizada a estado: COMPLETADA!");
    }

    // ------------------------------------------------------------------ auxiliares

    /**
     * @return un DNI de 7 u 8 dígitos
     */
    protected String leerDni() {
        return Consola.leerTexto("DNI: ", "^\\d{7,8}$",
                "Error: El DNI debe ser un número entero de 7 u 8 dígitos. Intente nuevamente.");
    }

    /**
     * @return un email con @ y terminado en .com
     */
    protected String leerEmail() {
        return Consola.leerTexto("Email: ", "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$",
                "Error: El email no es válido (debe contener '@' y finalizar en '.com'). Intente nuevamente.");
    }

    /**
     * @return una fecha de nacimiento que no es futura
     */
    protected LocalDate leerFechaNacimiento() {
        while (true) {
            LocalDate fecha = Consola.leerFecha("Fecha de nacimiento (AAAA-MM-DD): ");
            if (!fecha.isAfter(LocalDate.now())) {
                return fecha;
            }
            System.out.println("Error: La fecha de nacimiento no puede ser futura. Intente nuevamente.");
        }
    }

    /**
     * @return un nombre de usuario que nadie usa todavía
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    protected String leerUsernameLibre() throws AccesoDatosException {
        while (true) {
            String username = Consola.leerTexto("Usuario: ");
            if (gestorUsuarios.usernameDisponible(username)) {
                return username;
            }
            System.out.println("Ese nombre de usuario ya está en uso. Elegí otro.");
        }
    }

    /**
     * @param dni DNI a consultar
     * @return true si ningún jugador tiene ese DNI
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    private boolean dniLibre(String dni) throws AccesoDatosException {
        for (Jugador j : gestorJugador.listarTodos()) {
            if (j.getDni().equals(dni)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return los jugadores mayores de edad, que pueden ser responsables de un menor
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    private List<Jugador> jugadoresAdultos() throws AccesoDatosException {
        List<Jugador> adultos = new ArrayList<>();
        for (Jugador j : gestorJugador.listarTodos()) {
            if (!j.esMenor()) {
                adultos.add(j);
            }
        }
        return adultos;
    }
}
