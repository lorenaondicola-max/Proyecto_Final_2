package tenis.dao;

import tenis.config.ConexionDB;
import tenis.enums.Superficie;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Cancha;
import tenis.modelo.Club;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CanchaDAO implements DAO<Cancha> {

    private static final String SQL_INSERTAR = "INSERT INTO canchas (id, numero, superficie, tiene_luz, club_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT * FROM canchas ORDER BY numero";
    private static final String SQL_BUSCAR = "SELECT * FROM canchas WHERE id = ?";
    private static final String SQL_ACTUALIZAR = "UPDATE canchas SET numero = ?, superficie = ?, tiene_luz = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM canchas WHERE id = ?";

    @Override
    public void insertar(Cancha c) throws AccesoDatosException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
            ps.setInt(1, c.getId());
            ps.setInt(2, c.getNumero());
            ps.setString(3, c.getSuperficie().name());
            ps.setBoolean(4, c.isTieneLuz());
            ps.setInt(5, c.getClub().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar la cancha ID: " + c.getId(), e);
        }
    }

    @Override
    public List<Cancha> listarTodos() throws AccesoDatosException {
        List<Cancha> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearCancha(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar las canchas", e);
        }
        return lista;
    }

    @Override
    public Cancha buscarPorId(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearCancha(rs);
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar la cancha con ID: " + id, e);
        }
        throw new ElementoNoEncontradoException("No se encontró la cancha con ID: " + id);
    }

    @Override
    public void actualizar(Cancha c) throws AccesoDatosException, ElementoNoEncontradoException {
        int filas;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setInt(1, c.getNumero());
            ps.setString(2, c.getSuperficie().name());
            ps.setBoolean(3, c.isTieneLuz());
            ps.setInt(4, c.getId());
            filas = ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar la cancha ID: " + c.getId(), e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo actualizar, no existe la cancha con ID: " + c.getId());
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
            throw new AccesoDatosException("Error al eliminar la cancha con ID: " + id, e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo eliminar, no existe la cancha con ID: " + id);
        }
    }

    private Cancha crearCancha(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int numero = rs.getInt("numero");
        Superficie superficie = Superficie.valueOf(rs.getString("superficie"));
        boolean tieneLuz = rs.getBoolean("tiene_luz");

        // Nota: El club asociado se puede obtener consultando a su vez al ClubDAO si es necesario,
        // o pasando una instancia básica según el diseño de relaciones de tu base de datos.
        Club club = null;

        return new Cancha(id, numero, superficie, tieneLuz, club);
    }
}
