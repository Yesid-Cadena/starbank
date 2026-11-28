package com.starbank.vista.paneles;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.SesionControlador;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.entidades.Transaccion;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class PanelDepositoCliente extends JPanel implements PanelPrincipalCliente.Refrescable {
    private final ConfiguracionServicios config; private final SesionControlador sesion;
    private final EncabezadoModulo encabezado = new EncabezadoModulo("Depositar", "Realiza un depósito en una de tus cuentas");
    private final JComboBox<String> cbCuenta = new JComboBox<>();
    private final CampoTexto monto = new CampoTexto("Monto en COP");
    private final CampoTexto descripcion = new CampoTexto("Descripción del depósito");
    private final JLabel mensaje = new JLabel(" ");
    private final BotonPrimario btn = new BotonPrimario("Confirmar depósito");
    private List<Cuenta> cuentas = List.of();

    public PanelDepositoCliente(ConfiguracionServicios config, SesionControlador sesion) {
        this.config = config; this.sesion = sesion;
        setLayout(new BorderLayout()); setBackground(Estilos.FONDO);
        add(encabezado, BorderLayout.NORTH);
        JPanel form = new JPanel(); form.setOpaque(false); form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS)); form.setBorder(new EmptyBorder(24,24,24,24)); form.setMaximumSize(new Dimension(720, Integer.MAX_VALUE));
        form.add(label("Cuenta destino")); form.add(cbCuenta); form.add(Box.createVerticalStrut(12));
        form.add(label("Monto (COP)")); form.add(monto); form.add(Box.createVerticalStrut(12));
        form.add(label("Descripción")); form.add(descripcion); form.add(Box.createVerticalStrut(12));
        mensaje.setForeground(Estilos.ERROR); form.add(mensaje); form.add(Box.createVerticalStrut(12));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,52)); btn.addActionListener(e -> ejecutar()); form.add(btn);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(form, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap); sp.setBorder(null); sp.getViewport().setBackground(Estilos.FONDO); add(sp, BorderLayout.CENTER);
    }

    private JLabel label(String t) { JLabel l = new JLabel(t); l.setFont(Estilos.FUENTE_NORMAL.deriveFont(Font.BOLD)); l.setForeground(Estilos.TEXTO); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }

    @Override public void refrescar() {
        try {
            encabezado.setUsuario(sesion.usuarioActual().obtenerNombre());
            cuentas = config.obtenerCuentas().consultarPorCliente(sesion.getSesion());
            cbCuenta.removeAllItems();
            for (Cuenta c : cuentas) cbCuenta.addItem(c.obtenerNumeroCuenta() + " - " + c.obtenerTipoCuenta() + " (" + UtilFormato.moneda(c.consultarSaldo()) + ")");
            if (cuentas.isEmpty()) mensaje.setText("No tienes cuentas disponibles.");
        } catch (Exception ex) { mensaje.setForeground(Estilos.ERROR); mensaje.setText(ex.getMessage()); }
    }

    private void ejecutar() {
        if (cbCuenta.getSelectedIndex() < 0) { mensaje.setForeground(Estilos.ERROR); mensaje.setText("Selecciona una cuenta."); return; }
        Cuenta cuenta = cuentas.get(cbCuenta.getSelectedIndex());
        BigDecimal valor;
        try { valor = new BigDecimal(monto.getText().trim().replace(".", "").replace(",", ".")); } catch (Exception e) { mensaje.setForeground(Estilos.ERROR); mensaje.setText("Monto inválido."); return; }
        int conf = JOptionPane.showConfirmDialog(this, "Confirmar depósito de " + UtilFormato.moneda(valor) + " en la cuenta " + cuenta.obtenerNumeroCuenta() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;
        btn.setEnabled(false);
        new SwingWorker<Transaccion, Void>() {
            Exception err;
            @Override protected Transaccion doInBackground() { try { return config.obtenerTransacciones().depositar(sesion.getSesion(), cuenta.obtenerNumeroCuenta(), valor, descripcion.getText()); } catch (Exception ex) { err = ex; return null; } }
            @Override protected void done() { btn.setEnabled(true); if (err != null) { mensaje.setForeground(Estilos.ERROR); mensaje.setText(err.getMessage()); } else { try { Transaccion t = get(); mensaje.setForeground(Estilos.EXITO); mensaje.setText("Depósito exitoso. Comprobante: " + t.obtenerIdentificador()); monto.setText(""); descripcion.setText(""); refrescar(); } catch (Exception ex) { mensaje.setForeground(Estilos.ERROR); mensaje.setText(ex.getMessage()); } } }
        }.execute();
    }
}
