package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class CampoClave extends JPanel {
    private final JPasswordField campo = new JPasswordField();
    private final JToggleButton ver = new JToggleButton("Mostrar");
    public CampoClave() {
        setLayout(new BorderLayout(5,0));
        setOpaque(false);
        campo.setFont(Estilos.FUENTE_NORMAL);
        ver.setFocusPainted(false);
        ver.addActionListener(e -> campo.setEchoChar(ver.isSelected() ? (char)0 : '•'));
        campo.setEchoChar('•');
        add(campo, BorderLayout.CENTER);
        add(ver, BorderLayout.EAST);
    }
    public char[] getPassword() { return campo.getPassword(); }
    public void limpiar() { campo.setText(""); ver.setSelected(false); campo.setEchoChar('•'); }
    public JPasswordField getCampo() { return campo; }
}
