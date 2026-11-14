package com.example.ferreteria;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import com.example.ferreteria.data.AuthRepository;
import com.example.ferreteria.data.entity.UserEntity;
import com.example.ferreteria.data.entity.AuditEntity;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.ZoneEntity;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminActivity extends AppCompatActivity {
    private AuthRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);
        repository = new AuthRepository(this);
        setContentView(R.layout.activity_admin);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        setupClickListeners();
    }


    private void mostrarUsuarios() {
        repository.listarUsuarios(new AuthRepository.ResultCallback<List<UserEntity>>() {
            @Override public void onSuccess(List<UserEntity> users) {
                StringBuilder b = new StringBuilder();
                for (UserEntity u : users) b.append(u.userId).append(" • ").append(u.nombre).append(" • ").append(u.rol).append(u.activo ? " • Activo\n" : " • Inactivo\n");
                new AlertDialog.Builder(AdminActivity.this).setTitle("Usuarios locales")
                        .setMessage(b.length() == 0 ? "Sin usuarios" : b.toString())
                        .setPositiveButton("Crear usuario", (d,w) -> dialogCrearUsuario()).setNegativeButton("Cerrar",null).show();
            }
            @Override public void onError(Exception e) { Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show(); }
        });
    }

    private void dialogCrearUsuario() {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        int pad=(int)(18*getResources().getDisplayMetrics().density); box.setPadding(pad,pad,pad,pad);
        EditText id=new EditText(this); id.setHint("Código (ej. E101)"); box.addView(id);
        EditText name=new EditText(this); name.setHint("Nombre completo"); box.addView(name);
        EditText pass=new EditText(this); pass.setHint("Contraseña (mínimo 8 caracteres)"); pass.setInputType(129); box.addView(pass);
        Spinner role=new Spinner(this); role.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"OPERARIO","SUPERVISOR","ADMINISTRADOR"})); box.addView(role);
        new AlertDialog.Builder(this).setTitle("Nuevo usuario").setView(box).setNegativeButton("Cancelar",null)
                .setPositiveButton("Guardar",(d,w)->repository.crearUsuario(id.getText().toString(),name.getText().toString(),pass.getText().toString(),role.getSelectedItem().toString(),new AuthRepository.ResultCallback<Void>(){
                    @Override public void onSuccess(Void v){Toast.makeText(AdminActivity.this,"Usuario creado.",Toast.LENGTH_SHORT).show();mostrarUsuarios();}
                    @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
                })).show();
    }

    private void mostrarAuditoria() {
        repository.listarAuditoria(new AuthRepository.ResultCallback<List<AuditEntity>>() {
            @Override public void onSuccess(List<AuditEntity> events) {
                StringBuilder b=new StringBuilder();
                for(AuditEntity a:events) b.append(android.text.format.DateFormat.format("dd/MM/yyyy HH:mm",a.fecha)).append(" • ").append(a.accion).append(" • ").append(a.entidad).append(" • ").append(a.detalle).append("\n");
                new AlertDialog.Builder(AdminActivity.this).setTitle("Auditoría local").setMessage(b.length()==0?"No hay eventos registrados.":b.toString()).setPositiveButton("Aceptar",null).show();
            }
            @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
        });
    }

    private void mostrarCatalogos() {
        String[] options={"Inventario","Granjas","Zonas"};
        new AlertDialog.Builder(this).setTitle("Administrar catálogos").setItems(options,(d,which)->{
            if(which==0) listarInventario(); else if(which==1) listarGranjas(); else listarZonas();
        }).show();
    }

    private void listarInventario() {
        repository.listarInventario(new AuthRepository.ResultCallback<List<com.example.ferreteria.data.entity.InventoryEntity>>() {
            @Override public void onSuccess(List<com.example.ferreteria.data.entity.InventoryEntity> items) {
                StringBuilder b=new StringBuilder();
                for(com.example.ferreteria.data.entity.InventoryEntity i:items)b.append(i.nombre).append(" • ").append(i.sku).append(" • ").append(i.cantidad).append(" ").append(i.unidad).append("\n");
                new AlertDialog.Builder(AdminActivity.this).setTitle("Inventario local").setMessage(b.length()==0?"Inventario vacío":b.toString()).setPositiveButton("Agregar",(d,w)->dialogCrearInventario()).setNegativeButton("Cerrar",null).show();
            }
            @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
        });
    }

    private void dialogCrearInventario() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);int p=(int)(18*getResources().getDisplayMetrics().density);box.setPadding(p,p,p,p);
        EditText n=new EditText(this);n.setHint("Nombre");box.addView(n);EditText sku=new EditText(this);sku.setHint("SKU");box.addView(sku);
        EditText q=new EditText(this);q.setHint("Cantidad");q.setInputType(8194);box.addView(q);EditText unit=new EditText(this);unit.setHint("Unidad");box.addView(unit);
        new AlertDialog.Builder(this).setTitle("Nuevo insumo").setView(box).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->{
            try { repository.crearInventario(n.getText().toString(),sku.getText().toString(),Double.parseDouble(q.getText().toString()),unit.getText().toString(),new AuthRepository.ResultCallback<Void>(){
                @Override public void onSuccess(Void v){Toast.makeText(AdminActivity.this,"Insumo agregado.",Toast.LENGTH_SHORT).show();listarInventario();}
                @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
            }); } catch(Exception e){Toast.makeText(this,"Cantidad inválida.",Toast.LENGTH_SHORT).show();}
        }).show();
    }

    private void listarGranjas() {
        repository.listarGranjas(new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override public void onSuccess(List<FarmEntity> farms) {
                StringBuilder b=new StringBuilder();for(FarmEntity f:farms)b.append(f.nombre).append(" • ").append(f.zona).append(f.activa?" • Activa":" • Inactiva").append("\n");
                new AlertDialog.Builder(AdminActivity.this).setTitle("Granjas").setMessage(b.length()==0?"Sin granjas":b.toString()).setPositiveButton("Agregar",(d,w)->dialogCrearGranja()).setNegativeButton("Cerrar",null).show();
            }
            @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
        });
    }

    private void dialogCrearGranja() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);int p=(int)(18*getResources().getDisplayMetrics().density);box.setPadding(p,p,p,p);
        EditText n=new EditText(this);n.setHint("Nombre de granja");box.addView(n);EditText z=new EditText(this);z.setHint("Zona");box.addView(z);
        new AlertDialog.Builder(this).setTitle("Nueva granja").setView(box).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->repository.crearGranja(n.getText().toString(),z.getText().toString(),new AuthRepository.ResultCallback<Void>(){
            @Override public void onSuccess(Void v){Toast.makeText(AdminActivity.this,"Granja creada.",Toast.LENGTH_SHORT).show();listarGranjas();}
            @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
        })).show();
    }

    private void listarZonas() {
        repository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override public void onSuccess(List<ZoneEntity> zones) {
                StringBuilder b=new StringBuilder();for(ZoneEntity z:zones)b.append(z.nombre).append(" • ").append(z.descripcion).append(z.activa?" • Activa":" • Inactiva").append("\n");
                new AlertDialog.Builder(AdminActivity.this).setTitle("Zonas").setMessage(b.length()==0?"Sin zonas":b.toString()).setPositiveButton("Agregar",(d,w)->dialogCrearZona()).setNegativeButton("Cerrar",null).show();
            }
            @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
        });
    }

    private void dialogCrearZona() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);int p=(int)(18*getResources().getDisplayMetrics().density);box.setPadding(p,p,p,p);
        EditText n=new EditText(this);n.setHint("Nombre de zona");box.addView(n);EditText desc=new EditText(this);desc.setHint("Descripción");box.addView(desc);
        new AlertDialog.Builder(this).setTitle("Nueva zona").setView(box).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->repository.crearZona(n.getText().toString(),desc.getText().toString(),new AuthRepository.ResultCallback<Void>(){
            @Override public void onSuccess(Void v){Toast.makeText(AdminActivity.this,"Zona creada.",Toast.LENGTH_SHORT).show();listarZonas();}
            @Override public void onError(Exception e){Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
        })).show();
    }

    private void setupClickListeners() {
        // Back to Login
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        // Send Push Notification
        findViewById(R.id.btnPushNotif).setOnClickListener(v -> mostrarUsuarios());

        // Auditoria & Export
        findViewById(R.id.cardAuditoria).setOnClickListener(v -> mostrarAuditoria());

        findViewById(R.id.cardExportar).setOnClickListener(v -> mostrarCatalogos());

        // Tabs Navigation
        findViewById(R.id.tabPedido).setOnClickListener(v -> {
            Intent intent = new Intent(this, OperarioActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tabRevisiones).setOnClickListener(v -> {
            Intent intent = new Intent(this, SupervisorActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tabVentanas).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Control de Ventanas y Administración General", Toast.LENGTH_SHORT).show()
        );
    }
}