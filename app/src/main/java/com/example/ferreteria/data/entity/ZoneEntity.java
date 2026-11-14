package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "zonas")
public class ZoneEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String nombre;
    public String descripcion;
    public boolean activa;

    public ZoneEntity(String nombre, String descripcion, boolean activa) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activa = activa;
    }
}
