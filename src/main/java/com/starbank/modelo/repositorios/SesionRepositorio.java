package com.starbank.modelo.repositorios;

import com.starbank.modelo.entidades.Sesion;
import java.util.List;
import java.util.Optional;

public interface SesionRepositorio {
    void guardar(Sesion sesion);
    void actualizar(Sesion sesion);
    Optional<Sesion> buscarPorIdentificador(String identificador);
    List<Sesion> buscarPorUsuario(String identificacionUsuario);
    List<Sesion> listar();
}
