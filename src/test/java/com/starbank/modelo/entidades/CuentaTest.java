package com.starbank.modelo.entidades;

import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.enumeraciones.EstadoCuenta;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class CuentaTest {
    private static Cliente cliente;

    @BeforeAll
    static void prepararCliente() {
        cliente = new Cliente("001", "Ana", "ana@example.com", "3000000001",
                ClaveProtegida.crear("Clave-ficticia-21".toCharArray()));
    }

    @ParameterizedTest
    @EnumSource(TipoCuenta.class)
    void abreCuentaConSaldoCeroTitularYFecha(TipoCuenta tipo) {
        LocalDateTime antes = LocalDateTime.now();
        Cuenta cuenta = new Cuenta("000001", tipo, cliente);
        assertEquals("000001", cuenta.obtenerNumeroCuenta());
        assertEquals(tipo, cuenta.obtenerTipoCuenta());
        assertSame(cliente, cuenta.obtenerCliente());
        assertEquals(new BigDecimal("0.00"), cuenta.consultarSaldo());
        assertEquals(EstadoCuenta.ACTIVA, cuenta.obtenerEstado());
        assertFalse(cuenta.obtenerFechaCreacion().isBefore(antes));
        assertFalse(cuenta.obtenerFechaCreacion().isAfter(LocalDateTime.now()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "ABC", "-12", "12.5", "12 34"})
    void rechazaNumerosInvalidos(String numero) {
        assertThrows(ValidacionException.class, () -> new Cuenta(numero, TipoCuenta.AHORROS, cliente));
    }

    @Test
    void exigeTipoYTitular() {
        assertThrows(ValidacionException.class, () -> new Cuenta("001", null, cliente));
        assertThrows(ValidacionException.class, () -> new Cuenta("001", TipoCuenta.AHORROS, null));
    }

    @Test
    void variasCuentasPuedenCompartirTitular() {
        Cuenta ahorros = new Cuenta("001", TipoCuenta.AHORROS, cliente);
        Cuenta corriente = new Cuenta("002", TipoCuenta.CORRIENTE, cliente);
        assertSame(ahorros.obtenerCliente(), corriente.obtenerCliente());
        assertNotEquals(ahorros.obtenerNumeroCuenta(), corriente.obtenerNumeroCuenta());
    }

    @Test
    void cambioDeEstadoNoModificaSaldoYNuloConservaEstado() {
        Cuenta cuenta = new Cuenta("001", TipoCuenta.AHORROS, cliente);
        cuenta.actualizarEstado(EstadoCuenta.BLOQUEADA);
        assertThrows(ValidacionException.class, () -> cuenta.actualizarEstado(null));
        assertEquals(EstadoCuenta.BLOQUEADA, cuenta.obtenerEstado());
        assertEquals(new BigDecimal("0.00"), cuenta.consultarSaldo());
        cuenta.actualizarEstado(EstadoCuenta.ACTIVA);
        assertEquals(EstadoCuenta.ACTIVA, cuenta.obtenerEstado());
    }
}
