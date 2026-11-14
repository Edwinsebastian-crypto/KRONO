package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "granjas")
public class FarmEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String nombre;
    public String zona;
    public boolean activa;
    public String codigoAcceso;
    public String password;

    public FarmEntity(String nombre, String zona, boolean activa, String codigoAcceso, String password) {
        this.nombre = nombre;
        this.zona = zona;
        this.activa = activa;
        this.codigoAcceso = codigoAcceso != null ? codigoAcceso : "";
        this.password = password != null ? password : "";
    }

    @Ignore
    public FarmEntity(String nombre, String zona, boolean activa) {
        this(nombre, zona, activa, "", "");
    }
}
