package com.starbank.configuracion;

import com.starbank.modelo.repositorios.memoria.AlmacenMemoria;
import com.starbank.modelo.repositorios.memoria.CuentaRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.TransaccionRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.UsuarioRepositorioMemoria;
import com.starbank.modelo.servicios.ClienteServicio;
import com.starbank.modelo.servicios.CuentaServicio;
import com.starbank.modelo.servicios.TransaccionServicio;

/** Compone repositorios y servicios sobre una misma unidad de trabajo. */
public final class ConfiguracionMemoria {
    private final ClienteServicio clientes;
    private final CuentaServicio cuentas;
    private final TransaccionServicio transacciones;

    public ConfiguracionMemoria() {
        AlmacenMemoria almacen = new AlmacenMemoria();
        UsuarioRepositorioMemoria usuarios = new UsuarioRepositorioMemoria(almacen);
        CuentaRepositorioMemoria repositorioCuentas = new CuentaRepositorioMemoria(almacen);
        TransaccionRepositorioMemoria repositorioTransacciones = new TransaccionRepositorioMemoria(almacen);
        clientes = new ClienteServicio(usuarios, almacen);
        cuentas = new CuentaServicio(repositorioCuentas, usuarios, almacen);
        transacciones = new TransaccionServicio(repositorioCuentas, repositorioTransacciones, cuentas, almacen);
    }

    public ClienteServicio obtenerClientes() { return clientes; }
    public CuentaServicio obtenerCuentas() { return cuentas; }
    public TransaccionServicio obtenerTransacciones() { return transacciones; }
}
