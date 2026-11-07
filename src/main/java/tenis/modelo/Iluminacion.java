package tenis.modelo;

import java.time.Duration;

public class Iluminacion extends Extra {

    public Iluminacion(int id, double precioUnitario, Reserva reserva) {
        super(id, precioUnitario, reserva);
    }

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
