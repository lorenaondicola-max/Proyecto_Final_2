package tenis.excepciones;


/**
 * La cancha tiene un bloqueo (mantenimiento, torneo o clases) en el período pedido.
 * Es verificada: quien registra una reserva debe contemplarla.
 */

public class CanchaBloqueadaException extends Exception {
    public CanchaBloqueadaException(String message) {
        super(message);
    }
}
