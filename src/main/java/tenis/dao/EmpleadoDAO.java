package tenis.dao;

import tenis.config.ConexionDB;
import tenis.excepciones.AccesoDatosException;
import tenis.excepciones.DatosInvalidosException;
import tenis.excepciones.ElementoNoEncontradoException;
import tenis.interfaces.DAO;
import tenis.modelo.Administrador;
import tenis.modelo.Club;
import tenis.modelo.Recepcionista;
import tenis.modelo.Usuario;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los administradores y recepcionistas, sobre la tabla {@code empleados}.
 * Las dos clases se guardan en la misma tabla, con una columna {@code tipo} que indica cuál es
 * (tabla única por jerarquía, como los extras). El club solo se completa para los recepcionistas;
 * al leerlo, se carga con {@link ClubDAO}.
 */
public class EmpleadoDAO implements DAO<Usuario> {

    private static final String TIPO_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String TIPO_RECEPCIONISTA = "RECEPCIONISTA";

    private static final String SQL_INSERTAR = "INSERT INTO empleados (tipo, nombre, dni, fecha_nacimiento, domicilio, telefono, email, username, password_hash, club_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT * FROM empleados ORDER BY nombre";
    private static final String SQL_BUSCAR = "SELECT * FROM empleados WHERE id = ?";
    private static final String SQL_ACTUALIZAR = "UPDATE empleados SET tipo = ?, nombre = ?, dni = ?, fecha_nacimiento = ?, domicilio = ?, telefono = ?, email = ?, username = ?, password_hash = ?, club_id = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM empleados WHERE id = ?";

    private final ClubDAO clubDAO = new ClubDAO();

    /**
     * Guarda un administrador o un recepcionista y le asigna el id que genera la base.
     *
     * @param u empleado a guardar
     * @throws AccesoDatosException si falla la comunicación con la base
     * @throws DatosInvalidosException si el usuario no es administrador ni recepcionista
     */
    @Override
    public void insertar(Usuario u) throws AccesoDatosException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            completarParametros(ps, u);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar el empleado", e);
        }
    }

    @Override
    public List<Usuario> listarTodos() throws AccesoDatosException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearEmpleado(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar los empleados", e);
        }
        return lista;
    }

    @Override
    public Usuario buscarPorId(int id) throws AccesoDatosException, ElementoNoEncontradoException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar el empleado con ID: " + id, e);
        }
        throw new ElementoNoEncontradoException("No se encontró el empleado con ID: " + id);
    }

    @Override
    public void actualizar(Usuario u) throws AccesoDatosException, ElementoNoEncontradoException {
        int filas;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            completarParametros(ps, u);
            ps.setInt(11, u.getId());
            filas = ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar el empleado ID: " + u.getId(), e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo actualizar, no existe el empleado con ID: " + u.getId());
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
            throw new AccesoDatosException("Error al eliminar el empleado con ID: " + id, e);
        }
        if (filas == 0) {
            throw new ElementoNoEncontradoException("No se pudo eliminar, no existe el empleado con ID: " + id);
        }
    }

    /**
     * Carga los diez primeros parámetros de las sentencias de inserción y actualización,
     * que son los mismos en las dos. El tipo y el club dependen de la subclase.
     *
     * @param ps sentencia a completar
     * @param u empleado del que se toman los datos
     * @throws SQLException si falla la carga de algún parámetro
     * @throws DatosInvalidosException si el usuario no es administrador ni recepcionista
     */
    private void completarParametros(PreparedStatement ps, Usuario u) throws SQLException {
        if (u instanceof Recepcionista r) {
            ps.setString(1, TIPO_RECEPCIONISTA);
            ps.setInt(10, r.getClub().getId());
        } else if (u instanceof Administrador) {
            ps.setString(1, TIPO_ADMINISTRADOR);
            ps.setNull(10, Types.INTEGER);
        } else {
            throw new DatosInvalidosException("Solo se guardan administradores y recepcionistas como empleados");
        }
        ps.setString(2, u.getNombre());
        ps.setString(3, u.getDni());
        ps.setDate(4, Date.valueOf(u.getFechaNacimiento()));
        ps.setString(5, u.getDomicilio());
        ps.setString(6, u.getTelefono());
        ps.setString(7, u.getEmail());
        ps.setString(8, u.getUsername());
        ps.setString(9, u.getPasswordHash());
    }

    /**
     * Arma un administrador o un recepcionista a partir de la fila actual del resultado,
     * según lo que indique la columna {@code tipo}. Al recepcionista le carga su club.
     *
     * @param rs resultado posicionado en la fila a leer
     * @return el empleado reconstruido
     * @throws SQLException si falla la lectura, el tipo es desconocido o no se puede cargar el club
     */
    private Usuario crearEmpleado(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String tipo = rs.getString("tipo");
        String nombre = rs.getString("nombre");
        String dni = rs.getString("dni");
        LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate();
        String domicilio = rs.getString("domicilio");
        String telefono = rs.getString("telefono");
        String email = rs.getString("email");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");

        if (TIPO_ADMINISTRADOR.equals(tipo)) {
            return new Administrador(id, nombre, dni, fechaNacimiento, domicilio, telefono, email,
                    username, passwordHash);
        }
        if (TIPO_RECEPCIONISTA.equals(tipo)) {
            int clubId = rs.getInt("club_id");
            Club club;
            try {
                club = clubDAO.buscarPorId(clubId);
            } catch (AccesoDatosException | ElementoNoEncontradoException e) {
                throw new SQLException("No se pudo cargar el club del recepcionista ID: " + id, e);
            }
            return new Recepcionista(id, nombre, dni, fechaNacimiento, domicilio, telefono, email,
                    username, passwordHash, club);
        }
        throw new SQLException("Tipo de empleado desconocido: " + tipo);
    }
}
