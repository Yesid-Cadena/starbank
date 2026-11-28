package com.starbank.vista.paneles;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.SesionControlador;
import com.starbank.modelo.entidades.Cliente;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelPerfilCliente extends JPanel implements PanelPrincipalCliente.Refrescable {
    private final ConfiguracionServicios config; private final SesionControlador sesion; private final Runnable alCerrarSesion;
    private final EncabezadoModulo encabezado = new EncabezadoModulo("Mi perfil", "Consulta y actualiza tu información");
    private final CampoTexto nombre = new CampoTexto("Nombre completo");
    private final CampoTexto correo = new CampoTexto("Correo electrónico");
    private final CampoTexto telefono = new CampoTexto("Teléfono");
    private final JLabel info = new JLabel(" ");
    private final JLabel msgPerfil = new JLabel(" ");
    private final CampoClave claveActual = new CampoClave("Contraseña actual");
    private final CampoClave claveNueva = new CampoClave("Nueva contraseña");
    private final CampoClave claveConfirm = new CampoClave("Confirmar nueva contraseña");
    private final JLabel msgClave = new JLabel(" ");

    public PanelPerfilCliente(ConfiguracionServicios config, SesionControlador sesion, Runnable alCerrarSesion) {
        this.config = config; this.sesion = sesion; this.alCerrarSesion = alCerrarSesion;
        setLayout(new BorderLayout()); setBackground(Estilos.FONDO); add(encabezado, BorderLayout.NORTH);
        JPanel c = new JPanel(); c.setOpaque(false); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); c.setBorder(new EmptyBorder(24,24,24,24));
        info.setAlignmentX(Component.LEFT_ALIGNMENT); info.setForeground(Estilos.TEXTO_SECUNDARIO); c.add(info); c.add(Box.createVerticalStrut(16));
        c.add(label("Nombre completo")); c.add(nombre); c.add(Box.createVerticalStrut(10));
        c.add(label("Correo electrónico")); c.add(correo); c.add(Box.createVerticalStrut(10));
        c.add(label("Teléfono")); c.add(telefono); c.add(Box.createVerticalStrut(10));
        msgPerfil.setForeground(Estilos.ERROR); c.add(msgPerfil); c.add(Box.createVerticalStrut(8));
        BotonPrimario guardar = new BotonPrimario("Guardar cambios"); guardar.setMaximumSize(new Dimension(Integer.MAX_VALUE,48)); guardar.addActionListener(e -> guardar()); c.add(guardar);
        c.add(Box.createVerticalStrut(24)); c.add(new JSeparator()); c.add(Box.createVerticalStrut(16));
        JLabel t2 = new JLabel("Cambiar contraseña"); t2.setFont(Estilos.FUENTE_SUBTITULO); t2.setForeground(Estilos.TEXTO); t2.setAlignmentX(Component.LEFT_ALIGNMENT); c.add(t2); c.add(Box.createVerticalStrut(10));
        c.add(label("Contraseña actual")); c.add(claveActual); c.add(Box.createVerticalStrut(10));
        c.add(label("Nueva contraseña")); c.add(claveNueva); c.add(Box.createVerticalStrut(10));
        c.add(label("Confirmar nueva contraseña")); c.add(claveConfirm); c.add(Box.createVerticalStrut(10));
        msgClave.setForeground(Estilos.ERROR); c.add(msgClave); c.add(Box.createVerticalStrut(8));
        BotonPrimario cambiar = new BotonPrimario("Actualizar contraseña"); cambiar.setMaximumSize(new Dimension(Integer.MAX_VALUE,48)); cambiar.addActionListener(e -> cambiarClave()); c.add(cambiar);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(c, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap); sp.setBorder(null); sp.getViewport().setBackground(Estilos.FONDO); sp.getVerticalScrollBar().setUnitIncrement(16); add(sp, BorderLayout.CENTER);
    }
    private JLabel label(String t) { JLabel l = new JLabel(t); l.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD)); l.setForeground(Estilos.TEXTO); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }
    @Override public void refrescar() {
        try { Cliente cl = config.obtenerClientes().consultarPerfil(sesion.getSesion()); encabezado.setUsuario(cl.obtenerNombre()); info.setText("Documento: " + cl.obtenerIdentificacion() + "  |  Estado: " + cl.obtenerEstado()); nombre.setText(cl.obtenerNombre()); correo.setText(cl.obtenerCorreo()); telefono.setText(cl.obtenerTelefono()); msgPerfil.setText(" "); msgClave.setText(" "); }
        catch (Exception ex) { msgPerfil.setForeground(Estilos.ERROR); msgPerfil.setText(ex.getMessage()); }
    }
    private void guardar() {
        try { config.obtenerClientes().actualizarPerfil(sesion.getSesion(), nombre.getText(), correo.getText(), telefono.getText()); msgPerfil.setForeground(Estilos.EXITO); msgPerfil.setText("Perfil actualizado correctamente."); refrescar(); }
        catch (Exception ex) { msgPerfil.setForeground(Estilos.ERROR); msgPerfil.setText(ex.getMessage()); }
    }
    private void cambiarClave() {
        char[] actual = claveActual.getPassword(); char[] nueva = claveNueva.getPassword(); char[] conf = claveConfirm.getPassword();
        if (!java.util.Arrays.equals(nueva, conf)) { msgClave.setForeground(Estilos.ERROR); msgClave.setText("La confirmación no coincide."); claveNueva.limpiar(); claveConfirm.limpiar(); return; }
        try {
            Sesion nuevaSesion = config.obtenerAutenticacion().cambiarClave(sesion.getSesion().obtenerIdentificador(), actual, nueva);
            sesion.iniciar(nuevaSesion);
            claveActual.limpiar(); claveNueva.limpiar(); claveConfirm.limpiar();
            msgClave.setForeground(Estilos.EXITO); msgClave.setText("Contraseña actualizada. La sesión fue renovada.");
        } catch (Exception ex) { claveActual.limpiar(); claveNueva.limpiar(); claveConfirm.limpiar(); msgClave.setForeground(Estilos.ERROR); msgClave.setText(ex.getMessage()); }
    }
}
