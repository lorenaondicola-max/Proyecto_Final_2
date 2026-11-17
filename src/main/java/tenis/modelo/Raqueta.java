package tenis.modelo;


import tenis.enums.TipoExtra;

/**
 * Alquiler de raquetas. Se cobra por unidad.
 */
public class Raqueta extends Extra {
    private int cantidad;

    /**
     * @param precioUnitario precio del alquiler de una raqueta
     * @param cantidad cantidad de raquetas alquiladas
     */
    public Raqueta(int id, double precioUnitario, Reserva reserva, int cantidad) {
        super(id, precioUnitario, reserva);
        this.cantidad = cantidad;
    }

    @Override
    public TipoExtra getTipo() {
        return TipoExtra.RAQUETA;
    }

    /**
     * @return precio de una raqueta multiplicado por la cantidad alquilada
     */
    @Override
    public double calcularCosto() {
        return this.precioUnitario * this.cantidad;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
