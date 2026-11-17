package tenis.gestor;

import tenis.dao.ReservaDAO;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.excepciones.TurnoNoDisponibleException;
import tenis.interfaces.DAO;
import tenis.modelo.Extra;
import tenis.modelo.Reserva;

import java.util.List;

/**
 * Operaciones sobre las reservas.
 * Antes de guardar una reserva nueva verifica que la cancha esté libre en ese horario.
 */

public class GestorReserva {

    private final DAO<Reserva> dao = new ReservaDAO();

    /**
     * Busca una reserva por su id.
     *
     * @param id identificador de la reserva
     * @return la reserva encontrada
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */

    public Reserva buscarEnBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        // Al buscar por ID, el DAO ya se encarga de instanciar la reserva y cargarle sus extras
        return dao.buscarPorId(id);
    }

    /**
     * @return todos los reservas guardadas
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public List<Reserva> listarDesdeBD() throws AccesoDatosException {
        // Retorna todas las reservas, cada una con su respectiva lista de extras cargada
        return dao.listarTodos();
    }

    /**
     * Guarda una reserva nuevo. La base le asigna el id.
     *
     * @param r reserva a guardar
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    public void guardarEnBD(Reserva r) throws AccesoDatosException, TurnoNoDisponibleException {
        // Inserta la reserva y recorre su lista de extras para guardarlos en la tabla secundaria
        List<Reserva> existentes = dao.listarTodos();
        for (Reserva otra : existentes){
            if (otra.getCancha().getId() == r.getCancha().getId() && otra.seSuperpone(r.getInicio(), r.getFin())){
                throw new TurnoNoDisponibleException("La cancha ya está reservada en ese horario");
            }
        }
        dao.insertar(r);
    }

    /**
     * Guarda los cambios de una reserva que ya existe.
     *
     * @param r reserva con los datos nuevos
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */

    public void actualizarEnBD(Reserva r) throws AccesoDatosException, ElementoNoEncontradoException {
        // Actualiza los datos de la reserva y sincroniza (borra e reinserta) sus extras
        dao.actualizar(r);
    }

    /**
     * Elimina una reserva.
     *
     * @param id identificador de la reserva a eliminar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */

    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        // Borra los extras asociados y luego elimina la reserva principal
        dao.eliminar(id);
    }

    /**
     * Agrega un extra a una reserva ya guardada.
     * Lee la reserva de la base, le suma el extra y guarda el cambio.
     *
     * @param reservaId identificador de la reserva
     * @param extra extra a agregar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una reserva con ese id
     */

    public void agregarExtraAReserva(int reservaId, Extra extra) throws AccesoDatosException, ElementoNoEncontradoException {
        // Traemos la reserva de la base de datos
        Reserva reserva = buscarEnBD(reservaId);

        // Sumamos a la lista en memoria
        reserva.agregarExtra(extra);

        // Persistimos el cambio (el DAO actualizará la tabla secundaria)
        actualizarEnBD(reserva);
    }
}
