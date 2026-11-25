# StarBank

StarBank es un sistema bancario digital ficticio desarrollado como proyecto académico
de Programación Avanzada de Ingeniería de Sistemas de la Universidad Distrital
Francisco José de Caldas.

Aplicación Java 21 con Maven y arquitectura MVC. Implementa clientes, cuentas,
transacciones, autenticación por sesión, autorización por rol/propiedad, auditoría
básica y política monetaria COP con máximo dos decimales (sin redondeo implícito).
Soporta almacenamiento en memoria y PostgreSQL/JDBC.

## Ejecución

- Memoria (demo): `./mvnw -B -ntp clean package && java -jar target/starbank-1.0-SNAPSHOT.jar`
- Memoria (menú): `java -jar target/starbank-1.0-SNAPSHOT.jar --menu`
- PostgreSQL: configurar `STARBANK_DB_URL`, `STARBANK_DB_USER`, `STARBANK_DB_PASSWORD`;
  inicializar: `java -jar target/starbank-1.0-SNAPSHOT.jar --postgres --init-db`;
  crear admin: `--postgres --crear-admin`; menú: `--postgres --menu`.
- Pruebas: `./mvnw -B -ntp clean verify`. Pruebas JDBC (`*IT`) requieren BD de prueba
  (`STARBANK_DB_TEST_URL/USER/PASSWORD`, URL debe contener `test`); si no están configuradas,
  se omiten y se reportan como omitidas.
