package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class UserEntity {
    @PrimaryKey public String userId;
    public String nombre;
    public String passwordHash;
    public String rol;
    public boolean activo;
    public long creadoEn;

    public UserEntity(String userId, String nombre, String passwordHash, String rol, boolean activo, long creadoEn) {
        this.userId = userId;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
        this.creadoEn = creadoEn;
    }
}
