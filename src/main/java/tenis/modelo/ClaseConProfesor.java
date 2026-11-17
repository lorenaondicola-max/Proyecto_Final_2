package tenis.modelo;

import tenis.enums.TipoExtra;

/**
 * Clase con profesor. Se cobra por hora de clase, que puede durar distinto que el turno.
 */
public class ClaseConProfesor extends Extra {
    private String profesor;
    private int duracionMin; // Duración de la clase en minutos

    /**
     * @param precioUnitarioHora precio de una hora de clase
     * @param profesor nombre del profesor
     * @param duracionMin duración de la clase, en minutos
     */
    public ClaseConProfesor(int id, double precioUnitarioHora, Reserva reserva, String profesor, int duracionMin) {
        super(id, precioUnitarioHora, reserva);
        this.profesor = profesor;
        this.duracionMin = duracionMin;
    }

    @Override
    public TipoExtra getTipo() {
        return TipoExtra.CLASE;
    }

    /**
     * @return precio por hora multiplicado por la duración de la clase, pasada de minutos a horas
     */
    @Override
    public double calcularCosto() {
        // El precio unitario se asume por hora, por lo que se convierte la duración en minutos a horas
        double horasClase = this.duracionMin / 60.0;
        return this.precioUnitario * horasClase;
    }

    public String getProfesor() { return profesor; }
    public void setProfesor(String profesor) { this.profesor = profesor; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }
}
