package com.starbank.modelo.entidades;

import com.starbank.utilidades.Validaciones;
import java.time.LocalDateTime;

/** Registro inmutable de auditoría. No debe contener contraseñas ni secretos de sesión. */
public final class RegistroActividad {
    private final String identificador;
    private final String identificacionUsuario;
    private final String tipoActividad;
    private final LocalDateTime fechaHora;
    private final String resultado;
    private final String descripcion;
    private final String recursoAfectado;

    public RegistroActividad(String identificador, String identificacionUsuario, String tipoActividad,
                             LocalDateTime fechaHora, String resultado, String descripcion, String recursoAfectado) {
        this.identificador = Validaciones.texto(identificador, "El identificador");
        this.identificacionUsuario = identificacionUsuario == null ? null : Validaciones.texto(identificacionUsuario, "El usuario");
        this.tipoActividad = Validaciones.texto(tipoActividad, "El tipo de actividad");
        this.fechaHora = Validaciones.obligatorio(fechaHora, "La fecha y hora");
        this.resultado = Validaciones.texto(resultado, "El resultado");
        this.descripcion = Validaciones.texto(descripcion, "La descripción");
        this.recursoAfectado = recursoAfectado == null ? null : Validaciones.texto(recursoAfectado, "El recurso afectado");
    }

    public String obtenerIdentificador() { return identificador; }
    public String obtenerIdentificacionUsuario() { return identificacionUsuario; }
    public String obtenerTipoActividad() { return tipoActividad; }
    public LocalDateTime obtenerFechaHora() { return fechaHora; }
    public String obtenerResultado() { return resultado; }
    public String obtenerDescripcion() { return descripcion; }
    public String obtenerRecursoAfectado() { return recursoAfectado; }
}
