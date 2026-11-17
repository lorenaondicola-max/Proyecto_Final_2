package tenis.interfaces;

import java.util.List;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
/**
 * Contrato de persistencia común a todas las entidades que se guardan en la base de datos.
 * Define las cinco operaciones básicas y qué puede fallar en cada una.
 * Quien la usa no necesita saber que detrás hay MySQL.
 *
 * @param <T> tipo de entidad que se guarda: Club, Cancha, Jugador o Reserva
 */

public interface DAO<T> {

    /**
     * Guarda una entidad nueva y le asigna el id que genera la base.
     *
     * @param entidad entidad a guardar
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    void insertar(T entidad) throws AccesoDatosException;
    /**
     * @return todas las entidades guardadas; lista vacía si no hay ninguna
     * @throws AccesoDatosException si falla la comunicación con la base
     */

    List<T> listarTodos() throws AccesoDatosException;

    /**
     * @param id identificador de la entidad buscada
     * @return la entidad con ese id
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una entidad con ese id
     */

    T buscarPorId(int id) throws AccesoDatosException, ElementoNoEncontradoException;

    /**
     * Guarda los cambios de una entidad que ya existe en la base.
     *
     * @param entidad entidad con los datos nuevos; se identifica por su id
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una entidad con ese id
     */

    void actualizar(T entidad) throws AccesoDatosException, ElementoNoEncontradoException;

    /**
     * @param id identificador de la entidad a eliminar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe una entidad con ese id
     */
    
    void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException;
}
