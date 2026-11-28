package com.starbank.controlador;
public interface RegistroVista {
    void mostrarCargando(boolean cargando);
    void mostrarError(String mensaje);
    void mostrarExito(String mensaje);
    void limpiarFormulario();
}
