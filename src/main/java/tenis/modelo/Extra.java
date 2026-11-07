package tenis.modelo;

import tenis.interfaces.Tarifable;

public abstract class Extra implements Tarifable {
    protected int id;
    protected double precioUnitario;
    protected Reserva reserva;

    public Extra(int id, double precioUnitario, Reserva reserva) {
        this.id = id;
        this.precioUnitario = precioUnitario;
        this.reserva = reserva;
    }

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
