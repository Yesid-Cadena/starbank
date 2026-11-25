package com.starbank.modelo.entidades;

import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.Rol;
import java.time.LocalDateTime;

public final class Cliente extends Usuario {
    public Cliente(String identificacion, String nombre, String correo,
                   String telefono, ClaveProtegida clave) {
        super(identificacion, nombre, correo, telefono, clave);
    }

    public Cliente(String identificacion, String nombre, String correo, String telefono,
                   ClaveProtegida clave, LocalDateTime fechaRegistro, EstadoUsuario estado) {
        super(identificacion, nombre, correo, telefono, clave, fechaRegistro, estado);
    }

    private Cliente(Cliente original) {
        super(original);
    }

    @Override
    public Cliente copiar() {
        return new Cliente(this);
    }

    @Override
    public Rol obtenerRol() {
        return Rol.CLIENTE;
    }
}
