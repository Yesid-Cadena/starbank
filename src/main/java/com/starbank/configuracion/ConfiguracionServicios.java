package com.starbank.configuracion;

import com.starbank.modelo.entidades.Administrador;
import com.starbank.modelo.repositorios.ActividadRepositorio;
import com.starbank.modelo.servicios.AuditoriaServicio;
import com.starbank.modelo.servicios.AutenticacionServicio;
import com.starbank.modelo.servicios.AutorizacionServicio;
import com.starbank.modelo.servicios.ClienteServicio;
import com.starbank.modelo.servicios.CuentaServicio;
import com.starbank.modelo.servicios.SesionServicio;
import com.starbank.modelo.servicios.TransaccionServicio;

public interface ConfiguracionServicios {
    Administrador crearAdministrador(String identificacion, String nombre, String correo, String telefono, char[] clave);
    AuditoriaServicio obtenerAuditoria();
    SesionServicio obtenerSesiones();
    AutorizacionServicio obtenerAutorizacion();
    AutenticacionServicio obtenerAutenticacion();
    ClienteServicio obtenerClientes();
    CuentaServicio obtenerCuentas();
    TransaccionServicio obtenerTransacciones();
}
