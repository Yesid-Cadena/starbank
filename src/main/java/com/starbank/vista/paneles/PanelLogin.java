package com.starbank.vista.paneles;

import com.starbank.controlador.LoginControlador;
import com.starbank.controlador.LoginVista;
import com.starbank.vista.componentes.BotonPrimario;
import com.starbank.vista.componentes.BotonSecundario;
import com.starbank.vista.componentes.CampoClave;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelLogin extends JPanel implements LoginVista {
    private final JTextField correo = new JTextField(24);
    private final CampoClave clave = new CampoClave();
    private final JLabel mensaje = new JLabel(" ");
    private final BotonPrimario btnLogin = new BotonPrimario("Iniciar sesión");
    private final BotonSecundario btnRegistro = new BotonSecundario("Registrarse");
    private final JProgressBar progreso = new JProgressBar();
    private LoginControlador controlador;

    public PanelLogin(Runnable irARegistro) {
        setLayout(new GridLayout(1,2));
        add(panelMarca());
        add(panelFormulario(irARegistro));
    }

    public void setControlador(LoginControlador controlador) { this.controlador = controlador; }

    private JPanel panelMarca() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Estilos.AZUL_OSCURO);
        JPanel box = new JPanel(); box.setOpaque(false); box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel nombre = new JLabel("StarBank"); nombre.setFont(new Font("SansSerif", Font.BOLD, 40)); nombre.setForeground(Color.WHITE); nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel slogan = new JLabel("Tu dinero, tus metas, nuestro compromiso"); slogan.setForeground(new Color(0xCBD5E1)); slogan.setFont(Estilos.FUENTE_NORMAL); slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(nombre); box.add(Box.createVerticalStrut(10)); box.add(slogan);
        p.add(box);
        return p;
    }

    private JPanel panelFormulario(Runnable irARegistro) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Estilos.FONDO);
        JPanel card = Estilos.tarjeta();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.fill = GridBagConstraints.HORIZONTAL; c.gridx=0; c.weightx=1;
        JLabel t = Estilos.subtitulo("Bienvenido"); c.gridy=0; card.add(t,c);
        c.gridy=1; card.add(new JLabel("Correo electrónico"),c);
        c.gridy=2; card.add(correo,c);
        c.gridy=3; card.add(new JLabel("Contraseña"),c);
        c.gridy=4; card.add(clave,c);
        progreso.setIndeterminate(true); progreso.setVisible(false); c.gridy=5; card.add(progreso,c);
        mensaje.setForeground(Estilos.ERROR); c.gridy=6; card.add(mensaje,c);
        c.gridy=7; card.add(btnLogin,c);
        c.gridy=8; card.add(btnRegistro,c);
        btnLogin.addActionListener(e -> ejecutarLogin());
        btnRegistro.addActionListener(e -> irARegistro.run());
        clave.getCampo().addActionListener(e -> ejecutarLogin());
        p.add(card);
        return p;
    }

    private void ejecutarLogin() {
        if (controlador == null) return;
        String c = correo.getText();
        char[] pass = clave.getPassword();
        btnLogin.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return controlador.iniciarSesion(c, pass); }
            @Override protected void done() { btnLogin.setEnabled(true); }
        }.execute();
    }

    @Override public void mostrarCargando(boolean cargando) { SwingUtilities.invokeLater(() -> { progreso.setVisible(cargando); btnLogin.setEnabled(!cargando); }); }
    @Override public void mostrarError(String m) { SwingUtilities.invokeLater(() -> { mensaje.setForeground(Estilos.ERROR); mensaje.setText(m); }); }
    @Override public void mostrarExito(String m) { SwingUtilities.invokeLater(() -> { mensaje.setForeground(Estilos.EXITO); mensaje.setText(m); }); }
    @Override public void limpiarClave() { SwingUtilities.invokeLater(() -> clave.limpiar()); }
}
