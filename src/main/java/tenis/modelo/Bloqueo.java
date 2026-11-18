package tenis.modelo;

import tenis.enums.MotivoBloqueo;
import tenis.interfaces.Identificable;

import java.time.LocalDateTime;

/**
 * Período en el que una cancha no puede reservarse por un motivo particular:
 * mantenimiento, torneo o clases.
 */
public class Bloqueo implements Identificable {
    private int id;
    private MotivoBloqueo motivo;
    private LocalDateTime desde;
    private LocalDateTime hasta;
    private String detalle;

    /**
     * @param motivo causa del bloqueo
     * @param desde inicio del bloqueo
     * @param hasta fin del bloqueo
     * @param detalle texto libre que amplía el motivo
     */
    public Bloqueo(int id, MotivoBloqueo motivo, LocalDateTime desde, LocalDateTime hasta, String detalle) {
        this.id = id;
        this.motivo = motivo;
        this.desde = desde;
        this.hasta = hasta;
        this.detalle = detalle;
    }

    /**
     * Indica si el bloqueo se superpone con un período.
     * Dos períodos se superponen si cada uno empieza antes de que termine el otro.
     * Un período que empieza justo cuando termina el bloqueo no se superpone.
     *
     * @param inicio inicio del período a consultar
     * @param fin fin del período a consultar
     * @return true si hay superposición
     */
    public boolean seSuperpone(LocalDateTime inicio, LocalDateTime fin) {
        // Un intervalo (inicio, fin) se superpone con (desde, hasta) si:
        // el inicio es antes de que termine el bloqueo Y el fin es después de que empiece el bloqueo.
        return inicio.isBefore(this.hasta) && fin.isAfter(this.desde);
    }

    // Getters y Setters
    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MotivoBloqueo getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoBloqueo motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public void setDesde(LocalDateTime desde) {
        this.desde = desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public void setHasta(LocalDateTime hasta) {
        this.hasta = hasta;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    @Override
    public String toString() {
        return "Bloqueo id " + id + ": " + motivo + ", de " + desde + " a " + hasta
                + (detalle != null && !detalle.isBlank() ? " (" + detalle + ")" : "");
    }
}
