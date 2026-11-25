package com.starbank.utilidades;

import com.starbank.excepciones.ValidacionException;
import java.math.BigDecimal;

public final class Validaciones {
    public static BigDecimal monto(BigDecimal monto) {
        obligatorio(monto, "El monto");
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El monto debe ser mayor que cero.");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new ValidacionException("El monto debe tener como máximo dos decimales.");
        }
        return monto;
    }

    public static BigDecimal montoNoNegativo(BigDecimal monto) {
        obligatorio(monto, "El monto");
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El monto no puede ser negativo.");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new ValidacionException("El monto debe tener como máximo dos decimales.");
        }
        return monto;
    }

    private Validaciones() {
    }

    public static <T> T obligatorio(T valor, String atributo) {
        if (valor == null) {
            throw new ValidacionException(atributo + " es obligatorio.");
        }
        return valor;
    }

    public static String texto(String valor, String atributo) {
        obligatorio(valor, atributo);
        if (valor.isBlank()) {
            throw new ValidacionException(atributo + " no puede estar vacío.");
        }
        return valor.strip();
    }

    public static String correo(String valor) {
        String correo = texto(valor, "El correo");
        if (!correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
        return correo;
    }

    public static String telefono(String valor) {
        String telefono = texto(valor, "El teléfono");
        if (!telefono.matches("\\+?[0-9 ()-]+")
                || telefono.replaceAll("[^0-9]", "").length() < 7
                || telefono.replaceAll("[^0-9]", "").length() > 15) {
            throw new ValidacionException("El teléfono debe contener entre 7 y 15 dígitos.");
        }
        return telefono;
    }
}
