package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CampoClave extends JPanel {
    private final JPasswordField campo = new JPasswordField();
    private final JButton ver = new JButton() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h / 2;
            g2.setColor(campo.hasFocus() ? Estilos.AZUL_ACCION : Estilos.TEXTO_SECUNDARIO);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int rw = 11, rh = 7;
            g2.drawArc(cx - rw, cy - rh, rw * 2, rh * 2, 0, 180);
            g2.drawArc(cx - rw, cy - rh, rw * 2, rh * 2, 180, 180);
            g2.fillOval(cx - 3, cy - 3, 6, 6);
            if (!visible) {
                g2.drawLine(cx - 9, cy - 8, cx + 9, cy + 8);
            }
            g2.dispose();
        }
    };
    private final String placeholder;
    private boolean error;
    private boolean visible;

    public CampoClave() { this("Ingresa tu contraseña"); }
    public CampoClave(String placeholder) {
        this.placeholder = placeholder;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(200, 50));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        campo.setBorder(new EmptyBorder(10,14,10,4));
        campo.setFont(Estilos.FUENTE_NORMAL);
        campo.setOpaque(false);
        campo.setEchoChar('•');
        ver.setPreferredSize(new Dimension(44, 50));
        ver.setContentAreaFilled(false);
        ver.setBorderPainted(false);
        ver.setFocusPainted(false);
        ver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ver.setToolTipText("Mostrar u ocultar contraseña");
        ver.getAccessibleContext().setAccessibleName("Mostrar u ocultar contraseña");
        ver.addActionListener(e -> { visible=!visible; campo.setEchoChar(visible ? (char)0 : '•'); ver.repaint(); });
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); ver.repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); ver.repaint(); }
        });
        add(campo, BorderLayout.CENTER);
        add(ver, BorderLayout.EAST);
    }

    public char[] getPassword() { return campo.getPassword(); }
    public void limpiar() { campo.setText(""); visible=false; campo.setEchoChar('•'); ver.repaint(); }
    public JPasswordField getCampo() { return campo; }
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
        if (campo.getPassword().length == 0 && !campo.hasFocus()) {
            g2.setColor(Estilos.PLACEHOLDER);
            g2.setFont(Estilos.FUENTE_NORMAL);
            Insets ins = campo.getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, ins.left, (getHeight()+fm.getAscent()-fm.getDescent())/2);
        }
        g2.dispose();
    }
}
