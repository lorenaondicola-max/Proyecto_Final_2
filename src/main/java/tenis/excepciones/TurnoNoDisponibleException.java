package tenis.excepciones;

/**
 * El turno pedido no puede reservarse: el club está cerrado en ese horario
 * o la cancha ya tiene otra reserva que se superpone.
 * Es verificada: quien registra una reserva debe contemplarla.
 */

public class TurnoNoDisponibleException extends Exception {
    public TurnoNoDisponibleException(String message) {
        super(message);
    }
}
