package tenis.gestor;

import tenis.dao.ReservaDAO;
import tenis.interfaces.DAO;
import tenis.modelo.Reserva;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import java.util.List;

public class GestorReserva {

    private final DAO<Reserva> dao = new ReservaDAO();

    public Reserva buscarEnBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    public List<Reserva> listarDesdeBD() throws AccesoDatosException {
        return dao.listarTodos();
    }

    public void guardarEnBD(Reserva r) throws AccesoDatosException {
        dao.insertar(r);
    }

    public void actualizarEnBD(Reserva r) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(r);
    }

    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}
