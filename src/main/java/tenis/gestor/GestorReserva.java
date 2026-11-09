package tenis.gestor;

import tenis.dao.ReservaDAO;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Extra;
import tenis.modelo.Reserva;

import java.util.List;

public class GestorReserva {

    private final DAO<Reserva> dao = new ReservaDAO();

    public Reserva buscarEnBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        // Al buscar por ID, el DAO ya se encarga de instanciar la reserva y cargarle sus extras
        return dao.buscarPorId(id);
    }

    public List<Reserva> listarDesdeBD() throws AccesoDatosException {
        // Retorna todas las reservas, cada una con su respectiva lista de extras cargada
        return dao.listarTodos();
    }

    public void guardarEnBD(Reserva r) throws AccesoDatosException {
        // Inserta la reserva y recorre su lista de extras para guardarlos en la tabla secundaria
        dao.insertar(r);
    }

    public void actualizarEnBD(Reserva r) throws AccesoDatosException, ElementoNoEncontradoException {
        // Actualiza los datos de la reserva y sincroniza (borra e reinserta) sus extras
        dao.actualizar(r);
    }

    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        // Borra los extras asociados y luego elimina la reserva principal
        dao.eliminar(id);
    }

    // Agrega un extra a una reserva ya guardada
    public void agregarExtraAReserva(int reservaId, Extra extra) throws AccesoDatosException, ElementoNoEncontradoException {
        // Traemos la reserva de la base de datos
        Reserva reserva = buscarEnBD(reservaId);

        // Sumamos a la lista en memoria
        reserva.agregarExtra(extra);

        // Persistimos el cambio (el DAO actualizará la tabla secundaria)
        actualizarEnBD(reserva);
    }
}
