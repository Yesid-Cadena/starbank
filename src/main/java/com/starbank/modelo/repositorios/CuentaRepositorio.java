package com.starbank.modelo.repositorios;

import com.starbank.modelo.entidades.Cuenta;
import java.util.List;
import java.util.Optional;

public interface CuentaRepositorio {
    void guardar(Cuenta cuenta);
    void actualizar(Cuenta cuenta);
    Optional<Cuenta> buscarPorNumero(String numero);
    List<Cuenta> buscarPorCliente(String identificacion);
    List<Cuenta> listar();
}
