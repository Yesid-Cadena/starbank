package com.starbank.configuracion;

import com.starbank.excepciones.PersistenciaException;
import com.starbank.modelo.entidades.Administrador;
import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.repositorios.jdbc.ActividadRepositorioJdbc;
import com.starbank.modelo.repositorios.jdbc.CuentaRepositorioJdbc;
import com.starbank.modelo.repositorios.jdbc.TransaccionRepositorioJdbc;
import com.starbank.modelo.repositorios.jdbc.UnidadTrabajoJdbc;
import com.starbank.modelo.repositorios.jdbc.UsuarioRepositorioJdbc;
import com.starbank.modelo.repositorios.memoria.SesionRepositorioMemoria;
import com.starbank.modelo.servicios.AuditoriaServicio;
import com.starbank.modelo.servicios.AutenticacionServicio;
import com.starbank.modelo.servicios.AutorizacionServicio;
import com.starbank.modelo.servicios.ClienteServicio;
import com.starbank.modelo.servicios.CuentaServicio;
import com.starbank.modelo.servicios.SesionServicio;
import com.starbank.modelo.servicios.TransaccionServicio;
import com.starbank.utilidades.Validaciones;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.stream.Collectors;

public final class ConfiguracionPostgres implements ConfiguracionServicios {
    private final ConexionPostgres conexion;
    private final UnidadTrabajoJdbc unidadTrabajo;
    private final UsuarioRepositorioJdbc usuarios;
    private final ActividadRepositorioJdbc repositorioActividad;
    private final AuditoriaServicio auditoria;
    private final SesionServicio sesiones;
    private final AutorizacionServicio autorizacion;
    private final AutenticacionServicio autenticacion;
    private final ClienteServicio clientes;
    private final CuentaServicio cuentas;
    private final TransaccionServicio transacciones;

    public ConfiguracionPostgres(ConexionPostgres conexion) {
        this(conexion, Clock.systemDefaultZone());
    }

    public ConfiguracionPostgres(ConexionPostgres conexion, Clock reloj) {
        this.conexion = Validaciones.obligatorio(conexion, "La conexión PostgreSQL");
        this.unidadTrabajo = new UnidadTrabajoJdbc(conexion);
        this.usuarios = new UsuarioRepositorioJdbc(unidadTrabajo);
        CuentaRepositorioJdbc repositorioCuentas = new CuentaRepositorioJdbc(unidadTrabajo);
        TransaccionRepositorioJdbc repositorioTransacciones = new TransaccionRepositorioJdbc(unidadTrabajo);
        this.repositorioActividad = new ActividadRepositorioJdbc(conexion);
        this.auditoria = new AuditoriaServicio(repositorioActividad, reloj);
        this.sesiones = new SesionServicio(new SesionRepositorioMemoria(), new java.security.SecureRandom(), reloj, java.time.Duration.ofMinutes(30));
        this.autorizacion = new AutorizacionServicio(sesiones, usuarios, repositorioCuentas, auditoria);
        this.autenticacion = new AutenticacionServicio(usuarios, sesiones, auditoria, unidadTrabajo, reloj, 5, java.time.Duration.ofMinutes(5));
        this.clientes = new ClienteServicio(usuarios, unidadTrabajo, autorizacion, auditoria, sesiones);
        this.cuentas = new CuentaServicio(repositorioCuentas, usuarios, unidadTrabajo, autorizacion, auditoria);
        this.transacciones = new TransaccionServicio(repositorioCuentas, repositorioTransacciones, cuentas, unidadTrabajo, autorizacion);
    }

    public void inicializarEsquema() {
        String sql = cargarRecurso("/sql/schema.sql");
        try (Connection con = conexion.abrir(); Statement st = con.createStatement()) {
            con.setAutoCommit(true);
            for (String sentencia : sql.split(";")) {
                String s = sentencia.trim();
                if (!s.isEmpty()) st.execute(s);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error inicializando el esquema PostgreSQL.", e);
        }
    }

    public void verificarConexion() {
        try (Connection con = conexion.abrir()) {
            if (con == null || con.isClosed()) throw new PersistenciaException("Conexión PostgreSQL inválida.");
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a PostgreSQL. Verifica STARBANK_DB_URL, STARBANK_DB_USER y STARBANK_DB_PASSWORD.", e);
        }
    }

    @Override
    public Administrador crearAdministrador(String identificacion, String nombre, String correo, String telefono, char[] clave) {
        Administrador admin = new Administrador(identificacion, nombre, correo, telefono, ClaveProtegida.crear(clave));
        unidadTrabajo.ejecutar(() -> { usuarios.guardar(admin); return null; });
        return admin;
    }

    private String cargarRecurso(String ruta) {
        try (InputStream is = ConfiguracionPostgres.class.getResourceAsStream(ruta)) {
            if (is == null) throw new PersistenciaException("No se encontró el recurso SQL: " + ruta);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new PersistenciaException("Error leyendo recurso SQL.", e);
        }
    }

    public UnidadTrabajoJdbc obtenerUnidadTrabajo() { return unidadTrabajo; }
    public UsuarioRepositorioJdbc obtenerUsuarios() { return usuarios; }
    @Override public AuditoriaServicio obtenerAuditoria() { return auditoria; }
    @Override public SesionServicio obtenerSesiones() { return sesiones; }
    @Override public AutorizacionServicio obtenerAutorizacion() { return autorizacion; }
    @Override public AutenticacionServicio obtenerAutenticacion() { return autenticacion; }
    @Override public ClienteServicio obtenerClientes() { return clientes; }
    @Override public CuentaServicio obtenerCuentas() { return cuentas; }
    @Override public TransaccionServicio obtenerTransacciones() { return transacciones; }
}
