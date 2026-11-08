package tenis.modelo;

import tenis.enums.Superficie;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Administrador extends Usuario {

    // Constructor
    public Administrador(int id, String nombre, String dni, LocalDate fechaNacimiento,
                         String domicilio, String telefono, String email, String username, String passwordHash) {
        super(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
    }

    @Override
    public List<String> getPermisos() {
        // El administrador tiene acceso total al sistema
        return Arrays.asList("BACKUP_INFORMACION", "CREAR_USUARIOS", "ASIGNAR_PERMISOS", "GESTIONAR_CLUB");
    }

    // Método para crear un Club
    public Club crearClub(int id, String nombre, String direccion, LocalTime horaApertura, LocalTime horaCierre,
                          LocalTime inicioHoraPico, LocalTime finHoraPico, ArrayList<DayOfWeek> diasAtencion,
                          int horasCancelacion, double porcentajePenalizacion, Tarifa tarifa) {
        return new Club(id, nombre, direccion, horaApertura, horaCierre, inicioHoraPico, finHoraPico,
                diasAtencion, horasCancelacion, porcentajePenalizacion, tarifa);
    }

    // Método para crear y agregar una Cancha a un club
    public Cancha crearCancha(int id, int numero, Superficie superficie, boolean tieneLuz, Club club) {
        Cancha nuevaCancha = new Cancha(id, numero, superficie, tieneLuz, club);
        club.agregarCancha(nuevaCancha);
        return nuevaCancha;
    }

    // Método para la creación de otros usuarios del sistema (Administradores, Recepcionistas o Jugadores)
    public Usuario crearUsuario(String tipoUsuario, int id, String nombre, String dni, LocalDate fechaNacimiento,
                                String domicilio, String telefono, String email, String username, String passwordHash, Jugador responsable) {
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
                        throw new IllegalArgumentException("El jugador es menor de edad y requiere obligatoriamente un responsable asignado.");
                    }
                    // Utiliza el constructor de menores (10 parámetros, sin credenciales)
                    return new Jugador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, false, null, null, responsable);
                } else {
                    // Utiliza el constructor de adultos (11 parámetros, con credenciales)
                    return new Jugador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash, false, null, null);
                }
            default:
                throw new IllegalArgumentException("Tipo de usuario desconocido: " + tipoUsuario);
        }
    }
}
