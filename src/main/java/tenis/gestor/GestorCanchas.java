package tenis.gestor;


import tenis.dao.CanchaDAO;
import tenis.enums.Superficie;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Cancha;

import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones sobre las canchas: alta, baja, modificación, búsqueda y listados.
 */

public class GestorCanchas {

    private final DAO<Cancha> dao = new CanchaDAO();

    public Cancha buscar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    public List<Cancha> listarTodos() throws AccesoDatosException {
        return dao.listarTodos();
    }

    /**
     * Lista las canchas de una superficie. Trae todas las canchas y se queda con las que coinciden.
     *
     * @param superficie superficie buscada
     * @return canchas de esa superficie; lista vacía si no hay ninguna
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public List<Cancha> listarPorSuperficie(Superficie superficie) throws AccesoDatosException {
        List<Cancha> resultado = new ArrayList<>();
        for (Cancha c : dao.listarTodos()) {
            if (c.getSuperficie() == superficie) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * Guarda una cancha nueva. La base le asigna el id.
     *
     * @param c cancha a guardar
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public void guardar(Cancha c) throws AccesoDatosException {
        dao.insertar(c);
    }

    /**
     * Guarda los cambios de una cancha que ya existe.
     *
     * @param c cancha con los datos nuevos
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un cancha con ese id
     */

    public void actualizar(Cancha c) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(c);
    }

    /**
     * Elimina la cancha.
     *
     * @param id identificador de la cancha a eliminar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una cancha con ese id
     */

    public void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}