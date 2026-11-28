package com.starbank.vista.paneles;

import com.starbank.controlador.RegistroControlador;
import com.starbank.controlador.RegistroVista;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PanelRegistro extends JPanel implements RegistroVista {
    private final CampoTexto documento = new CampoTexto("Número de documento");
    private final CampoTexto nombre = new CampoTexto("Nombre completo");
    private final CampoTexto correo = new CampoTexto("correo@ejemplo.com");
    private final CampoTexto telefono = new CampoTexto("Número de teléfono");
    private final CampoClave clave = new CampoClave("Crea una contraseña");
    private final CampoClave confirmacion = new CampoClave("Confirma tu contraseña");
    private final JLabel msgDocumento = msg(), msgNombre = msg(), msgCorreo = msg(), msgTelefono = msg(), msgClave = msg(), msgConfirmacion = msg();
    private final JLabel mensajeGeneral = new JLabel(" ");
    private final BotonPrimario btnRegistrar = new BotonPrimario("Crear cuenta");
    private final JProgressBar progreso = new JProgressBar();
    private final JPanel formGrid = new JPanel(new GridBagLayout());
    private final PanelMarca marca = new PanelMarca(PanelMarca.Variante.REGISTRO);
    private final JScrollPane scroll;
    private final JPanel wrapperDerecho = new JPanel(new GridBagLayout());
    private RegistroControlador controlador;
    private boolean dosColumnas = true;
    private boolean modoEscritorio = true;

    public PanelRegistro() {
        setLayout(new GridBagLayout());
        scroll = new JScrollPane();
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Estilos.FONDO);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        btnRegistrar.addActionListener(e -> ejecutarRegistro());
        construirEscritorio();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { actualizarModo(); }
        });
    }

    private void construirEscritorio() {
        removeAll();
        setLayout(new GridBagLayout());
        prepararContenidoDerecho();
        JPanel m = marca; m.setPreferredSize(new Dimension(0,0));
        wrapperDerecho.setPreferredSize(new Dimension(0,0));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH; gc.weighty = 1; gc.gridy = 0;
        gc.gridx = 0; gc.weightx = 0.44; add(m, gc);
        gc.gridx = 1; gc.weightx = 0.56; add(wrapperDerecho, gc);
        modoEscritorio = true;
        revalidate(); repaint();
        SwingUtilities.invokeLater(() -> actualizarColumnas(scroll.getViewport().getWidth()));
    }

    private void construirEstrecho() {
        removeAll();
        setLayout(new BorderLayout());
        prepararContenidoDerecho();
        add(crearHeaderCompacto(), BorderLayout.NORTH);
        add(wrapperDerecho, BorderLayout.CENTER);
        modoEscritorio = false;
        dosColumnas = false;
        construirGrid();
        revalidate(); repaint();
    }

    private void prepararContenidoDerecho() {
        wrapperDerecho.removeAll();
        wrapperDerecho.setBackground(Estilos.FONDO);
        wrapperDerecho.setLayout(new GridBagLayout());
        JPanel form = construirFormulario();
        scroll.setViewportView(form);
        scroll.getViewport().setBackground(Estilos.FONDO);
        // remove resize listeners from old viewport? easier to use one listener
        for (var l : scroll.getViewport().getComponentListeners()) {
            if (l.getClass().getName().contains("PanelRegistro")) scroll.getViewport().removeComponentListener(l);
        }
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { if (modoEscritorio) actualizarColumnas(scroll.getViewport().getWidth()); }
        });
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        wrapperDerecho.add(scroll, c);
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
        } else if (modoEscritorio) {
            actualizarColumnas(scroll.getViewport().getWidth());
        }
    }

    private JPanel construirFormulario() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Estilos.FONDO);
        wrapper.setBorder(new EmptyBorder(28, 36, 28, 36));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));

        JLabel titulo = new JLabel("Crea tu cuenta");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 36));
        titulo.setForeground(Estilos.TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subt = new JLabel("Completa tus datos para comenzar a utilizar StarBank.");
        subt.setFont(Estilos.FUENTE_NORMAL);
        subt.setForeground(Estilos.TEXTO_SECUNDARIO);
        subt.setAlignmentX(Component.LEFT_ALIGNMENT);

        contenido.add(titulo); contenido.add(Box.createVerticalStrut(8)); contenido.add(subt); contenido.add(Box.createVerticalStrut(24));

        formGrid.setOpaque(false);
        formGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        formGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        construirGrid();
        contenido.add(formGrid);
        contenido.add(Box.createVerticalStrut(6));

        JLabel req = new JLabel("La contraseña debe tener entre 8 y 128 caracteres y no puede contener solo espacios.");
        req.setFont(Estilos.FUENTE_PEQUENA); req.setForeground(Estilos.TEXTO_SECUNDARIO); req.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(req); contenido.add(Box.createVerticalStrut(6));
        JLabel nota = new JLabel("Este registro crea tu usuario en StarBank; no crea automáticamente una cuenta bancaria.");
        nota.setFont(Estilos.FUENTE_PEQUENA); nota.setForeground(Estilos.ADVERTENCIA.darker()); nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(nota); contenido.add(Box.createVerticalStrut(14));

        progreso.setIndeterminate(true); progreso.setVisible(false); progreso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6)); progreso.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(progreso); contenido.add(Box.createVerticalStrut(8));
        mensajeGeneral.setFont(Estilos.FUENTE_PEQUENA); mensajeGeneral.setForeground(Estilos.ERROR); mensajeGeneral.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(mensajeGeneral); contenido.add(Box.createVerticalStrut(14));

        btnRegistrar.setAlignmentX(Component.LEFT_ALIGNMENT); btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        contenido.add(btnRegistrar); contenido.add(Box.createVerticalStrut(18));

        JPanel login = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        login.setOpaque(false); login.setAlignmentX(Component.LEFT_ALIGNMENT); login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel ya = new JLabel("¿Ya tienes una cuenta?"); ya.setForeground(Estilos.TEXTO_SECUNDARIO); ya.setFont(Estilos.FUENTE_NORMAL);
        BotonEnlace link = new BotonEnlace("Iniciar sesión"); link.addActionListener(e -> { if (controlador != null) controlador.irALogin(); });
        login.add(ya); login.add(link);
        contenido.add(login);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.weighty = 1; gc.anchor = GridBagConstraints.NORTH; gc.fill = GridBagConstraints.HORIZONTAL;
        wrapper.add(contenido, gc);
        return wrapper;
    }

    private void construirGrid() {
        formGrid.removeAll();
        formGrid.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(0, 0, 12, 12);
        if (dosColumnas) {
            addCampo(formGrid, c, 0, 0, "Documento de identidad", documento, msgDocumento);
            c.insets = new Insets(0, 12, 12, 0); addCampo(formGrid, c, 1, 0, "Nombre completo", nombre, msgNombre);
            c.insets = new Insets(0, 0, 12, 12); addCampo(formGrid, c, 0, 1, "Correo electrónico", correo, msgCorreo);
            c.insets = new Insets(0, 12, 12, 0); addCampo(formGrid, c, 1, 1, "Teléfono", telefono, msgTelefono);
            c.insets = new Insets(0, 0, 12, 12); addCampo(formGrid, c, 0, 2, "Contraseña", clave, msgClave);
            c.insets = new Insets(0, 12, 12, 0); addCampo(formGrid, c, 1, 2, "Confirmar contraseña", confirmacion, msgConfirmacion);
        } else {
            c.insets = new Insets(0, 0, 12, 0);
            addCampo(formGrid, c, 0, 0, "Documento de identidad", documento, msgDocumento);
            addCampo(formGrid, c, 0, 1, "Nombre completo", nombre, msgNombre);
            addCampo(formGrid, c, 0, 2, "Correo electrónico", correo, msgCorreo);
            addCampo(formGrid, c, 0, 3, "Teléfono", telefono, msgTelefono);
            addCampo(formGrid, c, 0, 4, "Contraseña", clave, msgClave);
            addCampo(formGrid, c, 0, 5, "Confirmar contraseña", confirmacion, msgConfirmacion);
        }
        formGrid.revalidate(); formGrid.repaint();
    }

    private void addCampo(JPanel grid, GridBagConstraints base, int col, int row, String etiqueta, JComponent campo, JLabel msg) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = col; c.gridy = row; c.anchor = GridBagConstraints.NORTHWEST;
        JPanel bloque = new JPanel();
        bloque.setOpaque(false);
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(etiqueta);
        l.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD));
        l.setForeground(Estilos.TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (campo instanceof CampoTexto ct) ct.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        if (campo instanceof CampoClave cc) cc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        campo.setPreferredSize(new Dimension(200, 50));
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloque.add(l); bloque.add(Box.createVerticalStrut(6)); bloque.add(campo); bloque.add(Box.createVerticalStrut(4)); bloque.add(msg);
        grid.add(bloque, c);
    }

    private void actualizarColumnas(int anchoDisponible) {
        boolean deberiaSerDos = modoEscritorio && anchoDisponible >= 720;
        if (deberiaSerDos != dosColumnas) { dosColumnas = deberiaSerDos; construirGrid(); }
    }

    private static JLabel msg() { JLabel l = new JLabel(" "); l.setFont(Estilos.FUENTE_PEQUENA); l.setForeground(Estilos.ERROR); return l; }
    public void setControlador(RegistroControlador controlador) { this.controlador = controlador; }

    private void ejecutarRegistro() {
        if (controlador == null) return;
        limpiarErrores();
        char[] p1 = clave.getPassword(); char[] p2 = confirmacion.getPassword();
        btnRegistrar.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return controlador.registrar(documento.getText(), nombre.getText(), correo.getText(), telefono.getText(), p1, p2); }
            @Override protected void done() { btnRegistrar.setEnabled(true); }
        }.execute();
    }

    @Override public void mostrarCargando(boolean cargando) { SwingUtilities.invokeLater(() -> { progreso.setVisible(cargando); btnRegistrar.setEnabled(!cargando); }); }
    @Override public void mostrarError(String m) { SwingUtilities.invokeLater(() -> { mensajeGeneral.setForeground(Estilos.ERROR); mensajeGeneral.setText(m); mapearErrorACampo(m); if (m != null && m.toLowerCase().contains("contraseña")) { clave.limpiar(); confirmacion.limpiar(); } }); }
    @Override public void mostrarErrorCampo(String campo, String mensaje) { SwingUtilities.invokeLater(() -> { switch (campo) { case "documento" -> { documento.setError(true); msgDocumento.setText(mensaje); } case "nombre" -> { nombre.setError(true); msgNombre.setText(mensaje); } case "correo" -> { correo.setError(true); msgCorreo.setText(mensaje); } case "telefono" -> { telefono.setError(true); msgTelefono.setText(mensaje); } case "clave" -> { clave.setError(true); msgClave.setText(mensaje); } case "confirmacion" -> { confirmacion.setError(true); msgConfirmacion.setText(mensaje); } default -> mostrarError(mensaje); } }); }
    @Override public void limpiarErrores() { mensajeGeneral.setText(" "); documento.setError(false); nombre.setError(false); correo.setError(false); telefono.setError(false); clave.setError(false); confirmacion.setError(false); msgDocumento.setText(" "); msgNombre.setText(" "); msgCorreo.setText(" "); msgTelefono.setText(" "); msgClave.setText(" "); msgConfirmacion.setText(" "); }
    private void mapearErrorACampo(String m) { if (m == null) return; String s = m.toLowerCase(); if (s.contains("documento")) { documento.setError(true); msgDocumento.setText(m); } else if (s.contains("nombre")) { nombre.setError(true); msgNombre.setText(m); } else if (s.contains("correo")) { correo.setError(true); msgCorreo.setText(m); } else if (s.contains("teléfono") || s.contains("telefono")) { telefono.setError(true); msgTelefono.setText(m); } else if (s.contains("confirm")) { confirmacion.setError(true); msgConfirmacion.setText(m); } else if (s.contains("contraseña") || s.contains("clave")) { clave.setError(true); msgClave.setText(m); } }
    @Override public void mostrarExito(String m) { SwingUtilities.invokeLater(() -> { mensajeGeneral.setForeground(Estilos.EXITO); mensajeGeneral.setText(m); JOptionPane.showMessageDialog(this, m, "Registro exitoso", JOptionPane.INFORMATION_MESSAGE); }); }
    @Override public void limpiarFormulario() { SwingUtilities.invokeLater(() -> { documento.setText(""); nombre.setText(""); correo.setText(""); telefono.setText(""); clave.limpiar(); confirmacion.limpiar(); limpiarErrores(); }); }
}
