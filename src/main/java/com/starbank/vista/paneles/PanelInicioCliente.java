package com.starbank.vista.paneles;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.SesionControlador;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public class PanelInicioCliente extends JPanel implements PanelPrincipalCliente.Refrescable {
    private final ConfiguracionServicios config;
    private final SesionControlador sesion;
    private final Consumer<String> navegar;
    private final EncabezadoModulo encabezado = new EncabezadoModulo("Inicio", "Resumen de tu actividad financiera");
    private final JPanel cuerpo = new JPanel();
    private boolean saldoVisible = true;

    public PanelInicioCliente(ConfiguracionServicios config, SesionControlador sesion, Consumer<String> navegar) {
        this.config = config; this.sesion = sesion; this.navegar = navegar;
        setLayout(new BorderLayout()); setBackground(Estilos.FONDO);
        add(encabezado, BorderLayout.NORTH);
        cuerpo.setOpaque(false); cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBorder(new EmptyBorder(24,24,24,24));
        JScrollPane sp = new JScrollPane(cuerpo); sp.setBorder(null); sp.getViewport().setBackground(Estilos.FONDO); sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    @Override public void refrescar() {
        cuerpo.removeAll();
        try {
            var s = sesion.getSesion();
            var cliente = sesion.usuarioActual();
            encabezado.setUsuario(cliente.obtenerNombre());
            List<Cuenta> cuentas = config.obtenerCuentas().consultarPorCliente(s);
            BigDecimal total = cuentas.stream().map(Cuenta::consultarSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
            long activas = cuentas.stream().filter(c -> c.obtenerEstado().name().equals("ACTIVA")).count();

            JLabel saludo = new JLabel("Hola, " + cliente.obtenerNombre());
            saludo.setFont(new Font("SansSerif", Font.BOLD, 28)); saludo.setForeground(Estilos.TEXTO); saludo.setAlignmentX(Component.LEFT_ALIGNMENT);
            cuerpo.add(saludo); cuerpo.add(Box.createVerticalStrut(18));

            JPanel stats = new JPanel(new GridLayout(1,2,16,16)); stats.setOpaque(false); stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110)); stats.setAlignmentX(Component.LEFT_ALIGNMENT);
            stats.add(tarjetaStat("Saldo total disponible", UtilFormato.moneda(total)));
            stats.add(tarjetaStat("Cuentas activas", String.valueOf(activas)));
            cuerpo.add(stats); cuerpo.add(Box.createVerticalStrut(18));

            if (cuentas.isEmpty()) {
                cuerpo.add(new EstadoVacio("Aún no tienes cuentas bancarias", "Necesitas una cuenta bancaria habilitada para comenzar a operar. Contacta a un administrador."));
            } else {
                Cuenta cuenta = cuentas.get(0);
                cuerpo.add(tarjetaCuenta(cuenta));
                cuerpo.add(Box.createVerticalStrut(18));

                JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); acciones.setOpaque(false); acciones.setAlignmentX(Component.LEFT_ALIGNMENT);
                acciones.add(accion("Depositar", () -> navegar.accept("depositar")));
                acciones.add(accion("Retirar", () -> navegar.accept("retirar")));
                acciones.add(accion("Transferir", () -> navegar.accept("transferir")));
                cuerpo.add(acciones); cuerpo.add(Box.createVerticalStrut(20));

                JLabel ult = new JLabel("Últimos movimientos"); ult.setFont(Estilos.FUENTE_SUBTITULO); ult.setForeground(Estilos.TEXTO); ult.setAlignmentX(Component.LEFT_ALIGNMENT);
                cuerpo.add(ult); cuerpo.add(Box.createVerticalStrut(10));
                List<Transaccion> movs = config.obtenerTransacciones().historialCuenta(s, cuenta.obtenerNumeroCuenta());
                int from = Math.max(0, movs.size() - 5);
                for (Transaccion t : movs.subList(from, movs.size())) cuerpo.add(filaMovimiento(t, cuenta.obtenerNumeroCuenta()));
                if (movs.isEmpty()) { JLabel n = new JLabel("No hay movimientos registrados."); n.setForeground(Estilos.TEXTO_SECUNDARIO); n.setAlignmentX(Component.LEFT_ALIGNMENT); cuerpo.add(n); }
                BotonEnlace ver = new BotonEnlace("Ver historial completo"); ver.setAlignmentX(Component.LEFT_ALIGNMENT); ver.addActionListener(e -> navegar.accept("movimientos")); cuerpo.add(Box.createVerticalStrut(8)); cuerpo.add(ver);
            }
        } catch (Exception ex) { mostrarError(ex); }
        cuerpo.revalidate(); cuerpo.repaint();
    }

    private JPanel tarjetaStat(String titulo, String valor) {
        JPanel p = tarjetaBlanca(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo); t.setForeground(Estilos.TEXTO_SECUNDARIO); t.setFont(Estilos.FUENTE_PEQUENA);
        JLabel v = new JLabel(valor); v.setForeground(Estilos.TEXTO); v.setFont(new Font("SansSerif", Font.BOLD, 24));
        p.add(t); p.add(Box.createVerticalStrut(8)); p.add(v); return p;
    }

    private JPanel tarjetaCuenta(Cuenta cuenta) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,Estilos.AZUL_OSCURO,getWidth(),getHeight(),Estilos.AZUL_SECUNDARIO));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18); g2.dispose();
            }
        };
        p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(22,24,22,24));
        p.setMaximumSize(new Dimension(520, 220)); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel marca = new JLabel("StarBank"); marca.setForeground(Color.WHITE); marca.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel tipo = new JLabel(cuenta.obtenerTipoCuenta() + " · " + cuenta.obtenerEstado()); tipo.setForeground(new Color(0xCBD5E1));
        JLabel saldoLbl = new JLabel(saldoVisible ? UtilFormato.moneda(cuenta.consultarSaldo()) : "••••••"); saldoLbl.setForeground(Color.WHITE); saldoLbl.setFont(new Font("SansSerif", Font.BOLD, 30));
        JButton toggle = new JButton(saldoVisible ? "Ocultar saldo" : "Mostrar saldo"); toggle.setContentAreaFilled(false); toggle.setBorderPainted(false); toggle.setForeground(Estilos.AZUL_CLARO); toggle.setFocusPainted(false); toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> { saldoVisible = !saldoVisible; refrescar(); });
        JLabel num = new JLabel(UtilFormato.cuentaOculta(cuenta.obtenerNumeroCuenta())); num.setForeground(new Color(0xE2E8F0)); num.setFont(Estilos.FUENTE_NORMAL);
        p.add(marca); p.add(Box.createVerticalStrut(12)); p.add(tipo); p.add(Box.createVerticalStrut(12)); p.add(saldoLbl); p.add(Box.createVerticalStrut(8)); p.add(num); p.add(Box.createVerticalStrut(8)); p.add(toggle);
        return p;
    }

    private JButton accion(String texto, Runnable r) { BotonPrimario b = new BotonPrimario(texto); b.setPreferredSize(new Dimension(150,44)); b.addActionListener(e -> r.run()); return b; }

    private JPanel filaMovimiento(Transaccion t, String cuenta) {
        JPanel p = tarjetaBlanca(); p.setLayout(new BorderLayout()); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62)); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        boolean ingreso = cuenta.equals(t.obtenerCuentaDestino());
        boolean rechazado = t.obtenerEstado() == EstadoTransaccion.RECHAZADA;
        Color color = rechazado ? Estilos.ADVERTENCIA : (ingreso ? Estilos.EXITO : Estilos.ERROR);
        String signo = rechazado ? "" : (ingreso ? "+" : "-");
        JLabel izq = new JLabel(t.obtenerTipo() + " · " + UtilFormato.fecha(t.obtenerFechaHora())); izq.setForeground(Estilos.TEXTO);
        JLabel der = new JLabel(signo + UtilFormato.moneda(t.obtenerMonto()) + (rechazado ? " ("+t.obtenerEstado()+")" : ""));
        der.setForeground(color); der.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD));
        p.add(izq, BorderLayout.WEST); p.add(der, BorderLayout.EAST); return p;
    }

    private JPanel tarjetaBlanca() { JPanel p = new JPanel(); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), new EmptyBorder(14,16,14,16))); return p; }
    private void mostrarError(Exception ex) { cuerpo.add(new EstadoVacio("No se pudo cargar la información", ex.getMessage() == null ? "Error inesperado." : ex.getMessage())); }
}
