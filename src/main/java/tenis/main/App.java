package tenis.main;

import tenis.enums.Nivel;
import tenis.enums.Superficie;
import tenis.enums.TipoJuego;
import tenis.excepciones.*;
import tenis.gestor.GestorCanchas;
import tenis.gestor.GestorClub;
import tenis.gestor.GestorJugador;
import tenis.gestor.GestorReserva;
import tenis.modelo.*;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    private static final Scanner sc = new Scanner(System.in);
    private static final GestorClub gestorClub = new GestorClub();
    private static final GestorCanchas gestorCancha = new GestorCanchas();
    private static final GestorJugador gestorJugador = new GestorJugador();
    private static final GestorReserva gestorReserva = new GestorReserva();
    private static final double PRECIO_RAQUETA = 1500.0;
    private static final double PRECIO_TUBO_PELOTAS = 3000.0;
    private static final double PRECIO_HORA_LUZ = 500.0;
    private static final double PRECIO_HORA_CLASE = 15000.0;
    private static Recepcionista recepcionista;

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> cargarDatos();
                    case 2 -> listarCanchas();
                    case 3 -> altaJugador();
                    case 4 -> listarJugadores();
                    case 5 -> registrarReserva();
                    case 6 -> listarReservas();
                    case 7 -> cancelarReserva();
                    case 0 -> System.out.println("Hasta luego.");
                    default -> System.out.println("Opción inválida.");
                }
            } catch (TurnoNoDisponibleException | CanchaBloqueadaException | MenorSinResponsableException e) {
            System.out.println("Operación rechazada: " + e.getMessage());
            } catch (ElementoNoEncontradoException e) {
                System.out.println("No encontrado: " + e.getMessage());
            } catch (AccesoDatosException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (DatosInvalidosException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n=== SISTEMA DE TURNOS DE TENIS ===");
        System.out.println("1. Cargar datos de prueba");
        System.out.println("2. Listar canchas");
        System.out.println("3. Registrar nuevo jugador");
        System.out.println("4. Listar jugadores");
        System.out.println("5. Registrar reserva");
        System.out.println("6. Listar reservas");
        System.out.println("7. Cancelar reserva");
        System.out.println("0. Salir");
    }

    private static void cargarDatos() throws AccesoDatosException {
            if (recepcionista != null) {
                System.out.println("Los datos ya están cargados.");
                return;
            }

            List<Club> clubes = gestorClub.listarDesdeBD();
            if (clubes.isEmpty()) {
                cargarDatosDePrueba();
                clubes = gestorClub.listarDesdeBD();
                System.out.println("Se cargaron los datos de prueba.");
            } else {
                System.out.println("La base ya tenía datos: se usan esos.");
            }

            Club club = clubes.get(0);
            // Los días de atención no se guardan en la base (limitación conocida): se cargan acá
            club.setDiasAtencion(new ArrayList<>(List.of(DayOfWeek.values())));
            recepcionista = new Recepcionista(0, "Ana Gómez", "28111222", LocalDate.of(1990, 3, 4),
                    "Calle Real 456", "1133221100", "ana@email.com", "agomez", "clave123", club);
    }

    private static void cargarDatosDePrueba() throws AccesoDatosException {
        System.out.println("=== CARGANDO DATOS DE PRUEBA DEL SISTEMA DE TENIS ===");

        Tarifa tarifa = new Tarifa(5000.0, 10.0, 20.0, 15.0, 20.0);

        // 1. CREAR Y GUARDAR UN CLUB
        System.out.println("\n--- 1. Registrando Club ---");
        Club club = new Club(
                0,
                "Tenis Club Central",
                "Av. Libertador 1234",
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                LocalTime.of(18, 0),
                LocalTime.of(21, 0),
                new ArrayList<>(List.of(DayOfWeek.values())),
                24,
                50.0,
                tarifa
        );
        gestorClub.guardarEnBD(club);
        System.out.println("¡Club guardado con éxito: " + club.getNombre() + "!");

        // 2. CREAR Y GUARDAR JUGADORES
        System.out.println("\n--- 2. Registrando Jugadores ---");
        Jugador adulto = new Jugador(
                0,
                "Carlos Pérez",
                "30123456",
                LocalDate.of(1985, 5, 12),
                "Calle Falsa 123",
                "1122334455",
                "carlos@email.com",
                "cperez",
                "hash123",
                true,
                Nivel.AVANZADO,
                "Singles A"
        );
        gestorJugador.guardar(adulto);
        System.out.println("Jugador adulto guardado: " + adulto.getNombre());

        Jugador menor = new Jugador(
                0,
                "Lucas Pérez",
                "45123456",
                LocalDate.of(2010, 8, 20),
                "Calle Falsa 123",
                "1155443322",
                "lucas@email.com",
                false,
                Nivel.INTERMEDIO,
                "Juvenil",
                adulto
        );
        gestorJugador.guardar(menor);
        System.out.println("Jugador menor guardado con responsable: " + menor.getNombre());

        Jugador adulto2 = new Jugador(0,
                "Juan Estrugamou",
                "30886766", LocalDate.of(1984, 3, 15),
                "9 de Julio 3111",
                "2235394052",
                "jmestrugamou@gmail.com",
                "jmestrugamou",
                "pass1234",
                false,
                Nivel.PRINCIPIANTE,
                "Singles A"
        );
        gestorJugador.guardar(adulto2);
        System.out.println("Jugador adulto guardado: " + adulto2.getNombre());

        // 3. CREAR Y GUARDAR CANCHAS
        System.out.println("\n--- 3. Registrando Cancha ---");
        Cancha cancha = new Cancha(
                0,
                1,
                Superficie.POLVO_LADRILLO,
                true,
                club
        );
        gestorCancha.guardar(cancha);
        System.out.println("Cancha N°" + cancha.getNumero() + " (" + cancha.getSuperficie() + ") guardada con éxito.");

        Cancha cancha2 = new Cancha(
                0,
                2,
                Superficie.CEMENTO,
                false,
                club
        );
        gestorCancha.guardar(cancha2);
        System.out.println("Cancha N°" + cancha2.getNumero() + " (" + cancha2.getSuperficie() + ") guardada con éxito.");

        Cancha cancha3 = new Cancha(
                0,
                3,
                Superficie.CEMENTO,
                true,
                club
        );
        gestorCancha.guardar(cancha3);
        System.out.println("Cancha N°" + cancha3.getNumero() + " (" + cancha3.getSuperficie() + ") guardada con éxito.");

        System.out.println("\n=== ¡CARGA DE DATOS FINALIZADA CON ÉXITO! ===");
    }

    private static void listarCanchas() throws AccesoDatosException {
        List<Cancha> canchas = gestorCancha.listarTodos();
        if (canchas.isEmpty()) {
            System.out.println("No hay canchas cargadas.");
            return;
        }
        for (Cancha c : canchas) {
            System.out.println(c);
        }
    }

    private static void altaJugador() throws AccesoDatosException, ElementoNoEncontradoException,
            MenorSinResponsableException {
        String nombre = leerTexto("Nombre y apellido: ");
        String dni = leerTexto("DNI: ");
        for (Jugador j : gestorJugador.listarTodos()) {
            if (j.getDni().equals(dni)) {
                throw new DatosInvalidosException("Ya existe un jugador con DNI " + dni);
            }
        }
        LocalDate nacimiento = leerFecha("Fecha de nacimiento (AAAA-MM-DD): ");
        if (nacimiento.isAfter(LocalDate.now())) {
            throw new DatosInvalidosException("La fecha de nacimiento no puede ser futura");
        }
        String domicilio = leerTexto("Domicilio: ");
        String telefono = leerTexto("Teléfono: ");
        String email = leerTexto("Email: ");
        boolean esSocio = leerEntero("¿Es socio? 1 = Sí, 2 = No: ") == 1;
        Nivel nivel = leerNivel();
        String categoria = leerTexto("Categoría: ");

        Jugador nuevo;
        boolean esMenor = Period.between(nacimiento, LocalDate.now()).getYears() < 18;
        if (esMenor) {
            System.out.println("El jugador es menor de edad: hay que asignarle un responsable.");
            listarJugadores();
            Jugador responsable = gestorJugador.buscar(leerEntero("Id del responsable: "));
            if (responsable.esMenor()) {
                throw new MenorSinResponsableException("El responsable debe ser mayor de edad");
            }
            nuevo = new Jugador(nombre, dni, nacimiento, domicilio, telefono, email,
                    esSocio, nivel, categoria, responsable);
        } else {
            String usuario = leerTexto("Usuario: ");
            String clave = leerTexto("Clave: ");
            nuevo = new Jugador(nombre, dni, nacimiento, domicilio, telefono, email,
                    usuario, clave, esSocio, nivel, categoria);
        }

        gestorJugador.guardar(nuevo);
        System.out.println("Jugador registrado: " + nuevo);
    }

    private static void listarJugadores() throws AccesoDatosException {
        List<Jugador> jugadores = gestorJugador.listarTodos();
        if (jugadores.isEmpty()) {
            System.out.println("No hay jugadores cargados.");
            return;
        }
        for (Jugador j : jugadores) {
            System.out.println(j);
        }
    }

    private static void registrarReserva() throws AccesoDatosException, ElementoNoEncontradoException,
            TurnoNoDisponibleException, CanchaBloqueadaException, MenorSinResponsableException {
        if (recepcionista == null) {
            System.out.println("Primero cargá los datos (opción 1).");
            return;
        }
        listarCanchas();
        Cancha cancha = gestorCancha.buscar(leerEntero("Id de cancha: "));
        listarJugadores();
        Jugador jugador = gestorJugador.buscar(leerEntero("Id de jugador: "));
        LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
        LocalTime hora = leerHora("Hora de inicio (HH:MM): ");
        int horas = leerEntero("Duración en horas: ");
        TipoJuego tipo = TipoJuego.SINGLES;
        if (leerEntero("1 = Singles, 2 = Dobles: ") == 2) {
            tipo = TipoJuego.DOBLES;
        }

        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        Reserva r = recepcionista.registrarReserva(cancha, jugador, inicio, inicio.plusHours(horas), tipo);
        agregarExtras(r);
        gestorReserva.guardarEnBD(r);
        System.out.println("Reserva " + r.getId() + " registrada.");
        for (Extra extra : r.getExtras()) {
            System.out.println("  " + extra.getTipo() + ": $" + extra.calcularCosto());
        }
        System.out.println("  Total: $" + r.calcularCosto());
    }

    private static void agregarExtras(Reserva reserva) {
        int opcion;
        do {
            System.out.println("Agregar extra: 1. Raqueta  2. Pelotas  3. Iluminación  4. Clase con profesor  0. Terminar");
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> reserva.agregarExtra(new Raqueta(0, PRECIO_RAQUETA, reserva,
                        leerCantidad("Cantidad de raquetas: ")));
                case 2 -> reserva.agregarExtra(new Pelotas(0, PRECIO_TUBO_PELOTAS, reserva,
                        leerCantidad("Cantidad de tubos: ")));
                case 3 -> {
                    if (reserva.getCancha().isTieneLuz()) {
                        reserva.agregarExtra(new Iluminacion(0, PRECIO_HORA_LUZ, reserva));
                    } else {
                        System.out.println("Esa cancha no tiene iluminación.");
                    }
                }
                case 4 -> reserva.agregarExtra(new ClaseConProfesor(0, PRECIO_HORA_CLASE, reserva,
                        leerTexto("Profesor: "), leerCantidad("Duración de la clase en minutos: ")));
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void listarReservas() throws AccesoDatosException {
        List<Reserva> reservas = gestorReserva.listarDesdeBD();
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas.");
            return;
        }
        for (Reserva r : reservas) {
            System.out.println(r);
        }
    }

    private static void cancelarReserva() throws AccesoDatosException, ElementoNoEncontradoException {
        Reserva r = gestorReserva.buscarEnBD(leerEntero("Id de la reserva a cancelar: "));
        double cargo = r.cancelar(LocalDateTime.now());
        gestorReserva.actualizarEnBD(r);
        if (cargo == 0) {
            System.out.println("Reserva cancelada sin cargo.");
        } else {
            System.out.println("Cancelación tardía. Cargo: $" + cargo);
        }
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número.");
            }
        }
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return LocalDate.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Ejemplo: 2026-11-20");
            }
        }
    }

    private static LocalTime leerHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return LocalTime.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Ejemplo: 10:00");
            }
        }
    }

    private static String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = sc.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este dato es obligatorio.");
        }
    }

    private static Nivel leerNivel() {
        Nivel[] niveles = Nivel.values();
        for (int i = 0; i < niveles.length; i++) {
            System.out.println((i + 1) + ". " + niveles[i]);
        }
        int n = leerEntero("Nivel: ");
        if (n < 1 || n > niveles.length) {
            throw new DatosInvalidosException("Nivel inexistente: " + n);
        }
        return niveles[n - 1];
    }

    private static int leerCantidad(String mensaje) {
        while (true) {
            int n = leerEntero(mensaje);
            if (n > 0) {
                return n;
            }
            System.out.println("Tiene que ser mayor que cero.");
        }
    }
}