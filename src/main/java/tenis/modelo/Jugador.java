package tenis.modelo;

import tenis.enums.Nivel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Jugador extends Usuario {
    private boolean esSocio;
    private Nivel nivel;
    private String categoria;
    private int cancelacionesTardias;
    private Jugador responsable; // Obligatorio si es menor de 18 años
    private ArrayList<Reserva> historial;

    // Constructor para jugadores mayores de edad
    public Jugador(int id, String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email, String username,
                   String passwordHash, boolean esSocio, Nivel nivel, String categoria) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
        this.esSocio = esSocio;
        this.nivel = nivel;
        this.categoria = categoria;
        this.cancelacionesTardias = 0;
        this.responsable = null;
        this.historial = new ArrayList<>();
    }

    // Constructor para jugadores menores de edad (requieren un responsable mayor)
    public Jugador(int id, String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email,
                   boolean esSocio, Nivel nivel, String categoria, Jugador responsable) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, null, null); // Menores sin credenciales
        this.esSocio = esSocio;
        this.nivel = nivel;
        this.categoria = categoria;
        this.cancelacionesTardias = 0;
        this.responsable = responsable;
        this.historial = new ArrayList<>();
    }

    @Override
    public List<String> getPermisos() {
        if (this.esMenor()) {
            // Los menores están registrados pero no tienen acceso al sistema
            return new ArrayList<>();
        }
        // Jugador adulto con credenciales
        return Arrays.asList("CONSULTAR_DISPONIBILIDAD", "RESERVAR_CANCHA", "CANCELAR_RESERVA", "VER_HISTORIAL");
    }

    public void marcarCancelacionTardia() {
        this.cancelacionesTardias++;
    }

    public void agregarReservaAlHistorial(Reserva reserva) {
        this.historial.add(reserva);
    }

    // Getters y Setters
    public boolean isEsSocio() { return esSocio; }
    public void setEsSocio(boolean esSocio) { this.esSocio = esSocio; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getCancelacionesTardias() { return cancelacionesTardias; }
    public void setCancelacionesTardias(int cancelacionesTardias) { this.cancelacionesTardias = cancelacionesTardias; }

    public Jugador getResponsable() { return responsable; }
    public void setResponsable(Jugador responsable) { this.responsable = responsable; }

    public ArrayList<Reserva> getHistorial() { return historial; }
    public void setHistorial(ArrayList<Reserva> historial) { this.historial = historial; }
}
