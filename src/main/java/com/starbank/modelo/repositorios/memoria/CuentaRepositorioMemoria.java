package com.starbank.modelo.repositorios.memoria;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.excepciones.ValidacionException;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.repositorios.CuentaRepositorio;
import com.starbank.utilidades.Validaciones;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public final class CuentaRepositorioMemoria implements CuentaRepositorio {
    private final AlmacenMemoria almacen;

    public CuentaRepositorioMemoria(AlmacenMemoria almacen) {
        this.almacen = Validaciones.obligatorio(almacen, "El almacén");
    }

    @Override
    public void guardar(Cuenta cuenta) {
        Validaciones.obligatorio(cuenta, "La cuenta");
        synchronized (almacen) {
            if (almacen.cuentas().containsKey(cuenta.obtenerNumeroCuenta())) {
                throw new RegistroDuplicadoException("El número de cuenta ya está registrado.");
            }
            if (cuenta.consultarSaldo().compareTo(BigDecimal.ZERO) != 0) {
                throw new ValidacionException("Una cuenta nueva debe tener saldo cero.");
            }
            almacen.cuentas().put(cuenta.obtenerNumeroCuenta(), cuenta.copiar());
        }
    }

    @Override
    public void actualizar(Cuenta cuenta) {
        Validaciones.obligatorio(cuenta, "La cuenta");
        synchronized (almacen) {
            Cuenta anterior = almacen.cuentas().get(cuenta.obtenerNumeroCuenta());
            if (anterior == null) {
                throw new RegistroNoEncontradoException("La cuenta no existe.");
            }
            if (!anterior.obtenerCliente().obtenerIdentificacion().equals(cuenta.obtenerCliente().obtenerIdentificacion())
                    || anterior.obtenerTipoCuenta() != cuenta.obtenerTipoCuenta()
                    || !anterior.obtenerFechaCreacion().equals(cuenta.obtenerFechaCreacion())) {
                throw new ValidacionException("No se puede reemplazar la identidad de una cuenta.");
            }
            almacen.cuentas().put(cuenta.obtenerNumeroCuenta(), cuenta.copiar());
        }
    }

    @Override
    public Optional<Cuenta> buscarPorNumero(String numero) {
        String clave = Validaciones.texto(numero, "El número de cuenta");
        synchronized (almacen) {
            return Optional.ofNullable(almacen.cuentas().get(clave)).map(Cuenta::copiar);
        }
    }

    @Override
    public List<Cuenta> buscarPorCliente(String identificacion) {
        String buscada = Validaciones.texto(identificacion, "La identificación");
        return listar().stream().filter(cuenta -> cuenta.obtenerCliente().obtenerIdentificacion().equals(buscada)).toList();
    }

    @Override
    public List<Cuenta> listar() {
        synchronized (almacen) {
            return almacen.cuentas().values().stream().map(Cuenta::copiar).toList();
        }
    }
}
