package tenis.modelo;

import tenis.enums.MotivoBloqueo;
import tenis.interfaces.Identificable;

import java.time.LocalDateTime;

public class Bloqueo implements Identificable {
    private int id;
    private MotivoBloqueo motivo;
    private LocalDateTime desde;
    private LocalDateTime hasta;
    private String detalle;

    // Constructor
    public Bloqueo(int id, MotivoBloqueo motivo, LocalDateTime desde, LocalDateTime hasta, String detalle) {
        this.id = id;
        this.motivo = motivo;
        this.desde = desde;
        this.hasta = hasta;
        this.detalle = detalle;
    }

    public boolean seSuperpone(LocalDateTime inicio, LocalDateTime fin) {
        // Un intervalo (inicio, fin) se superpone con (desde, hasta) si:
        // el inicio es antes de que termine el bloqueo Y el fin es después de que empiece el bloqueo.
        return inicio.isBefore(this.hasta) && fin.isAfter(this.desde);
    }

    // Getters y Setters
    @Override
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public MotivoBloqueo getMotivo() { return motivo; }
    public void setMotivo(MotivoBloqueo motivo) { this.motivo = motivo; }

    public LocalDateTime getDesde() { return desde; }
    public void setDesde(LocalDateTime desde) { this.desde = desde; }

    public LocalDateTime getHasta() { return hasta; }
    public void setHasta(LocalDateTime hasta) { this.hasta = hasta; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
