package tenis.modelo;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public abstract class Usuario {
    protected int id;
    protected String nombre;
    protected String dni;
    protected LocalDate fechaNacimiento;
    protected String domicilio;
    protected String telefono;
    protected String email;
    protected String username;
    protected String passwordHash;

    // Constructor
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

    // Método abstracto para que cada tipo de usuario defina sus permisos específicos
    public abstract List<String> getPermisos();

    // Valida si el usuario cuenta con credenciales para iniciar sesión
    public boolean tieneCredenciales() {
        return this.username != null && !this.username.trim().isEmpty() &&
                this.passwordHash != null && !this.passwordHash.trim().isEmpty();
    }

    // Calcula si el usuario es menor de 18 años
    public boolean esMenor() {
        if (this.fechaNacimiento == null) {
            return false;
        }
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears() < 18;
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
