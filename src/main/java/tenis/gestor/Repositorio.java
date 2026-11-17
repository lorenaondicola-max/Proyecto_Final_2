package tenis.gestor;

import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.Identificable;

import java.util.ArrayList;
import java.util.List;

/**
 * Colección en memoria de elementos que se identifican por un id.
 * Es genérica: sirve para cualquier tipo que implemente {@link Identificable},
 * y por eso puede buscar y eliminar por id sin saber qué guarda.
 * En el sistema se usa para los bloqueos de cada cancha.
 *
 * @param <T> tipo de elemento que guarda; debe implementar Identificable
 */
public class Repositorio<T extends Identificable> {

    private final List<T> elementos;

    /** Crea un repositorio vacío. */
    public Repositorio() {
        this.elementos = new ArrayList<>();
    }

    /**
     * Indica si la colección tiene un elemento con ese id.
     *
     * @param id identificador a consultar
     * @return true si hay un elemento con ese id
     */
    public boolean existe(int id){
        for (T elemento : elementos){
            if (elemento.getId() == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Agrega un elemento, siempre que no haya otro con el mismo id.
     *
     * @param t elemento a agregar
     * @throws DatosInvalidosException si ya existe un elemento con ese id
     */
    public void agregar (T t){
        if (existe(t.getId())){
            throw new DatosInvalidosException("Ya existe un elemento registrado con el id: " + t.getId());
        }
        elementos.add(t);
    }

    /**
     * Busca un elemento por su id.
     *
     * @param id identificador del elemento buscado
     * @return el elemento con ese id
     * @throws ElementoNoEncontradoException si no hay un elemento con ese id
     */
    public T buscar (int id) throws ElementoNoEncontradoException {
        for (T elemento : elementos){
            if (elemento.getId() == id){
                return elemento;
            }
        }
        throw new ElementoNoEncontradoException("No se ha encontrado un elemento con el identificador: " + id);
    }

    /**
     * Elimina el elemento que tiene ese id.
     *
     * @param id identificador del elemento a eliminar
     * @throws ElementoNoEncontradoException si no hay un elemento con ese id
     */
    public void eliminar(int id) throws ElementoNoEncontradoException {
        T elemento = buscar(id);
        elementos.remove(elemento);
    }

    /**
     * Devuelve los elementos en una lista nueva.
     * Es una copia: quien la recibe puede modificarla sin alterar
     * la colección interna, que solo cambia con agregar y eliminar.
     *
     * @return copia de la lista de elementos
     */
    public List<T> listar(){
        return new ArrayList<>(elementos);
    }

    /**
     * @return cantidad de elementos guardados
     */
    public int cantidad() {
        return elementos.size();
    }
}
