CREATE TABLE IF NOT EXISTS usuarios (
    identificacion VARCHAR(50) PRIMARY KEY,
    tipo_usuario VARCHAR(20) NOT NULL CHECK (tipo_usuario IN ('CLIENTE', 'ADMINISTRADOR')),
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(30) NOT NULL,
    clave_sal BYTEA NOT NULL,
    clave_resumen BYTEA NOT NULL,
    clave_iteraciones INTEGER NOT NULL CHECK (clave_iteraciones > 0),
    fecha_registro TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO'))
);

CREATE TABLE IF NOT EXISTS cuentas (
    numero_cuenta VARCHAR(50) PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE')),
    identificacion_cliente VARCHAR(50) NOT NULL REFERENCES usuarios(identificacion),
    fecha_creacion TIMESTAMP NOT NULL,
    saldo NUMERIC(19,2) NOT NULL CHECK (saldo >= 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVA', 'BLOQUEADA', 'CERRADA'))
);

CREATE INDEX IF NOT EXISTS idx_cuentas_cliente ON cuentas(identificacion_cliente);

CREATE TABLE IF NOT EXISTS transacciones (
    identificador VARCHAR(60) PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('DEPOSITO', 'RETIRO', 'TRANSFERENCIA')),
    monto NUMERIC(19,2) NOT NULL CHECK (monto > 0),
    fecha_hora TIMESTAMP NOT NULL,
    cuenta_origen VARCHAR(50) REFERENCES cuentas(numero_cuenta),
    cuenta_destino VARCHAR(50) REFERENCES cuentas(numero_cuenta),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'COMPLETADA', 'RECHAZADA')),
    descripcion VARCHAR(500) NOT NULL,
    CHECK (
        (tipo = 'DEPOSITO' AND cuenta_origen IS NULL AND cuenta_destino IS NOT NULL) OR
        (tipo = 'RETIRO' AND cuenta_origen IS NOT NULL AND cuenta_destino IS NULL) OR
        (tipo = 'TRANSFERENCIA' AND cuenta_origen IS NOT NULL AND cuenta_destino IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_transacciones_origen ON transacciones(cuenta_origen);
CREATE INDEX IF NOT EXISTS idx_transacciones_destino ON transacciones(cuenta_destino);
CREATE INDEX IF NOT EXISTS idx_transacciones_fecha ON transacciones(fecha_hora);

CREATE TABLE IF NOT EXISTS registros_actividad (
    identificador VARCHAR(60) PRIMARY KEY,
    identificacion_usuario VARCHAR(50),
    tipo_actividad VARCHAR(80) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,
    resultado VARCHAR(30) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    recurso_afectado VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_registros_usuario ON registros_actividad(identificacion_usuario);
CREATE INDEX IF NOT EXISTS idx_registros_fecha ON registros_actividad(fecha_hora);
