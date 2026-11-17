package tenis.modelo;

import tenis.enums.Superficie;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.excepciones.DatosInvalidosException;
import tenis.gestor.Repositorio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Espacio de juego de un club, con un tipo de superficie y, opcionalmente, iluminación.
 * Guarda los bloqueos que impiden reservarla en ciertos períodos.
 */
public class Cancha {
    private int id;
    private int numero;
    private Superficie superficie;
    private boolean tieneLuz;
    private Club club;
    private Repositorio<Bloqueo> bloqueos;

    /**
     * @param numero número con el que el club identifica la cancha
     * @param superficie polvo de ladrillo o cemento
     * @param tieneLuz true si la cancha tiene iluminación
     * @param club club al que pertenece
     */
    public Cancha(int id, int numero, Superficie superficie, boolean tieneLuz, Club club) {
        this.id = id;
        this.numero = numero;
        this.superficie = superficie;
        this.tieneLuz = tieneLuz;
        this.club = club;
        this.bloqueos = new Repositorio<>();
    }

    /**
     * Indica si la cancha tiene algún bloqueo que se superponga con un período.
     *
     * @param desde inicio del período a consultar
     * @param hasta fin del período a consultar
     * @return true si al menos un bloqueo se superpone con ese período
     */
    public boolean estaBloqueada(LocalDateTime desde, LocalDateTime hasta) {
        for (Bloqueo bloqueo : this.bloqueos.listar()) {
            if (bloqueo.seSuperpone(desde, hasta)) {
                return true; // Se encontró al menos un bloqueo que se superpone
            }
        }
        return false;
    }

    /**
     * Agrega un bloqueo a la cancha.
     *
     * @param bloqueo bloqueo a agregar
     * @throws DatosInvalidosException si la cancha ya tiene un bloqueo con ese id
     */
    public void agregarBloqueo(Bloqueo bloqueo) {
        this.bloqueos.agregar(bloqueo);
    }

    /**
     * Elimina un bloqueo a una cancha.
     *
     * @param idBloqueo identificador del bloqueo a eliminar
     * @throws ElementoNoEncontradoException si la cancha no tiene un bloqueo con ese id
     */
    public void eliminarBloqueo(int idBloqueo) throws ElementoNoEncontradoException {
        this.bloqueos.eliminar(idBloqueo);
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Superficie getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Superficie superficie) {
        this.superficie = superficie;
    }

    public boolean isTieneLuz() {
        return tieneLuz;
    }

    public void setTieneLuz(boolean tieneLuz) {
        this.tieneLuz = tieneLuz;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public List<Bloqueo> getBloqueos() {
        return bloqueos.listar();
    }
}
