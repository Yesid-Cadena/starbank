package com.starbank.modelo.repositorios;

import com.starbank.modelo.entidades.Transaccion;
import java.util.List;
import java.util.Optional;

public interface TransaccionRepositorio {
    void guardar(Transaccion transaccion);
    /** Solo admite finalizar registros pendientes conservando todos sus demás datos. */
    void actualizar(Transaccion transaccion);
    Optional<Transaccion> buscarPorIdentificador(String identificador);
    List<Transaccion> buscarPorCuenta(String numero);
    List<Transaccion> listar();
}
