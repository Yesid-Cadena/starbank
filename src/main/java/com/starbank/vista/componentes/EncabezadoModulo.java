package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EncabezadoModulo extends JPanel {
    private final JLabel titulo = new JLabel();
    private final JLabel descripcion = new JLabel();
    private final JLabel usuario = new JLabel();
    private final AvatarIniciales avatar = new AvatarIniciales("SB", 38);

    public EncabezadoModulo(String titulo, String descripcion) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,Estilos.BORDE),
                new EmptyBorder(18, 24, 18, 24)));
        JPanel izq = new JPanel(); izq.setOpaque(false); izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        this.titulo.setText(titulo); this.titulo.setFont(new Font("SansSerif", Font.BOLD, 24)); this.titulo.setForeground(Estilos.TEXTO);
        this.descripcion.setText(descripcion); this.descripcion.setFont(Estilos.FUENTE_NORMAL); this.descripcion.setForeground(Estilos.TEXTO_SECUNDARIO);
        izq.add(this.titulo); izq.add(Box.createVerticalStrut(4)); izq.add(this.descripcion);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); der.setOpaque(false);
        usuario.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD)); usuario.setForeground(Estilos.TEXTO);
        der.add(usuario); der.add(avatar);
        add(izq, BorderLayout.WEST); add(der, BorderLayout.EAST);
    }
    public void setUsuario(String nombre) { usuario.setText(nombre == null ? "" : nombre); avatar.setIniciales(UtilFormato.iniciales(nombre)); }
    public void setTitulo(String t) { titulo.setText(t); }
}
