package tenis.excepciones;

/**
 * El usuario o la contraseña ingresados no corresponden a ningún usuario con acceso al sistema.
 * Es verificada: quien intenta iniciar sesión debe contemplar que los datos sean incorrectos.
 */
public class CredencialesInvalidasException extends Exception {
    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
