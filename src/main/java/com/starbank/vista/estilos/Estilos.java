package com.starbank.vista.estilos;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class Estilos {
    public static final Color AZUL_OSCURO = new Color(0x0B1B36);
    public static final Color AZUL_SECUNDARIO = new Color(0x19488C);
    public static final Color AZUL_ACCION = new Color(0x2563EB);
    public static final Color AZUL_HOVER = new Color(0x1D4ED8);
    public static final Color AZUL_CLARO = new Color(0x80B8FF);
    public static final Color FONDO = new Color(0xF8FAFC);
    public static final Color FONDO_SECUNDARIO = new Color(0xF1F5F9);
    public static final Color TEXTO = new Color(0x102B49);
    public static final Color TEXTO_SECUNDARIO = new Color(0x64748B);
    public static final Color BORDE = new Color(0xCBD5E1);
    public static final Color EXITO = new Color(0x16A34A);
    public static final Color ERROR = new Color(0xDC2626);
    public static final Color ADVERTENCIA = new Color(0xD97706);
    public static final Color PLACEHOLDER = new Color(0x94A3B8);

    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 36);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FUENTE_NORMAL = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font FUENTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_BOTON = new Font("SansSerif", Font.BOLD, 16);

    private Estilos() {}

    public static void aplicarTema() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", FUENTE_NORMAL);
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusColor", AZUL_ACCION);
        UIManager.put("TextComponent.margin", new Insets(10,14,10,14));
        UIManager.put("Button.margin", new Insets(10,18,10,18));
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("PasswordField.foreground", TEXTO);
        UIManager.put("Component.borderColor", BORDE);
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto); l.setFont(FUENTE_TITULO); l.setForeground(TEXTO); return l;
    }
    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto); l.setFont(FUENTE_SUBTITULO); l.setForeground(TEXTO); return l;
    }
    public static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), new EmptyBorder(24,24,24,24)));
        return p;
    }
}
