package com.starbank.configuracion;

import com.starbank.excepciones.PersistenciaException;
import com.starbank.utilidades.Validaciones;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionPostgres {
    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionPostgres(String url, String usuario, String clave) {
        this.url = Validaciones.texto(url, "La URL de base de datos");
        this.usuario = Validaciones.texto(usuario, "El usuario de base de datos");
        this.clave = Validaciones.obligatorio(clave, "La contraseña de base de datos");
    }

    public static ConexionPostgres desdeEntorno() {
        String url = System.getenv("STARBANK_DB_URL");
        String usuario = System.getenv("STARBANK_DB_USER");
        String clave = System.getenv("STARBANK_DB_PASSWORD");
        if (url == null || url.isBlank() || usuario == null || usuario.isBlank() || clave == null) {
            throw new PersistenciaException("Configura STARBANK_DB_URL, STARBANK_DB_USER y STARBANK_DB_PASSWORD para el modo PostgreSQL.");
        }
        return new ConexionPostgres(url, usuario, clave);
    }

    public Connection abrir() {
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a PostgreSQL. Verifica URL, credenciales y que el servicio esté activo.", e);
        }
    }
}
