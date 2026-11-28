package com.starbank.vista.paneles;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class PanelPendiente extends JPanel {
    public PanelPendiente(String modulo) {
        setBackground(Estilos.FONDO);
        setLayout(new GridBagLayout());
        JLabel lbl = new JLabel("Módulo pendiente: " + modulo);
        lbl.setFont(Estilos.FUENTE_SUBTITULO);
        lbl.setForeground(Estilos.TEXTO_SECUNDARIO);
        add(lbl);
    }
}
