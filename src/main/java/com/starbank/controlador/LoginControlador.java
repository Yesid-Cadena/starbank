package com.starbank.controlador;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.excepciones.AutenticacionException;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.Rol;
import java.util.function.Consumer;

public class LoginControlador {
    private final ConfiguracionServicios config;
    private final SesionControlador sesionControlador;
    private final LoginVista vista;
    private final Consumer<Rol> navegacionPorRol;
    private boolean procesando;

    public LoginControlador(ConfiguracionServicios config, SesionControlador sesionControlador, LoginVista vista, Consumer<Rol> navegacionPorRol) {
        this.config = config;
        this.sesionControlador = sesionControlador;
        this.vista = vista;
        this.navegacionPorRol = navegacionPorRol;
    }

    public synchronized boolean iniciarSesion(String correo, char[] clave) {
        if (procesando) return false;
        if (correo == null || correo.isBlank() || clave == null || clave.length == 0) {
            vista.mostrarError("Correo y contraseña son obligatorios.");
            return false;
        }
        procesando = true;
        vista.mostrarCargando(true);
        try {
            Sesion sesion = config.obtenerAutenticacion().iniciarSesion(correo, clave);
            sesionControlador.iniciar(sesion);
            Usuario usuario = config.obtenerAutorizacion().exigirUsuario(sesion.obtenerIdentificador());
            vista.limpiarClave();
            vista.mostrarExito("Bienvenido.");
            navegacionPorRol.accept(usuario.obtenerRol());
            return true;
        } catch (AutenticacionException e) {
            vista.limpiarClave();
            vista.mostrarError(e.getMessage());
            return false;
        } catch (Exception e) {
            vista.limpiarClave();
            vista.mostrarError("No se pudo iniciar sesión.");
            return false;
        } finally {
            procesando = false;
            vista.mostrarCargando(false);
        }
    }
}
