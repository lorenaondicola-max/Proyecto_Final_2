package tenis.dao;

import tenis.config.ConexionDB;
import tenis.enums.EstadoReserva;
import tenis.enums.TipoJuego;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Cancha;
import tenis.modelo.Jugador;
import tenis.modelo.Reserva;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO implements DAO<Reserva> {

    private static final String SQL_INSERTAR = "INSERT INTO reservas (id, cancha_id, jugador_id, titular_id, inicio, fin, tipo_juego, estado, cargo_cancelacion, asistio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT * FROM reservas ORDER BY inicio";
    private static final String SQL_BUSCAR = "SELECT * FROM reservas WHERE id = ?";
    private static final String SQL_ACTUALIZAR = "UPDATE reservas SET cancha_id = ?, jugador_id = ?, titular_id = ?, inicio = ?, fin = ?, tipo_juego = ?, estado = ?, cargo_cancelacion = ?, asistio = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM reservas WHERE id = ?";

    private final CanchaDAO canchaDAO = new CanchaDAO();
    private final JugadorDAO jugadorDAO = new JugadorDAO();

    @Override
    public void insertar(Reserva r) throws AccesoDatosException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
            ps.setInt(1, r.getId());
            ps.setInt(2, r.getCancha().getId());
            ps.setInt(3, r.getJugador().getId());

            // Si el titular es distinto o existe, guardamos su ID; si no, manejamos nulo o el mismo jugador
            if (r.getTitular() != null) {
                ps.setInt(4, r.getTitular().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setTimestamp(5, Timestamp.valueOf(r.getInicio()));
            ps.setTimestamp(6, Timestamp.valueOf(r.getFin()));
            ps.setString(7, r.getTipoJuego().name());
            ps.setString(8, r.getEstado().name());
            ps.setDouble(9, r.getCargoCancelacion());
            ps.setBoolean(10, r.isAsistio());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar la reserva ID: " + r.getId(), e);
        }
    }

    @Override
    public List<Reserva> listarTodos() throws AccesoDatosException {
        List<Reserva> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearReserva(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar las reservas", e);
        }
        return lista;
    }

    @Override
    public Reserva buscarPorId(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearReserva(rs);
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar la reserva con ID: " + id, e);
        }
        throw new ElementoNoEncontradoException("No se encontró la reserva con ID: " + id);
    }

    @Override
    public void actualizar(Reserva r) throws AccesoDatosException, ElementoNoEncontradoException {
        int filas;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setInt(1, r.getCancha().getId());
            ps.setInt(2, r.getJugador().getId());

            if (r.getTitular() != null) {
                ps.setInt(3, r.getTitular().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setTimestamp(4, Timestamp.valueOf(r.getInicio()));
            ps.setTimestamp(5, Timestamp.valueOf(r.getFin()));
            ps.setString(6, r.getTipoJuego().name());
            ps.setString(7, r.getEstado().name());
            ps.setDouble(8, r.getCargoCancelacion());
            ps.setBoolean(9, r.isAsistio());
            ps.setInt(10, r.getId());

            filas = ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar la reserva ID: " + r.getId(), e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo actualizar, no existe la reserva con ID: " + r.getId());
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
            throw new AccesoDatosException("Error al eliminar la reserva con ID: " + id, e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo eliminar, no existe la reserva con ID: " + id);
        }
    }

    private Reserva crearReserva(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int canchaId = rs.getInt("cancha_id");
        int jugadorId = rs.getInt("jugador_id");

        int titularId = rs.getInt("titular_id");
        boolean tieneTitular = !rs.wasNull();

        LocalDateTime inicio = rs.getTimestamp("inicio").toLocalDateTime();
        LocalDateTime fin = rs.getTimestamp("fin").toLocalDateTime();
        TipoJuego tipoJuego = TipoJuego.valueOf(rs.getString("tipo_juego"));

        Cancha cancha;
        Jugador jugador;
        Jugador titular = null;

        try {
            cancha = canchaDAO.buscarPorId(canchaId);
            jugador = jugadorDAO.buscarPorId(jugadorId);
            if (tieneTitular) {
                titular = jugadorDAO.buscarPorId(titularId);
            }
        } catch (AccesoDatosException | ElementoNoEncontradoException e) {
            throw new SQLException("Error al asociar Cancha, Jugador o Titular en la reserva ID: " + id, e);
        }

        Reserva reserva = new Reserva(id, cancha, jugador, titular, inicio, fin, tipoJuego);

        // Seteamos los atributos adicionales si la base de datos ya los guardó (estado, cargo, asistencia)
        reserva.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
        reserva.setCargoCancelacion(rs.getDouble("cargo_cancelacion"));
        reserva.setAsistio(rs.getBoolean("asistio"));

        return reserva;
    }
}
