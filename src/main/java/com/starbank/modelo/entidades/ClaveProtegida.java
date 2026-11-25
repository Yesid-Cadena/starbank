package com.starbank.modelo.entidades;

import com.starbank.excepciones.ValidacionException;
import com.starbank.utilidades.Validaciones;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Valor inmutable que conserva únicamente sal y derivación, nunca la clave original. */
public final class ClaveProtegida {
    private static final int ITERACIONES = 600_000;
    private final byte[] sal;
    private final byte[] resumen;

    private ClaveProtegida(char[] clave) {
        sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        resumen = derivar(clave, sal);
    }

    /** El llamador conserva la responsabilidad de limpiar el arreglo recibido. */
    public static ClaveProtegida crear(char[] clave) {
        Validaciones.obligatorio(clave, "La contraseña");
        if (clave.length < 8 || clave.length > 128) {
            throw new ValidacionException("La contraseña debe tener entre 8 y 128 caracteres.");
        }
        boolean soloEspacios = true;
        for (char caracter : clave) {
            soloEspacios &= Character.isWhitespace(caracter);
        }
        if (soloEspacios) {
            throw new ValidacionException("La contraseña no puede contener solo espacios.");
        }
        return new ClaveProtegida(clave);
    }

    public boolean coincide(char[] candidata) {
        if (candidata == null || candidata.length < 8 || candidata.length > 128) {
            return false;
        }
        byte[] resultado = derivar(candidata, sal);
        try {
            return MessageDigest.isEqual(resumen, resultado);
        } finally {
            Arrays.fill(resultado, (byte) 0);
        }
    }

    private static byte[] derivar(char[] clave, byte[] sal) {
        PBEKeySpec especificacion = new PBEKeySpec(clave, sal, ITERACIONES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException excepcion) {
            throw new IllegalStateException("No se pudo proteger la contraseña.", excepcion);
        } finally {
            especificacion.clearPassword();
        }
    }
}
