package com.starbank.vista.paneles;

import com.starbank.controlador.LoginControlador;
import com.starbank.controlador.LoginVista;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PanelLogin extends JPanel implements LoginVista {
    private final CampoTexto correo = new CampoTexto("correo@ejemplo.com");
    private final CampoClave clave = new CampoClave("Ingresa tu contraseña");
    private final JLabel mensaje = new JLabel(" ");
    private final BotonPrimario btnLogin = new BotonPrimario("Iniciar sesión");
    private final JProgressBar progreso = new JProgressBar();
    private final PanelMarca marca = new PanelMarca(PanelMarca.Variante.LOGIN);
    private final JPanel contenedorDerecho = new JPanel(new GridBagLayout());
    private final Runnable irARegistro;
    private LoginControlador controlador;
    private boolean modoEscritorio = true;

    public PanelLogin(Runnable irARegistro) {
        this.irARegistro = irARegistro;
        setLayout(new GridBagLayout());
        btnLogin.addActionListener(e -> ejecutarLogin());
        clave.getCampo().addActionListener(e -> ejecutarLogin());
        construirEscritorio();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { actualizarModo(); }
        });
    }

    public void setControlador(LoginControlador controlador) { this.controlador = controlador; }

    private void construirEscritorio() {
        removeAll();
        setLayout(new GridBagLayout());
        contenedorDerecho.removeAll();
        contenedorDerecho.setBackground(Estilos.FONDO);
        contenedorDerecho.setLayout(new GridBagLayout());
        contenedorDerecho.add(crearFormulario(450), new GridBagConstraints());

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH; gc.weighty = 1; gc.gridy = 0;
        JPanel m = marca; m.setPreferredSize(new Dimension(0,0));
        contenedorDerecho.setPreferredSize(new Dimension(0,0));
        gc.gridx = 0; gc.weightx = 0.44; add(m, gc);
        gc.gridx = 1; gc.weightx = 0.56; add(contenedorDerecho, gc);
        modoEscritorio = true;
        revalidate(); repaint();
    }

    private void construirEstrecho() {
        removeAll();
        setLayout(new BorderLayout());
        JPanel header = crearHeaderCompacto();
        contenedorDerecho.removeAll();
        contenedorDerecho.setBackground(Estilos.FONDO);
        contenedorDerecho.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.gridy=0; c.weightx=1; c.weighty=1; c.fill=GridBagConstraints.HORIZONTAL; c.anchor=GridBagConstraints.NORTH;
        c.insets = new Insets(20,20,20,20);
        contenedorDerecho.add(crearFormulario(Integer.MAX_VALUE), c);
        JScrollPane sp = new JScrollPane(contenedorDerecho);
        sp.setBorder(null); sp.getViewport().setBackground(Estilos.FONDO);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(header, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        modoEscritorio = false;
        revalidate(); repaint();
    }

    private JPanel crearHeaderCompacto() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,Estilos.AZUL_OSCURO,getWidth(),0,Estilos.AZUL_SECUNDARIO));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        h.setBorder(new EmptyBorder(14, 18, 14, 18));
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); izq.setOpaque(false);
        izq.add(new PanelMarca.LogoStar(24));
        JLabel n = new JLabel("StarBank"); n.setForeground(Color.WHITE); n.setFont(new Font("SansSerif", Font.BOLD, 22));
        izq.add(n);
        h.add(izq, BorderLayout.WEST);
        return h;
    }

    private void actualizarModo() {
        boolean debeEscritorio = getWidth() >= 700;
        if (debeEscritorio != modoEscritorio) {
            if (debeEscritorio) construirEscritorio(); else construirEstrecho();
        }
    }

    private JPanel crearFormulario(int anchoMax) {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        if (anchoMax != Integer.MAX_VALUE) {
            form.setPreferredSize(new Dimension(anchoMax, 520));
            form.setMaximumSize(new Dimension(480, 700));
        } else {
            form.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        JLabel titulo = new JLabel("Bienvenido");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 36));
        titulo.setForeground(Estilos.TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subt = new JLabel("Ingresa a tu cuenta de StarBank");
        subt.setFont(Estilos.FUENTE_NORMAL);
        subt.setForeground(Estilos.TEXTO_SECUNDARIO);
        subt.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(titulo); form.add(Box.createVerticalStrut(8)); form.add(subt); form.add(Box.createVerticalStrut(36));
        form.add(etiqueta("Correo electrónico")); form.add(Box.createVerticalStrut(8));
        correo.setAlignmentX(Component.LEFT_ALIGNMENT); correo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); form.add(correo); form.add(Box.createVerticalStrut(18));
        form.add(etiqueta("Contraseña")); form.add(Box.createVerticalStrut(8));
        clave.setAlignmentX(Component.LEFT_ALIGNMENT); clave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); form.add(clave); form.add(Box.createVerticalStrut(14));

        progreso.setIndeterminate(true); progreso.setVisible(false); progreso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6)); progreso.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(progreso); form.add(Box.createVerticalStrut(8));
        mensaje.setForeground(Estilos.ERROR); mensaje.setFont(Estilos.FUENTE_PEQUENA); mensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(mensaje); form.add(Box.createVerticalStrut(18));

        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT); btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        form.add(btnLogin); form.add(Box.createVerticalStrut(24));

        JPanel registro = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        registro.setOpaque(false); registro.setAlignmentX(Component.LEFT_ALIGNMENT); registro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel noCuenta = new JLabel("¿No tienes una cuenta?"); noCuenta.setForeground(Estilos.TEXTO_SECUNDARIO); noCuenta.setFont(Estilos.FUENTE_NORMAL);
        BotonEnlace link = new BotonEnlace("Registrarse"); link.addActionListener(e -> irARegistro.run());
        registro.add(noCuenta); registro.add(link);
        form.add(registro);
        return form;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD));
        l.setForeground(Estilos.TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void ejecutarLogin() {
        if (controlador == null) return;
        mensaje.setText(" ");
        correo.setError(false); clave.setError(false);
        String c = correo.getText();
        char[] pass = clave.getPassword();
        btnLogin.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return controlador.iniciarSesion(c, pass); }
            @Override protected void done() { btnLogin.setEnabled(true); }
        }.execute();
    }

    @Override public void mostrarCargando(boolean cargando) { SwingUtilities.invokeLater(() -> { progreso.setVisible(cargando); btnLogin.setEnabled(!cargando); }); }
    @Override public void mostrarError(String m) { SwingUtilities.invokeLater(() -> { mensaje.setForeground(Estilos.ERROR); mensaje.setText(m); correo.setError(true); clave.setError(true); }); }
    @Override public void mostrarExito(String m) { SwingUtilities.invokeLater(() -> { mensaje.setForeground(Estilos.EXITO); mensaje.setText(m); correo.setError(false); clave.setError(false); }); }
    @Override public void limpiarClave() { SwingUtilities.invokeLater(() -> clave.limpiar()); }
}
