package com.starbank.controlador;

import com.starbank.excepciones.SesionInvalidaException;
import com.starbank.modelo.enumeraciones.Rol;
import com.starbank.soporte.BancoPrueba;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControladoresGuiTest extends BancoPrueba {

    static class LoginVistaFake implements LoginVista {
        String error; String exito; boolean cargando; boolean claveLimpia;
        @Override public void mostrarCargando(boolean c) { cargando = c; }
        @Override public void mostrarError(String m) { error = m; }
        @Override public void mostrarExito(String m) { exito = m; }
        @Override public void limpiarClave() { claveLimpia = true; }
    }

    static class RegistroVistaFake implements RegistroVista {
        String error; String exito; boolean cargando; boolean limpio;
        @Override public void mostrarCargando(boolean c) { cargando = c; }
        @Override public void mostrarError(String m) { error = m; }
        @Override public void mostrarExito(String m) { exito = m; }
        @Override public void limpiarFormulario() { limpio = true; }
    }

    @Test
    void loginValidaCamposObligatorios() {
        LoginVistaFake vista = new LoginVistaFake();
        AtomicReference<Rol> rol = new AtomicReference<>();
        LoginControlador c = new LoginControlador(configuracion, new SesionControlador(configuracion), vista, rol::set);
        assertFalse(c.iniciarSesion("", "x".toCharArray()));
        assertEquals("Correo y contraseña son obligatorios.", vista.error);
        assertNull(rol.get());
    }

    @Test
    void loginManejaCredencialesIncorrectas() {
        LoginVistaFake vista = new LoginVistaFake();
        AtomicReference<Rol> rol = new AtomicReference<>();
        LoginControlador c = new LoginControlador(configuracion, new SesionControlador(configuracion), vista, rol::set);
        assertFalse(c.iniciarSesion("ana@example.com", "mala-clave-21".toCharArray()));
        assertNotNull(vista.error);
        assertTrue(vista.claveLimpia);
        assertNull(rol.get());
    }

    @Test
    void loginNavegaPorRol() {
        LoginVistaFake vista = new LoginVistaFake();
        AtomicReference<Rol> rol = new AtomicReference<>();
        SesionControlador sesion = new SesionControlador(configuracion);
        LoginControlador c = new LoginControlador(configuracion, sesion, vista, rol::set);
        assertTrue(c.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray()));
        assertEquals(Rol.CLIENTE, rol.get());
        assertTrue(sesion.haySesion());

        LoginVistaFake vistaAdmin = new LoginVistaFake();
        AtomicReference<Rol> rolAdmin = new AtomicReference<>();
        SesionControlador sesionAdminCtrl = new SesionControlador(configuracion);
        LoginControlador ca = new LoginControlador(configuracion, sesionAdminCtrl, vistaAdmin, rolAdmin::set);
        assertTrue(ca.iniciarSesion("admin@example.com", CLAVE_TEXTO.toCharArray()));
        assertEquals(Rol.ADMINISTRADOR, rolAdmin.get());
    }

    @Test
    void registroValidaYPrevieneDuplicados() {
        RegistroVistaFake vista = new RegistroVistaFake();
        RegistroControlador c = new RegistroControlador(configuracion, vista, () -> {});
        assertFalse(c.registrar("", "Nombre", "a@b.com", "3000000001", "ClaveSegura-21".toCharArray(), "ClaveSegura-21".toCharArray()));
        assertNotNull(vista.error);

        RegistroVistaFake vista2 = new RegistroVistaFake();
        RegistroControlador c2 = new RegistroControlador(configuracion, vista2, () -> {});
        assertFalse(c2.registrar("C100", "Nombre", "nuevo@test.com", "3000000001", "ClaveSegura-21".toCharArray(), "otra-clave-21".toCharArray()));
        assertTrue(vista2.error.toLowerCase().contains("confirm"));

        RegistroVistaFake vista3 = new RegistroVistaFake();
        RegistroControlador c3 = new RegistroControlador(configuracion, vista3, () -> {});
        assertTrue(c3.registrar("C100", "Nuevo", "nuevo@test.com", "3000000001", "ClaveSegura-21".toCharArray(), "ClaveSegura-21".toCharArray()));
        assertTrue(vista3.limpio);
    }

    @Test
    void cierreYSesionExpirada() {
        SesionControlador sesion = new SesionControlador(configuracion);
        var s = autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray());
        sesion.iniciar(s);
        assertEquals(Rol.CLIENTE, sesion.rolActual());
        sesion.cerrar();
        assertFalse(sesion.haySesion());
        assertThrows(SesionInvalidaException.class, sesion::usuarioActual);
    }

    @Test
    void impedirAccesoSinSesion() {
        SesionControlador sesion = new SesionControlador(configuracion);
        assertFalse(sesion.haySesion());
        assertThrows(SesionInvalidaException.class, sesion::usuarioActual);
    }
}
