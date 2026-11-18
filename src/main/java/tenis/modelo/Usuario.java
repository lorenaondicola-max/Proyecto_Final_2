package tenis.modelo;

import tenis.interfaces.Identificable;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Persona registrada en el sistema.
 * Es la clase base de {@link Administrador}, {@link Recepcionista} y {@link Jugador}:
 * reúne los datos personales y de acceso que comparten, y obliga a cada
 * subclase a definir sus propios permisos.
 * Es abstracta porque no existe un usuario "a secas": siempre es de alguno de esos tres tipos.
 */
public abstract class Usuario implements Identificable {
    protected int id;
    protected String nombre;
    protected String dni;
    protected LocalDate fechaNacimiento;
    protected String domicilio;
    protected String telefono;
    protected String email;
    protected String username;
    protected String passwordHash;

    /**
     * Crea un usuario con un id ya conocido.
     * Se usa al reconstruir un usuario leído de la base de datos.
     *
     * @param id identificador asignado por la base de datos
     * @param username nombre de usuario; puede ser null si no tiene acceso al sistema
     * @param passwordHash clave de acceso; puede ser null si no tiene acceso al sistema
     */
    public Usuario(int id, String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email, String username, String passwordHash) {
        this.id = id;
        this.nombre = nombre;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.domicilio = domicilio;
        this.telefono = telefono;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Crea un usuario nuevo, todavía sin guardar.
     * El id queda en 0 hasta que la base de datos le asigne uno.
     */
    public Usuario(String nombre, String dni, LocalDate fechaNacimiento,
                   String domicilio, String telefono, String email, String username, String passwordHash) {
        this(0, nombre, dni, fechaNacimiento, domicilio, telefono, email, username, passwordHash);
    }

    /**
     * Devuelve los permisos de este usuario. Cada subclase define los suyos.
     *
     * @return lista con los nombres de los permisos; vacía si no tiene acceso al sistema
     */
    public abstract List<String> getPermisos();

    /**
     * Indica si este usuario puede reservar o cancelar un turno de ese jugador en esa cancha.
     * Cada subclase lo responde según su rol: el jugador solo para sí mismo y los menores
     * a su cargo, el recepcionista solo en las canchas de su club, y el administrador siempre.
     *
     * @param cancha cancha del turno
     * @param jugador jugador del turno
     * @return true si tiene permiso
     */
    public abstract boolean puedeGestionarReserva(Cancha cancha, Jugador jugador);

    /**
     * Indica si este usuario puede administrar una cancha: bloquearla, desbloquearla
     * o marcar como completadas sus reservas.
     *
     * @param cancha cancha a administrar
     * @return true si tiene permiso
     */
    public abstract boolean puedeGestionarCancha(Cancha cancha);

    /**
     * Indica si el usuario tiene menos de 18 años a la fecha de hoy.
     *
     * @return true si es menor; false si es mayor o si no tiene fecha de nacimiento cargada
     */
    public boolean esMenor() {
        if (this.fechaNacimiento == null) {
            return false;
        }
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears() < 18;
    }

    /**
     * Indica si el usuario puede iniciar sesión.
     *
     * @return true si tiene nombre de usuario y clave, ambos no vacíos
     */
    public boolean tieneCredenciales() {
        return this.username != null && !this.username.trim().isEmpty() &&
                this.passwordHash != null && !this.passwordHash.trim().isEmpty();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
