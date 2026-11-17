package tenis.modelo;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Establecimiento que agrupa canchas y define sus reglas de funcionamiento:
 * días y horario de atención, franja de hora pico, tarifa y política de cancelación.
 */
public class Club {
    private int id;
    private String nombre;
    private String direccion;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private LocalTime inicioHoraPico;
    private LocalTime finHoraPico;
    private ArrayList<DayOfWeek> diasAtencion;
    private int horasCancelacion;
    private double porcentajePenalizacion;
    private ArrayList<Cancha> canchas;
    private Tarifa tarifa;


    /**
     * Crea un club nuevo, todavía sin guardar. El id queda en 0 hasta que la base le asigne uno.
     *
     * @param inicioHoraPico hora desde la que rige el recargo por franja pico
     * @param finHoraPico hora hasta la que rige el recargo por franja pico
     * @param diasAtencion días de la semana en que el club abre
     * @param horasCancelacion anticipación mínima, en horas, para cancelar sin cargo
     * @param porcentajePenalizacion porcentaje del turno que se cobra por una cancelación tardía
     * @param tarifa precios y recargos del club
     */
    public Club(String nombre, String direccion, LocalTime horaApertura, LocalTime horaCierre,
                    LocalTime inicioHoraPico, LocalTime finHoraPico, ArrayList<DayOfWeek> diasAtencion,
                    int horasCancelacion, double porcentajePenalizacion, Tarifa tarifa) {
            this.id = 0; // Se autogenerará en la BD
            this.nombre = nombre;
            this.direccion = direccion;
            this.horaApertura = horaApertura;
            this.horaCierre = horaCierre;
            this.inicioHoraPico = inicioHoraPico;
            this.finHoraPico = finHoraPico;
            this.diasAtencion = diasAtencion;
            this.horasCancelacion = horasCancelacion;
            this.porcentajePenalizacion = porcentajePenalizacion;
            this.tarifa = tarifa;
            this.canchas = new ArrayList<>();
    }

    /**
     * Crea un club con un id ya conocido. Se usa al leerlo de la base de datos.
     */
    public Club(int id, String nombre, String direccion, LocalTime horaApertura, LocalTime horaCierre,
                    LocalTime inicioHoraPico, LocalTime finHoraPico, ArrayList<DayOfWeek> diasAtencion,
                    int horasCancelacion, double porcentajePenalizacion, Tarifa tarifa) {
            this.id = id;
            this.nombre = nombre;
            this.direccion = direccion;
            this.horaApertura = horaApertura;
            this.horaCierre = horaCierre;
            this.inicioHoraPico = inicioHoraPico;
            this.finHoraPico = finHoraPico;
            this.diasAtencion = diasAtencion;
            this.horasCancelacion = horasCancelacion;
            this.porcentajePenalizacion = porcentajePenalizacion;
            this.tarifa = tarifa;
            this.canchas = new ArrayList<>();
    }

    /**
     * Agrega una cancha a la lista de canchas del club.
     *
     * @param cancha cancha a agregar
     */
    public void agregarCancha(Cancha cancha) {
            this.canchas.add(cancha);
    }

    /**
     * Busca una cancha del club por su id.
     *
     * @param idCancha id de la cancha buscada
     * @return la cancha, o null si el club no tiene ninguna con ese id
     */
    public Cancha buscarCancha(int idCancha) {
            for (Cancha cancha : this.canchas) {
                if (cancha.getId() == idCancha) {
                    return cancha;
                }
            }
            return null;
    }

    /**
     * Indica si el club atiende en un momento dado.
     * Verifica que el día esté entre los días de atención y que la hora
     * esté entre la apertura y el cierre, ambos extremos incluidos.
     *
     * @param momento fecha y hora a consultar
     * @return true si el club está abierto en ese momento
     */
    public boolean estaAbierto(LocalDateTime momento) {
            DayOfWeek dia = momento.getDayOfWeek();
            LocalTime hora = momento.toLocalTime();

            // Verificar si el día está incluido en los días de atención
            if (!this.diasAtencion.contains(dia)) {
                return false;
            }

            // Verificar si la hora se encuentra dentro del rango de apertura y cierre
            return !hora.isBefore(this.horaApertura) && !hora.isAfter(this.horaCierre);
    }

    /**
     * Indica si un turno empieza dentro de la franja de hora pico.
     * Solo se mira la hora de inicio del turno, con ambos extremos de la franja incluidos.
     *
     * @param inicioTurno fecha y hora de inicio del turno
     * @return true si corresponde el recargo por hora pico
     */
    public boolean comienzaEnHoraPico(LocalDateTime inicioTurno) {
            LocalTime horaInicio = inicioTurno.toLocalTime();
            return !horaInicio.isBefore(this.inicioHoraPico) && !horaInicio.isAfter(this.finHoraPico);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }

    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }

    public LocalTime getInicioHoraPico() { return inicioHoraPico; }
    public void setInicioHoraPico(LocalTime inicioHoraPico) { this.inicioHoraPico = inicioHoraPico; }

    public LocalTime getFinHoraPico() { return finHoraPico; }
    public void setFinHoraPico(LocalTime finHoraPico) { this.finHoraPico = finHoraPico; }

    public ArrayList<DayOfWeek> getDiasAtencion() { return diasAtencion; }
    public void setDiasAtencion(ArrayList<DayOfWeek> diasAtencion) { this.diasAtencion = diasAtencion; }

    public int getHorasCancelacion() { return horasCancelacion; }
    public void setHorasCancelacion(int horasCancelacion) { this.horasCancelacion = horasCancelacion; }

    public double getPorcentajePenalizacion() { return porcentajePenalizacion; }
    public void setPorcentajePenalizacion(double porcentajePenalizacion) { this.porcentajePenalizacion = porcentajePenalizacion; }

    public ArrayList<Cancha> getCanchas() { return canchas; }
    public void setCanchas(ArrayList<Cancha> canchas) { this.canchas = canchas; }

    public Tarifa getTarifa() { return tarifa; }
    public void setTarifa(Tarifa tarifa) { this.tarifa = tarifa; }
}
