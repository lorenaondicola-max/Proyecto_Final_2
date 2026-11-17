package tenis.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto único de conexión a la base de datos MySQL del sistema.
 * Reúne la dirección de la base, el usuario y la clave, para que los DAO no los repitan.
 */

public class ConexionDB {

    private static final String URL = "jdbc:mysql://localhost:3306/gestion_turnos_tenis_db";
    private static final String USUARIO = "root";
    private static final String CLAVE = "root";

    /**
     * Constructor privado: la clase solo ofrece un método estático y no se instancia.
     */

    private ConexionDB() {
    }

    /**
     * Abre una conexión nueva con la base de datos.
     * Quien la pide debe cerrarla; los DAO lo hacen con try-with-resources.
     *
     * @return conexión abierta
     * @throws SQLException si no se puede conectar (servidor apagado, clave incorrecta, base inexistente)
     */
    
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
