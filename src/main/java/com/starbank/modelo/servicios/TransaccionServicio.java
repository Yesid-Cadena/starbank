package com.starbank.modelo.servicios;

import com.starbank.excepciones.OperacionRechazadaException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.entidades.Transferencia;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import com.starbank.modelo.repositorios.CuentaRepositorio;
import com.starbank.modelo.repositorios.TransaccionRepositorio;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import com.starbank.utilidades.Validaciones;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class TransaccionServicio {
    private final CuentaRepositorio cuentas;
    private final TransaccionRepositorio transacciones;
    private final CuentaServicio cuentaServicio;
    private final UnidadTrabajo unidadTrabajo;

    public TransaccionServicio(CuentaRepositorio cuentas, TransaccionRepositorio transacciones,
                               CuentaServicio cuentaServicio, UnidadTrabajo unidadTrabajo) {
        this.cuentas = Validaciones.obligatorio(cuentas, "El repositorio de cuentas");
        this.transacciones = Validaciones.obligatorio(transacciones, "El repositorio de transacciones");
        this.cuentaServicio = Validaciones.obligatorio(cuentaServicio, "El servicio de cuentas");
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
    }

    // La identidad deberá proceder de la sesión autenticada en la siguiente fase.
    public Transaccion depositar(String cliente, String numero, BigDecimal monto, String descripcion) {
        return operar(cliente, null, numero, monto, descripcion, TipoTransaccion.DEPOSITO);
    }

    public Transaccion retirar(String cliente, String numero, BigDecimal monto, String descripcion) {
        return operar(cliente, numero, null, monto, descripcion, TipoTransaccion.RETIRO);
    }

    public Transaccion transferir(String cliente, String origen, String destino,
                                  BigDecimal monto, String descripcion) {
        Validaciones.texto(origen, "La cuenta de origen");
        Validaciones.texto(destino, "La cuenta de destino");
        return operar(cliente, origen, destino, monto, descripcion, TipoTransaccion.TRANSFERENCIA);
    }

    private Transaccion operar(String cliente, String origen, String destino, BigDecimal monto,
                               String descripcion, TipoTransaccion tipo) {
        Validaciones.monto(monto);
        String detalle = Validaciones.texto(descripcion, "La descripción");
        return unidadTrabajo.ejecutar(() -> {
            Cuenta cuentaPropia = cuentaServicio.consultarPropia(cliente, origen == null ? destino : origen);
            Cuenta cuentaOrigen = origen == null ? null : cuentaPropia;
            Cuenta cuentaDestino = destino == null ? null
                    : origen == null ? cuentaPropia : cuentaServicio.consultar(destino);
            Cuenta nuevoOrigen = cuentaOrigen;
            Cuenta nuevoDestino = cuentaDestino;
            EstadoTransaccion estado = EstadoTransaccion.COMPLETADA;
            String descripcionFinal = detalle;
            try {
                if (cuentaOrigen != null) { cuentaOrigen.validarHabilitada(); }
                if (cuentaDestino != null) { cuentaDestino.validarHabilitada(); }
                if (tipo == TipoTransaccion.TRANSFERENCIA
                        && cuentaOrigen.obtenerNumeroCuenta().equals(cuentaDestino.obtenerNumeroCuenta())) {
                    throw new OperacionRechazadaException("No se permite transferir a la misma cuenta.");
                }
                if (cuentaOrigen != null) { nuevoOrigen = cuentaOrigen.debitar(monto); }
                if (cuentaDestino != null) { nuevoDestino = cuentaDestino.acreditar(monto); }
            } catch (OperacionRechazadaException excepcion) {
                estado = EstadoTransaccion.RECHAZADA;
                descripcionFinal = detalle + " | Rechazo: " + excepcion.getMessage();
            }

            String numeroOrigen = cuentaOrigen == null ? null : cuentaOrigen.obtenerNumeroCuenta();
            String numeroDestino = cuentaDestino == null ? null : cuentaDestino.obtenerNumeroCuenta();
            String identificador = UUID.randomUUID().toString();
            Transaccion transaccion = tipo == TipoTransaccion.TRANSFERENCIA
                    ? new Transferencia(identificador, monto, LocalDateTime.now(), numeroOrigen,
                            numeroDestino, estado, descripcionFinal)
                    : new Transaccion(identificador, tipo, monto, LocalDateTime.now(), numeroOrigen,
                            numeroDestino, estado, descripcionFinal);
            if (estado == EstadoTransaccion.COMPLETADA) {
                if (nuevoOrigen != null) { cuentas.actualizar(nuevoOrigen); }
                if (nuevoDestino != null) { cuentas.actualizar(nuevoDestino); }
            }
            // Los fallos de persistencia salen de la unidad de trabajo y revierten todo.
            transacciones.guardar(transaccion);
            return transaccion;
        });
    }

    public Transaccion consultar(String identificador) {
        return transacciones.buscarPorIdentificador(identificador)
                .orElseThrow(() -> new RegistroNoEncontradoException("La transacción no existe."));
    }

    public List<Transaccion> historialCuenta(String cliente, String numero) {
        return historialCuenta(cliente, numero, null, null, null);
    }

    public List<Transaccion> historialCuenta(String cliente, String numero, TipoTransaccion tipo,
                                            LocalDateTime desde, LocalDateTime hasta) {
        return unidadTrabajo.ejecutar(() -> {
            Cuenta cuenta = cuentaServicio.consultarPropia(cliente, numero);
            return filtrar(transacciones.buscarPorCuenta(cuenta.obtenerNumeroCuenta()), tipo, desde, hasta);
        });
    }

    public List<Transaccion> historialCliente(String cliente) {
        return historialCliente(cliente, null, null, null);
    }

    public List<Transaccion> historialCliente(String cliente, TipoTransaccion tipo,
                                             LocalDateTime desde, LocalDateTime hasta) {
        return unidadTrabajo.ejecutar(() -> {
            Set<String> numeros = cuentaServicio.consultarPorCliente(cliente).stream()
                    .map(Cuenta::obtenerNumeroCuenta).collect(Collectors.toSet());
            List<Transaccion> historial = transacciones.listar().stream()
                    .filter(transaccion -> numeros.stream().anyMatch(transaccion::involucra)).toList();
            return filtrar(historial, tipo, desde, hasta);
        });
    }

    private List<Transaccion> filtrar(List<Transaccion> historial, TipoTransaccion tipo,
                                      LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("El inicio del rango debe ser anterior o igual al final.");
        }
        return historial.stream().filter(transaccion -> tipo == null || transaccion.obtenerTipo() == tipo)
                .filter(transaccion -> desde == null || !transaccion.obtenerFechaHora().isBefore(desde))
                .filter(transaccion -> hasta == null || !transaccion.obtenerFechaHora().isAfter(hasta))
                .sorted(Comparator.comparing(Transaccion::obtenerFechaHora)
                        .thenComparing(Transaccion::obtenerIdentificador)).toList();
    }
}
