package tenis.interfaces;

import java.util.List;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;

public interface DAO<T> {
    void insertar(T entidad) throws AccesoDatosException;
    List<T> listarTodos() throws AccesoDatosException;
    T buscarPorId(int id) throws AccesoDatosException, ElementoNoEncontradoException;
    void actualizar(T entidad) throws AccesoDatosException, ElementoNoEncontradoException;
    void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException;
}
