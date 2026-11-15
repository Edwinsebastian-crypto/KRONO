package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
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
    public String granja;
    public String lote;
    public long creadoEn;
    public long actualizadoEn;

    public PedidoEntity(String codigo, String operarioId, String zona, String estado,
                        String granja, String lote, long creadoEn, long actualizadoEn) {
        this.codigo = codigo;
        this.operarioId = operarioId;
        this.zona = zona;
        this.estado = estado;
        this.granja = granja != null ? granja : "";
        this.lote = (lote != null && !lote.trim().isEmpty()) ? lote.trim() : "Lote 01";
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    @Ignore
    public PedidoEntity(String codigo, String operarioId, String zona, String estado,
                        String granja, long creadoEn, long actualizadoEn) {
        this(codigo, operarioId, zona, estado, granja, "Lote 01", creadoEn, actualizadoEn);
    }

    @Ignore
    public PedidoEntity(String codigo, String operarioId, String zona, String estado,
                        long creadoEn, long actualizadoEn) {
        this(codigo, operarioId, zona, estado, "", "Lote 01", creadoEn, actualizadoEn);
    }
}
