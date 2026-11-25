package com.starbank.modelo.entidades;

import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.Rol;
import com.starbank.utilidades.Validaciones;
import java.time.LocalDateTime;

public abstract class Usuario {
    private final String identificacion;
    private String nombre;
    private String correo;
    private String telefono;
    private final ClaveProtegida clave;
    private final LocalDateTime fechaRegistro;
    private EstadoUsuario estado;

    protected Usuario(String identificacion, String nombre, String correo,
                      String telefono, ClaveProtegida clave) {
        this.identificacion = Validaciones.texto(identificacion, "La identificación");
        this.nombre = Validaciones.texto(nombre, "El nombre");
        this.correo = Validaciones.correo(correo);
        this.telefono = Validaciones.telefono(telefono);
        this.clave = Validaciones.obligatorio(clave, "La clave protegida");
        fechaRegistro = LocalDateTime.now();
        estado = EstadoUsuario.ACTIVO;
    }

    protected Usuario(Usuario original) {
        identificacion = original.identificacion;
        nombre = original.nombre;
        correo = original.correo;
        telefono = original.telefono;
        clave = original.clave;
        fechaRegistro = original.fechaRegistro;
        estado = original.estado;
    }

    public abstract Usuario copiar();

    public abstract Rol obtenerRol();

    public final void actualizarInformacion(String nombre, String correo, String telefono) {
        String nuevoNombre = Validaciones.texto(nombre, "El nombre");
        String nuevoCorreo = Validaciones.correo(correo);
        String nuevoTelefono = Validaciones.telefono(telefono);
        this.nombre = nuevoNombre;
        this.correo = nuevoCorreo;
        this.telefono = nuevoTelefono;
    }

    public final void actualizarEstado(EstadoUsuario estado) {
        this.estado = Validaciones.obligatorio(estado, "El estado del usuario");
    }

    public final String obtenerIdentificacion() { return identificacion; }
    public final String obtenerNombre() { return nombre; }
    public final String obtenerCorreo() { return correo; }
    public final String obtenerTelefono() { return telefono; }
    public final LocalDateTime obtenerFechaRegistro() { return fechaRegistro; }
    public final EstadoUsuario obtenerEstado() { return estado; }

    /** Comparación criptográfica local; no crea sesiones ni concede permisos. */
    public final boolean verificarClave(char[] candidata) {
        return clave.coincide(candidata);
    }
}
