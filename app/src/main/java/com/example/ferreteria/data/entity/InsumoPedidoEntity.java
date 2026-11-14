package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Línea de insumo perteneciente a una hoja de pedido.
 */
@Entity(
        tableName = "insumos_pedido",
        foreignKeys = @ForeignKey(
                entity = PedidoEntity.class,
                parentColumns = "id",
                childColumns = "pedidoId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("pedidoId")}
)
public class InsumoPedidoEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long pedidoId;
    public String nombre;
    public String cantidad;
    public String sku;

    public InsumoPedidoEntity(long pedidoId, String nombre, String cantidad, String sku) {
        this.pedidoId = pedidoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.sku = sku;
    }
}
