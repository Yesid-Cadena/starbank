package com.starbank.controlador;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.excepciones.SesionExpiradaException;
import com.starbank.excepciones.SesionInvalidaException;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.Rol;

public class SesionControlador {
    private final ConfiguracionServicios config;
    private Sesion sesion;

    public SesionControlador(ConfiguracionServicios config) {
        this.config = config;
    }

    public synchronized Sesion getSesion() { return sesion; }

    public synchronized void iniciar(Sesion sesion) { this.sesion = sesion; }

    public synchronized void cerrar() {
        if (sesion != null) {
            try { config.obtenerAutenticacion().cerrarSesion(sesion.obtenerIdentificador()); } catch (Exception ignorada) {}
            sesion = null;
        }
    }

    public synchronized Usuario usuarioActual() {
        if (sesion == null) throw new SesionInvalidaException("No hay sesión activa.");
        try {
            Usuario u = config.obtenerAutorizacion().exigirUsuario(sesion.obtenerIdentificador());
            // refrescar referencia de sesión no es necesario; la validez la da el servicio
            return u;
        } catch (SesionExpiradaException | SesionInvalidaException e) {
            sesion = null;
            throw e;
        }
    }

    public synchronized Rol rolActual() { return usuarioActual().obtenerRol(); }
    public synchronized boolean haySesion() { return sesion != null; }
}
