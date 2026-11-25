package com.starbank.modelo.entidades;

import com.starbank.utilidades.Validaciones;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** Sesión en memoria; el identificador se genera con un mecanismo seguro y no debe exponerse en logs. */
public final class Sesion {
    private final String identificador;
    private final String identificacionUsuario;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime ultimaActividad;
    private final LocalDateTime fechaExpiracion;
    private final boolean activa;

    public Sesion(String identificador, String identificacionUsuario, LocalDateTime fechaCreacion,
                  LocalDateTime ultimaActividad, LocalDateTime fechaExpiracion, boolean activa) {
        this.identificador = Validaciones.texto(identificador, "El identificador de sesión");
        this.identificacionUsuario = Validaciones.texto(identificacionUsuario, "El identificador de usuario");
        this.fechaCreacion = Validaciones.obligatorio(fechaCreacion, "La fecha de creación");
        this.ultimaActividad = Validaciones.obligatorio(ultimaActividad, "La última actividad");
        this.fechaExpiracion = Validaciones.obligatorio(fechaExpiracion, "La fecha de expiración");
        this.activa = activa;
    }

    public Sesion conActividad(LocalDateTime ultimaActividad, Duration inactividad) {
        Validaciones.obligatorio(ultimaActividad, "La última actividad");
        Validaciones.obligatorio(inactividad, "La inactividad");
        return new Sesion(identificador, identificacionUsuario, fechaCreacion, ultimaActividad,
                ultimaActividad.plus(inactividad), activa);
    }

    public Sesion invalidada() {
        return new Sesion(identificador, identificacionUsuario, fechaCreacion, ultimaActividad, fechaExpiracion, false);
    }

    public boolean estaExpirada(LocalDateTime ahora) {
        return !ahora.isBefore(fechaExpiracion);
    }

    public String obtenerIdentificador() { return identificador; }
    public String obtenerIdentificacionUsuario() { return identificacionUsuario; }
    public LocalDateTime obtenerFechaCreacion() { return fechaCreacion; }
    public LocalDateTime obtenerUltimaActividad() { return ultimaActividad; }
    public LocalDateTime obtenerFechaExpiracion() { return fechaExpiracion; }
    public boolean esActiva() { return activa; }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Sesion sesion && identificador.equals(sesion.identificador);
    }

    @Override
    public int hashCode() { return Objects.hash(identificador); }
}
