package com.starbank.modelo.repositorios;

import java.util.function.Supplier;

/** Ejecuta sobre repositorios del mismo contexto: confirma todo o revierte todo ante un fallo. */
public interface UnidadTrabajo {
    <T> T ejecutar(Supplier<T> operacion);
}
