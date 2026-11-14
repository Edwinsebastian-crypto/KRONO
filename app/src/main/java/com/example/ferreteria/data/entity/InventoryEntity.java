package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventario")
public class InventoryEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String nombre;
    public String sku;
    public double cantidad;
    public String unidad;

    public InventoryEntity(String nombre, String sku, double cantidad, String unidad) {
        this.nombre = nombre;
        this.sku = sku;
        this.cantidad = cantidad;
        this.unidad = unidad;
    }
}
