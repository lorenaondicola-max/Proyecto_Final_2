package tenis.main;

import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.CredencialesInvalidasException;
import tenis.gestor.GestorUsuarios;
import tenis.menu.Consola;
import tenis.menu.Menu;
import tenis.menu.MenuAdministrador;
import tenis.menu.MenuJugador;
import tenis.menu.MenuRecepcionista;
import tenis.modelo.Administrador;
import tenis.modelo.Jugador;
import tenis.modelo.Recepcionista;
import tenis.modelo.Usuario;

/**
 * Punto de entrada del sistema de turnos de tenis.
 * Pide usuario y clave y, según el tipo de usuario que inició sesión, abre su menú.
 * Al cerrar sesión vuelve a pedir credenciales, hasta que se elige salir.
 * Antes de ejecutarlo hay que correr {@code script/tenis_db.sql}, que crea la base
 * con los datos de prueba (el administrador es {@code admin} / {@code admin123}).
 */
public class App {

    public static void main(String[] args) {
        GestorUsuarios gestorUsuarios = new GestorUsuarios();
        System.out.println("=== SISTEMA DE TURNOS DE TENIS ===");

        while (true) {
            System.out.println("\n1. Iniciar sesión");
            System.out.println("0. Salir");
            int opcion = Consola.leerEntero("Opción: ");
            if (opcion == 0) {
                System.out.println("Hasta luego.");
                return;
            }
            if (opcion != 1) {
                System.out.println("Opción inválida.");
                continue;
            }

            String username = Consola.leerTexto("Usuario: ");
            String clave = Consola.leerTexto("Clave: ");
            try {
                Usuario usuario = gestorUsuarios.login(username, clave);
                crearMenu(usuario).ejecutar();
            } catch (CredencialesInvalidasException e) {
                System.out.println(e.getMessage());
            } catch (AccesoDatosException e) {
                System.out.println("No se pudo conectar con la base de datos: " + e.getMessage());
            }
        }
    }

    /**
     * Crea el menú que corresponde al tipo de usuario.
     * Es el único lugar del programa que pregunta de qué clase es el usuario:
     * a partir de acá, cada menú y cada permiso se resuelven por polimorfismo.
     *
     * @param usuario usuario que inició sesión
     * @return el menú de su tipo
     */
    private static Menu crearMenu(Usuario usuario) {
        if (usuario instanceof Administrador administrador) {
            return new MenuAdministrador(administrador);
        }
        if (usuario instanceof Recepcionista recepcionista) {
            return new MenuRecepcionista(recepcionista);
        }
        return new MenuJugador((Jugador) usuario);
    }
}
