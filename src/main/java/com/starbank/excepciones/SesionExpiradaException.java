package com.starbank.excepciones;

public class SesionExpiradaException extends AutenticacionException {
    public SesionExpiradaException(String mensaje) { super(mensaje); }
}
