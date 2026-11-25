package com.starbank.modelo.repositorios.memoria;

import com.starbank.excepciones.RegistroDuplicadoException;
import com.starbank.excepciones.RegistroNoEncontradoException;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.repositorios.SesionRepositorio;
import com.starbank.utilidades.Validaciones;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SesionRepositorioMemoria implements SesionRepositorio {
    private final Map<String, Sesion> sesiones = new LinkedHashMap<>();

    @Override
    public synchronized void guardar(Sesion sesion) {
        Validaciones.obligatorio(sesion, "La sesión");
        if (sesiones.containsKey(sesion.obtenerIdentificador())) {
            throw new RegistroDuplicadoException("La sesión ya existe.");
        }
        sesiones.put(sesion.obtenerIdentificador(), sesion);
    }

    @Override
    public synchronized void actualizar(Sesion sesion) {
        Validaciones.obligatorio(sesion, "La sesión");
        if (!sesiones.containsKey(sesion.obtenerIdentificador())) {
            throw new RegistroNoEncontradoException("La sesión no existe.");
        }
        sesiones.put(sesion.obtenerIdentificador(), sesion);
    }

    @Override
    public synchronized Optional<Sesion> buscarPorIdentificador(String identificador) {
        String id = Validaciones.texto(identificador, "El identificador de sesión");
        return Optional.ofNullable(sesiones.get(id));
    }

    @Override
    public synchronized List<Sesion> buscarPorUsuario(String identificacionUsuario) {
        String id = Validaciones.texto(identificacionUsuario, "El identificador de usuario");
        return sesiones.values().stream().filter(s -> s.obtenerIdentificacionUsuario().equals(id)).toList();
    }

    @Override
    public synchronized List<Sesion> listar() {
        return List.copyOf(new ArrayList<>(sesiones.values()));
    }
}
