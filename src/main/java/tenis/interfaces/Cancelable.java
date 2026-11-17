package tenis.interfaces;

import java.time.LocalDateTime;

/**
 * Capacidad de cancelarse y de informar cuánto cuesta hacerlo.
 */

public interface Cancelable {

    /**
     * Cancela el elemento.
     *
     * @param ahora momento en que se pide la cancelación
     * @return el cargo por cancelar; 0 si no corresponde ninguno
     */

    double cancelar(LocalDateTime ahora);
}
