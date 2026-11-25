package com.starbank.soporte;

import com.starbank.configuracion.ConfiguracionMemoria;
import com.starbank.modelo.entidades.Administrador;
import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.enumeraciones.TipoCuenta;
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
import java.math.BigDecimal;
import java.time.Clock;

public class BancoPrueba {
    protected static final String CLAVE_TEXTO = "Clave-ficticia-21";
    protected static final ClaveProtegida CLAVE = ClaveProtegida.crear(CLAVE_TEXTO.toCharArray());
    public final Clock reloj;
    public final ConfiguracionMemoria configuracion;
    public final AlmacenMemoria almacen;
    public final UsuarioRepositorioMemoria usuarios;
    public final CuentaRepositorioMemoria cuentas;
    public final TransaccionRepositorioMemoria transacciones;
    public final SesionRepositorioMemoria repositorioSesiones;
    public final ActividadRepositorioMemoria repositorioActividad;
    public final AuditoriaServicio auditoria;
    public final SesionServicio sesiones;
    public final AutorizacionServicio autorizacion;
    public final AutenticacionServicio autenticacion;
    public final ClienteServicio clientes;
    public final CuentaServicio servicioCuentas;
    public final TransaccionServicio operaciones;
    public final String origen;
    public final String destino;
    public final Sesion sesionAdmin;

    public BancoPrueba() {
        this(Clock.systemDefaultZone());
    }

    public BancoPrueba(Clock reloj) {
        this.reloj = reloj;
        this.configuracion = new ConfiguracionMemoria(reloj);
        this.almacen = configuracion.obtenerAlmacen();
        this.usuarios = configuracion.obtenerUsuarios();
        this.cuentas = configuracion.obtenerRepositorioCuentas();
        this.transacciones = configuracion.obtenerRepositorioTransacciones();
        this.repositorioSesiones = configuracion.obtenerRepositorioSesiones();
        this.repositorioActividad = configuracion.obtenerRepositorioActividad();
        this.auditoria = configuracion.obtenerAuditoria();
        this.sesiones = configuracion.obtenerSesiones();
        this.autorizacion = configuracion.obtenerAutorizacion();
        this.autenticacion = configuracion.obtenerAutenticacion();
        this.clientes = configuracion.obtenerClientes();
        this.servicioCuentas = configuracion.obtenerCuentas();
        this.operaciones = configuracion.obtenerTransacciones();

        usuarios.guardar(new Cliente("001", "Ana", "ana@example.com", "3000000001", CLAVE));
        usuarios.guardar(new Cliente("002", "Luis", "luis@example.com", "3000000002", CLAVE));
        configuracion.crearAdministrador("admin", "Admin", "admin@example.com", "3000000000", CLAVE_TEXTO.toCharArray());
        sesionAdmin = autenticacion.iniciarSesion("admin@example.com", CLAVE_TEXTO.toCharArray());
        origen = servicioCuentas.crear(sesionAdmin, "001", TipoCuenta.AHORROS).obtenerNumeroCuenta();
        destino = servicioCuentas.crear(sesionAdmin, "002", TipoCuenta.CORRIENTE).obtenerNumeroCuenta();
    }

    protected Sesion iniciarSesion(String correo) {
        return autenticacion.iniciarSesion(correo, CLAVE_TEXTO.toCharArray());
    }

    protected Sesion iniciarSesion(String correo, String clave) {
        return autenticacion.iniciarSesion(correo, clave.toCharArray());
    }

    protected String iniciarSesionId(String correo) {
        return iniciarSesion(correo).obtenerIdentificador();
    }

    protected BigDecimal saldo(String numero) {
        return cuentas.buscarPorNumero(numero).orElseThrow().consultarSaldo();
    }

    protected Cuenta obtenerCuenta(String numero) {
        return cuentas.buscarPorNumero(numero).orElseThrow();
    }
}
