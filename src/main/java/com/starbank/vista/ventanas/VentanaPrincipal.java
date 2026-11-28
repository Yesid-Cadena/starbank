package com.starbank.vista.ventanas;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.LoginControlador;
import com.starbank.controlador.RegistroControlador;
import com.starbank.controlador.SesionControlador;
import com.starbank.modelo.enumeraciones.Rol;
import com.starbank.vista.estilos.Estilos;
import com.starbank.vista.paneles.*;
import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class VentanaPrincipal extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final Supplier<ConfiguracionServicios> proveedorConfig;
    private ConfiguracionServicios config;
    private SesionControlador sesionControlador;
    private final PanelCarga panelCarga = new PanelCarga();
    private PanelLogin panelLogin;
    private PanelRegistro panelRegistro;

    public VentanaPrincipal(Supplier<ConfiguracionServicios> proveedorConfig) {
        super("StarBank - Tu dinero, tus metas, nuestro compromiso");
        this.proveedorConfig = proveedorConfig;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { confirmarSalida(); }
        });
        root.add(panelCarga, "carga");
        setContentPane(root);
        cards.show(root, "carga");
    }

    public void iniciar() {
        setVisible(true);
        cargarServicios();
    }

    private void cargarServicios() {
        panelCarga.setEstado("Inicializando servicios...");
        new SwingWorker<ConfiguracionServicios, Void>() {
            @Override protected ConfiguracionServicios doInBackground() { return proveedorConfig.get(); }
            @Override protected void done() {
                try {
                    config = get();
                    panelCarga.detener();
                    construirAplicacion();
                    cards.show(root, "login");
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    int op = JOptionPane.showOptionDialog(VentanaPrincipal.this,
                            "No se pudo iniciar la aplicación:\n" + causa.getMessage(),
                            "Error de inicialización", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, new Object[]{"Reintentar", "Cerrar"}, "Reintentar");
                    if (op == 0) cargarServicios(); else dispose();
                }
            }
        }.execute();
    }

    private void construirAplicacion() {
        sesionControlador = new SesionControlador(config);
        panelLogin = new PanelLogin(() -> cards.show(root, "registro"));
        panelRegistro = new PanelRegistro();
        LoginControlador loginControlador = new LoginControlador(config, sesionControlador, panelLogin, this::navegarPorRol);
        RegistroControlador registroControlador = new RegistroControlador(config, panelRegistro, () -> cards.show(root, "login"));
        panelLogin.setControlador(loginControlador);
        panelRegistro.setControlador(registroControlador);
        PanelPrincipalCliente cliente = new PanelPrincipalCliente(config, sesionControlador, this::volverLogin);
        PanelPrincipalAdmin admin = new PanelPrincipalAdmin(sesionControlador, this::volverLogin);
        root.add(panelLogin, "login");
        root.add(panelRegistro, "registro");
        root.add(cliente, "cliente");
        root.add(admin, "admin");
    }

    private void navegarPorRol(Rol rol) {
        if (rol == Rol.ADMINISTRADOR) cards.show(root, "admin");
        else cards.show(root, "cliente");
    }

    private void volverLogin() {
        cards.show(root, "login");
    }

    private void confirmarSalida() {
        int op = JOptionPane.showConfirmDialog(this, "¿Desea cerrar StarBank?", "Confirmar salida", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) dispose();
    }
}
