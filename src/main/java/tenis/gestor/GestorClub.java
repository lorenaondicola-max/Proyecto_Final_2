package tenis.gestor;

import tenis.dao.ClubDAO;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Club;

import java.util.List;

public class GestorClub {

    private final DAO<Club> dao = new ClubDAO();

    public Club buscarEnBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    public List<Club> listarDesdeBD() throws AccesoDatosException {
        return dao.listarTodos();
    }

    public void guardarEnBD(Club c) throws AccesoDatosException {
        dao.insertar(c);
    }

    public void actualizarEnBD(Club c) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(c);
    }

    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}
