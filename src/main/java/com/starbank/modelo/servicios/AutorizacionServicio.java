package com.starbank.modelo.servicios;

import com.starbank.excepciones.AccesoDenegadoException;
import com.starbank.excepciones.SesionInvalidaException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.Rol;
import com.starbank.modelo.repositorios.CuentaRepositorio;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;

public final class AutorizacionServicio {
    private final SesionServicio sesiones;
    private final UsuarioRepositorio usuarios;
    private final CuentaRepositorio cuentas;
    private final AuditoriaServicio auditoria;

    public AutorizacionServicio(SesionServicio sesiones, UsuarioRepositorio usuarios,
                                CuentaRepositorio cuentas, AuditoriaServicio auditoria) {
        this.sesiones = Validaciones.obligatorio(sesiones, "El servicio de sesiones");
        this.usuarios = Validaciones.obligatorio(usuarios, "El repositorio de usuarios");
        this.cuentas = Validaciones.obligatorio(cuentas, "El repositorio de cuentas");
        this.auditoria = Validaciones.obligatorio(auditoria, "El servicio de auditoría");
    }

    public Usuario exigirUsuario(String idSesion) {
        String identificacion = sesiones.validar(idSesion).obtenerIdentificacionUsuario();
        Usuario usuario = usuarios.buscarPorIdentificacion(identificacion)
                .orElseThrow(() -> new SesionInvalidaException("La sesión no es válida."));
        if (usuario.obtenerEstado() != EstadoUsuario.ACTIVO) {
            sesiones.invalidar(idSesion);
            auditoria.registrar(usuario.obtenerIdentificacion(), "ACCESO_NO_AUTORIZADO", "RECHAZADO",
                    "Usuario inactivo con sesión invalidada.", usuario.obtenerIdentificacion());
            throw new AccesoDenegadoException("El usuario no está autorizado.");
        }
        return usuario;
    }

    public Cliente exigirCliente(String idSesion) {
        Usuario usuario = exigirUsuario(idSesion);
        if (usuario.obtenerRol() != Rol.CLIENTE || !(usuario instanceof Cliente cliente)) {
            denegar(usuario, "Se requiere rol CLIENTE.");
            throw new AccesoDenegadoException("Acceso denegado.");
        }
        return cliente;
    }

    public Usuario exigirAdministrador(String idSesion) {
        Usuario usuario = exigirUsuario(idSesion);
        if (usuario.obtenerRol() != Rol.ADMINISTRADOR) {
            denegar(usuario, "Se requiere rol ADMINISTRADOR.");
            throw new AccesoDenegadoException("Acceso denegado.");
        }
        return usuario;
    }

    public Cuenta exigirCuentaPropia(String idSesion, String numeroCuenta) {
        Cliente cliente = exigirCliente(idSesion);
        Cuenta cuenta = cuentas.buscarPorNumero(Validaciones.texto(numeroCuenta, "El número de cuenta"))
                .orElseThrow(() -> new AccesoDenegadoException("Acceso denegado."));
        if (!cuenta.obtenerCliente().obtenerIdentificacion().equals(cliente.obtenerIdentificacion())) {
            auditoria.registrar(cliente.obtenerIdentificacion(), "ACCESO_NO_AUTORIZADO", "RECHAZADO",
                    "Intento de acceso a cuenta no propia.", cuenta.obtenerNumeroCuenta());
            throw new AccesoDenegadoException("Acceso denegado.");
        }
        return cuenta;
    }

    public void exigirPermisoAdmin(String idSesion, String accion, String recurso) {
        exigirAdministrador(idSesion);
        // permiso concedido; el servicio llamador puede auditar la acción específica
    }

    private void denegar(Usuario usuario, String detalle) {
        auditoria.registrar(usuario.obtenerIdentificacion(), "ACCESO_NO_AUTORIZADO", "RECHAZADO",
                detalle, usuario.obtenerIdentificacion());
    }
}
