package com.starbank.modelo.repositorios.jdbc;

import com.starbank.excepciones.PersistenciaException;
import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Administrador;
import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.Rol;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UsuarioRepositorioJdbc implements UsuarioRepositorio {
    private final UnidadTrabajoJdbc unidadTrabajo;

    public UsuarioRepositorioJdbc(UnidadTrabajoJdbc unidadTrabajo) {
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
    }

    @Override
    public void guardar(Usuario usuario) {
        Validaciones.obligatorio(usuario, "El usuario");
        String sql = "INSERT INTO usuarios(identificacion,tipo_usuario,nombre,correo,telefono,clave_sal,clave_resumen,clave_iteraciones,fecha_registro,estado) VALUES(?,?,?,?,?,?,?,?,?,?)";
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                cargar(ps, usuario);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw mapearDuplicado(e, "usuario");
            }
            return null;
        });
    }

    @Override
    public void actualizar(Usuario usuario) {
        Validaciones.obligatorio(usuario, "El usuario");
        ejecutar(con -> {
            Usuario anterior = buscarPorIdentificacionInterno(con, usuario.obtenerIdentificacion())
                    .orElseThrow(() -> new RegistroNoEncontradoException("El usuario no existe."));
            if (anterior.obtenerRol() != usuario.obtenerRol() || !anterior.obtenerFechaRegistro().equals(usuario.obtenerFechaRegistro())) {
                throw new ValidacionException("No se puede reemplazar la identidad de un usuario.");
            }
            String sql = "UPDATE usuarios SET nombre=?, correo=?, telefono=?, clave_sal=?, clave_resumen=?, clave_iteraciones=?, estado=? WHERE identificacion=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, usuario.obtenerNombre());
                ps.setString(2, usuario.obtenerCorreo());
                ps.setString(3, usuario.obtenerTelefono());
                ps.setBytes(4, usuario.obtenerClave().obtenerSal());
                ps.setBytes(5, usuario.obtenerClave().obtenerResumen());
                ps.setInt(6, usuario.obtenerClave().obtenerIteraciones());
                ps.setString(7, usuario.obtenerEstado().name());
                ps.setString(8, usuario.obtenerIdentificacion());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw mapearDuplicado(e, "usuario");
            }
            return null;
        });
    }

    @Override
    public Optional<Usuario> buscarPorIdentificacion(String identificacion) {
        String id = Validaciones.texto(identificacion, "La identificación");
        return consultar(con -> buscarPorIdentificacionInterno(con, id));
    }

    @Override
    public Optional<Cliente> buscarClientePorDocumento(String documento) {
        return buscarPorIdentificacion(documento).filter(Cliente.class::isInstance).map(Cliente.class::cast);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        String c = Validaciones.correo(correo);
        return consultar(con -> {
            String sql = "SELECT * FROM usuarios WHERE LOWER(correo)=LOWER(?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c);
                try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
            } catch (SQLException e) { throw new PersistenciaException("Error consultando usuario por correo.", e); }
        });
    }

    @Override
    public List<Usuario> listar() {
        return consultar(con -> {
            String sql = "SELECT * FROM usuarios ORDER BY identificacion";
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                List<Usuario> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista;
            } catch (SQLException e) { throw new PersistenciaException("Error listando usuarios.", e); }
        });
    }

    private Optional<Usuario> buscarPorIdentificacionInterno(Connection con, String id) {
        String sql = "SELECT * FROM usuarios WHERE identificacion=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new PersistenciaException("Error consultando usuario.", e); }
    }

    private void cargar(PreparedStatement ps, Usuario u) throws SQLException {
        ps.setString(1, u.obtenerIdentificacion());
        ps.setString(2, u.obtenerRol() == Rol.CLIENTE ? "CLIENTE" : "ADMINISTRADOR");
        ps.setString(3, u.obtenerNombre());
        ps.setString(4, u.obtenerCorreo());
        ps.setString(5, u.obtenerTelefono());
        ps.setBytes(6, u.obtenerClave().obtenerSal());
        ps.setBytes(7, u.obtenerClave().obtenerResumen());
        ps.setInt(8, u.obtenerClave().obtenerIteraciones());
        ps.setTimestamp(9, Timestamp.valueOf(u.obtenerFechaRegistro()));
        ps.setString(10, u.obtenerEstado().name());
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        ClaveProtegida clave = ClaveProtegida.reconstruir(rs.getBytes("clave_sal"), rs.getBytes("clave_resumen"), rs.getInt("clave_iteraciones"));
        String tipo = rs.getString("tipo_usuario");
        String id = rs.getString("identificacion");
        String nombre = rs.getString("nombre");
        String correo = rs.getString("correo");
        String telefono = rs.getString("telefono");
        java.time.LocalDateTime fecha = rs.getTimestamp("fecha_registro").toLocalDateTime();
        EstadoUsuario estado = EstadoUsuario.valueOf(rs.getString("estado"));
        if ("ADMINISTRADOR".equals(tipo)) return new Administrador(id, nombre, correo, telefono, clave, fecha, estado);
        return new Cliente(id, nombre, correo, telefono, clave, fecha, estado);
    }

    private <T> T ejecutar(FuncionConexion<T> fn) {
        if (unidadTrabajo.hayTransaccionActiva()) return fn.aplicar(unidadTrabajo.obtenerConexion());
        try (Connection con = unidadTrabajo.obtenerConexionFueraDeTransaccion()) {
            con.setAutoCommit(true);
            return fn.aplicar(con);
        } catch (SQLException e) { throw new PersistenciaException("Error de conexión JDBC.", e); }
    }

    private <T> T consultar(FuncionConexion<T> fn) { return ejecutar(fn); }

    private RuntimeException mapearDuplicado(SQLException e, String entidad) {
        String estado = e.getSQLState();
        if (estado != null && estado.startsWith("23")) return new RegistroDuplicadoException("Ya existe un " + entidad + " con datos únicos duplicados.");
        return new PersistenciaException("Error de persistencia de " + entidad + ".", e);
    }

    @FunctionalInterface
    interface FuncionConexion<T> { T aplicar(Connection con); }
}
