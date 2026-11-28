package com.starbank.vista.paneles;

import com.starbank.controlador.SesionControlador;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelPrincipalCliente extends JPanel {
    private final CardLayout cards = new CardLayout();
    private final JPanel contenido = new JPanel(cards);
    private final SesionControlador sesion;
    private final Runnable alCerrarSesion;

    public PanelPrincipalCliente(SesionControlador sesion, Runnable alCerrarSesion) {
        this.sesion = sesion; this.alCerrarSesion = alCerrarSesion;
        setLayout(new BorderLayout());
        add(crearMenu(), BorderLayout.WEST);
        contenido.add(inicio(), "inicio");
        contenido.add(new PanelPendiente("Mis cuentas"), "cuentas");
        contenido.add(new PanelPendiente("Depositar"), "depositar");
        contenido.add(new PanelPendiente("Retirar"), "retirar");
        contenido.add(new PanelPendiente("Transferir"), "transferir");
        contenido.add(new PanelPendiente("Movimientos"), "movimientos");
        contenido.add(new PanelPendiente("Mi perfil"), "perfil");
        contenido.add(new PanelPendiente("Atención al cliente"), "atencion");
        add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel(); menu.setBackground(Estilos.AZUL_OSCURO); menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS)); menu.setBorder(new EmptyBorder(20,15,20,15));
        JLabel titulo = new JLabel("StarBank"); titulo.setForeground(Color.WHITE); titulo.setFont(Estilos.FUENTE_SUBTITULO); titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(titulo); menu.add(Box.createVerticalStrut(20));
        agregarBoton(menu, "Inicio", () -> cards.show(contenido, "inicio"));
        agregarBoton(menu, "Mis cuentas", () -> cards.show(contenido, "cuentas"));
        agregarBoton(menu, "Depositar", () -> cards.show(contenido, "depositar"));
        agregarBoton(menu, "Retirar", () -> cards.show(contenido, "retirar"));
        agregarBoton(menu, "Transferir", () -> cards.show(contenido, "transferir"));
        agregarBoton(menu, "Movimientos", () -> cards.show(contenido, "movimientos"));
        agregarBoton(menu, "Mi perfil", () -> cards.show(contenido, "perfil"));
        agregarBoton(menu, "Atención al cliente", () -> cards.show(contenido, "atencion"));
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
        card.add(Estilos.titulo("Panel del cliente"));
        card.add(Box.createVerticalStrut(10));
        JLabel info = new JLabel("Bienvenido a StarBank. Selecciona una opción del menú.");
        info.setForeground(Estilos.TEXTO_SECUNDARIO);
        card.add(info);
        card.add(Box.createVerticalStrut(10));
        JLabel nota = new JLabel("Los módulos funcionales se habilitarán en fases posteriores.");
        nota.setForeground(Estilos.ADVERTENCIA);
        card.add(nota);
        p.add(card); return p;
    }
}
