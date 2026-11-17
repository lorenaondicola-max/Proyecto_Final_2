package tenis.excepciones;

/**
 * Se intentó usar un dato que viola una regla del sistema: un precio negativo,
 * un id repetido, cancelar una reserva que no está activa.
 * No es verificada, porque señala un error de quien programa la llamada
 * y no una situación de la operación normal.
 */

public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String message) {
        super(message);
    }
}
