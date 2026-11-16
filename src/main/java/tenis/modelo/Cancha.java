package tenis.modelo;

import tenis.enums.Superficie;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.gestor.Repositorio;

import java.time.LocalDateTime;
import java.util.List;

public class Cancha {
    private int id;
    private int numero;
    private Superficie superficie;
    private boolean tieneLuz;
    private Club club;
    private Repositorio<Bloqueo> bloqueos;

    // Constructor
    public Cancha(int id, int numero, Superficie superficie, boolean tieneLuz, Club club) {
        this.id = id;
        this.numero = numero;
        this.superficie = superficie;
        this.tieneLuz = tieneLuz;
        this.club = club;
        this.bloqueos = new Repositorio<>();
    }

    // Método verificar si está bloqueada en un rango de tiempo
    public boolean estaBloqueada(LocalDateTime desde, LocalDateTime hasta) {
        for (Bloqueo bloqueo : this.bloqueos.listar()) {
            if (bloqueo.seSuperpone(desde, hasta)) {
                return true; // Se encontró al menos un bloqueo que se superpone
            }
        }
        return false;
    }

    // Método para agregar un bloqueo a la cancha
    public void agregarBloqueo(Bloqueo bloqueo) {
        this.bloqueos.agregar(bloqueo);
    }

    // Método para eliminar un bloqueo
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
