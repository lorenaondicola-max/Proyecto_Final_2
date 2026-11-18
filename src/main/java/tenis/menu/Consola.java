package tenis.menu;

import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.Identificable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Lectura de datos por teclado, compartida por todos los menús.
 * Cada método vuelve a preguntar hasta que el dato tenga un formato válido,
 * así que nunca devuelve un valor inválido ni corta el programa.
 * Es una clase utilitaria: solo tiene métodos static y no se instancia.
 */
public final class Consola {

    private static final Scanner sc = new Scanner(System.in);

    /** Constructor privado: la clase no se instancia. */
    private Consola() {
    }

    /**
     * @param mensaje texto que se muestra antes de leer
     * @return el número entero ingresado
     */
    public static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número.");
            }
        }
    }

    /**
     * @param mensaje texto que se muestra antes de leer
     * @return un número entero mayor que cero
     */
    public static int leerEnteroPositivo(String mensaje) {
        while (true) {
            int n = leerEntero(mensaje);
            if (n > 0) {
                return n;
            }
            System.out.println("Tiene que ser mayor que cero.");
        }
    }

    /**
     * @param mensaje texto que se muestra antes de leer
     * @return un número con decimales, cero o positivo; acepta punto o coma
     */
    public static double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                double n = Double.parseDouble(sc.nextLine().trim().replace(',', '.'));
                if (n >= 0) {
                    return n;
                }
                System.out.println("No puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número. Ejemplo: 5000 o 12,5");
            }
        }
    }

    /**
     * @param mensaje texto que se muestra antes de leer
     * @return el texto ingresado, sin espacios al principio ni al final; nunca vacío
     */
    public static String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = sc.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este dato es obligatorio.");
        }
    }

    /**
     * Lee un texto que debe cumplir una expresión regular.
     *
     * @param mensaje texto que se muestra antes de leer
     * @param formato expresión regular que el texto debe cumplir
     * @param error mensaje que se muestra si no la cumple
     * @return el texto ingresado, ya validado
     */
    public static String leerTexto(String mensaje, String formato, String error) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.matches(formato)) {
                return texto;
            }
            System.out.println(error);
        }
    }

    /**
     * @param pregunta texto de la pregunta
     * @return true si el usuario responde 1 (sí), false si responde 2 (no)
     */
    public static boolean leerSiNo(String pregunta) {
        while (true) {
            int n = leerEntero(pregunta + " 1 = Sí, 2 = No: ");
            if (n == 1 || n == 2) {
                return n == 1;
            }
            System.out.println("Respondé 1 o 2.");
        }
    }

    /**
     * @param mensaje texto que se muestra antes de leer
     * @return la fecha ingresada, en formato AAAA-MM-DD
     */
    public static LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return LocalDate.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Ejemplo: 2026-11-20");
            }
        }
    }

    /**
     * @param mensaje texto que se muestra antes de leer
     * @return la hora ingresada, en formato HH:MM
     */
    public static LocalTime leerHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return LocalTime.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Ejemplo: 10:00");
            }
        }
    }

    /**
     * Muestra los valores de un enum numerados desde 1 y devuelve el elegido.
     * Es un método genérico: sirve para cualquier enum (niveles, categorías, superficies,
     * motivos de bloqueo, tipos de juego) sin escribir un método para cada uno.
     *
     * @param titulo texto que se muestra antes de las opciones
     * @param valores los valores posibles, por ejemplo {@code Nivel.values()}
     * @param <E> tipo del enum
     * @return el valor elegido
     */
    public static <E extends Enum<E>> E elegirEnum(String titulo, E[] valores) {
        System.out.println(titulo);
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ". " + valores[i]);
        }
        while (true) {
            int n = leerEntero("Opción: ");
            if (n >= 1 && n <= valores.length) {
                return valores[n - 1];
            }
            System.out.println("Elegí un número entre 1 y " + valores.length + ".");
        }
    }

    /**
     * Muestra una lista de elementos y devuelve el que el usuario elige por su id.
     * Solo acepta ids que estén en la lista, así que sirve también para limitar
     * lo que cada usuario puede elegir. Es genérico con cota: funciona con cualquier
     * clase que implemente {@link Identificable}.
     *
     * @param titulo texto que se muestra antes de la lista
     * @param opciones elementos entre los que se elige
     * @param <T> tipo de los elementos
     * @return el elemento elegido
     * @throws ElementoNoEncontradoException si la lista está vacía
     */
    public static <T extends Identificable> T elegirDeLista(String titulo, List<T> opciones)
            throws ElementoNoEncontradoException {
        if (opciones.isEmpty()) {
            throw new ElementoNoEncontradoException("No hay elementos para elegir: " + titulo);
        }
        System.out.println(titulo);
        for (T opcion : opciones) {
            System.out.println("  " + opcion);
        }
        while (true) {
            int id = leerEntero("Id: ");
            for (T opcion : opciones) {
                if (opcion.getId() == id) {
                    return opcion;
                }
            }
            System.out.println("Ese id no está en la lista.");
        }
    }
}
