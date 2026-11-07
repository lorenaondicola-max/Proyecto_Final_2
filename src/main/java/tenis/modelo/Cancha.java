package tenis.modelo;

import tenis.enums.Superficie;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Cancha {
    private int id;
    private int numero;
    private Superficie superficie;
    private boolean tieneLuz;
    private Club club;
    private ArrayList<Bloqueo> bloqueos;

    // Constructor
    public Cancha(int id, int numero, Superficie superficie, boolean tieneLuz, Club club) {
        this.id = id;
        this.numero = numero;
        this.superficie = superficie;
        this.tieneLuz = tieneLuz;
        this.club = club;
        this.bloqueos = new ArrayList<>();
    }

    // Método verificar si está bloqueada en un rango de tiempo
    public boolean estaBloqueada(LocalDateTime desde, LocalDateTime hasta) {
        for (Bloqueo bloqueo : this.bloqueos) {
            if (bloqueo.seSuperpone(desde, hasta)) {
                return true; // Se encontró al menos un bloqueo que se superpone
            }
        }
        return false;
    }

    // Método para agregar un bloqueo a la cancha
    public void agregarBloqueo(Bloqueo bloqueo) {
        this.bloqueos.add(bloqueo);
    }

    // Método para eliminar un bloqueo
    public void eliminarBloqueo(int idBloqueo) {
        for (int i = 0; i < this.bloqueos.size(); i++) {
            if (this.bloqueos.get(i).getId() == idBloqueo) {
                this.bloqueos.remove(i);
                break;
            }
        }
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public Superficie getSuperficie() { return superficie; }
    public void setSuperficie(Superficie superficie) { this.superficie = superficie; }

    public boolean isTieneLuz() { return tieneLuz; }
    public void setTieneLuz(boolean tieneLuz) { this.tieneLuz = tieneLuz; }

    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }

    public ArrayList<Bloqueo> getBloqueos() { return bloqueos; }
    public void setBloqueos(ArrayList<Bloqueo> bloqueos) { this.bloqueos = bloqueos; }
}
