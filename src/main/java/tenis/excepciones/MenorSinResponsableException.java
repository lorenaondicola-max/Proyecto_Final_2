package tenis.excepciones;

/**
 * Un jugador menor de 18 años no tiene un responsable adulto asociado.
 * Es verificada: puede ocurrir al dar de alta un jugador o al reservar para él.
 */

public class MenorSinResponsableException extends Exception {
    public MenorSinResponsableException(String message) {
        super(message);
    }
}
