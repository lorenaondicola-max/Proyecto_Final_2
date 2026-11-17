package tenis.gestor;

import tenis.dao.ClubDAO;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Club;

import java.util.List;

/**
 * Operaciones sobre los clubes. Accede a la base a través de la interfaz {@code DAO},
 * sin depender de cómo está implementada.
 */

public class GestorClub {

    private final DAO<Club> dao = new ClubDAO();

    /**
     * Busca un club por su id.
     *
     * @param id identificador del club
     * @return el club encontrado
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un club con ese id
     */

    public Club buscarEnBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    /**
     * @return todos los clubes guardados
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public List<Club> listarDesdeBD() throws AccesoDatosException {
        return dao.listarTodos();
    }

    /**
     * Guarda un club nuevo. La base le asigna el id.
     *
     * @param c club a guardar
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public void guardarEnBD(Club c) throws AccesoDatosException {
        dao.insertar(c);
    }

    /**
     * Guarda los cambios de un club que ya existe.
     *
     * @param c club con los datos nuevos
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un club con ese id
     */

    public void actualizarEnBD(Club c) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(c);
    }

    /**
     * Elimina un club.
     *
     * @param id identificador del club a eliminar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un club con ese id
     */

    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}
