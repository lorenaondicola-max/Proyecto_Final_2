package tenis.menu;

import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.CanchaBloqueadaException;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.excepciones.MenorSinResponsableException;
import tenis.excepciones.TurnoNoDisponibleException;
import tenis.excepciones.UsuarioSinPermisoException;
import tenis.modelo.Administrador;
import tenis.modelo.Cancha;
import tenis.modelo.Club;
import tenis.modelo.Recepcionista;
import tenis.modelo.Tarifa;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Menú del administrador: todo lo del recepcionista, pero sobre todos los clubes,
 * más el alta de clubes y de recepcionistas.
 * Hereda de {@link MenuRecepcionista} y sobrescribe {@link #elegirClub()} y
 * {@link #canchasGestionables()}: el resto de las opciones del recepcionista
 * funcionan sin cambios.
 */
public class MenuAdministrador extends MenuRecepcionista {

    /**
     * @param administrador administrador que inició sesión
     */
    public MenuAdministrador(Administrador administrador) {
        super(administrador);
    }

    @Override
    protected String titulo() {
        return "MENÚ DEL ADMINISTRADOR";
    }

    /**
     * @return el club que el administrador elige de la lista
     */
    @Override
    protected Club elegirClub() throws AccesoDatosException, ElementoNoEncontradoException {
        return Consola.elegirDeLista("Clubes:", gestorClub.listarDesdeBD());
    }

    /**
     * @return las canchas de todos los clubes
     */
    @Override
    protected List<Cancha> canchasGestionables() throws AccesoDatosException {
        return gestorCancha.listarTodos();
    }

    @Override
    protected void mostrarOpciones() {
        super.mostrarOpciones();
        System.out.println("11. Registrar club");
        System.out.println("12. Registrar recepcionista");
    }

    @Override
    protected boolean ejecutarOpcion(int opcion)
            throws AccesoDatosException, ElementoNoEncontradoException, TurnoNoDisponibleException,
            CanchaBloqueadaException, MenorSinResponsableException, UsuarioSinPermisoException {
        switch (opcion) {
            case 11 -> registrarClub();
            case 12 -> registrarRecepcionista(elegirClub());
            default -> {
                return super.ejecutarOpcion(opcion);
            }
        }
        return true;
    }

    /**
     * Da de alta un club con sus horarios, su política de cancelación y su tarifa,
     * y ofrece registrar a continuación su recepcionista.
     */
    private void registrarClub() throws AccesoDatosException {
        String nombre = Consola.leerTexto("Nombre del club: ");
        String direccion = Consola.leerTexto("Dirección: ");
        LocalTime apertura = Consola.leerHora("Hora de apertura (HH:MM): ");
        LocalTime cierre = Consola.leerHora("Hora de cierre (HH:MM): ");
        if (!cierre.isAfter(apertura)) {
            throw new DatosInvalidosException("El club debe cerrar después de abrir");
        }
        LocalTime inicioPico = Consola.leerHora("Inicio de la hora pico (HH:MM): ");
        LocalTime finPico = Consola.leerHora("Fin de la hora pico (HH:MM): ");
        if (!finPico.isAfter(inicioPico)) {
            throw new DatosInvalidosException("La hora pico debe terminar después de empezar");
        }
        ArrayList<DayOfWeek> dias = leerDiasAtencion();
        int horasCancelacion = Consola.leerEntero("Horas de anticipación para cancelar sin cargo: ");
        double penalizacion = Consola.leerDecimal("Porcentaje que se cobra por cancelación tardía: ");

        System.out.println("Tarifa:");
        Tarifa tarifa = new Tarifa(
                Consola.leerDecimal("  Precio base por hora: "),
                Consola.leerDecimal("  Recargo por polvo de ladrillo (%): "),
                Consola.leerDecimal("  Recargo por hora pico (%): "),
                Consola.leerDecimal("  Recargo por dobles (%): "),
                Consola.leerDecimal("  Descuento para socios (%): "));

        Club nuevo = new Club(nombre, direccion, apertura, cierre, inicioPico, finPico, dias,
                horasCancelacion, penalizacion, tarifa);
        gestorClub.guardarEnBD(nuevo);
        System.out.println("Club registrado: " + nuevo);

        if (Consola.leerSiNo("¿Registrar ahora el recepcionista de este club?")) {
            registrarRecepcionista(nuevo);
        }
    }

    /**
     * Da de alta un recepcionista asignado a un club.
     *
     * @param club club en el que va a trabajar
     */
    private void registrarRecepcionista(Club club) throws AccesoDatosException {
        System.out.println("Nuevo recepcionista de " + club.getNombre() + ":");
        String nombre = Consola.leerTexto("Nombre y apellido: ", "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s']+$",
                "Error: El nombre solo debe contener letras, espacios y apóstrofes. Intente nuevamente.");
        String dni = leerDni();
        LocalDate nacimiento = leerFechaNacimiento();
        if (Period.between(nacimiento, LocalDate.now()).getYears() < 18) {
            throw new DatosInvalidosException("Un recepcionista debe ser mayor de edad");
        }
        String domicilio = Consola.leerTexto("Domicilio: ");
        String telefono = Consola.leerTexto("Teléfono: ", "^\\d+$",
                "Error: El teléfono solo debe contener números. Intente nuevamente.");
        String email = leerEmail();
        String username = leerUsernameLibre();
        String clave = Consola.leerTexto("Clave: ");

        Recepcionista nuevo = new Recepcionista(0, nombre, dni, nacimiento, domicilio, telefono, email,
                username, clave, club);
        gestorUsuarios.registrarRecepcionista(nuevo);
        System.out.println("Recepcionista registrado: " + nuevo);
    }

    /**
     * @return los días de atención elegidos: una de tres opciones habituales o uno por uno
     */
    private ArrayList<DayOfWeek> leerDiasAtencion() {
        System.out.println("Días de atención: 1. Todos  2. Lunes a sábado  3. Lunes a viernes  4. Elegir uno por uno");
        ArrayList<DayOfWeek> dias = new ArrayList<>();
        while (true) {
            int opcion = Consola.leerEntero("Opción: ");
            if (opcion >= 1 && opcion <= 3) {
                // Los valores de DayOfWeek van de MONDAY a SUNDAY: se toman los primeros 7, 6 o 5
                int cantidad = 8 - opcion;
                for (int i = 0; i < cantidad; i++) {
                    dias.add(DayOfWeek.values()[i]);
                }
                return dias;
            }
            if (opcion == 4) {
                for (DayOfWeek dia : DayOfWeek.values()) {
                    if (Consola.leerSiNo("¿Atiende el " + dia.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")) + "?")) {
                        dias.add(dia);
                    }
                }
                if (!dias.isEmpty()) {
                    return dias;
                }
                System.out.println("Tiene que atender al menos un día.");
            } else {
                System.out.println("Opción inválida.");
            }
        }
    }
}
