package com.starbank.modelo.entidades;

import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import com.starbank.utilidades.Validaciones;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro inmutable. Las cuentas se referencian por número, no por objetos mutables. */
public sealed class Transaccion permits Transferencia {
    private final String identificador;
    private final TipoTransaccion tipo;
    private final BigDecimal monto;
    private final LocalDateTime fechaHora;
    private final String cuentaOrigen;
    private final String cuentaDestino;
    private final EstadoTransaccion estado;
    private final String descripcion;

    public Transaccion(String identificador, TipoTransaccion tipo, BigDecimal monto,
                       LocalDateTime fechaHora, String cuentaOrigen, String cuentaDestino,
                       EstadoTransaccion estado, String descripcion) {
        this.identificador = Validaciones.texto(identificador, "El identificador");
        this.tipo = Validaciones.obligatorio(tipo, "El tipo");
        this.monto = Validaciones.monto(monto);
        this.fechaHora = Validaciones.obligatorio(fechaHora, "La fecha y hora");
        this.estado = Validaciones.obligatorio(estado, "El estado");
        this.descripcion = Validaciones.texto(descripcion, "La descripción");
        this.cuentaOrigen = cuentaOrigen == null ? null : numero(cuentaOrigen);
        this.cuentaDestino = cuentaDestino == null ? null : numero(cuentaDestino);
        if ((tipo == TipoTransaccion.DEPOSITO && (cuentaOrigen != null || cuentaDestino == null))
                || (tipo == TipoTransaccion.RETIRO && (cuentaOrigen == null || cuentaDestino != null))
                || (tipo == TipoTransaccion.TRANSFERENCIA && (cuentaOrigen == null || cuentaDestino == null))) {
            throw new ValidacionException("Las cuentas no corresponden al tipo de transacción.");
        }
        if (tipo == TipoTransaccion.TRANSFERENCIA && !(this instanceof Transferencia)) {
            throw new ValidacionException("Utilice Transferencia para registrar ambas cuentas.");
        }
        if (tipo == TipoTransaccion.TRANSFERENCIA && this.cuentaOrigen.equals(this.cuentaDestino)
                && estado != EstadoTransaccion.RECHAZADA) {
            throw new ValidacionException("No se permite transferir a la misma cuenta.");
        }
    }

    private static String numero(String valor) {
        String numero = Validaciones.texto(valor, "La cuenta");
        if (!numero.matches("[0-9]+")) {
            throw new ValidacionException("El número de cuenta debe contener solo dígitos.");
        }
        return numero;
    }

    public Transaccion finalizar(EstadoTransaccion resultado) {
        Validaciones.obligatorio(resultado, "El resultado");
        if (estado != EstadoTransaccion.PENDIENTE || resultado == EstadoTransaccion.PENDIENTE) {
            throw new ValidacionException("Solo una transacción pendiente puede finalizarse.");
        }
        if (this instanceof Transferencia) {
            return new Transferencia(identificador, monto, fechaHora, cuentaOrigen, cuentaDestino,
                    resultado, descripcion);
        }
        return new Transaccion(identificador, tipo, monto, fechaHora, cuentaOrigen, cuentaDestino,
                resultado, descripcion);
    }

    public boolean involucra(String numeroCuenta) {
        return numeroCuenta != null && (numeroCuenta.equals(cuentaOrigen) || numeroCuenta.equals(cuentaDestino));
    }

    public String obtenerIdentificador() { return identificador; }
    public TipoTransaccion obtenerTipo() { return tipo; }
    public BigDecimal obtenerMonto() { return monto; }
    public LocalDateTime obtenerFechaHora() { return fechaHora; }
    public String obtenerCuentaOrigen() { return cuentaOrigen; }
    public String obtenerCuentaDestino() { return cuentaDestino; }
    public EstadoTransaccion obtenerEstado() { return estado; }
    public String obtenerDescripcion() { return descripcion; }
}
