package tenis.gestor;

import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.Identificable;

import java.util.ArrayList;
import java.util.List;

public class Repositorio<T extends Identificable> {

    private final List<T> elementos;

    public Repositorio() {
        this.elementos = new ArrayList<>();
    }

    public boolean existe(int id){
        for (T elemento : elementos){
            if (elemento.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public void agregar (T t){
        if (existe(t.getId())){
            throw new DatosInvalidosException("Ya existe un elemento registrado con el id: " + t.getId());
        }
        elementos.add(t);
    }

    public T buscar (int id) throws ElementoNoEncontradoException {
        for (T elemento : elementos){
            if (elemento.getId() == id){
                return elemento;
            }
        }
        throw new ElementoNoEncontradoException("No se ha encontrado un elemento con el identificador: " + id);
    }

    public void eliminar(int id) throws ElementoNoEncontradoException {
        T elemento = buscar(id);
        elementos.remove(elemento);
    }

    public List<T> listar(){
        return new ArrayList<>(elementos);
    }

    public int cantidad() {
        return elementos.size();
    }
}
