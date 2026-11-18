package tenis.modelo;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Usuario que atiende a los jugadores en un club.
 * Pertenece a un único club y solo puede operar sobre las canchas y reservas de ese club:
 * esa regla está en {@link #puedeGestionarReserva} y {@link #puedeGestionarCancha}.
 * Las operaciones en sí (reservar, cancelar, bloquear) las hacen los gestores.
 */
public class Recepcionista extends Usuario {
    private Club club;

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
     * @return true si la cancha es de su club, sea cual sea el jugador
     */
    @Override
    public boolean puedeGestionarReserva(Cancha cancha, Jugador jugador) {
        return esDeSuClub(cancha);
    }

    /**
     * @return true si la cancha es de su club
     */
    @Override
    public boolean puedeGestionarCancha(Cancha cancha) {
        return esDeSuClub(cancha);
    }

    /**
     * @param cancha cancha a consultar
     * @return true si la cancha pertenece al club del recepcionista
     */
    private boolean esDeSuClub(Cancha cancha) {
        return club != null && cancha.getClub().getId() == club.getId();
    }

    // Getters y Setters
    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    @Override
    public String toString() {
        return "Recepcionista id " + id + ": " + nombre
                + (club != null ? ", " + club.getNombre() : ", sin club asignado");
    }
}
