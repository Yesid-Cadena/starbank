package com.starbank.controlador;
public interface LoginVista {
    void mostrarCargando(boolean cargando);
    void mostrarError(String mensaje);
    void mostrarExito(String mensaje);
    void limpiarClave();
}
