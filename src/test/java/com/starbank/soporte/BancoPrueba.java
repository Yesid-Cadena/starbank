package com.starbank.soporte;

import com.starbank.modelo.entidades.ClaveProtegida;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.modelo.repositorios.memoria.AlmacenMemoria;
import com.starbank.modelo.repositorios.memoria.CuentaRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.TransaccionRepositorioMemoria;
import com.starbank.modelo.repositorios.memoria.UsuarioRepositorioMemoria;
import com.starbank.modelo.servicios.ClienteServicio;
import com.starbank.modelo.servicios.CuentaServicio;
import com.starbank.modelo.servicios.TransaccionServicio;
import java.math.BigDecimal;

public class BancoPrueba {
    protected static final ClaveProtegida CLAVE = ClaveProtegida.crear("Clave-ficticia-21".toCharArray());
    protected final AlmacenMemoria almacen = new AlmacenMemoria();
    protected final UsuarioRepositorioMemoria usuarios = new UsuarioRepositorioMemoria(almacen);
    protected final CuentaRepositorioMemoria cuentas = new CuentaRepositorioMemoria(almacen);
    protected final TransaccionRepositorioMemoria transacciones = new TransaccionRepositorioMemoria(almacen);
    protected final ClienteServicio clientes = new ClienteServicio(usuarios, almacen);
    protected final CuentaServicio servicioCuentas = new CuentaServicio(cuentas, usuarios, almacen);
    protected final TransaccionServicio operaciones = new TransaccionServicio(cuentas, transacciones, servicioCuentas, almacen);
    protected final String origen;
    protected final String destino;

    public BancoPrueba() {
        usuarios.guardar(new Cliente("001", "Ana", "ana@example.com", "3000000001", CLAVE));
        usuarios.guardar(new Cliente("002", "Luis", "luis@example.com", "3000000002", CLAVE));
        origen = servicioCuentas.crear("001", TipoCuenta.AHORROS).obtenerNumeroCuenta();
        destino = servicioCuentas.crear("002", TipoCuenta.CORRIENTE).obtenerNumeroCuenta();
    }

    protected BigDecimal saldo(String numero) {
        return cuentas.buscarPorNumero(numero).orElseThrow().consultarSaldo();
    }

    protected Cuenta obtenerCuenta(String numero) {
        return cuentas.buscarPorNumero(numero).orElseThrow();
    }
}
