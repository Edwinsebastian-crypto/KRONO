package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "granjas")
public class FarmEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String nombre;
    public String zona;
    public boolean activa;

    public FarmEntity(String nombre, String zona, boolean activa) {
        this.nombre = nombre;
        this.zona = zona;
        this.activa = activa;
    }
}
