package com.starbank.modelo.servicios;

import com.starbank.modelo.entidades.RegistroActividad;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.repositorios.ActividadRepositorio;
import com.starbank.utilidades.Validaciones;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class AuditoriaServicio {
    private final ActividadRepositorio registros;
    private final Clock reloj;

    public AuditoriaServicio(ActividadRepositorio registros) {
        this(registros, Clock.systemDefaultZone());
    }

    public AuditoriaServicio(ActividadRepositorio registros, Clock reloj) {
        this.registros = Validaciones.obligatorio(registros, "El repositorio de auditoría");
        this.reloj = Validaciones.obligatorio(reloj, "El reloj");
    }

    public RegistroActividad registrar(String identificacionUsuario, String tipoActividad,
                                       String resultado, String descripcion, String recursoAfectado) {
        RegistroActividad registro = new RegistroActividad(
                UUID.randomUUID().toString(),
                identificacionUsuario,
                tipoActividad,
                LocalDateTime.now(reloj),
                resultado,
                descripcion,
                recursoAfectado);
        registros.guardar(registro);
        return registro;
    }

    List<RegistroActividad> listarInterno() {
        return registros.listar();
    }

    public List<RegistroActividad> listar(Sesion sesion, AutorizacionServicio autorizacion) {
        Validaciones.obligatorio(autorizacion, "El servicio de autorización").exigirAdministrador(sesion.obtenerIdentificador());
        return registros.listar();
    }
}
