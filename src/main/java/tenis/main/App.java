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
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DEL SISTEMA DE TENIS ===");

        // Instanciamos los gestores
        GestorClub gestorClub = new GestorClub();
        GestorJugador gestorJugador = new GestorJugador();
        GestorCanchas gestorCancha = new GestorCanchas();
        GestorReserva gestorReserva = new GestorReserva();

        Tarifa tarifa = new Tarifa(5000.0, 10.0, 20.0, 15.0, 20.0);

        try {
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

            // El recepcionista atiende en el club; no se persiste, vive en memoria
            Recepcionista recepcionista = new Recepcionista(
                    0,
                    "Ana Gómez",
                    "28111222",
                    LocalDate.of(1990, 3, 4),
                    "Calle Real 456",
                    "1133221100",
                    "ana@email.com",
                    "agomez",
                    "clave123",
                    club
            );

            // 4. CREAR Y GUARDAR RESERVA CON EXTRAS
            System.out.println("\n--- 4. Registrando Reserva ---");
            LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime fin = inicio.plusHours(1);

            Reserva reserva = recepcionista.registrarReserva(cancha, menor, inicio, fin, TipoJuego.SINGLES);

            // Agregamos un extra (ej: Alquiler de Raqueta o Pelotas)
            Extra extraReserva = new Raqueta(1, 1500.0, reserva, 1);
            reserva.agregarExtra(extraReserva);
            gestorReserva.guardarEnBD(reserva);
            System.out.println("¡Reserva ID " + reserva.getId() + " guardada con sus extras!");

            //Probamos registrar una reserva repetida para ver el rechazo
            try {
                Reserva repetida = recepcionista.registrarReserva(cancha, adulto, inicio, fin, TipoJuego.SINGLES);
                gestorReserva.guardarEnBD(repetida);
            } catch (TurnoNoDisponibleException e){
                System.out.println("Reserva rechazada: " + e.getMessage());
            }

            // 5. PROBAR LECTURA (BUSCAR DE LA BASE DE DATOS)
            System.out.println("\n--- 5. Consultando Reserva desde la BD ---");
            Reserva reservaRecuperada = gestorReserva.buscarEnBD(reserva.getId());
            System.out.println("Reserva encontrada en BD:");
            System.out.println(" - Cancha N°: " + reservaRecuperada.getCancha().getNumero());
            System.out.println(" - Jugador: " + reservaRecuperada.getJugador().getNombre());
            System.out.println(" - Titular: " + reservaRecuperada.getTitular().getNombre());
            System.out.println(" - Cantidad de Extras: " + reservaRecuperada.getExtras().size());
            System.out.println(" - Costo Total calculado: $" + reservaRecuperada.calcularCosto());

            System.out.println("\n=== ¡PRUEBA FINALIZADA CON ÉXITO! ===");

        } catch (TurnoNoDisponibleException | CanchaBloqueadaException | MenorSinResponsableException e) {
            System.out.println("No se pudo reservar: " + e.getMessage());
        } catch (AccesoDatosException | ElementoNoEncontradoException e) {
            System.err.println("❌ Error en la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}