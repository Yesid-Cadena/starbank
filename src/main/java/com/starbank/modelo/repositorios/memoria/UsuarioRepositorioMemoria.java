package com.starbank.modelo.repositorios.memoria;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;
import java.util.List;
import java.util.Optional;

public final class UsuarioRepositorioMemoria implements UsuarioRepositorio {
    private final AlmacenMemoria almacen;

    public UsuarioRepositorioMemoria(AlmacenMemoria almacen) {
        this.almacen = Validaciones.obligatorio(almacen, "El almacén");
    }

    @Override
    public void guardar(Usuario usuario) {
        Validaciones.obligatorio(usuario, "El usuario");
        synchronized (almacen) {
            if (almacen.usuarios().containsKey(usuario.obtenerIdentificacion())) {
                throw new RegistroDuplicadoException("La identificación ya está registrada.");
            }
            validarCorreoUnico(usuario);
            almacen.usuarios().put(usuario.obtenerIdentificacion(), usuario.copiar());
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        Validaciones.obligatorio(usuario, "El usuario");
        synchronized (almacen) {
            Usuario anterior = almacen.usuarios().get(usuario.obtenerIdentificacion());
            if (anterior == null) {
                throw new RegistroNoEncontradoException("El usuario no existe.");
            }
            if (anterior.obtenerRol() != usuario.obtenerRol()
                    || !anterior.obtenerFechaRegistro().equals(usuario.obtenerFechaRegistro())) {
                throw new ValidacionException("No se puede reemplazar la identidad de un usuario.");
            }
            validarCorreoUnico(usuario);
            almacen.usuarios().put(usuario.obtenerIdentificacion(), usuario.copiar());
        }
    }

    private void validarCorreoUnico(Usuario usuario) {
        boolean duplicado = almacen.usuarios().values().stream().anyMatch(otro ->
                !otro.obtenerIdentificacion().equals(usuario.obtenerIdentificacion())
                        && otro.obtenerCorreo().equalsIgnoreCase(usuario.obtenerCorreo()));
        if (duplicado) {
            throw new RegistroDuplicadoException("El correo ya está registrado.");
        }
    }

    @Override
    public Optional<Usuario> buscarPorIdentificacion(String identificacion) {
        String clave = Validaciones.texto(identificacion, "La identificación");
        synchronized (almacen) {
            return Optional.ofNullable(almacen.usuarios().get(clave)).map(Usuario::copiar);
        }
    }

    @Override
    public Optional<Cliente> buscarClientePorDocumento(String documento) {
        return buscarPorIdentificacion(documento).filter(Cliente.class::isInstance).map(Cliente.class::cast);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        String buscado = Validaciones.correo(correo);
        synchronized (almacen) {
            return almacen.usuarios().values().stream()
                    .filter(usuario -> usuario.obtenerCorreo().equalsIgnoreCase(buscado))
                    .findFirst().map(Usuario::copiar);
        }
    }

    @Override
    public List<Usuario> listar() {
        synchronized (almacen) {
            return almacen.usuarios().values().stream().map(Usuario::copiar).toList();
        }
    }
}
