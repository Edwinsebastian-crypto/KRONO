package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "revisiones_pedido", indices = {@Index("pedidoId")})
public class RevisionPedidoEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long pedidoId;
    public String supervisorId;
    public String textoEditado;
    public String observacion;
    public long fecha;

    public RevisionPedidoEntity(long pedidoId, String supervisorId, String textoEditado,
                                String observacion, long fecha) {
        this.pedidoId = pedidoId;
        this.supervisorId = supervisorId;
        this.textoEditado = textoEditado;
        this.observacion = observacion;
        this.fecha = fecha;
    }
}
