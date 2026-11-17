package tenis.modelo;

import tenis.interfaces.Tarifable;

/**
 * Servicio adicional que se contrata junto con una reserva.
 * Es abstracta porque cada tipo de extra se cobra de una manera distinta:
 * las subclases definen cómo se calcula el costo a partir del precio unitario.
 */

public abstract class Extra implements Tarifable {
    protected int id;
    protected double precioUnitario;
    protected Reserva reserva;

    /**
     * @param precioUnitario precio de una unidad; qué es "una unidad" lo define cada subclase
     * @param reserva reserva a la que pertenece el extra
     */


    public Extra(int id, double precioUnitario, Reserva reserva) {
        this.id = id;
        this.precioUnitario = precioUnitario;
        this.reserva = reserva;
    }
    /**
     * Calcula lo que cuesta este extra. Cada subclase aplica su propia fórmula.
     *
     * @return costo total del extra
     */
    @Override
    public abstract double calcularCosto();

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
}
