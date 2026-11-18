package tenis.modelo;

import tenis.enums.Categoria;
import tenis.enums.Nivel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Persona que usa las canchas.
 * Guarda su nivel de juego, su categoría, si es socio y cuántas cancelaciones tardías acumula.
 * Un jugador menor de 18 años no tiene credenciales y tiene asociado un responsable adulto,
 * que es quien responde por sus reservas.
 */
public class Jugador extends Usuario {
    private boolean esSocio;
    private Nivel nivel;
    private Categoria categoria;
    private int cancelacionesTardias;
    private Jugador responsable; // Obligatorio si es menor de 18 años
    private ArrayList<Reserva> historial;


    /**
     * Crea un jugador adulto con un id ya conocido. Se usa al leerlo de la base de datos.
     *
     * @param esSocio true si es socio del club; los socios tienen descuento en la tarifa
     * @param nivel nivel de juego
     * @param categoria categoría en la que compite
     */
    public Jugador(int id, String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email, String username,
                   String passwordHash, boolean esSocio, Nivel nivel, Categoria categoria) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
        this.esSocio = esSocio;
        this.nivel = nivel;
        this.categoria = categoria;
        this.cancelacionesTardias = 0;
        this.responsable = null;
        this.historial = new ArrayList<>();
    }

    /**
     * Crea un jugador menor de edad con un id ya conocido. No recibe credenciales.
     *
     * @param responsable jugador adulto que responde por las reservas del menor
     */
    public Jugador(int id, String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email,
                   boolean esSocio, Nivel nivel, Categoria categoria, Jugador responsable) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, null, null);
        this.esSocio = esSocio;
        this.nivel = nivel;
        this.categoria = categoria;
        this.cancelacionesTardias = 0;
        this.responsable = responsable;
        this.historial = new ArrayList<>();
    }

    /**
     * Crea un jugador adulto nuevo, todavía sin guardar.
     */
    public Jugador(String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email, String username,
                   String passwordHash, boolean esSocio, Nivel nivel, Categoria categoria) {
        this(0, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash, esSocio, nivel, categoria);
    }

    /**
     * Crea un jugador menor de edad nuevo, todavía sin guardar.
     */
    public Jugador(String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email,
                   boolean esSocio, Nivel nivel, Categoria categoria, Jugador responsable) {
        this(0, nombre, dni, fechaNacimiento, domicilio, telefono, email, esSocio, nivel, categoria, responsable);
    }

    /**
     * @return los permisos de un jugador adulto (consultar disponibilidad, reservar,
     *         cancelar y ver su historial); lista vacía si es menor
     */
    @Override
    public List<String> getPermisos() {
        if (this.esMenor()) {
            return new ArrayList<>();
        }
        return Arrays.asList("CONSULTAR_DISPONIBILIDAD", "RESERVAR_CANCHA", "CANCELAR_RESERVA", "VER_HISTORIAL");
    }

    /**
     * Suma una cancelación tardía al jugador.
     * La llama la reserva cuando su titular cancela con menos anticipación que la exigida.
     */
    /**
     * Indica si este jugador es el responsable de otro.
     *
     * @param otro jugador a consultar
     * @return true si el otro jugador tiene a este como responsable
     */
    public boolean esResponsableDe(Jugador otro) {
        return otro.getResponsable() != null && otro.getResponsable().getId() == this.id;
    }

    /**
     * @return true si el turno es del propio jugador o de un menor a su cargo
     */
    @Override
    public boolean puedeGestionarReserva(Cancha cancha, Jugador jugador) {
        return jugador.getId() == this.id || esResponsableDe(jugador);
    }

    /**
     * @return siempre false: un jugador no administra canchas
     */
    @Override
    public boolean puedeGestionarCancha(Cancha cancha) {
        return false;
    }

    public void marcarCancelacionTardia() {
        this.cancelacionesTardias++;
    }

    /**
     * Agrega una reserva al historial de turnos del jugador.
     *
     * @param reserva reserva a registrar
     */
    public void agregarReservaAlHistorial(Reserva reserva) {
        this.historial.add(reserva);
    }

    // Getters y Setters
    public boolean isEsSocio() { return esSocio; }
    public void setEsSocio(boolean esSocio) { this.esSocio = esSocio; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public int getCancelacionesTardias() { return cancelacionesTardias; }
    public void setCancelacionesTardias(int cancelacionesTardias) { this.cancelacionesTardias = cancelacionesTardias; }

    public Jugador getResponsable() { return responsable; }
    public void setResponsable(Jugador responsable) { this.responsable = responsable; }

    public ArrayList<Reserva> getHistorial() { return historial; }
    public void setHistorial(ArrayList<Reserva> historial) { this.historial = historial; }

    @Override
    public String toString() {
        return "Jugador id " + id + ": " + nombre + ", DNI " + dni + ", nivel " + nivel + ", categoria " + categoria
                + (esMenor() && responsable != null ? " (menor, responsable: " + responsable.getNombre() + ")" : "");
    }
}
