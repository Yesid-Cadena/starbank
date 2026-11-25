package com.starbank.modelo.repositorios;

import com.starbank.modelo.entidades.RegistroActividad;
import java.util.List;

public interface ActividadRepositorio {
    void guardar(RegistroActividad registro);
    List<RegistroActividad> listar();
}
