package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class EstadoVacio extends JPanel {
    public EstadoVacio(String titulo, String detalle) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo);
        t.setFont(Estilos.FUENTE_SUBTITULO);
        t.setForeground(Estilos.TEXTO);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel d = new JLabel("<html><div style='text-align:center;width:320px'>" + detalle + "</div></html>");
        d.setFont(Estilos.FUENTE_NORMAL);
        d.setForeground(Estilos.TEXTO_SECUNDARIO);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(t); box.add(Box.createVerticalStrut(8)); box.add(d);
        add(box);
    }
}
