package tenis.excepciones;
/**
 * Falla al comunicarse con la base de datos.
 * Envuelve la SQLException original como causa, para que el resto del sistema
 * no dependa de JDBC. Es verificada: quien llama a un DAO debe contemplarla.
 */
public class AccesoDatosException extends Exception {
    public AccesoDatosException(String message, Throwable cause) {
        super(message, cause);
    }
}
