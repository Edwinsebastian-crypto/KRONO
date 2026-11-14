package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Cabecera de una hoja de pedido creada por un operario.
 */
@Entity(tableName = "pedidos")
public class PedidoEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String codigo;
    public String operarioId;
    public String zona;
    public String estado;
    public long creadoEn;
    public long actualizadoEn;

    public PedidoEntity(String codigo, String operarioId, String zona, String estado,
                        long creadoEn, long actualizadoEn) {
        this.codigo = codigo;
        this.operarioId = operarioId;
        this.zona = zona;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
