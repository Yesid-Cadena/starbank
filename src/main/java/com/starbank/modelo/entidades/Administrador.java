package com.starbank.modelo.entidades;

import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.Rol;
import java.time.LocalDateTime;

public final class Administrador extends Usuario {
    public Administrador(String identificacion, String nombre, String correo,
                         String telefono, ClaveProtegida clave) {
        super(identificacion, nombre, correo, telefono, clave);
    }

    public Administrador(String identificacion, String nombre, String correo, String telefono,
                         ClaveProtegida clave, LocalDateTime fechaRegistro, EstadoUsuario estado) {
        super(identificacion, nombre, correo, telefono, clave, fechaRegistro, estado);
    }

    private Administrador(Administrador original) {
        super(original);
    }

    @Override
    public Administrador copiar() {
        return new Administrador(this);
    }

    @Override
    public Rol obtenerRol() {
        return Rol.ADMINISTRADOR;
    }
}
