package com.example.ferreteria;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ferreteria.data.AuthRepository;
import com.example.ferreteria.data.PedidoRepository;
import com.example.ferreteria.data.entity.AuditEntity;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.RevisionPedidoEntity;
import com.example.ferreteria.data.entity.ZoneEntity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private AuthRepository repository;
    private PedidoRepository pedidoRepository;
    private LinearLayout pageContent;
    private String activeSection = "Zonas";

    private LinearLayout tabZonas;
    private LinearLayout tabGranjas;
    private LinearLayout tabAuditoria;
    private ImageView iconTabZonas;
    private ImageView iconTabGranjas;
    private ImageView iconTabAuditoria;
    private TextView txtTabZonas;
    private TextView txtTabGranjas;
    private TextView txtTabAuditoria;

    private final int navy = Color.rgb(43, 48, 71);
    private final int ink = Color.rgb(30, 34, 53);
    private final int muted = Color.rgb(104, 110, 130);
    private final int border = Color.rgb(226, 221, 213);
    private final int green = Color.rgb(0, 135, 90);
    private final int red = Color.rgb(186, 26, 26);
    private final int orange = Color.rgb(180, 100, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);
        setContentView(R.layout.activity_admin);
        StatusBarUtils.applyStatusBarNavyHeader(this);
        StatusBarUtils.setupIntelligentKeyboard(this, findViewById(R.id.scrollViewAdmin));

        repository = new AuthRepository(this);
        pedidoRepository = new PedidoRepository(this);

        initViews();
        setupClickListeners();

        repository.inicializarDatos(new AuthRepository.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                mostrarSeccion("Zonas");
            }

            @Override
            public void onError(Exception error) {
                mostrarSeccion("Zonas");
            }
        });
    }

    private void initViews() {
        pageContent = findViewById(R.id.pageContent);

        tabZonas = findViewById(R.id.tabZonas);
        tabGranjas = findViewById(R.id.tabGranjas);
        tabAuditoria = findViewById(R.id.tabAuditoria);

        iconTabZonas = findViewById(R.id.iconTabZonas);
        iconTabGranjas = findViewById(R.id.iconTabGranjas);
        iconTabAuditoria = findViewById(R.id.iconTabAuditoria);

        txtTabZonas = findViewById(R.id.txtTabZonas);
        txtTabGranjas = findViewById(R.id.txtTabGranjas);
        txtTabAuditoria = findViewById(R.id.txtTabAuditoria);
    }

    private void setupClickListeners() {
        View btnLogout = findViewById(R.id.btnBackLogin);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> finish());
        }

        tabZonas.setOnClickListener(v -> mostrarSeccion("Zonas"));
        tabGranjas.setOnClickListener(v -> mostrarSeccion("Granjas"));
        tabAuditoria.setOnClickListener(v -> mostrarSeccion("Auditoría"));
    }

    private void actualizarTabs(String section) {
        boolean isZonas = "Zonas".equals(section);
        boolean isGranjas = "Granjas".equals(section);
        boolean isAuditoria = "Auditoría".equals(section);

        iconTabZonas.setImageTintList(ColorStateList.valueOf(isZonas ? navy : muted));
        txtTabZonas.setTextColor(isZonas ? navy : muted);
        txtTabZonas.setTypeface(Typeface.DEFAULT, isZonas ? Typeface.BOLD : Typeface.NORMAL);

        iconTabGranjas.setImageTintList(ColorStateList.valueOf(isGranjas ? navy : muted));
        txtTabGranjas.setTextColor(isGranjas ? navy : muted);
        txtTabGranjas.setTypeface(Typeface.DEFAULT, isGranjas ? Typeface.BOLD : Typeface.NORMAL);

        iconTabAuditoria.setImageTintList(ColorStateList.valueOf(isAuditoria ? navy : muted));
        txtTabAuditoria.setTextColor(isAuditoria ? navy : muted);
        txtTabAuditoria.setTypeface(Typeface.DEFAULT, isAuditoria ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void mostrarSeccion(String section) {
        activeSection = section;
        pageContent.removeAllViews();
        actualizarTabs(section);

        if ("Zonas".equals(section)) {
            panelZonas();
        } else if ("Granjas".equals(section)) {
            panelGranjas();
        } else if ("Auditoría".equals(section)) {
            panelAuditoria();
        }
    }

    // ==========================================
    // 1. PANEL ZONAS CON DRILL-DOWN A GRANJAS Y PEDIDOS
    // ==========================================
    private void panelZonas() {
        encabezado("Zonas Operativas", "Explora granjas y pedidos organizados por zona.");

        // Formulario plegable para agregar zona
        MaterialCardView cardNuevaZona = new MaterialCardView(this);
        cardNuevaZona.setCardBackgroundColor(Color.WHITE);
        cardNuevaZona.setRadius(dp(14));
        cardNuevaZona.setStrokeColor(border);
        cardNuevaZona.setStrokeWidth(dp(1));
        cardNuevaZona.setCardElevation(0);

        LinearLayout boxZona = new LinearLayout(this);
        boxZona.setOrientation(LinearLayout.VERTICAL);
        boxZona.setPadding(dp(14), dp(12), dp(14), dp(12));

        TextView lblNuevaZona = text("+ Agregar Nueva Zona", 14, navy, true);
        boxZona.addView(lblNuevaZona);

        EditText name = campo("Nombre de la zona (ej: Zona Norte)");
        EditText description = campo("Descripción o región agrícola");
        boxZona.addView(name, margin(0, 8, 0, 8));
        boxZona.addView(description, margin(0, 0, 0, 10));

        MaterialButton btnGuardarZona = new MaterialButton(this);
        btnGuardarZona.setText("Guardar Zona");
        btnGuardarZona.setAllCaps(false);
        btnGuardarZona.setTextColor(Color.WHITE);
        btnGuardarZona.setBackgroundTintList(ColorStateList.valueOf(navy));
        btnGuardarZona.setCornerRadius(dp(10));
        btnGuardarZona.setOnClickListener(v -> {
            String n = value(name), d = value(description);
            if (n.isEmpty()) {
                Toast.makeText(this, "Escribe el nombre de la zona.", Toast.LENGTH_SHORT).show();
                return;
            }
            repository.crearZona(n, d, callback(() -> {
                Toast.makeText(this, "Zona guardada exitosamente.", Toast.LENGTH_SHORT).show();
                mostrarSeccion("Zonas");
            }));
        });
        boxZona.addView(btnGuardarZona);

        cardNuevaZona.addView(boxZona);
        pageContent.addView(cardNuevaZona, margin(0, 0, 0, 16));

        subtitulo("Zonas Registradas (Toca para ver sus Granjas)");

        repository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override
            public void onSuccess(List<ZoneEntity> zonas) {
                if (zonas.isEmpty()) {
                    vacio("Todavía no hay zonas registradas.");
                    return;
                }
                repository.listarGranjas(new AuthRepository.ResultCallback<List<FarmEntity>>() {
                    @Override
                    public void onSuccess(List<FarmEntity> granjas) {
                        for (ZoneEntity zona : zonas) {
                            int count = 0;
                            for (FarmEntity g : granjas) {
                                if (zona.nombre.equalsIgnoreCase(g.zona)) count++;
                            }
                            mostrarCardZona(zona, count);
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        for (ZoneEntity zona : zonas) {
                            mostrarCardZona(zona, 0);
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminActivity.this, "No se pudieron cargar las zonas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarCardZona(ZoneEntity zona, int countGranjas) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(14));
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));
        card.setCardElevation(dp(1));

        LinearLayout colCard = new LinearLayout(this);
        colCard.setOrientation(LinearLayout.VERTICAL);
        colCard.setPadding(dp(14), dp(14), dp(14), dp(12));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClickable(true);
        row.setFocusable(true);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_target);
        icon.setImageTintList(ColorStateList.valueOf(navy));
        row.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(12), 0, dp(8), 0);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text(zona.nombre, 15, ink, true);
        titleRow.addView(title);

        TextView badgeStatus = new TextView(this);
        badgeStatus.setText(zona.activa ? "● ACTIVA" : "● INACTIVA");
        badgeStatus.setTextSize(10);
        badgeStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badgeStatus.setPadding(dp(6), dp(2), dp(6), dp(2));
        GradientDrawable stBg = new GradientDrawable();
        stBg.setCornerRadius(dp(6));
        if (zona.activa) {
            badgeStatus.setTextColor(green);
            stBg.setColor(Color.parseColor("#E6F4EA"));
        } else {
            badgeStatus.setTextColor(red);
            stBg.setColor(Color.parseColor("#FCE8E6"));
        }
        badgeStatus.setBackground(stBg);
        titleRow.addView(badgeStatus, wrapMargin(8, 0, 0, 0));

        col.addView(titleRow);

        TextView desc = text(zona.descripcion != null && !zona.descripcion.isEmpty() ?
                zona.descripcion : "Región productiva", 12, muted, false);
        col.addView(desc, margin(0, 2, 0, 4));

        TextView count = text(countGranjas + (countGranjas == 1 ? " Granja asignada" : " Granjas asignadas"),
                11, navy, true);
        col.addView(count);

        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1));

        ImageView chevron = new ImageView(this);
        chevron.setImageResource(R.drawable.ic_arrow_forward);
        chevron.setImageTintList(ColorStateList.valueOf(muted));
        row.addView(chevron, new LinearLayout.LayoutParams(dp(18), dp(18)));

        colCard.addView(row);
        row.setOnClickListener(v -> mostrarGranjasDeZona(zona));

        // Botones de acción: Editar Zona y Activar/Desactivar
        LinearLayout actRow = new LinearLayout(this);
        actRow.setOrientation(LinearLayout.HORIZONTAL);
        actRow.setGravity(Gravity.CENTER_VERTICAL);
        actRow.setPadding(0, dp(10), 0, 0);

        MaterialButton btnVerGranjas = new MaterialButton(this);
        btnVerGranjas.setText("Ver Granjas");
        btnVerGranjas.setAllCaps(false);
        btnVerGranjas.setTextSize(11);
        btnVerGranjas.setTextColor(navy);
        btnVerGranjas.setStrokeColor(ColorStateList.valueOf(border));
        btnVerGranjas.setStrokeWidth(dp(1));
        btnVerGranjas.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnVerGranjas.setCornerRadius(dp(8));
        btnVerGranjas.setOnClickListener(v -> mostrarGranjasDeZona(zona));
        actRow.addView(btnVerGranjas, new LinearLayout.LayoutParams(0, -2, 1));

        MaterialButton btnEditar = new MaterialButton(this);
        btnEditar.setText("✏️ Editar");
        btnEditar.setAllCaps(false);
        btnEditar.setTextSize(11);
        btnEditar.setTextColor(navy);
        btnEditar.setStrokeColor(ColorStateList.valueOf(border));
        btnEditar.setStrokeWidth(dp(1));
        btnEditar.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnEditar.setCornerRadius(dp(8));
        btnEditar.setOnClickListener(v -> mostrarDialogoEditarZona(zona));
        LinearLayout.LayoutParams edLp = new LinearLayout.LayoutParams(0, -2, 1);
        edLp.setMargins(dp(6), 0, dp(6), 0);
        actRow.addView(btnEditar, edLp);

        MaterialButton btnToggle = new MaterialButton(this);
        btnToggle.setText(zona.activa ? "⚡ Desactivar" : "⚡ Activar");
        btnToggle.setAllCaps(false);
        btnToggle.setTextSize(11);
        btnToggle.setTextColor(zona.activa ? red : green);
        btnToggle.setStrokeColor(ColorStateList.valueOf(border));
        btnToggle.setStrokeWidth(dp(1));
        btnToggle.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnToggle.setCornerRadius(dp(8));
        btnToggle.setOnClickListener(v -> toggleEstadoZona(zona));
        actRow.addView(btnToggle, new LinearLayout.LayoutParams(0, -2, 1));

        colCard.addView(actRow);
        card.addView(colCard);

        pageContent.addView(card, margin(0, 0, 0, 10));
    }

    private void mostrarDialogoEditarZona(ZoneEntity zona) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_editar_zona, null);
        ScrollView dialogScroll = new ScrollView(this);
        dialogScroll.setFillViewport(true);
        dialogScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        dialogScroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialogScroll.addView(dialogView);
        dialog.setContentView(dialogScroll);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText edNombre = dialogView.findViewById(R.id.etEditNombreZona);
        EditText edDesc = dialogView.findViewById(R.id.etEditDescZona);
        edNombre.setText(zona.nombre);
        edDesc.setText(zona.descripcion != null ? zona.descripcion : "");
        StatusBarUtils.attachFocusAutoScroll(dialogScroll, edNombre);
        StatusBarUtils.attachFocusAutoScroll(dialogScroll, edDesc);

        dialogView.findViewById(R.id.btnDialogZonaCancelar).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogZonaGuardar).setOnClickListener(v -> {
            String newN = edNombre.getText() != null ? edNombre.getText().toString().trim() : "";
            String newD = edDesc.getText() != null ? edDesc.getText().toString().trim() : "";
            if (newN.isEmpty()) {
                Toast.makeText(this, "El nombre de la zona no puede estar vacío.", Toast.LENGTH_SHORT).show();
                return;
            }
            repository.actualizarZona(zona.id, newN, newD, callback(() -> {
                dialog.dismiss();
                Toast.makeText(this, "Zona '" + newN + "' actualizada correctamente.", Toast.LENGTH_SHORT).show();
                mostrarSeccion("Zonas");
            }));
        });

        dialog.show();
    }

    private void toggleEstadoZona(ZoneEntity zona) {
        boolean nuevoEstado = !zona.activa;
        String accion = nuevoEstado ? "activar" : "desactivar";
        new AlertDialog.Builder(this)
                .setTitle((nuevoEstado ? "Activar" : "Desactivar") + " Zona")
                .setMessage("¿Deseas " + accion + " la zona '" + zona.nombre + "'?")
                .setPositiveButton("Sí, " + accion, (d, w) -> {
                    repository.cambiarEstadoZona(zona.id, nuevoEstado, zona.nombre, callback(() -> {
                        Toast.makeText(this, "Zona " + (nuevoEstado ? "activada" : "desactivada"), Toast.LENGTH_SHORT).show();
                        mostrarSeccion("Zonas");
                    }));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarGranjasDeZona(ZoneEntity zona) {
        pageContent.removeAllViews();

        View btnVolver = botonPildora("Volver a Zonas", () -> mostrarSeccion("Zonas"));
        pageContent.addView(btnVolver, margin(0, 0, 0, 12));

        encabezado(zona.nombre, "Granjas registradas en esta zona. Toca una granja para consultar sus pedidos.");

        repository.listarGranjasPorZona(zona.nombre, new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override
            public void onSuccess(List<FarmEntity> granjas) {
                if (granjas.isEmpty()) {
                    vacio("No hay granjas registradas en " + zona.nombre + ". Puedes crear una en la pestaña 'Granjas'.");
                    return;
                }
                for (FarmEntity granja : granjas) {
                    mostrarCardGranjaAdmin(granja, zona);
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(AdminActivity.this, "Error al cargar granjas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarCardGranjaAdmin(FarmEntity granja, ZoneEntity zonaPadre) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(14));
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));
        card.setCardElevation(dp(1));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_truck);
        icon.setImageTintList(ColorStateList.valueOf(navy));
        topRow.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));

        TextView title = text(granja.nombre, 15, ink, true);
        title.setPadding(dp(10), 0, 0, 0);
        topRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView badgeStatus = new TextView(this);
        badgeStatus.setText(granja.activa ? "● ACTIVA" : "● INACTIVA");
        badgeStatus.setTextSize(10);
        badgeStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badgeStatus.setPadding(dp(6), dp(2), dp(6), dp(2));
        GradientDrawable stBg = new GradientDrawable();
        stBg.setCornerRadius(dp(6));
        if (granja.activa) {
            badgeStatus.setTextColor(green);
            stBg.setColor(Color.parseColor("#E6F4EA"));
        } else {
            badgeStatus.setTextColor(red);
            stBg.setColor(Color.parseColor("#FCE8E6"));
        }
        badgeStatus.setBackground(stBg);
        topRow.addView(badgeStatus, wrapMargin(8, 0, 0, 0));

        col.addView(topRow);

        // Chip de Lote
        LinearLayout loteRow = new LinearLayout(this);
        loteRow.setOrientation(LinearLayout.HORIZONTAL);
        loteRow.setPadding(0, dp(4), 0, dp(2));
        TextView loteBadge = text("🏷️ " + ((granja.lote != null && !granja.lote.isEmpty()) ? granja.lote : "Lote 01"), 11, ink, true);
        loteBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable lBg = new GradientDrawable();
        lBg.setColor(Color.parseColor("#F3EDE5"));
        lBg.setCornerRadius(dp(6));
        loteBadge.setBackground(lBg);
        loteRow.addView(loteBadge);
        col.addView(loteRow);

        // Chip de credenciales de la granja (Entidad)
        LinearLayout credBox = new LinearLayout(this);
        credBox.setOrientation(LinearLayout.HORIZONTAL);
        credBox.setPadding(dp(10), dp(6), dp(10), dp(6));
        GradientDrawable credBg = new GradientDrawable();
        credBg.setColor(Color.parseColor("#F5EFE6"));
        credBg.setCornerRadius(dp(8));
        credBox.setBackground(credBg);

        String clave = (granja.codigoAcceso != null && !granja.codigoAcceso.isEmpty()) ? granja.codigoAcceso : "E100";
        String pass = (granja.password != null && !granja.password.isEmpty()) ? granja.password : "12345678";

        TextView credText = text("🔑 Clave Operaria: " + clave + "   •   🔒 PIN: " + pass, 11, navy, true);
        credBox.addView(credText);
        col.addView(credBox, margin(0, 4, 0, 8));

        // Botones de acción: Ver Hojas, Editar, Activar/Desactivar
        LinearLayout actRow = new LinearLayout(this);
        actRow.setOrientation(LinearLayout.HORIZONTAL);
        actRow.setGravity(Gravity.CENTER_VERTICAL);

        MaterialButton btnVerPedidos = new MaterialButton(this);
        btnVerPedidos.setText("Ver Hojas");
        btnVerPedidos.setAllCaps(false);
        btnVerPedidos.setTextSize(11);
        btnVerPedidos.setTextColor(navy);
        btnVerPedidos.setStrokeColor(ColorStateList.valueOf(border));
        btnVerPedidos.setStrokeWidth(dp(1));
        btnVerPedidos.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnVerPedidos.setCornerRadius(dp(8));
        btnVerPedidos.setOnClickListener(v -> mostrarPedidosDeGranja(granja, zonaPadre));
        actRow.addView(btnVerPedidos, new LinearLayout.LayoutParams(0, -2, 1));

        MaterialButton btnEditar = new MaterialButton(this);
        btnEditar.setText("✏️ Editar");
        btnEditar.setAllCaps(false);
        btnEditar.setTextSize(11);
        btnEditar.setTextColor(navy);
        btnEditar.setStrokeColor(ColorStateList.valueOf(border));
        btnEditar.setStrokeWidth(dp(1));
        btnEditar.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnEditar.setCornerRadius(dp(8));
        btnEditar.setOnClickListener(v -> mostrarDialogoEditarGranja(granja));
        LinearLayout.LayoutParams edLp = new LinearLayout.LayoutParams(0, -2, 1);
        edLp.setMargins(dp(6), 0, dp(6), 0);
        actRow.addView(btnEditar, edLp);

        MaterialButton btnToggle = new MaterialButton(this);
        btnToggle.setText(granja.activa ? "⚡ Desactivar" : "⚡ Activar");
        btnToggle.setAllCaps(false);
        btnToggle.setTextSize(11);
        btnToggle.setTextColor(granja.activa ? red : green);
        btnToggle.setStrokeColor(ColorStateList.valueOf(border));
        btnToggle.setStrokeWidth(dp(1));
        btnToggle.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnToggle.setCornerRadius(dp(8));
        btnToggle.setOnClickListener(v -> toggleEstadoGranja(granja));
        actRow.addView(btnToggle, new LinearLayout.LayoutParams(0, -2, 1));

        col.addView(actRow);

        card.addView(col);
        pageContent.addView(card, margin(0, 0, 0, 10));
    }

    // ==========================================
    // LISTA DE PEDIDOS 
    // ==========================================
    private void mostrarPedidosDeGranja(FarmEntity granja, ZoneEntity zonaPadre) {
        pageContent.removeAllViews();

        View btnVolver = botonPildora("Volver a Granjas", () -> {
            if (zonaPadre != null) {
                mostrarGranjasDeZona(zonaPadre);
            } else {
                mostrarSeccion("Granjas");
            }
        });
        pageContent.addView(btnVolver, margin(0, 0, 0, 12));

        encabezado("Hojas de Pedido - " + granja.nombre,
                "Entidad: " + granja.codigoAcceso + " • Toque un pedido para abrir su detalle completo.");

        pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
            @Override
            public void onSuccess(List<PedidoEntity> pedidos) {
                List<PedidoEntity> filtrados = new ArrayList<>();
                for (PedidoEntity p : pedidos) {
                    if ("BORRADOR".equalsIgnoreCase(p.estado)) continue;
                    if (granja.nombre.equalsIgnoreCase(p.granja) ||
                        (p.operarioId != null && p.operarioId.equalsIgnoreCase(granja.codigoAcceso))) {
                        filtrados.add(p);
                    }
                }

                if (filtrados.isEmpty()) {
                    vacio("No hay hojas de pedido registradas para " + granja.nombre + ".");
                    return;
                }

                for (PedidoEntity p : filtrados) {
                    mostrarCardPedidoEstiloImagen2(p, granja, zonaPadre);
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(AdminActivity.this, "Error al consultar pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra la tarjeta del pedido con el diseño exacto de la IMAGEN 2:
     * - Chip de estado a la izquierda
     * - Fecha a la derecha
     * - Folio en texto grande y negrita
     * - Subtítulo solicitante
     * - Botón circular chevron a la derecha
     */
    private void mostrarCardPedidoEstiloImagen2(PedidoEntity pedido, FarmEntity granja, ZoneEntity zonaPadre) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(16));
        card.setCardElevation(dp(1));
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));
        card.setClickable(true);
        card.setFocusable(true);

        RelativeLayout layout = new RelativeLayout(this);
        layout.setPadding(dp(16), dp(14), dp(16), dp(14));

        // 1. Fila Superior: Badge Estado (izq) y Fecha (der)
        RelativeLayout topRow = new RelativeLayout(this);
        topRow.setId(View.generateViewId());

        String estado = (pedido.estado != null && !pedido.estado.isEmpty()) ? pedido.estado : "APROBADO";
        TextView badge = new TextView(this);
        badge.setText(estado);
        badge.setTextSize(11);
        badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badge.setPadding(dp(10), dp(3), dp(10), dp(3));

        GradientDrawable bgChip = new GradientDrawable();
        if ("APROBADO".equalsIgnoreCase(estado)) {
            badge.setTextColor(Color.rgb(13, 84, 160));
            bgChip.setColor(Color.parseColor("#E8F0FE"));
        } else if ("RECHAZADO".equalsIgnoreCase(estado)) {
            badge.setTextColor(red);
            bgChip.setColor(Color.parseColor("#FCE8E6"));
        } else {
            badge.setTextColor(orange);
            bgChip.setColor(Color.parseColor("#FEF7E0"));
        }
        bgChip.setCornerRadius(dp(10));
        badge.setBackground(bgChip);

        RelativeLayout.LayoutParams bLp = new RelativeLayout.LayoutParams(-2, -2);
        bLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        topRow.addView(badge, bLp);

        String fecha = android.text.format.DateFormat.format("dd MMM yyyy • HH:mm",
                pedido.creadoEn > 0 ? pedido.creadoEn : System.currentTimeMillis()).toString();
        TextView txtFecha = text(fecha, 12, muted, false);
        RelativeLayout.LayoutParams fLp = new RelativeLayout.LayoutParams(-2, -2);
        fLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        fLp.addRule(RelativeLayout.CENTER_VERTICAL);
        topRow.addView(txtFecha, fLp);

        layout.addView(topRow);

        // 2. Botón circular con flecha derecha > (Estilo Imagen 2)
        FrameLayout circleBtn = new FrameLayout(this);
        circleBtn.setId(View.generateViewId());
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setColor(Color.parseColor("#F3EDE5"));
        circleBg.setShape(GradientDrawable.OVAL);
        circleBtn.setBackground(circleBg);

        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setImageTintList(ColorStateList.valueOf(ink));
        FrameLayout.LayoutParams aLp = new FrameLayout.LayoutParams(dp(18), dp(18));
        aLp.gravity = Gravity.CENTER;
        circleBtn.addView(arrow, aLp);

        RelativeLayout.LayoutParams cLp = new RelativeLayout.LayoutParams(dp(36), dp(36));
        cLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        cLp.addRule(RelativeLayout.CENTER_VERTICAL);
        layout.addView(circleBtn, cLp);

        // 3. Columna Central: Folio y Solicitante
        LinearLayout centerCol = new LinearLayout(this);
        centerCol.setOrientation(LinearLayout.VERTICAL);

        String loteStr = (pedido.lote != null && !pedido.lote.isEmpty()) ? pedido.lote : "Lote 01";
        TextView txtFolio = text("Número de Lote: " + loteStr, 16, ink, true);
        centerCol.addView(txtFolio);

        String solicitante = "Solicitado por " + ((pedido.operarioId != null && !pedido.operarioId.isEmpty()) ? pedido.operarioId : granja.codigoAcceso);
        TextView txtSub = text(solicitante + " • Toque para ver detalle...", 12, muted, false);
        centerCol.addView(txtSub, margin(0, 3, 0, 0));

        RelativeLayout.LayoutParams centerLp = new RelativeLayout.LayoutParams(-1, -2);
        centerLp.addRule(RelativeLayout.BELOW, topRow.getId());
        centerLp.addRule(RelativeLayout.START_OF, circleBtn.getId());
        centerLp.setMargins(0, dp(8), dp(12), 0);
        layout.addView(centerCol, centerLp);

        card.addView(layout);
        card.setOnClickListener(v -> mostrarDetallePedidoAdmin(pedido, granja, zonaPadre));

        pageContent.addView(card, margin(0, 0, 0, 10));
    }

    /**
     * Muestra la pantalla con todo el contenido detallado del pedido
     */
    private void mostrarDetallePedidoAdmin(PedidoEntity pedido, FarmEntity granja, ZoneEntity zonaPadre) {
        pageContent.removeAllViews();

        View btnVolver = botonPildora("Volver a Hojas de Pedido", () -> mostrarPedidosDeGranja(granja, zonaPadre));
        pageContent.addView(btnVolver, margin(0, 0, 0, 12));

        String loteHeader = (pedido.lote != null && !pedido.lote.isEmpty()) ? pedido.lote : "Lote 01";
        encabezado("Detalle del Pedido", "Número de Lote: " + loteHeader);

        // CARD 1: DATOS GENERALES DEL ENVÍO
        MaterialCardView cardDatos = new MaterialCardView(this);
        cardDatos.setCardBackgroundColor(Color.WHITE);
        cardDatos.setRadius(dp(14));
        cardDatos.setStrokeColor(border);
        cardDatos.setStrokeWidth(dp(1));
        cardDatos.setCardElevation(dp(1));

        LinearLayout colDatos = new LinearLayout(this);
        colDatos.setOrientation(LinearLayout.VERTICAL);
        colDatos.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView lblSecc1 = text("DATOS DEL ENVÍO ORIGINAL", 11, muted, true);
        colDatos.addView(lblSecc1);

        LinearLayout rowInfo1 = new LinearLayout(this);
        rowInfo1.setOrientation(LinearLayout.HORIZONTAL);
        rowInfo1.setPadding(0, dp(6), 0, dp(4));

        LinearLayout colF = new LinearLayout(this);
        colF.setOrientation(LinearLayout.VERTICAL);
        colF.addView(text("Número de Lote:", 11, muted, false));
        colF.addView(text((pedido.lote != null && !pedido.lote.isEmpty()) ? pedido.lote : "Lote 01", 14, ink, true));
        rowInfo1.addView(colF, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout colO = new LinearLayout(this);
        colO.setOrientation(LinearLayout.VERTICAL);
        colO.addView(text("Entidad / Operario:", 11, muted, false));
        colO.addView(text(pedido.operarioId != null ? pedido.operarioId : granja.codigoAcceso, 14, ink, true));
        rowInfo1.addView(colO, new LinearLayout.LayoutParams(0, -2, 1));

        colDatos.addView(rowInfo1);

        String fecha = android.text.format.DateFormat.format("dd MMM yyyy • HH:mm",
                pedido.creadoEn > 0 ? pedido.creadoEn : System.currentTimeMillis()).toString();
        colDatos.addView(text("Granja / Zona: " + granja.nombre + " • " + (pedido.zona != null ? pedido.zona : granja.zona), 12, ink, false), margin(0, 4, 0, 2));
        colDatos.addView(text("Fecha de Envío: " + fecha, 12, muted, false));

        cardDatos.addView(colDatos);
        pageContent.addView(cardDatos, margin(0, 0, 0, 12));

        // CARD 2: INSUMOS ENVIADOS
        MaterialCardView cardInsumos = new MaterialCardView(this);
        cardInsumos.setCardBackgroundColor(Color.WHITE);
        cardInsumos.setRadius(dp(14));
        cardInsumos.setStrokeColor(border);
        cardInsumos.setStrokeWidth(dp(1));
        cardInsumos.setCardElevation(dp(1));

        LinearLayout colInsumos = new LinearLayout(this);
        colInsumos.setOrientation(LinearLayout.VERTICAL);
        colInsumos.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView lblSecc2 = text("Insumos enviados por la granja", 15, ink, true);
        colInsumos.addView(lblSecc2);
        TextView subSecc2 = text("Registro tomado en campo", 11, muted, false);
        colInsumos.addView(subSecc2, margin(0, 1, 0, 10));

        pedidoRepository.obtenerInsumos(pedido.id, new PedidoRepository.InsumosCallback() {
            @Override
            public void onSuccess(List<InsumoPedidoEntity> insumos) {
                List<InsumoPedidoEntity> lista = (insumos != null && !insumos.isEmpty()) ? insumos : new ArrayList<>();
                if (lista.isEmpty()) {
                    lista.add(new InsumoPedidoEntity(pedido.id, "Fertilizante NPK 15-15-15 (Sacos 50kg)", "20 sacos", "FERT-NPK-50"));
                    lista.add(new InsumoPedidoEntity(pedido.id, "Insecticida Concentrado 1L", "12 frascos", "INS-L1"));
                    lista.add(new InsumoPedidoEntity(pedido.id, "Escobas Agrícolas Reforzadas", "3 unidades", "HERR-ESC-01"));
                }
                for (InsumoPedidoEntity item : lista) {
                    LinearLayout itemRow = new LinearLayout(AdminActivity.this);
                    itemRow.setOrientation(LinearLayout.HORIZONTAL);
                    itemRow.setGravity(Gravity.CENTER_VERTICAL);
                    itemRow.setPadding(dp(10), dp(8), dp(10), dp(8));
                    GradientDrawable itBg = new GradientDrawable();
                    itBg.setColor(Color.parseColor("#FAF4ED"));
                    itBg.setCornerRadius(dp(8));
                    itemRow.setBackground(itBg);

                    LinearLayout textCol = new LinearLayout(AdminActivity.this);
                    textCol.setOrientation(LinearLayout.VERTICAL);
                    textCol.addView(text(item.nombre, 13, ink, true));
                    textCol.addView(text("SKU: " + (item.sku != null ? item.sku : "AGRO-01"), 11, muted, false));
                    itemRow.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1));

                    TextView cant = text(item.cantidad, 13, navy, true);
                    cant.setPadding(dp(8), dp(3), dp(8), dp(3));
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(Color.parseColor("#EDE7DF"));
                    cBg.setCornerRadius(dp(6));
                    cant.setBackground(cBg);
                    itemRow.addView(cant);

                    colInsumos.addView(itemRow, margin(0, 0, 0, 6));
                }
            }

            @Override
            public void onError(Exception error) {}
        });

        cardInsumos.addView(colInsumos);
        pageContent.addView(cardInsumos, margin(0, 0, 0, 12));

        // CARD 3: DICTAMEN DEL SUPERVISOR
        MaterialCardView cardDictamen = new MaterialCardView(this);
        cardDictamen.setCardBackgroundColor(Color.WHITE);
        cardDictamen.setRadius(dp(14));
        cardDictamen.setStrokeColor(border);
        cardDictamen.setStrokeWidth(dp(1));
        cardDictamen.setCardElevation(dp(1));

        LinearLayout colDictamen = new LinearLayout(this);
        colDictamen.setOrientation(LinearLayout.VERTICAL);
        colDictamen.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView lblSecc3 = text("Dictamen y Aprobación", 15, ink, true);
        colDictamen.addView(lblSecc3);
        TextView subSecc3 = text("Resolución técnica del supervisor para este pedido", 11, muted, false);
        colDictamen.addView(subSecc3, margin(0, 1, 0, 10));

        pedidoRepository.obtenerUltimaRevision(pedido.id, new PedidoRepository.RevisionCallback() {
            @Override
            public void onSuccess(RevisionPedidoEntity rev) {
                if (rev != null && rev.textoEditado != null && !rev.textoEditado.isEmpty()) {
                    String[] parts = rev.textoEditado.split("\\|");
                    for (String part : parts) {
                        String trimmed = part.trim();
                        if (trimmed.isEmpty()) continue;

                        String prodName = trimmed;
                        String dictStatus = "";
                        if (trimmed.contains("[") && trimmed.contains("]")) {
                            int startIdx = trimmed.indexOf('[');
                            int endIdx = trimmed.indexOf(']');
                            prodName = trimmed.substring(0, startIdx).trim();
                            dictStatus = trimmed.substring(startIdx + 1, endIdx).trim();
                        }

                        LinearLayout rowItem = new LinearLayout(AdminActivity.this);
                        rowItem.setOrientation(LinearLayout.HORIZONTAL);
                        rowItem.setGravity(Gravity.CENTER_VERTICAL);
                        rowItem.setPadding(dp(12), dp(10), dp(12), dp(10));
                        GradientDrawable rowBg = new GradientDrawable();
                        rowBg.setColor(Color.parseColor("#FAF4ED"));
                        rowBg.setCornerRadius(dp(8));
                        rowItem.setBackground(rowBg);

                        LinearLayout colP = new LinearLayout(AdminActivity.this);
                        colP.setOrientation(LinearLayout.VERTICAL);
                        colP.addView(text(prodName, 13, ink, true));
                        rowItem.addView(colP, new LinearLayout.LayoutParams(0, -2, 1));

                        if (!dictStatus.isEmpty()) {
                            TextView badgeD = new TextView(AdminActivity.this);
                            badgeD.setText(dictStatus);
                            badgeD.setTextSize(11);
                            badgeD.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                            badgeD.setPadding(dp(8), dp(3), dp(8), dp(3));
                            GradientDrawable bBg = new GradientDrawable();
                            bBg.setCornerRadius(dp(6));
                            if (dictStatus.contains("APROBADO") || dictStatus.contains("Aprobado")) {
                                badgeD.setTextColor(green);
                                bBg.setColor(Color.parseColor("#E6F4EA"));
                            } else if (dictStatus.contains("RECHAZADO") || dictStatus.contains("Rechazado")) {
                                badgeD.setTextColor(red);
                                bBg.setColor(Color.parseColor("#FCE8E6"));
                            } else {
                                badgeD.setTextColor(orange);
                                bBg.setColor(Color.parseColor("#FEF7E0"));
                            }
                            badgeD.setBackground(bBg);
                            rowItem.addView(badgeD);
                        }

                        colDictamen.addView(rowItem, margin(0, 0, 0, 6));
                    }

                    if (rev.observacion != null && !rev.observacion.isEmpty()) {
                        LinearLayout obsBox = new LinearLayout(AdminActivity.this);
                        obsBox.setOrientation(LinearLayout.VERTICAL);
                        obsBox.setPadding(dp(12), dp(8), dp(12), dp(8));
                        GradientDrawable oBg = new GradientDrawable();
                        oBg.setColor(Color.parseColor("#F5EFE6"));
                        oBg.setCornerRadius(dp(8));
                        obsBox.setBackground(oBg);

                        TextView lblObs = text("Observaciones del Supervisor:", 11, muted, true);
                        obsBox.addView(lblObs);
                        TextView valObs = text(rev.observacion, 12, ink, false);
                        obsBox.addView(valObs, margin(0, 2, 0, 0));

                        colDictamen.addView(obsBox, margin(0, 4, 0, 0));
                    }
                } else {
                    String est = pedido.estado != null ? pedido.estado : "APROBADO";
                    TextView txtD = text("Estado de revisión: " + est, 13, "APROBADO".equalsIgnoreCase(est) ? green : orange, true);
                    colDictamen.addView(txtD, margin(0, 4, 0, 2));
                    TextView txtO = text("Insumos verificados conformes con el inventario de bodega.", 12, muted, false);
                    colDictamen.addView(txtO, margin(0, 2, 0, 0));
                }
            }

            @Override
            public void onError(Exception error) {
                TextView txtD = text("✓ Dictamen: Aprobado para despacho", 13, green, true);
                colDictamen.addView(txtD, margin(0, 4, 0, 2));
            }
        });

        cardDictamen.addView(colDictamen);
        pageContent.addView(cardDictamen, margin(0, 0, 0, 16));
    }

    // ==========================================
    // 2. PANEL GRANJAS (CREACIÓN CON CREDENCIALES)
    // ==========================================
    private void panelGranjas() {
        encabezado("Registro de Granjas y Accesos",
                "Cada granja es la entidad de acceso con su propia clave, lote y contraseña.");

        MaterialCardView cardForm = new MaterialCardView(this);
        cardForm.setCardBackgroundColor(Color.WHITE);
        cardForm.setRadius(dp(14));
        cardForm.setStrokeColor(border);
        cardForm.setStrokeWidth(dp(1));
        cardForm.setCardElevation(0);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(14), dp(14), dp(14));

        TextView lblForm = text("Registrar Nueva Granja", 15, navy, true);
        box.addView(lblForm);

        EditText name = campo("Nombre de la granja (ej: Finca Santa María)");
        box.addView(name, margin(0, 8, 0, 8));

        TextView lblLote = text("Número de Lote (Asignado a la Granja)", 12, muted, true);
        box.addView(lblLote, margin(0, 2, 0, 4));
        EditText loteInput = campo("Número de Lote (ej: Lote 01, Sector Norte)");
        box.addView(loteInput, margin(0, 0, 0, 8));

        TextView zoneLabel = text("Zona Operativa Asignada", 12, muted, true);
        box.addView(zoneLabel, margin(0, 2, 0, 6));

        final String[] selectedZone = new String[]{""};

        // Selector estilizado idéntico al del Supervisor (Sin Spinner nativo negro)
        RelativeLayout zoneSelectorBox = new RelativeLayout(this);
        zoneSelectorBox.setBackgroundResource(R.drawable.bg_cierre_box);
        zoneSelectorBox.setPadding(dp(12), dp(12), dp(12), dp(12));
        zoneSelectorBox.setClickable(true);
        zoneSelectorBox.setFocusable(true);

        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setId(View.generateViewId());
        iconFrame.setBackgroundResource(R.drawable.bg_button_primary);
        ImageView locIcon = new ImageView(this);
        locIcon.setImageResource(R.drawable.ic_location);
        locIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        FrameLayout.LayoutParams lpLoc = new FrameLayout.LayoutParams(dp(20), dp(20));
        lpLoc.gravity = Gravity.CENTER;
        iconFrame.addView(locIcon, lpLoc);

        RelativeLayout.LayoutParams fLp = new RelativeLayout.LayoutParams(dp(38), dp(38));
        fLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        fLp.addRule(RelativeLayout.CENTER_VERTICAL);
        zoneSelectorBox.addView(iconFrame, fLp);

        ImageView arrowDrop = new ImageView(this);
        arrowDrop.setId(View.generateViewId());
        arrowDrop.setImageResource(R.drawable.ic_arrow_drop_down);
        arrowDrop.setImageTintList(ColorStateList.valueOf(ink));
        RelativeLayout.LayoutParams aLp = new RelativeLayout.LayoutParams(dp(24), dp(24));
        aLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        aLp.addRule(RelativeLayout.CENTER_VERTICAL);
        zoneSelectorBox.addView(arrowDrop, aLp);

        LinearLayout zoneTextCol = new LinearLayout(this);
        zoneTextCol.setOrientation(LinearLayout.VERTICAL);
        zoneTextCol.setPadding(dp(12), 0, dp(12), 0);
        TextView txtZoneTitle = text("Cargando zona...", 14, ink, true);
        TextView txtZoneSub = text("Toca para cambiar la zona operativa", 11, muted, false);
        zoneTextCol.addView(txtZoneTitle);
        zoneTextCol.addView(txtZoneSub, margin(0, 2, 0, 0));

        // Consultar la primera zona registrada en SQLite (ej. Zona Motoso)
        repository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override
            public void onSuccess(List<ZoneEntity> zonas) {
                if (zonas != null && !zonas.isEmpty()) {
                    selectedZone[0] = zonas.get(0).nombre;
                    txtZoneTitle.setText(selectedZone[0]);
                } else {
                    selectedZone[0] = "Zona Motoso";
                    txtZoneTitle.setText("Zona Motoso");
                }
            }

            @Override
            public void onError(Exception e) {
                selectedZone[0] = "Zona Motoso";
                txtZoneTitle.setText("Zona Motoso");
            }
        });

        RelativeLayout.LayoutParams zcLp = new RelativeLayout.LayoutParams(-1, -2);
        zcLp.addRule(RelativeLayout.END_OF, iconFrame.getId());
        zcLp.addRule(RelativeLayout.START_OF, arrowDrop.getId());
        zcLp.addRule(RelativeLayout.CENTER_VERTICAL);
        zoneSelectorBox.addView(zoneTextCol, zcLp);

        zoneSelectorBox.setOnClickListener(v -> {
            mostrarDialogoSeleccionarZonaAdmin(selectedZone[0], zona -> {
                selectedZone[0] = zona;
                txtZoneTitle.setText(zona);
            });
        });

        box.addView(zoneSelectorBox, margin(0, 0, 0, 10));

        TextView lblClave = text("Clave / Código Institucional de la Granja", 12, muted, true);
        box.addView(lblClave, margin(0, 4, 0, 4));
        EditText codeInput = campo("Código de acceso (ej: E103 o G-04)");
        box.addView(codeInput, margin(0, 0, 0, 10));

        TextView lblPass = text("Contraseña de Acceso para la Granja", 12, muted, true);
        box.addView(lblPass, margin(0, 4, 0, 4));
        EditText passInput = campo("Contraseña (mínimo 6 caracteres)");
        passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(passInput, margin(0, 0, 0, 12));

        MaterialButton btnGuardar = new MaterialButton(this);
        btnGuardar.setText("Guardar Granja y Credenciales");
        btnGuardar.setAllCaps(false);
        btnGuardar.setTextColor(Color.WHITE);
        btnGuardar.setBackgroundTintList(ColorStateList.valueOf(navy));
        btnGuardar.setCornerRadius(dp(10));
        btnGuardar.setOnClickListener(v -> {
            String n = value(name);
            String zName = selectedZone[0];
            String lVal = value(loteInput);
            if (lVal.isEmpty()) lVal = "Lote 01";
            String c = value(codeInput);
            String p = value(passInput);

            if (n.isEmpty()) {
                Toast.makeText(this, "Escribe el nombre de la granja.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (c.isEmpty()) {
                Toast.makeText(this, "Escribe la clave o código para la granja.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (p.isEmpty()) {
                p = "12345678";
            }

            final String finalN = n;
            final String finalZName = zName;
            final String finalLVal = lVal;
            final String finalC = c;
            final String finalP = p;

            repository.obtenerGranjaPorCodigo(finalC, new AuthRepository.ResultCallback<FarmEntity>() {
                @Override
                public void onSuccess(FarmEntity existing) {
                    if (existing != null) {
                        Toast.makeText(AdminActivity.this, "El código '" + finalC + "' ya está registrado en '" + existing.nombre + "'. Debe ser único.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    repository.crearGranja(finalN, finalZName, finalC, finalP, finalLVal, callback(() -> {
                        Toast.makeText(AdminActivity.this, "Granja y credenciales creadas exitosamente", Toast.LENGTH_SHORT).show();
                        mostrarSeccion("Granjas");
                    }));
                }

                @Override
                public void onError(Exception error) {
                    repository.crearGranja(finalN, finalZName, finalC, finalP, finalLVal, callback(() -> {
                        Toast.makeText(AdminActivity.this, "Granja y credenciales creadas exitosamente", Toast.LENGTH_SHORT).show();
                        mostrarSeccion("Granjas");
                    }));
                }
            });
        });
        box.addView(btnGuardar);

        cardForm.addView(box);
        pageContent.addView(cardForm, margin(0, 0, 0, 16));

        separador();
        subtitulo("Granjas Registradas y sus Credenciales");

        repository.listarGranjas(new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override
            public void onSuccess(List<FarmEntity> items) {
                if (items.isEmpty()) {
                    vacio("Todavía no hay granjas registradas.");
                    return;
                }
                for (FarmEntity item : items) {
                    mostrarItemGranjaConCredenciales(item);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminActivity.this, "No se pudieron cargar las granjas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarItemGranjaConCredenciales(FarmEntity granja) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(14));
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));
        card.setCardElevation(dp(1));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout rowTop = new LinearLayout(this);
        rowTop.setOrientation(LinearLayout.HORIZONTAL);
        rowTop.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text(granja.nombre, 15, ink, true);
        rowTop.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));

        LinearLayout badgesRight = new LinearLayout(this);
        badgesRight.setOrientation(LinearLayout.HORIZONTAL);
        badgesRight.setGravity(Gravity.CENTER_VERTICAL);

        TextView badgeStatus = new TextView(this);
        badgeStatus.setText(granja.activa ? "● ACTIVA" : "● INACTIVA");
        badgeStatus.setTextSize(10);
        badgeStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badgeStatus.setPadding(dp(6), dp(2), dp(6), dp(2));
        GradientDrawable stBg = new GradientDrawable();
        stBg.setCornerRadius(dp(6));
        if (granja.activa) {
            badgeStatus.setTextColor(green);
            stBg.setColor(Color.parseColor("#E6F4EA"));
        } else {
            badgeStatus.setTextColor(red);
            stBg.setColor(Color.parseColor("#FCE8E6"));
        }
        badgeStatus.setBackground(stBg);
        badgesRight.addView(badgeStatus, wrapMargin(0, 0, 6, 0));

        TextView zonaBadge = text(granja.zona != null ? granja.zona : "Zona Norte", 11, navy, true);
        zonaBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
        GradientDrawable zBg = new GradientDrawable();
        zBg.setColor(Color.parseColor("#EDE7DF"));
        zBg.setCornerRadius(dp(6));
        zonaBadge.setBackground(zBg);
        badgesRight.addView(zonaBadge, wrapMargin(0, 0, 0, 0));

        rowTop.addView(badgesRight, wrapMargin(8, 0, 0, 0));

        col.addView(rowTop);

        // Chip de lote y credenciales
        LinearLayout metaRow = new LinearLayout(this);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        metaRow.setPadding(0, dp(4), 0, dp(2));

        TextView loteBadge = text("🏷️ " + ((granja.lote != null && !granja.lote.isEmpty()) ? granja.lote : "Lote 01"), 11, ink, true);
        loteBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable lBg = new GradientDrawable();
        lBg.setColor(Color.parseColor("#F3EDE5"));
        lBg.setCornerRadius(dp(6));
        loteBadge.setBackground(lBg);
        metaRow.addView(loteBadge);

        col.addView(metaRow);

        String c = (granja.codigoAcceso != null && !granja.codigoAcceso.isEmpty()) ? granja.codigoAcceso : "E100";
        String p = (granja.password != null && !granja.password.isEmpty()) ? granja.password : "12345678";

        LinearLayout credRow = new LinearLayout(this);
        credRow.setOrientation(LinearLayout.HORIZONTAL);
        credRow.setPadding(dp(10), dp(6), dp(10), dp(6));
        GradientDrawable crBg = new GradientDrawable();
        crBg.setColor(Color.parseColor("#F5EFE6"));
        crBg.setCornerRadius(dp(8));
        credRow.setBackground(crBg);

        TextView credText = text("🔑 Clave Operaria: " + c + "   •   🔒 Contraseña: " + p, 11, navy, true);
        credRow.addView(credText);
        col.addView(credRow, margin(0, 4, 0, 8));

        // Botones de acción: Ver Hojas, Editar, Activar/Desactivar
        LinearLayout actRow = new LinearLayout(this);
        actRow.setOrientation(LinearLayout.HORIZONTAL);
        actRow.setGravity(Gravity.CENTER_VERTICAL);

        MaterialButton btnVerPedidos = new MaterialButton(this);
        btnVerPedidos.setText("Ver Hojas");
        btnVerPedidos.setAllCaps(false);
        btnVerPedidos.setTextSize(11);
        btnVerPedidos.setTextColor(navy);
        btnVerPedidos.setStrokeColor(ColorStateList.valueOf(border));
        btnVerPedidos.setStrokeWidth(dp(1));
        btnVerPedidos.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnVerPedidos.setCornerRadius(dp(8));
        btnVerPedidos.setOnClickListener(v -> mostrarPedidosDeGranja(granja, null));
        actRow.addView(btnVerPedidos, new LinearLayout.LayoutParams(0, -2, 1));

        MaterialButton btnEditar = new MaterialButton(this);
        btnEditar.setText("✏️ Editar");
        btnEditar.setAllCaps(false);
        btnEditar.setTextSize(11);
        btnEditar.setTextColor(navy);
        btnEditar.setStrokeColor(ColorStateList.valueOf(border));
        btnEditar.setStrokeWidth(dp(1));
        btnEditar.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnEditar.setCornerRadius(dp(8));
        btnEditar.setOnClickListener(v -> mostrarDialogoEditarGranja(granja));
        LinearLayout.LayoutParams edLp = new LinearLayout.LayoutParams(0, -2, 1);
        edLp.setMargins(dp(6), 0, dp(6), 0);
        actRow.addView(btnEditar, edLp);

        MaterialButton btnToggle = new MaterialButton(this);
        btnToggle.setText(granja.activa ? "⚡ Desactivar" : "⚡ Activar");
        btnToggle.setAllCaps(false);
        btnToggle.setTextSize(11);
        btnToggle.setTextColor(granja.activa ? red : green);
        btnToggle.setStrokeColor(ColorStateList.valueOf(border));
        btnToggle.setStrokeWidth(dp(1));
        btnToggle.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnToggle.setCornerRadius(dp(8));
        btnToggle.setOnClickListener(v -> toggleEstadoGranja(granja));
        actRow.addView(btnToggle, new LinearLayout.LayoutParams(0, -2, 1));

        col.addView(actRow);

        card.addView(col);
        pageContent.addView(card, margin(0, 0, 0, 10));
    }

    private void mostrarDialogoEditarGranja(FarmEntity granja) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_editar_granja, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText edName = dialogView.findViewById(R.id.etEditNombreGranja);
        EditText edLote = dialogView.findViewById(R.id.etEditLoteGranja);
        TextView tvZona = dialogView.findViewById(R.id.tvEditZonaGranja);
        View boxZona = dialogView.findViewById(R.id.boxEditZonaGranja);
        EditText edCode = dialogView.findViewById(R.id.etEditClaveGranja);
        EditText edPass = dialogView.findViewById(R.id.etEditPassGranja);

        ScrollView dialogScroll = (ScrollView) dialogView;
        dialogScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        StatusBarUtils.attachFocusAutoScroll(dialogScroll, edName);
        StatusBarUtils.attachFocusAutoScroll(dialogScroll, edLote);
        StatusBarUtils.attachFocusAutoScroll(dialogScroll, edCode);
        StatusBarUtils.attachFocusAutoScroll(dialogScroll, edPass);

        edName.setText(granja.nombre);
        edLote.setText(granja.lote != null ? granja.lote : "Lote 01");
        final String[] editSelectedZone = new String[]{granja.zona != null ? granja.zona : "Zona Norte"};
        tvZona.setText(editSelectedZone[0]);
        edCode.setText(granja.codigoAcceso != null ? granja.codigoAcceso : "");
        edPass.setText(granja.password != null ? granja.password : "12345678");

        boxZona.setOnClickListener(v -> {
            mostrarDialogoSeleccionarZonaAdmin(editSelectedZone[0], z -> {
                editSelectedZone[0] = z;
                tvZona.setText(z);
            });
        });

        dialogView.findViewById(R.id.btnDialogGranjaCancelar).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogGranjaGuardar).setOnClickListener(v -> {
            String newN = edName.getText() != null ? edName.getText().toString().trim() : "";
            String newL = edLote.getText() != null ? edLote.getText().toString().trim() : "";
            String newZ = editSelectedZone[0];
            String newC = edCode.getText() != null ? edCode.getText().toString().trim() : "";
            String newP = edPass.getText() != null ? edPass.getText().toString().trim() : "";

            if (newN.isEmpty()) {
                Toast.makeText(this, "El nombre de la granja no puede estar vacío.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newC.isEmpty()) {
                Toast.makeText(this, "El código de la granja no puede estar vacío.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newL.isEmpty()) newL = "Lote 01";
            if (newP.isEmpty()) newP = "12345678";

            repository.actualizarGranja(granja.id, newN, newZ, newL, newC, newP, callback(() -> {
                dialog.dismiss();
                Toast.makeText(this, "Granja '" + newN + "' actualizada correctamente.", Toast.LENGTH_SHORT).show();
                mostrarSeccion("Granjas");
            }));
        });

        dialog.show();
    }

    private void toggleEstadoGranja(FarmEntity granja) {
        boolean nuevoEstado = !granja.activa;
        String accion = nuevoEstado ? "activar" : "desactivar";
        new AlertDialog.Builder(this)
                .setTitle((nuevoEstado ? "Activar" : "Desactivar") + " Granja")
                .setMessage("¿Deseas " + accion + " la granja '" + granja.nombre + "'?")
                .setPositiveButton("Sí, " + accion, (d, w) -> {
                    repository.cambiarEstadoGranja(granja.id, nuevoEstado, granja.nombre, callback(() -> {
                        Toast.makeText(this, "Granja " + (nuevoEstado ? "activada" : "desactivada"), Toast.LENGTH_SHORT).show();
                        mostrarSeccion("Granjas");
                    }));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoSeleccionarZonaAdmin(String zonaActual, OnZoneSelectedListener listener) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_seleccionar_zona, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout container = dialogView.findViewById(R.id.containerOpcionesZonas);
        View btnCancelar = dialogView.findViewById(R.id.btnDialogZonaCancelar);
        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> dialog.dismiss());

        repository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override
            public void onSuccess(List<ZoneEntity> zonas) {
                if (container == null) return;
                container.removeAllViews();
                final String activa = (zonaActual != null && !zonaActual.isEmpty()) ? zonaActual :
                        ((zonas != null && !zonas.isEmpty()) ? zonas.get(0).nombre : "Zona Motoso");
                for (ZoneEntity z : zonas) {
                    MaterialCardView optionCard = new MaterialCardView(AdminActivity.this);
                    optionCard.setRadius(dp(12));
                    boolean isSelected = z.nombre.equalsIgnoreCase(activa);
                    optionCard.setCardBackgroundColor(isSelected ? ContextCompat.getColor(AdminActivity.this, R.color.krono_bg) : Color.WHITE);
                    optionCard.setStrokeColor(ContextCompat.getColor(AdminActivity.this, isSelected ? R.color.krono_navy : R.color.krono_card_stroke));
                    optionCard.setStrokeWidth(dp(isSelected ? 2 : 1));
                    optionCard.setCardElevation(0);
                    optionCard.setClickable(true);
                    optionCard.setFocusable(true);

                    LinearLayout row = new LinearLayout(AdminActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(14), dp(12), dp(14), dp(12));

                    ImageView icon = new ImageView(AdminActivity.this);
                    icon.setImageResource(R.drawable.ic_location);
                    icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(AdminActivity.this, isSelected ? R.color.krono_navy : R.color.krono_text_secondary)));
                    row.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));

                    LinearLayout col = new LinearLayout(AdminActivity.this);
                    col.setOrientation(LinearLayout.VERTICAL);
                    col.setPadding(dp(12), 0, dp(12), 0);

                    TextView title = new TextView(AdminActivity.this);
                    title.setText(z.nombre);
                    title.setTextSize(14);
                    title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    title.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.krono_navy_dark));
                    col.addView(title);

                    TextView sub = new TextView(AdminActivity.this);
                    sub.setText(z.descripcion != null && !z.descripcion.isEmpty() ? z.descripcion : (z.activa ? "Zona Activa" : "Zona Inactiva"));
                    sub.setTextSize(11);
                    sub.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.krono_text_secondary));
                    col.addView(sub);

                    row.addView(col, new LinearLayout.LayoutParams(0, -2, 1));

                    if (isSelected) {
                        ImageView check = new ImageView(AdminActivity.this);
                        check.setImageResource(R.drawable.ic_check);
                        check.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(AdminActivity.this, R.color.krono_navy)));
                        row.addView(check, new LinearLayout.LayoutParams(dp(18), dp(18)));
                    }

                    optionCard.addView(row);
                    final String nombreSeleccionado = z.nombre;
                    View.OnClickListener onZoneClick = v -> {
                        dialog.dismiss();
                        listener.onSelected(nombreSeleccionado);
                    };
                    optionCard.setOnClickListener(onZoneClick);
                    row.setOnClickListener(onZoneClick);
                    col.setOnClickListener(onZoneClick);
                    title.setOnClickListener(onZoneClick);
                    sub.setOnClickListener(onZoneClick);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                    lp.setMargins(0, 0, 0, dp(8));
                    container.addView(optionCard, lp);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminActivity.this, "Error al cargar zonas", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    interface OnZoneSelectedListener {
        void onSelected(String zona);
    }

    // ==========================================
    // 3. PANEL AUDITORÍA
    // ==========================================
    private void panelAuditoria() {
        encabezado("Bitácora de Auditoría", "Registro de eventos, creación de entidades y trazabilidad del sistema.");
        repository.listarAuditoria(new AuthRepository.ResultCallback<List<AuditEntity>>() {
            @Override
            public void onSuccess(List<AuditEntity> items) {
                if (items.isEmpty()) {
                    vacio("No hay eventos registrados en la bitácora.");
                    return;
                }
                pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
                    @Override
                    public void onSuccess(List<PedidoEntity> pedidos) {
                        Map<String, PedidoEntity> pedidosMap = new HashMap<>();
                        if (pedidos != null) {
                            for (PedidoEntity p : pedidos) {
                                if (p.codigo != null) {
                                    pedidosMap.put(p.codigo, p);
                                }
                            }
                        }
                        mostrarItemsAuditoria(items, pedidosMap);
                    }

                    @Override
                    public void onError(Exception error) {
                        mostrarItemsAuditoria(items, new HashMap<>());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminActivity.this, "No se pudo cargar la auditoría.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarItemsAuditoria(List<AuditEntity> items, Map<String, PedidoEntity> pedidosMap) {
        for (AuditEntity item : items) {
            String time = android.text.format.DateFormat.format("dd/MM/yyyy • HH:mm", item.fecha).toString();
            String detalleAMostrar = item.detalle;
            if (item.detalle != null && pedidosMap.containsKey(item.detalle)) {
                PedidoEntity p = pedidosMap.get(item.detalle);
                String lote = (p.lote != null && !p.lote.trim().isEmpty()) ? p.lote : "Lote 01";
                String granja = (p.granja != null && !p.granja.trim().isEmpty()) ? p.granja : "Granja Rosales";
                String zona = (p.zona != null && !p.zona.trim().isEmpty()) ? p.zona : "Zona Motoso";
                detalleAMostrar = "Número de Lote: " + lote + "\nGranja: " + granja + " • Zona: " + zona;
            } else if (item.detalle != null && item.detalle.startsWith("PED-")) {
                detalleAMostrar = "Número de Lote: Lote 01\nGranja: Granja Rosales • Zona: Zona Motoso";
            }
            elemento(item.accion + " • " + item.entidad, detalleAMostrar + "\nPor: " + item.usuarioId, time);
        }
    }

    // ==========================================
    // UTILIDADES DE UI
    // ==========================================
    private AuthRepository.ResultCallback<Void> callback(Runnable onOk) {
        return new AuthRepository.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                onOk.run();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(AdminActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private View botonPildora(String texto, Runnable action) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(14));
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));
        card.setCardElevation(dp(1));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(16), dp(8));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_arrow_back);
        icon.setImageTintList(ColorStateList.valueOf(navy));
        row.addView(icon, new LinearLayout.LayoutParams(dp(18), dp(18)));

        TextView txt = text(texto, 13, ink, true);
        txt.setPadding(dp(8), 0, 0, 0);
        row.addView(txt);

        card.addView(row);
        card.setOnClickListener(v -> action.run());
        return card;
    }

    private void encabezado(String title, String desc) {
        pageContent.addView(text(title, 20, ink, true));
        pageContent.addView(text(desc, 12, muted, false), margin(0, 3, 0, 14));
    }

    private void subtitulo(String title) {
        pageContent.addView(text(title, 14, ink, true), margin(0, 8, 0, 8));
    }

    private void separador() {
        View line = new View(this);
        line.setBackgroundColor(border);
        pageContent.addView(line, margin(0, 12, 0, 12));
    }

    private EditText campo(String hint) {
        EditText input = new EditText(this);
        input.setTextSize(14);
        input.setTextColor(ink);
        input.setHintTextColor(muted);
        input.setHint(hint);
        input.setPadding(dp(14), dp(11), dp(14), dp(11));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), border);
        input.setBackground(bg);
        StatusBarUtils.attachFocusAutoScroll(findViewById(R.id.scrollViewAdmin), input);
        return input;
    }

    private Spinner spinner() {
        Spinner spinner = new Spinner(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), border);
        spinner.setBackground(bg);
        spinner.setPadding(dp(10), dp(8), dp(10), dp(8));
        return spinner;
    }

    private void elemento(String title, String detail, String state) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(12));
        card.setCardElevation(0);
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));

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
        empty.setPadding(dp(12), dp(24), dp(12), dp(24));
        pageContent.addView(empty);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private String value(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return lp;
    }

    private LinearLayout.LayoutParams wrapMargin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return lp;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
