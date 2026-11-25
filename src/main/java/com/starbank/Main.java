package com.starbank;

import com.starbank.configuracion.ConexionPostgres;
import com.starbank.configuracion.ConfiguracionMemoria;
import com.starbank.configuracion.ConfiguracionPostgres;
import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.excepciones.AccesoDenegadoException;
import com.starbank.excepciones.AutenticacionException;
import com.starbank.excepciones.SesionInvalidaException;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.RegistroActividad;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        List<String> argumentos = args == null ? List.of() : Arrays.asList(args);
        boolean postgres = argumentos.contains("--postgres") || argumentos.contains("--modo=postgres");
        boolean memoria = argumentos.contains("--memoria") || argumentos.contains("--modo=memoria") || !postgres;
        boolean initDb = argumentos.contains("--init-db");
        boolean crearAdmin = argumentos.contains("--crear-admin");
        boolean menu = argumentos.contains("--menu");
        boolean demo = argumentos.contains("--demo") || (!postgres && !initDb && !crearAdmin && !menu);

        if (postgres) {
            ConfiguracionPostgres config = crearConfiguracionPostgres();
            if (initDb) {
                config.inicializarEsquema();
                System.out.println("Esquema PostgreSQL inicializado.");
            }
            if (crearAdmin) {
                crearAdministradorInteractivo(config);
            }
            if (menu) {
                ejecutarMenuSimple(config);
            } else if (demo) {
                System.out.println("Modo PostgreSQL: no se insertan datos ficticios automáticamente. Use --menu, --init-db o --crear-admin.");
            }
            return;
        }

        ConfiguracionMemoria config = new ConfiguracionMemoria();
        if (demo) ejecutarDemo(config);
        if (menu) ejecutarMenuSimple(config);
    }

    private static ConfiguracionPostgres crearConfiguracionPostgres() {
        ConfiguracionPostgres config = new ConfiguracionPostgres(ConexionPostgres.desdeEntorno());
        config.verificarConexion();
        return config;
    }

    private static void crearAdministradorInteractivo(ConfiguracionPostgres config) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print("Identificación admin: "); String id = scanner.nextLine().trim();
        System.out.print("Nombre: "); String nombre = scanner.nextLine().trim();
        System.out.print("Correo: "); String correo = scanner.nextLine().trim();
        System.out.print("Teléfono: "); String telefono = scanner.nextLine().trim();
        System.out.print("Contraseña: "); char[] clave = scanner.nextLine().toCharArray();
        config.crearAdministrador(id, nombre, correo, telefono, clave);
        System.out.println("Administrador creado.");
    }

    static void ejecutarDemo(ConfiguracionServicios config) {
        char[] claveCliente1 = "ClienteUno-21".toCharArray();
        char[] claveCliente2 = "ClienteDos-21".toCharArray();
        char[] claveAdmin = "AdminStar-21".toCharArray();
        char[] claveMala = "incorrecta-21".toCharArray();

        config.obtenerClientes().registrar("C001", "Ana Cliente", "ana@starbank.test", "3000000001", claveCliente1);
        config.obtenerClientes().registrar("C002", "Luis Cliente", "luis@starbank.test", "3000000002", claveCliente2);
        System.out.println("1. Clientes registrados.");

        config.crearAdministrador("A001", "Admin StarBank", "admin@starbank.test", "3000000099", claveAdmin);
        System.out.println("2. Administrador creado internamente.");

        try {
            config.obtenerAutenticacion().iniciarSesion("ana@starbank.test", claveMala);
            throw new IllegalStateException("Debió fallar la autenticación.");
        } catch (AutenticacionException e) {
            System.out.println("3. Autenticación fallida rechazada: " + e.getMessage());
        }

        Sesion sesionAdmin = config.obtenerAutenticacion().iniciarSesion("admin@starbank.test", "AdminStar-21".toCharArray());
        System.out.println("4. Administrador autenticado.");

        Cuenta cuentaAna = config.obtenerCuentas().crear(sesionAdmin, "C001", TipoCuenta.AHORROS);
        Cuenta cuentaLuis = config.obtenerCuentas().crear(sesionAdmin, "C002", TipoCuenta.AHORROS);
        System.out.println("5. Cuentas creadas para ambos clientes.");

        Sesion sesionAna = config.obtenerAutenticacion().iniciarSesion("ana@starbank.test", "ClienteUno-21".toCharArray());
        System.out.println("6. Cliente Ana autenticado.");

        config.obtenerTransacciones().depositar(sesionAna, cuentaAna.obtenerNumeroCuenta(), new BigDecimal("1000000"), "Depósito inicial");
        config.obtenerTransacciones().retirar(sesionAna, cuentaAna.obtenerNumeroCuenta(), new BigDecimal("100000"), "Retiro autorizado");
        System.out.println("7. Depósito y retiro autorizados ejecutados.");

        config.obtenerTransacciones().transferir(sesionAna, cuentaAna.obtenerNumeroCuenta(), cuentaLuis.obtenerNumeroCuenta(), new BigDecimal("200000"), "Transferencia a Luis");
        System.out.println("8. Transferencia autorizada ejecutada.");

        BigDecimal saldoAnaAntes = config.obtenerCuentas().consultarSaldo(sesionAna, cuentaAna.obtenerNumeroCuenta());
        BigDecimal saldoLuisAntes = config.obtenerCuentas().consultar(sesionAdmin, cuentaLuis.obtenerNumeroCuenta()).consultarSaldo();

        try { config.obtenerCuentas().consultarSaldo(sesionAna, cuentaLuis.obtenerNumeroCuenta()); throw new IllegalStateException("Debió rechazar consulta de cuenta ajena."); }
        catch (RuntimeException e) { System.out.println("9. Consulta de saldo ajeno rechazada."); }

        try { config.obtenerTransacciones().retirar(sesionAna, cuentaLuis.obtenerNumeroCuenta(), new BigDecimal("10000"), "Retiro no autorizado"); throw new IllegalStateException("Debió rechazar retiro de cuenta ajena."); }
        catch (RuntimeException e) { System.out.println("10. Retiro de cuenta ajena rechazado."); }

        try { config.obtenerClientes().listarClientes(sesionAna); throw new IllegalStateException("Debió rechazar función administrativa."); }
        catch (AccesoDenegadoException e) { System.out.println("11. Función administrativa rechazada para cliente."); }

        config.obtenerCuentas().bloquear(sesionAdmin, cuentaAna.obtenerNumeroCuenta());
        System.out.println("12. Cuenta bloqueada por administrador.");

        Transaccion rechazada = config.obtenerTransacciones().depositar(sesionAna, cuentaAna.obtenerNumeroCuenta(), new BigDecimal("1000"), "Depósito en cuenta bloqueada");
        if ("RECHAZADA".equals(rechazada.obtenerEstado().name())) System.out.println("13. Operación sobre cuenta bloqueada rechazada.");
        else throw new IllegalStateException("La operación sobre cuenta bloqueada debió ser rechazada.");

        config.obtenerAutenticacion().cerrarSesion(sesionAna.obtenerIdentificador());
        System.out.println("14. Sesión de Ana cerrada.");

        try { config.obtenerCuentas().consultarPorCliente(sesionAna); throw new IllegalStateException("Debió rechazar sesión cerrada."); }
        catch (SesionInvalidaException e) { System.out.println("15. Reutilización de sesión cerrada rechazada."); }

        List<RegistroActividad> registros = config.obtenerAuditoria().listar(sesionAdmin, config.obtenerAutorizacion());
        System.out.println("16. Auditoría consultada por administrador: " + registros.size() + " registro(s).");

        BigDecimal saldoAnaDespues = config.obtenerCuentas().consultar(sesionAdmin, cuentaAna.obtenerNumeroCuenta()).consultarSaldo();
        BigDecimal saldoLuisDespues = config.obtenerCuentas().consultar(sesionAdmin, cuentaLuis.obtenerNumeroCuenta()).consultarSaldo();
        if (saldoAnaAntes.compareTo(saldoAnaDespues) != 0 || saldoLuisAntes.compareTo(saldoLuisDespues) != 0) {
            throw new IllegalStateException("Una operación rechazada modificó saldos.");
        }
        System.out.println("Verificación: operaciones rechazadas no modificaron saldos.");
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        System.out.println("Saldo Ana: " + formato.format(saldoAnaDespues));
        System.out.println("Saldo Luis: " + formato.format(saldoLuisDespues));
    }

    private static void ejecutarMenuSimple(ConfiguracionServicios config) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        Sesion sesion = null;
        System.out.println("\n=== StarBank - Interfaz básica de prueba ===");
        while (true) {
            System.out.println("\n1) Iniciar sesión  2) Ver perfil  3) Mis cuentas  4) Depositar  5) Retirar  6) Transferir  7) Historial  8) Cerrar sesión  0) Salir");
            System.out.print("> ");
            String opcion = scanner.nextLine().trim();
            try {
                if ("0".equals(opcion)) { System.out.println("Saliendo."); return; }
                switch (opcion) {
                    case "1" -> { System.out.print("Correo: "); String correo = scanner.nextLine().trim(); System.out.print("Contraseña: "); char[] clave = scanner.nextLine().toCharArray(); sesion = config.obtenerAutenticacion().iniciarSesion(correo, clave); System.out.println("Sesión iniciada."); }
                    case "2" -> { exigirSesion(sesion); System.out.println(config.obtenerClientes().consultarPerfil(sesion).obtenerNombre()); }
                    case "3" -> { exigirSesion(sesion); for (Cuenta c : config.obtenerCuentas().consultarPorCliente(sesion)) System.out.println(c.obtenerNumeroCuenta() + " | " + c.obtenerTipoCuenta() + " | " + c.consultarSaldo() + " | " + c.obtenerEstado()); }
                    case "4" -> { exigirSesion(sesion); System.out.print("Cuenta: "); String cuenta = scanner.nextLine().trim(); System.out.print("Monto: "); BigDecimal monto = new BigDecimal(scanner.nextLine().trim()); System.out.print("Descripción: "); String desc = scanner.nextLine().trim(); System.out.println(config.obtenerTransacciones().depositar(sesion, cuenta, monto, desc).obtenerEstado()); }
                    case "5" -> { exigirSesion(sesion); System.out.print("Cuenta: "); String cuenta = scanner.nextLine().trim(); System.out.print("Monto: "); BigDecimal monto = new BigDecimal(scanner.nextLine().trim()); System.out.print("Descripción: "); String desc = scanner.nextLine().trim(); System.out.println(config.obtenerTransacciones().retirar(sesion, cuenta, monto, desc).obtenerEstado()); }
                    case "6" -> { exigirSesion(sesion); System.out.print("Origen: "); String origen = scanner.nextLine().trim(); System.out.print("Destino: "); String destino = scanner.nextLine().trim(); System.out.print("Monto: "); BigDecimal monto = new BigDecimal(scanner.nextLine().trim()); System.out.print("Descripción: "); String desc = scanner.nextLine().trim(); System.out.println(config.obtenerTransacciones().transferir(sesion, origen, destino, monto, desc).obtenerEstado()); }
                    case "7" -> { exigirSesion(sesion); System.out.print("Cuenta: "); String cuenta = scanner.nextLine().trim(); for (Transaccion t : config.obtenerTransacciones().historialCuenta(sesion, cuenta)) System.out.println(t.obtenerFechaHora() + " | " + t.obtenerTipo() + " | " + t.obtenerMonto() + " | " + t.obtenerEstado()); }
                    case "8" -> { exigirSesion(sesion); config.obtenerAutenticacion().cerrarSesion(sesion.obtenerIdentificador()); sesion = null; System.out.println("Sesión cerrada."); }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) { System.out.println("Error: " + e.getMessage()); }
        }
    }

    private static void exigirSesion(Sesion sesion) {
        if (sesion == null) throw new SesionInvalidaException("Debe iniciar sesión.");
    }
}
