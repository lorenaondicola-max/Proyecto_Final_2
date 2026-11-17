package tenis.excepciones;
/**
 * Se buscó, se quiso modificar o se quiso eliminar un elemento que no existe.
 * Es verificada: es una situación previsible en la operación normal.
 */
public class ElementoNoEncontradoException extends Exception {
    public ElementoNoEncontradoException(String message) {
        super(message);
    }
}
