package com.starbank.modelo.repositorios.jdbc;

import com.starbank.excepciones.PersistenciaException;
import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.entidades.Transferencia;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import com.starbank.modelo.repositorios.TransaccionRepositorio;
import com.starbank.utilidades.Validaciones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TransaccionRepositorioJdbc implements TransaccionRepositorio {
    private final UnidadTrabajoJdbc unidadTrabajo;

    public TransaccionRepositorioJdbc(UnidadTrabajoJdbc unidadTrabajo) {
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
    }

    @Override
    public void guardar(Transaccion t) {
        Validaciones.obligatorio(t, "La transacción");
        String sql = "INSERT INTO transacciones(identificador,tipo,monto,fecha_hora,cuenta_origen,cuenta_destino,estado,descripcion) VALUES(?,?,?,?,?,?,?,?)";
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, t.obtenerIdentificador());
                ps.setString(2, t.obtenerTipo().name());
                ps.setBigDecimal(3, t.obtenerMonto());
                ps.setTimestamp(4, Timestamp.valueOf(t.obtenerFechaHora()));
                ps.setString(5, t.obtenerCuentaOrigen());
                ps.setString(6, t.obtenerCuentaDestino());
                ps.setString(7, t.obtenerEstado().name());
                ps.setString(8, t.obtenerDescripcion());
                ps.executeUpdate();
            } catch (SQLException e) { throw mapearDuplicado(e); }
            return null;
        });
    }

    @Override
    public void actualizar(Transaccion nueva) {
        Validaciones.obligatorio(nueva, "La transacción");
        ejecutar(con -> {
            Transaccion anterior = buscarInterno(con, nueva.obtenerIdentificador())
                    .orElseThrow(() -> new RegistroNoEncontradoException("La transacción no existe."));
            if (anterior.obtenerEstado() != EstadoTransaccion.PENDIENTE
                    || nueva.obtenerEstado() == EstadoTransaccion.PENDIENTE
                    || anterior.obtenerTipo() != nueva.obtenerTipo()
                    || anterior.obtenerMonto().compareTo(nueva.obtenerMonto()) != 0
                    || !anterior.obtenerFechaHora().equals(nueva.obtenerFechaHora())
                    || !Objects.equals(anterior.obtenerCuentaOrigen(), nueva.obtenerCuentaOrigen())
                    || !Objects.equals(anterior.obtenerCuentaDestino(), nueva.obtenerCuentaDestino())
                    || !anterior.obtenerDescripcion().equals(nueva.obtenerDescripcion())) {
                throw new ValidacionException("El historial solo admite finalizar pendientes sin alterar sus datos.");
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE transacciones SET estado=? WHERE identificador=? AND estado='PENDIENTE'")) {
                ps.setString(1, nueva.obtenerEstado().name());
                ps.setString(2, nueva.obtenerIdentificador());
                ps.executeUpdate();
            } catch (SQLException e) { throw new PersistenciaException("Error actualizando transacción.", e); }
            return null;
        });
    }

    @Override
    public Optional<Transaccion> buscarPorIdentificador(String identificador) {
        String id = Validaciones.texto(identificador, "El identificador");
        return consultar(con -> buscarInterno(con, id));
    }

    @Override
    public List<Transaccion> buscarPorCuenta(String numero) {
        String n = Validaciones.texto(numero, "El número de cuenta");
        return consultar(con -> listarPorSql(con, "WHERE cuenta_origen=? OR cuenta_destino=? ORDER BY fecha_hora, identificador", ps -> { ps.setString(1, n); ps.setString(2, n); }));
    }

    @Override
    public List<Transaccion> listar() {
        return consultar(con -> listarPorSql(con, "ORDER BY fecha_hora, identificador", ps -> {}));
    }

    private Optional<Transaccion> buscarInterno(Connection con, String id) {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM transacciones WHERE identificador=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new PersistenciaException("Error consultando transacción.", e); }
    }

    private List<Transaccion> listarPorSql(Connection con, String whereOrder, Cargador cargador) {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM transacciones " + whereOrder)) {
            cargador.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaccion> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista;
            }
        } catch (SQLException e) { throw new PersistenciaException("Error listando transacciones.", e); }
    }

    private Transaccion mapear(ResultSet rs) throws SQLException {
        String id = rs.getString("identificador");
        TipoTransaccion tipo = TipoTransaccion.valueOf(rs.getString("tipo"));
        java.math.BigDecimal monto = rs.getBigDecimal("monto");
        java.time.LocalDateTime fecha = rs.getTimestamp("fecha_hora").toLocalDateTime();
        String origen = rs.getString("cuenta_origen");
        String destino = rs.getString("cuenta_destino");
        EstadoTransaccion estado = EstadoTransaccion.valueOf(rs.getString("estado"));
        String descripcion = rs.getString("descripcion");
        if (tipo == TipoTransaccion.TRANSFERENCIA) return new Transferencia(id, monto, fecha, origen, destino, estado, descripcion);
        return new Transaccion(id, tipo, monto, fecha, origen, destino, estado, descripcion);
    }

    private <T> T ejecutar(FuncionConexion<T> fn) {
        if (unidadTrabajo.hayTransaccionActiva()) return fn.aplicar(unidadTrabajo.obtenerConexion());
        try (Connection con = unidadTrabajo.obtenerConexionFueraDeTransaccion()) { con.setAutoCommit(true); return fn.aplicar(con); }
        catch (SQLException e) { throw new PersistenciaException("Error de conexión JDBC.", e); }
    }

    private <T> T consultar(FuncionConexion<T> fn) { return ejecutar(fn); }

    private RuntimeException mapearDuplicado(SQLException e) {
        String estado = e.getSQLState();
        if (estado != null && estado.startsWith("23")) return new RegistroDuplicadoException("La transacción ya existe.");
        return new PersistenciaException("Error de persistencia de transacción.", e);
    }

    @FunctionalInterface interface FuncionConexion<T> { T aplicar(Connection con); }
    @FunctionalInterface interface Cargador { void cargar(PreparedStatement ps) throws SQLException; }
}
