package tenis.modelo;

import tenis.enums.MotivoBloqueo;
import tenis.enums.TipoJuego;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Recepcionista extends Usuario {
    private Club club;
    private static int contadorIdReserva = 1;

    // Constructor
    public Recepcionista(int id, String nombre, String dni, LocalDate fechaNacimiento,
                         String domicilio, String telefono, String email, String username,
                         String passwordHash, Club club) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
        this.club = club;
    }

    @Override
    public List<String> getPermisos() {
        // El recepcionista tiene permisos operativos sobre su club asignado
        return Arrays.asList("GESTIONAR_RESERVAS", "CANCELAR_RESERVAS", "BLOQUEAR_CANCHAS", "CARGAR_JUGADORES");
    }

    // Métodos específicos según el diagrama UML
    public Reserva registrarReserva(Cancha cancha, Jugador jugador, LocalDateTime inicio, LocalDateTime fin, TipoJuego tipoJuego) {
        // 1. Validar que el club esté abierto en ese momento
        if (!this.club.estaAbierto(inicio)) {
            throw new IllegalArgumentException("El club se encuentra cerrado en el horario seleccionado.");
        }

        // 2. Validar que la cancha no esté bloqueada por mantenimiento, torneo o clases
        if (cancha.estaBloqueada(inicio, fin)) {
            throw new IllegalArgumentException("La cancha se encuentra bloqueada en el período seleccionado.");
        }

        // 3. Validar las reglas específicas para jugadores menores de edad
        Jugador titularFinal = jugador;
        if (jugador.esMenor()) {
            if (jugador.getResponsable() == null) {
                throw new IllegalArgumentException("El jugador menor de edad no tiene un responsable asociado.");
            }
            // El titular y responsable que responde por la reserva es el adulto asignado
            titularFinal = jugador.getResponsable();
        }

        // 4. Crear y retornar la nueva reserva (el ID luego será manejado por la BD)
        int idReserva = contadorIdReserva++;
        Reserva nuevaReserva = new Reserva(idReserva, cancha, jugador, titularFinal, inicio, fin, tipoJuego);

        return nuevaReserva;
    }

    public double cancelarReserva(Reserva reserva, LocalDateTime ahora) {
        // Lógica para cancelar la reserva y calcular posibles cargos tardíos
        return reserva.cancelar(ahora);
    }

    public void bloquearCancha(Cancha cancha, MotivoBloqueo motivo, LocalDateTime desde, LocalDateTime hasta, String detalle) {
        int idBloqueo = cancha.getBloqueos().size() + 1; // Generación simple de ID en memoria
        Bloqueo nuevoBloqueo = new Bloqueo(idBloqueo, motivo, desde, hasta, detalle);
        cancha.agregarBloqueo(nuevoBloqueo);
    }

    // Getters y Setters
    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }
}
