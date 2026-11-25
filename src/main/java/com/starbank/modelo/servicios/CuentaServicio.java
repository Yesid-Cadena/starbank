package com.starbank.modelo.servicios;

import com.starbank.excepciones.OperacionRechazadaException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.EstadoCuenta;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.Rol;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.modelo.repositorios.CuentaRepositorio;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public final class CuentaServicio {
    private final CuentaRepositorio cuentas;
    private final UsuarioRepositorio usuarios;
    private final UnidadTrabajo unidadTrabajo;
    private final AutorizacionServicio autorizacion;
    private final AuditoriaServicio auditoria;

    public CuentaServicio(CuentaRepositorio cuentas, UsuarioRepositorio usuarios, UnidadTrabajo unidadTrabajo) {
        this(cuentas, usuarios, unidadTrabajo, null, null);
    }

    public CuentaServicio(CuentaRepositorio cuentas, UsuarioRepositorio usuarios, UnidadTrabajo unidadTrabajo,
                          AutorizacionServicio autorizacion, AuditoriaServicio auditoria) {
        this.cuentas = Validaciones.obligatorio(cuentas, "El repositorio de cuentas");
        this.usuarios = Validaciones.obligatorio(usuarios, "El repositorio de usuarios");
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    public Cuenta crear(Sesion sesion, String identificacionCliente, TipoCuenta tipo) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        Cuenta cuenta = crear(identificacionCliente, tipo);
        registrarAuditoria(sesion.obtenerIdentificacionUsuario(), "CREACION_CUENTA", "EXITOSO", "Cuenta creada.", cuenta.obtenerNumeroCuenta());
        return cuenta;
    }

    public Cuenta consultar(Sesion sesion, String numero) {
        Usuario usuario = exigirAutorizacion().exigirUsuario(sesion.obtenerIdentificador());
        if (usuario.obtenerRol() == Rol.ADMINISTRADOR) {
            return consultar(numero);
        }
        if (usuario.obtenerRol() == Rol.CLIENTE) {
            return exigirAutorizacion().exigirCuentaPropia(sesion.obtenerIdentificador(), numero);
        }
        throw new OperacionRechazadaException("Acceso denegado.");
    }

    public List<Cuenta> consultarPorCliente(Sesion sesion) {
        Cliente cliente = exigirAutorizacion().exigirCliente(sesion.obtenerIdentificador());
        return consultarPorClienteInterno(cliente.obtenerIdentificacion());
    }

    public List<Cuenta> consultarPorCliente(Sesion sesion, String identificacionCliente) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        return consultarPorClienteInterno(identificacionCliente);
    }

    public BigDecimal consultarSaldo(Sesion sesion, String numero) {
        Cuenta cuenta = consultar(sesion, numero);
        return cuenta.consultarSaldo();
    }

    public void bloquear(Sesion sesion, String numero) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        bloquear(numero);
        registrarAuditoria(sesion.obtenerIdentificacionUsuario(), "BLOQUEO_CUENTA", "EXITOSO", "Cuenta bloqueada.", numero);
    }

    public void desbloquear(Sesion sesion, String numero) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        desbloquear(numero);
        registrarAuditoria(sesion.obtenerIdentificacionUsuario(), "DESBLOQUEO_CUENTA", "EXITOSO", "Cuenta desbloqueada.", numero);
    }

    Cuenta crear(String identificacionCliente, TipoCuenta tipo) {
        return unidadTrabajo.ejecutar(() -> {
            Cliente cliente = buscarCliente(identificacionCliente);
            if (cliente.obtenerEstado() != EstadoUsuario.ACTIVO) {
                throw new OperacionRechazadaException("El cliente debe estar activo para crear cuentas.");
            }
            String numero;
            do {
                numero = new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
            } while (cuentas.buscarPorNumero(numero).isPresent());
            Cuenta cuenta = new Cuenta(numero, tipo, cliente);
            cuentas.guardar(cuenta);
            return cuenta;
        });
    }

    Cuenta consultar(String numero) {
        return cuentas.buscarPorNumero(numero)
                .orElseThrow(() -> new RegistroNoEncontradoException("La cuenta no existe."));
    }

    Cuenta consultarPropia(String identificacionCliente, String numero) {
        Cuenta cuenta = consultar(numero);
        String identificacion = Validaciones.texto(identificacionCliente, "El cliente");
        if (!cuenta.obtenerCliente().obtenerIdentificacion().equals(identificacion)) {
            throw new OperacionRechazadaException("La cuenta no pertenece al cliente indicado.");
        }
        return cuenta;
    }

    List<Cuenta> consultarPorCliente(String identificacionCliente) { return consultarPorClienteInterno(identificacionCliente); }

    List<Cuenta> consultarPorClienteInterno(String identificacionCliente) {
        return unidadTrabajo.ejecutar(() -> {
            Cliente cliente = buscarCliente(identificacionCliente);
            return cuentas.buscarPorCliente(cliente.obtenerIdentificacion());
        });
    }

    BigDecimal consultarSaldo(String identificacionCliente, String numero) { return consultarSaldoInterno(identificacionCliente, numero); }

    BigDecimal consultarSaldoInterno(String identificacionCliente, String numero) {
        return consultarPropia(identificacionCliente, numero).consultarSaldo();
    }

    Cuenta validarHabilitada(String numero) {
        Cuenta cuenta = consultar(numero);
        cuenta.validarHabilitada();
        return cuenta;
    }

    void bloquear(String numero) { cambiarEstado(numero, EstadoCuenta.BLOQUEADA); }

    void desbloquear(String numero) {
        unidadTrabajo.ejecutar(() -> {
            Cuenta cuenta = consultar(numero);
            if (cuenta.obtenerEstado() != EstadoCuenta.BLOQUEADA) {
                throw new OperacionRechazadaException("Solo se puede desbloquear una cuenta bloqueada.");
            }
            cuenta.actualizarEstado(EstadoCuenta.ACTIVA);
            cuentas.actualizar(cuenta);
            return null;
        });
    }

    private void cambiarEstado(String numero, EstadoCuenta estado) {
        unidadTrabajo.ejecutar(() -> {
            Cuenta cuenta = consultar(numero);
            cuenta.actualizarEstado(estado);
            cuentas.actualizar(cuenta);
            return null;
        });
    }

    private Cliente buscarCliente(String identificacion) {
        return usuarios.buscarClientePorDocumento(identificacion)
                .orElseThrow(() -> new RegistroNoEncontradoException("El cliente no existe."));
    }

    private AutorizacionServicio exigirAutorizacion() {
        return Validaciones.obligatorio(autorizacion, "El servicio de autorización");
    }

    private void registrarAuditoria(String usuario, String tipo, String resultado, String descripcion, String recurso) {
        if (auditoria != null) {
            auditoria.registrar(usuario, tipo, resultado, descripcion, recurso);
        }
    }
}
