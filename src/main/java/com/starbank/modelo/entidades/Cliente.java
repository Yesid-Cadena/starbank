package com.starbank.modelo.entidades;

import com.starbank.modelo.enumeraciones.Rol;

public final class Cliente extends Usuario {
    public Cliente(String identificacion, String nombre, String correo,
                   String telefono, ClaveProtegida clave) {
        super(identificacion, nombre, correo, telefono, clave);
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
