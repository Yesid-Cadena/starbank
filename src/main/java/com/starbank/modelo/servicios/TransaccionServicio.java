package com.starbank.modelo.servicios;

import com.starbank.excepciones.AccesoDenegadoException;
import com.starbank.excepciones.OperacionRechazadaException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.entidades.Transferencia;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.Rol;
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
    private final AutorizacionServicio autorizacion;

    public TransaccionServicio(CuentaRepositorio cuentas, TransaccionRepositorio transacciones,
                               CuentaServicio cuentaServicio, UnidadTrabajo unidadTrabajo) {
        this(cuentas, transacciones, cuentaServicio, unidadTrabajo, null);
    }

    public TransaccionServicio(CuentaRepositorio cuentas, TransaccionRepositorio transacciones,
                               CuentaServicio cuentaServicio, UnidadTrabajo unidadTrabajo,
                               AutorizacionServicio autorizacion) {
        this.cuentas = Validaciones.obligatorio(cuentas, "El repositorio de cuentas");
        this.transacciones = Validaciones.obligatorio(transacciones, "El repositorio de transacciones");
        this.cuentaServicio = Validaciones.obligatorio(cuentaServicio, "El servicio de cuentas");
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
        this.autorizacion = autorizacion;
    }

    public Transaccion depositar(Sesion sesion, String numero, BigDecimal monto, String descripcion) {
        Cliente cliente = exigirAutorizacion().exigirCliente(sesion.obtenerIdentificador());
        exigirAutorizacion().exigirCuentaPropia(sesion.obtenerIdentificador(), numero);
        return depositarInterno(cliente.obtenerIdentificacion(), numero, monto, descripcion);
    }

    public Transaccion retirar(Sesion sesion, String numero, BigDecimal monto, String descripcion) {
        Cliente cliente = exigirAutorizacion().exigirCliente(sesion.obtenerIdentificador());
        exigirAutorizacion().exigirCuentaPropia(sesion.obtenerIdentificador(), numero);
        return retirarInterno(cliente.obtenerIdentificacion(), numero, monto, descripcion);
    }

    public Transaccion transferir(Sesion sesion, String origen, String destino, BigDecimal monto, String descripcion) {
        Cliente cliente = exigirAutorizacion().exigirCliente(sesion.obtenerIdentificador());
        exigirAutorizacion().exigirCuentaPropia(sesion.obtenerIdentificador(), origen);
        return transferirInterno(cliente.obtenerIdentificacion(), origen, destino, monto, descripcion);
    }

    public Transaccion consultar(Sesion sesion, String identificador) {
        exigirAutorizacion().exigirAdministrador(sesion.obtenerIdentificador());
        return consultarInterno(identificador);
    }

    public List<Transaccion> historialCuenta(Sesion sesion, String numero) {
        return historialCuenta(sesion, numero, null, null, null);
    }

    public List<Transaccion> historialCuenta(Sesion sesion, String numero, TipoTransaccion tipo,
                                            LocalDateTime desde, LocalDateTime hasta) {
        Usuario usuario = exigirAutorizacion().exigirUsuario(sesion.obtenerIdentificador());
        if (usuario.obtenerRol() == Rol.ADMINISTRADOR) {
            return unidadTrabajo.ejecutar(() -> filtrar(transacciones.buscarPorCuenta(
                    Validaciones.texto(numero, "El número de cuenta")), tipo, desde, hasta));
        }
        if (usuario instanceof Cliente cliente) {
            return historialCuentaInterno(cliente.obtenerIdentificacion(), numero, tipo, desde, hasta);
        }
        throw new AccesoDenegadoException("Acceso denegado.");
    }

    public List<Transaccion> historialCliente(Sesion sesion) {
        return historialCliente(sesion, null, null, null);
    }

    public List<Transaccion> historialCliente(Sesion sesion, TipoTransaccion tipo,
                                             LocalDateTime desde, LocalDateTime hasta) {
        Cliente cliente = exigirAutorizacion().exigirCliente(sesion.obtenerIdentificador());
        return historialClienteInterno(cliente.obtenerIdentificacion(), tipo, desde, hasta);
    }

    // Métodos internos para compatibilidad de pruebas y composición en el mismo paquete.
    Transaccion depositar(String cliente, String numero, BigDecimal monto, String descripcion) { return depositarInterno(cliente, numero, monto, descripcion); }
    Transaccion retirar(String cliente, String numero, BigDecimal monto, String descripcion) { return retirarInterno(cliente, numero, monto, descripcion); }
    Transaccion transferir(String cliente, String origen, String destino, BigDecimal monto, String descripcion) { return transferirInterno(cliente, origen, destino, monto, descripcion); }

    Transaccion depositarInterno(String cliente, String numero, BigDecimal monto, String descripcion) {
        return operar(cliente, null, numero, monto, descripcion, TipoTransaccion.DEPOSITO);
    }

    Transaccion retirarInterno(String cliente, String numero, BigDecimal monto, String descripcion) {
        return operar(cliente, numero, null, monto, descripcion, TipoTransaccion.RETIRO);
    }

    Transaccion transferirInterno(String cliente, String origen, String destino,
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
            transacciones.guardar(transaccion);
            return transaccion;
        });
    }

    Transaccion consultar(String identificador) { return consultarInterno(identificador); }

    Transaccion consultarInterno(String identificador) {
        return transacciones.buscarPorIdentificador(identificador)
                .orElseThrow(() -> new RegistroNoEncontradoException("La transacción no existe."));
    }

    List<Transaccion> historialCuenta(String cliente, String numero) { return historialCuentaInterno(cliente, numero); }
    List<Transaccion> historialCuenta(String cliente, String numero, TipoTransaccion tipo, LocalDateTime desde, LocalDateTime hasta) { return historialCuentaInterno(cliente, numero, tipo, desde, hasta); }

    List<Transaccion> historialCuentaInterno(String cliente, String numero) {
        return historialCuentaInterno(cliente, numero, null, null, null);
    }

    List<Transaccion> historialCuentaInterno(String cliente, String numero, TipoTransaccion tipo,
                                            LocalDateTime desde, LocalDateTime hasta) {
        return unidadTrabajo.ejecutar(() -> {
            Cuenta cuenta = cuentaServicio.consultarPropia(cliente, numero);
            return filtrar(transacciones.buscarPorCuenta(cuenta.obtenerNumeroCuenta()), tipo, desde, hasta);
        });
    }

    List<Transaccion> historialCliente(String cliente) { return historialClienteInterno(cliente); }
    List<Transaccion> historialCliente(String cliente, TipoTransaccion tipo, LocalDateTime desde, LocalDateTime hasta) { return historialClienteInterno(cliente, tipo, desde, hasta); }

    List<Transaccion> historialClienteInterno(String cliente) {
        return historialClienteInterno(cliente, null, null, null);
    }

    List<Transaccion> historialClienteInterno(String cliente, TipoTransaccion tipo,
                                             LocalDateTime desde, LocalDateTime hasta) {
        return unidadTrabajo.ejecutar(() -> {
            Set<String> numeros = cuentaServicio.consultarPorClienteInterno(cliente).stream()
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

    private AutorizacionServicio exigirAutorizacion() {
        return Validaciones.obligatorio(autorizacion, "El servicio de autorización");
    }
}
