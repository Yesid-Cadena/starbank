package com.starbank.modelo.entidades;

import com.starbank.excepciones.ValidacionException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ClaveProtegidaTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"corta", "        "})
    void rechazaClavesInvalidas(String clave) {
        assertThrows(ValidacionException.class,
                () -> ClaveProtegida.crear(clave == null ? null : clave.toCharArray()));
    }

    @Test
    void rechazaClaveDemasiadoLarga() {
        assertThrows(ValidacionException.class,
                () -> ClaveProtegida.crear("a".repeat(129).toCharArray()));
    }

    @Test
    void limpiarLaEntradaNoAlteraLaClaveProtegida() {
        char[] entrada = "Ejemplo-seguro-21".toCharArray();
        ClaveProtegida clave = ClaveProtegida.crear(entrada);
        Arrays.fill(entrada, '\0');
        assertTrue(clave.coincide("Ejemplo-seguro-21".toCharArray()));
        assertFalse(clave.coincide(entrada));
        assertFalse(clave.coincide(null));
        assertFalse(clave.coincide(new char[0]));
        assertFalse(clave.coincide(new char[129]));
        assertFalse(clave.toString().contains("Ejemplo-seguro-21"));
    }
}
