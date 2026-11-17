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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                    case 3 -> listarJugadores();
                    case 4 -> registrarReserva();
                    case 5 -> listarReservas();
                    case 6 -> cancelarReserva();
                    case 0 -> System.out.println("Hasta luego.");
                    default -> System.out.println("Opción inválida.");
                }
            } catch (TurnoNoDisponibleException | CanchaBloqueadaException | MenorSinResponsableException e) {
            System.out.println("No se pudo reservar: " + e.getMessage());
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
        System.out.println("1. Cargar datos");
        System.out.println("2. Listar canchas");
        System.out.println("3. Listar jugadores");
        System.out.println("4. Registrar reserva");
        System.out.println("5. Listar reservas");
        System.out.println("6. Cancelar reserva");
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

        // 3. CREAR Y GUARDAR CANCHA
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
        gestorReserva.guardarEnBD(r);
        System.out.println("Reserva " + r.getId() + " registrada. Costo: $" + r.calcularCosto());
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
}