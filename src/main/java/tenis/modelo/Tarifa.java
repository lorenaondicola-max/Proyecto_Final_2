package tenis.modelo;

import tenis.enums.Superficie;
import tenis.enums.TipoJuego;
import tenis.excepciones.DatosInvalidosException;

/**
 * Precios de un club: el precio base por hora, los recargos por superficie,
 * hora pico y dobles, y el descuento para socios. Todos los recargos y el
 * descuento se expresan como porcentajes.
 * No tiene setters: una tarifa no cambia después de creada.
 */
public class Tarifa {
    private double precioBaseHora;
    private double recargoPolvoPorcentaje;
    private double recargoPicoPorcentaje;
    private double recargoDoblesPorcentaje;
    private double descuentoSocioPorcentaje;

    /**
     * @param precioBaseHora precio de una hora de cancha, sin recargos
     * @param recargoPolvoPorcentaje recargo por jugar en polvo de ladrillo
     * @param recargoPicoPorcentaje recargo por empezar en hora pico
     * @param recargoDoblesPorcentaje recargo por jugar dobles
     * @param descuentoSocioPorcentaje descuento para socios, entre 0 y 100
     * @throws DatosInvalidosException si el precio o algún recargo es negativo,
     *         o si el descuento no está entre 0 y 100
     */
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

    /**
     * Calcula el precio de una hora de cancha.
     * Suma los porcentajes de recargo que correspondan, los aplica juntos sobre
     * el precio base y, si el jugador es socio, descuenta sobre ese resultado.
     *
     * @param superficie superficie de la cancha
     * @param esPico true si el turno empieza en hora pico
     * @param tipoJuego singles o dobles
     * @param esSocio true si el jugador es socio
     * @return precio por hora, con recargos y descuento aplicados
     */
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

    // Getters
    public double getPrecioBaseHora() {
        return precioBaseHora;
    }

    public double getRecargoPolvoPorcentaje() {
        return recargoPolvoPorcentaje;
    }

    public double getRecargoPicoPorcentaje() {
        return recargoPicoPorcentaje;
    }

    public double getRecargoDoblesPorcentaje() {
        return recargoDoblesPorcentaje;
    }

    public double getDescuentoSocioPorcentaje() {
        return descuentoSocioPorcentaje;
    }
}
