package com.starbank.vista.paneles;

import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;

public class PanelMarca extends JPanel {
    public enum Variante { LOGIN, REGISTRO }

    private static final String[] FRASES_LOGIN = {
            "Tu futuro financiero,|en tus manos.",
            "Cada ahorro,|un paso hacia tus sueños.",
            "Tu dinero, tus decisiones,|tu libertad.",
            "Construye hoy|el futuro que imaginas.",
            "Más confianza, más control,|más posibilidades.",
            "Tus metas comienzan|con una decisión."
    };
    private static final String[] FRASES_REGISTRO = {
            "Comienza a construir|tu futuro financiero.",
            "Abre hoy la puerta|a tus metas.",
            "Tu camino financiero|empieza aquí.",
            "Da el primer paso|hacia tu bienestar.",
            "Crea tu cuenta y|toma el control.",
            "Empieza hoy con|StarBank."
    };

    private final Variante variante;
    private final String[] frases;
    private int indice = 0;
    private int indiceAnterior = 0;
    private float alpha = 1f;
    private float offsetY = 0f;
    private boolean enTransicion = false;
    private boolean mostrandoAnterior = false;
    private long inicioTransicion;
    private final Timer rotacion;
    private final Timer animacion;
    private final FrasePanel frasePanel;
    private final JPanel indicadores;

    public PanelMarca(Variante variante) {
        this.variante = variante;
        this.frases = variante == Variante.LOGIN ? FRASES_LOGIN : FRASES_REGISTRO;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(36, 40, 28, 40));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logo.setOpaque(false);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        logo.add(new LogoStar(34));
        JLabel nombre = new JLabel("StarBank");
        nombre.setFont(new Font("SansSerif", Font.BOLD, 34));
        nombre.setForeground(Color.WHITE);
        logo.add(nombre);
        contenido.add(logo);
        contenido.add(Box.createVerticalGlue());

        frasePanel = new FrasePanel();
        frasePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        frasePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        frasePanel.setPreferredSize(new Dimension(400, 190));
        contenido.add(frasePanel);
        contenido.add(Box.createVerticalStrut(14));

        indicadores = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        indicadores.setOpaque(false);
        indicadores.setAlignmentX(Component.LEFT_ALIGNMENT);
        indicadores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        reconstruirIndicadores();
        contenido.add(indicadores);
        contenido.add(Box.createVerticalStrut(24));

        JLabel slogan1 = linea("Tu dinero, tus metas,", new Color(0xCBD5E1), 20);
        JLabel slogan2 = linea(variante == Variante.LOGIN ? "nuestro compromiso." : "nuestro compromiso.", new Color(0xCBD5E1), 20);
        contenido.add(slogan1);
        contenido.add(slogan2);
        contenido.add(Box.createVerticalGlue());

        add(contenido, BorderLayout.CENTER);
        JLabel pie = new JLabel("StarBank · Plataforma digital");
        pie.setForeground(new Color(255, 255, 255, 170));
        pie.setFont(Estilos.FUENTE_PEQUENA);
        add(pie, BorderLayout.SOUTH);

        animacion = new Timer(16, e -> pasoAnimacion());
        rotacion = new Timer(6000, e -> iniciarTransicion());
        rotacion.setInitialDelay(6000);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) rotacion.start(); else { rotacion.stop(); animacion.stop(); }
            }
        });
    }

    private void iniciarTransicion() {
        if (enTransicion || frases.length < 2) return;
        indiceAnterior = indice;
        indice = (indice + 1) % frases.length;
        enTransicion = true;
        mostrandoAnterior = true;
        inicioTransicion = System.currentTimeMillis();
        animacion.start();
    }

    private float easeInOut(float p) {
        return p < 0.5f ? 4f * p * p * p : 1f - (float) Math.pow(-2f * p + 2f, 3) / 2f;
    }

    private void pasoAnimacion() {
        long transcurrido = System.currentTimeMillis() - inicioTransicion;
        float duracion = 1200f;
        float p = Math.min(1f, transcurrido / duracion);
        if (p <= 0.45f) {
            float pe = easeInOut(p / 0.45f);
            mostrandoAnterior = true;
            alpha = 1f - pe;
            offsetY = -10f * pe;
        } else if (p <= 0.55f) {
            mostrandoAnterior = false;
            alpha = 0f;
            offsetY = 10f;
        } else {
            float pe = easeInOut((p - 0.55f) / 0.45f);
            mostrandoAnterior = false;
            alpha = pe;
            offsetY = 10f * (1f - pe);
        }
        if (p >= 1f) {
            alpha = 1f; offsetY = 0f; enTransicion = false; mostrandoAnterior = false; animacion.stop();
            reconstruirIndicadores();
        }
        frasePanel.repaint();
    }

    private void reconstruirIndicadores() {
        indicadores.removeAll();
        for (int i = 0; i < frases.length; i++) {
            final int idx = i;
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(idx == indice ? Estilos.AZUL_CLARO : new Color(255, 255, 255, 90));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                }
            };
            dot.setOpaque(false);
            dot.setPreferredSize(new Dimension(idx == indice ? 22 : 8, 8));
            indicadores.add(dot);
        }
        indicadores.revalidate();
        indicadores.repaint();
    }

    private JLabel linea(String texto, Color color, int tam) {
        JLabel l = new JLabel(texto);
        l.setForeground(color);
        l.setFont(new Font("SansSerif", Font.BOLD, tam));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, Estilos.AZUL_OSCURO, getWidth(), getHeight(), Estilos.AZUL_SECUNDARIO);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillOval(getWidth() - 180, -80, 320, 320);
        g2.setColor(new Color(128, 184, 255, 24));
        g2.fillOval(-120, getHeight() - 180, 300, 300);
        g2.setColor(new Color(255, 255, 255, 12));
        g2.fillOval(getWidth() / 2, getHeight() - 120, 220, 220);
        g2.dispose();
        super.paintComponent(g);
    }

    private class FrasePanel extends JPanel {
        FrasePanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            String texto = enTransicion && mostrandoAnterior ? frases[indiceAnterior] : frases[indice];
            float a = Math.max(0f, Math.min(1f, alpha));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            int y = 45 + Math.round(offsetY);
            int ancho = Math.max(80, getWidth() - 4);
            g2.setFont(new Font("SansSerif", Font.BOLD, 36));
            FontMetrics fm = g2.getFontMetrics();
            String[] partes = texto.split("\\|");
            for (int i = 0; i < partes.length; i++) {
                boolean ultima = i == partes.length - 1;
                g2.setColor(ultima ? Estilos.AZUL_CLARO : Color.WHITE);
                java.util.List<String> lineas = envolver(partes[i].trim(), fm, ancho);
                for (String ln : lineas) {
                    g2.drawString(ln, 0, y);
                    y += fm.getHeight() + 4;
                }
                y += 4;
            }
            g2.dispose();
        }

        private java.util.List<String> envolver(String texto, FontMetrics fm, int anchoMax) {
            java.util.List<String> out = new java.util.ArrayList<>();
            if (texto == null || texto.isEmpty()) { out.add(""); return out; }
            String[] palabras = texto.split("\\s+");
            StringBuilder linea = new StringBuilder();
            for (String p : palabras) {
                String cand = linea.length() == 0 ? p : linea + " " + p;
                if (fm.stringWidth(cand) <= anchoMax) { linea.setLength(0); linea.append(cand); }
                else { if (linea.length() > 0) out.add(linea.toString()); linea.setLength(0); linea.append(p); }
            }
            if (linea.length() > 0) out.add(linea.toString());
            return out.isEmpty() ? java.util.List.of(texto) : out;
        }
    }

    static class LogoStar extends JComponent {
        LogoStar(int size) { setPreferredSize(new Dimension(size, size)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2, r = Math.min(w, h) / 2;
            int[] xs = {cx, cx + r / 4, cx + r, cx + r / 4, cx, cx - r / 4, cx - r, cx - r / 4};
            int[] ys = {cy - r, cy - r / 4, cy, cy + r / 4, cy + r, cy + r / 4, cy, cy - r / 4};
            g2.setColor(Estilos.AZUL_CLARO);
            g2.fillPolygon(xs, ys, 8);
            g2.dispose();
        }
    }
}
