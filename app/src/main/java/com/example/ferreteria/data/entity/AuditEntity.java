package com.example.ferreteria.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "auditoria")
public class AuditEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String usuarioId;
    public String accion;
    public String entidad;
    public String detalle;
    public long fecha;

    public AuditEntity(String usuarioId, String accion, String entidad, String detalle, long fecha) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.detalle = detalle;
        this.fecha = fecha;
    }
}
