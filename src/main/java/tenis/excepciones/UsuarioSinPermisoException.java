package tenis.excepciones;

public class UsuarioSinPermisoException extends RuntimeException {
    public UsuarioSinPermisoException(String message) {
        super(message);
    }
}
