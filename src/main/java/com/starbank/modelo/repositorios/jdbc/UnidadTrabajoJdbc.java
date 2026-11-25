package com.starbank.modelo.repositorios.jdbc;

import com.starbank.configuracion.ConexionPostgres;
import com.starbank.excepciones.PersistenciaException;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import com.starbank.utilidades.Validaciones;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public final class UnidadTrabajoJdbc implements UnidadTrabajo {
    private final ConexionPostgres conexion;
    private final ThreadLocal<Connection> actual = new ThreadLocal<>();

    public UnidadTrabajoJdbc(ConexionPostgres conexion) {
        this.conexion = Validaciones.obligatorio(conexion, "La conexión PostgreSQL");
    }

    public Connection obtenerConexion() {
        Connection c = actual.get();
        if (c == null) {
            throw new PersistenciaException("No hay una transacción JDBC activa.");
        }
        return c;
    }

    public boolean hayTransaccionActiva() {
        return actual.get() != null;
    }

    public Connection obtenerConexionFueraDeTransaccion() {
        return conexion.abrir();
    }

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        if (actual.get() != null) {
            return operacion.get();
        }
        try (Connection c = conexion.abrir()) {
            c.setAutoCommit(false);
            actual.set(c);
            try {
                T resultado = operacion.get();
                c.commit();
                return resultado;
            } catch (RuntimeException | Error e) {
                try { c.rollback(); } catch (SQLException re) { e.addSuppressed(re); }
                throw e;
            } finally {
                actual.remove();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error en la transacción JDBC.", e);
        }
    }
}
