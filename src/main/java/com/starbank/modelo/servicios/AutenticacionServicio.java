package com.starbank.modelo.servicios;

import com.starbank.excepciones.AutenticacionException;
import com.starbank.modelo.entidades.Sesion;
import com.starbank.modelo.entidades.Usuario;
import com.starbank.modelo.enumeraciones.EstadoUsuario;
import com.starbank.modelo.repositorios.UnidadTrabajo;
import com.starbank.modelo.repositorios.UsuarioRepositorio;
import com.starbank.utilidades.Validaciones;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class AutenticacionServicio {
    private static final String CREDENCIALES_INVALIDAS = "Credenciales inválidas.";
    private static final String ACCESO_TEMPORAL = "Acceso temporalmente bloqueado. Intente más tarde.";
    private final UsuarioRepositorio usuarios;
    private final SesionServicio sesiones;
    private final AuditoriaServicio auditoria;
    private final UnidadTrabajo unidadTrabajo;
    private final Clock reloj;
    private final int maxIntentos;
    private final Duration bloqueoTemporal;
    private final Map<String, IntentoFallido> intentos = new HashMap<>();

    private static final class IntentoFallido {
        private int cantidad;
        private LocalDateTime bloqueadoHasta;
        private IntentoFallido(int cantidad, LocalDateTime bloqueadoHasta) {
            this.cantidad = cantidad;
            this.bloqueadoHasta = bloqueadoHasta;
        }
    }

    public AutenticacionServicio(UsuarioRepositorio usuarios, SesionServicio sesiones,
                                 AuditoriaServicio auditoria, UnidadTrabajo unidadTrabajo) {
        this(usuarios, sesiones, auditoria, unidadTrabajo, Clock.systemDefaultZone(), 5, Duration.ofMinutes(5));
    }

    public AutenticacionServicio(UsuarioRepositorio usuarios, SesionServicio sesiones,
                                 AuditoriaServicio auditoria, UnidadTrabajo unidadTrabajo,
                                 Clock reloj, int maxIntentos, Duration bloqueoTemporal) {
        this.usuarios = Validaciones.obligatorio(usuarios, "El repositorio de usuarios");
        this.sesiones = Validaciones.obligatorio(sesiones, "El servicio de sesiones");
        this.auditoria = Validaciones.obligatorio(auditoria, "El servicio de auditoría");
        this.unidadTrabajo = Validaciones.obligatorio(unidadTrabajo, "La unidad de trabajo");
        this.reloj = Validaciones.obligatorio(reloj, "El reloj");
        this.maxIntentos = maxIntentos;
        this.bloqueoTemporal = Validaciones.obligatorio(bloqueoTemporal, "El bloqueo temporal");
    }

    public Sesion iniciarSesion(String correo, char[] clave) {
        String correoNormalizado = normalizarCorreo(correo);
        validarNoBloqueado(correoNormalizado);
        try {
            Usuario usuario = usuarios.buscarPorCorreo(correoNormalizado).orElse(null);
            if (usuario == null || !usuario.verificarClave(clave) || usuario.obtenerEstado() != EstadoUsuario.ACTIVO) {
                registrarFallo(correoNormalizado, usuario);
                throw new AutenticacionException(CREDENCIALES_INVALIDAS);
            }
            limpiarIntentos(correoNormalizado);
            Sesion sesion = sesiones.crear(usuario.obtenerIdentificacion());
            auditoria.registrar(usuario.obtenerIdentificacion(), "INICIO_SESION", "EXITOSO",
                    "Inicio de sesión exitoso.", usuario.obtenerIdentificacion());
            return sesion;
        } finally {
            borrarClave(clave);
        }
    }

    public void cerrarSesion(String idSesion) {
        String usuario = null;
        try {
            usuario = sesiones.validar(idSesion).obtenerIdentificacionUsuario();
        } catch (RuntimeException ignorada) {
            // Se invalida si existe; no exponer detalles.
        }
        sesiones.invalidar(idSesion);
        if (usuario != null) {
            auditoria.registrar(usuario, "CIERRE_SESION", "EXITOSO", "Cierre de sesión.", usuario);
        }
    }

    public Sesion cambiarClave(String idSesion, char[] claveActual, char[] claveNueva) {
        try {
            String identificacion = sesiones.validar(idSesion).obtenerIdentificacionUsuario();
            unidadTrabajo.ejecutar(() -> {
                Usuario usuario = usuarios.buscarPorIdentificacion(identificacion)
                        .orElseThrow(() -> new AutenticacionException(CREDENCIALES_INVALIDAS));
                if (usuario.obtenerEstado() != EstadoUsuario.ACTIVO || !usuario.verificarClave(claveActual)) {
                    auditoria.registrar(identificacion, "CAMBIO_CLAVE", "RECHAZADO",
                            "Cambio de contraseña rechazado.", identificacion);
                    throw new AutenticacionException(CREDENCIALES_INVALIDAS);
                }
                usuario.cambiarClave(claveActual, claveNueva);
                usuarios.actualizar(usuario);
                return null;
            });
            sesiones.invalidarPorUsuario(identificacion);
            Sesion nuevaSesion = sesiones.crear(identificacion);
            auditoria.registrar(identificacion, "CAMBIO_CLAVE", "EXITOSO",
                    "Contraseña actualizada; sesiones anteriores invalidadas.", identificacion);
            return nuevaSesion;
        } finally {
            borrarClave(claveActual);
            borrarClave(claveNueva);
        }
    }

    private void validarNoBloqueado(String correoNormalizado) {
        synchronized (intentos) {
            IntentoFallido intento = intentos.get(correoNormalizado);
            if (intento == null || intento.bloqueadoHasta == null) {
                return;
            }
            LocalDateTime ahora = LocalDateTime.now(reloj);
            if (ahora.isBefore(intento.bloqueadoHasta)) {
                auditoria.registrar(null, "INICIO_SESION", "RECHAZADO",
                        "Inicio de sesión bloqueado temporalmente.", null);
                throw new AutenticacionException(ACCESO_TEMPORAL);
            }
            intentos.remove(correoNormalizado);
        }
    }

    private void registrarFallo(String correoNormalizado, Usuario usuario) {
        synchronized (intentos) {
            IntentoFallido intento = intentos.computeIfAbsent(correoNormalizado, c -> new IntentoFallido(0, null));
            intento.cantidad++;
            String identificacion = usuario == null ? null : usuario.obtenerIdentificacion();
            if (intento.cantidad >= maxIntentos) {
                intento.bloqueadoHasta = LocalDateTime.now(reloj).plus(bloqueoTemporal);
                auditoria.registrar(identificacion, "INICIO_SESION", "RECHAZADO",
                        "Inicio de sesión rechazado; límite temporal aplicado.", identificacion);
            } else {
                auditoria.registrar(identificacion, "INICIO_SESION", "RECHAZADO",
                        "Inicio de sesión rechazado.", identificacion);
            }
        }
    }

    private void limpiarIntentos(String correoNormalizado) {
        synchronized (intentos) { intentos.remove(correoNormalizado); }
    }

    private String normalizarCorreo(String correo) {
        return Validaciones.correo(correo).toLowerCase();
    }

    private static void borrarClave(char[] clave) {
        if (clave != null) { Arrays.fill(clave, '\0'); }
    }
}
