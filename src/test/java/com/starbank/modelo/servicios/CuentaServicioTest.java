package com.starbank.modelo.servicios;

import com.starbank.excepciones.OperacionRechazadaException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.enumeraciones.EstadoCuenta;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.soporte.BancoPrueba;
import java.math.BigDecimal;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

class CuentaServicioTest extends BancoPrueba {
    @Test
    void permiteVariasCuentasConNumerosUnicosSaldoCeroYTitularRegistrado() {
        var numeros = new HashSet<String>();
        numeros.add(origen);
        for (int indice = 0; indice < 100; indice++) {
            Cuenta cuenta = servicioCuentas.crear("001", TipoCuenta.CORRIENTE);
            assertTrue(numeros.add(cuenta.obtenerNumeroCuenta()));
            assertEquals("001", cuenta.obtenerCliente().obtenerIdentificacion());
            assertEquals(0, cuenta.consultarSaldo().compareTo(BigDecimal.ZERO));
        }
        assertEquals(101, servicioCuentas.consultarPorCliente("001").size());
        assertEquals(1, servicioCuentas.consultarPorCliente("002").size());
        assertTrue(transacciones.listar().isEmpty());
    }

    @Test
    void rechazaClienteInexistente() {
        assertThrows(RegistroNoEncontradoException.class,
                () -> servicioCuentas.crear("999", TipoCuenta.AHORROS));
        assertEquals(2, cuentas.listar().size());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoUsuario.class, names = {"BLOQUEADO", "INACTIVO"})
    void rechazaClienteNoActivo(EstadoUsuario estado) {
        Cliente cliente = clientes.consultar("001");
        cliente.actualizarEstado(estado);
        usuarios.actualizar(cliente);
        assertThrows(OperacionRechazadaException.class,
                () -> servicioCuentas.crear("001", TipoCuenta.AHORROS));
        assertEquals(2, cuentas.listar().size());
    }

    @Test
    void bloqueaDesbloqueaYValidaEstado() {
        servicioCuentas.bloquear(origen);
        assertEquals(EstadoCuenta.BLOQUEADA, servicioCuentas.consultar(origen).obtenerEstado());
        assertThrows(OperacionRechazadaException.class, () -> servicioCuentas.validarHabilitada(origen));
        servicioCuentas.desbloquear(origen);
        assertEquals(EstadoCuenta.ACTIVA, servicioCuentas.validarHabilitada(origen).obtenerEstado());
        assertThrows(OperacionRechazadaException.class, () -> servicioCuentas.desbloquear(origen));
    }

    @Test
    void consultaSaldoPropioYRechazaCuentaAjenaOInexistente() {
        assertEquals(0, servicioCuentas.consultarSaldo("001", origen).compareTo(BigDecimal.ZERO));
        assertThrows(OperacionRechazadaException.class, () -> servicioCuentas.consultarSaldo("002", origen));
        assertThrows(RegistroNoEncontradoException.class, () -> servicioCuentas.consultar("999"));
    }
}
