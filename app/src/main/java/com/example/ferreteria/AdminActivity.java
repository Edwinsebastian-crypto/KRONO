package com.example.ferreteria;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ferreteria.data.AuthRepository;
import com.example.ferreteria.data.entity.AuditEntity;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.InventoryEntity;
import com.example.ferreteria.data.entity.UserEntity;
import com.example.ferreteria.data.entity.ZoneEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private AuthRepository repository;
    private LinearLayout pageContent;
    private TextView statusView;
    private String activeSection = "Zonas";
    private final int navy = Color.rgb(43, 48, 71);
    private final int ink = Color.rgb(30, 34, 53);
    private final int muted = Color.rgb(104, 110, 130);
    private final int canvas = Color.rgb(250, 244, 237);
    private final int border = Color.rgb(226, 221, 213);
    private final int green = Color.rgb(0, 135, 90);

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);
        repository = new AuthRepository(this);
        construirInterfaz();
        mostrarSeccion("Zonas");
    }

    private void construirInterfaz() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(canvas);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(14), dp(18), dp(14));
        header.setBackgroundColor(Color.WHITE);
        TextView brand = text("KRONO", 18, ink, true);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton logout = new MaterialButton(this);
        logout.setText("Cerrar sesión");
        logout.setTextSize(12);
        logout.setCornerRadius(dp(12));
        logout.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        logout.setOnClickListener(v -> finish());
        header.addView(logout);
        root.addView(header);

        TextView title = text("Administración", 25, ink, true);
        title.setPadding(dp(20), dp(18), dp(20), 0);
        root.addView(title);
        TextView subtitle = text("Gestiona la estructura y los recursos de tu operación", 13, muted, false);
        subtitle.setPadding(dp(20), dp(4), dp(20), dp(14));
        root.addView(subtitle);

        HorizontalScrollViewCompat nav = new HorizontalScrollViewCompat(this);
        nav.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setPadding(dp(14), 0, dp(14), dp(10));
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        String[] sections = {"Zonas", "Granjas", "Inventario", "Usuarios", "Auditoría"};
        for (String section : sections) {
            MaterialButton tab = new MaterialButton(this);
            tab.setText(section);
            tab.setTextAllCaps(false);
            tab.setTextSize(12);
            tab.setCornerRadius(dp(18));
            tab.setInsetTop(0); tab.setInsetBottom(0);
            tab.setMinHeight(dp(40));
            tab.setMinimumHeight(dp(40));
            tab.setPadding(dp(12), 0, dp(12), 0);
            boolean selected = section.equals(activeSection);
            tab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected ? navy : Color.WHITE));
            tab.setTextColor(selected ? Color.WHITE : ink);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(40));
            lp.setMargins(dp(4), 0, dp(4), 0);
            tabs.addView(tab, lp);
            tab.setOnClickListener(v -> mostrarSeccion(section));
        }
        nav.addView(tabs);
        root.addView(nav);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        pageContent = new LinearLayout(this);
        pageContent.setOrientation(LinearLayout.VERTICAL);
        pageContent.setPadding(dp(16), dp(4), dp(16), dp(28));
        scroll.addView(pageContent);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        statusView = text("", 13, green, true);
        statusView.setPadding(dp(18), dp(8), dp(18), dp(10));
        statusView.setBackgroundColor(Color.WHITE);
        root.addView(statusView);
        setContentView(root);
        StatusBarUtils.applyStatusBarNavyHeader(this);
    }

    // A small horizontal scroller without introducing another layout resource.
    private static class HorizontalScrollViewCompat extends android.widget.HorizontalScrollView {
        HorizontalScrollViewCompat(android.content.Context context) { super(context); }
    }

    private void mostrarSeccion(String section) {
        activeSection = section;
        pageContent.removeAllViews();
        statusView.setText("");
        if ("Zonas".equals(section)) panelZonas();
        else if ("Granjas".equals(section)) panelGranjas();
        else if ("Inventario".equals(section)) panelInventario();
        else if ("Usuarios".equals(section)) panelUsuarios();
        else panelAuditoria();
        // Refresh tab selection without rebuilding the active form.
        reconstruirTabsSeleccion(section);
    }

    private void reconstruirTabsSeleccion(String section) {
        // Keep navigation responsive and visually indicate the active panel.
        // The nav is the second child in the root; its buttons are updated in place.
        View root = (View) pageContent.getParent().getParent();
        if (!(root instanceof LinearLayout)) return;
        LinearLayout layout = (LinearLayout) root;
        if (layout.getChildCount() < 4) return;
        android.widget.HorizontalScrollView nav = (android.widget.HorizontalScrollView) layout.getChildAt(3);
        if (nav.getChildCount() == 0) return;
        LinearLayout tabs = (LinearLayout) nav.getChildAt(0);
        String[] names = {"Zonas", "Granjas", "Inventario", "Usuarios", "Auditoría"};
        for (int i = 0; i < tabs.getChildCount(); i++) {
            MaterialButton button = (MaterialButton) tabs.getChildAt(i);
            boolean selected = names[i].equals(section);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected ? navy : Color.WHITE));
            button.setTextColor(selected ? Color.WHITE : ink);
        }
    }

    private void panelZonas() {
        encabezado("Zonas operativas", "Crea y consulta las zonas de trabajo.");
        EditText name = campo("Nombre de la zona");
        EditText description = campo("Descripción o ubicación");
        botonGuardar("Agregar zona", () -> {
            String n = value(name), d = value(description);
            if (n.isEmpty()) { aviso("Escribe el nombre de la zona."); return; }
            repository.crearZona(n, d, callback(() -> { aviso("Zona guardada correctamente."); mostrarSeccion("Zonas"); }));
        });
        separador();
        subtitulo("Zonas registradas");
        repository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override public void onSuccess(List<ZoneEntity> items) {
                if (items.isEmpty()) vacio("Todavía no hay zonas registradas.");
                for (ZoneEntity item : items) elemento(item.nombre, item.descripcion, item.activa ? "Activa" : "Inactiva");
            }
            @Override public void onError(Exception e) { aviso("No se pudieron cargar las zonas: " + e.getMessage()); }
        });
    }

    private void panelGranjas() {
        encabezado("Granjas y fincas", "Registra cada granja y asígnala a una zona.");
        EditText name = campo("Nombre de la granja o finca");
        Spinner zone = spinner();
        TextView zoneLabel = text("Zona asignada", 12, muted, true);
        pageContent.addView(zoneLabel, margin(0, 8, 0, 4));
        pageContent.addView(zone, margin(0, 0, 0, 10));
        repository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override public void onSuccess(List<ZoneEntity> zones) {
                List<String> labels = new ArrayList<>();
                for (ZoneEntity z : zones) if (z.activa) labels.add(z.nombre);
                if (labels.isEmpty()) labels.add("Primero crea una zona");
                zone.setAdapter(new ArrayAdapter<>(AdminActivity.this, android.R.layout.simple_spinner_dropdown_item, labels));
            }
            @Override public void onError(Exception e) { aviso("No se pudieron cargar las zonas."); }
        });
        botonGuardar("Agregar granja", () -> {
            String n = value(name);
            Object selected = zone.getSelectedItem();
            if (n.isEmpty()) { aviso("Escribe el nombre de la granja."); return; }
            if (selected == null || "Primero crea una zona".contentEquals(selected.toString())) { aviso("Debes crear una zona primero."); return; }
            repository.crearGranja(n, selected.toString(), callback(() -> { aviso("Granja guardada correctamente."); mostrarSeccion("Granjas"); }));
        });
        separador();
        subtitulo("Granjas registradas");
        repository.listarGranjas(new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override public void onSuccess(List<FarmEntity> items) {
                if (items.isEmpty()) vacio("Todavía no hay granjas registradas.");
                for (FarmEntity item : items) elemento(item.nombre, item.zona, item.activa ? "Activa" : "Inactiva");
            }
            @Override public void onError(Exception e) { aviso("No se pudieron cargar las granjas: " + e.getMessage()); }
        });
    }

    private void panelInventario() {
        encabezado("Inventario de insumos", "Mantén actualizado el catálogo de materiales.");
        EditText name = campo("Nombre del insumo");
        EditText sku = campo("Código SKU");
        EditText quantity = campo("Cantidad inicial");
        quantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText unit = campo("Unidad (sacos, litros, unidades...)");
        botonGuardar("Agregar insumo", () -> {
            String n=value(name), code=value(sku), u=value(unit);
            double q;
            try { q=Double.parseDouble(value(quantity)); } catch(Exception e) { aviso("Ingresa una cantidad válida."); return; }
            if(n.isEmpty() || code.isEmpty() || u.isEmpty() || q < 0) { aviso("Completa todos los campos correctamente."); return; }
            repository.crearInventario(n, code, q, u, callback(() -> { aviso("Insumo guardado correctamente."); mostrarSeccion("Inventario"); }));
        });
        separador();
        subtitulo("Insumos registrados");
        repository.listarInventario(new AuthRepository.ResultCallback<List<InventoryEntity>>() {
            @Override public void onSuccess(List<InventoryEntity> items) {
                if(items.isEmpty()) vacio("El catálogo de inventario está vacío.");
                for(InventoryEntity item:items) elemento(item.nombre, item.sku, item.cantidad + " " + item.unidad);
            }
            @Override public void onError(Exception e) { aviso("No se pudo cargar el inventario: " + e.getMessage()); }
        });
    }

    private void panelUsuarios() {
        encabezado("Usuarios del sistema", "Crea accesos según el rol de cada integrante.");
        EditText id = campo("Código de usuario");
        EditText name = campo("Nombre completo");
        EditText password = campo("Contraseña (mínimo 8 caracteres)");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Spinner role = spinner();
        role.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"OPERARIO", "SUPERVISOR", "ADMINISTRADOR"}));
        pageContent.addView(text("Rol de acceso", 12, muted, true), margin(0, 8, 0, 4));
        pageContent.addView(role, margin(0, 0, 0, 10));
        botonGuardar("Crear usuario", () -> {
            String userId=value(id), fullName=value(name), pass=value(password);
            if(userId.isEmpty() || fullName.isEmpty() || pass.length()<8) { aviso("Completa los campos. La contraseña debe tener al menos 8 caracteres."); return; }
            repository.crearUsuario(userId, fullName, pass, role.getSelectedItem().toString(), callback(() -> { aviso("Usuario creado correctamente."); mostrarSeccion("Usuarios"); }));
        });
        separador();
        subtitulo("Usuarios registrados");
        repository.listarUsuarios(new AuthRepository.ResultCallback<List<UserEntity>>() {
            @Override public void onSuccess(List<UserEntity> items) {
                if(items.isEmpty()) vacio("No hay usuarios registrados.");
                for(UserEntity item:items) elemento(item.nombre, item.userId + " • " + item.rol, item.activo ? "Activo" : "Inactivo");
            }
            @Override public void onError(Exception e) { aviso("No se pudieron cargar los usuarios: " + e.getMessage()); }
        });
    }

    private void panelAuditoria() {
        encabezado("Registro de actividad", "Consulta los cambios realizados en la aplicación.");
        repository.listarAuditoria(new AuthRepository.ResultCallback<List<AuditEntity>>() {
            @Override public void onSuccess(List<AuditEntity> items) {
                if(items.isEmpty()) vacio("Todavía no hay movimientos registrados.");
                for(AuditEntity item:items) {
                    String date = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", item.fecha).toString();
                    elemento(item.accion + " • " + item.entidad, item.detalle, date);
                }
            }
            @Override public void onError(Exception e) { aviso("No se pudo cargar la auditoría: " + e.getMessage()); }
        });
    }

    private AuthRepository.ResultCallback<Void> callback(Runnable success) {
        return new AuthRepository.ResultCallback<Void>() {
            @Override public void onSuccess(Void ignored) { success.run(); }
            @Override public void onError(Exception error) { aviso("No se pudo guardar: " + error.getMessage()); }
        };
    }

    private void encabezado(String title, String description) {
        pageContent.addView(text(title, 20, ink, true), margin(2, 4, 2, 2));
        pageContent.addView(text(description, 13, muted, false), margin(2, 0, 2, 14));
    }
    private void subtitulo(String title) { pageContent.addView(text(title, 16, ink, true), margin(2, 10, 2, 10)); }
    private void separador() {
        View line = new View(this); line.setBackgroundColor(border);
        pageContent.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
    }
    private EditText campo(String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setTextColor(ink);
        input.setHintTextColor(muted);
        input.setHint(hint);
        input.setPadding(dp(14), dp(11), dp(14), dp(11));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE); bg.setCornerRadius(dp(12)); bg.setStroke(dp(1), border);
        input.setBackground(bg);
        pageContent.addView(input, margin(0, 0, 0, 10));
        return input;
    }
    private Spinner spinner() {
        Spinner spinner = new Spinner(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE); bg.setCornerRadius(dp(12)); bg.setStroke(dp(1), border);
        spinner.setBackground(bg);
        spinner.setPadding(dp(10), dp(4), dp(10), dp(4));
        return spinner;
    }
    private void botonGuardar(String label, Runnable action) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label); button.setTextAllCaps(false); button.setTextSize(14);
        button.setCornerRadius(dp(12)); button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        button.setOnClickListener(v -> action.run());
        pageContent.addView(button, margin(0, 2, 0, 18));
    }
    private void elemento(String title, String detail, String state) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE); card.setRadius(dp(12)); card.setCardElevation(0);
        card.setStrokeColor(border); card.setStrokeWidth(dp(1));
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.addView(text(title, 14, ink, true));
        row.addView(text(detail == null ? "" : detail, 12, muted, false), margin(0, 4, 0, 0));
        TextView badge = text(state == null ? "" : state, 11, green, true);
        row.addView(badge, margin(0, 6, 0, 0));
        card.addView(row);
        pageContent.addView(card, margin(0, 0, 0, 8));
    }
    private void vacio(String message) {
        TextView empty = text(message, 13, muted, false);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(12), dp(20), dp(12), dp(20));
        pageContent.addView(empty);
    }
    private void aviso(String message) { if(statusView != null) statusView.setText(message); }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color);
        if(bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private String value(EditText input) { return input.getText() == null ? "" : input.getText().toString().trim(); }
    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(left), dp(top), dp(right), dp(bottom)); return lp;
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
}
