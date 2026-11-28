package com.starbank.vista.paneles;

import com.starbank.controlador.SesionControlador;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelPrincipalAdmin extends JPanel {
    private final CardLayout cards = new CardLayout();
    private final JPanel contenido = new JPanel(cards);
    private final SesionControlador sesion;
    private final Runnable alCerrarSesion;

    public PanelPrincipalAdmin(SesionControlador sesion, Runnable alCerrarSesion) {
        this.sesion = sesion; this.alCerrarSesion = alCerrarSesion;
        setLayout(new BorderLayout());
        add(crearMenu(), BorderLayout.WEST);
        contenido.add(inicio(), "resumen");
        contenido.add(new PanelPendiente("Clientes"), "clientes");
        contenido.add(new PanelPendiente("Cuentas"), "cuentas");
        contenido.add(new PanelPendiente("Transacciones"), "transacciones");
        contenido.add(new PanelPendiente("Solicitudes"), "solicitudes");
        contenido.add(new PanelPendiente("Auditoría"), "auditoria");
        contenido.add(new PanelPendiente("Reportes"), "reportes");
        add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel(); menu.setBackground(Estilos.AZUL_SECUNDARIO); menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS)); menu.setBorder(new EmptyBorder(20,15,20,15));
        JLabel titulo = new JLabel("StarBank Admin"); titulo.setForeground(Color.WHITE); titulo.setFont(Estilos.FUENTE_SUBTITULO); titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(titulo); menu.add(Box.createVerticalStrut(20));
        agregarBoton(menu, "Resumen", () -> cards.show(contenido, "resumen"));
        agregarBoton(menu, "Clientes", () -> cards.show(contenido, "clientes"));
        agregarBoton(menu, "Cuentas", () -> cards.show(contenido, "cuentas"));
        agregarBoton(menu, "Transacciones", () -> cards.show(contenido, "transacciones"));
        agregarBoton(menu, "Solicitudes", () -> cards.show(contenido, "solicitudes"));
        agregarBoton(menu, "Auditoría", () -> cards.show(contenido, "auditoria"));
        agregarBoton(menu, "Reportes", () -> cards.show(contenido, "reportes"));
        menu.add(Box.createVerticalGlue());
        JButton salir = new JButton("Cerrar sesión"); salir.setAlignmentX(Component.LEFT_ALIGNMENT); salir.addActionListener(e -> { sesion.cerrar(); alCerrarSesion.run(); });
        menu.add(salir);
        return menu;
    }

    private void agregarBoton(JPanel menu, String texto, Runnable accion) {
        JButton b = new JButton(texto); b.setMaximumSize(new Dimension(180,35)); b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> accion.run()); menu.add(b); menu.add(Box.createVerticalStrut(6));
    }

    private JPanel inicio() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Estilos.FONDO);
        JPanel card = Estilos.tarjeta(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Estilos.titulo("Panel administrativo"));
        card.add(Box.createVerticalStrut(10));
        card.add(new JLabel("Resumen operativo disponible en fases posteriores."));
        p.add(card); return p;
    }
}
