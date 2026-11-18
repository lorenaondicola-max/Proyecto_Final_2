package tenis.dao;

import tenis.config.ConexionDB;
import tenis.enums.MotivoBloqueo;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.modelo.Bloqueo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los bloqueos de cancha, sobre la tabla {@code bloqueos}.
 * No implementa la interfaz {@code DAO} porque un bloqueo no se maneja solo:
 * siempre pertenece a una cancha, y por eso se guarda y se lista a partir del id de la cancha.
 */
public class BloqueoDAO {

    private static final String SQL_INSERTAR = "INSERT INTO bloqueos (cancha_id, motivo, desde, hasta, detalle) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR_POR_CANCHA = "SELECT * FROM bloqueos WHERE cancha_id = ? ORDER BY desde";
    private static final String SQL_ELIMINAR = "DELETE FROM bloqueos WHERE id = ?";

    /**
     * Guarda un bloqueo de una cancha y le asigna el id que genera la base.
     *
     * @param b bloqueo a guardar
     * @param canchaId id de la cancha bloqueada
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public void insertar(Bloqueo b, int canchaId) throws AccesoDatosException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, canchaId);
            ps.setString(2, b.getMotivo().name());
            ps.setTimestamp(3, Timestamp.valueOf(b.getDesde()));
            ps.setTimestamp(4, Timestamp.valueOf(b.getHasta()));
            ps.setString(5, b.getDetalle());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    b.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar el bloqueo", e);
        }
    }

    /**
     * Lista los bloqueos de una cancha, ordenados por fecha de inicio.
     *
     * @param canchaId id de la cancha
     * @return los bloqueos de la cancha; lista vacía si no tiene
     * @throws AccesoDatosException si falla la comunicación con la base
     */
    public List<Bloqueo> listarPorCancha(int canchaId) throws AccesoDatosException {
        List<Bloqueo> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_CANCHA)) {
            ps.setInt(1, canchaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Bloqueo(
                            rs.getInt("id"),
                            MotivoBloqueo.valueOf(rs.getString("motivo")),
                            rs.getTimestamp("desde").toLocalDateTime(),
                            rs.getTimestamp("hasta").toLocalDateTime(),
                            rs.getString("detalle")));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar los bloqueos de la cancha ID: " + canchaId, e);
        }
        return lista;
    }

    /**
     * Elimina un bloqueo.
     *
     * @param id identificador del bloqueo a eliminar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws ElementoNoEncontradoException si no existe un bloqueo con ese id
     */
    public void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        int filas;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            filas = ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al eliminar el bloqueo con ID: " + id, e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No existe el bloqueo con ID: " + id);
        }
    }
}
