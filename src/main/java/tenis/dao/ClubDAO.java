package tenis.dao;

import tenis.config.ConexionDB;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Club;
import tenis.modelo.Tarifa;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los clubes, sobre la tabla {@code club}.
 * La tarifa no tiene tabla propia: sus cinco valores se guardan como columnas del club.
 * Los días de atención se guardan en una sola columna, como texto separado por comas
 * (por ejemplo {@code MONDAY,TUESDAY}). La lista de canchas no se carga: las canchas
 * de un club se consultan con {@link CanchaDAO}.
 */
public class ClubDAO implements DAO<Club> {

    private static final String SQL_INSERTAR = "INSERT INTO club (nombre, direccion, hora_apertura, hora_cierre, inicio_hora_pico, fin_hora_pico, horas_cancelacion, porcentaje_penalizacion, precio_base_hora, recargo_polvo_porcentaje, recargo_pico_porcentaje, recargo_dobles_porcentaje, descuento_socio_porcentaje, dias_atencion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT * FROM club";
    private static final String SQL_BUSCAR = "SELECT * FROM club WHERE id = ?";
    private static final String SQL_ACTUALIZAR = "UPDATE club SET nombre = ?, direccion = ?, hora_apertura = ?, hora_cierre = ?, inicio_hora_pico = ?, fin_hora_pico = ?, horas_cancelacion = ?, porcentaje_penalizacion = ?, precio_base_hora = ?, recargo_polvo_porcentaje = ?, recargo_pico_porcentaje = ?, recargo_dobles_porcentaje = ?, descuento_socio_porcentaje = ?, dias_atencion = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM club WHERE id = ?";

    @Override
    public void insertar(Club c) throws AccesoDatosException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDireccion());
            ps.setTime(3, Time.valueOf(c.getHoraApertura()));
            ps.setTime(4, Time.valueOf(c.getHoraCierre()));
            ps.setTime(5, Time.valueOf(c.getInicioHoraPico()));
            ps.setTime(6, Time.valueOf(c.getFinHoraPico()));
            ps.setInt(7, c.getHorasCancelacion());
            ps.setDouble(8, c.getPorcentajePenalizacion());
            ps.setDouble(9, c.getTarifa().getPrecioBaseHora());
            ps.setDouble(10, c.getTarifa().getRecargoPolvoPorcentaje());
            ps.setDouble(11, c.getTarifa().getRecargoPicoPorcentaje());
            ps.setDouble(12, c.getTarifa().getRecargoDoblesPorcentaje());
            ps.setDouble(13, c.getTarifa().getDescuentoSocioPorcentaje());
            ps.setString(14, diasATexto(c.getDiasAtencion()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar el club", e);
        }
    }

    @Override
    public List<Club> listarTodos() throws AccesoDatosException {
        List<Club> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearClub(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar los clubes", e);
        }
        return lista;
    }

    @Override
    public Club buscarPorId(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearClub(rs);
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar el club con ID: " + id, e);
        }
        throw new ElementoNoEncontradoException("No se encontró el club con ID: " + id);
    }

    @Override
    public void actualizar(Club c) throws AccesoDatosException, ElementoNoEncontradoException {
        int filas;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDireccion());
            ps.setTime(3, Time.valueOf(c.getHoraApertura()));
            ps.setTime(4, Time.valueOf(c.getHoraCierre()));
            ps.setTime(5, Time.valueOf(c.getInicioHoraPico()));
            ps.setTime(6, Time.valueOf(c.getFinHoraPico()));
            ps.setInt(7, c.getHorasCancelacion());
            ps.setDouble(8, c.getPorcentajePenalizacion());

            ps.setDouble(9, c.getTarifa().getPrecioBaseHora());
            ps.setDouble(10, c.getTarifa().getRecargoPolvoPorcentaje());
            ps.setDouble(11, c.getTarifa().getRecargoPicoPorcentaje());
            ps.setDouble(12, c.getTarifa().getRecargoDoblesPorcentaje());
            ps.setDouble(13, c.getTarifa().getDescuentoSocioPorcentaje());
            ps.setString(14, diasATexto(c.getDiasAtencion()));

            ps.setInt(15, c.getId());

            filas = ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar el club ID: " + c.getId(), e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo actualizar, no existe el club con ID: " + c.getId());
        }
    }

    @Override
    public void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        int filas;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            filas = ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al eliminar el club con ID: " + id, e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo eliminar, no existe el club con ID: " + id);
        }
    }

    /**
     * Arma un club, con su tarifa, a partir de la fila actual del resultado.
     *
     * @param rs resultado posicionado en la fila a leer
     * @return el club reconstruido
     * @throws SQLException si falta alguna columna o falla la lectura
     */
    private Club crearClub(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String direccion = rs.getString("direccion");
        LocalTime horaApertura = rs.getTime("hora_apertura").toLocalTime();
        LocalTime horaCierre = rs.getTime("hora_cierre").toLocalTime();
        LocalTime inicioPico = rs.getTime("inicio_hora_pico").toLocalTime();
        LocalTime finPico = rs.getTime("fin_hora_pico").toLocalTime();
        int horasCancelacion = rs.getInt("horas_cancelacion");
        double porcentajePenalizacion = rs.getDouble("porcentaje_penalizacion");

        double precioBase = rs.getDouble("precio_base_hora");
        double recargoPolvo = rs.getDouble("recargo_polvo_porcentaje");
        double recargoPico = rs.getDouble("recargo_pico_porcentaje");
        double recargoDobles = rs.getDouble("recargo_dobles_porcentaje");
        double descuentoSocio = rs.getDouble("descuento_socio_porcentaje");

        Tarifa tarifa = new Tarifa(precioBase, recargoPolvo, recargoPico, recargoDobles, descuentoSocio);
        ArrayList<DayOfWeek> dias = textoADias(rs.getString("dias_atencion"));

        Club club = new Club(id, nombre, direccion, horaApertura, horaCierre, inicioPico, finPico, dias, horasCancelacion, porcentajePenalizacion, tarifa);

        return club;
    }

    /**
     * Convierte la lista de días de atención en el texto que se guarda en la base.
     *
     * @param dias días de atención del club
     * @return los nombres de los días separados por coma; texto vacío si no hay días
     */
    private String diasATexto(List<DayOfWeek> dias) {
        List<String> nombres = new ArrayList<>();
        for (DayOfWeek dia : dias) {
            nombres.add(dia.name());
        }
        return String.join(",", nombres);
    }

    /**
     * Convierte el texto guardado en la base en la lista de días de atención.
     *
     * @param texto nombres de los días separados por coma
     * @return los días de atención; lista vacía si el texto está vacío
     */
    private ArrayList<DayOfWeek> textoADias(String texto) {
        ArrayList<DayOfWeek> dias = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return dias;
        }
        for (String nombre : texto.split(",")) {
            dias.add(DayOfWeek.valueOf(nombre.trim()));
        }
        return dias;
    }
}
