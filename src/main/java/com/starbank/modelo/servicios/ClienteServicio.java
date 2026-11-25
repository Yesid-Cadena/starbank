package com.starbank.modelo.servicios;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;
import java.util.List;

public final class ClienteServicio {
    private final UsuarioRepositorio usuarios;
    private final UnidadTrabajo unidadTrabajo;

    public ClienteServicio(UsuarioRepositorio usuarios, UnidadTrabajo unidadTrabajo) {
        this.usuarios = Validaciones.obligatorio(usuarios, "El repositorio de usuarios");
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
    }

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

    public Cliente consultar(String identificacion) {
        return usuarios.buscarClientePorDocumento(identificacion)
                .orElseThrow(() -> new RegistroNoEncontradoException("El cliente no existe."));
    }

    public Cliente actualizarInformacion(String identificacion, String nombre, String correo, String telefono) {
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

    public EstadoUsuario consultarEstado(String identificacion) {
        return consultar(identificacion).obtenerEstado();
    }

    public List<Cliente> listarClientes() {
        return usuarios.listar().stream().filter(Cliente.class::isInstance).map(Cliente.class::cast).toList();
    }
}
