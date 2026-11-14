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
import android.widget.ScrollView;
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
import com.example.ferreteria.data.entity.ZoneEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private AuthRepository repository;
    private PedidoRepository pedidoRepository;
    private LinearLayout pageContent;
    private TextView statusView;
    private String activeSection = "Zonas";

    private LinearLayout tabZonas;
    private LinearLayout tabGranjas;
    private LinearLayout tabAuditoria;

    private final int navy = Color.rgb(43, 48, 71);
    private final int ink = Color.rgb(30, 34, 53);
    private final int muted = Color.rgb(104, 110, 130);
    private final int canvas = Color.rgb(250, 244, 237);
    private final int border = Color.rgb(226, 221, 213);
    private final int green = Color.rgb(0, 135, 90);
    private final int red = Color.rgb(186, 26, 26);
    private final int orange = Color.rgb(180, 100, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);
        repository = new AuthRepository(this);
        pedidoRepository = new PedidoRepository(this);

        construirInterfaz();

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

    private void construirInterfaz() {
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(canvas);

        // 1. Status Bar Navy Strip
        View statusBarBg = new View(this);
        statusBarBg.setId(View.generateViewId());
        statusBarBg.setBackgroundColor(navy);
        RelativeLayout.LayoutParams statusLp = new RelativeLayout.LayoutParams(-1, dp(24));
        statusLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        root.addView(statusBarBg, statusLp);

        // 2. Top App Bar oficial KRONO
        LinearLayout header = new LinearLayout(this);
        header.setId(View.generateViewId());
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(10), dp(16), dp(10));
        header.setBackgroundColor(Color.WHITE);
        header.setElevation(dp(2));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        header.addView(logo, new LinearLayout.LayoutParams(dp(36), dp(36)));

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setPadding(dp(12), 0, 0, 0);

        LinearLayout rowBrand = new LinearLayout(this);
        rowBrand.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("KRONO", 16, navy, true);
        rowBrand.addView(brand);
        TextView revTag = text("REV.", 12, muted, true);
        revTag.setPadding(dp(6), 0, 0, 0);
        rowBrand.addView(revTag);
        titleCol.addView(rowBrand);

        TextView subBrand = text("Administrador General", 12, muted, false);
        titleCol.addView(subBrand);

        header.addView(titleCol, new LinearLayout.LayoutParams(0, -2, 1));

        // Botón Cerrar Sesión (Regresar al Login)
        FrameLayout btnLogout = new FrameLayout(this);
        btnLogout.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_icon_container));
        btnLogout.setClickable(true);
        btnLogout.setFocusable(true);
        btnLogout.setOnClickListener(v -> finish());

        ImageView iconLogout = new ImageView(this);
        iconLogout.setImageResource(R.drawable.ic_logout);
        iconLogout.setImageTintList(ColorStateList.valueOf(navy));
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(dp(20), dp(20));
        iconLp.gravity = Gravity.CENTER;
        btnLogout.addView(iconLogout, iconLp);

        header.addView(btnLogout, new LinearLayout.LayoutParams(dp(38), dp(38)));

        RelativeLayout.LayoutParams headerLp = new RelativeLayout.LayoutParams(-1, dp(64));
        headerLp.addRule(RelativeLayout.BELOW, statusBarBg.getId());
        root.addView(header, headerLp);

        // 3. Bottom Navigation Bar con 3 tabs: Zonas, Granjas, Auditoría
        LinearLayout bottomNav = new LinearLayout(this);
        bottomNav.setId(View.generateViewId());
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setBackgroundColor(Color.WHITE);
        bottomNav.setElevation(dp(8));

        tabZonas = crearTab(R.drawable.ic_target, "Zonas");
        tabGranjas = crearTab(R.drawable.ic_truck, "Granjas");
        tabAuditoria = crearTab(R.drawable.ic_trending_up, "Auditoría");

        tabZonas.setOnClickListener(v -> mostrarSeccion("Zonas"));
        tabGranjas.setOnClickListener(v -> mostrarSeccion("Granjas"));
        tabAuditoria.setOnClickListener(v -> mostrarSeccion("Auditoría"));

        bottomNav.addView(tabZonas, new LinearLayout.LayoutParams(0, -1, 1));
        bottomNav.addView(tabGranjas, new LinearLayout.LayoutParams(0, -1, 1));
        bottomNav.addView(tabAuditoria, new LinearLayout.LayoutParams(0, -1, 1));

        RelativeLayout.LayoutParams bottomLp = new RelativeLayout.LayoutParams(-1, dp(64));
        bottomLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        root.addView(bottomNav, bottomLp);

        // 4. ScrollView de Contenido Central
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout scrollContent = new LinearLayout(this);
        scrollContent.setOrientation(LinearLayout.VERTICAL);

        pageContent = new LinearLayout(this);
        pageContent.setOrientation(LinearLayout.VERTICAL);
        pageContent.setPadding(dp(16), dp(14), dp(16), dp(28));
        scrollContent.addView(pageContent);

        statusView = text("", 13, green, true);
        statusView.setPadding(dp(18), dp(4), dp(18), dp(10));
        scrollContent.addView(statusView);

        scroll.addView(scrollContent);

        RelativeLayout.LayoutParams scrollLp = new RelativeLayout.LayoutParams(-1, -1);
        scrollLp.addRule(RelativeLayout.BELOW, header.getId());
        scrollLp.addRule(RelativeLayout.ABOVE, bottomNav.getId());
        root.addView(scroll, scrollLp);

        setContentView(root);
        StatusBarUtils.applyStatusBarNavyHeader(this);
    }

    private LinearLayout crearTab(int iconRes, String title) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setClickable(true);
        tab.setFocusable(true);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(muted));
        tab.addView(icon, new LinearLayout.LayoutParams(dp(20), dp(20)));

        TextView label = text(title, 10, muted, false);
        label.setPadding(0, dp(2), 0, 0);
        tab.addView(label);

        return tab;
    }

    private void actualizarTabs(String section) {
        actualizarTabItem(tabZonas, "Zonas".equals(section));
        actualizarTabItem(tabGranjas, "Granjas".equals(section));
        actualizarTabItem(tabAuditoria, "Auditoría".equals(section));
    }

    private void actualizarTabItem(LinearLayout tab, boolean active) {
        if (tab == null || tab.getChildCount() < 2) return;
        ImageView icon = (ImageView) tab.getChildAt(0);
        TextView label = (TextView) tab.getChildAt(1);

        int color = active ? navy : muted;
        icon.setImageTintList(ColorStateList.valueOf(color));
        label.setTextColor(color);
        label.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void mostrarSeccion(String section) {
        activeSection = section;
        pageContent.removeAllViews();
        statusView.setText("");
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
                aviso("Escribe el nombre de la zona.");
                return;
            }
            repository.crearZona(n, d, callback(() -> {
                aviso("Zona guardada exitosamente.");
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
                aviso("No se pudieron cargar las zonas: " + e.getMessage());
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
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_target);
        icon.setImageTintList(ColorStateList.valueOf(navy));
        row.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(12), 0, dp(8), 0);

        TextView title = text(zona.nombre, 15, ink, true);
        col.addView(title);

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

        card.addView(row);
        card.setOnClickListener(v -> mostrarGranjasDeZona(zona));

        pageContent.addView(card, margin(0, 0, 0, 10));
    }

    private void mostrarGranjasDeZona(ZoneEntity zona) {
        pageContent.removeAllViews();
        statusView.setText("");

        // Botón volver estilo píldora
        View btnVolver = botonPildora("Volver a Zonas", () -> mostrarSeccion("Zonas"));
        pageContent.addView(btnVolver, margin(0, 0, 0, 12));

        encabezado(zona.nombre, "Granjas registradas en esta zona. Toca una granja para auditar sus pedidos.");

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
                aviso("Error al cargar granjas: " + error.getMessage());
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
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_truck);
        icon.setImageTintList(ColorStateList.valueOf(navy));
        topRow.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));

        TextView title = text(granja.nombre, 15, ink, true);
        title.setPadding(dp(10), 0, 0, 0);
        topRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        ImageView chevron = new ImageView(this);
        chevron.setImageResource(R.drawable.ic_arrow_forward);
        chevron.setImageTintList(ColorStateList.valueOf(muted));
        topRow.addView(chevron, new LinearLayout.LayoutParams(dp(18), dp(18)));

        col.addView(topRow);

        // Chip de credenciales de la granja (La granja es la entidad)
        LinearLayout credBox = new LinearLayout(this);
        credBox.setOrientation(LinearLayout.HORIZONTAL);
        credBox.setPadding(dp(10), dp(6), dp(10), dp(6));
        GradientDrawable credBg = new GradientDrawable();
        credBg.setColor(Color.parseColor("#F5EFE6"));
        credBg.setCornerRadius(dp(8));
        credBox.setBackground(credBg);

        String clave = (granja.codigoAcceso != null && !granja.codigoAcceso.isEmpty()) ? granja.codigoAcceso : "E100";
        String pass = (granja.password != null && !granja.password.isEmpty()) ? granja.password : "12345678";

        TextView credText = text("🔑 Clave: " + clave + "   •   🔒 Clave PIN: " + pass, 11, navy, true);
        credBox.addView(credText);
        col.addView(credBox, margin(0, 8, 0, 4));

        TextView hint = text("Toca para consultar hojas de pedido enviadas por esta granja", 11, muted, false);
        col.addView(hint, margin(0, 2, 0, 0));

        card.addView(col);
        card.setOnClickListener(v -> mostrarPedidosDeGranja(granja, zonaPadre));

        pageContent.addView(card, margin(0, 0, 0, 10));
    }

    private void mostrarPedidosDeGranja(FarmEntity granja, ZoneEntity zonaPadre) {
        pageContent.removeAllViews();
        statusView.setText("");

        // Botón volver
        View btnVolver = botonPildora("Volver a Granjas de " + granja.zona, () -> {
            if (zonaPadre != null) {
                mostrarGranjasDeZona(zonaPadre);
            } else {
                mostrarSeccion("Zonas");
            }
        });
        pageContent.addView(btnVolver, margin(0, 0, 0, 12));

        encabezado("Pedidos de " + granja.nombre, "Historial de pedidos levantados por la entidad " + granja.nombre + " (" + granja.codigoAcceso + ").");

        pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
            @Override
            public void onSuccess(List<PedidoEntity> pedidos) {
                List<PedidoEntity> filtrados = new ArrayList<>();
                for (PedidoEntity p : pedidos) {
                    if (granja.nombre.equalsIgnoreCase(p.granja) ||
                        (p.operarioId != null && p.operarioId.equalsIgnoreCase(granja.codigoAcceso)) ||
                        (granja.nombre.contains("Laureles") && (p.operarioId != null && p.operarioId.contains("E100")))) {
                        filtrados.add(p);
                    }
                }

                if (filtrados.isEmpty()) {
                    vacio("No hay pedidos registrados aún para " + granja.nombre + ".");
                    return;
                }

                for (PedidoEntity p : filtrados) {
                    mostrarCardPedidoAdmin(p);
                }
            }

            @Override
            public void onError(Exception error) {
                aviso("Error al consultar pedidos: " + error.getMessage());
            }
        });
    }

    private void mostrarCardPedidoAdmin(PedidoEntity pedido) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(14));
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));
        card.setCardElevation(dp(1));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(14), dp(12), dp(14), dp(12));

        // Cabecera: Folio y Estado
        RelativeLayout topRow = new RelativeLayout(this);

        TextView folio = text(pedido.codigo != null ? pedido.codigo : ("PED-" + pedido.id), 15, ink, true);
        RelativeLayout.LayoutParams fLp = new RelativeLayout.LayoutParams(-2, -2);
        fLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        topRow.addView(folio, fLp);

        TextView badgeEstado = text(pedido.estado != null ? pedido.estado : "ENVIADO", 11,
                "APROBADO".equalsIgnoreCase(pedido.estado) ? green :
                "RECHAZADO".equalsIgnoreCase(pedido.estado) ? red : orange, true);
        badgeEstado.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable bBg = new GradientDrawable();
        bBg.setColor("APROBADO".equalsIgnoreCase(pedido.estado) ? Color.parseColor("#E6F4EA") :
                "RECHAZADO".equalsIgnoreCase(pedido.estado) ? Color.parseColor("#FCE8E6") : Color.parseColor("#FEF7E0"));
        bBg.setCornerRadius(dp(6));
        badgeEstado.setBackground(bBg);

        RelativeLayout.LayoutParams bLp = new RelativeLayout.LayoutParams(-2, -2);
        bLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        bLp.addRule(RelativeLayout.CENTER_VERTICAL);
        topRow.addView(badgeEstado, bLp);

        col.addView(topRow);

        String fecha = android.text.format.DateFormat.format("dd MMM yyyy • HH:mm", pedido.creadoEn > 0 ? pedido.creadoEn : System.currentTimeMillis()).toString();
        TextView txtFecha = text("Fecha de envío: " + fecha, 12, muted, false);
        col.addView(txtFecha, margin(0, 4, 0, 2));

        TextView txtResp = text("Operario / Entidad: " + (pedido.operarioId != null ? pedido.operarioId : "E100"), 12, muted, false);
        col.addView(txtResp, margin(0, 0, 0, 6));

        // Insumos del pedido
        pedidoRepository.obtenerInsumos(pedido.id, new PedidoRepository.InsumosCallback() {
            @Override
            public void onSuccess(List<InsumoPedidoEntity> insumos) {
                if (insumos.isEmpty()) {
                    TextView txtItem = text("• Suministros base de campo registrados", 11, ink, false);
                    col.addView(txtItem);
                } else {
                    for (InsumoPedidoEntity item : insumos) {
                        TextView txtItem = text("• " + item.nombre + " — " + item.cantidad, 12, ink, false);
                        col.addView(txtItem, margin(0, 2, 0, 2));
                    }
                }
            }

            @Override
            public void onError(Exception error) {}
        });

        card.addView(col);
        pageContent.addView(card, margin(0, 0, 0, 10));
    }

    // ==========================================
    // 2. PANEL GRANJAS (CREACIÓN CON CLAVE Y CONTRASEÑA)
    // ==========================================
    private void panelGranjas() {
        encabezado("Registro de Granjas y Accesos",
                "Cada granja es la entidad de acceso con su propia clave y contraseña (el operario accede con los datos de su granja).");

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

        TextView zoneLabel = text("Zona Operativa Asignada", 12, muted, true);
        box.addView(zoneLabel, margin(0, 2, 0, 4));

        Spinner zone = spinner();
        box.addView(zone, margin(0, 0, 0, 10));

        repository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override
            public void onSuccess(List<ZoneEntity> zones) {
                List<String> labels = new ArrayList<>();
                for (ZoneEntity z : zones) if (z.activa) labels.add(z.nombre);
                if (labels.isEmpty()) labels.add("Zona Norte");
                zone.setAdapter(new ArrayAdapter<>(AdminActivity.this, android.R.layout.simple_spinner_dropdown_item, labels));
            }

            @Override
            public void onError(Exception e) {
                List<String> fallback = new ArrayList<>();
                fallback.add("Zona Norte");
                fallback.add("Zona Valle Central");
                fallback.add("Zona Sur");
                zone.setAdapter(new ArrayAdapter<>(AdminActivity.this, android.R.layout.simple_spinner_dropdown_item, fallback));
            }
        });

        // Clave / Código Institucional para la granja
        TextView lblClave = text("Clave / Código Institucional de la Granja", 12, muted, true);
        box.addView(lblClave, margin(0, 4, 0, 4));
        EditText codeInput = campo("Código de acceso (ej: E103 o G-04)");
        box.addView(codeInput, margin(0, 0, 0, 10));

        // Contraseña para la granja
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
            Object selectedZone = zone.getSelectedItem();
            String c = value(codeInput);
            String p = value(passInput);

            if (n.isEmpty()) {
                aviso("Escribe el nombre de la granja.");
                return;
            }
            if (c.isEmpty()) {
                aviso("Escribe la clave o código para la granja.");
                return;
            }
            if (p.isEmpty()) {
                p = "12345678";
            }
            String zName = selectedZone != null ? selectedZone.toString() : "Zona Norte";

            repository.crearGranja(n, zName, c, p, callback(() -> {
                aviso("Granja '" + n + "' registrada con clave '" + c + "'.");
                Toast.makeText(AdminActivity.this, "Granja y credenciales creadas exitosamente", Toast.LENGTH_SHORT).show();
                mostrarSeccion("Granjas");
            }));
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
                aviso("No se pudieron cargar las granjas.");
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

        RelativeLayout rowTop = new RelativeLayout(this);

        TextView title = text(granja.nombre, 15, ink, true);
        RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(-2, -2);
        tLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        rowTop.addView(title, tLp);

        TextView zonaBadge = text(granja.zona != null ? granja.zona : "Zona Norte", 11, navy, true);
        zonaBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
        GradientDrawable zBg = new GradientDrawable();
        zBg.setColor(Color.parseColor("#EDE7DF"));
        zBg.setCornerRadius(dp(6));
        zonaBadge.setBackground(zBg);
        RelativeLayout.LayoutParams zLp = new RelativeLayout.LayoutParams(-2, -2);
        zLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        zLp.addRule(RelativeLayout.CENTER_VERTICAL);
        rowTop.addView(zonaBadge, zLp);

        col.addView(rowTop);

        // Credenciales
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
        col.addView(credRow, margin(0, 8, 0, 8));

        // Botón ver pedidos
        MaterialButton btnVerPedidos = new MaterialButton(this);
        btnVerPedidos.setText("Ver Hojas de Pedido");
        btnVerPedidos.setAllCaps(false);
        btnVerPedidos.setTextSize(12);
        btnVerPedidos.setTextColor(navy);
        btnVerPedidos.setStrokeColor(ColorStateList.valueOf(border));
        btnVerPedidos.setStrokeWidth(dp(1));
        btnVerPedidos.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnVerPedidos.setCornerRadius(dp(8));
        btnVerPedidos.setOnClickListener(v -> mostrarPedidosDeGranja(granja, null));
        col.addView(btnVerPedidos);

        card.addView(col);
        pageContent.addView(card, margin(0, 0, 0, 10));
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
                for (AuditEntity item : items) {
                    String time = android.text.format.DateFormat.format("dd/MM/yyyy • HH:mm", item.fecha).toString();
                    elemento(item.accion + " • " + item.entidad, item.detalle + "\nPor: " + item.usuarioId, time);
                }
            }

            @Override
            public void onError(Exception e) {
                aviso("No se pudo cargar la auditoría: " + e.getMessage());
            }
        });
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
                aviso(error.getMessage());
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

    private void aviso(String message) {
        if (statusView != null) statusView.setText(message);
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
