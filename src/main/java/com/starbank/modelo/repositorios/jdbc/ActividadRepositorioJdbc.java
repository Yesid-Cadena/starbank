package com.starbank.modelo.repositorios.jdbc;

import com.starbank.configuracion.ConexionPostgres;
import com.starbank.excepciones.PersistenciaException;
import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.modelo.entidades.RegistroActividad;
import com.starbank.modelo.repositorios.ActividadRepositorio;
import com.starbank.utilidades.Validaciones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class ActividadRepositorioJdbc implements ActividadRepositorio {
    private final ConexionPostgres conexion;

    public ActividadRepositorioJdbc(ConexionPostgres conexion) {
        this.conexion = Validaciones.obligatorio(conexion, "La conexión PostgreSQL");
    }

    @Override
    public void guardar(RegistroActividad registro) {
        Validaciones.obligatorio(registro, "El registro");
        String sql = "INSERT INTO registros_actividad(identificador,identificacion_usuario,tipo_actividad,fecha_hora,resultado,descripcion,recurso_afectado) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(true);
            ps.setString(1, registro.obtenerIdentificador());
            ps.setString(2, registro.obtenerIdentificacionUsuario());
            ps.setString(3, registro.obtenerTipoActividad());
            ps.setTimestamp(4, Timestamp.valueOf(registro.obtenerFechaHora()));
            ps.setString(5, registro.obtenerResultado());
            ps.setString(6, registro.obtenerDescripcion());
            ps.setString(7, registro.obtenerRecursoAfectado());
            ps.executeUpdate();
        } catch (SQLException e) {
            String estado = e.getSQLState();
            if (estado != null && estado.startsWith("23")) throw new RegistroDuplicadoException("El registro de actividad ya existe.");
            throw new PersistenciaException("Error guardando registro de auditoría.", e);
        }
    }

    @Override
    public List<RegistroActividad> listar() {
        String sql = "SELECT * FROM registros_actividad ORDER BY fecha_hora, identificador";
        try (Connection con = conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<RegistroActividad> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new RegistroActividad(
                        rs.getString("identificador"),
                        rs.getString("identificacion_usuario"),
                        rs.getString("tipo_actividad"),
                        rs.getTimestamp("fecha_hora").toLocalDateTime(),
                        rs.getString("resultado"),
                        rs.getString("descripcion"),
                        rs.getString("recurso_afectado")));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error listando registros de auditoría.", e);
        }
    }
}
