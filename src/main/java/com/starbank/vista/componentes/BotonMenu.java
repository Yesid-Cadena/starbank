package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonMenu extends JButton {
    private boolean seleccionado;
    private boolean hover;

    public BotonMenu(String texto) {
        super(texto);
        setFont(Estilos.FUENTE_NORMAL);
        setForeground(new Color(0xE2E8F0));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setHorizontalAlignment(LEFT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 16, 10, 16));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }
    public void setSeleccionado(boolean s) { this.seleccionado = s; repaint(); }
    public boolean isSeleccionado() { return seleccionado; }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (seleccionado) {
            g2.setColor(new Color(37, 99, 235, 170));
            g2.fillRoundRect(4, 2, getWidth()-8, getHeight()-4, 10, 10);
            g2.setColor(Estilos.AZUL_CLARO);
            g2.fillRoundRect(0, 8, 4, getHeight()-16, 4, 4);
        } else if (hover) {
            g2.setColor(new Color(255,255,255,20));
            g2.fillRoundRect(4, 2, getWidth()-8, getHeight()-4, 10, 10);
        }
        g2.dispose();
        setForeground(seleccionado ? Color.WHITE : new Color(0xE2E8F0));
        super.paintComponent(g);
    }
}
