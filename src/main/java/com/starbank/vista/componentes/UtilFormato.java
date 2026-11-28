package com.starbank.vista.componentes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class UtilFormato {
    private static final Locale CO = Locale.forLanguageTag("es-CO");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(CO);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private UtilFormato() {}
    public static String moneda(BigDecimal valor) { return valor == null ? "$ 0" : MONEDA.format(valor); }
    public static String fecha(LocalDateTime f) { return f == null ? "" : FECHA.format(f); }
    public static String cuentaOculta(String numero) {
        if (numero == null || numero.length() < 4) return "****";
        return "•••• " + numero.substring(numero.length() - 4);
    }
    public static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "SB";
        String[] p = nombre.trim().split("\\s+");
        String a = p[0].substring(0,1).toUpperCase();
        String b = p.length > 1 ? p[p.length-1].substring(0,1).toUpperCase() : "";
        return a + b;
    }
}
