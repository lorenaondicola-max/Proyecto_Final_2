package tenis.excepciones;

/**
 * El usuario que inició sesión intentó operar sobre algo que no le corresponde:
 * un jugador sobre la reserva de otro jugador, o un recepcionista sobre una cancha de otro club.
 * Es verificada: toda operación que depende del usuario debe contemplarla.
 */
public class UsuarioSinPermisoException extends Exception {
    public UsuarioSinPermisoException(String message) {
        super(message);
    }
}
