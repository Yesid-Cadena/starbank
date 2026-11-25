# Servicios bancarios y repositorios en memoria

## Alcance implementado

- `ClienteServicio`: registro, documento y correo únicos, consulta, actualización,
  estado y listado. Documento equivale a identificación; el correo se compara sin
  distinguir mayúsculas y se eliminan los espacios externos con las validaciones existentes.
- `CuentaServicio`: creación para clientes activos registrados, números únicos,
  asociación de múltiples cuentas, consulta, saldo y bloqueo/desbloqueo.
  Los números se generan convirtiendo UUID a texto decimal, comprobando unicidad
  dentro de la unidad de trabajo. Las cuentas comienzan en cero.
- `TransaccionServicio`: depósitos, retiros y transferencias simuladas con validación
  de monto, titular de la cuenta operada, estado, destino y fondos.
- Historial por cuenta y cliente, consulta por identificador, filtro opcional por tipo
  y fechas inclusivas, orden ascendente por fecha e identificador como desempate.
- Transferencia almacenada una sola vez y visible en ambas cuentas. Si las dos
  pertenecen al mismo cliente, también aparece una sola vez en su historial.

## Decisiones de diseño e integridad

`Transaccion` es inmutable y `Transferencia` la especializa; el UML de p. 75 las
presenta separadas. Esta adaptación evita duplicar monto, fecha, estado y registro.
Se conservan origen y destino mediante sus números inmutables, evitando que cambios
posteriores en objetos Cuenta alteren la evidencia histórica.
Se implementan PENDIENTE, COMPLETADA y RECHAZADA según esta etapa. Cancelación y
estados intermedios del PDF siguen pendientes de aclaración, no se implementan todavía.

`Cuenta.acreditar` y `Cuenta.debitar` producen nuevas versiones validadas; no hay setter
de saldo. Los servicios persisten esas versiones junto con la transacción. Los métodos
anteriores de consulta y estado se conservan. Los repositorios copian usuarios y cuentas
al guardar y al consultar, incluyendo sus referencias mutables. La clave protegida puede
compartirse porque es inmutable. Las listas devueltas no admiten modificaciones.
El Cliente contenido en Cuenta es una instantánea; `ClienteServicio` es la fuente de
datos personales y estado actualizados, y la relación se identifica por documento.

Los repositorios son componentes internos del modelo. Las futuras vistas/controladores
deben acceder a los servicios, nunca actualizar repositorios por su cuenta.
El contrato de actualización de transacciones solo admite pasar de PENDIENTE a un estado
final conservando el resto de los datos; COMPLETADA y RECHAZADA no se reescriben.
Las operaciones actuales crean directamente su resultado final dentro de la unidad de trabajo.

### Atomicidad

Las interfaces `UsuarioRepositorio`, `CuentaRepositorio`, `TransaccionRepositorio` y
`UnidadTrabajo` desacoplan servicios y almacenamiento. `ConfiguracionMemoria` construye
todos los componentes sobre el mismo `AlmacenMemoria`. Es obligatorio que los repositorios
y la unidad de trabajo inyectados pertenezcan al mismo contexto transaccional.

La implementación en memoria sincroniza consultas y escrituras con un monitor compartido.
Antes de cada unidad de trabajo toma instantáneas de los mapas; ante una excepción revierte
usuarios, cuentas y transacciones. Como los objetos almacenados nunca se modifican en sitio
ni se exponen, restaurar los mapas restaura íntegramente sus versiones. Se verificaron fallos
antes del débito, después del débito, después del crédito y antes/después de insertar el historial.
También se verificaron retiros concurrentes: no hay sobregiros ni actualizaciones perdidas.

Para JDBC, `UnidadTrabajo` deberá compartir una conexión con todos los repositorios de la
operación, usar commit/rollback y bloquear o versionar las cuentas para evitar concurrencia
incorrecta. Los servicios no contienen dependencias JDBC. Esta implementación mantiene
todo en memoria y serializa las operaciones; no es una solución distribuida ni durable.

### Rechazos y autorización futura

- Fondos insuficientes, cuentas no habilitadas y transferencia a la misma cuenta generan
  una transacción RECHAZADA con motivo, sin cambios de saldo.
- Monto no positivo/nulo, descripción vacía, cuentas inexistentes o ajenas generan excepciones
  de validación sin registrar una transacción financiera incompleta. Una futura auditoría
  podrá registrar esos intentos aparte, sin vulnerar RF-14.
- Un fallo de almacenamiento se propaga y revierte toda la operación, incluso si el
  repositorio alcanzó a insertar el registro antes de fallar.
- Las operaciones y consultas por cuenta reciben identificación del cliente y comprueban
  titularidad. Esa identificación aún es proporcionada por el llamador: no hay autenticación.
  En la próxima fase deberá obtenerse exclusivamente de la sesión autenticada.
- Crear/bloquear/desbloquear cuentas, listar clientes y consultar transacciones por ID son
  puntos de servicio internos que deberán protegerse con autorización administrativa.
  Las reglas de sesión, bloqueo de usuario y permisos se completarán en esa fase.
- Se conserva la precisión de BigDecimal sin redondeo implícito. El PDF no fija escala monetaria;
  la política de decimales COP deberá acordarse antes de definir columnas SQL o formularios.

## Requerimientos cubiertos en la capa de modelo/servicios

RQF001–003: registro y datos de clientes; RQF006–008: cuentas y saldos;
RQF009–015: movimientos, depósitos, retiros, transferencias, fondos e historial;
RQF018: bloqueo/desbloqueo a nivel de servicio. RQF017 tiene soporte parcial de consulta,
listado y actualización, aún sin panel ni permisos administrativos.
RD-01–10 y RD-12 tienen soporte de asociación, unicidad, validación y consistencia en
estos casos de uso. RD-11 y RQF004–005 requieren la futura autenticación/autorización.
La interfaz gráfica y el resto de requisitos continúan pendientes.

## Verificación y ejecución

```sh
./mvnw clean verify
java -jar target/starbank-1.0-SNAPSHOT.jar
```

Windows: usar `.\mvnw.cmd clean verify` desde PowerShell.
Resultado verificado en Linux con Java 21: **96 pruebas exitosas, 0 fallidas, 0 errores**,
incluyendo las 40 originales sin modificar. La demostración deja **700.000 COP en cada
cuenta**, cuatro operaciones completadas y dos rechazadas. Una misma transferencia
aparece en ambos historiales con el mismo identificador. No se añadieron dependencias.

## Ramas propuestas

Repositorio: https://github.com/Yesid-Cadena/starbank.git

| Rama | Trabajo |
| --- | --- |
| `main` | Versiones verificadas e integradas |
| `feat/servicios-bancarios` | Esta entrega: repositorios, servicios, operaciones e historial |
| `feat/autenticacion-autorizacion` | Próxima fase: sesiones, roles, propiedad y permisos administrativos |
| `feat/persistencia-postgresql` | Repositorios JDBC, esquema SQL y unidad de trabajo transaccional |
| `feat/interfaz-swing` | Vistas Swing/FlatLaf y controladores MVC |
| `feat/panel-administrativo` | Gestión administrativa y supervisión |
| `feat/atencion-auditoria-reportes` | Solicitud, RegistroActividad, atención y reportes |
| `feat/cliente-servidor` | Separación de cliente y servidor conforme al despliegue UML |
| `test/integracion-final` | Integración, concurrencia y validación Linux/Windows |

Crear cada rama futura desde `main` actualizado cuando su fase sea autorizada. Integrar
mediante pull request con revisión y `./mvnw clean verify` satisfactorio. Las ramas futuras
son propuestas: no contienen implementaciones adelantadas. Esta etapa inicia el historial
Git local, pues no existía repositorio local y el remoto consultado no tenía referencias.
