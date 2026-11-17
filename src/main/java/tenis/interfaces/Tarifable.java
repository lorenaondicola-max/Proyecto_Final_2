package tenis.interfaces;

/**
 * Capacidad de calcular un costo. La implementan {@code Reserva} y {@code Extra},
 * lo que permite sumar el costo de una reserva y el de sus extras de la misma manera.
 */

public interface Tarifable {

    /**
     * @return el costo, en pesos
     */

    double calcularCosto();
}
