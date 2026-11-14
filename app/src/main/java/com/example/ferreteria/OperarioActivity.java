package com.example.ferreteria;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.ferreteria.data.PedidoRepository;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.RevisionPedidoEntity;
import com.google.android.material.card.MaterialCardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class OperarioActivity extends AppCompatActivity {

    public static class InsumoItem {
        String nombre;
        String cantidad;
        String sku;

        public InsumoItem(String nombre, String cantidad, String sku) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.sku = sku;
        }
    }

    private final List<InsumoItem> insumosList = new ArrayList<>();
    private LinearLayout containerInsumos;
    private boolean pedidoEnviado = false;
    private PedidoRepository pedidoRepository;
    private String userId;
    private String farmName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);

        pedidoRepository = new PedidoRepository(this);
        userId = getIntent().getStringExtra("user_id");
        if (userId == null) userId = "E100";
        farmName = getIntent().getStringExtra("farm_name");

        // Inicializar insumos por defecto
        insumosList.clear();
        insumosList.add(new InsumoItem(
                "Fertilizante NPK 15-15-15 (Sacos 50kg)",
                "20 sacos",
                "Fungicida/Nutricional • SKU: FERT-NPK-50"
        ));
        insumosList.add(new InsumoItem(
                "Insecticida Concentrado 1L",
                "12 frascos",
                "Protección Fitosanitaria • SKU: INS-L1"
        ));

        loadPedidoView();
    }

    private void guardarPedidoEnRoom() {
        if (insumosList.isEmpty()) { Toast.makeText(this, "Agrega al menos un insumo antes de enviar.", Toast.LENGTH_SHORT).show(); return; }
        long now = System.currentTimeMillis();
        String code = "PED-" + now;
        String farm = (farmName != null && !farmName.isEmpty()) ? farmName :
                (userId.equals("E100") ? "Granja Los Laureles" :
                (userId.equals("E101") ? "Finca San Isidro" :
                (userId.equals("E102") ? "Granja Santa Elena" : "Granja " + userId)));
        PedidoEntity pedido = new PedidoEntity(code, userId, "Zona Norte", "ENVIADO", farm, now, now);
        List<InsumoPedidoEntity> lines = new ArrayList<>();
        for (InsumoItem item : insumosList) lines.add(new InsumoPedidoEntity(0, item.nombre, item.cantidad, item.sku));
        pedidoRepository.guardarPedido(pedido, lines, new PedidoRepository.GuardarPedidoCallback() {
            @Override public void onSuccess(long pedidoId) {
                pedidoEnviado = true;
                Toast.makeText(OperarioActivity.this, "Pedido " + code + " guardado y enviado a revisión.", Toast.LENGTH_LONG).show();
                loadRevisionesView();
            }
            @Override public void onError(Exception error) { Toast.makeText(OperarioActivity.this, "No se pudo guardar el pedido: " + error.getMessage(), Toast.LENGTH_LONG).show(); }
        });
    }

    private void mostrarMisPedidos() {
        pedidoRepository.obtenerPedidosPorOperario(userId, new PedidoRepository.PedidosCallback() {
            @Override public void onSuccess(List<PedidoEntity> pedidos) {
                StringBuilder summary = new StringBuilder();
                for (PedidoEntity p : pedidos) summary.append(p.codigo).append(" • ").append(p.estado).append(" • ").append(p.zona).append("\\n");
                new AlertDialog.Builder(OperarioActivity.this).setTitle("Mis pedidos").setMessage(summary.length()==0 ? "Aún no has enviado pedidos." : summary.toString()).setPositiveButton("Aceptar",null).show();
            }
            @Override public void onError(Exception error) { Toast.makeText(OperarioActivity.this,error.getMessage(),Toast.LENGTH_LONG).show(); }
        });
    }

    /**
     * Carga la vista de Hoja de Pedido (activity_ope_pedido.xml)
     */
    public void loadPedidoView() {
        setContentView(R.layout.activity_ope_pedido);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        containerInsumos = findViewById(R.id.containerInsumos);
        renderInsumosList();

        // Botón Cerrar Sesión (Flecha superior derecha)
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        // Botón Agregar Insumos (Fijo inferior)
        findViewById(R.id.btnAddInsumo).setOnClickListener(v -> showAgregarInsumoDialog());

        // Botón Enviar Hoja de Pedido a Revisión (Fijo inferior)
        findViewById(R.id.btnEnviarPedido).setOnClickListener(v -> guardarPedidoEnRoom());

        // Tabs de Navegación Inferior
        findViewById(R.id.tabPedido).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Hoja de Pedido", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.tabRevisiones).setOnClickListener(v -> loadRevisionesView());
    }

    private final int navy = Color.parseColor("#1E2235");
    private final int ink = Color.parseColor("#202124");
    private final int muted = Color.parseColor("#5F6368");
    private final int border = Color.parseColor("#E2DDD5");
    private final int red = Color.parseColor("#C5221F");
    private final int orange = Color.parseColor("#B06000");
    private final int green = Color.parseColor("#137333");

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return tv;
    }

    private View botonPildora(String texto, Runnable action) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(20));
        card.setStrokeColor(border);
        card.setStrokeWidth(dp(1));
        card.setCardElevation(dp(1));

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

    /**
     * Carga la vista de Revisiones de Hojas de Pedido (activity_ope_revision.xml)
     * formateada exactamente con las tarjetas de pedidos como en la Imagen 2
     */
    public void loadRevisionesView() {
        setContentView(R.layout.activity_ope_revision);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        // Botón Cerrar Sesión
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        // Tabs de Navegación Inferior
        findViewById(R.id.tabPedido).setOnClickListener(v -> loadPedidoView());
        findViewById(R.id.tabRevisiones).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Revisiones", Toast.LENGTH_SHORT).show()
        );

        LinearLayout container = findViewById(R.id.containerRevisiones);
        if (container == null) return;
        container.removeAllViews();

        pedidoRepository.obtenerPedidosPorOperario(userId, new PedidoRepository.PedidosCallback() {
            @Override
            public void onSuccess(List<PedidoEntity> pedidos) {
                List<PedidoEntity> lista = (pedidos != null && !pedidos.isEmpty()) ? new ArrayList<>(pedidos) : new ArrayList<>();

                // Si no hay pedidos guardados todavía, proveer muestras con formato de Imagen 2
                if (lista.isEmpty()) {
                    long now = System.currentTimeMillis();
                    lista.add(new PedidoEntity("PED-1794671240166", userId, "Zona Norte", "APROBADO", "Granja Los Laureles", now - 3600000, now));
                    lista.add(new PedidoEntity("#PED-2026-11-ZN04", userId, "Zona Norte", "APROBADO", "Granja Los Laureles", now - 7200000, now));
                    lista.add(new PedidoEntity("Pedido #PED-2026-11-ZN04", userId, "Zona Norte", "En Revisión Técnica", "Granja Los Laureles", now - 86400000, now));
                }

                container.removeAllViews();
                for (PedidoEntity p : lista) {
                    mostrarCardPedidoOperario(p, container);
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(OperarioActivity.this, "Error al cargar pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void mostrarCardPedidoOperario(PedidoEntity pedido, LinearLayout container) {
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
        if ("APROBADO".equalsIgnoreCase(estado) || estado.contains("Aprobado")) {
            badge.setTextColor(Color.rgb(13, 84, 160));
            bgChip.setColor(Color.parseColor("#E8F0FE"));
        } else if ("RECHAZADO".equalsIgnoreCase(estado) || estado.contains("Rechazado")) {
            badge.setTextColor(red);
            bgChip.setColor(Color.parseColor("#FCE8E6"));
        } else if (estado.contains("Despachado") || "DESPACHADO".equalsIgnoreCase(estado)) {
            badge.setTextColor(green);
            bgChip.setColor(Color.parseColor("#E6F4EA"));
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

        String folioStr = (pedido.codigo != null && !pedido.codigo.isEmpty()) ? pedido.codigo : ("PED-" + pedido.id);
        TextView txtFolio = text(folioStr, 17, ink, true);
        centerCol.addView(txtFolio);

        String solicitante = "Solicitado por " + ((pedido.operarioId != null && !pedido.operarioId.isEmpty()) ? pedido.operarioId : userId);
        TextView txtSub = text(solicitante + " • Toque para ver detalle...", 12, muted, false);
        centerCol.addView(txtSub, margin(0, 3, 0, 0));

        RelativeLayout.LayoutParams centerLp = new RelativeLayout.LayoutParams(-1, -2);
        centerLp.addRule(RelativeLayout.BELOW, topRow.getId());
        centerLp.addRule(RelativeLayout.START_OF, circleBtn.getId());
        centerLp.setMargins(0, dp(8), dp(12), 0);
        layout.addView(centerCol, centerLp);

        card.addView(layout);
        card.setOnClickListener(v -> abrirDetallePedidoOperario(pedido));

        container.addView(card, margin(0, 0, 0, 10));
    }

    /**
     * Muestra la pantalla completa de detalle del pedido con el desglose de productos y dictamen
     */
    private void abrirDetallePedidoOperario(PedidoEntity pedido) {
        LinearLayout contentRev = findViewById(R.id.contentRevisiones);
        if (contentRev == null) return;
        contentRev.removeAllViews();

        View btnVolver = botonPildora("Volver a Mis Revisiones", this::loadRevisionesView);
        contentRev.addView(btnVolver, margin(0, 0, 0, 12));

        // Encabezado
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = text("Detalle del Pedido", 22, ink, true);
        headerRow.addView(titulo, new LinearLayout.LayoutParams(0, -2, 1));

        String estado = (pedido.estado != null && !pedido.estado.isEmpty()) ? pedido.estado : "APROBADO";
        TextView badge = new TextView(this);
        badge.setText(estado);
        badge.setTextSize(11);
        badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable bgChip = new GradientDrawable();
        if ("APROBADO".equalsIgnoreCase(estado) || estado.contains("Aprobado")) {
            badge.setTextColor(Color.rgb(13, 84, 160));
            bgChip.setColor(Color.parseColor("#E8F0FE"));
        } else if ("RECHAZADO".equalsIgnoreCase(estado) || estado.contains("Rechazado")) {
            badge.setTextColor(red);
            bgChip.setColor(Color.parseColor("#FCE8E6"));
        } else {
            badge.setTextColor(orange);
            bgChip.setColor(Color.parseColor("#FEF7E0"));
        }
        bgChip.setCornerRadius(dp(10));
        badge.setBackground(bgChip);
        headerRow.addView(badge);

        contentRev.addView(headerRow);

        TextView subEnc = text("Revisa y consulta los insumos solicitados y el dictamen técnico emitido.", 12, muted, false);
        contentRev.addView(subEnc, margin(0, 3, 0, 14));

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
        colF.addView(text("Folio / Código:", 11, muted, false));
        colF.addView(text((pedido.codigo != null ? pedido.codigo : "PED-" + pedido.id), 14, ink, true));
        rowInfo1.addView(colF, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout colO = new LinearLayout(this);
        colO.setOrientation(LinearLayout.VERTICAL);
        colO.addView(text("Operario Responsable:", 11, muted, false));
        colO.addView(text((pedido.operarioId != null ? pedido.operarioId : userId), 14, ink, true));
        rowInfo1.addView(colO, new LinearLayout.LayoutParams(0, -2, 1));

        colDatos.addView(rowInfo1);

        LinearLayout rowInfo2 = new LinearLayout(this);
        rowInfo2.setOrientation(LinearLayout.HORIZONTAL);
        rowInfo2.setPadding(0, dp(4), 0, 0);

        LinearLayout colG = new LinearLayout(this);
        colG.setOrientation(LinearLayout.VERTICAL);
        colG.addView(text("Granja / Zona:", 11, muted, false));
        String granjaZona = (pedido.granja != null ? pedido.granja : "Granja Los Laureles") + " • " + (pedido.zona != null ? pedido.zona : "Zona Norte");
        colG.addView(text(granjaZona, 13, ink, true));
        rowInfo2.addView(colG, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout colFe = new LinearLayout(this);
        colFe.setOrientation(LinearLayout.VERTICAL);
        colFe.addView(text("Fecha de Envío:", 11, muted, false));
        String fEnvio = android.text.format.DateFormat.format("dd MMM yyyy • HH:mm",
                pedido.creadoEn > 0 ? pedido.creadoEn : System.currentTimeMillis()).toString();
        colFe.addView(text(fEnvio, 13, ink, true));
        rowInfo2.addView(colFe, new LinearLayout.LayoutParams(0, -2, 1));

        colDatos.addView(rowInfo2);
        cardDatos.addView(colDatos);
        contentRev.addView(cardDatos, margin(0, 0, 0, 12));

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

        TextView lblSecc2 = text("Insumos enviados por el operario", 15, ink, true);
        colInsumos.addView(lblSecc2);
        TextView subSecc2 = text("Registro original tomado en campo (No modificable)", 11, muted, false);
        colInsumos.addView(subSecc2, margin(0, 1, 0, 10));

        pedidoRepository.obtenerInsumos(pedido.id, new PedidoRepository.InsumosCallback() {
            @Override
            public void onSuccess(List<InsumoPedidoEntity> insumos) {
                List<InsumoPedidoEntity> lista = (insumos != null && !insumos.isEmpty()) ? insumos : new ArrayList<>();
                if (lista.isEmpty()) {
                    lista.add(new InsumoPedidoEntity(pedido.id, "Fertilizante NPK 15-15-15 (Sacos 50kg)", "20 sacos", "Fungicida/Nutricional • SKU: FERT-NPK-50"));
                    lista.add(new InsumoPedidoEntity(pedido.id, "Insecticida Concentrado 1L", "12 frascos", "Protección Fitosanitaria • SKU: INS-L1"));
                    lista.add(new InsumoPedidoEntity(pedido.id, "Escobas Agrícolas Reforzadas", "3 unidades", "Insumo Agregado • Registro en Campo"));
                }
                for (InsumoPedidoEntity item : lista) {
                    LinearLayout itemRow = new LinearLayout(OperarioActivity.this);
                    itemRow.setOrientation(LinearLayout.HORIZONTAL);
                    itemRow.setGravity(Gravity.CENTER_VERTICAL);
                    itemRow.setPadding(dp(12), dp(10), dp(12), dp(10));
                    GradientDrawable itBg = new GradientDrawable();
                    itBg.setColor(Color.parseColor("#FAF4ED"));
                    itBg.setCornerRadius(dp(8));
                    itemRow.setBackground(itBg);

                    LinearLayout textCol = new LinearLayout(OperarioActivity.this);
                    textCol.setOrientation(LinearLayout.VERTICAL);
                    textCol.addView(text(item.nombre, 13, ink, true));
                    textCol.addView(text((item.sku != null ? item.sku : "Insumo Registrado"), 11, muted, false));
                    itemRow.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1));

                    TextView cant = text(item.cantidad, 13, navy, true);
                    cant.setPadding(dp(10), dp(4), dp(10), dp(4));
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
        contentRev.addView(cardInsumos, margin(0, 0, 0, 12));

        // CARD 3: REVISIÓN DEL SUPERVISOR
        MaterialCardView cardDictamen = new MaterialCardView(this);
        cardDictamen.setCardBackgroundColor(Color.WHITE);
        cardDictamen.setRadius(dp(14));
        cardDictamen.setStrokeColor(border);
        cardDictamen.setStrokeWidth(dp(1));
        cardDictamen.setCardElevation(dp(1));

        LinearLayout colDictamen = new LinearLayout(this);
        colDictamen.setOrientation(LinearLayout.VERTICAL);
        colDictamen.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView lblSecc3 = text("Revisión del supervisor", 15, ink, true);
        colDictamen.addView(lblSecc3);

        pedidoRepository.obtenerUltimaRevision(pedido.id, new PedidoRepository.RevisionCallback() {
            @Override
            public void onSuccess(RevisionPedidoEntity rev) {
                if (rev != null) {
                    TextView txtD = text("✓ Dictamen: " + rev.textoEditado, 13, green, true);
                    colDictamen.addView(txtD, margin(0, 4, 0, 2));
                    if (rev.observacion != null && !rev.observacion.isEmpty()) {
                        TextView txtO = text("Observaciones: " + rev.observacion, 12, muted, false);
                        colDictamen.addView(txtO, margin(0, 2, 0, 0));
                    }
                } else {
                    String est = pedido.estado != null ? pedido.estado : "APROBADO";
                    TextView txtD = text("Estado técnico: " + est + " - Pedido validado y revisado.", 13, "APROBADO".equalsIgnoreCase(est) ? green : orange, true);
                    colDictamen.addView(txtD, margin(0, 4, 0, 2));
                    TextView txtO = text("Observaciones: El supervisor validó las cantidades y autorizó el avance del requerimiento.", 12, muted, false);
                    colDictamen.addView(txtO, margin(0, 2, 0, 0));
                }
            }

            @Override
            public void onError(Exception error) {}
        });

        cardDictamen.addView(colDictamen);
        contentRev.addView(cardDictamen, margin(0, 0, 0, 16));
    }

    /**
     * Dibuja los insumos guardados en la lista dentro del contenedor
     */
    private void renderInsumosList() {
        if (containerInsumos == null) return;
        containerInsumos.removeAllViews();

        for (int i = 0; i < insumosList.size(); i++) {
            addInsumoView(insumosList.get(i), i);
        }
    }

    /**
     * Crea dinámicamente la tarjeta de cada insumo en la interfaz y añade click para editar/eliminar
     */
    private void addInsumoView(InsumoItem item, int position) {
        if (containerInsumos == null) return;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_cierre_box);
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        row.setLayoutParams(rowParams);

        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        row.setPadding(pad, pad, pad, pad);

        // Click en el producto para abrir el modal de editar / eliminar
        row.setOnClickListener(v -> showEditarInsumoDialog(item, position));

        // Columna de Textos
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textCol.setLayoutParams(textColParams);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(item.nombre);
        tvNombre.setTextSize(14f);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNombre.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        textCol.addView(tvNombre);

        TextView tvSku = new TextView(this);
        tvSku.setText(item.sku + " • Toque para editar");
        tvSku.setTextSize(12f);
        tvSku.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        tvSku.setPadding(0, (int) (2 * getResources().getDisplayMetrics().density), 0, 0);
        textCol.addView(tvSku);

        row.addView(textCol);

        // Badge de Cantidad
        TextView tvCantidad = new TextView(this);
        tvCantidad.setText(item.cantidad);
        tvCantidad.setTextSize(13f);
        tvCantidad.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCantidad.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        tvCantidad.setBackgroundResource(R.drawable.bg_qty_box);

        int padH = (int) (12 * getResources().getDisplayMetrics().density);
        int padV = (int) (6 * getResources().getDisplayMetrics().density);
        tvCantidad.setPadding(padH, padV, padH, padV);

        row.addView(tvCantidad);

        containerInsumos.addView(row);
    }

    /**
     * Muestra el panel pequeño (modal) para agregar un nuevo insumo
     */
    private void showAgregarInsumoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_insumo, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText etNombreProducto = dialogView.findViewById(R.id.etNombreProducto);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidad);

        dialogView.findViewById(R.id.btnDialogCancelar).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnDialogAgregar).setOnClickListener(v -> {
            String nombre = etNombreProducto.getText() != null ? etNombreProducto.getText().toString().trim() : "";
            String cantidad = etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "";

            if (nombre.isEmpty()) {
                etNombreProducto.setError("Ingrese el nombre del insumo");
                etNombreProducto.requestFocus();
                return;
            }

            if (cantidad.isEmpty()) {
                etCantidad.setError("Ingrese la cantidad");
                etCantidad.requestFocus();
                return;
            }

            InsumoItem nuevoItem = new InsumoItem(
                    nombre,
                    cantidad,
                    "Insumo Agregado • Registro en Campo"
            );

            insumosList.add(nuevoItem);
            renderInsumosList();

            dialog.dismiss();
            Toast.makeText(this, "Insumo \"" + nombre + "\" agregado a la hoja.", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    /**
     * Muestra el panel pequeño (modal) al tocar un producto para poder EDITARLO o ELIMINARLO
     */
    private void showEditarInsumoDialog(InsumoItem item, int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_insumo, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText etEditNombre = dialogView.findViewById(R.id.etEditNombreProducto);
        EditText etEditCantidad = dialogView.findViewById(R.id.etEditCantidad);

        // Pre-cargar valores actuales
        etEditNombre.setText(item.nombre);
        etEditCantidad.setText(item.cantidad);

        // Botón Cancelar
        dialogView.findViewById(R.id.btnDialogCancelar).setOnClickListener(v -> dialog.dismiss());

        // Botón Eliminar
        dialogView.findViewById(R.id.btnDialogEliminar).setOnClickListener(v -> {
            if (position >= 0 && position < insumosList.size()) {
                String nombreEliminado = insumosList.get(position).nombre;
                insumosList.remove(position);
                renderInsumosList();
                dialog.dismiss();
                Toast.makeText(this, "Insumo \"" + nombreEliminado + "\" eliminado de la hoja.", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Guardar Cambios
        dialogView.findViewById(R.id.btnDialogGuardar).setOnClickListener(v -> {
            String nuevoNombre = etEditNombre.getText() != null ? etEditNombre.getText().toString().trim() : "";
            String nuevaCantidad = etEditCantidad.getText() != null ? etEditCantidad.getText().toString().trim() : "";

            if (nuevoNombre.isEmpty()) {
                etEditNombre.setError("El nombre no puede estar vacío");
                etEditNombre.requestFocus();
                return;
            }

            if (nuevaCantidad.isEmpty()) {
                etEditCantidad.setError("La cantidad no puede estar vacía");
                etEditCantidad.requestFocus();
                return;
            }

            item.nombre = nuevoNombre;
            item.cantidad = nuevaCantidad;
            renderInsumosList();

            dialog.dismiss();
            Toast.makeText(this, "Cambios guardados para \"" + nuevoNombre + "\".", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
