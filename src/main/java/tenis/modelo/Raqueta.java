package tenis.modelo;

public class Raqueta extends Extra {
    private int cantidad;

    public Raqueta(int id, double precioUnitario, Reserva reserva, int cantidad) {
        super(id, precioUnitario, reserva);
        this.cantidad = cantidad;
    }

    @Override
    public double calcularCosto() {
        return this.precioUnitario * this.cantidad;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
