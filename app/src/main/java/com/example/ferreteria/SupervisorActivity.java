package com.example.ferreteria;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.Button;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.ferreteria.data.PedidoRepository;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.RevisionPedidoEntity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SupervisorActivity extends AppCompatActivity {

    private enum Zone {
        NORTE("Zona Norte (Valle Agrícola)", "8 Granjas asignadas • 3 listas", 6, 3, 1, "83% Cobertura", "Zona Norte"),
        VALLE("Zona Valle Central", "12 Granjas asignadas • 10 listas", 12, 10, 2, "92% Cobertura", "Zona Valle Central"),
        SUR("Zona Sur", "6 Granjas asignadas • 2 listas", 6, 2, 4, "33% Cobertura", "Zona Sur"),
        OCCIDENTE("Zona Occidente", "5 Granjas asignadas • 4 listas", 5, 4, 1, "80% Cobertura", "Zona Occidente");

        final String title;
        final String subtitle;
        final int enZona;
        final int listas;
        final int pendiente;
        final String cobertura;
        final String shortName;

        Zone(String title, String subtitle, int enZona, int listas, int pendiente, String cobertura, String shortName) {
            this.title = title;
            this.subtitle = subtitle;
            this.enZona = enZona;
            this.listas = listas;
            this.pendiente = pendiente;
            this.cobertura = cobertura;
            this.shortName = shortName;
        }
    }

    private Zone currentZone = Zone.NORTE;
    private PedidoRepository pedidoRepository;
    private com.example.ferreteria.data.AuthRepository auditRepository;
    private String userId;
    private LinearLayout dynamicContent;
    private final int panelNavy = Color.rgb(43, 48, 71);
    private final int panelInk = Color.rgb(35, 39, 57);
    private final int panelMuted = Color.rgb(105, 111, 130);
    private final int panelBg = Color.rgb(249, 246, 240);
    private final int panelStroke = Color.rgb(226, 221, 213);

    private TextView txtSelectedZoneTitle;
    private TextView txtSelectedZoneSub;
    private TextView txtCountEnZona;
    private TextView txtCountListas;
    private TextView txtCountPendiente;
    private TextView txtHeaderGranjas;
    private TextView txtCountVisibles;

    private TextView chipZoneNorte;
    private TextView chipZoneValle;
    private TextView chipZoneSur;
    private TextView chipZoneOccidente;

    private TextView txtResumenTitle;
    private TextView txtCoberturaPct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);
        pedidoRepository = new PedidoRepository(this);
        auditRepository = new com.example.ferreteria.data.AuthRepository(this);
        userId = getIntent().getStringExtra("user_id");
        loadZonasView();
    }

    public void loadZonasView() {
        setContentView(R.layout.activity_supervisor);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        initZonasViews();
        setupZoneSelection();
        setupZonasClickListeners();
    }

    public void loadHojaPedidoView() {
        loadHojaPedidoView("Finca San Isidro");
    }

    public void loadHojaPedidoView(String farmName) {
        setContentView(R.layout.activity_sup_hojapedido);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        TextView txtFarm = findViewById(R.id.txtDetalleGranjaNombre);
        if (txtFarm != null && farmName != null) {
            txtFarm.setText(farmName);
        }

        setupHojaPedidoClickListeners();
    }

    public void loadPedidosView() {
        construirPanel("Pedidos por zona", "Selecciona una zona para consultar sus hojas de pedido", true);
        cargarPedidosAgrupados();
    }

    public void loadHistorialView() {
        setContentView(R.layout.activity_sup_historial);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        setupHistorialClickListeners();
    }

    private void initZonasViews() {
        txtSelectedZoneTitle = findViewById(R.id.txtSelectedZoneTitle);
        txtSelectedZoneSub = findViewById(R.id.txtSelectedZoneSub);

        txtCountEnZona = findViewById(R.id.txtCountEnZona);
        txtCountListas = findViewById(R.id.txtCountListas);
        txtCountPendiente = findViewById(R.id.txtCountPendiente);

        txtHeaderGranjas = findViewById(R.id.txtHeaderGranjas);
        txtCountVisibles = findViewById(R.id.txtCountVisibles);

        chipZoneNorte = findViewById(R.id.chipZoneNorte);
        chipZoneValle = findViewById(R.id.chipZoneValle);
        chipZoneSur = findViewById(R.id.chipZoneSur);
        chipZoneOccidente = findViewById(R.id.chipZoneOccidente);

        txtResumenTitle = findViewById(R.id.txtResumenTitle);
        txtCoberturaPct = findViewById(R.id.txtCoberturaPct);
    }

    private void setupZoneSelection() {
        if (chipZoneNorte == null) return;

        chipZoneNorte.setOnClickListener(v -> selectZone(Zone.NORTE));
        chipZoneValle.setOnClickListener(v -> selectZone(Zone.VALLE));
        chipZoneSur.setOnClickListener(v -> selectZone(Zone.SUR));
        chipZoneOccidente.setOnClickListener(v -> selectZone(Zone.OCCIDENTE));

        findViewById(R.id.btnZoneDropdown).setOnClickListener(v -> {
            Zone[] zones = Zone.values();
            int nextOrdinal = (currentZone.ordinal() + 1) % zones.length;
            selectZone(zones[nextOrdinal]);
        });
    }

    private void selectZone(Zone zone) {
        currentZone = zone;

        if (txtSelectedZoneTitle != null) txtSelectedZoneTitle.setText(zone.title);
        if (txtSelectedZoneSub != null) txtSelectedZoneSub.setText(zone.subtitle);

        if (txtCountEnZona != null) txtCountEnZona.setText(String.valueOf(zone.enZona));
        if (txtCountListas != null) txtCountListas.setText(String.valueOf(zone.listas));
        if (txtCountPendiente != null) txtCountPendiente.setText(String.valueOf(zone.pendiente));

        if (txtHeaderGranjas != null) txtHeaderGranjas.setText(getString(R.string.granjas_registradas_fmt, zone.shortName));
        if (txtCountVisibles != null) txtCountVisibles.setText(getString(R.string.visibles_fmt, zone.enZona));

        if (txtResumenTitle != null) txtResumenTitle.setText(getString(R.string.resumen_operativo_fmt, zone.shortName));
        if (txtCoberturaPct != null) txtCoberturaPct.setText(zone.cobertura);

        updateChipStyle(chipZoneNorte, zone == Zone.NORTE, "• Zona Norte (Activo)", "Zona Norte");
        updateChipStyle(chipZoneValle, zone == Zone.VALLE, "• Zona Valle (Activo)", "Zona Valle Central");
        updateChipStyle(chipZoneSur, zone == Zone.SUR, "• Zona Sur (Activo)", "Zona Sur");
        updateChipStyle(chipZoneOccidente, zone == Zone.OCCIDENTE, "• Zona Occ. (Activo)", "Zona Occidente");

        Toast.makeText(this, "Filtro cambiado a: " + zone.title, Toast.LENGTH_SHORT).show();
    }

    private void updateChipStyle(TextView chip, boolean isActive, String activeText, String inactiveText) {
        if (chip == null) return;
        if (isActive) {
            chip.setText(activeText);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_primary));
        } else {
            chip.setText(inactiveText);
            chip.setTextColor(ContextCompat.getColor(this, R.color.krono_chip_text));
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip));
        }
    }

    private void setupZonasClickListeners() {
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        View cardFarm1 = findViewById(R.id.cardFarm1);
        if (cardFarm1 != null) {
            cardFarm1.setOnClickListener(v -> loadHojaPedidoView("Granja Los Laureles"));
        }

        View cardFarm2 = findViewById(R.id.cardFarm2);
        if (cardFarm2 != null) {
            cardFarm2.setOnClickListener(v -> loadHojaPedidoView("Finca San Isidro"));
        }

        View cardFarm3 = findViewById(R.id.cardFarm3);
        if (cardFarm3 != null) {
            cardFarm3.setOnClickListener(v -> loadHojaPedidoView("Granja Santa Elena"));
        }

        findViewById(R.id.tabZonas).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Zonas y Granjas Operativas", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.tabPedidos).setOnClickListener(v -> loadPedidosView());
        findViewById(R.id.tabHistorial).setOnClickListener(v -> loadHistorialView());
    }

    private void setupHojaPedidoClickListeners() {
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        View btnVolver = findViewById(R.id.btnVolverGranjas);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> loadZonasView());
        }

        View btnAprobar = findViewById(R.id.btnAprobarHoja);
        if (btnAprobar != null) btnAprobar.setOnClickListener(v -> mostrarPedidosGuardados());

        setupCardToggle(R.id.cardRevision1, R.id.detailsRevision1, R.id.arrowRevision1);
        setupCardToggle(R.id.cardRevision2, R.id.detailsRevision2, R.id.arrowRevision2);
        setupCardToggle(R.id.cardRevision3, R.id.detailsRevision3, R.id.arrowRevision3);

        findViewById(R.id.tabZonas).setOnClickListener(v -> loadZonasView());
        findViewById(R.id.tabPedidos).setOnClickListener(v -> loadPedidosView());
        findViewById(R.id.tabHistorial).setOnClickListener(v -> loadHistorialView());
    }

    private void setupCardToggle(int cardId, int detailsId, int arrowId) {
        View card = findViewById(cardId);
        View details = findViewById(detailsId);
        android.widget.ImageView arrow = findViewById(arrowId);
        if (card != null && details != null && arrow != null) {
            card.setOnClickListener(v -> {
                boolean isVisible = details.getVisibility() == View.VISIBLE;
                details.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                arrow.animate().rotation(isVisible ? 0f : 180f).setDuration(200).start();
            });
        }
    }

    private void cargarPedidosAgrupados() {
        pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
            @Override public void onSuccess(List<PedidoEntity> pedidos) {
                dynamicContent.removeAllViews();
                if (pedidos.isEmpty()) { agregarVacio("Todavía no hay pedidos enviados."); return; }
                Map<String, List<PedidoEntity>> grupos = new LinkedHashMap<>();
                for (PedidoEntity pedido : pedidos) {
                    String zona = pedido.zona == null || pedido.zona.trim().isEmpty() ? "Sin zona asignada" : pedido.zona;
                    if (!grupos.containsKey(zona)) grupos.put(zona, new ArrayList<>());
                    grupos.get(zona).add(pedido);
                }
                for (Map.Entry<String, List<PedidoEntity>> entry : grupos.entrySet()) {
                    agregarGrupoZona(entry.getKey(), entry.getValue());
                }
            }
            @Override public void onError(Exception error) { agregarVacio("No se pudieron cargar los pedidos: " + error.getMessage()); }
        });
    }

    private void agregarGrupoZona(String zona, List<PedidoEntity> pedidos) {
        LinearLayout card = crearTarjeta();
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(14), dp(14), dp(14));
        header.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL);
        TextView name = texto(zona, 16, panelInk, true);
        TextView count = texto(pedidos.size() + (pedidos.size()==1 ? " pedido" : " pedidos"), 12, panelMuted, false);
        labels.addView(name); labels.addView(count);
        header.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = texto("⌄", 24, panelNavy, true); header.addView(arrow);
        card.addView(header);
        LinearLayout orders = new LinearLayout(this); orders.setOrientation(LinearLayout.VERTICAL);
        orders.setPadding(dp(12), 0, dp(12), dp(12)); orders.setVisibility(View.GONE);
        for (PedidoEntity pedido : pedidos) {
            LinearLayout order = crearTarjeta();
            order.setPadding(dp(12), dp(10), dp(12), dp(10));
            TextView code = texto(pedido.codigo == null ? "Pedido #" + pedido.id : pedido.codigo, 14, panelInk, true);
            TextView detail = texto("Operario: " + pedido.operarioId + "   •   " + pedido.estado, 12, panelMuted, false);
            order.addView(code); order.addView(detail);
            order.setOnClickListener(v -> abrirDetallePedido(pedido));
            orders.addView(order, params(-1, -2, 0, 0, 0, 8));
        }
        card.addView(orders);
        header.setOnClickListener(v -> {
            boolean visible = orders.getVisibility() == View.VISIBLE;
            orders.setVisibility(visible ? View.GONE : View.VISIBLE);
            arrow.setText(visible ? "⌄" : "⌃");
        });
        dynamicContent.addView(card, params(-1, -2, 0, 0, 0, 12));
    }

    private void abrirDetallePedido(PedidoEntity pedido) {
        construirPanel("Detalle del pedido", pedido.codigo == null ? "Pedido #" + pedido.id : pedido.codigo, false);
        agregarBoton("← Regresar a hojas de pedidos", () -> loadPedidosView(), false);
        agregarTextoSeccion("Datos del envío original");
        agregarInfo("Operario", pedido.operarioId);
        agregarInfo("Zona", pedido.zona);
        agregarInfo("Estado", pedido.estado);
        agregarInfo("Enviado", android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", pedido.creadoEn).toString());
        agregarTextoSeccion("Insumos enviados por el operario");
        LinearLayout originalBox = crearTarjeta();
        originalBox.setPadding(dp(14), dp(12), dp(14), dp(12));
        dynamicContent.addView(originalBox, params(-1, -2, 0, 0, 0, 10));
        pedidoRepository.obtenerInsumos(pedido.id, new PedidoRepository.InsumosCallback() {
            @Override public void onSuccess(List<InsumoPedidoEntity> items) {
                originalBox.removeAllViews();
                if (items.isEmpty()) originalBox.addView(texto("Este pedido no contiene insumos.", 13, panelMuted, false));
                StringBuilder draft = new StringBuilder();
                for (InsumoPedidoEntity item : items) {
                    originalBox.addView(texto(item.nombre, 14, panelInk, true));
                    originalBox.addView(texto("Cantidad: " + item.cantidad + (item.sku == null || item.sku.isEmpty() ? "" : "   •   SKU: " + item.sku), 12, panelMuted, false), params(-1, -2, 0, 3, 0, 9));
                    draft.append(item.nombre).append(" | Cantidad: ").append(item.cantidad);
                    if (item.sku != null && !item.sku.isEmpty()) draft.append(" | SKU: ").append(item.sku);
                    draft.append("\\n");
                }
                cargarEditorRevision(pedido, draft.toString());
            }
            @Override public void onError(Exception error) { agregarAviso("No se pudieron consultar los insumos: " + error.getMessage()); }
        });
    }

    private void cargarEditorRevision(PedidoEntity pedido, String textoOriginal) {
        agregarTextoSeccion("Revisión del supervisor");
        agregarTextoAyuda("El texto original de arriba no se modifica. Aquí puedes ajustar las cantidades, quitar o agregar insumos.");
        EditText edited = campoMultilinea("Escribe el pedido revisado...", textoOriginal);
        dynamicContent.addView(edited, params(-1, -2, 0, 0, 0, 10));
        EditText note = campoMultilinea("Observaciones del supervisor (opcional)", "");
        dynamicContent.addView(note, params(-1, -2, 0, 0, 0, 10));
        LinearLayout previous = crearTarjeta(); previous.setPadding(dp(14),dp(12),dp(14),dp(12));
        dynamicContent.addView(previous, params(-1,-2,0,0,0,10));
        previous.addView(texto("Última revisión guardada",14,panelInk,true));
        pedidoRepository.obtenerUltimaRevision(pedido.id, new PedidoRepository.RevisionCallback() {
            @Override public void onSuccess(RevisionPedidoEntity revision) {
                if (revision == null) previous.addView(texto("Este pedido todavía no tiene modificaciones del supervisor.",12,panelMuted,false));
                else {
                    if (revision.textoEditado != null && !revision.textoEditado.trim().isEmpty()) edited.setText(revision.textoEditado);
                    if (revision.observacion != null) note.setText(revision.observacion);
                    previous.addView(texto(revision.textoEditado,13,panelInk,false),params(-1,-2,0,6,0,4));
                    if (revision.observacion != null && !revision.observacion.trim().isEmpty()) previous.addView(texto("Observación: "+revision.observacion,12,panelMuted,false));
                    previous.addView(texto("Guardado: "+android.text.format.DateFormat.format("dd/MM/yyyy HH:mm",revision.fecha),11,panelMuted,false),params(-1,-2,0,6,0,0));
                }
            }
            @Override public void onError(Exception error) { previous.addView(texto("No se pudo cargar la revisión anterior.",12,panelMuted,false)); }
        });
        agregarBoton("Guardar revisión del supervisor", () -> {
            String editedText = edited.getText().toString().trim();
            if (editedText.isEmpty()) { agregarAviso("El texto revisado no puede quedar vacío."); return; }
            RevisionPedidoEntity revision = new RevisionPedidoEntity(pedido.id, userId,
                    editedText, note.getText().toString().trim(), System.currentTimeMillis());
            pedidoRepository.guardarRevision(revision, new PedidoRepository.GuardarPedidoCallback() {
                @Override public void onSuccess(long id) {
                    auditRepository.auditar(userId, "REVISION_GUARDADA", "PEDIDO", pedido.codigo);
                    abrirDetallePedido(pedido);
                }
                @Override public void onError(Exception error) { agregarAviso("No se pudo guardar la revisión: "+error.getMessage()); }
            });
        }, true);
        agregarBoton("Aprobar pedido", () -> cambiarEstadoPedido(pedido, "APROBADO"), false);
        agregarBoton("Rechazar pedido", () -> cambiarEstadoPedido(pedido, "RECHAZADO"), false);
    }

    private void cambiarEstadoPedido(PedidoEntity pedido, String status) {
        pedidoRepository.actualizarEstado(pedido.id, status, new PedidoRepository.GuardarPedidoCallback() {
            @Override public void onSuccess(long id) {
                auditRepository.auditar(userId, status, "PEDIDO", pedido.codigo);
                Toast.makeText(SupervisorActivity.this, "Pedido actualizado: " + status, Toast.LENGTH_SHORT).show();
                loadPedidosView();
            }
            @Override public void onError(Exception error) { agregarAviso(error.getMessage()); }
        });
    }

    private void construirPanel(String title, String subtitle, boolean showNav) {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(panelBg);
        View status = new View(this); status.setBackgroundColor(panelNavy); root.addView(status, new LinearLayout.LayoutParams(-1, dp(26)));
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(18),dp(14),dp(18),dp(14)); top.setBackgroundColor(Color.WHITE);
        TextView brand = texto("KRONO  •  SUPERVISOR", 15, panelNavy, true); top.addView(brand);
        root.addView(top);
        TextView heading = texto(title, 23, panelInk, true); heading.setPadding(dp(18),dp(18),dp(18),0); root.addView(heading);
        TextView sub = texto(subtitle, 13, panelMuted, false); sub.setPadding(dp(18),dp(4),dp(18),dp(12)); root.addView(sub);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        dynamicContent = new LinearLayout(this); dynamicContent.setOrientation(LinearLayout.VERTICAL); dynamicContent.setPadding(dp(16),dp(4),dp(16),dp(20));
        scroll.addView(dynamicContent); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if (showNav) {
            LinearLayout nav = new LinearLayout(this); nav.setBackgroundColor(Color.WHITE); nav.setPadding(dp(8),dp(8),dp(8),dp(8));
            nav.addView(navButton("Zonas", () -> loadZonasView()), new LinearLayout.LayoutParams(0,dp(44),1));
            nav.addView(navButton("Pedidos", () -> loadPedidosView()), new LinearLayout.LayoutParams(0,dp(44),1));
            nav.addView(navButton("Historial", () -> loadHistorialView()), new LinearLayout.LayoutParams(0,dp(44),1));
            root.addView(nav);
        }
        setContentView(root);
    }

    private Button navButton(String label, Runnable action) {
        Button button = new Button(this); button.setText(label); button.setTextSize(12); button.setAllCaps(false);
        button.setOnClickListener(v -> action.run()); return button;
    }
    private LinearLayout crearTarjeta() {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setBackground(panelDrawable(Color.WHITE, panelStroke, 14)); card.setClipToOutline(true);
        return card;
    }
    private GradientDrawable panelDrawable(int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(fill); d.setCornerRadius(dp(radius)); d.setStroke(dp(1),stroke); return d;
    }
    private TextView texto(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value == null ? "" : value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    private void agregarTextoSeccion(String title) {
        TextView t = texto(title,16,panelInk,true); dynamicContent.addView(t,params(-1,-2,0,14,0,8));
    }
    private void agregarTextoAyuda(String value) {
        dynamicContent.addView(texto(value,12,panelMuted,false),params(-1,-2,0,0,0,8));
    }
    private void agregarInfo(String label, String value) {
        LinearLayout card = crearTarjeta(); card.setPadding(dp(12),dp(9),dp(12),dp(9));
        card.addView(texto(label,11,panelMuted,true)); card.addView(texto(value == null ? "—" : value,14,panelInk,false));
        dynamicContent.addView(card,params(-1,-2,0,0,0,7));
    }
    private EditText campoMultilinea(String hint, String value) {
        EditText edit = new EditText(this); edit.setText(value); edit.setHint(hint); edit.setTextSize(14); edit.setTextColor(panelInk); edit.setHintTextColor(panelMuted);
        edit.setGravity(Gravity.TOP|Gravity.START); edit.setMinLines(4); edit.setPadding(dp(13),dp(12),dp(13),dp(12));
        edit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edit.setBackground(panelDrawable(Color.WHITE,panelStroke,12)); return edit;
    }
    private void agregarBoton(String label, Runnable action, boolean primary) {
        Button button = new Button(this); button.setText(label); button.setAllCaps(false); button.setTextSize(14);
        button.setTextColor(primary ? Color.WHITE : panelNavy);
        button.setBackground(panelDrawable(primary ? panelNavy : Color.WHITE, primary ? panelNavy : panelStroke, 12));
        button.setOnClickListener(v -> action.run());
        dynamicContent.addView(button,params(-1,dp(50),0,8,0,8));
    }
    private void agregarAviso(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }
    private void agregarVacio(String message) {
        TextView empty = texto(message,14,panelMuted,false); empty.setGravity(Gravity.CENTER); empty.setPadding(dp(20),dp(30),dp(20),dp(30));
        dynamicContent.addView(empty);
    }
    private LinearLayout.LayoutParams params(int width,int height,int left,int top,int right,int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width,height); p.setMargins(dp(left),dp(top),dp(right),dp(bottom)); return p;
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private void setupPedidosClickListeners() {
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        // Ver Hoja buttons
        int[] verHojaIds = new int[]{R.id.btnVerHoja1, R.id.btnVerHoja2, R.id.btnVerHoja3, R.id.btnVerHoja4};
        for (int id : verHojaIds) {
            if (findViewById(id) != null) {
                findViewById(id).setOnClickListener(v -> loadHojaPedidoView("Finca San Isidro"));
            }
        }

        // Iniciar Despacho buttons
        int[] iniciarDespachoIds = new int[]{R.id.btnIniciarDespacho1, R.id.btnIniciarDespacho2, R.id.btnIniciarDespacho3, R.id.btnIniciarDespacho4};
        for (int id : iniciarDespachoIds) {
            if (findViewById(id) != null) {
                findViewById(id).setOnClickListener(v ->
                        Toast.makeText(this, "Iniciando proceso de despacho y remisión de rampa...", Toast.LENGTH_SHORT).show()
                );
            }
        }

        // Consolidar
        if (findViewById(R.id.btnConsolidar) != null) {
            findViewById(R.id.btnConsolidar).setOnClickListener(v ->
                    Toast.makeText(this, "Consolidando 5 pedidos de bodega para salida en ruta...", Toast.LENGTH_SHORT).show()
            );
        }

        findViewById(R.id.tabZonas).setOnClickListener(v -> loadZonasView());

        findViewById(R.id.tabPedidos).setOnClickListener(v -> mostrarPedidosGuardados());

        findViewById(R.id.tabHistorial).setOnClickListener(v -> loadHistorialView());
    }

    private void setupHistorialClickListeners() {
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        int[] pdfIds = new int[]{R.id.btnPdf1, R.id.btnPdf2, R.id.btnPdf3, R.id.btnExportPdf};
        for (int id : pdfIds) {
            if (findViewById(id) != null) {
                findViewById(id).setOnClickListener(v ->
                        Toast.makeText(this, "Generando remisión en formato PDF...", Toast.LENGTH_SHORT).show()
                );
            }
        }

        if (findViewById(R.id.btnExportCsv) != null) {
            findViewById(R.id.btnExportCsv).setOnClickListener(v ->
                    Toast.makeText(this, "Generando reporte de historial de liquidaciones en CSV...", Toast.LENGTH_SHORT).show()
            );
        }

        findViewById(R.id.tabZonas).setOnClickListener(v -> loadZonasView());
        findViewById(R.id.tabPedidos).setOnClickListener(v -> loadPedidosView());

        findViewById(R.id.tabHistorial).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Ventas e Historial Consolidado", Toast.LENGTH_SHORT).show()
        );
    }
}