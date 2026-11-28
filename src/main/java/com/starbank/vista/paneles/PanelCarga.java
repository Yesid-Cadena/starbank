package com.starbank.vista.paneles;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelCarga extends JPanel {
    private final JLabel estado = new JLabel("Iniciando servicios...");
    private final JProgressBar barra = new JProgressBar();
    public PanelCarga() {
        setBackground(Estilos.AZUL_OSCURO);
        setLayout(new GridBagLayout());
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel nombre = new JLabel("StarBank");
        nombre.setFont(new Font("SansSerif", Font.BOLD, 42));
        nombre.setForeground(Color.WHITE);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel slogan = new JLabel("Tu dinero, tus metas, nuestro compromiso");
        slogan.setFont(Estilos.FUENTE_NORMAL);
        slogan.setForeground(new Color(0xC7D2FE));
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        estado.setForeground(Color.WHITE);
        estado.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.setIndeterminate(true);
        barra.setMaximumSize(new Dimension(320, 10));
        box.add(nombre); box.add(Box.createVerticalStrut(8)); box.add(slogan); box.add(Box.createVerticalStrut(24));
        box.add(barra); box.add(Box.createVerticalStrut(10)); box.add(estado);
        add(box);
    }
    public void setEstado(String texto) { estado.setText(texto); }
    public void detener() { barra.setIndeterminate(false); barra.setValue(100); }
}
