package tenis.modelo;

import tenis.enums.EstadoReserva;
import tenis.enums.Superficie;
import tenis.enums.TipoJuego;
import tenis.excepciones.DatosInvalidosException;
import tenis.interfaces.Cancelable;
import tenis.interfaces.Tarifable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Turno reservado para que un jugador use una cancha durante un período.
 * Distingue al jugador, que usa la cancha, del titular, que responde por la
 * reserva: para un adulto son la misma persona; para un menor, el titular es su responsable.
 * Puede tener extras asociados, y sabe calcular su costo y cancelarse.
 */
public class Reserva implements Cancelable, Tarifable {
    private int id;
    private Cancha cancha;
    private Jugador jugador;
    private Jugador titular;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private TipoJuego tipoJuego;
    private EstadoReserva estado;
    private double cargoCancelacion;
    private boolean asistio;
    private ArrayList<Extra> extras;

    /**
     * Crea una reserva activa, sin cargo de cancelación, sin asistencia registrada y sin extras.
     *
     * @param cancha cancha reservada
     * @param jugador quien va a jugar
     * @param titular quien responde por la reserva
     * @param inicio comienzo del turno
     * @param fin final del turno
     * @param tipoJuego singles o dobles
     */
    public Reserva(int id, Cancha cancha, Jugador jugador, Jugador titular,
                   LocalDateTime inicio, LocalDateTime fin, TipoJuego tipoJuego) {
        this.id = id;
        this.cancha = cancha;
        this.jugador = jugador;
        this.titular = titular;
        this.inicio = inicio;
        this.fin = fin;
        this.tipoJuego = tipoJuego;
        this.estado = EstadoReserva.ACTIVA;
        this.cargoCancelacion = 0.0;
        this.asistio = false;
        this.extras = new ArrayList<>();
    }


    /**
     * Calcula el costo total de la reserva.
     * Multiplica el precio por hora que da la tarifa del club por la duración del turno,
     * y le suma el costo de cada extra. El descuento de socio depende del jugador, no del titular.
     *
     * @return costo del turno más el de sus extras
     */
    @Override
    public double calcularCosto() {
        Club club = cancha.getClub();
        Tarifa tarifa = club.getTarifa();

        // Calcular la duración del turno en horas
        long minutos = Duration.between(inicio, fin).toMinutes();
        double horas = minutos / 60.0;

        // Obtener los parámetros requeridos por la clase Tarifa
        Superficie superficie = cancha.getSuperficie();
        boolean esHoraPico = club.comienzaEnHoraPico(inicio);
        boolean esSocio = jugador.isEsSocio();

        // Llamada correcta a calcularPrecioHora con los tipos que espera Tarifa
        double precioHora = tarifa.calcularPrecioHora(superficie, esHoraPico, tipoJuego, esSocio);
        double costoTurno = precioHora * horas;

        // Sumar el costo acumulado de todos los servicios extras asociados
        double costoExtras = 0.0;
        for (Extra extra : extras) {
            costoExtras += extra.calcularCosto();
        }

        return costoTurno + costoExtras;
    }

    /**
     * Cancela la reserva y calcula si corresponde un cargo, según la anticipación
     * mínima que exige el {@link Club}.
     * Si se cancela a tiempo, no hay cargo. Si la cancelación es tardía, se cobra
     * el porcentaje del costo del turno que define el club y se le suma una
     * cancelación tardía al titular de la reserva.
     *
     * @param ahora fecha y hora en la que se pide la cancelación
     * @return el cargo a pagar; 0 si se canceló a tiempo
     * @throws DatosInvalidosException si la reserva no está activa
     */
    @Override
    public double cancelar(LocalDateTime ahora) {

        if (this.estado != EstadoReserva.ACTIVA) {
            throw new DatosInvalidosException("Solo se puede cancelar una reserva activa");
        }

        Club club = cancha.getClub();
        int horasAnticipacionMinima = club.getHorasCancelacion(); // Por defecto 12 horas

        // Calcular el límite temporal permitido para cancelar sin cargo
        LocalDateTime limiteSinCargo = inicio.minusHours(horasAnticipacionMinima);

        if (ahora.isBefore(limiteSinCargo) || ahora.isEqual(limiteSinCargo)) {
            // Cancelación a tiempo (sin cargo)
            this.estado = EstadoReserva.CANCELADA;
            this.cargoCancelacion = 0.0;
            return 0.0;
        } else {
            // Cancelación tardía: genera cargo y señala al titular
            this.estado = EstadoReserva.CANCELADA;
            double costoTotalTurno = calcularCosto();
            double porcentajePenalizacion = club.getPorcentajePenalizacion();

            this.cargoCancelacion = costoTotalTurno * (porcentajePenalizacion / 100.0);

            // Registrar la penalización en el historial/contador del titular responsable
            this.titular.marcarCancelacionTardia();

            return this.cargoCancelacion;
        }
    }

    /**
     * Indica si esta reserva ocupa la cancha durante alguna parte de un período.
     * Una reserva cancelada no ocupa la cancha, así que nunca se superpone.
     *
     * @param otroInicio inicio del período a consultar
     * @param otroFin fin del período a consultar
     * @return true si la reserva no está cancelada y los períodos se superponen
     */
    public boolean seSuperpone(LocalDateTime otroInicio, LocalDateTime otroFin) {
        if (this.estado == EstadoReserva.CANCELADA) {
            return false; // Las reservas canceladas no ocupan la cancha
        }
        return otroInicio.isBefore(this.fin) && otroFin.isAfter(this.inicio);
    }

    /**
     * Asocia un servicio extra a la reserva. Su costo se suma al calcular el total.
     *
     * @param extra extra a agregar
     */
    public void agregarExtra(Extra extra) {
        this.extras.add(extra);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cancha getCancha() { return cancha; }
    public void setCancha(Cancha cancha) { this.cancha = cancha; }

    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jug) { this.jugador = jug; }

    public Jugador getTitular() { return titular; }
    public void setTitular(Jugador titular) { this.titular = titular; }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }

    public TipoJuego getTipoJuego() { return tipoJuego; }
    public void setTipoJuego(TipoJuego tipoJuego) { this.tipoJuego = tipoJuego; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public double getCargoCancelacion() { return cargoCancelacion; }

    public void setCargoCancelacion(double cargoCancelacion) {
        this.cargoCancelacion = cargoCancelacion;
    }

    public boolean isAsistio() { return asistio; }
    public void setAsistio(boolean asistio) { this.asistio = asistio; }

    public ArrayList<Extra> getExtras() { return extras; }

    @Override
    public String toString() {
        String texto = "Reserva id " + id + ": cancha " + cancha.getNumero()
                + ", jugador " + jugador.getNombre()
                + ", de " + inicio + " a " + fin + ", " + estado;
        if (titular != null && titular.getId() != jugador.getId()) {
            texto += " (titular: " + titular.getNombre() + ")";
        }
        return texto;
    }
}