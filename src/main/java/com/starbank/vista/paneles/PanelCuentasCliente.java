package com.starbank.vista.paneles;

import com.starbank.configuracion.ConfiguracionServicios;
import com.starbank.controlador.SesionControlador;
import com.starbank.modelo.entidades.Cuenta;
import com.starbank.modelo.enumeraciones.TipoCuenta;
import com.starbank.vista.componentes.*;
import com.starbank.vista.estilos.Estilos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class PanelCuentasCliente extends JPanel implements PanelPrincipalCliente.Refrescable {
    private final ConfiguracionServicios config; private final SesionControlador sesion;
    private final EncabezadoModulo encabezado = new EncabezadoModulo("Mis cuentas", "Consulta y administra tus cuentas bancarias");
    private final JPanel cuerpo = new JPanel();
    private final BotonPrimario btnAbrir = new BotonPrimario("+ Abrir cuenta bancaria");

    public PanelCuentasCliente(ConfiguracionServicios config, SesionControlador sesion) {
        this.config = config; this.sesion = sesion;
        setLayout(new BorderLayout()); setBackground(Estilos.FONDO);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(encabezado, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 18)); acciones.setOpaque(false); acciones.setBackground(Color.WHITE);
        btnAbrir.setPreferredSize(new Dimension(240, 46)); btnAbrir.addActionListener(e -> abrirCuenta());
        top.add(acciones, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        // put button inside header area by overlay: simpler to add below header
        cuerpo.setOpaque(false); cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS)); cuerpo.setBorder(new EmptyBorder(24,24,24,24));
        JScrollPane sp = new JScrollPane(cuerpo); sp.setBorder(null); sp.getViewport().setBackground(Estilos.FONDO); sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    @Override public void refrescar() {
        cuerpo.removeAll();
        try {
            var usuario = sesion.usuarioActual();
            encabezado.setUsuario(usuario.obtenerNombre());
            JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); barra.setOpaque(false); barra.setAlignmentX(Component.LEFT_ALIGNMENT);
            barra.add(btnAbrir); barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            cuerpo.add(barra); cuerpo.add(Box.createVerticalStrut(18));

            List<Cuenta> cuentas = config.obtenerCuentas().consultarPorCliente(sesion.getSesion());
            BigDecimal total = cuentas.stream().filter(c -> c.obtenerEstado().name().equals("ACTIVA")).map(Cuenta::consultarSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
            JPanel resumen = tarjeta(); resumen.setLayout(new BorderLayout()); resumen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); resumen.setAlignmentX(Component.LEFT_ALIGNMENT);
            resumen.add(new JLabel("Saldo total disponible (cuentas habilitadas): " + UtilFormato.moneda(total)), BorderLayout.WEST);
            resumen.add(new JLabel("Cuentas: " + cuentas.size()), BorderLayout.EAST);
            cuerpo.add(resumen); cuerpo.add(Box.createVerticalStrut(16));

            if (cuentas.isEmpty()) {
                EstadoVacio vacio = new EstadoVacio("Aún no tienes cuentas bancarias", "Abre tu primera cuenta para comenzar a usar StarBank.");
                vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
                cuerpo.add(vacio);
            } else {
                for (Cuenta c : cuentas) {
                    cuerpo.add(tarjetaCuenta(c));
                    cuerpo.add(Box.createVerticalStrut(12));
                }
            }
        } catch (Exception ex) { cuerpo.add(new EstadoVacio("Error al cargar cuentas", ex.getMessage() == null ? "Error inesperado." : ex.getMessage())); }
        cuerpo.revalidate(); cuerpo.repaint();
    }

    private JPanel tarjetaCuenta(Cuenta c) {
        JPanel card = tarjeta(); card.setLayout(new BorderLayout(12, 8));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130)); card.setAlignmentX(Component.LEFT_ALIGNMENT);
        boolean activa = c.obtenerEstado().name().equals("ACTIVA");
        String estadoHtml = activa ? "<span style='color:#16A34A'>ACTIVA</span>" : "<span style='color:#D97706'>" + c.obtenerEstado() + "</span>";
        JLabel izq = new JLabel("<html><b>" + c.obtenerTipoCuenta() + "</b><br>"
                + "Número: " + c.obtenerNumeroCuenta() + "<br>"
                + "<span style='color:#64748B'>Estado: " + estadoHtml + " · Creada: " + UtilFormato.fecha(c.obtenerFechaCreacion()) + "</span></html>");
        izq.setForeground(Estilos.TEXTO);
        JPanel der = new JPanel(); der.setOpaque(false); der.setLayout(new BoxLayout(der, BoxLayout.Y_AXIS));
        JLabel saldo = new JLabel(UtilFormato.moneda(c.consultarSaldo())); saldo.setFont(new Font("SansSerif", Font.BOLD, 22)); saldo.setForeground(activa ? Estilos.TEXTO : Estilos.TEXTO_SECUNDARIO); saldo.setAlignmentX(Component.RIGHT_ALIGNMENT);
        BotonEnlace copiar = new BotonEnlace("Copiar número"); copiar.setAlignmentX(Component.RIGHT_ALIGNMENT);
        copiar.addActionListener(e -> { Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(c.obtenerNumeroCuenta()), null); JOptionPane.showMessageDialog(this, "Número de cuenta copiado."); });
        der.add(saldo); der.add(Box.createVerticalStrut(6)); der.add(copiar);
        card.add(izq, BorderLayout.CENTER); card.add(der, BorderLayout.EAST);
        return card;
    }

    private void abrirCuenta() {
        try {
            var usuario = sesion.usuarioActual();
            TipoCuenta[] tipos = TipoCuenta.values();
            JComboBox<TipoCuenta> cbTipo = new JComboBox<>(tipos);
            JLabel titular = new JLabel(usuario.obtenerNombre());
            JLabel saldoInicial = new JLabel(UtilFormato.moneda(BigDecimal.ZERO));
            JLabel infoNumero = new JLabel("Se generará automáticamente al confirmar.");
            JLabel desc = new JLabel(" ");
            Runnable actualizarDesc = () -> { TipoCuenta t = (TipoCuenta) cbTipo.getSelectedItem(); desc.setText(t == null ? " " : "Tipo seleccionado: " + t.name()); };
            actualizarDesc.run();
            cbTipo.addActionListener(e -> actualizarDesc.run());

            JPanel form = new JPanel(new GridBagLayout()); form.setBorder(new EmptyBorder(8,8,8,8));
            GridBagConstraints gc = new GridBagConstraints(); gc.insets = new Insets(6,6,6,6); gc.anchor = GridBagConstraints.WEST; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            int y=0;
            gc.gridx=0; gc.gridy=y; form.add(new JLabel("Tipo de cuenta:"), gc); gc.gridx=1; form.add(cbTipo, gc); y++;
            gc.gridx=0; gc.gridy=y; form.add(new JLabel("Descripción:"), gc); gc.gridx=1; form.add(desc, gc); y++;
            gc.gridx=0; gc.gridy=y; form.add(new JLabel("Titular:"), gc); gc.gridx=1; form.add(titular, gc); y++;
            gc.gridx=0; gc.gridy=y; form.add(new JLabel("Saldo inicial:"), gc); gc.gridx=1; form.add(saldoInicial, gc); y++;
            gc.gridx=0; gc.gridy=y; form.add(new JLabel("Número de cuenta:"), gc); gc.gridx=1; form.add(infoNumero, gc); y++;

            int op = JOptionPane.showConfirmDialog(this, form, "Abrir cuenta bancaria", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op != JOptionPane.OK_OPTION) return;
            TipoCuenta tipo = (TipoCuenta) cbTipo.getSelectedItem();
            btnAbrir.setEnabled(false);
            new SwingWorker<Cuenta, Void>() {
                Exception err;
                @Override protected Cuenta doInBackground() {
                    try { return config.obtenerCuentas().abrirCuentaPropia(sesion.getSesion(), tipo); }
                    catch (Exception ex) { err = ex; return null; }
                }
                @Override protected void done() {
                    btnAbrir.setEnabled(true);
                    if (err != null) { JOptionPane.showMessageDialog(PanelCuentasCliente.this, err.getMessage(), "No se pudo abrir la cuenta", JOptionPane.ERROR_MESSAGE); }
                    else { try { Cuenta c = get(); JOptionPane.showMessageDialog(PanelCuentasCliente.this, "Cuenta creada exitosamente.\nNúmero: " + c.obtenerNumeroCuenta(), "Cuenta abierta", JOptionPane.INFORMATION_MESSAGE); refrescar(); } catch (Exception ex) { JOptionPane.showMessageDialog(PanelCuentasCliente.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); } }
                }
            }.execute();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private JPanel tarjeta() { JPanel p = new JPanel(); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), new EmptyBorder(14,16,14,16))); return p; }
}
