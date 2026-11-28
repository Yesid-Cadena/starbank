package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class AvatarIniciales extends JComponent {
    private String iniciales;
    public AvatarIniciales(String iniciales, int tam) {
        this.iniciales = iniciales;
        setPreferredSize(new Dimension(tam, tam));
        setMinimumSize(new Dimension(tam, tam));
        setMaximumSize(new Dimension(tam, tam));
    }
    public void setIniciales(String ini) { this.iniciales = ini; repaint(); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Estilos.AZUL_SECUNDARIO);
        g2.fillOval(0,0,getWidth(),getHeight());
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, getWidth()/3));
        FontMetrics fm = g2.getFontMetrics();
        String t = iniciales == null ? "" : iniciales;
        g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
        g2.dispose();
    }
}
