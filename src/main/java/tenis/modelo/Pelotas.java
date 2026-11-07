package tenis.modelo;

public class Pelotas extends Extra {
    private int cantidadTubos;

    public Pelotas(int id, double precioUnitario, Reserva reserva, int cantidadTubos) {
        super(id, precioUnitario, reserva);
        this.cantidadTubos = cantidadTubos;
    }

    @Override
    public double calcularCosto() {
        return this.precioUnitario * this.cantidadTubos;
    }

    public int getCantidadTubos() { return cantidadTubos; }
    public void setCantidadTubos(int cantidadTubos) { this.cantidadTubos = cantidadTubos; }
}
