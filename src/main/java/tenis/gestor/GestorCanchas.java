package tenis.gestor;

import tenis.dao.CanchaDAO;
import tenis.enums.Superficie;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Cancha;

import java.util.ArrayList;
import java.util.List;

public class GestorCanchas {

    // Lista en memoria para operaciones rápidas
    private List<Cancha> elementos = new ArrayList<>();

    // Conexión con la capa de persistencia MySQL
    private final DAO<Cancha> dao = new CanchaDAO();

    public Cancha buscar(int id) throws ElementoNoEncontradoException {
        for (Cancha c : elementos) {
            if (c.getId() == id) {
                return c;
            }
        }
        throw new ElementoNoEncontradoException("No se encontró la cancha con ID: " + id);
    }

    public List<Cancha> listarPorSuperficie(Superficie superficie) {
        List<Cancha> resultado = new ArrayList<>();
        for (Cancha c : elementos) {
            if (c.getSuperficie() == superficie) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // Métodos para interactuar con la Base de Datos mediante el DAO
    public void guardarEnBD(Cancha c) throws AccesoDatosException {
        dao.insertar(c);
        elementos.add(c); // Sincronizamos la memoria
    }

    public List<Cancha> listarDesdeBD() throws AccesoDatosException {
        this.elementos = dao.listarTodos(); // Carga los elementos de la BD a la memoria
        return this.elementos;
    }

    public void actualizarEnBD(Cancha c) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(c);
    }

    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
        elementos.removeIf(c -> c.getId() == id);
    }
}
