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

    private final DAO<Jugador> dao = new JugadorDAO();

    public Jugador buscar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    public List<Jugador> listarTodos() throws AccesoDatosException {
        return dao.listarTodos();
    }

    public List<Jugador> listarSocios() throws AccesoDatosException {
        List<Jugador> socios = new ArrayList<>();
        for (Jugador j : dao.listarTodos()) {
            if (j.isEsSocio()) {
                socios.add(j);
            }
        }
        return socios;
    }

    public List<Jugador> listarPorNivel(Nivel nivel) throws AccesoDatosException {
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador j : dao.listarTodos()) {
            if (j.getNivel() == nivel) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    public void guardar(Jugador j) throws AccesoDatosException {
        dao.insertar(j);
    }

    public void actualizar(Jugador j) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(j);
    }

    public void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}