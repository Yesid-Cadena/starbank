package com.starbank.excepciones;

public class ValidacionException extends IllegalArgumentException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
