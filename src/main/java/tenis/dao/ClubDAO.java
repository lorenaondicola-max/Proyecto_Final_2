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

public class ClubDAO implements DAO<Club> {

    private static final String SQL_INSERTAR = "INSERT INTO club (id, nombre, direccion, hora_apertura, hora_cierre, inicio_hora_pico, fin_hora_pico, horas_cancelacion, porcentaje_penalizacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT * FROM club";
    private static final String SQL_BUSCAR = "SELECT * FROM club WHERE id = ?";
    private static final String SQL_ACTUALIZAR = "UPDATE club SET nombre = ?, direccion = ?, hora_apertura = ?, hora_cierre = ?, inicio_hora_pico = ?, fin_hora_pico = ?, horas_cancelacion = ?, porcentaje_penalizacion = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM club WHERE id = ?";

    @Override
    public void insertar(Club c) throws AccesoDatosException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getDireccion());
            ps.setTime(4, Time.valueOf(c.getHoraApertura()));
            ps.setTime(5, Time.valueOf(c.getHoraCierre()));
            ps.setTime(6, Time.valueOf(c.getInicioHoraPico()));
            ps.setTime(7, Time.valueOf(c.getFinHoraPico()));
            ps.setInt(8, c.getHorasCancelacion());
            ps.setDouble(9, c.getPorcentajePenalizacion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar el club ID: " + c.getId(), e);
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
            ps.setInt(9, c.getId());
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

    private Club crearClub(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String direccion = rs.getString("direccion");
        LocalTime horaApertura = rs.getTime("hora_apertura").toLocalTime();
        LocalTime horaCierre = rs.getTime("hora_cierre").toLocalTime();
        LocalTime inicioHoraPico = rs.getTime("inicio_hora_pico").toLocalTime();
        LocalTime finHoraPico = rs.getTime("fin_hora_pico").toLocalTime();
        int horasCancelacion = rs.getInt("horas_cancelacion");
        double porcentajePenalizacion = rs.getDouble("porcentaje_penalizacion");

        // Días de atención y tarifa se pueden inicializar por defecto o cargar según relaciones
        ArrayList<DayOfWeek> diasAtencion = new ArrayList<>();
        Tarifa tarifa = null;

        return new Club(id, nombre, direccion, horaApertura, horaCierre, inicioHoraPico, finHoraPico,
                diasAtencion, horasCancelacion, porcentajePenalizacion, tarifa);
    }
}
