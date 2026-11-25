package com.starbank.modelo.entidades;

import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.Rol;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {
    private static ClaveProtegida clave;

    @BeforeAll
    static void prepararClave() {
        clave = ClaveProtegida.crear("Clave-ficticia-21".toCharArray());
    }

    private Cliente cliente() {
        return new Cliente("001", "Ana", "ana@example.com", "3000000001", clave);
    }

    @Test
    void representaAmbosRolesMedianteLaAbstraccionUsuario() {
        LocalDateTime antes = LocalDateTime.now();
        Usuario cliente = cliente();
        Usuario administrador = new Administrador("002", "Luis", "luis@example.com",
                "3000000002", clave);
        assertEquals(Rol.CLIENTE, cliente.obtenerRol());
        assertEquals(Rol.ADMINISTRADOR, administrador.obtenerRol());
        assertEquals(EstadoUsuario.ACTIVO, cliente.obtenerEstado());
        assertEquals(EstadoUsuario.ACTIVO, administrador.obtenerEstado());
        assertFalse(cliente.obtenerFechaRegistro().isBefore(antes));
        assertFalse(cliente.obtenerFechaRegistro().isAfter(LocalDateTime.now()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rechazaIdentificacionYNombreVaciosEnAmbosTipos(String valor) {
        assertThrows(ValidacionException.class,
                () -> new Cliente(valor, "Ana", "ana@example.com", "3000000001", clave));
        assertThrows(ValidacionException.class,
                () -> new Cliente("001", valor, "ana@example.com", "3000000001", clave));
        assertThrows(ValidacionException.class,
                () -> new Administrador(valor, "Luis", "luis@example.com", "3000000002", clave));
        assertThrows(ValidacionException.class,
                () -> new Administrador("002", valor, "luis@example.com", "3000000002", clave));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ana", "ana@", "@ejemplo.com", "ana @ejemplo.com", "ana@@ejemplo.com"})
    void rechazaCorreosInvalidos(String correo) {
        assertThrows(ValidacionException.class,
                () -> new Cliente("001", "Ana", correo, "3000000001", clave));
        assertThrows(ValidacionException.class,
                () -> new Administrador("002", "Luis", correo, "3000000002", clave));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abcdefg", "123456", "1234567890123456", "+---()"})
    void rechazaTelefonosInvalidos(String telefono) {
        assertThrows(ValidacionException.class,
                () -> new Cliente("001", "Ana", "ana@example.com", telefono, clave));
        assertThrows(ValidacionException.class,
                () -> new Administrador("002", "Luis", "luis@example.com", telefono, clave));
    }

    @Test
    void actualizaInformacionSinCambiarIdentidad() {
        Cliente cliente = cliente();
        cliente.actualizarInformacion(" Ana María ", " nueva@example.com ", "+57 300 000 0001");
        assertEquals("Ana María", cliente.obtenerNombre());
        assertEquals("nueva@example.com", cliente.obtenerCorreo());
        assertEquals("+57 300 000 0001", cliente.obtenerTelefono());
        assertEquals("001", cliente.obtenerIdentificacion());
    }

    @Test
    void actualizacionInvalidaConservaTodosLosDatosAnteriores() {
        Cliente cliente = cliente();
        assertThrows(ValidacionException.class,
                () -> cliente.actualizarInformacion("Nuevo", "nuevo@example.com", "x"));
        assertEquals("Ana", cliente.obtenerNombre());
        assertEquals("ana@example.com", cliente.obtenerCorreo());
        assertEquals("3000000001", cliente.obtenerTelefono());
    }

    @Test
    void estadoNuloNoReemplazaElAnterior() {
        Usuario usuario = cliente();
        usuario.actualizarEstado(EstadoUsuario.BLOQUEADO);
        assertThrows(ValidacionException.class, () -> usuario.actualizarEstado(null));
        assertEquals(EstadoUsuario.BLOQUEADO, usuario.obtenerEstado());
    }

    @Test
    void exigeClaveProtegidaYCompruebaCandidatas() {
        assertThrows(ValidacionException.class,
                () -> new Cliente("001", "Ana", "ana@example.com", "3000000001", null));
        assertThrows(ValidacionException.class,
                () -> new Administrador("002", "Luis", "luis@example.com", "3000000002", null));
        assertTrue(cliente().verificarClave("Clave-ficticia-21".toCharArray()));
        assertFalse(cliente().verificarClave("Clave-incorrecta".toCharArray()));
    }
}
