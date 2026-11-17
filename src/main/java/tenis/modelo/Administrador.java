package tenis.modelo;

import tenis.enums.Superficie;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.MenorSinResponsableException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Usuario a cargo de la administración del sistema.
 * Arma la estructura de cada club (el club y sus canchas) y da de alta a los demás usuarios.
 * Sus métodos crean los objetos en memoria; guardarlos en la base es tarea de los gestores.
 */
public class Administrador extends Usuario {

    // Constructor
    public Administrador(int id, String nombre, String dni, LocalDate fechaNacimiento,
                         String domicilio, String telefono, String email, String username, String passwordHash) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
    }

    /**
     * @return los permisos administrativos: backup, creación de usuarios,
     *         asignación de permisos y gestión del club
     */
    @Override
    public List<String> getPermisos() {
        // El administrador tiene acceso total al sistema
        return Arrays.asList("BACKUP_INFORMACION", "CREAR_USUARIOS", "ASIGNAR_PERMISOS", "GESTIONAR_CLUB");
    }

    /**
     * Crea un club con su configuración de horarios, política de cancelación y tarifa.
     *
     * @param diasAtencion días de la semana en que el club abre
     * @param horasCancelacion anticipación mínima, en horas, para cancelar sin cargo
     * @param porcentajePenalizacion porcentaje del turno que se cobra por una cancelación tardía
     * @return el club creado, sin canchas
     */
    public Club crearClub(int id, String nombre, String direccion, LocalTime horaApertura, LocalTime horaCierre,
                          LocalTime inicioHoraPico, LocalTime finHoraPico, ArrayList<DayOfWeek> diasAtencion,
                          int horasCancelacion, double porcentajePenalizacion, Tarifa tarifa) {
        return new Club(id, nombre, direccion, horaApertura, horaCierre, inicioHoraPico, finHoraPico,
                diasAtencion, horasCancelacion, porcentajePenalizacion, tarifa);
    }

    /**
     * Crea una cancha y la agrega a la lista de canchas del club.
     *
     * @param numero número con el que el club identifica la cancha
     * @param tieneLuz true si la cancha tiene iluminación
     * @param club club al que pertenece
     * @return la cancha creada
     */
    public Cancha crearCancha(int id, int numero, Superficie superficie, boolean tieneLuz, Club club) {
        Cancha nuevaCancha = new Cancha(id, numero, superficie, tieneLuz, club);
        club.agregarCancha(nuevaCancha);
        return nuevaCancha;
    }

    /**
     * Crea un usuario del tipo indicado.
     * Para un jugador, decide por la fecha de nacimiento si es menor de 18 años:
     * un menor se crea sin credenciales y con su responsable; un adulto, con credenciales.
     * El jugador se crea sin nivel ni categoría y como no socio; esos datos se cargan después.
     * El recepcionista se crea sin club asignado.
     *
     * @param tipoUsuario "administrador", "recepcionista" o "jugador"; no distingue mayúsculas
     * @param responsable adulto responsable; solo se usa si el jugador es menor
     * @return el usuario creado
     * @throws MenorSinResponsableException si el jugador es menor y no se indicó responsable
     * @throws DatosInvalidosException si el tipo de usuario no es ninguno de los tres
     */
    public Usuario crearUsuario(String tipoUsuario, int id, String nombre, String dni, LocalDate fechaNacimiento,
                                String domicilio, String telefono, String email, String username, String passwordHash,
                                Jugador responsable) throws MenorSinResponsableException {
        switch (tipoUsuario.toLowerCase()) {
            case "administrador":
                return new Administrador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
            case "recepcionista":
                return new Recepcionista(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash, null);
            case "jugador":
                // Validar si es menor de edad (menos de 18 años)
                int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();

                if (edad < 18) {
                    if (responsable == null) {
                        throw new MenorSinResponsableException("El jugador es menor de edad y requiere obligatoriamente un responsable asignado.");
                    }
                    // Utiliza el constructor de menores (10 parámetros, sin credenciales)
                    return new Jugador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, false, null, null, responsable);
                } else {
                    // Utiliza el constructor de adultos (11 parámetros, con credenciales)
                    return new Jugador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash, false, null, null);
                }
            default:
                throw new DatosInvalidosException("Tipo de usuario desconocido: " + tipoUsuario);
        }
    }
}
