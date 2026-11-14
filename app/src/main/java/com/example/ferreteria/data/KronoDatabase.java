package com.example.ferreteria.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ferreteria.data.dao.PedidoDao;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;

@Database(
        entities = {PedidoEntity.class, InsumoPedidoEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class KronoDatabase extends RoomDatabase {
    private static volatile KronoDatabase INSTANCE;

    public abstract PedidoDao pedidoDao();

    public static KronoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (KronoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            KronoDatabase.class,
                            "krono_local.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
