package com.starbank.modelo.repositorios;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.EstadoCuenta;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.soporte.BancoPrueba;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepositoriosMemoriaTest extends BancoPrueba {
    @Test
    void rechazaIdentificadoresDuplicadosEnLosTresRepositorios() {
        assertThrows(RegistroDuplicadoException.class, () -> usuarios.guardar(usuarios.buscarClientePorDocumento("001").orElseThrow()));
        assertThrows(RegistroDuplicadoException.class, () -> cuentas.guardar(obtenerCuenta(origen)));
        Sesion sesionAna = iniciarSesion("ana@example.com");
        Transaccion deposito = operaciones.depositar(sesionAna, origen, BigDecimal.ONE, "Inicial");
        assertThrows(RegistroDuplicadoException.class, () -> transacciones.guardar(deposito));
        assertEquals(3, usuarios.listar().size());
        assertEquals(2, cuentas.listar().size());
        assertEquals(1, transacciones.listar().size());
    }

    @Test
    void repositorioUsuariosImpideCorreoDuplicadoAlGuardarYActualizar() {
        assertThrows(RegistroDuplicadoException.class,
                () -> usuarios.guardar(new Cliente("003", "Eva", "ANA@example.com", "3000000003", CLAVE)));
        Cliente cliente = usuarios.buscarClientePorDocumento("002").orElseThrow();
        cliente.actualizarInformacion("Luis", "ana@example.com", "3000000002");
        assertThrows(RegistroDuplicadoException.class, () -> usuarios.actualizar(cliente));
        assertEquals("luis@example.com", usuarios.buscarClientePorDocumento("002").orElseThrow().obtenerCorreo());
    }

    @Test
    void listasNoPermitenModificarLasColeccionesInternas() {
        operaciones.depositar(iniciarSesion("ana@example.com"), origen, BigDecimal.ONE, "Inicial");
        assertThrows(UnsupportedOperationException.class, () -> usuarios.listar().clear());
        assertThrows(UnsupportedOperationException.class, () -> cuentas.listar().clear());
        assertThrows(UnsupportedOperationException.class, () -> cuentas.buscarPorCliente("001").clear());
        assertThrows(UnsupportedOperationException.class, () -> transacciones.listar().clear());
        assertThrows(UnsupportedOperationException.class, () -> transacciones.buscarPorCuenta(origen).clear());
        assertThrows(UnsupportedOperationException.class, () -> operaciones.historialCliente(iniciarSesion("ana@example.com")).clear());
        assertThrows(UnsupportedOperationException.class, () -> clientes.listarClientes(sesionAdmin).clear());
    }

    @Test
    void objetosDevueltosNoPermitenCambiarElAlmacenIndirectamente() {
        Cliente cliente = usuarios.buscarClientePorDocumento("001").orElseThrow();
        cliente.actualizarInformacion("Alterado", "alterado@example.com", "3000000003");
        usuarios.listar().getFirst().actualizarEstado(EstadoUsuario.BLOQUEADO);
        Cuenta cuenta = obtenerCuenta(origen);
        cuenta.actualizarEstado(EstadoCuenta.BLOQUEADA);
        cuenta.obtenerCliente().actualizarEstado(EstadoUsuario.INACTIVO);
        assertEquals("Ana", usuarios.buscarClientePorDocumento("001").orElseThrow().obtenerNombre());
        assertEquals(EstadoUsuario.ACTIVO, usuarios.buscarClientePorDocumento("001").orElseThrow().obtenerEstado());
        assertEquals(EstadoUsuario.ACTIVO, usuarios.buscarClientePorDocumento("002").orElseThrow().obtenerEstado());
        assertEquals(EstadoCuenta.ACTIVA, obtenerCuenta(origen).obtenerEstado());
        assertEquals(EstadoUsuario.ACTIVO, obtenerCuenta(origen).obtenerCliente().obtenerEstado());
    }

    @Test
    void guardarYActualizarConservanCopiasIndependientes() {
        Cliente cliente = new Cliente("003", "Eva", "eva@example.com", "3000000003", CLAVE);
        usuarios.guardar(cliente);
        cliente.actualizarEstado(EstadoUsuario.INACTIVO);
        assertEquals(EstadoUsuario.ACTIVO, usuarios.buscarClientePorDocumento("003").orElseThrow().obtenerEstado());
        usuarios.actualizar(cliente);
        cliente.actualizarEstado(EstadoUsuario.ACTIVO);
        assertEquals(EstadoUsuario.INACTIVO, usuarios.buscarClientePorDocumento("003").orElseThrow().obtenerEstado());
        Cuenta cuenta = obtenerCuenta(origen);
        cuenta.actualizarEstado(EstadoCuenta.BLOQUEADA);
        cuentas.actualizar(cuenta);
        cuenta.actualizarEstado(EstadoCuenta.ACTIVA);
        assertEquals(EstadoCuenta.BLOQUEADA, obtenerCuenta(origen).obtenerEstado());
    }

    @Test
    void actualizarRegistroInexistenteFallaSinInsertarlo() {
        Cliente cliente = new Cliente("003", "Eva", "eva@example.com", "3000000003", CLAVE);
        assertThrows(RegistroNoEncontradoException.class, () -> usuarios.actualizar(cliente));
        assertThrows(RegistroNoEncontradoException.class,
                () -> cuentas.actualizar(new Cuenta("999", TipoCuenta.AHORROS, cliente)));
        assertThrows(RegistroNoEncontradoException.class,
                () -> transacciones.actualizar(pendiente("nueva").finalizar(EstadoTransaccion.COMPLETADA)));
    }

    @Test
    void finalizaPendientesPeroNoPermiteReescribirHistorialFinalizado() {
        Transaccion pendiente = pendiente("pendiente");
        transacciones.guardar(pendiente);
        Transaccion completada = pendiente.finalizar(EstadoTransaccion.COMPLETADA);
        transacciones.actualizar(completada);
        assertEquals(EstadoTransaccion.COMPLETADA,
                transacciones.buscarPorIdentificador("pendiente").orElseThrow().obtenerEstado());
        assertThrows(ValidacionException.class, () -> transacciones.actualizar(completada));
        assertThrows(ValidacionException.class, () -> completada.finalizar(EstadoTransaccion.RECHAZADA));
        assertEquals(EstadoTransaccion.PENDIENTE, pendiente.obtenerEstado());
    }

    @Test
    void noPermiteCambiarMontoAlFinalizarPendiente() {
        Transaccion pendiente = pendiente("pendiente");
        transacciones.guardar(pendiente);
        Transaccion alterada = new Transaccion("pendiente", TipoTransaccion.DEPOSITO, BigDecimal.TEN,
                pendiente.obtenerFechaHora(), null, origen, EstadoTransaccion.COMPLETADA, "Pendiente");
        assertThrows(ValidacionException.class, () -> transacciones.actualizar(alterada));
        assertSame(pendiente, transacciones.buscarPorIdentificador("pendiente").orElseThrow());
    }

    @Test
    void tampocoPermiteReescribirTransaccionesRechazadas() {
        Transaccion rechazada = operaciones.retirar(iniciarSesion("ana@example.com"), origen, BigDecimal.ONE, "Sin saldo");
        assertThrows(ValidacionException.class, () -> transacciones.actualizar(rechazada));
        assertThrows(ValidacionException.class, () -> rechazada.finalizar(EstadoTransaccion.COMPLETADA));
    }

    @Test
    void noPermiteInsertarCuentaConSaldoInicialPositivo() {
        Cuenta cuenta = new Cuenta("999", TipoCuenta.AHORROS, usuarios.buscarClientePorDocumento("001").orElseThrow()).acreditar(BigDecimal.ONE);
        assertThrows(ValidacionException.class, () -> cuentas.guardar(cuenta));
        assertTrue(cuentas.buscarPorNumero("999").isEmpty());
    }

    @Test
    void noPermiteCambiarTitularDeCuentaExistente() {
        Cuenta alterada = new Cuenta(origen, TipoCuenta.AHORROS, usuarios.buscarClientePorDocumento("002").orElseThrow());
        assertThrows(ValidacionException.class, () -> cuentas.actualizar(alterada));
        assertEquals("001", obtenerCuenta(origen).obtenerCliente().obtenerIdentificacion());
    }

    @Test
    void unidadDeTrabajoRevierteTambienUsuarios() {
        assertThrows(IllegalStateException.class, () -> almacen.ejecutar(() -> {
            Cliente cliente = usuarios.buscarClientePorDocumento("001").orElseThrow();
            cliente.actualizarInformacion("Otro", "otro@example.com", "3000000003");
            usuarios.actualizar(cliente);
            throw new IllegalStateException("Fallo simulado");
        }));
        assertEquals("ana@example.com", usuarios.buscarClientePorDocumento("001").orElseThrow().obtenerCorreo());
    }

    private Transaccion pendiente(String identificador) {
        return new Transaccion(identificador, TipoTransaccion.DEPOSITO, BigDecimal.ONE,
                LocalDateTime.now(), null, origen, EstadoTransaccion.PENDIENTE, "Pendiente");
    }
}
