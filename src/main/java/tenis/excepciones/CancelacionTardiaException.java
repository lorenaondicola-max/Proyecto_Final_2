package tenis.excepciones;

public class CancelacionTardiaException extends RuntimeException {
    public CancelacionTardiaException(String message) {
        super(message);
    }
}
