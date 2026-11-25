package com.starbank.modelo.repositorios.jdbc;

import com.starbank.excepciones.PersistenciaException;
import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.enumeraciones.EstadoCuenta;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.modelo.repositorios.CuentaRepositorio;
import com.starbank.utilidades.Validaciones;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CuentaRepositorioJdbc implements CuentaRepositorio {
    private final UnidadTrabajoJdbc unidadTrabajo;

    public CuentaRepositorioJdbc(UnidadTrabajoJdbc unidadTrabajo) {
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
    }

    @Override
    public void guardar(Cuenta cuenta) {
        Validaciones.obligatorio(cuenta, "La cuenta");
        if (cuenta.consultarSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidacionException("Una cuenta nueva debe tener saldo cero.");
        }
        String sql = "INSERT INTO cuentas(numero_cuenta,tipo_cuenta,identificacion_cliente,fecha_creacion,saldo,estado) VALUES(?,?,?,?,?,?)";
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cuenta.obtenerNumeroCuenta());
                ps.setString(2, cuenta.obtenerTipoCuenta().name());
                ps.setString(3, cuenta.obtenerCliente().obtenerIdentificacion());
                ps.setTimestamp(4, Timestamp.valueOf(cuenta.obtenerFechaCreacion()));
                ps.setBigDecimal(5, cuenta.consultarSaldo());
                ps.setString(6, cuenta.obtenerEstado().name());
                ps.executeUpdate();
            } catch (SQLException e) { throw mapearDuplicado(e); }
            return null;
        });
    }

    @Override
    public void actualizar(Cuenta cuenta) {
        Validaciones.obligatorio(cuenta, "La cuenta");
        ejecutar(con -> {
            Cuenta anterior = buscarInterno(con, cuenta.obtenerNumeroCuenta(), true)
                    .orElseThrow(() -> new RegistroNoEncontradoException("La cuenta no existe."));
            if (!anterior.obtenerCliente().obtenerIdentificacion().equals(cuenta.obtenerCliente().obtenerIdentificacion())
                    || anterior.obtenerTipoCuenta() != cuenta.obtenerTipoCuenta()
                    || !anterior.obtenerFechaCreacion().equals(cuenta.obtenerFechaCreacion())) {
                throw new ValidacionException("No se puede reemplazar la identidad de una cuenta.");
            }
            String sql = "UPDATE cuentas SET saldo=?, estado=? WHERE numero_cuenta=? AND saldo >= 0";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBigDecimal(1, cuenta.consultarSaldo());
                ps.setString(2, cuenta.obtenerEstado().name());
                ps.setString(3, cuenta.obtenerNumeroCuenta());
                ps.executeUpdate();
            } catch (SQLException e) { throw new PersistenciaException("Error actualizando cuenta.", e); }
            return null;
        });
    }

    @Override
    public Optional<Cuenta> buscarPorNumero(String numero) {
        String n = Validaciones.texto(numero, "El número de cuenta");
        return consultar(con -> buscarInterno(con, n, unidadTrabajo.hayTransaccionActiva()));
    }

    @Override
    public List<Cuenta> buscarPorCliente(String identificacion) {
        String id = Validaciones.texto(identificacion, "La identificación");
        return consultar(con -> listarPorSql(con, "WHERE c.identificacion_cliente=? ORDER BY c.numero_cuenta", ps -> ps.setString(1, id)));
    }

    @Override
    public List<Cuenta> listar() {
        return consultar(con -> listarPorSql(con, "ORDER BY c.numero_cuenta", ps -> {}));
    }

    private Optional<Cuenta> buscarInterno(Connection con, String numero, boolean bloquear) {
        String sql = "SELECT c.*, u.identificacion uid, u.nombre, u.correo, u.telefono, u.clave_sal, u.clave_resumen, u.clave_iteraciones, u.fecha_registro ufecha, u.estado uestado "
                + "FROM cuentas c JOIN usuarios u ON u.identificacion=c.identificacion_cliente WHERE c.numero_cuenta=?" + (bloquear ? " FOR UPDATE OF c" : "");
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new PersistenciaException("Error consultando cuenta.", e); }
    }

    private List<Cuenta> listarPorSql(Connection con, String whereOrder, Cargador cargador) {
        String sql = "SELECT c.*, u.identificacion uid, u.nombre, u.correo, u.telefono, u.clave_sal, u.clave_resumen, u.clave_iteraciones, u.fecha_registro ufecha, u.estado uestado "
                + "FROM cuentas c JOIN usuarios u ON u.identificacion=c.identificacion_cliente " + whereOrder;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            cargador.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Cuenta> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista;
            }
        } catch (SQLException e) { throw new PersistenciaException("Error listando cuentas.", e); }
    }

    private Cuenta mapear(ResultSet rs) throws SQLException {
        ClaveProtegida clave = ClaveProtegida.reconstruir(rs.getBytes("clave_sal"), rs.getBytes("clave_resumen"), rs.getInt("clave_iteraciones"));
        Cliente cliente = new Cliente(rs.getString("uid"), rs.getString("nombre"), rs.getString("correo"), rs.getString("telefono"), clave,
                rs.getTimestamp("ufecha").toLocalDateTime(), EstadoUsuario.valueOf(rs.getString("uestado")));
        return new Cuenta(rs.getString("numero_cuenta"), TipoCuenta.valueOf(rs.getString("tipo_cuenta")), cliente,
                rs.getTimestamp("fecha_creacion").toLocalDateTime(), rs.getBigDecimal("saldo"), EstadoCuenta.valueOf(rs.getString("estado")));
    }

    private <T> T ejecutar(FuncionConexion<T> fn) {
        if (unidadTrabajo.hayTransaccionActiva()) return fn.aplicar(unidadTrabajo.obtenerConexion());
        try (Connection con = unidadTrabajo.obtenerConexionFueraDeTransaccion()) { con.setAutoCommit(true); return fn.aplicar(con); }
        catch (SQLException e) { throw new PersistenciaException("Error de conexión JDBC.", e); }
    }

    private <T> T consultar(FuncionConexion<T> fn) { return ejecutar(fn); }

    private RuntimeException mapearDuplicado(SQLException e) {
        String estado = e.getSQLState();
        if (estado != null && estado.startsWith("23")) return new RegistroDuplicadoException("El número de cuenta ya está registrado.");
        return new PersistenciaException("Error de persistencia de cuenta.", e);
    }

    @FunctionalInterface interface FuncionConexion<T> { T aplicar(Connection con); }
    @FunctionalInterface interface Cargador { void cargar(PreparedStatement ps) throws SQLException; }
}
