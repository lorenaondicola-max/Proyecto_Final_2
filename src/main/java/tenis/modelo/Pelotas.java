package tenis.modelo;

import tenis.enums.TipoExtra;

/**
 *  Venta de pelotas. Se cobra por tubo.
 */

public class Pelotas extends Extra {
    private int cantidadTubos;

    /**
     * @param precioUnitario precio de un tubo de pelotas
     * @param cantidadTubos cantidad de tubos
     */
    public Pelotas(int id, double precioUnitario, Reserva reserva, int cantidadTubos) {
        super(id, precioUnitario, reserva);
        this.cantidadTubos = cantidadTubos;
    }

    @Override
    public TipoExtra getTipo() {
        return TipoExtra.PELOTAS;
    }

    /**
     * @return precio de un tubo multiplicado por la cantidad de tubos
     */
    @Override
    public double calcularCosto() {
        return this.precioUnitario * this.cantidadTubos;
    }

    public int getCantidadTubos() { return cantidadTubos; }
    public void setCantidadTubos(int cantidadTubos) { this.cantidadTubos = cantidadTubos; }
}
