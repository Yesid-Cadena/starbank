package com.starbank.modelo.entidades;

import com.starbank.excepciones.ValidacionException;
import com.starbank.excepciones.OperacionRechazadaException;
import com.starbank.modelo.enumeraciones.EstadoCuenta;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.utilidades.Validaciones;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Cuenta {
    private final String numeroCuenta;
    private final TipoCuenta tipoCuenta;
    private final Cliente cliente;
    private final LocalDateTime fechaCreacion;
    private final BigDecimal saldo;
    private EstadoCuenta estado;

    public Cuenta(String numeroCuenta, TipoCuenta tipoCuenta, Cliente cliente) {
        this.numeroCuenta = Validaciones.texto(numeroCuenta, "El número de cuenta");
        if (!this.numeroCuenta.matches("[0-9]+")) {
            throw new ValidacionException("El número de cuenta debe contener solo dígitos.");
        }
        this.tipoCuenta = Validaciones.obligatorio(tipoCuenta, "El tipo de cuenta");
        this.cliente = Validaciones.obligatorio(cliente, "El cliente titular");
        fechaCreacion = LocalDateTime.now();
        saldo = new BigDecimal("0.00");
        estado = EstadoCuenta.ACTIVA;
    }

    public void actualizarEstado(EstadoCuenta estado) {
        this.estado = Validaciones.obligatorio(estado, "El estado de la cuenta");
    }

    private Cuenta(Cuenta original, BigDecimal saldo) {
        numeroCuenta = original.numeroCuenta;
        tipoCuenta = original.tipoCuenta;
        cliente = original.cliente.copiar();
        fechaCreacion = original.fechaCreacion;
        estado = original.estado;
        this.saldo = saldo;
    }

    public Cuenta copiar() { return new Cuenta(this, saldo); }

    public void validarHabilitada() {
        if (estado != EstadoCuenta.ACTIVA) {
            throw new OperacionRechazadaException("La cuenta no está habilitada.");
        }
    }

    /** Produce una nueva versión; el servicio persiste saldo e historial juntos. */
    public Cuenta acreditar(BigDecimal monto) {
        Validaciones.monto(monto);
        validarHabilitada();
        return new Cuenta(this, saldo.add(monto));
    }

    public Cuenta debitar(BigDecimal monto) {
        Validaciones.monto(monto);
        validarHabilitada();
        if (saldo.compareTo(monto) < 0) {
            throw new OperacionRechazadaException("Fondos insuficientes.");
        }
        return new Cuenta(this, saldo.subtract(monto));
    }

    public String obtenerNumeroCuenta() { return numeroCuenta; }
    public TipoCuenta obtenerTipoCuenta() { return tipoCuenta; }
    public Cliente obtenerCliente() { return cliente; }
    public LocalDateTime obtenerFechaCreacion() { return fechaCreacion; }
    public BigDecimal consultarSaldo() { return saldo; }
    public EstadoCuenta obtenerEstado() { return estado; }
}
