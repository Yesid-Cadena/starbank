package com.starbank.vista.ventanas;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.RegistroActividad;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class VentanaPruebas extends JFrame {
    private final ConfiguracionServicios config;
    private Sesion sesion;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JLabel lblUsuario = new JLabel(" ");
    private final DefaultTableModel modeloCuentas = new DefaultTableModel(new String[]{"Número","Tipo","Saldo","Estado"}, 0);
    private final DefaultTableModel modeloHistorial = new DefaultTableModel(new String[]{"Fecha","Tipo","Monto","Estado","Descripción"}, 0);
    private final DefaultTableModel modeloAuditoria = new DefaultTableModel(new String[]{"Fecha","Usuario","Tipo","Resultado","Descripción"}, 0);

    public VentanaPruebas(ConfiguracionServicios config) {
        super("StarBank - GUI de Pruebas");
        this.config = config;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        root.add(panelLogin(), "login");
        root.add(panelApp(), "app");
        setContentPane(root);
        cards.show(root, "login");
    }

    private JPanel panelLogin() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5,5,5,5); c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=0; c.gridy=0; p.add(new JLabel("Correo:"), c); c.gridx=1; p.add(txtCorreo, c);
        c.gridx=0; c.gridy=1; p.add(new JLabel("Contraseña:"), c); c.gridx=1; p.add(txtClave, c);
        JButton btnLogin = new JButton("Iniciar sesión");
        JButton btnRegistrar = new JButton("Registrar cliente demo");
        c.gridx=0; c.gridy=2; c.gridwidth=2; p.add(btnLogin, c);
        c.gridy=3; p.add(btnRegistrar, c);
        btnLogin.addActionListener(e -> login());
        btnRegistrar.addActionListener(e -> registrarDemo());
        return p;
    }

    private JPanel panelApp() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(lblUsuario);
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnLogout = new JButton("Cerrar sesión");
        top.add(btnRefrescar); top.add(btnLogout);
        p.add(top, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Cuentas", panelCuentas());
        tabs.add("Operaciones", panelOperaciones());
        tabs.add("Historial", panelHistorial());
        tabs.add("Admin", panelAdmin());
        p.add(tabs, BorderLayout.CENTER);
        btnRefrescar.addActionListener(e -> refrescar());
        btnLogout.addActionListener(e -> logout());
        return p;
    }

    private JPanel panelCuentas() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(new JTable(modeloCuentas)), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelOperaciones() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets=new Insets(4,4,4,4); c.fill=GridBagConstraints.HORIZONTAL;
        JTextField cuenta = new JTextField(15), destino = new JTextField(15), monto = new JTextField(10), desc = new JTextField(15);
        JButton dep = new JButton("Depositar"), ret = new JButton("Retirar"), tra = new JButton("Transferir");
        int y=0;
        c.gridx=0;c.gridy=y; p.add(new JLabel("Cuenta:"),c); c.gridx=1; p.add(cuenta,c);
        c.gridx=2; p.add(new JLabel("Destino:"),c); c.gridx=3; p.add(destino,c); y++;
        c.gridx=0;c.gridy=y; p.add(new JLabel("Monto:"),c); c.gridx=1; p.add(monto,c);
        c.gridx=2; p.add(new JLabel("Descripción:"),c); c.gridx=3; p.add(desc,c); y++;
        c.gridx=0;c.gridy=y; p.add(dep,c); c.gridx=1; p.add(ret,c); c.gridx=2; p.add(tra,c);
        dep.addActionListener(e -> ejecutar(() -> config.obtenerTransacciones().depositar(sesion, cuenta.getText().trim(), new BigDecimal(monto.getText().trim()), desc.getText().trim())));
        ret.addActionListener(e -> ejecutar(() -> config.obtenerTransacciones().retirar(sesion, cuenta.getText().trim(), new BigDecimal(monto.getText().trim()), desc.getText().trim())));
        tra.addActionListener(e -> ejecutar(() -> config.obtenerTransacciones().transferir(sesion, cuenta.getText().trim(), destino.getText().trim(), new BigDecimal(monto.getText().trim()), desc.getText().trim())));
        return p;
    }

    private JPanel panelHistorial() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField cuenta = new JTextField(15);
        JButton btn = new JButton("Ver historial");
        top.add(new JLabel("Cuenta:")); top.add(cuenta); top.add(btn);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(modeloHistorial)), BorderLayout.CENTER);
        btn.addActionListener(e -> ejecutar(() -> {
            modeloHistorial.setRowCount(0);
            for (Transaccion t : config.obtenerTransacciones().historialCuenta(sesion, cuenta.getText().trim())) {
                modeloHistorial.addRow(new Object[]{t.obtenerFechaHora(), t.obtenerTipo(), t.obtenerMonto(), t.obtenerEstado(), t.obtenerDescripcion()});
            }
            return null;
        }));
        return p;
    }

    private JPanel panelAdmin() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField idCliente = new JTextField(8), tipo = new JTextField(8), numCuenta = new JTextField(12);
        JButton crearCuenta = new JButton("Crear cuenta"), bloquear = new JButton("Bloquear"), desbloquear = new JButton("Desbloquear"), auditoria = new JButton("Ver auditoría");
        top.add(new JLabel("Cliente:")); top.add(idCliente); top.add(new JLabel("Tipo AHORROS/CORRIENTE:")); top.add(tipo);
        top.add(new JLabel("Cuenta:")); top.add(numCuenta);
        top.add(crearCuenta); top.add(bloquear); top.add(desbloquear); top.add(auditoria);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(modeloAuditoria)), BorderLayout.CENTER);
        crearCuenta.addActionListener(e -> ejecutar(() -> config.obtenerCuentas().crear(sesion, idCliente.getText().trim(), TipoCuenta.valueOf(tipo.getText().trim().toUpperCase()))));
        bloquear.addActionListener(e -> ejecutar(() -> { config.obtenerCuentas().bloquear(sesion, numCuenta.getText().trim()); return "ok"; }));
        desbloquear.addActionListener(e -> ejecutar(() -> { config.obtenerCuentas().desbloquear(sesion, numCuenta.getText().trim()); return "ok"; }));
        auditoria.addActionListener(e -> ejecutar(() -> {
            modeloAuditoria.setRowCount(0);
            for (RegistroActividad r : config.obtenerAuditoria().listar(sesion, config.obtenerAutorizacion())) {
                modeloAuditoria.addRow(new Object[]{r.obtenerFechaHora(), r.obtenerIdentificacionUsuario(), r.obtenerTipoActividad(), r.obtenerResultado(), r.obtenerDescripcion()});
            }
            return null;
        }));
        return p;
    }

    private void login() {
        ejecutar(() -> {
            sesion = config.obtenerAutenticacion().iniciarSesion(txtCorreo.getText().trim(), txtClave.getPassword());
            lblUsuario.setText("Usuario: " + sesion.obtenerIdentificacionUsuario());
            cards.show(root, "app");
            refrescar();
            return null;
        });
    }

    private void registrarDemo() {
        ejecutar(() -> {
            config.obtenerClientes().registrar("GUI001", "Cliente GUI", "gui@starbank.test", "3000000001", "GuiClave-21".toCharArray());
            try { config.crearAdministrador("GUIADM", "Admin GUI", "admingui@starbank.test", "3000000000", "AdminGui-21".toCharArray()); } catch (Exception ignorada) {}
            JOptionPane.showMessageDialog(this, "Creado: gui@starbank.test / GuiClave-21 (cliente) y admingui@starbank.test / AdminGui-21 (admin si no existía)");
            return null;
        });
    }

    private void refrescar() {
        if (sesion == null) return;
        ejecutar(() -> {
            modeloCuentas.setRowCount(0);
            try {
                List<Cuenta> cuentas = config.obtenerCuentas().consultarPorCliente(sesion);
                for (Cuenta cta : cuentas) modeloCuentas.addRow(new Object[]{cta.obtenerNumeroCuenta(), cta.obtenerTipoCuenta(), cta.consultarSaldo(), cta.obtenerEstado()});
            } catch (Exception e) { /* admin puede no tener cuentas propias */ }
            return null;
        });
    }

    private void logout() {
        if (sesion != null) {
            try { config.obtenerAutenticacion().cerrarSesion(sesion.obtenerIdentificador()); } catch (Exception ignorada) {}
        }
        sesion = null;
        txtClave.setText("");
        cards.show(root, "login");
    }

    private <T> void ejecutar(Accion<T> accion) {
        try {
            T r = accion.ejecutar();
            if (r != null) JOptionPane.showMessageDialog(this, String.valueOf(r));
            if (sesion != null) refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FunctionalInterface interface Accion<T> { T ejecutar() throws Exception; }
}
