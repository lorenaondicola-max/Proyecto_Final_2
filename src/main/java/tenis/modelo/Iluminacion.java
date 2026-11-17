package tenis.modelo;

import tenis.enums.TipoExtra;

import java.time.Duration;

/**
 * Uso de la iluminación de la cancha. Se cobra por hora de turno.
 */
public class Iluminacion extends Extra {

    /**
     * @param precioUnitario precio de una hora de iluminación
     * @param reserva reserva cuya duración determina el costo
     */
    public Iluminacion(int id, double precioUnitario, Reserva reserva) {
        super(id, precioUnitario, reserva);
    }

    @Override
    public TipoExtra getTipo() {
        return TipoExtra.ILUMINACION;
    }

    /**
     * Calcula el costo según la duración del turno de la reserva asociada.
     *
     * @return precio por hora multiplicado por las horas del turno;
     * 0 si el extra no tiene reserva o la reserva no tiene horario
     */
    @Override
    public double calcularCosto() {
        if (reserva == null || reserva.getInicio() == null || reserva.getFin() == null) {
            return 0.0;
        }
        // Calcula las horas de duración del turno
        long minutos = Duration.between(reserva.getInicio(), reserva.getFin()).toMinutes();
        double horas = minutos / 60.0;

        return this.precioUnitario * horas;
    }
}
