package com.starbank.modelo.repositorios.memoria;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.modelo.entidades.RegistroActividad;
import com.starbank.modelo.repositorios.ActividadRepositorio;
import com.starbank.utilidades.Validaciones;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Almacenamiento independiente: los registros sobreviven reversiones de UnidadTrabajo. */
public final class ActividadRepositorioMemoria implements ActividadRepositorio {
    private final Map<String, RegistroActividad> registros = new LinkedHashMap<>();

    @Override
    public synchronized void guardar(RegistroActividad registro) {
        Validaciones.obligatorio(registro, "El registro");
        if (registros.containsKey(registro.obtenerIdentificador())) {
            throw new RegistroDuplicadoException("El registro de actividad ya existe.");
        }
        registros.put(registro.obtenerIdentificador(), registro);
    }

    @Override
    public synchronized List<RegistroActividad> listar() {
        return List.copyOf(new ArrayList<>(registros.values()));
    }
}
