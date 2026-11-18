package tenis.menu;

import tenis.excepciones.AccesoDatosException;
import tenis.modelo.Cancha;
import tenis.modelo.Jugador;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú de un jugador. Tiene solo las opciones comunes: puede reservar en cualquier club,
 * pero únicamente para sí mismo o para los menores a su cargo.
 */
public class MenuJugador extends Menu {

    private final Jugador jugador;

    /**
     * @param jugador jugador que inició sesión
     */
    public MenuJugador(Jugador jugador) {
        super(jugador);
        this.jugador = jugador;
    }

    @Override
    protected String titulo() {
        return "MENÚ DEL JUGADOR";
    }

    /**
     * @return el propio jugador y los menores de los que es responsable
     */
    @Override
    protected List<Jugador> jugadoresHabilitados() throws AccesoDatosException {
        List<Jugador> resultado = new ArrayList<>();
        resultado.add(jugador);
        for (Jugador j : gestorJugador.listarTodos()) {
            if (jugador.esResponsableDe(j)) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    /**
     * @return las canchas de todos los clubes
     */
    @Override
    protected List<Cancha> canchasParaReservar() throws AccesoDatosException {
        return gestorCancha.listarTodos();
    }
}
