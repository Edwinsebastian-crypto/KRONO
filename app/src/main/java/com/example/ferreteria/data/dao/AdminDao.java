package com.example.ferreteria.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ferreteria.data.entity.AuditEntity;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.InventoryEntity;
import com.example.ferreteria.data.entity.UserEntity;
import com.example.ferreteria.data.entity.ZoneEntity;

import java.util.List;

@Dao
public interface AdminDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void guardarUsuario(UserEntity usuario);
    @Query("SELECT * FROM usuarios ORDER BY nombre") List<UserEntity> obtenerUsuarios();
    @Query("SELECT * FROM usuarios WHERE userId = :userId LIMIT 1") UserEntity obtenerUsuario(String userId);
    @Query("UPDATE usuarios SET activo = :activo WHERE userId = :userId") int cambiarEstadoUsuario(String userId, boolean activo);
    @Query("DELETE FROM usuarios WHERE userId = :userId") int eliminarUsuario(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE) long guardarGranja(FarmEntity granja);
    @Query("SELECT * FROM granjas ORDER BY nombre") List<FarmEntity> obtenerGranjas();
    @Query("DELETE FROM granjas WHERE id = :id") int eliminarGranja(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE) long guardarZona(ZoneEntity zona);
    @Query("SELECT * FROM zonas ORDER BY nombre") List<ZoneEntity> obtenerZonas();
    @Query("DELETE FROM zonas WHERE id = :id") int eliminarZona(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE) long guardarInventario(InventoryEntity item);
    @Query("SELECT * FROM inventario ORDER BY nombre") List<InventoryEntity> obtenerInventario();
    @Query("DELETE FROM inventario WHERE id = :id") int eliminarInventario(long id);

    @Insert void registrarAuditoria(AuditEntity evento);
    @Query("SELECT * FROM auditoria ORDER BY fecha DESC LIMIT 500") List<AuditEntity> obtenerAuditoria();
}
