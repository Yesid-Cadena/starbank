package com.starbank.modelo.entidades;

import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TransaccionTest {
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-0.1"})
    void entidadRechazaMontosInvalidos(String valor) {
        BigDecimal monto = valor == null ? null : new BigDecimal(valor);
        assertThrows(ValidacionException.class, () -> new Transaccion("1", TipoTransaccion.DEPOSITO,
                monto, LocalDateTime.now(), null, "001", EstadoTransaccion.COMPLETADA, "Prueba"));
    }

    @Test
    void exigeCuentasAcordesConElTipo() {
        assertThrows(ValidacionException.class, () -> new Transaccion("1", TipoTransaccion.DEPOSITO,
                BigDecimal.ONE, LocalDateTime.now(), "001", null, EstadoTransaccion.COMPLETADA, "Prueba"));
        assertThrows(ValidacionException.class, () -> new Transaccion("1", TipoTransaccion.RETIRO,
                BigDecimal.ONE, LocalDateTime.now(), null, "001", EstadoTransaccion.COMPLETADA, "Prueba"));
        assertThrows(ValidacionException.class, () -> new Transferencia("1", BigDecimal.ONE,
                LocalDateTime.now(), "001", null, EstadoTransaccion.COMPLETADA, "Prueba"));
        assertThrows(ValidacionException.class, () -> new Transferencia("1", BigDecimal.ONE,
                LocalDateTime.now(), "001", "001", EstadoTransaccion.COMPLETADA, "Prueba"));
    }

    @Test
    void transferenciaConservaDatosYSubtipoAlFinalizar() {
        Transferencia pendiente = new Transferencia("1", BigDecimal.ONE, LocalDateTime.now(),
                "001", "002", EstadoTransaccion.PENDIENTE, "Prueba");
        Transaccion completada = pendiente.finalizar(EstadoTransaccion.COMPLETADA);
        assertInstanceOf(Transferencia.class, completada);
        assertEquals(pendiente.obtenerFechaHora(), completada.obtenerFechaHora());
        assertEquals("001", completada.obtenerCuentaOrigen());
        assertEquals("002", completada.obtenerCuentaDestino());
        assertEquals(EstadoTransaccion.PENDIENTE, pendiente.obtenerEstado());
        assertTrue(completada.involucra("001"));
        assertTrue(completada.involucra("002"));
        assertFalse(completada.involucra(null));
        assertFalse(completada.involucra("003"));
    }

    @Test
    void noPermiteFinalizarComoPendiente() {
        Transaccion pendiente = new Transaccion("1", TipoTransaccion.DEPOSITO, BigDecimal.ONE,
                LocalDateTime.now(), null, "001", EstadoTransaccion.PENDIENTE, "Prueba");
        assertThrows(ValidacionException.class, () -> pendiente.finalizar(EstadoTransaccion.PENDIENTE));
        assertThrows(ValidacionException.class, () -> pendiente.finalizar(null));
    }
}
