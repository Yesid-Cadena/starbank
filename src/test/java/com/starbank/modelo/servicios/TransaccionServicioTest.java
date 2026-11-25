package com.starbank.modelo.servicios;

import com.starbank.excepciones.OperacionRechazadaException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.entidades.Transferencia;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import com.starbank.soporte.BancoPrueba;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TransaccionServicioTest extends BancoPrueba {
    @Test
    void ejecutaLaSecuenciaSolicitadaConSaldosExactos() {
        operaciones.depositar("001", origen, new BigDecimal("1000000"), "Inicial");
        operaciones.depositar("002", destino, new BigDecimal("500000"), "Inicial");
        operaciones.retirar("001", origen, new BigDecimal("100000"), "Retiro");
        Transaccion transferencia = operaciones.transferir("001", origen, destino, new BigDecimal("200000"), "Envío");
        assertEquals(0, saldo(origen).compareTo(new BigDecimal("700000")));
        assertEquals(0, saldo(destino).compareTo(new BigDecimal("700000")));
        assertInstanceOf(Transferencia.class, transferencia);
        assertEquals(EstadoTransaccion.COMPLETADA, transferencia.obtenerEstado());
        assertEquals(4, transacciones.listar().size());
        assertEquals(3, operaciones.historialCuenta("001", origen).size());
        assertEquals(2, operaciones.historialCuenta("002", destino).size());
        assertSame(transferencia, operaciones.consultar(transferencia.obtenerIdentificador()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void rechazaMontosInvalidosEnTodasLasOperaciones(String valor) {
        BigDecimal monto = valor == null ? null : new BigDecimal(valor);
        assertThrows(ValidacionException.class, () -> operaciones.depositar("001", origen, monto, "Prueba"));
        assertThrows(ValidacionException.class, () -> operaciones.retirar("001", origen, monto, "Prueba"));
        assertThrows(ValidacionException.class, () -> operaciones.transferir("001", origen, destino, monto, "Prueba"));
        assertEquals(0, saldo(origen).compareTo(BigDecimal.ZERO));
        assertEquals(0, saldo(destino).compareTo(BigDecimal.ZERO));
        assertTrue(transacciones.listar().isEmpty());
    }

    @Test
    void conservaPrecisionDecimalYPermiteRetirarSaldoExacto() {
        operaciones.depositar("001", origen, new BigDecimal("0.10"), "Uno");
        operaciones.depositar("001", origen, new BigDecimal("0.20"), "Dos");
        assertEquals(0, saldo(origen).compareTo(new BigDecimal("0.30")));
        assertEquals(EstadoTransaccion.COMPLETADA,
                operaciones.retirar("001", origen, new BigDecimal("0.300"), "Todo").obtenerEstado());
        assertEquals(0, saldo(origen).compareTo(BigDecimal.ZERO));
    }

    @Test
    void fondosInsuficientesRegistranRechazoSinCambiarSaldos() {
        Transaccion retiro = operaciones.retirar("001", origen, BigDecimal.ONE, "Sin fondos");
        Transaccion transferencia = operaciones.transferir("001", origen, destino, BigDecimal.ONE, "Sin fondos");
        assertEquals(EstadoTransaccion.RECHAZADA, retiro.obtenerEstado());
        assertEquals(EstadoTransaccion.RECHAZADA, transferencia.obtenerEstado());
        assertEquals(0, saldo(origen).compareTo(BigDecimal.ZERO));
        assertEquals(0, saldo(destino).compareTo(BigDecimal.ZERO));
        assertEquals(2, transacciones.listar().size());
    }

    @Test
    void cuentaBloqueadaRechazaDepositoRetiroYTransferencia() {
        operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        servicioCuentas.bloquear(origen);
        assertEquals(EstadoTransaccion.RECHAZADA,
                operaciones.depositar("001", origen, BigDecimal.ONE, "Bloqueada").obtenerEstado());
        assertEquals(EstadoTransaccion.RECHAZADA,
                operaciones.retirar("001", origen, BigDecimal.ONE, "Bloqueada").obtenerEstado());
        assertEquals(EstadoTransaccion.RECHAZADA,
                operaciones.transferir("001", origen, destino, BigDecimal.ONE, "Bloqueada").obtenerEstado());
        assertEquals(0, saldo(origen).compareTo(BigDecimal.TEN));
        assertEquals(0, saldo(destino).compareTo(BigDecimal.ZERO));
        servicioCuentas.desbloquear(origen);
        assertEquals(EstadoTransaccion.COMPLETADA,
                operaciones.retirar("001", origen, BigDecimal.ONE, "Desbloqueada").obtenerEstado());
    }

    @Test
    void destinoBloqueadoNoPermiteDebitarElOrigen() {
        operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        servicioCuentas.bloquear(destino);
        assertEquals(EstadoTransaccion.RECHAZADA,
                operaciones.transferir("001", origen, destino, BigDecimal.ONE, "Destino bloqueado").obtenerEstado());
        assertEquals(0, saldo(origen).compareTo(BigDecimal.TEN));
        assertEquals(0, saldo(destino).compareTo(BigDecimal.ZERO));
    }

    @Test
    void mismaCuentaRegistraUnSoloRechazoSinCambiarSaldo() {
        operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        Transaccion rechazo = operaciones.transferir("001", origen, origen, BigDecimal.ONE, "Misma cuenta");
        assertEquals(EstadoTransaccion.RECHAZADA, rechazo.obtenerEstado());
        assertEquals(0, saldo(origen).compareTo(BigDecimal.TEN));
        assertEquals(2, transacciones.listar().size());
    }

    @Test
    void cuentasInexistentesNoGeneranRegistrosIncompletos() {
        assertThrows(RegistroNoEncontradoException.class,
                () -> operaciones.depositar("001", "999", BigDecimal.ONE, "Prueba"));
        assertThrows(RegistroNoEncontradoException.class,
                () -> operaciones.retirar("001", "999", BigDecimal.ONE, "Prueba"));
        assertThrows(RegistroNoEncontradoException.class,
                () -> operaciones.transferir("001", origen, "999", BigDecimal.ONE, "Prueba"));
        assertThrows(RegistroNoEncontradoException.class,
                () -> operaciones.transferir("001", "999", destino, BigDecimal.ONE, "Prueba"));
        assertTrue(transacciones.listar().isEmpty());
        assertEquals(0, saldo(origen).compareTo(BigDecimal.ZERO));
    }

    @Test
    void clienteNoPuedeOperarNiConsultarHistorialDeCuentaAjena() {
        assertThrows(OperacionRechazadaException.class,
                () -> operaciones.depositar("002", origen, BigDecimal.ONE, "Ajena"));
        assertThrows(OperacionRechazadaException.class,
                () -> operaciones.retirar("002", origen, BigDecimal.ONE, "Ajena"));
        assertThrows(OperacionRechazadaException.class,
                () -> operaciones.transferir("002", origen, destino, BigDecimal.ONE, "Ajena"));
        assertThrows(OperacionRechazadaException.class, () -> operaciones.historialCuenta("002", origen));
        assertTrue(transacciones.listar().isEmpty());
    }

    @Test
    void filtraPorTipoYFechasInclusivasYOrdenaCronologicamente() {
        Transaccion primero = operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        Transaccion segundo = operaciones.retirar("001", origen, BigDecimal.ONE, "Retiro");
        Transaccion tercero = operaciones.transferir("001", origen, destino, BigDecimal.ONE, "Transferencia");
        assertEquals(List.of(primero, segundo, tercero), operaciones.historialCuenta("001", origen));
        assertEquals(List.of(segundo), operaciones.historialCuenta("001", origen, TipoTransaccion.RETIRO, null, null));
        assertEquals(List.of(segundo), operaciones.historialCliente("001", null,
                segundo.obtenerFechaHora(), segundo.obtenerFechaHora()));
        assertTrue(operaciones.historialCuenta("001", origen, null, null,
                primero.obtenerFechaHora().minusNanos(1)).isEmpty());
        assertEquals(List.of(tercero), operaciones.historialCliente("001", TipoTransaccion.TRANSFERENCIA,
                tercero.obtenerFechaHora(), null));
        assertThrows(ValidacionException.class, () -> operaciones.historialCliente("001", null,
                LocalDateTime.of(2030, 1, 1, 0, 0), LocalDateTime.of(2020, 1, 1, 0, 0)));
    }

    @Test
    void transferenciaEntreCuentasDelMismoClienteNoDuplicaSuHistorial() {
        Cuenta otra = servicioCuentas.crear("001", TipoCuenta.CORRIENTE);
        operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        Transaccion transferencia = operaciones.transferir("001", origen, otra.obtenerNumeroCuenta(), BigDecimal.ONE, "Propias");
        assertEquals(2, operaciones.historialCliente("001").size());
        assertEquals(List.of(transferencia), operaciones.historialCuenta("001", otra.obtenerNumeroCuenta()));
        assertEquals(2, transacciones.listar().size());
    }

    @Test
    void consultasSinMovimientosDevuelvenListasVacias() {
        assertTrue(operaciones.historialCuenta("001", origen).isEmpty());
        assertTrue(operaciones.historialCliente("001").isEmpty());
        assertThrows(RegistroNoEncontradoException.class, () -> operaciones.consultar("ausente"));
    }

    @Test
    void retirosConcurrentesNoSobregiranNiPierdenMovimientos() throws Exception {
        operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        List<Callable<Transaccion>> tareas = new ArrayList<>();
        for (int indice = 0; indice < 20; indice++) {
            tareas.add(() -> operaciones.retirar("001", origen, BigDecimal.ONE, "Concurrente"));
        }
        try (var ejecutor = Executors.newFixedThreadPool(4)) {
            for (var resultado : ejecutor.invokeAll(tareas)) { resultado.get(); }
        }
        assertEquals(0, saldo(origen).compareTo(BigDecimal.ZERO));
        assertEquals(21, transacciones.listar().size());
        assertEquals(10, transacciones.listar().stream()
                .filter(transaccion -> transaccion.obtenerEstado() == EstadoTransaccion.RECHAZADA).count());
    }
}
