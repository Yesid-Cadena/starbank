package com.starbank.modelo.servicios;

import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import com.starbank.modelo.repositorios.CuentaRepositorio;
import com.starbank.modelo.repositorios.TransaccionRepositorio;
import com.starbank.soporte.BancoPrueba;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class AtomicidadTest extends BancoPrueba {
    @ParameterizedTest
    @ValueSource(strings = {"ANTES_ORIGEN", "DESPUES_ORIGEN", "DESPUES_DESTINO", "ANTES_HISTORIAL", "DESPUES_HISTORIAL"})
    void falloDePersistenciaRevierteAmbosSaldosYElHistorial(String puntoFallo) {
        Transaccion inicial = operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        TransaccionServicio servicio = conFallo(puntoFallo);
        assertThrows(IllegalStateException.class,
                () -> servicio.transferir("001", origen, destino, BigDecimal.ONE, "Debe revertirse"));
        assertEquals(0, saldo(origen).compareTo(BigDecimal.TEN));
        assertEquals(0, saldo(destino).compareTo(BigDecimal.ZERO));
        assertEquals(List.of(inicial), transacciones.listar());
        assertEquals(List.of(inicial), operaciones.historialCuenta("001", origen));
        assertTrue(operaciones.historialCuenta("002", destino).isEmpty());
        operaciones.transferir("001", origen, destino, BigDecimal.ONE, "Recuperada");
        assertEquals(0, saldo(origen).compareTo(new BigDecimal("9")));
        assertEquals(0, saldo(destino).compareTo(BigDecimal.ONE));
        assertEquals(2, transacciones.listar().size());
    }

    @ParameterizedTest
    @EnumSource(value = TipoTransaccion.class, names = {"DEPOSITO", "RETIRO"})
    void depositoYRetiroTambienReviertenSiFallaElRegistro(TipoTransaccion tipo) {
        Transaccion inicial = operaciones.depositar("001", origen, BigDecimal.TEN, "Inicial");
        TransaccionServicio servicio = conFallo("DESPUES_HISTORIAL");
        assertThrows(IllegalStateException.class, () -> {
            if (tipo == TipoTransaccion.DEPOSITO) {
                servicio.depositar("001", origen, BigDecimal.ONE, "Fallo");
            } else {
                servicio.retirar("001", origen, BigDecimal.ONE, "Fallo");
            }
        });
        assertEquals(0, saldo(origen).compareTo(BigDecimal.TEN));
        assertEquals(List.of(inicial), transacciones.listar());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANTES_HISTORIAL", "DESPUES_HISTORIAL"})
    void falloAlRegistrarRechazoNoDejaUnRegistroParcial(String puntoFallo) {
        assertThrows(IllegalStateException.class,
                () -> conFallo(puntoFallo).retirar("001", origen, BigDecimal.ONE, "Sin fondos"));
        assertTrue(transacciones.listar().isEmpty());
        assertEquals(0, saldo(origen).compareTo(BigDecimal.ZERO));
    }

    private TransaccionServicio conFallo(String puntoFallo) {
        CuentaRepositorio cuentasConFallo = new CuentaRepositorio() {
            @Override
            public void guardar(Cuenta cuenta) { cuentas.guardar(cuenta); }
            @Override
            public void actualizar(Cuenta cuenta) {
                if (puntoFallo.equals("ANTES_ORIGEN") && cuenta.obtenerNumeroCuenta().equals(origen)) {
                    throw new IllegalStateException("Fallo antes del débito");
                }
                cuentas.actualizar(cuenta);
                if ((puntoFallo.equals("DESPUES_ORIGEN") && cuenta.obtenerNumeroCuenta().equals(origen))
                        || (puntoFallo.equals("DESPUES_DESTINO") && cuenta.obtenerNumeroCuenta().equals(destino))) {
                    throw new IllegalStateException("Fallo después de escribir saldo");
                }
            }
            @Override
            public Optional<Cuenta> buscarPorNumero(String numero) { return cuentas.buscarPorNumero(numero); }
            @Override
            public List<Cuenta> buscarPorCliente(String cliente) { return cuentas.buscarPorCliente(cliente); }
            @Override
            public List<Cuenta> listar() { return cuentas.listar(); }
        };
        TransaccionRepositorio historialConFallo = new TransaccionRepositorio() {
            @Override
            public void guardar(Transaccion transaccion) {
                if (puntoFallo.equals("ANTES_HISTORIAL")) { throw new IllegalStateException("Fallo antes del registro"); }
                transacciones.guardar(transaccion);
                if (puntoFallo.equals("DESPUES_HISTORIAL")) { throw new IllegalStateException("Fallo después del registro"); }
            }
            @Override
            public void actualizar(Transaccion transaccion) { transacciones.actualizar(transaccion); }
            @Override
            public Optional<Transaccion> buscarPorIdentificador(String identificador) {
                return transacciones.buscarPorIdentificador(identificador);
            }
            @Override
            public List<Transaccion> buscarPorCuenta(String numero) { return transacciones.buscarPorCuenta(numero); }
            @Override
            public List<Transaccion> listar() { return transacciones.listar(); }
        };
        CuentaServicio servicioCuentasConFallo = new CuentaServicio(cuentasConFallo, usuarios, almacen);
        return new TransaccionServicio(cuentasConFallo, historialConFallo, servicioCuentasConFallo, almacen);
    }
}
