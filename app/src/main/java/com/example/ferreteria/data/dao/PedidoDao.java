package com.example.ferreteria.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;

import java.util.List;

@Dao
public interface PedidoDao {
    @Insert
    long insertarPedido(PedidoEntity pedido);

    @Insert
    void insertarInsumos(List<InsumoPedidoEntity> insumos);

    @Query("SELECT * FROM pedidos WHERE id = :pedidoId LIMIT 1")
    PedidoEntity obtenerPedido(long pedidoId);

    @Query("SELECT * FROM pedidos WHERE operarioId = :operarioId ORDER BY creadoEn DESC")
    List<PedidoEntity> obtenerPorOperario(String operarioId);

    @Query("SELECT * FROM pedidos ORDER BY creadoEn DESC")
    List<PedidoEntity> obtenerTodos();

    @Query("SELECT * FROM pedidos WHERE estado = :estado ORDER BY creadoEn DESC")
    List<PedidoEntity> obtenerPorEstado(String estado);

    @Query("SELECT * FROM insumos_pedido WHERE pedidoId = :pedidoId ORDER BY id ASC")
    List<InsumoPedidoEntity> obtenerInsumos(long pedidoId);

    @Query("UPDATE pedidos SET estado = :nuevoEstado, actualizadoEn = :actualizadoEn WHERE id = :pedidoId")
    int actualizarEstado(long pedidoId, String nuevoEstado, long actualizadoEn);

    @Query("DELETE FROM pedidos WHERE id = :pedidoId")
    int eliminarPedido(long pedidoId);
}
