package tenis.gestor;

import tenis.dao.JugadorDAO;
import tenis.enums.Nivel;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Jugador;

import java.util.ArrayList;
import java.util.List;

public class GestorJugador {


    private List<Jugador> elementos = new ArrayList<>();
    private final DAO<Jugador> dao = new JugadorDAO();


    public Jugador buscar(int id) throws ElementoNoEncontradoException {
        for (Jugador j : elementos) {
            if (j.getId() == id) {
                return j;
            }
        }
        throw new ElementoNoEncontradoException("No se encontró al jugador con ID: " + id);
    }

    public List<Jugador> listarSocios() {
        List<Jugador> socios = new ArrayList<>();
        for (Jugador j : elementos) {
            if (j.isEsSocio()) {
                socios.add(j);
            }
        }
        return socios;
    }

    public List<Jugador> listarPorNivel(Nivel nivel) {
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador j : elementos) {
            if (j.getNivel() == nivel) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    public Jugador buscarEnBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    public List<Jugador> listarDesdeBD() throws AccesoDatosException {
        return dao.listarTodos();
    }

    public void guardarEnBD(Jugador j) throws AccesoDatosException {
        dao.insertar(j);
    }

    public void actualizarEnBD(Jugador j) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(j);
    }

    public void eliminarDeBD(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}
