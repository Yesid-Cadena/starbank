package com.starbank;

import com.starbank.configuracion.ConfiguracionMemoria;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.modelo.servicios.ClienteServicio;
import com.starbank.modelo.servicios.CuentaServicio;
import com.starbank.modelo.servicios.TransaccionServicio;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

public final class Main {
    private Main() {
    }

    public static void main(String[] argumentos) {
        ConfiguracionMemoria configuracion = new ConfiguracionMemoria();
        ClienteServicio clientes = configuracion.obtenerClientes();
        CuentaServicio cuentas = configuracion.obtenerCuentas();
        TransaccionServicio operaciones = configuracion.obtenerTransacciones();
        char[] claveFicticia = "Ejemplo-academico-21".toCharArray();
        try {
            clientes.registrar("DEMO-001", "Ana Ejemplo", "ana@example.com", "3000000001", claveFicticia);
            clientes.registrar("DEMO-002", "Luis Ejemplo", "luis@example.com", "3000000002", claveFicticia);
        } finally {
            Arrays.fill(claveFicticia, '\0');
        }
        Cuenta primera = cuentas.crear("DEMO-001", TipoCuenta.AHORROS);
        Cuenta segunda = cuentas.crear("DEMO-002", TipoCuenta.AHORROS);
        String origen = primera.obtenerNumeroCuenta();
        String destino = segunda.obtenerNumeroCuenta();
        operaciones.depositar("DEMO-001", origen, new BigDecimal("1000000"), "Depósito inicial simulado");
        operaciones.depositar("DEMO-002", destino, new BigDecimal("500000"), "Depósito inicial simulado");
        operaciones.retirar("DEMO-001", origen, new BigDecimal("100000"), "Retiro simulado");
        operaciones.transferir("DEMO-001", origen, destino, new BigDecimal("200000"), "Transferencia simulada");

        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));
        formato.setMaximumFractionDigits(2);
        System.out.println("StarBank — operaciones ficticias en memoria");
        for (Cuenta cuenta : new Cuenta[] {primera, segunda}) {
            String cliente = cuenta.obtenerCliente().obtenerIdentificacion();
            BigDecimal saldo = cuentas.consultarSaldo(cliente, cuenta.obtenerNumeroCuenta());
            if (saldo.compareTo(new BigDecimal("700000")) != 0) {
                throw new IllegalStateException("Saldo final inesperado.");
            }
            System.out.printf("%s: %s COP%n", cuenta.obtenerCliente().obtenerNombre(), formato.format(saldo));
            for (Transaccion transaccion : operaciones.historialCuenta(cliente, cuenta.obtenerNumeroCuenta())) {
                System.out.printf("  %s | %s | %s COP | %s | %s%n", transaccion.obtenerIdentificador(),
                        transaccion.obtenerTipo(), formato.format(transaccion.obtenerMonto()),
                        transaccion.obtenerEstado(), transaccion.obtenerFechaHora());
            }
        }

        BigDecimal saldoAnterior = cuentas.consultarSaldo("DEMO-001", origen);
        Transaccion retiroRechazado = operaciones.retirar("DEMO-001", origen,
                new BigDecimal("900000"), "Retiro superior al saldo");
        cuentas.bloquear(origen);
        Transaccion depositoRechazado = operaciones.depositar("DEMO-001", origen,
                new BigDecimal("1000"), "Depósito sobre cuenta bloqueada");
        if (retiroRechazado.obtenerEstado() != EstadoTransaccion.RECHAZADA
                || depositoRechazado.obtenerEstado() != EstadoTransaccion.RECHAZADA
                || saldoAnterior.compareTo(cuentas.consultarSaldo("DEMO-001", origen)) != 0
                || cuentas.consultarSaldo("DEMO-002", destino).compareTo(new BigDecimal("700000")) != 0) {
            throw new IllegalStateException("Los rechazos no conservaron la integridad.");
        }
        System.out.println(retiroRechazado.obtenerDescripcion());
        System.out.println(depositoRechazado.obtenerDescripcion());
        System.out.println("Verificado: ambos saldos permanecen en 700.000 COP; rechazos registrados.");
    }
}
