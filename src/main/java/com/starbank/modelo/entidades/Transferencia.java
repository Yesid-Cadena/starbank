package com.starbank.modelo.entidades;

import com.starbank.modelo.enumeraciones.EstadoTransaccion;
import com.starbank.modelo.enumeraciones.TipoTransaccion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Transferencia extends Transaccion {
    public Transferencia(String identificador, BigDecimal monto, LocalDateTime fechaHora,
                         String cuentaOrigen, String cuentaDestino,
                         EstadoTransaccion estado, String descripcion) {
        super(identificador, TipoTransaccion.TRANSFERENCIA, monto, fechaHora,
                cuentaOrigen, cuentaDestino, estado, descripcion);
    }
}
