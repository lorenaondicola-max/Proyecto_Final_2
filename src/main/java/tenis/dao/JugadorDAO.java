package tenis.dao;


import tenis.config.ConexionDB;
import tenis.enums.Categoria;
import tenis.enums.Nivel;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Jugador;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los jugadores, sobre la tabla {@code jugadores}.
 * El responsable de un menor es otro jugador: la tabla se referencia a sí misma
 * con la columna {@code responsable_id}, que queda en NULL para los adultos.
 * La cantidad de cancelaciones tardías y el historial no se guardan.
 */

public class JugadorDAO implements DAO<Jugador> {

    private static final String SQL_INSERTAR = "INSERT INTO jugadores (nombre, dni, fecha_nacimiento, domicilio, telefono, email, username, password_hash, es_socio, nivel, categoria, responsable_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT * FROM jugadores ORDER BY nombre";
    private static final String SQL_BUSCAR = "SELECT * FROM jugadores WHERE id = ?";
    private static final String SQL_ACTUALIZAR = "UPDATE jugadores SET nombre = ?, dni = ?, fecha_nacimiento = ?, domicilio = ?, telefono = ?, email = ?, username = ?, password_hash = ?, es_socio = ?, nivel = ?, categoria = ?, responsable_id = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM jugadores WHERE id = ?";

    @Override
    public void insertar(Jugador j) throws AccesoDatosException {

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, j.getNombre());
            ps.setString(2, j.getDni());
            ps.setDate(3, Date.valueOf(j.getFechaNacimiento()));
            ps.setString(4, j.getDomicilio());
            ps.setString(5, j.getTelefono());
            ps.setString(6, j.getEmail());
            ps.setString(7, j.getUsername());
            ps.setString(8, j.getPasswordHash());
            ps.setBoolean(9, j.isEsSocio());
            ps.setString(10, j.getNivel().name());
            ps.setString(11, j.getCategoria().name());

            if (j.getResponsable() != null) {
                ps.setInt(12, j.getResponsable().getId());
            } else {
                ps.setNull(12, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    j.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar el jugador", e);
        }
    }

    @Override
    public List<Jugador> listarTodos() throws AccesoDatosException {
        List<Jugador> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearJugador(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar los jugadores", e);
        }
        return lista;
    }

    @Override
    public Jugador buscarPorId(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearJugador(rs);
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar el jugador con ID: " + id, e);
        }
        throw new ElementoNoEncontradoException("No se encontró el jugador con ID: " + id);
    }

    @Override
    public void actualizar(Jugador j) throws AccesoDatosException, ElementoNoEncontradoException {
        int filas;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, j.getNombre());
            ps.setString(2, j.getDni());
            ps.setDate(3, Date.valueOf(j.getFechaNacimiento()));
            ps.setString(4, j.getDomicilio());
            ps.setString(5, j.getTelefono());
            ps.setString(6, j.getEmail());
            ps.setString(7, j.getUsername());
            ps.setString(8, j.getPasswordHash());
            ps.setBoolean(9, j.isEsSocio());
            ps.setString(10, j.getNivel().name());
            ps.setString(11, j.getCategoria().name());

            if (j.getResponsable() != null) {
                ps.setInt(12, j.getResponsable().getId());
            } else {
                ps.setNull(12, Types.INTEGER);
            }

            ps.setInt(13, j.getId());
            filas = ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar el jugador ID: " + j.getId(), e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo actualizar, no existe el jugador con ID: " + j.getId());
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
            throw new AccesoDatosException("Error al eliminar el jugador con ID: " + id, e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo eliminar, no existe el jugador con ID: " + id);
        }
    }


    /**
     * Arma un jugador a partir de la fila actual del resultado.
     * Si la fila tiene responsable, lo busca en la base y crea al jugador como menor,
     * sin credenciales; si no lo tiene, lo crea como adulto.
     *
     * @param rs resultado posicionado en la fila a leer
     * @return el jugador reconstruido
     * @throws SQLException si falla la lectura o no se puede cargar el responsable
     */

    private Jugador crearJugador(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String dni = rs.getString("dni");
        LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate();
        String domicilio = rs.getString("domicilio");
        String telefono = rs.getString("telefono");
        String email = rs.getString("email");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        boolean esSocio = rs.getBoolean("es_socio");
        Nivel nivel = Nivel.valueOf(rs.getString("nivel"));
        Categoria categoria = Categoria.valueOf(rs.getString("categoria"));

        int responsableId = rs.getInt("responsable_id");

        // Si no hay responsable_id (es NULL), es un jugador adulto
        if (rs.wasNull()) {
            return new Jugador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email,
                    username, passwordHash, esSocio, nivel, categoria);
        } else {
            // Si tiene responsable_id, se trata de un jugador menor de edad
            Jugador responsable = null;
            try {
                responsable = this.buscarPorId(responsableId);
            } catch (AccesoDatosException | ElementoNoEncontradoException e) {
                throw new SQLException("No se pudo cargar el responsable con ID: " + responsableId, e);
            }
            return new Jugador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email,
                    esSocio, nivel, categoria, responsable);
        }
    }
}