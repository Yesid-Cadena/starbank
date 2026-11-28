package com.starbank.controlador;
public interface RegistroVista {
    void mostrarCargando(boolean cargando);
    void mostrarError(String mensaje);
    void mostrarExito(String mensaje);
    void limpiarFormulario();
    default void mostrarErrorCampo(String campo, String mensaje) { mostrarError(mensaje); }
    default void limpiarErrores() {}
}
