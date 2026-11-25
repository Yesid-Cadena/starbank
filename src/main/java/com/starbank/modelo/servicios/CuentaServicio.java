package com.starbank.modelo.servicios;

import com.starbank.excepciones.OperacionRechazadaException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.enumeraciones.EstadoCuenta;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.modelo.repositorios.CuentaRepositorio;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public final class CuentaServicio {
    private final CuentaRepositorio cuentas;
    private final UsuarioRepositorio usuarios;
    private final UnidadTrabajo unidadTrabajo;

    public CuentaServicio(CuentaRepositorio cuentas, UsuarioRepositorio usuarios, UnidadTrabajo unidadTrabajo) {
        this.cuentas = Validaciones.obligatorio(cuentas, "El repositorio de cuentas");
        this.usuarios = Validaciones.obligatorio(usuarios, "El repositorio de usuarios");
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
    }

    public Cuenta crear(String identificacionCliente, TipoCuenta tipo) {
        return unidadTrabajo.ejecutar(() -> {
            Cliente cliente = buscarCliente(identificacionCliente);
            if (cliente.obtenerEstado() != EstadoUsuario.ACTIVO) {
                throw new OperacionRechazadaException("El cliente debe estar activo para crear cuentas.");
            }
            String numero;
            do {
                numero = new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
            } while (cuentas.buscarPorNumero(numero).isPresent());
            Cuenta cuenta = new Cuenta(numero, tipo, cliente);
            cuentas.guardar(cuenta);
            return cuenta;
        });
    }

    public Cuenta consultar(String numero) {
        return cuentas.buscarPorNumero(numero)
                .orElseThrow(() -> new RegistroNoEncontradoException("La cuenta no existe."));
    }

    public Cuenta consultarPropia(String identificacionCliente, String numero) {
        Cuenta cuenta = consultar(numero);
        String identificacion = Validaciones.texto(identificacionCliente, "El cliente");
        if (!cuenta.obtenerCliente().obtenerIdentificacion().equals(identificacion)) {
            throw new OperacionRechazadaException("La cuenta no pertenece al cliente indicado.");
        }
        return cuenta;
    }

    public List<Cuenta> consultarPorCliente(String identificacionCliente) {
        return unidadTrabajo.ejecutar(() -> {
            Cliente cliente = buscarCliente(identificacionCliente);
            return cuentas.buscarPorCliente(cliente.obtenerIdentificacion());
        });
    }

    public BigDecimal consultarSaldo(String identificacionCliente, String numero) {
        return consultarPropia(identificacionCliente, numero).consultarSaldo();
    }

    public Cuenta validarHabilitada(String numero) {
        Cuenta cuenta = consultar(numero);
        cuenta.validarHabilitada();
        return cuenta;
    }

    public void bloquear(String numero) { cambiarEstado(numero, EstadoCuenta.BLOQUEADA); }

    public void desbloquear(String numero) {
        unidadTrabajo.ejecutar(() -> {
            Cuenta cuenta = consultar(numero);
            if (cuenta.obtenerEstado() != EstadoCuenta.BLOQUEADA) {
                throw new OperacionRechazadaException("Solo se puede desbloquear una cuenta bloqueada.");
            }
            cuenta.actualizarEstado(EstadoCuenta.ACTIVA);
            cuentas.actualizar(cuenta);
            return null;
        });
    }

    private void cambiarEstado(String numero, EstadoCuenta estado) {
        unidadTrabajo.ejecutar(() -> {
            Cuenta cuenta = consultar(numero);
            cuenta.actualizarEstado(estado);
            cuentas.actualizar(cuenta);
            return null;
        });
    }

    private Cliente buscarCliente(String identificacion) {
        return usuarios.buscarClientePorDocumento(identificacion)
                .orElseThrow(() -> new RegistroNoEncontradoException("El cliente no existe."));
    }
}
