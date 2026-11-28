package com.starbank.vista.paneles;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.SesionControlador;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PanelMovimientosCliente extends JPanel implements PanelPrincipalCliente.Refrescable {
    private final ConfiguracionServicios config; private final SesionControlador sesion;
    private final EncabezadoModulo encabezado = new EncabezadoModulo("Movimientos", "Consulta el historial de tus cuentas");
    private final JComboBox<String> cbCuenta = new JComboBox<>();
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Todos", "DEPOSITO", "RETIRO", "TRANSFERENCIA"});
    private final JPanel lista = new JPanel();
    private List<Cuenta> cuentas = List.of();

    public PanelMovimientosCliente(ConfiguracionServicios config, SesionControlador sesion) {
        this.config = config; this.sesion = sesion;
        setLayout(new BorderLayout()); setBackground(Estilos.FONDO); add(encabezado, BorderLayout.NORTH);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); top.setOpaque(false); top.setBorder(new EmptyBorder(16,24,0,24));
        top.add(new JLabel("Cuenta:")); top.add(cbCuenta); top.add(new JLabel("Tipo:")); top.add(cbTipo);
        BotonPrimario filtrar = new BotonPrimario("Consultar"); filtrar.setPreferredSize(new Dimension(140,40)); filtrar.addActionListener(e -> cargar()); top.add(filtrar);
        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false); north.add(top, BorderLayout.CENTER);
        add(north, BorderLayout.BEFORE_FIRST_LINE);
        lista.setOpaque(false); lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS)); lista.setBorder(new EmptyBorder(16,24,24,24));
        JScrollPane sp = new JScrollPane(lista); sp.setBorder(null); sp.getViewport().setBackground(Estilos.FONDO); sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    @Override public void refrescar() {
        try {
            encabezado.setUsuario(sesion.usuarioActual().obtenerNombre());
            cuentas = config.obtenerCuentas().consultarPorCliente(sesion.getSesion());
            cbCuenta.removeAllItems(); for (Cuenta c : cuentas) cbCuenta.addItem(c.obtenerNumeroCuenta());
            if (cuentas.isEmpty()) { lista.removeAll(); lista.add(new EstadoVacio("Sin cuentas", "No hay cuentas para consultar movimientos.")); lista.revalidate(); lista.repaint(); }
            else cargar();
        } catch (Exception ex) { lista.removeAll(); lista.add(new EstadoVacio("Error", ex.getMessage())); lista.revalidate(); lista.repaint(); }
    }

    private void cargar() {
        lista.removeAll();
        try {
            if (cbCuenta.getSelectedIndex() < 0) return;
            String numero = cuentas.get(cbCuenta.getSelectedIndex()).obtenerNumeroCuenta();
            TipoTransaccion tipo = cbTipo.getSelectedIndex() <= 0 ? null : TipoTransaccion.valueOf((String) cbTipo.getSelectedItem());
            List<Transaccion> movs = config.obtenerTransacciones().historialCuenta(sesion.getSesion(), numero, tipo, null, null);
            if (movs.isEmpty()) lista.add(new EstadoVacio("Sin movimientos", "No se encontraron movimientos con los filtros seleccionados."));
            for (Transaccion t : movs) {
                JPanel p = new JPanel(new BorderLayout()); p.setBackground(Color.WHITE);
                p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), new EmptyBorder(12,14,12,14)));
                p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72)); p.setAlignmentX(Component.LEFT_ALIGNMENT);
                boolean ingreso = numero.equals(t.obtenerCuentaDestino());
                boolean rechazado = t.obtenerEstado() == EstadoTransaccion.RECHAZADA;
                Color color = rechazado ? Estilos.ADVERTENCIA : (ingreso ? Estilos.EXITO : Estilos.ERROR);
                String signo = rechazado ? "" : (ingreso ? "+" : "-");
                JLabel izq = new JLabel("<html><b>" + t.obtenerTipo() + "</b> · " + t.obtenerEstado() + "<br><span style='color:#64748B'>" + UtilFormato.fecha(t.obtenerFechaHora()) + " · " + t.obtenerDescripcion() + "</span></html>");
                JLabel der = new JLabel(signo + UtilFormato.moneda(t.obtenerMonto()));
                der.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD)); der.setForeground(color);
                p.add(izq, BorderLayout.CENTER); p.add(der, BorderLayout.EAST);
                lista.add(p); lista.add(Box.createVerticalStrut(8));
            }
        } catch (Exception ex) { lista.add(new EstadoVacio("Error", ex.getMessage())); }
        lista.revalidate(); lista.repaint();
    }
}
