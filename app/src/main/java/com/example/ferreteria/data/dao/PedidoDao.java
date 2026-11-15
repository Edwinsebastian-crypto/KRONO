package com.example.ferreteria.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.RevisionPedidoEntity;

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

    @Insert
    long insertarRevision(RevisionPedidoEntity revision);

    @Query("SELECT * FROM revisiones_pedido WHERE pedidoId = :pedidoId ORDER BY fecha DESC, id DESC LIMIT 1")
    RevisionPedidoEntity obtenerUltimaRevision(long pedidoId);

    @Query("SELECT * FROM insumos_pedido ORDER BY pedidoId, id ASC")
    List<InsumoPedidoEntity> obtenerTodosLosInsumos();

    @Query("SELECT * FROM revisiones_pedido ORDER BY fecha DESC, id DESC")
    List<RevisionPedidoEntity> obtenerTodasLasRevisiones();

    @Query("UPDATE pedidos SET granja = :granja, zona = :zona WHERE operarioId = :operarioId")
    int sincronizarGranjaYZonaPorOperario(String operarioId, String granja, String zona);

    @Query("DELETE FROM pedidos WHERE id = :pedidoId")
    int eliminarPedido(long pedidoId);
}
