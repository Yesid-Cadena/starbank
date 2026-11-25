package com.starbank.modelo.repositorios.memoria;

import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Un único monitor serializa operaciones y consultas para impedir lecturas parciales. */
public final class AlmacenMemoria implements UnidadTrabajo {
    private Map<String, Usuario> usuarios = new HashMap<>();
    private Map<String, Cuenta> cuentas = new HashMap<>();
    private Map<String, Transaccion> transacciones = new HashMap<>();

    Map<String, Usuario> usuarios() { return usuarios; }
    Map<String, Cuenta> cuentas() { return cuentas; }
    Map<String, Transaccion> transacciones() { return transacciones; }

    @Override
    public synchronized <T> T ejecutar(Supplier<T> operacion) {
        // Los repositorios reemplazan versiones y nunca exponen los objetos almacenados.
        Map<String, Usuario> usuariosAnteriores = new HashMap<>(usuarios);
        Map<String, Cuenta> cuentasAnteriores = new HashMap<>(cuentas);
        Map<String, Transaccion> transaccionesAnteriores = new HashMap<>(transacciones);
        try {
            return operacion.get();
        } catch (RuntimeException | Error excepcion) {
            usuarios = usuariosAnteriores;
            cuentas = cuentasAnteriores;
            transacciones = transaccionesAnteriores;
            throw excepcion;
        }
    }
}
