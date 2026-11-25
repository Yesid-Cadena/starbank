package com.starbank.modelo.servicios;

import com.starbank.excepciones.SesionExpiradaException;
import com.starbank.excepciones.SesionInvalidaException;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.repositorios.SesionRepositorio;
import com.starbank.utilidades.Validaciones;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

public final class SesionServicio {
    private static final Duration INACTIVIDAD_PREDETERMINADA = Duration.ofMinutes(30);
    private final SesionRepositorio sesiones;
    private final SecureRandom aleatorio;
    private final Clock reloj;
    private final Duration inactividadMaxima;

    public SesionServicio(SesionRepositorio sesiones) {
        this(sesiones, new SecureRandom(), Clock.systemDefaultZone(), INACTIVIDAD_PREDETERMINADA);
    }

    public SesionServicio(SesionRepositorio sesiones, SecureRandom aleatorio, Clock reloj, Duration inactividadMaxima) {
        this.sesiones = Validaciones.obligatorio(sesiones, "El repositorio de sesiones");
        this.aleatorio = Validaciones.obligatorio(aleatorio, "El generador aleatorio");
        this.reloj = Validaciones.obligatorio(reloj, "El reloj");
        this.inactividadMaxima = Validaciones.obligatorio(inactividadMaxima, "La inactividad máxima");
        if (inactividadMaxima.isZero() || inactividadMaxima.isNegative()) {
            throw new IllegalArgumentException("La inactividad máxima debe ser positiva.");
        }
    }

    public Sesion crear(String identificacionUsuario) {
        String usuario = Validaciones.texto(identificacionUsuario, "El identificador de usuario");
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Sesion sesion = new Sesion(generarIdentificador(), usuario, ahora, ahora, ahora.plus(inactividadMaxima), true);
        sesiones.guardar(sesion);
        return sesion;
    }

    public Sesion validar(String identificadorSesion) {
        String id = Validaciones.texto(identificadorSesion, "La sesión");
        Sesion sesion = sesiones.buscarPorIdentificador(id)
                .orElseThrow(() -> new SesionInvalidaException("La sesión no es válida."));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (!sesion.esActiva()) {
            throw new SesionInvalidaException("La sesión no es válida.");
        }
        if (sesion.estaExpirada(ahora)) {
            sesiones.actualizar(sesion.invalidada());
            throw new SesionExpiradaException("La sesión ha expirado por inactividad.");
        }
        Sesion actualizada = sesion.conActividad(ahora, inactividadMaxima);
        sesiones.actualizar(actualizada);
        return actualizada;
    }

    public void cerrar(String identificadorSesion) {
        Sesion sesion = validar(identificadorSesion);
        sesiones.actualizar(sesion.invalidada());
    }

    public void invalidar(String identificadorSesion) {
        String id = Validaciones.texto(identificadorSesion, "La sesión");
        sesiones.buscarPorIdentificador(id).ifPresent(sesion -> {
            if (sesion.esActiva()) {
                sesiones.actualizar(sesion.invalidada());
            }
        });
    }

    public void invalidarPorUsuario(String identificacionUsuario) {
        String usuario = Validaciones.texto(identificacionUsuario, "El identificador de usuario");
        for (Sesion sesion : sesiones.buscarPorUsuario(usuario)) {
            if (sesion.esActiva()) {
                sesiones.actualizar(sesion.invalidada());
            }
        }
    }

    public String obtenerUsuarioAutenticado(String identificadorSesion) {
        return validar(identificadorSesion).obtenerIdentificacionUsuario();
    }

    private String generarIdentificador() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
