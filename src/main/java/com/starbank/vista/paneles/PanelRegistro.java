package com.starbank.vista.paneles;

import com.starbank.controlador.RegistroControlador;
import com.starbank.controlador.RegistroVista;
import com.starbank.vista.componentes.BotonPrimario;
import com.starbank.vista.componentes.BotonSecundario;
import com.starbank.vista.componentes.CampoClave;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class PanelRegistro extends JPanel implements RegistroVista {
    private final JTextField documento = new JTextField(20), nombre = new JTextField(20), correo = new JTextField(20), telefono = new JTextField(20);
    private final CampoClave clave = new CampoClave(), confirmacion = new CampoClave();
    private final JLabel mensaje = new JLabel(" ");
    private final BotonPrimario btnRegistrar = new BotonPrimario("Crear cuenta");
    private final BotonSecundario btnVolver = new BotonSecundario("Volver al inicio de sesión");
    private final JProgressBar progreso = new JProgressBar();
    private RegistroControlador controlador;

    public PanelRegistro() {
        setBackground(Estilos.FONDO);
        setLayout(new GridBagLayout());
        JPanel card = Estilos.tarjeta();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets=new Insets(5,5,5,5); c.fill=GridBagConstraints.HORIZONTAL; c.gridx=0; c.weightx=1;
        int y=0; c.gridy=y++; card.add(Estilos.subtitulo("Registro de cliente"), c);
        agregar(card,c,y++,"Documento",documento); agregar(card,c,y++,"Nombre completo",nombre); agregar(card,c,y++,"Correo",correo); agregar(card,c,y++,"Teléfono",telefono);
        c.gridy=y++; card.add(new JLabel("Contraseña"),c); c.gridy=y++; card.add(clave,c);
        c.gridy=y++; card.add(new JLabel("Confirmar contraseña"),c); c.gridy=y++; card.add(confirmacion,c);
        progreso.setIndeterminate(true); progreso.setVisible(false); c.gridy=y++; card.add(progreso,c);
        mensaje.setForeground(Estilos.ERROR); c.gridy=y++; card.add(mensaje,c);
        c.gridy=y++; card.add(btnRegistrar,c); c.gridy=y++; card.add(btnVolver,c);
        btnRegistrar.addActionListener(e -> ejecutarRegistro());
        btnVolver.addActionListener(e -> { if (controlador != null) controlador.irALogin(); });
        add(card);
    }

    public void setControlador(RegistroControlador controlador) { this.controlador = controlador; }

    private void agregar(JPanel card, GridBagConstraints c, int y, String label, JComponent comp) { c.gridy=y; card.add(new JLabel(label),c); c.gridy=y+1; card.add(comp,c); }

    private void ejecutarRegistro() {
        if (controlador == null) return;
        char[] p1 = clave.getPassword(); char[] p2 = confirmacion.getPassword();
        btnRegistrar.setEnabled(false);
        new SwingWorker<Boolean,Void>() {
            @Override protected Boolean doInBackground() { return controlador.registrar(documento.getText(), nombre.getText(), correo.getText(), telefono.getText(), p1, p2); }
            @Override protected void done() { btnRegistrar.setEnabled(true); }
        }.execute();
    }

    @Override public void mostrarCargando(boolean cargando) { SwingUtilities.invokeLater(() -> { progreso.setVisible(cargando); btnRegistrar.setEnabled(!cargando); }); }
    @Override public void mostrarError(String m) { SwingUtilities.invokeLater(() -> { mensaje.setForeground(Estilos.ERROR); mensaje.setText(m); }); }
    @Override public void mostrarExito(String m) { SwingUtilities.invokeLater(() -> { mensaje.setForeground(Estilos.EXITO); mensaje.setText(m); JOptionPane.showMessageDialog(this, m); }); }
    @Override public void limpiarFormulario() { SwingUtilities.invokeLater(() -> { documento.setText(""); nombre.setText(""); correo.setText(""); telefono.setText(""); clave.limpiar(); confirmacion.limpiar(); }); }
}
