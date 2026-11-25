package com.starbank.excepciones;

public class SesionInvalidaException extends AutenticacionException {
    public SesionInvalidaException(String mensaje) { super(mensaje); }
}
