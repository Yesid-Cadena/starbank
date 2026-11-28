package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonPrimario extends JButton {
    public BotonPrimario(String texto) {
        super(texto);
        setFont(Estilos.FUENTE_BOTON);
        setForeground(Color.WHITE);
        setBackground(Estilos.AZUL_ACCION);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(200, 52));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) setBackground(Estilos.AZUL_HOVER); repaint(); }
            @Override public void mouseExited(MouseEvent e) { setBackground(Estilos.AZUL_ACCION); repaint(); }
            @Override public void mousePressed(MouseEvent e) { if (isEnabled()) setBackground(new Color(0x1A45C8)); repaint(); }
            @Override public void mouseReleased(MouseEvent e) { setBackground(isEnabled() ? Estilos.AZUL_HOVER : Estilos.AZUL_ACCION); repaint(); }
        });
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? getBackground() : new Color(0x93B4FD));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
