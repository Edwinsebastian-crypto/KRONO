package com.example.ferreteria.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ferreteria.data.dao.PedidoDao;
import com.example.ferreteria.data.dao.AdminDao;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.UserEntity;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.ZoneEntity;
import com.example.ferreteria.data.entity.InventoryEntity;
import com.example.ferreteria.data.entity.AuditEntity;
import com.example.ferreteria.data.entity.RevisionPedidoEntity;

@Database(
        entities = {PedidoEntity.class, InsumoPedidoEntity.class, UserEntity.class, FarmEntity.class, ZoneEntity.class, InventoryEntity.class, AuditEntity.class, RevisionPedidoEntity.class},
        version = 5,
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

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS revisiones_pedido (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, pedidoId INTEGER NOT NULL, supervisorId TEXT, textoEditado TEXT, observacion TEXT, fecha INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_revisiones_pedido_pedidoId ON revisiones_pedido (pedidoId)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            try {
                db.execSQL("ALTER TABLE granjas ADD COLUMN codigoAcceso TEXT DEFAULT ''");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE granjas ADD COLUMN password TEXT DEFAULT ''");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE pedidos ADD COLUMN granja TEXT DEFAULT ''");
            } catch (Exception ignored) {}
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            try {
                db.execSQL("ALTER TABLE granjas ADD COLUMN lote TEXT DEFAULT 'Lote 01'");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE pedidos ADD COLUMN lote TEXT DEFAULT 'Lote 01'");
            } catch (Exception ignored) {}
        }
    };

    public abstract PedidoDao pedidoDao();
    public abstract AdminDao adminDao();

    public static KronoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (KronoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            KronoDatabase.class,
                            "krono_local.db"
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                     .fallbackToDestructiveMigration()
                     .build();
                }
            }
        }
        return INSTANCE;
    }
}
