package com.starbank.modelo.servicios;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;
import java.util.List;

public final class ClienteServicio {
    private final UsuarioRepositorio usuarios;
    private final UnidadTrabajo unidadTrabajo;
    private final AutorizacionServicio autorizacion;
    private final AuditoriaServicio auditoria;
    private final SesionServicio sesiones;

    public ClienteServicio(UsuarioRepositorio usuarios, UnidadTrabajo unidadTrabajo) {
        this(usuarios, unidadTrabajo, null, null, null);
    }

    public ClienteServicio(UsuarioRepositorio usuarios, UnidadTrabajo unidadTrabajo,
                           AutorizacionServicio autorizacion, AuditoriaServicio auditoria,
                           SesionServicio sesiones) {
        this.usuarios = Validaciones.obligatorio(usuarios, "El repositorio de usuarios");
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
        this.sesiones = sesiones;
    }

    /** Registro público: solo crea clientes, nunca administradores. */
    public Cliente registrar(String documento, String nombre, String correo, String telefono, char[] clave) {
        String identificacion = Validaciones.texto(documento, "El documento");
        String correoValidado = Validaciones.correo(correo);
        return unidadTrabajo.ejecutar(() -> {
            if (usuarios.buscarPorIdentificacion(identificacion).isPresent()) {
                throw new RegistroDuplicadoException("El documento ya está registrado.");
            }
            validarCorreoDisponible(correoValidado, identificacion);
            Cliente cliente = new Cliente(identificacion, nombre, correoValidado, telefono,
                    ClaveProtegida.crear(clave));
            usuarios.guardar(cliente);
            return cliente;
        });
    }

    public Cliente consultarPerfil(Sesion sesion) {
        Cliente cliente = exigirAutorizacion().exigirCliente(sesion.obtenerIdentificador());
        return consultar(cliente.obtenerIdentificacion());
    }

    public Cliente actualizarPerfil(Sesion sesion, String nombre, String correo, String telefono) {
        Cliente cliente = exigirAutorizacion().exigirCliente(sesion.obtenerIdentificador());
        return actualizarInformacion(cliente.obtenerIdentificacion(), nombre, correo, telefono);
    }

    public Cliente consultar(Sesion sesion, String identificacion) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        return consultar(identificacion);
    }

    public Cliente actualizarInformacion(Sesion sesion, String identificacion, String nombre, String correo, String telefono) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        return actualizarInformacion(identificacion, nombre, correo, telefono);
    }

    public void cambiarEstado(Sesion sesion, String identificacion, EstadoUsuario estado) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        EstadoUsuario nuevoEstado = Validaciones.obligatorio(estado, "El estado");
        unidadTrabajo.ejecutar(() -> {
            Cliente cliente = consultar(identificacion);
            cliente.actualizarEstado(nuevoEstado);
            usuarios.actualizar(cliente);
            return null;
        });
        if (nuevoEstado != EstadoUsuario.ACTIVO && sesiones != null) {
            sesiones.invalidarPorUsuario(identificacion);
        }
        if (auditoria != null) {
            auditoria.registrar(sesion.obtenerIdentificacionUsuario(), "GESTION_CLIENTE", "EXITOSO",
                    "Estado de cliente actualizado a " + nuevoEstado + ".", identificacion);
        }
    }

    public EstadoUsuario consultarEstado(Sesion sesion, String identificacion) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        return consultarEstado(identificacion);
    }

    public List<Cliente> listarClientes(Sesion sesion) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        return listarClientes();
    }

    Cliente consultar(String identificacion) {
        return usuarios.buscarClientePorDocumento(identificacion)
                .orElseThrow(() -> new RegistroNoEncontradoException("El cliente no existe."));
    }

    Cliente actualizarInformacion(String identificacion, String nombre, String correo, String telefono) {
        return unidadTrabajo.ejecutar(() -> {
            Cliente cliente = consultar(identificacion);
            String correoValidado = Validaciones.correo(correo);
            validarCorreoDisponible(correoValidado, cliente.obtenerIdentificacion());
            cliente.actualizarInformacion(nombre, correoValidado, telefono);
            usuarios.actualizar(cliente);
            return cliente;
        });
    }

    private void validarCorreoDisponible(String correo, String identificacion) {
        usuarios.buscarPorCorreo(correo).ifPresent(usuario -> {
            if (!usuario.obtenerIdentificacion().equals(identificacion)) {
                throw new RegistroDuplicadoException("El correo ya está registrado.");
            }
        });
    }

    EstadoUsuario consultarEstado(String identificacion) {
        return consultar(identificacion).obtenerEstado();
    }

    List<Cliente> listarClientes() {
        return usuarios.listar().stream().filter(Cliente.class::isInstance).map(Cliente.class::cast).toList();
    }

    private AutorizacionServicio exigirAutorizacion() {
        return Validaciones.obligatorio(autorizacion, "El servicio de autorización");
    }
}
