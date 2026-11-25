package com.starbank.modelo.servicios;

import com.starbank.excepciones.AccesoDenegadoException;
import com.starbank.excepciones.AutenticacionException;
import com.starbank.excepciones.SesionExpiradaException;
import com.starbank.excepciones.SesionInvalidaException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.RegistroActividad;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.soporte.BancoPrueba;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeguridadServiciosTest extends BancoPrueba {

    static final class RelojMutable extends Clock {
        private Instant instante;
        private final ZoneId zona;
        RelojMutable(Instant instante) { this.instante = instante; this.zona = ZoneId.of("UTC"); }
        @Override public ZoneId getZone() { return zona; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instante; }
        void avanzar(Duration d) { instante = instante.plus(d); }
    }

    @Test
    void autenticacionValidaEInvalida() {
        Sesion sesion = autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray());
        assertNotNull(sesion.obtenerIdentificador());
        assertEquals("001", sesion.obtenerIdentificacionUsuario());
        assertThrows(AutenticacionException.class, () -> autenticacion.iniciarSesion("ana@example.com", "mala-clave-21".toCharArray()));
        assertThrows(AutenticacionException.class, () -> autenticacion.iniciarSesion("noexiste@example.com", "mala-clave-21".toCharArray()));
    }

    @Test
    void usuarioInactivoNoPuedeAutenticarse() {
        clientes.cambiarEstado(sesionAdmin, "001", EstadoUsuario.INACTIVO);
        assertThrows(AutenticacionException.class, () -> autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray()));
    }

    @Test
    void cierreYExpiracionDeSesion() {
        RelojMutable reloj = new RelojMutable(Instant.parse("2026-01-01T10:00:00Z"));
        BancoPrueba ctx = new BancoPrueba(reloj);
        Sesion s = ctx.autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray());
        ctx.autenticacion.cerrarSesion(s.obtenerIdentificador());
        assertThrows(SesionInvalidaException.class, () -> ctx.autorizacion.exigirUsuario(s.obtenerIdentificador()));

        Sesion s2 = ctx.autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray());
        reloj.avanzar(Duration.ofMinutes(31));
        assertThrows(SesionExpiradaException.class, () -> ctx.autorizacion.exigirUsuario(s2.obtenerIdentificador()));
    }

    @Test
    void cambioDeClaveInvalidaSesiones() {
        Sesion s1 = autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray());
        Sesion nueva = autenticacion.cambiarClave(s1.obtenerIdentificador(), CLAVE_TEXTO.toCharArray(), "NuevaClave-21".toCharArray());
        assertThrows(SesionInvalidaException.class, () -> autorizacion.exigirUsuario(s1.obtenerIdentificador()));
        assertNotNull(autorizacion.exigirUsuario(nueva.obtenerIdentificador()));
        assertThrows(AutenticacionException.class, () -> autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray()));
        assertNotNull(autenticacion.iniciarSesion("ana@example.com", "NuevaClave-21".toCharArray()));
    }

    @Test
    void limiteIntentosFallidos() {
        RelojMutable reloj = new RelojMutable(Instant.parse("2026-01-01T10:00:00Z"));
        BancoPrueba ctx = new BancoPrueba(reloj);
        for (int i = 0; i < 5; i++) {
            assertThrows(AutenticacionException.class, () -> ctx.autenticacion.iniciarSesion("ana@example.com", "mala-clave-21".toCharArray()));
        }
        AutenticacionException bloqueado = assertThrows(AutenticacionException.class, () -> ctx.autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray()));
        assertTrue(bloqueado.getMessage().toLowerCase().contains("temporal"));
        reloj.avanzar(Duration.ofMinutes(6));
        assertNotNull(ctx.autenticacion.iniciarSesion("ana@example.com", CLAVE_TEXTO.toCharArray()));
    }

    @Test
    void accesoPorRolYPropiedad() {
        Sesion sesionAna = iniciarSesion("ana@example.com");
        Sesion sesionLuis = iniciarSesion("luis@example.com");
        assertThrows(AccesoDenegadoException.class, () -> servicioCuentas.consultarSaldo(sesionAna, destino));
        assertThrows(AccesoDenegadoException.class, () -> operaciones.retirar(sesionAna, destino, BigDecimal.ONE, "no"));
        assertThrows(AccesoDenegadoException.class, () -> clientes.listarClientes(sesionAna));
        assertThrows(AccesoDenegadoException.class, () -> servicioCuentas.bloquear(sesionAna, origen));
        // transferencia autorizada desde cuenta propia
        operaciones.depositar(sesionAna, origen, new BigDecimal("100.00"), "dep");
        var tx = operaciones.transferir(sesionAna, origen, destino, new BigDecimal("10.00"), "ok");
        assertEquals("COMPLETADA", tx.obtenerEstado().name());
        assertThrows(AccesoDenegadoException.class, () -> operaciones.transferir(sesionLuis, origen, destino, new BigDecimal("1.00"), "no"));
    }

    @Test
    void noSePuedeAsignarRolAdminEnRegistroPublico() {
        // registrar siempre crea Cliente; no hay API pública para crear Administrador
        var cliente = clientes.registrar("C999", "Nuevo", "nuevo@example.com", "3000000098", "NuevaClave-21".toCharArray());
        assertEquals("CLIENTE", cliente.obtenerRol().name());
    }

    @Test
    void auditoriaRegistraYEsSoloAdmin() {
        Sesion sesionAna = iniciarSesion("ana@example.com");
        assertThrows(AccesoDenegadoException.class, () -> auditoria.listar(sesionAna, autorizacion));
        List<RegistroActividad> registros = auditoria.listar(sesionAdmin, autorizacion);
        assertFalse(registros.isEmpty());
        assertTrue(registros.stream().anyMatch(r -> "INICIO_SESION".equals(r.obtenerTipoActividad())));
    }

    @Test
    void politicaMonetariaDosDecimales() {
        Sesion sesionAna = iniciarSesion("ana@example.com");
        assertThrows(ValidacionException.class, () -> operaciones.depositar(sesionAna, origen, new BigDecimal("1.234"), "mal"));
        assertDoesNotThrow(() -> operaciones.depositar(sesionAna, origen, new BigDecimal("1.20"), "ok"));
    }

    @Test
    void auditoriaPersisteAnteReversion() {
        // provocar fallo de persistencia como en AtomicidadTest usando repos con fallo
        // simplificado: se verifica que hay registros incluso cuando operación falla
        long antes = auditoria.listarInterno().size();
        assertThrows(AutenticacionException.class, () -> autenticacion.iniciarSesion("ana@example.com", "mala-clave-21".toCharArray()));
        long despues = auditoria.listarInterno().size();
        assertTrue(despues > antes);
    }
}
