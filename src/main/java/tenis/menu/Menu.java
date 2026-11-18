package tenis.menu;

import tenis.enums.EstadoReserva;
import tenis.enums.TipoJuego;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.CanchaBloqueadaException;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.excepciones.MenorSinResponsableException;
import tenis.excepciones.TurnoNoDisponibleException;
import tenis.excepciones.UsuarioSinPermisoException;
import tenis.gestor.GestorCanchas;
import tenis.gestor.GestorClub;
import tenis.gestor.GestorJugador;
import tenis.gestor.GestorReserva;
import tenis.modelo.Cancha;
import tenis.modelo.ClaseConProfesor;
import tenis.modelo.Club;
import tenis.modelo.Extra;
import tenis.modelo.Iluminacion;
import tenis.modelo.Jugador;
import tenis.modelo.Pelotas;
import tenis.modelo.Raqueta;
import tenis.modelo.Reserva;
import tenis.modelo.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Menú de opciones de un usuario que inició sesión.
 * Tiene las cuatro opciones que comparten todos los usuarios: consultar horarios libres,
 * reservar, cancelar y ver reservas. Las subclases agregan las suyas sobrescribiendo
 * {@link #mostrarOpciones()} y {@link #ejecutarOpcion(int)}, y llamando a la versión
 * de la superclase para conservar las anteriores.
 * Es abstracta porque lo que cambia entre usuarios (para quién y en qué canchas puede reservar)
 * lo definen las subclases en {@link #jugadoresHabilitados()} y {@link #canchasParaReservar()}.
 */
public abstract class Menu {

    private static final double PRECIO_RAQUETA = 1500.0;
    private static final double PRECIO_TUBO_PELOTAS = 3000.0;
    private static final double PRECIO_HORA_LUZ = 500.0;
    private static final double PRECIO_HORA_CLASE = 15000.0;

    protected final Usuario usuario;
    protected final GestorClub gestorClub = new GestorClub();
    protected final GestorCanchas gestorCancha = new GestorCanchas();
    protected final GestorJugador gestorJugador = new GestorJugador();
    protected final GestorReserva gestorReserva = new GestorReserva();

    /**
     * @param usuario usuario que inició sesión
     */
    protected Menu(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Muestra el menú y ejecuta opciones hasta que el usuario cierra sesión.
     * Es el único lugar donde se atrapan las excepciones: cada error se informa
     * y el menú sigue funcionando.
     */
    public final void ejecutar() {
        System.out.println("\nBienvenido/a, " + usuario.getNombre() + ".");
        int opcion;
        do {
            System.out.println("\n=== " + titulo() + " ===");
            mostrarOpciones();
            System.out.println("0. Cerrar sesión");
            opcion = Consola.leerEntero("Opción: ");
            if (opcion == 0) {
                break;
            }
            try {
                if (!ejecutarOpcion(opcion)) {
                    System.out.println("Opción inválida.");
                }
            } catch (TurnoNoDisponibleException | CanchaBloqueadaException | MenorSinResponsableException e) {
                System.out.println("Operación rechazada: " + e.getMessage());
            } catch (UsuarioSinPermisoException e) {
                System.out.println("Sin permiso: " + e.getMessage());
            } catch (ElementoNoEncontradoException e) {
                System.out.println("No encontrado: " + e.getMessage());
            } catch (AccesoDatosException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (DatosInvalidosException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            }
        } while (true);
        System.out.println("Sesión cerrada.");
    }

    /**
     * @return el título del menú, distinto para cada tipo de usuario
     */
    protected abstract String titulo();

    /**
     * @return los jugadores para los que este usuario puede reservar
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    protected abstract List<Jugador> jugadoresHabilitados() throws AccesoDatosException;

    /**
     * @return las canchas en las que este usuario puede reservar
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    protected abstract List<Cancha> canchasParaReservar() throws AccesoDatosException;

    /**
     * Muestra las opciones comunes. Las subclases la sobrescriben para agregar las suyas.
     */
    protected void mostrarOpciones() {
        System.out.println("1. Ver canchas y horarios disponibles por club");
        System.out.println("2. Registrar reserva");
        System.out.println("3. Cancelar reserva");
        System.out.println("4. Ver reservas");
    }

    /**
     * Ejecuta una opción común. Las subclases la sobrescriben para atender sus propias
     * opciones y, si el número no es suyo, se lo pasan a esta versión.
     * Ninguna subclase puede declarar más excepciones verificadas que estas:
     * un método sobrescrito no puede ampliar el {@code throws} del original.
     *
     * @param opcion número elegido
     * @return true si la opción existe; false si no corresponde a ninguna
     */
    protected boolean ejecutarOpcion(int opcion)
            throws AccesoDatosException, ElementoNoEncontradoException, TurnoNoDisponibleException,
            CanchaBloqueadaException, MenorSinResponsableException, UsuarioSinPermisoException {
        switch (opcion) {
            case 1 -> verHorariosDisponibles();
            case 2 -> registrarReserva();
            case 3 -> cancelarReserva();
            case 4 -> verReservas();
            default -> {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ opciones comunes

    /**
     * Pide un club y una fecha, y muestra los turnos libres de cada cancha de ese club.
     */
    private void verHorariosDisponibles() throws AccesoDatosException, ElementoNoEncontradoException {
        Club club = Consola.elegirDeLista("Clubes:", gestorClub.listarDesdeBD());
        LocalDate fecha = leerFechaFutura("Fecha (AAAA-MM-DD): ");

        if (!club.getDiasAtencion().contains(fecha.getDayOfWeek())) {
            System.out.println(club.getNombre() + " no atiende ese día. Atiende: " + club.diasAtencionTexto() + ".");
            return;
        }
        List<Cancha> canchas = gestorCancha.listarPorClub(club.getId());
        if (canchas.isEmpty()) {
            System.out.println("El club no tiene canchas cargadas.");
            return;
        }
        System.out.println("\nTurnos de una hora libres en " + club.getNombre() + " el " + fecha + ":");
        for (Cancha cancha : canchas) {
            List<LocalTime> libres = gestorReserva.horariosLibres(cancha, fecha);
            System.out.println(cancha);
            System.out.println("   " + (libres.isEmpty() ? "Sin turnos libres." : libres));
        }
    }

    /**
     * Pide los datos de un turno, valida las reglas, ofrece extras y guarda la reserva.
     */
    private void registrarReserva() throws AccesoDatosException, ElementoNoEncontradoException,
            TurnoNoDisponibleException, CanchaBloqueadaException, MenorSinResponsableException,
            UsuarioSinPermisoException {

        List<Jugador> jugadores = jugadoresHabilitados();
        Jugador jugador;
        if (jugadores.size() == 1) {
            jugador = jugadores.get(0);
            System.out.println("Reserva para: " + jugador.getNombre());
        } else {
            jugador = Consola.elegirDeLista("¿Para quién es la reserva?", jugadores);
        }

        Cancha cancha = Consola.elegirDeLista("Canchas:", canchasParaReservar());
        LocalDate fecha = leerFechaFutura("Fecha (AAAA-MM-DD): ");
        LocalTime hora = Consola.leerHora("Hora de inicio (HH:MM): ");
        int horas = Consola.leerEnteroPositivo("Duración en horas: ");
        TipoJuego tipo = Consola.elegirEnum("Tipo de juego:", TipoJuego.values());

        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        // Primero se validan todas las reglas; recién después se piden los extras
        Reserva r = gestorReserva.prepararReserva(usuario, cancha, jugador, inicio, inicio.plusHours(horas), tipo);
        agregarExtras(r);
        gestorReserva.guardarEnBD(r);
        mostrarResumen(r, horas);
    }

    /**
     * Muestra las reservas activas que el usuario puede cancelar y cancela la elegida.
     */
    private void cancelarReserva() throws AccesoDatosException, ElementoNoEncontradoException,
            UsuarioSinPermisoException {
        Reserva r = Consola.elegirDeLista("Reservas activas:", reservasActivas());
        double cargo = gestorReserva.cancelar(usuario, r.getId());
        if (cargo == 0) {
            System.out.println("Reserva cancelada sin cargo.");
        } else {
            String titular = r.getTitular() != null ? r.getTitular().getNombre() : "el titular";
            System.out.println("Cancelación tardía. Cargo: $" + cargo + ". Queda registrada para " + titular + ".");
        }
    }

    /**
     * Muestra las reservas que el usuario puede ver, con su costo.
     */
    private void verReservas() throws AccesoDatosException {
        List<Reserva> reservas = gestorReserva.listarVisibles(usuario);
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas.");
            return;
        }
        for (Reserva r : reservas) {
            System.out.println(r + ", costo $" + r.calcularCosto());
        }
    }

    // ------------------------------------------------------------------ auxiliares

    /**
     * @return las reservas activas que el usuario puede gestionar
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    protected List<Reserva> reservasActivas() throws AccesoDatosException {
        List<Reserva> activas = new ArrayList<>();
        for (Reserva r : gestorReserva.listarVisibles(usuario)) {
            if (r.getEstado() == EstadoReserva.ACTIVA) {
                activas.add(r);
            }
        }
        return activas;
    }

    /**
     * @param mensaje texto que se muestra antes de leer
     * @return una fecha de hoy en adelante
     */
    protected LocalDate leerFechaFutura(String mensaje) {
        while (true) {
            LocalDate fecha = Consola.leerFecha(mensaje);
            if (!fecha.isBefore(LocalDate.now())) {
                return fecha;
            }
            System.out.println("La fecha no puede ser anterior a hoy.");
        }
    }

    /**
     * Ofrece extras en un bucle hasta que el usuario elige terminar.
     *
     * @param reserva reserva a la que se agregan
     */
    private void agregarExtras(Reserva reserva) {
        int opcion;
        do {
            System.out.println("Agregar extra: 1. Raqueta  2. Pelotas  3. Iluminación  4. Clase con profesor  0. Terminar");
            opcion = Consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> reserva.agregarExtra(new Raqueta(0, PRECIO_RAQUETA, reserva,
                        Consola.leerEnteroPositivo("Cantidad de raquetas: ")));
                case 2 -> reserva.agregarExtra(new Pelotas(0, PRECIO_TUBO_PELOTAS, reserva,
                        Consola.leerEnteroPositivo("Cantidad de tubos: ")));
                case 3 -> {
                    if (reserva.getCancha().isTieneLuz()) {
                        reserva.agregarExtra(new Iluminacion(0, PRECIO_HORA_LUZ, reserva));
                    } else {
                        System.out.println("Esa cancha no tiene iluminación.");
                    }
                }
                case 4 -> reserva.agregarExtra(new ClaseConProfesor(0, PRECIO_HORA_CLASE, reserva,
                        Consola.leerTexto("Profesor: "), Consola.leerEnteroPositivo("Duración de la clase en minutos: ")));
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    /**
     * Muestra el detalle de una reserva recién guardada, con el costo de cada extra.
     *
     * @param r reserva guardada
     * @param horas duración del turno
     */
    private void mostrarResumen(Reserva r, int horas) {
        System.out.println("\n==========================================");
        System.out.println("   ¡RESERVA REGISTRADA CON ÉXITO! (#" + r.getId() + ")");
        System.out.println("==========================================");
        System.out.println(" Cancha: " + r.getCancha());
        System.out.println(" Jugador: " + r.getJugador().getNombre());
        if (r.getTitular().getId() != r.getJugador().getId()) {
            System.out.println(" Titular: " + r.getTitular().getNombre());
        }
        System.out.println(" Inicio: " + r.getInicio());
        System.out.println(" Duración: " + horas + " hora(s)");
        System.out.println(" Tipo de juego: " + r.getTipoJuego());
        System.out.println("------------------------------------------");
        System.out.println("  Extras contratados:");
        if (r.getExtras().isEmpty()) {
            System.out.println("  • Sin extras adicionales.");
        } else {
            for (Extra extra : r.getExtras()) {
                System.out.println("  • " + extra.getTipo() + ": $" + extra.calcularCosto());
            }
        }
        System.out.println("------------------------------------------");
        System.out.println("  Costo total de la reserva: $" + r.calcularCosto());
        System.out.println("==========================================\n");
    }
}
