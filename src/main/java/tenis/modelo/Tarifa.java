package tenis.modelo;

import tenis.enums.Superficie;
import tenis.enums.TipoJuego;
import tenis.excepciones.DatosInvalidosException;


public class Tarifa {

    private double precioBaseHora;
    private double recargoPolvoPorcentaje;
    private double recargoPicoPorcentaje;
    private double recargoDoblesPorcentaje;
    private double descuentoSocioPorcentaje;

    public Tarifa(double precioBaseHora, double recargoPolvoPorcentaje, double recargoPicoPorcentaje,
                  double recargoDoblesPorcentaje, double descuentoSocioPorcentaje) throws DatosInvalidosException {
        if (precioBaseHora < 0 || recargoPolvoPorcentaje < 0 || recargoPicoPorcentaje < 0 || recargoDoblesPorcentaje < 0) {
            throw new DatosInvalidosException("El precio y los recargos no pueden ser negativos");
        }
        if (descuentoSocioPorcentaje < 0 || descuentoSocioPorcentaje > 100) {
            throw new DatosInvalidosException("El descuento debe estar entre 0 y 100");
        }
        this.precioBaseHora = precioBaseHora;
        this.recargoPolvoPorcentaje = recargoPolvoPorcentaje;
        this.recargoPicoPorcentaje = recargoPicoPorcentaje;
        this.recargoDoblesPorcentaje = recargoDoblesPorcentaje;
        this.descuentoSocioPorcentaje = descuentoSocioPorcentaje;
    }

    public double calcularPrecioHora(Superficie superficie, boolean esPico,
                                     TipoJuego tipoJuego, boolean esSocio) {
        double recargo = 0;
        if (superficie == Superficie.POLVO_LADRILLO) recargo += recargoPolvoPorcentaje;
        if (esPico) recargo += recargoPicoPorcentaje;
        if (tipoJuego == TipoJuego.DOBLES) recargo += recargoDoblesPorcentaje;

        double precio = precioBaseHora * (1 + recargo / 100.0);
        if (esSocio) {
            precio = precio * (1 - descuentoSocioPorcentaje / 100.0);
        }
        return precio;
    }

    public double getPrecioBaseHora() { return precioBaseHora; }
    public double getRecargoPolvoPorcentaje() { return recargoPolvoPorcentaje; }
    public double getRecargoPicoPorcentaje() { return recargoPicoPorcentaje; }
    public double getRecargoDoblesPorcentaje() { return recargoDoblesPorcentaje; }
    public double getDescuentoSocioPorcentaje() { return descuentoSocioPorcentaje; }
}
