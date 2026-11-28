package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class BotonPrimario extends JButton {
    public BotonPrimario(String texto) {
        super(texto);
        setBackground(Estilos.AZUL_ACCION);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
