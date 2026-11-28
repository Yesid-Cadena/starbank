package com.starbank.vista.paneles;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.SesionControlador;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PanelCuentasCliente extends JPanel implements PanelPrincipalCliente.Refrescable {
    private final ConfiguracionServicios config; private final SesionControlador sesion;
    private final EncabezadoModulo encabezado = new EncabezadoModulo("Mis cuentas", "Consulta tus cuentas y su estado");
    private final JPanel cuerpo = new JPanel();

    public PanelCuentasCliente(ConfiguracionServicios config, SesionControlador sesion) {
        this.config = config; this.sesion = sesion;
        setLayout(new BorderLayout()); setBackground(Estilos.FONDO);
        add(encabezado, BorderLayout.NORTH);
        cuerpo.setOpaque(false); cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS)); cuerpo.setBorder(new EmptyBorder(24,24,24,24));
        JScrollPane sp = new JScrollPane(cuerpo); sp.setBorder(null); sp.getViewport().setBackground(Estilos.FONDO); sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    @Override public void refrescar() {
        cuerpo.removeAll();
        try {
            encabezado.setUsuario(sesion.usuarioActual().obtenerNombre());
            List<Cuenta> cuentas = config.obtenerCuentas().consultarPorCliente(sesion.getSesion());
            if (cuentas.isEmpty()) {
                cuerpo.add(new EstadoVacio("No tienes cuentas registradas", "Cuando un administrador habilite una cuenta, aparecerá aquí."));
            } else {
                for (Cuenta c : cuentas) {
                    JPanel card = new JPanel(new BorderLayout());
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), new EmptyBorder(16,18,16,18)));
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
                    card.setAlignmentX(Component.LEFT_ALIGNMENT);
                    boolean activa = c.obtenerEstado().name().equals("ACTIVA");
                    JLabel izq = new JLabel("<html><b>" + c.obtenerTipoCuenta() + "</b> · " + UtilFormato.cuentaOculta(c.obtenerNumeroCuenta())
                            + "<br><span style='color:#64748B'>Estado: " + c.obtenerEstado() + " · Creada: " + UtilFormato.fecha(c.obtenerFechaCreacion()) + "</span></html>");
                    izq.setForeground(Estilos.TEXTO);
                    JLabel saldo = new JLabel(UtilFormato.moneda(c.consultarSaldo()));
                    saldo.setFont(new Font("SansSerif", Font.BOLD, 22));
                    saldo.setForeground(activa ? Estilos.TEXTO : Estilos.TEXTO_SECUNDARIO);
                    card.add(izq, BorderLayout.CENTER); card.add(saldo, BorderLayout.EAST);
                    cuerpo.add(card); cuerpo.add(Box.createVerticalStrut(12));
                }
            }
        } catch (Exception ex) { cuerpo.add(new EstadoVacio("Error al cargar cuentas", ex.getMessage() == null ? "Error inesperado." : ex.getMessage())); }
        cuerpo.revalidate(); cuerpo.repaint();
    }
}
