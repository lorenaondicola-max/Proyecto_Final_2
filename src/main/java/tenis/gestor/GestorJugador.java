package tenis.gestor;

import tenis.dao.JugadorDAO;
import tenis.enums.Nivel;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Jugador;

import java.util.ArrayList;
import java.util.List;
/**
 * Operaciones sobre los jugadores: alta, baja, modificación, búsqueda y listados.
 */


public class GestorJugador {

    private final DAO<Jugador> dao = new JugadorDAO();

    /**
     * Busca un jugador por su id.
     *
     * @param id identificador del club
     * @return el jugador encontrado
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un club con ese id
     */

    public Jugador buscar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    /**
     * @return todos los jugadores guardados
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public List<Jugador> listarTodos() throws AccesoDatosException {
        return dao.listarTodos();
    }

    /**
     * @return los jugadores que son socios; lista vacía si no hay ninguno
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public List<Jugador> listarSocios() throws AccesoDatosException {
        List<Jugador> socios = new ArrayList<>();
        for (Jugador j : dao.listarTodos()) {
            if (j.isEsSocio()) {
                socios.add(j);
            }
        }
        return socios;
    }

    /**
     * @param nivel nivel de juego buscado
     * @return los jugadores de ese nivel; lista vacía si no hay ninguno
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public List<Jugador> listarPorNivel(Nivel nivel) throws AccesoDatosException {
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador j : dao.listarTodos()) {
            if (j.getNivel() == nivel) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    /**
     * Guarda un jugador nuevo. La base le asigna el id.
     *
     * @param j jugador a guardar
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public void guardar(Jugador j) throws AccesoDatosException {
        dao.insertar(j);
    }

    /**
     * Guarda los cambios de un jugador que ya existe.
     *
     * @param j jugador con los datos nuevos
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un jugador con ese id
     */

    public void actualizar(Jugador j) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(j);
    }

    /**
     * Elimina un jugador.
     *
     * @param id identificador del jugadir a eliminar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un jugador con ese id
     */

    public void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}