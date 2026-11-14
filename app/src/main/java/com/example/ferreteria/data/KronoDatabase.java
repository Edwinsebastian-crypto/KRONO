package com.example.ferreteria.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS usuarios (userId TEXT NOT NULL PRIMARY KEY, nombre TEXT, passwordHash TEXT, rol TEXT, activo INTEGER NOT NULL, creadoEn INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS granjas (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT, zona TEXT, activa INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS zonas (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT, descripcion TEXT, activa INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS inventario (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT, sku TEXT, cantidad REAL NOT NULL, unidad TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS auditoria (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, usuarioId TEXT, accion TEXT, entidad TEXT, detalle TEXT, fecha INTEGER NOT NULL)");
        }
    };

    public abstract PedidoDao pedidoDao();

    public static KronoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (KronoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            KronoDatabase.class,
                            "krono_local.db"
                    ).addMigrations(MIGRATION_1_2).build();
                }
            }
        }
        return INSTANCE;
    }
}
