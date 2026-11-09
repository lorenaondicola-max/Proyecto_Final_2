package tenis.dao;

import tenis.config.ConexionDB;
import tenis.enums.EstadoReserva;
import tenis.enums.TipoJuego;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Cancha;
import tenis.modelo.Extra;
import tenis.modelo.Jugador;
import tenis.modelo.Reserva;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO implements DAO<Reserva> {


    private static final String SQL_INSERTAR = "INSERT INTO reservas (cancha_id, jugador_id, titular_id, inicio, fin, tipo_juego, estado, cargo_cancelacion, asistio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_INSERTAR_EXTRA = "INSERT INTO reserva_extras (id, reserva_id, precio_unitario) VALUES (?, ?, ?)";
    private static final String SQL_ELIMINAR_EXTRAS = "DELETE FROM reserva_extras WHERE reserva_id = ?";
    private static final String SQL_LISTAR_EXTRAS = "SELECT id, precio_unitario FROM reserva_extras WHERE reserva_id = ?";

    private static final String SQL_LISTAR = "SELECT * FROM reservas ORDER BY inicio";
    private static final String SQL_BUSCAR = "SELECT * FROM reservas WHERE id = ?";
    private static final String SQL_ACTUALIZAR = "UPDATE reservas SET cancha_id = ?, jugador_id = ?, titular_id = ?, inicio = ?, fin = ?, tipo_juego = ?, estado = ?, cargo_cancelacion = ?, asistio = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM reservas WHERE id = ?";

    private final CanchaDAO canchaDAO = new CanchaDAO();
    private final JugadorDAO jugadorDAO = new JugadorDAO();

    @Override
    public void insertar(Reserva r) throws AccesoDatosException {

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

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

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar la reserva", e);
        }

        if (r.getExtras() != null && !r.getExtras().isEmpty()) {
            for (Extra extra : r.getExtras()) {
                try (Connection con = ConexionDB.getConexion();
                     PreparedStatement psExtra = con.prepareStatement(SQL_INSERTAR_EXTRA)) {

                    psExtra.setInt(1, extra.getId());
                    psExtra.setInt(2, r.getId()); // 👈 Acá ahora usa el ID real de la reserva (ej: 1)
                    psExtra.setDouble(3, extra.getPrecioUnitario());

                    psExtra.executeUpdate();
                } catch (SQLException e) {
                    throw new AccesoDatosException("Error al insertar los extras de la reserva", e);
                }
            }
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
        // Reserva principal
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

        // 2. Sincronizamos los extras: borramos los viejos asociados y reinsertamos los actuales
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement psDel = con.prepareStatement(SQL_ELIMINAR_EXTRAS)) {
            psDel.setInt(1, r.getId());
            psDel.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al limpiar los extras antiguos de la reserva ID: " + r.getId(), e);
        }

        if (r.getExtras() != null && !r.getExtras().isEmpty()) {
            for (Extra extra : r.getExtras()) {
                try (Connection con = ConexionDB.getConexion();
                     PreparedStatement psExtra = con.prepareStatement(SQL_INSERTAR_EXTRA)) {
                    psExtra.setInt(1, extra.getId());
                    psExtra.setInt(2, r.getId());
                    psExtra.setDouble(3, extra.getPrecioUnitario());
                    psExtra.executeUpdate();
                } catch (SQLException e) {
                    throw new AccesoDatosException("Error al actualizar los extras de la reserva ID: " + r.getId(), e);
                }
            }
        }
    }

    @Override
    public void eliminar(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        // Borramos primero los extras asociados para evitar problemas de clave foránea
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement psDelExtra = con.prepareStatement(SQL_ELIMINAR_EXTRAS)) {
            psDelExtra.setInt(1, id);
            psDelExtra.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al eliminar los extras de la reserva ID: " + id, e);
        }

        // Borramos la reserva principal
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
        reserva.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
        reserva.setCargoCancelacion(rs.getDouble("cargo_cancelacion"));
        reserva.setAsistio(rs.getBoolean("asistio"));

        // Cargar los extras asociados a esta reserva
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement psExtras = con.prepareStatement(SQL_LISTAR_EXTRAS)) {
            psExtras.setInt(1, id);
            try (ResultSet rsExtras = psExtras.executeQuery()) {
                while (rsExtras.next()) {
                    int extraId = rsExtras.getInt("id");
                    double precioUnitario = rsExtras.getDouble("precio_unitario");

                    Extra extra = new Extra(extraId, precioUnitario, reserva) {
                        @Override
                        public double calcularCosto() {
                            return this.precioUnitario; // O el cálculo que corresponda
                        }
                    };

                    reserva.agregarExtra(extra);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar los extras de la reserva ID: " + id, e);
        }

        return reserva;
    }
}