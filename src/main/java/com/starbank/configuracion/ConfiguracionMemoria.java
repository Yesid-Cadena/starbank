package com.starbank.configuracion;

import com.starbank.modelo.entidades.Administrador;
import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.repositorios.memoria.ActividadRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.AlmacenMemoria;
import com.starbank.modelo.repositorios.memoria.CuentaRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.SesionRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.TransaccionRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.UsuarioRepositorioMemoria;
import com.starbank.modelo.servicios.AuditoriaServicio;
import com.starbank.modelo.servicios.AutenticacionServicio;
import com.starbank.modelo.servicios.AutorizacionServicio;
import com.starbank.modelo.servicios.ClienteServicio;
import com.starbank.modelo.servicios.CuentaServicio;
import com.starbank.modelo.servicios.SesionServicio;
import com.starbank.modelo.servicios.TransaccionServicio;
import java.time.Clock;

/** Compone servicios en memoria. La creación de administradores es interna, no pública. */
public final class ConfiguracionMemoria implements ConfiguracionServicios {
    private final AlmacenMemoria almacen;
    private final UsuarioRepositorioMemoria usuarios;
    private final CuentaRepositorioMemoria repositorioCuentas;
    private final TransaccionRepositorioMemoria repositorioTransacciones;
    private final SesionRepositorioMemoria repositorioSesiones;
    private final ActividadRepositorioMemoria repositorioActividad;
    private final AuditoriaServicio auditoria;
    private final SesionServicio sesiones;
    private final AutorizacionServicio autorizacion;
    private final AutenticacionServicio autenticacion;
    private final ClienteServicio clientes;
    private final CuentaServicio cuentas;
    private final TransaccionServicio transacciones;

    public ConfiguracionMemoria() {
        this(Clock.systemDefaultZone());
    }

    public ConfiguracionMemoria(Clock reloj) {
        almacen = new AlmacenMemoria();
        usuarios = new UsuarioRepositorioMemoria(almacen);
        repositorioCuentas = new CuentaRepositorioMemoria(almacen);
        repositorioTransacciones = new TransaccionRepositorioMemoria(almacen);
        repositorioSesiones = new SesionRepositorioMemoria();
        repositorioActividad = new ActividadRepositorioMemoria();
        auditoria = new AuditoriaServicio(repositorioActividad, reloj);
        sesiones = new SesionServicio(repositorioSesiones, new java.security.SecureRandom(), reloj, java.time.Duration.ofMinutes(30));
        autorizacion = new AutorizacionServicio(sesiones, usuarios, repositorioCuentas, auditoria);
        autenticacion = new AutenticacionServicio(usuarios, sesiones, auditoria, almacen, reloj, 5, java.time.Duration.ofMinutes(5));
        clientes = new ClienteServicio(usuarios, almacen, autorizacion, auditoria, sesiones);
        cuentas = new CuentaServicio(repositorioCuentas, usuarios, almacen, autorizacion, auditoria);
        transacciones = new TransaccionServicio(repositorioCuentas, repositorioTransacciones, cuentas, almacen, autorizacion);
    }

    public Administrador crearAdministrador(String identificacion, String nombre, String correo, String telefono, char[] clave) {
        Administrador admin = new Administrador(identificacion, nombre, correo, telefono, ClaveProtegida.crear(clave));
        almacen.ejecutar(() -> {
            usuarios.guardar(admin);
            return null;
        });
        return admin;
    }

    public AlmacenMemoria obtenerAlmacen() { return almacen; }
    public UsuarioRepositorioMemoria obtenerUsuarios() { return usuarios; }
    public CuentaRepositorioMemoria obtenerRepositorioCuentas() { return repositorioCuentas; }
    public TransaccionRepositorioMemoria obtenerRepositorioTransacciones() { return repositorioTransacciones; }
    public SesionRepositorioMemoria obtenerRepositorioSesiones() { return repositorioSesiones; }
    public ActividadRepositorioMemoria obtenerRepositorioActividad() { return repositorioActividad; }
    public AuditoriaServicio obtenerAuditoria() { return auditoria; }
    public SesionServicio obtenerSesiones() { return sesiones; }
    public AutorizacionServicio obtenerAutorizacion() { return autorizacion; }
    public AutenticacionServicio obtenerAutenticacion() { return autenticacion; }
    public ClienteServicio obtenerClientes() { return clientes; }
    public CuentaServicio obtenerCuentas() { return cuentas; }
    public TransaccionServicio obtenerTransacciones() { return transacciones; }
}
