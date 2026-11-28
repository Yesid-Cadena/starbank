package com.starbank.controlador;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.excepciones.ValidacionException;
import java.util.Arrays;

public class RegistroControlador {
    private final ConfiguracionServicios config;
    private final RegistroVista vista;
    private final Runnable volverLogin;
    private boolean procesando;

    public RegistroControlador(ConfiguracionServicios config, RegistroVista vista, Runnable volverLogin) {
        this.config = config;
        this.vista = vista;
        this.volverLogin = volverLogin;
    }

    public synchronized boolean registrar(String documento, String nombre, String correo, String telefono, char[] clave, char[] confirmacion) {
        if (procesando) return false;
        if (vacio(documento) || vacio(nombre) || vacio(correo) || vacio(telefono) || clave == null || clave.length == 0 || confirmacion == null || confirmacion.length == 0) {
            vista.mostrarError("Todos los campos son obligatorios.");
            return false;
        }
        if (!Arrays.equals(clave, confirmacion)) {
            vista.mostrarError("La confirmación de contraseña no coincide.");
            return false;
        }
        procesando = true;
        vista.mostrarCargando(true);
        try {
            config.obtenerClientes().registrar(documento, nombre, correo, telefono, clave);
            vista.mostrarExito("Registro exitoso. Ya puedes iniciar sesión.");
            vista.limpiarFormulario();
            return true;
        } catch (ValidacionException e) {
            vista.mostrarError(e.getMessage());
            return false;
        } catch (Exception e) {
            vista.mostrarError("No se pudo completar el registro.");
            return false;
        } finally {
            procesando = false;
            vista.mostrarCargando(false);
            Arrays.fill(confirmacion, '\0');
        }
    }

    public void irALogin() { if (volverLogin != null) volverLogin.run(); }

    private boolean vacio(String s) { return s == null || s.isBlank(); }
}
