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

    private final DAO<Cancha> dao = new CanchaDAO();

    public Cancha buscar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        return dao.buscarPorId(id);
    }

    public List<Cancha> listarTodos() throws AccesoDatosException {
        return dao.listarTodos();
    }

    public List<Cancha> listarPorSuperficie(Superficie superficie) throws AccesoDatosException {
        List<Cancha> resultado = new ArrayList<>();
        for (Cancha c : dao.listarTodos()) {
            if (c.getSuperficie() == superficie) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public void guardar(Cancha c) throws AccesoDatosException {
        dao.insertar(c);
    }

    public void actualizar(Cancha c) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.actualizar(c);
    }

    public void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        dao.eliminar(id);
    }
}