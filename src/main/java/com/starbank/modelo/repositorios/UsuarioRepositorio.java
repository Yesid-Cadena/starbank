package com.starbank.modelo.repositorios;

import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    Optional<Usuario> buscarPorIdentificacion(String identificacion);
    Optional<Cliente> buscarClientePorDocumento(String documento);
    Optional<Usuario> buscarPorCorreo(String correo);
    List<Usuario> listar();
}
