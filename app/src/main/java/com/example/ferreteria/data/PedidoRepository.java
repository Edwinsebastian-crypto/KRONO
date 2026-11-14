package com.example.ferreteria.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ferreteria.data.dao.PedidoDao;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Acceso asíncrono a los pedidos. Las operaciones de Room nunca se ejecutan
 * en el hilo principal de Android.
 */
public class PedidoRepository {
    public interface GuardarPedidoCallback {
        void onSuccess(long pedidoId);
        void onError(Exception error);
    }

    public interface PedidosCallback {
        void onSuccess(List<PedidoEntity> pedidos);
        void onError(Exception error);
    }

    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private final KronoDatabase database;
    private final PedidoDao pedidoDao;

    public PedidoRepository(Context context) {
        database = KronoDatabase.getInstance(context);
        pedidoDao = database.pedidoDao();
    }

    public void guardarPedido(PedidoEntity pedido,
                              List<InsumoPedidoEntity> insumos,
                              GuardarPedidoCallback callback) {
        IO_EXECUTOR.execute(() -> {
            try {
                final long[] nuevoId = new long[1];
                database.runInTransaction(() -> {
                    long pedidoId = pedidoDao.insertarPedido(pedido);
                    nuevoId[0] = pedidoId;

                    List<InsumoPedidoEntity> lineas = new ArrayList<>();
                    for (InsumoPedidoEntity insumo : insumos) {
                        lineas.add(new InsumoPedidoEntity(
                                pedidoId, insumo.nombre, insumo.cantidad, insumo.sku
                        ));
                    }
                    if (!lineas.isEmpty()) {
                        pedidoDao.insertarInsumos(lineas);
                    }
                });
                MAIN_HANDLER.post(() -> callback.onSuccess(nuevoId[0]));
            } catch (Exception error) {
                MAIN_HANDLER.post(() -> callback.onError(error));
            }
        });
    }

    public void obtenerPedidos(PedidosCallback callback) {
        IO_EXECUTOR.execute(() -> {
            try {
                List<PedidoEntity> pedidos = pedidoDao.obtenerTodos();
                MAIN_HANDLER.post(() -> callback.onSuccess(pedidos));
            } catch (Exception error) {
                MAIN_HANDLER.post(() -> callback.onError(error));
            }
        });
    }

    public void actualizarEstado(long pedidoId, String nuevoEstado,
                                GuardarPedidoCallback callback) {
        IO_EXECUTOR.execute(() -> {
            try {
                int filas = pedidoDao.actualizarEstado(
                        pedidoId, nuevoEstado, System.currentTimeMillis()
                );
                if (filas == 0) {
                    throw new IllegalStateException("No se encontró el pedido " + pedidoId);
                }
                MAIN_HANDLER.post(() -> callback.onSuccess(pedidoId));
            } catch (Exception error) {
                MAIN_HANDLER.post(() -> callback.onError(error));
            }
        });
    }
}
