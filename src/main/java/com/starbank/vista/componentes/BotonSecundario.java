package com.starbank.vista.componentes;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import java.awt.*;

public class BotonSecundario extends JButton {
    public BotonSecundario(String texto) {
        super(texto);
        setFont(Estilos.FUENTE_NORMAL);
        setForeground(Estilos.AZUL_ACCION);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
