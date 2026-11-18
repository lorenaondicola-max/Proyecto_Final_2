package tenis.gestor;

import tenis.dao.EmpleadoDAO;
import tenis.dao.JugadorDAO;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.CredencialesInvalidasException;
import tenis.excepciones.DatosInvalidosException;
import tenis.interfaces.DAO;
import tenis.modelo.Jugador;
import tenis.modelo.Recepcionista;
import tenis.modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Inicio de sesión y alta de empleados.
 * Los usuarios con acceso al sistema están en dos tablas: los jugadores adultos en
 * {@code jugadores}, y los administradores y recepcionistas en {@code empleados}.
 * Este gestor los consulta juntos, como una sola lista de {@link Usuario}.
 */
public class GestorUsuarios {

    private final DAO<Usuario> empleadoDAO = new EmpleadoDAO();
    private final DAO<Jugador> jugadorDAO = new JugadorDAO();

    /**
     * Busca al usuario que tiene ese nombre de usuario y esa clave.
     * Los menores no tienen credenciales, así que nunca pueden iniciar sesión.
     *
     * @param username nombre de usuario ingresado
     * @param password clave ingresada
     * @return el usuario que inició sesión: un administrador, un recepcionista o un jugador
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws CredencialesInvalidasException si ningún usuario coincide con esos datos
     */
    public Usuario login(String username, String password)
            throws AccesoDatosException, CredencialesInvalidasException {
        for (Usuario u : listarConAcceso()) {
            if (u.tieneCredenciales() && u.getUsername().equals(username)
                    && u.getPasswordHash().equals(password)) {
                return u;
            }
        }
        // El mensaje no dice cuál de los dos datos está mal, para no dar pistas
        throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
    }

    /**
     * Indica si un nombre de usuario todavía no lo usa nadie, ni jugador ni empleado.
     *
     * @param username nombre de usuario a consultar
     * @return true si está libre
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public boolean usernameDisponible(String username) throws AccesoDatosException {
        for (Usuario u : listarConAcceso()) {
            if (username.equals(u.getUsername())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Da de alta un recepcionista, siempre que su DNI y su nombre de usuario no estén en uso.
     *
     * @param r recepcionista a guardar, con su club asignado
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws DatosInvalidosException si el DNI o el nombre de usuario ya están registrados
     */
    public void registrarRecepcionista(Recepcionista r) throws AccesoDatosException {
        for (Usuario u : empleadoDAO.listarTodos()) {
            if (u.getDni().equals(r.getDni())) {
                throw new DatosInvalidosException("Ya existe un empleado con DNI " + r.getDni());
            }
        }
        if (!usernameDisponible(r.getUsername())) {
            throw new DatosInvalidosException("El nombre de usuario " + r.getUsername() + " ya está en uso");
        }
        empleadoDAO.insertar(r);
    }

    /**
     * @return todos los empleados y todos los jugadores, en una sola lista
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    private List<Usuario> listarConAcceso() throws AccesoDatosException {
        List<Usuario> todos = new ArrayList<>(empleadoDAO.listarTodos());
        todos.addAll(jugadorDAO.listarTodos());
        return todos;
    }
}
