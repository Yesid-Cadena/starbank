package com.starbank.modelo.servicios;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Administrador;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.soporte.BancoPrueba;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteServicioTest extends BancoPrueba {
    @Test
    void registraClienteConClaveProtegida() {
        Cliente cliente = clientes.registrar("003", "Eva", "eva@example.com", "3000000003",
                "Otra-clave-21".toCharArray());
        assertEquals("003", clientes.consultar("003").obtenerIdentificacion());
        assertEquals(EstadoUsuario.ACTIVO, clientes.consultarEstado("003"));
        assertTrue(cliente.verificarClave("Otra-clave-21".toCharArray()));
        assertEquals(3, clientes.listarClientes().size());
    }

    @Test
    void rechazaDocumentoDuplicadoSinReemplazarCliente() {
        assertThrows(RegistroDuplicadoException.class, () -> clientes.registrar(" 001 ", "Otro",
                "otro@example.com", "3000000003", "Clave-123".toCharArray()));
        assertEquals("Ana", clientes.consultar("001").obtenerNombre());
        assertEquals(2, clientes.listarClientes().size());
    }

    @Test
    void rechazaCorreoDuplicadoIgnorandoMayusculasYEspacios() {
        assertThrows(RegistroDuplicadoException.class, () -> clientes.registrar("003", "Otro",
                " ANA@EXAMPLE.COM ", "3000000003", "Clave-123".toCharArray()));
        assertEquals(2, clientes.listarClientes().size());
    }

    @Test
    void actualizaDatosYConservaClaveFechaEIdentidad() {
        Cliente anterior = clientes.consultar("001");
        clientes.actualizarInformacion("001", "Ana María", "nueva@example.com", "3000000004");
        Cliente nuevo = clientes.consultar("001");
        assertEquals("Ana María", nuevo.obtenerNombre());
        assertEquals("3000000004", nuevo.obtenerTelefono());
        assertEquals(anterior.obtenerFechaRegistro(), nuevo.obtenerFechaRegistro());
        assertTrue(nuevo.verificarClave("Clave-ficticia-21".toCharArray()));
        assertTrue(usuarios.buscarPorCorreo("ana@example.com").isEmpty());
        assertEquals("001", usuarios.buscarPorCorreo("nueva@example.com").orElseThrow().obtenerIdentificacion());
    }

    @Test
    void actualizarCorreoDuplicadoNoModificaDatos() {
        assertThrows(RegistroDuplicadoException.class,
                () -> clientes.actualizarInformacion("001", "Nuevo", "luis@example.com", "3000000004"));
        assertEquals("Ana", clientes.consultar("001").obtenerNombre());
    }

    @Test
    void actualizarDatosInvalidosNoModificaDatos() {
        assertThrows(ValidacionException.class,
                () -> clientes.actualizarInformacion("001", "Nuevo", "nuevo@example.com", "x"));
        assertEquals("ana@example.com", clientes.consultar("001").obtenerCorreo());
    }

    @Test
    void permiteMantenerCorreoPropioYExcluyeAdministradoresDelListado() {
        usuarios.guardar(new Administrador("admin", "Admin", "admin@example.com", "3000000005", CLAVE));
        clientes.actualizarInformacion("001", "Ana", "ANA@example.com", "3000000001");
        assertEquals(2, clientes.listarClientes().size());
        assertThrows(RegistroNoEncontradoException.class, () -> clientes.consultar("admin"));
        assertThrows(RegistroNoEncontradoException.class, () -> clientes.consultar("ausente"));
        assertThrows(RegistroDuplicadoException.class, () -> clientes.registrar("003", "Otro",
                "admin@example.com", "3000000003", "Clave-123".toCharArray()));
    }
}
