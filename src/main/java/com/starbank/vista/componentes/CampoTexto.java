package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CampoTexto extends JPanel {
    private final JTextField campo = new JTextField();
    private final String placeholder;
    private boolean error;

    public CampoTexto(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(200, 50));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        campo.setBorder(new EmptyBorder(10,14,10,14));
        campo.setFont(Estilos.FUENTE_NORMAL);
        campo.setOpaque(false);
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
        add(campo, BorderLayout.CENTER);
    }

    public String getText() { return campo.getText(); }
    public void setText(String t) { campo.setText(t); }
    public JTextField getCampo() { return campo; }
    public void setError(boolean error) { this.error = error; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
        Color borde = error ? Estilos.ERROR : (campo.hasFocus() ? Estilos.AZUL_ACCION : Estilos.BORDE);
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(campo.hasFocus() || error ? 2f : 1f));
        g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
        if (campo.getText().isEmpty() && !campo.hasFocus()) {
            g2.setColor(Estilos.PLACEHOLDER);
            g2.setFont(Estilos.FUENTE_NORMAL);
            Insets ins = campo.getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, ins.left, (getHeight()+fm.getAscent()-fm.getDescent())/2);
        }
        g2.dispose();
    }
}
