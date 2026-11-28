package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class BotonSecundario extends JButton {
    public BotonSecundario(String texto) {
        super(texto);
        setBackground(Color.WHITE);
        setForeground(Estilos.AZUL_ACCION);
        setFocusPainted(false);
        setFont(Estilos.FUENTE_NORMAL);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
