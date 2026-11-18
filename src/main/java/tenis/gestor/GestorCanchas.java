package tenis.gestor;


import tenis.dao.BloqueoDAO;
import tenis.dao.CanchaDAO;
import tenis.enums.MotivoBloqueo;
import tenis.enums.Superficie;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Bloqueo;
import tenis.modelo.Cancha;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones sobre las canchas: alta, baja, modificación, búsqueda, listados
 * y bloqueos.
 */

public class GestorCanchas {

    private final DAO<Cancha> dao = new CanchaDAO();
    private final BloqueoDAO bloqueoDAO = new BloqueoDAO();

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

    /**
     * Lista las canchas de un club.
     *
     * @param clubId id del club
     * @return canchas del club; lista vacía si no tiene ninguna
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public List<Cancha> listarPorClub(int clubId) throws AccesoDatosException {
        List<Cancha> resultado = new ArrayList<>();
        for (Cancha c : dao.listarTodos()) {
            if (c.getClub().getId() == clubId) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * Bloquea una cancha durante un período y guarda el bloqueo en la base.
     *
     * @param cancha cancha a bloquear
     * @param motivo mantenimiento, torneo o clases
     * @param desde inicio del bloqueo
     * @param hasta fin del bloqueo
     * @param detalle texto libre con la causa
     * @return el bloqueo creado, con el id asignado por la base
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws DatosInvalidosException si el fin no es posterior al inicio
     */
    public Bloqueo bloquear(Cancha cancha, MotivoBloqueo motivo, LocalDateTime desde,
                            LocalDateTime hasta, String detalle) throws AccesoDatosException {
        if (!hasta.isAfter(desde)) {
            throw new DatosInvalidosException("El bloqueo debe terminar después de empezar");
        }
        Bloqueo bloqueo = new Bloqueo(0, motivo, desde, hasta, detalle);
        bloqueoDAO.insertar(bloqueo, cancha.getId());
        cancha.agregarBloqueo(bloqueo);
        return bloqueo;
    }

    /**
     * Quita un bloqueo de una cancha y lo borra de la base.
     * Primero lo busca entre los bloqueos de esa cancha, para no borrar
     * un bloqueo que pertenece a otra.
     *
     * @param cancha cancha a desbloquear
     * @param idBloqueo id del bloqueo a quitar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si la cancha no tiene un bloqueo con ese id
     */
    public void desbloquear(Cancha cancha, int idBloqueo)
            throws AccesoDatosException, ElementoNoEncontradoException {
        cancha.eliminarBloqueo(idBloqueo);
        bloqueoDAO.eliminar(idBloqueo);
    }
}
