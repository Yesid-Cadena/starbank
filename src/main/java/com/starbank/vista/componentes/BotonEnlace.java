package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonEnlace extends JButton {
    public BotonEnlace(String texto) {
        super(texto);
        setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD));
        setForeground(Estilos.AZUL_ACCION);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(0,0,0,0));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { setForeground(Estilos.AZUL_HOVER); }
            @Override public void mouseExited(MouseEvent e) { setForeground(Estilos.AZUL_ACCION); }
        });
    }
}
