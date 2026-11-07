package tenis.modelo;

public class ClaseConProfesor extends Extra {
    private String profesor;
    private int duracionMin; // Duración de la clase en minutos

    public ClaseConProfesor(int id, double precioUnitarioHora, Reserva reserva, String profesor, int duracionMin) {
        super(id, precioUnitarioHora, reserva);
        this.profesor = profesor;
        this.duracionMin = duracionMin;
    }

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
