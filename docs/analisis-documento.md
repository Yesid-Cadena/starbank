# Referencia y análisis inicial

Este documento conserva el diagnóstico de la primera entrega. Para el alcance actualizado
de servicios y operaciones, consultar [servicios y ramas](servicios-y-ramas.md).

Fuente principal: `/home/keiler/Downloads/StarBank .pdf`, 84 páginas.
Se inspeccionaron requerimientos (pp. 27–66), actores (pp. 67–71) y diagramas (pp. 72–84).
No se encontró código Maven previo en la carpeta de trabajo; se creó `~/StarBank`.

## Trazabilidad de esta entrega

| Referencia | Base implementada | Pendiente |
| --- | --- | --- |
| RQF001–003, RS-01, RF-03 | Datos de usuario y actualización validada sin cambios parciales | Registro, unicidad y control de acceso |
| RQF004–005, RQNF001–003 | Roles tipados y protección de contraseñas | Sesiones y autorización |
| RQF006–008, RD-01–03 | Cuenta, titular obligatorio, número, tipo, estado, saldo y fecha | Registro y unicidad global, consulta autorizada |
| RQF017–018 | Administrador y primitivas de cambio de estado | Gestión administrativa con permisos y auditoría |
| RQNF008–009 | Paquetes MVC y dominio sin dependencias gráficas o SQL | Contratos de repositorio y servicios |

RQF009–015 (movimientos, depósitos, retiros, transferencias, fondos e historial),
RQF016 (solicitudes), RQF019–020 (supervisión y auditoría), RU-12 (cierre de sesión)
y RU-20 (reportes) permanecen en el alcance del proyecto y se desarrollarán por fases.
Las reglas de atomicidad, propiedad, fondos positivos y cuentas habilitadas serán
obligatorias al implementar operaciones. Ninguna se considera satisfecha por esta entrega.

## Diferencias notificadas antes de implementar

1. **Clases, p. 75:** el UML usa `double` para dinero y `Date` para fechas;
   se aplican `BigDecimal` y `LocalDateTime` conforme a las instrucciones actuales.
   Tipos y estados pasan de `String` a enumeraciones.
2. **Clases, p. 75:** no aparecen `Usuario`, credenciales, roles, estado de usuario,
   fecha de creación de cuenta, `Solicitud` ni `RegistroActividad`, aunque los
   requerimientos sí necesitan esos datos. Se introduce la abstracción Usuario
   solicitada y se reservan solicitudes y auditoría para entregas posteriores.
3. **Identidad, p. 75:** el UML distingue idCliente y documento e idAdministrador.
   Esta entrega utiliza `identificacion` como identidad de dominio común, sin añadir
   claves técnicas de base de datos. El mapeo a claves SQL se decidirá en persistencia.
4. **Responsabilidades, p. 75:** registrarse, iniciar sesión, gestionar clientes y
   transferir figuran como métodos de entidades. Los casos de uso se ubicarán en
   servicios del modelo para respetar MVC; las entidades mantienen sus invariantes.
5. **Crear cuenta, p. 73 frente a las instrucciones actuales:** el caso de uso se
   conecta al Cliente, mientras la petición actual asigna creación al Administrador.
   Confirmar actores autorizados antes de implementar el servicio; hoy no hay permisos.
6. **Transferencias, pp. 76–82:** los diagramas muestran débito, crédito y registro
   secuenciales sin reversión explícita. RF-12 (p. 57) prohíbe resultados parciales.
   La futura operación debe incluir saldos y registro en una única unidad atómica.
   En p. 78 la confirmación de éxito está fuera del bloque alternativo: deberá emitirse
   solo cuando la transferencia termine correctamente.
7. **Actividad, p. 77:** se muestra bloqueo tras `intentos > 3`, sin precisar si se
   bloquea el usuario o la cuenta bancaria; el texto RF-04 habla de usuario bloqueado.
   Confirmar umbral, objeto bloqueado y recuperación en la fase de autenticación.
8. **Estados, p. 76:** aparecen cancelación y estados intermedios de transferencia
   sin reglas completas de cancelación en RF-12. Se requieren aclaraciones antes de
   implementar esas transiciones; no se descarta esa funcionalidad.
9. **Casos de uso/componentes, pp. 73 y 83:** el registro de clientes no aparece
   explícito en el diagrama principal de casos de uso y faltan componentes explícitos
   de solicitudes y auditoría. Los requisitos textuales se conservan como referencia.
10. **Despliegue, p. 84:** cliente, servidor de aplicaciones por HTTPS y servidor de
    datos se corresponden con la futura fase 10. El prototipo actual se ejecuta localmente.

## Secuencia acordada

1. Configuración y arquitectura MVC (base creada).
2. Entidades y enumeraciones (Usuario, Cliente, Administrador y Cuenta implementados;
   Transaccion, Transferencia, Solicitud y RegistroActividad pendientes).
3. Servicios y reglas de negocio con repositorios en memoria.
4. Depósitos, retiros, transferencias e historial.
5. Autenticación y autorización.
6. PostgreSQL mediante JDBC.
7. Swing, FlatLaf y controladores.
8. Panel administrativo.
9. Atención al cliente, auditoría y reportes.
10. Arquitectura cliente-servidor.
11. Pruebas e integración final, además de las pruebas incrementales de cada entrega.

No avanzar de entrega sin autorización.
