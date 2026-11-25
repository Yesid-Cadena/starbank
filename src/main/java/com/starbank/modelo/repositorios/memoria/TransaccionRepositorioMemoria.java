package com.starbank.modelo.repositorios.memoria;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.repositorios.TransaccionRepositorio;
import com.starbank.utilidades.Validaciones;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TransaccionRepositorioMemoria implements TransaccionRepositorio {
    private final AlmacenMemoria almacen;

    public TransaccionRepositorioMemoria(AlmacenMemoria almacen) {
        this.almacen = Validaciones.obligatorio(almacen, "El almacén");
    }

    @Override
    public void guardar(Transaccion transaccion) {
        Validaciones.obligatorio(transaccion, "La transacción");
        synchronized (almacen) {
            if (almacen.transacciones().containsKey(transaccion.obtenerIdentificador())) {
                throw new RegistroDuplicadoException("La transacción ya existe.");
            }
            almacen.transacciones().put(transaccion.obtenerIdentificador(), transaccion);
        }
    }

    @Override
    public void actualizar(Transaccion nueva) {
        Validaciones.obligatorio(nueva, "La transacción");
        synchronized (almacen) {
            Transaccion anterior = almacen.transacciones().get(nueva.obtenerIdentificador());
            if (anterior == null) {
                throw new RegistroNoEncontradoException("La transacción no existe.");
            }
            if (anterior.obtenerEstado() != EstadoTransaccion.PENDIENTE
                    || nueva.obtenerEstado() == EstadoTransaccion.PENDIENTE
                    || anterior.obtenerTipo() != nueva.obtenerTipo()
                    || anterior.obtenerMonto().compareTo(nueva.obtenerMonto()) != 0
                    || !anterior.obtenerFechaHora().equals(nueva.obtenerFechaHora())
                    || !Objects.equals(anterior.obtenerCuentaOrigen(), nueva.obtenerCuentaOrigen())
                    || !Objects.equals(anterior.obtenerCuentaDestino(), nueva.obtenerCuentaDestino())
                    || !anterior.obtenerDescripcion().equals(nueva.obtenerDescripcion())) {
                throw new ValidacionException("El historial solo admite finalizar pendientes sin alterar sus datos.");
            }
            almacen.transacciones().put(nueva.obtenerIdentificador(), nueva);
        }
    }

    @Override
    public Optional<Transaccion> buscarPorIdentificador(String identificador) {
        String clave = Validaciones.texto(identificador, "El identificador");
        synchronized (almacen) {
            return Optional.ofNullable(almacen.transacciones().get(clave));
        }
    }

    @Override
    public List<Transaccion> buscarPorCuenta(String numero) {
        String buscado = Validaciones.texto(numero, "El número de cuenta");
        return listar().stream().filter(transaccion -> transaccion.involucra(buscado)).toList();
    }

    @Override
    public List<Transaccion> listar() {
        synchronized (almacen) {
            return List.copyOf(almacen.transacciones().values());
        }
    }
}
