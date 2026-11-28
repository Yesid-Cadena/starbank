package com.starbank.vista.paneles;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.SesionControlador;
import com.starbank.vista.componentes.BotonMenu;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class PanelPrincipalCliente extends JPanel {
    private final CardLayout cards = new CardLayout();
    private final JPanel contenido = new JPanel(cards);
    private final ConfiguracionServicios config;
    private final SesionControlador sesion;
    private final Runnable alCerrarSesion;
    private final Map<String, BotonMenu> botones = new LinkedHashMap<>();
    private final Map<String, Component> paneles = new LinkedHashMap<>();
    private final JLabel lblUsuario = new JLabel(" ");
    private final JPanel sidebar = new JPanel(new BorderLayout());
    private final JPanel topBar = new JPanel(new BorderLayout());
    private final JButton btnMenu = new JButton("☰");
    private final JPopupMenu menuPopup = new JPopupMenu();
    private boolean modoEstrecho = false;

    public PanelPrincipalCliente(ConfiguracionServicios config, SesionControlador sesion, Runnable alCerrarSesion) {
        this.config = config; this.sesion = sesion; this.alCerrarSesion = alCerrarSesion;
        setLayout(new BorderLayout());
        construirSidebar();
        construirTopBar();
        add(sidebar, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        registrar("inicio", new PanelInicioCliente(config, sesion, this::mostrar));
        registrar("cuentas", new PanelCuentasCliente(config, sesion));
        registrar("depositar", new PanelDepositoCliente(config, sesion));
        registrar("retirar", new PanelRetiroCliente(config, sesion));
        registrar("transferir", new PanelTransferenciaCliente(config, sesion));
        registrar("movimientos", new PanelMovimientosCliente(config, sesion));
        registrar("perfil", new PanelPerfilCliente(config, sesion, alCerrarSesion));
        registrar("atencion", new PanelPendiente("Atención al cliente"));
        seleccionar("inicio");
        addComponentListener(new ComponentAdapter() { @Override public void componentResized(ComponentEvent e) { actualizarResponsive(); } });
    }

    private void construirSidebar() {
        sidebar.setBackground(Estilos.AZUL_OSCURO);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 16, 12));
        JPanel top = new JPanel(); top.setOpaque(false); top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("StarBank"); titulo.setForeground(Color.WHITE); titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Banca digital"); sub.setForeground(new Color(0x94A3B8)); sub.setFont(Estilos.FUENTE_PEQUENA); sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(titulo); top.add(Box.createVerticalStrut(2)); top.add(sub); top.add(Box.createVerticalStrut(28));
        agregar(top, "inicio", "Inicio"); agregar(top, "cuentas", "Mis cuentas"); agregar(top, "depositar", "Depositar"); agregar(top, "retirar", "Retirar");
        agregar(top, "transferir", "Transferir"); agregar(top, "movimientos", "Movimientos"); agregar(top, "perfil", "Mi perfil"); agregar(top, "atencion", "Atención al cliente");
        JPanel bottom = new JPanel(); bottom.setOpaque(false); bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        lblUsuario.setForeground(new Color(0xCBD5E1)); lblUsuario.setFont(Estilos.FUENTE_PEQUENA); lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(lblUsuario); bottom.add(Box.createVerticalStrut(10));
        JButton salir = new JButton("Cerrar sesión");
        salir.setAlignmentX(Component.LEFT_ALIGNMENT); salir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        salir.setFocusPainted(false); salir.setContentAreaFilled(false); salir.setBorderPainted(false);
        salir.setForeground(new Color(0xFCA5A5)); salir.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD));
        salir.setHorizontalAlignment(SwingConstants.LEFT); salir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        salir.addActionListener(e -> { sesion.cerrar(); alCerrarSesion.run(); });
        bottom.add(salir);
        sidebar.add(top, BorderLayout.NORTH); sidebar.add(bottom, BorderLayout.SOUTH);
        menuPopup.addSeparator();
        JMenuItem salirMi = new JMenuItem("Cerrar sesión");
        salirMi.addActionListener(e -> { sesion.cerrar(); alCerrarSesion.run(); });
        menuPopup.add(salirMi);
    }

    private void construirTopBar() {
        topBar.setBackground(Estilos.AZUL_OSCURO); topBar.setBorder(new EmptyBorder(10,12,10,12));
        btnMenu.setFocusPainted(false); btnMenu.setContentAreaFilled(false); btnMenu.setBorderPainted(false); btnMenu.setForeground(Color.WHITE); btnMenu.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMenu.addActionListener(e -> menuPopup.show(btnMenu, 0, btnMenu.getHeight()));
        JLabel t = new JLabel("StarBank"); t.setForeground(Color.WHITE); t.setFont(new Font("SansSerif", Font.BOLD, 20));
        topBar.add(btnMenu, BorderLayout.WEST); topBar.add(t, BorderLayout.CENTER);
        topBar.setVisible(false);
    }

    private void agregar(JPanel top, String clave, String texto) {
        BotonMenu b = new BotonMenu(texto);
        b.addActionListener(e -> mostrar(clave));
        botones.put(clave, b);
        top.add(b); top.add(Box.createVerticalStrut(4));
        JMenuItem mi = new JMenuItem(texto); mi.addActionListener(e -> mostrar(clave)); menuPopup.add(mi);
    }

    private void registrar(String clave, Component c) { paneles.put(clave, c); contenido.add(c, clave); }

    private void actualizarResponsive() {
        boolean estrecho = getWidth() < 900;
        if (estrecho != modoEstrecho) {
            modoEstrecho = estrecho;
            sidebar.setVisible(!estrecho);
            topBar.setVisible(estrecho);
            if (estrecho) add(topBar, BorderLayout.NORTH); else remove(topBar);
            revalidate(); repaint();
        }
        sidebar.setPreferredSize(new Dimension(getWidth() < 1100 ? 200 : 230, 0));
        sidebar.revalidate();
    }

    public void mostrar(String clave) {
        seleccionar(clave);
        cards.show(contenido, clave);
        Component c = paneles.get(clave);
        if (c instanceof Refrescable r) r.refrescar();
        actualizarUsuario();
    }

    private void seleccionar(String clave) { botones.forEach((k, b) -> b.setSeleccionado(k.equals(clave))); actualizarUsuario(); }

    private void actualizarUsuario() {
        try { lblUsuario.setText(sesion.usuarioActual().obtenerNombre()); }
        catch (Exception e) { lblUsuario.setText(" "); }
    }

    public interface Refrescable { void refrescar(); }
}
