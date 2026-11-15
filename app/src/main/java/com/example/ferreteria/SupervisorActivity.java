package com.example.ferreteria;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.ferreteria.data.AuthRepository;
import com.example.ferreteria.data.PedidoRepository;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.RevisionPedidoEntity;
import com.example.ferreteria.data.entity.ZoneEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // Modelo en memoria para el dictamen por insumo
    public static class InsumoDictamen {
        public InsumoPedidoEntity entity;
        public String estado; // "APROBADO", "AJUSTADO", "RECHAZADO"
        public String cantidadAjustada;
        public String observacion;

        public InsumoDictamen(InsumoPedidoEntity entity) {
            this.entity = entity;
            this.estado = "APROBADO";
            this.cantidadAjustada = entity.cantidad;
            this.observacion = "";
        }
    }

    private Zone currentZone = Zone.NORTE;
    private String currentZoneName = "Zona Norte";
    private Zone currentHistorialZone = Zone.NORTE;
    private String currentHistorialZoneName = "Zona Norte";
    private PedidoRepository pedidoRepository;
    private AuthRepository auditRepository;
    private String userId;
    private String currentFarmName = "Finca San Isidro";
    private List<InsumoDictamen> dictamenList = new ArrayList<>();

    private TextView txtSupervisorCicloSub;
    private TextView txtSupervisorCorte;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);
        pedidoRepository = new PedidoRepository(this);
        auditRepository = new AuthRepository(this);
        userId = getIntent().getStringExtra("user_id");
        if (userId == null) userId = "S100";
        loadZonasView();
    }

    // ==========================================
    // 1. ZONAS Y GRANJAS (Pantalla Principal)
    // ==========================================
    public void loadZonasView() {
        setContentView(R.layout.activity_supervisor);
        StatusBarUtils.applyStatusBarNavyHeader(this);
        StatusBarUtils.setupIntelligentKeyboard(this, findViewById(R.id.scrollViewSupervisor));

        initZonasViews();
        setupZoneSelection();
        setupZonasClickListeners();
        selectZoneByName(currentZoneName);
    }

    private void initZonasViews() {
        txtSupervisorCicloSub = findViewById(R.id.txtSupervisorCicloSub);
        txtSupervisorCorte = findViewById(R.id.txtSupervisorCorte);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", new java.util.Locale("es", "ES"));
        String mesAnio = sdf.format(new java.util.Date());
        if (!mesAnio.isEmpty()) {
            mesAnio = mesAnio.substring(0, 1).toUpperCase() + mesAnio.substring(1);
        }
        if (txtSupervisorCicloSub != null) {
            txtSupervisorCicloSub.setText("Supervisión de pedidos consolidados para insumos y fertilizantes • " + mesAnio);
        }
        if (txtSupervisorCorte != null) {
            txtSupervisorCorte.setText("Corte: Ciclo de " + mesAnio);
        }

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
    }

    private void setupZoneSelection() {
        if (chipZoneNorte == null) return;

        chipZoneNorte.setOnClickListener(v -> selectZone(Zone.NORTE));
        chipZoneValle.setOnClickListener(v -> selectZone(Zone.VALLE));
        chipZoneSur.setOnClickListener(v -> selectZone(Zone.SUR));
        chipZoneOccidente.setOnClickListener(v -> selectZone(Zone.OCCIDENTE));

        View btnDropdown = findViewById(R.id.btnZoneDropdown);
        if (btnDropdown != null) {
            btnDropdown.setOnClickListener(v -> mostrarDialogoSeleccionarZona(false));
        }
    }

    private void selectZone(Zone zone) {
        selectZoneByName(zone.shortName);
    }

    private void selectZoneByName(String zoneName) {
        if (zoneName == null || zoneName.isEmpty()) zoneName = "Zona Norte";
        currentZoneName = zoneName;

        for (Zone z : Zone.values()) {
            if (z.shortName.equalsIgnoreCase(zoneName) || z.title.equalsIgnoreCase(zoneName)) {
                currentZone = z;
                break;
            }
        }

        if (txtSelectedZoneTitle != null) txtSelectedZoneTitle.setText(zoneName);

        updateChipStyle(chipZoneNorte, zoneName.equalsIgnoreCase("Zona Norte"), "• Zona Norte (Activo)", "Zona Norte");
        updateChipStyle(chipZoneValle, zoneName.equalsIgnoreCase("Zona Valle Central"), "• Zona Valle (Activo)", "Zona Valle Central");
        updateChipStyle(chipZoneSur, zoneName.equalsIgnoreCase("Zona Sur"), "• Zona Sur (Activo)", "Zona Sur");
        updateChipStyle(chipZoneOccidente, zoneName.equalsIgnoreCase("Zona Occidente"), "• Zona Occ. (Activo)", "Zona Occidente");

        // Cargar dinámicamente todos los chips de zonas desde SQLite
        refrescarChipsDinamicosZonas(false, zoneName);

        if (txtHeaderGranjas != null) txtHeaderGranjas.setText("Granjas Registradas en " + zoneName);

        final String finalZoneName = zoneName;
        // Consultar conteos reales desde SQLite (datos no quemados)
        auditRepository.listarGranjasPorZona(finalZoneName, new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override
            public void onSuccess(List<FarmEntity> granjas) {
                int totalGranjas = (granjas != null) ? granjas.size() : 0;
                pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
                    @Override
                    public void onSuccess(List<PedidoEntity> pedidos) {
                        int listas = 0;
                        int pendientes = 0;
                        if (pedidos != null) {
                            for (PedidoEntity p : pedidos) {
                                if (p.zona != null && p.zona.equalsIgnoreCase(finalZoneName)) {
                                    if ("APROBADO".equalsIgnoreCase(p.estado)) {
                                        listas++;
                                    } else {
                                        pendientes++;
                                    }
                                }
                            }
                        }

                        if (txtSelectedZoneSub != null) {
                            txtSelectedZoneSub.setText(totalGranjas + (totalGranjas == 1 ? " Granja asignada" : " Granjas asignadas") +
                                    " • " + listas + (listas == 1 ? " lista" : " listas"));
                        }
                        if (txtCountEnZona != null) txtCountEnZona.setText(String.valueOf(totalGranjas));
                        if (txtCountListas != null) txtCountListas.setText(String.valueOf(listas));
                        if (txtCountPendiente != null) txtCountPendiente.setText(String.valueOf(pendientes));
                        if (txtCountVisibles != null) txtCountVisibles.setText(totalGranjas + (totalGranjas == 1 ? " visible" : " visibles"));
                    }

                    @Override
                    public void onError(Exception error) {
                        if (txtCountEnZona != null) txtCountEnZona.setText(String.valueOf(totalGranjas));
                    }
                });
            }

            @Override
            public void onError(Exception error) {}
        });

        cargarGranjasZonasByName(finalZoneName);
    }

    private void cargarGranjasZonas(Zone zone) {
        cargarGranjasZonasByName(zone.shortName);
    }

    private void cargarGranjasZonasByName(String zoneName) {
        LinearLayout container = findViewById(R.id.containerGranjasZonas);
        if (container == null) return;
        container.removeAllViews();

        auditRepository.listarGranjasPorZona(zoneName, new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override
            public void onSuccess(List<FarmEntity> granjas) {
                container.removeAllViews();
                if (granjas == null || granjas.isEmpty()) {
                    MaterialCardView emptyCard = new MaterialCardView(SupervisorActivity.this);
                    emptyCard.setCardBackgroundColor(Color.WHITE);
                    emptyCard.setRadius(dp(14));
                    emptyCard.setStrokeColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_card_stroke));
                    emptyCard.setStrokeWidth(dp(1));
                    TextView txt = new TextView(SupervisorActivity.this);
                    txt.setText("No hay granjas registradas en " + zoneName + ".");
                    txt.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                    txt.setTextSize(13);
                    txt.setPadding(dp(16), dp(16), dp(16), dp(16));
                    emptyCard.addView(txt);
                    container.addView(emptyCard, params(-1, -2, 0, 10, 0, 0));
                    return;
                }

                for (FarmEntity farm : granjas) {
                    View farmCard = crearCardGranjaZonas(farm);
                    container.addView(farmCard, params(-1, -2, 0, 8, 0, 0));
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(SupervisorActivity.this, "Error al cargar granjas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refrescarChipsDinamicosZonas(boolean esHistorial, String activeZoneName) {
        LinearLayout container = findViewById(esHistorial ? R.id.containerZoneChipsHistorial : R.id.containerZoneChips);
        if (container == null) return;

        auditRepository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
            @Override
            public void onSuccess(List<ZoneEntity> zonas) {
                List<String> listZonas = new ArrayList<>();
                if (zonas != null && !zonas.isEmpty()) {
                    for (ZoneEntity z : zonas) {
                        if (z.nombre != null && !z.nombre.isEmpty() && !listZonas.contains(z.nombre)) {
                            listZonas.add(z.nombre);
                        }
                    }
                }
                if (listZonas.isEmpty()) {
                    for (Zone z : Zone.values()) listZonas.add(z.shortName);
                }

                container.removeAllViews();
                for (String zName : listZonas) {
                    boolean isActive = zName.equalsIgnoreCase(activeZoneName);
                    TextView chip = new TextView(SupervisorActivity.this);
                    chip.setText(isActive ? "• " + zName + " (Activo)" : zName);
                    chip.setTextSize(12);
                    chip.setGravity(Gravity.CENTER);
                    chip.setPadding(dp(12), 0, dp(12), 0);
                    chip.setBackgroundResource(isActive ? R.drawable.bg_button_primary : R.drawable.bg_chip);
                    chip.setTextColor(ContextCompat.getColor(SupervisorActivity.this, isActive ? R.color.white : R.color.krono_chip_text));
                    chip.setTypeface(Typeface.DEFAULT, isActive ? Typeface.BOLD : Typeface.NORMAL);
                    chip.setClickable(true);
                    chip.setFocusable(true);
                    chip.setOnClickListener(v -> {
                        if (esHistorial) {
                            actualizarVistaHistorialZonaByName(zName);
                        } else {
                            selectZoneByName(zName);
                        }
                    });

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            dp(36)
                    );
                    lp.setMargins(0, 0, dp(6), 0);
                    container.addView(chip, lp);
                }
            }

            @Override
            public void onError(Exception error) {}
        });
    }

    private View crearCardGranjaZonas(FarmEntity farm) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(16));
        card.setCardElevation(0);
        card.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        card.setStrokeWidth(dp(1));
        card.setClickable(true);
        card.setFocusable(true);

        RelativeLayout layout = new RelativeLayout(this);
        layout.setPadding(dp(14), dp(14), dp(14), dp(14));

        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setId(View.generateViewId());
        iconFrame.setBackgroundResource(R.drawable.bg_icon_container);
        ImageView iconTractor = new ImageView(this);
        iconTractor.setImageResource(R.drawable.ic_tractor);
        iconTractor.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(dp(22), dp(22));
        tLp.gravity = Gravity.CENTER;
        iconFrame.addView(iconTractor, tLp);

        RelativeLayout.LayoutParams fLp = new RelativeLayout.LayoutParams(dp(44), dp(44));
        fLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        fLp.addRule(RelativeLayout.CENTER_VERTICAL);
        layout.addView(iconFrame, fLp);

        TextView badge = new TextView(this);
        badge.setId(View.generateViewId());
        badge.setText("✓ Hoja Lista");
        badge.setTextSize(11);
        badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badge.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        badge.setBackgroundResource(R.drawable.bg_blue_chip);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        RelativeLayout.LayoutParams bLp = new RelativeLayout.LayoutParams(-2, -2);
        bLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        bLp.addRule(RelativeLayout.CENTER_VERTICAL);
        layout.addView(badge, bLp);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView txtName = new TextView(this);
        txtName.setText(farm.nombre);
        txtName.setTextSize(15);
        txtName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        txtName.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        nameRow.addView(txtName);

        ImageView check = new ImageView(this);
        check.setImageResource(R.drawable.ic_check_circle);
        check.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(dp(16), dp(16));
        cLp.setMargins(dp(4), 0, 0, 0);
        nameRow.addView(check, cLp);
        textCol.addView(nameRow);

        TextView txtEnv = new TextView(this);
        txtEnv.setText("Clave: " + (farm.codigoAcceso != null ? farm.codigoAcceso : "E100") + " • Toca para abrir pedidos");
        txtEnv.setTextSize(11);
        txtEnv.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        txtEnv.setPadding(0, dp(2), 0, 0);
        textCol.addView(txtEnv);

        RelativeLayout.LayoutParams tLpCol = new RelativeLayout.LayoutParams(-1, -2);
        tLpCol.addRule(RelativeLayout.END_OF, iconFrame.getId());
        tLpCol.addRule(RelativeLayout.START_OF, badge.getId());
        tLpCol.addRule(RelativeLayout.CENTER_VERTICAL);
        tLpCol.setMargins(dp(12), 0, dp(10), 0);
        layout.addView(textCol, tLpCol);

        card.addView(layout);
        card.setOnClickListener(v -> loadHojaPedidoView(farm.nombre));
        return card;
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
        View btnLogout = findViewById(R.id.btnBackLogin);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> finish());

        View tabZonas = findViewById(R.id.tabZonas);
        if (tabZonas != null) {
            tabZonas.setOnClickListener(v ->
                    Toast.makeText(this, "Sección actual: Zonas y Granjas Operativas", Toast.LENGTH_SHORT).show()
            );
        }

        View tabHistorial = findViewById(R.id.tabHistorial);
        if (tabHistorial != null) tabHistorial.setOnClickListener(v -> loadHistorialView());
    }

    // ==========================================
    // 2. HOJAS DE PEDIDO DE UNA GRANJA
    // ==========================================
    public void loadHojaPedidoView() {
        loadHojaPedidoView("Finca San Isidro");
    }

    public void loadHojaPedidoView(String farmName) {
        currentFarmName = farmName != null ? farmName : "Finca San Isidro";
        setContentView(R.layout.activity_sup_hojapedido);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        TextView txtFarm = findViewById(R.id.txtDetalleGranjaNombre);
        if (txtFarm != null) {
            txtFarm.setText(currentFarmName);
        }

        setupHojaPedidoClickListeners();
        cargarHojasDinamicasDeGranja(currentFarmName);
    }

    private void setupHojaPedidoClickListeners() {
        View btnLogout = findViewById(R.id.btnBackLogin);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> finish());

        View btnVolver = findViewById(R.id.btnVolverGranjas);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> loadZonasView());
        }

        View tabZonas = findViewById(R.id.tabZonas);
        if (tabZonas != null) tabZonas.setOnClickListener(v -> loadZonasView());

        View tabHistorial = findViewById(R.id.tabHistorial);
        if (tabHistorial != null) tabHistorial.setOnClickListener(v -> loadHistorialView());
    }

    private void cargarHojasDinamicasDeGranja(String farmName) {
        LinearLayout container = findViewById(R.id.containerHojasDinamicas);
        if (container == null) return;
        container.removeAllViews();

        pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
            @Override
            public void onSuccess(List<PedidoEntity> pedidos) {
                List<PedidoEntity> filtrados = new ArrayList<>();
                for (PedidoEntity p : pedidos) {
                    if ("BORRADOR".equalsIgnoreCase(p.estado)) continue;
                    if (p.granja != null && p.granja.trim().equalsIgnoreCase(farmName.trim())) {
                        filtrados.add(p);
                    }
                }

                container.removeAllViews();
                if (filtrados.isEmpty()) {
                    MaterialCardView emptyCard = new MaterialCardView(SupervisorActivity.this);
                    emptyCard.setCardBackgroundColor(Color.WHITE);
                    emptyCard.setRadius(dp(14));
                    emptyCard.setStrokeColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_card_stroke));
                    emptyCard.setStrokeWidth(dp(1));
                    TextView txt = new TextView(SupervisorActivity.this);
                    txt.setText("No hay hojas de pedido pendientes para " + farmName + ".");
                    txt.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                    txt.setTextSize(13);
                    txt.setPadding(dp(16), dp(16), dp(16), dp(16));
                    emptyCard.addView(txt);
                    container.addView(emptyCard, params(-1, -2, 0, 10, 0, 0));
                    return;
                }

                for (PedidoEntity p : filtrados) {
                    View card = crearCardPedidoParaGranja(p, farmName);
                    container.addView(card, params(-1, -2, 0, 10, 0, 0));
                }
            }

            @Override
            public void onError(Exception error) {}
        });
    }

    private View crearCardPedidoParaGranja(PedidoEntity pedido, String farmName) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(16));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        card.setStrokeWidth(dp(1));
        card.setCardElevation(0);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(14), dp(16), dp(14));

        // Header con badge y fecha
        RelativeLayout top = new RelativeLayout(this);

        TextView badge = new TextView(this);
        badge.setText(pedido.estado != null ? pedido.estado : "En Revisión");
        badge.setTextSize(11);
        badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));

        if ("APROBADO".equalsIgnoreCase(pedido.estado)) {
            badge.setTextColor(ContextCompat.getColor(this, R.color.krono_green_text));
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_green_chip));
        } else if ("RECHAZADO".equalsIgnoreCase(pedido.estado)) {
            badge.setTextColor(ContextCompat.getColor(this, R.color.krono_red_text));
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_red_chip));
        } else {
            badge.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_blue_chip));
        }
        RelativeLayout.LayoutParams badgeLp = new RelativeLayout.LayoutParams(-2, -2);
        badgeLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        top.addView(badge, badgeLp);

        TextView date = new TextView(this);
        date.setText(android.text.format.DateFormat.format("dd MMM yyyy • HH:mm", pedido.creadoEn));
        date.setTextSize(12);
        date.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        date.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
        RelativeLayout.LayoutParams dateLp = new RelativeLayout.LayoutParams(-2, -2);
        dateLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        dateLp.addRule(RelativeLayout.CENTER_VERTICAL);
        top.addView(date, dateLp);

        layout.addView(top);

        // Body: Título y flecha redonda
        LinearLayout bodyRow = new LinearLayout(this);
        bodyRow.setOrientation(LinearLayout.HORIZONTAL);
        bodyRow.setGravity(Gravity.CENTER_VERTICAL);
        bodyRow.setPadding(0, dp(10), 0, 0);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView code = new TextView(this);
        String loteStr = (pedido.lote != null && !pedido.lote.trim().isEmpty()) ? pedido.lote.trim() : "Lote 01";
        code.setText("Número de Lote: " + loteStr);
        code.setTextSize(16);
        code.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        code.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        textCol.addView(code);

        TextView sub = new TextView(this);
        sub.setText("Solicitado por " + (pedido.operarioId != null ? pedido.operarioId : "Operario") + " • Toque para editar y aprobar");
        sub.setTextSize(12);
        sub.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        textCol.addView(sub);

        bodyRow.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1));

        FrameLayout circleArrow = new FrameLayout(this);
        circleArrow.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip));
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        FrameLayout.LayoutParams arrLp = new FrameLayout.LayoutParams(dp(16), dp(16));
        arrLp.gravity = Gravity.CENTER;
        circleArrow.addView(arrow, arrLp);

        LinearLayout.LayoutParams circLp = new LinearLayout.LayoutParams(dp(34), dp(34));
        circLp.setMargins(dp(8), 0, 0, 0);
        bodyRow.addView(circleArrow, circLp);

        layout.addView(bodyRow);
        card.addView(layout);
        card.setOnClickListener(v -> abrirDetallePedido(pedido, farmName, null));
        return card;
    }

    private void abrirDetallePedidoSample(int sampleNumber) {
        if (sampleNumber == 1) {
            List<InsumoPedidoEntity> items = new ArrayList<>();
            items.add(new InsumoPedidoEntity(0, "Fertilizante NPK 15-15-15 (Sacos 50kg)", "20 sacos", "FERT-NPK-50"));
            items.add(new InsumoPedidoEntity(0, "Insecticida Concentrado 1L", "12 frascos", "INS-L1"));
            items.add(new InsumoPedidoEntity(0, "Baldes Plásticos 20L", "2 un.", "Insumo Agregado"));
            PedidoEntity sample1 = new PedidoEntity("#PED-2026-11-ZN04", "Carlos Méndez", currentZone.title, "En Revisión Técnica", System.currentTimeMillis() - 7200000L, System.currentTimeMillis());
            abrirDetallePedido(sample1, currentFarmName, items);
        } else if (sampleNumber == 2) {
            List<InsumoPedidoEntity> items = new ArrayList<>();
            items.add(new InsumoPedidoEntity(0, "Urea Agrícola 46% (Sacos 50kg)", "25 sacos", "UREA-46"));
            items.add(new InsumoPedidoEntity(0, "Kits de Aspersión Manual", "10 kits", "ASP-MAN"));
            PedidoEntity sample2 = new PedidoEntity("#PED-2026-10-ZN02", "Carlos Méndez", currentZone.title, "Aprobado por Supervisor", System.currentTimeMillis() - 86400000L * 14, System.currentTimeMillis());
            abrirDetallePedido(sample2, currentFarmName, items);
        } else {
            List<InsumoPedidoEntity> items = new ArrayList<>();
            items.add(new InsumoPedidoEntity(0, "Fertilizante NPK 15-15-15", "40 sacos", "FERT-NPK-40"));
            items.add(new InsumoPedidoEntity(0, "Repuestos de Bomba", "5 repuestos", "REP-BOMBA"));
            PedidoEntity sample3 = new PedidoEntity("#PED-2026-09-ZN01", "Roberto Díaz", currentZone.title, "Despachado y Entregado", System.currentTimeMillis() - 86400000L * 45, System.currentTimeMillis());
            abrirDetallePedido(sample3, currentFarmName, items);
        }
    }

    // ==========================================
    // 3. DETALLE Y EDICIÓN/APROBACIÓN DEL PEDIDO
    // ==========================================
    private void abrirDetallePedido(PedidoEntity pedido, String farmName, List<InsumoPedidoEntity> fallbackItems) {
        setContentView(R.layout.activity_sup_detalle_pedido);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        // Header & Back Buttons
        View btnLogout = findViewById(R.id.btnBackLogin);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> finish());

        View btnVolver = findViewById(R.id.btnVolverHoja);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> loadHojaPedidoView(farmName));
        }

        // Bottom Nav (Tab Zonas activo)
        View tabZonas = findViewById(R.id.tabZonas);
        if (tabZonas != null) tabZonas.setOnClickListener(v -> loadZonasView());

        View tabHistorial = findViewById(R.id.tabHistorial);
        if (tabHistorial != null) tabHistorial.setOnClickListener(v -> loadHistorialView());

        // Text views
        TextView txtTitulo = findViewById(R.id.txtDetalleTitulo);
        TextView txtBadge = findViewById(R.id.txtBadgeEstado);
        TextView txtCodigo = findViewById(R.id.txtInfoCodigo);
        TextView txtOperario = findViewById(R.id.txtInfoOperario);
        TextView txtGranjaZona = findViewById(R.id.txtInfoGranjaZona);
        TextView txtFecha = findViewById(R.id.txtInfoFecha);
        TextView txtUltimaRevision = findViewById(R.id.txtUltimaRevision);
        EditText etObs = findViewById(R.id.etObservaciones);

        String loteVal = (pedido.lote != null && !pedido.lote.isEmpty()) ? pedido.lote : "Lote 01";
        if (txtTitulo != null) txtTitulo.setText("Detalle del Pedido");
        if (txtCodigo != null) txtCodigo.setText(loteVal);
        if (txtOperario != null) txtOperario.setText(pedido.operarioId != null ? pedido.operarioId : "Operario de Campo");
        if (txtGranjaZona != null) txtGranjaZona.setText((farmName != null ? farmName : currentFarmName) + " • " + (pedido.zona != null ? pedido.zona : currentZone.shortName));
        if (txtFecha != null) txtFecha.setText(android.text.format.DateFormat.format("dd MMM yyyy • HH:mm", pedido.creadoEn));
        if (txtBadge != null) txtBadge.setText(pedido.estado != null ? pedido.estado : "En Revisión");

        LinearLayout containerOriginales = findViewById(R.id.containerInsumosOriginales);

        if (pedido.id > 0) {
            pedidoRepository.obtenerInsumos(pedido.id, new PedidoRepository.InsumosCallback() {
                @Override
                public void onSuccess(List<InsumoPedidoEntity> items) {
                    prepararDictamenes(items, containerOriginales, txtUltimaRevision);
                }

                @Override
                public void onError(Exception error) {
                    prepararDictamenes(fallbackItems != null ? fallbackItems : new ArrayList<>(), containerOriginales, txtUltimaRevision);
                }
            });

            pedidoRepository.obtenerUltimaRevision(pedido.id, new PedidoRepository.RevisionCallback() {
                @Override
                public void onSuccess(RevisionPedidoEntity revision) {
                    if (revision != null) {
                        if (revision.observacion != null && etObs != null) {
                            etObs.setText(revision.observacion);
                        }
                    }
                }

                @Override
                public void onError(Exception error) {}
            });
        } else {
            prepararDictamenes(fallbackItems != null ? fallbackItems : new ArrayList<>(), containerOriginales, txtUltimaRevision);
        }

        // Botón Guardar Revisión
        MaterialButton btnGuardar = findViewById(R.id.btnGuardarRevision);
        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                guardarRevisionActual(pedido, farmName, etObs != null ? etObs.getText().toString().trim() : "", fallbackItems);
            });
        }

        // Botón Aprobar Pedido (Pasa directamente a Pedidos)
        MaterialButton btnAprobar = findViewById(R.id.btnAprobarPedido);
        if (btnAprobar != null) {
            btnAprobar.setOnClickListener(v -> {
                aprobarPedidoYPasarAPedidos(pedido, farmName, etObs != null ? etObs.getText().toString().trim() : "", fallbackItems);
            });
        }

        // Botón Rechazar Pedido
        MaterialButton btnRechazar = findViewById(R.id.btnRechazarPedido);
        if (btnRechazar != null) {
            btnRechazar.setOnClickListener(v -> {
                rechazarPedido(pedido, farmName, fallbackItems);
            });
        }
    }

    private void prepararDictamenes(List<InsumoPedidoEntity> items, LinearLayout container, TextView txtResumen) {
        dictamenList.clear();
        for (InsumoPedidoEntity it : items) {
            dictamenList.add(new InsumoDictamen(it));
        }
        renderizarListaInsumosDictamen(container, txtResumen);
    }

    private void renderizarListaInsumosDictamen(LinearLayout container, TextView txtResumen) {
        if (container == null) return;
        container.removeAllViews();

        int aprobados = 0, ajustados = 0, rechazados = 0;

        if (dictamenList.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No hay insumos en este pedido.");
            empty.setTextSize(12);
            empty.setTextColor(ContextCompat.getColor(this, R.color.krono_text_muted));
            container.addView(empty);
            return;
        }

        for (InsumoDictamen d : dictamenList) {
            if ("APROBADO".equals(d.estado)) aprobados++;
            else if ("AJUSTADO".equals(d.estado)) ajustados++;
            else if ("RECHAZADO".equals(d.estado)) rechazados++;

            MaterialCardView card = new MaterialCardView(this);
            card.setCardBackgroundColor(Color.WHITE);
            card.setRadius(dp(12));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
            card.setStrokeWidth(dp(1));
            card.setCardElevation(0);
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dp(12), dp(10), dp(12), dp(10));

            // Fila superior: Nombre de producto y Cantidad solicitada
            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout nameCol = new LinearLayout(this);
            nameCol.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(this);
            name.setText(d.entity.nombre);
            name.setTextSize(13);
            name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            name.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
            nameCol.addView(name);

            if (d.entity.sku != null && !d.entity.sku.trim().isEmpty()) {
                TextView sku = new TextView(this);
                sku.setText("SKU: " + d.entity.sku);
                sku.setTextSize(11);
                sku.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
                nameCol.addView(sku);
            }

            topRow.addView(nameCol, new LinearLayout.LayoutParams(0, -2, 1));

            TextView qtyOriginal = new TextView(this);
            qtyOriginal.setText(d.entity.cantidad);
            qtyOriginal.setTextSize(12);
            qtyOriginal.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            qtyOriginal.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
            qtyOriginal.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip));
            qtyOriginal.setPadding(dp(8), dp(3), dp(8), dp(3));
            topRow.addView(qtyOriginal);

            layout.addView(topRow);

            // Caja de Dictamen debajo del producto
            LinearLayout dictamenBox = new LinearLayout(this);
            dictamenBox.setOrientation(LinearLayout.VERTICAL);
            dictamenBox.setPadding(dp(10), dp(8), dp(10), dp(8));
            LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(-1, -2);
            boxLp.setMargins(0, dp(8), 0, 0);

            TextView txtDictamenTitulo = new TextView(this);
            txtDictamenTitulo.setTextSize(12);
            txtDictamenTitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            TextView txtDictamenDetalle = new TextView(this);
            txtDictamenDetalle.setTextSize(11);

            if ("APROBADO".equals(d.estado)) {
                dictamenBox.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_green_chip));
                txtDictamenTitulo.setText("✓ DICTAMEN: APROBADO");
                txtDictamenTitulo.setTextColor(ContextCompat.getColor(this, R.color.krono_green_text));
                txtDictamenDetalle.setText("Cantidad autorizada: " + d.cantidadAjustada);
                txtDictamenDetalle.setTextColor(ContextCompat.getColor(this, R.color.krono_green_text));
            } else if ("AJUSTADO".equals(d.estado)) {
                dictamenBox.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_blue_chip));
                txtDictamenTitulo.setText("✏️ DICTAMEN: AJUSTADO");
                txtDictamenTitulo.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
                txtDictamenDetalle.setText("Ajustado a " + d.cantidadAjustada + " (Original: " + d.entity.cantidad + ")");
                txtDictamenDetalle.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
            } else {
                dictamenBox.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_red_chip));
                txtDictamenTitulo.setText("✗ DICTAMEN: RECHAZADO");
                txtDictamenTitulo.setTextColor(ContextCompat.getColor(this, R.color.krono_red_text));
                txtDictamenDetalle.setText("Insumo no autorizado para despacho");
                txtDictamenDetalle.setTextColor(ContextCompat.getColor(this, R.color.krono_red_text));
            }

            dictamenBox.addView(txtDictamenTitulo);
            dictamenBox.addView(txtDictamenDetalle);

            if (d.observacion != null && !d.observacion.trim().isEmpty()) {
                TextView txtObs = new TextView(this);
                txtObs.setText("Nota: " + d.observacion);
                txtObs.setTextSize(11);
                txtObs.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
                txtObs.setPadding(0, dp(2), 0, 0);
                dictamenBox.addView(txtObs);
            }

            layout.addView(dictamenBox, boxLp);

            // Indicador de toque
            TextView hint = new TextView(this);
            hint.setText("✎ Toca para cambiar dictamen o cantidad");
            hint.setTextSize(10);
            hint.setTextColor(ContextCompat.getColor(this, R.color.krono_text_muted));
            hint.setPadding(0, dp(4), 0, 0);
            layout.addView(hint);

            card.addView(layout);
            card.setOnClickListener(v -> mostrarModalDictamenInsumo(d, container, txtResumen));

            container.addView(card, params(-1, -2, 0, 0, 0, 8));
        }

        if (txtResumen != null) {
            txtResumen.setText("Resumen del dictamen: " + aprobados + " aprobados • " + ajustados + " ajustados • " + rechazados + " rechazados.");
        }
    }

    private void mostrarModalDictamenInsumo(InsumoDictamen dictamen, LinearLayout container, TextView txtResumen) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_revision_insumo, null);

        TextView txtProducto = dialogView.findViewById(R.id.txtModalProductoNombre);
        TextView txtOriginal = dialogView.findViewById(R.id.txtModalCantidadOriginal);
        TextView btnAprobar = dialogView.findViewById(R.id.btnDictamenAprobar);
        TextView btnAjustar = dialogView.findViewById(R.id.btnDictamenAjustar);
        TextView btnRechazar = dialogView.findViewById(R.id.btnDictamenRechazar);
        EditText etCantidad = dialogView.findViewById(R.id.etModalCantidadAjustada);
        EditText etObs = dialogView.findViewById(R.id.etModalObservacionInsumo);
        ImageView btnCerrar = dialogView.findViewById(R.id.btnCerrarModal);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnModalCancelar);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnModalGuardarDictamen);

        txtProducto.setText(dictamen.entity.nombre);
        txtOriginal.setText("Cantidad solicitada: " + dictamen.entity.cantidad + (dictamen.entity.sku != null && !dictamen.entity.sku.isEmpty() ? " • SKU: " + dictamen.entity.sku : ""));
        etCantidad.setText(dictamen.cantidadAjustada);
        etObs.setText(dictamen.observacion);

        final String[] estadoSeleccionado = {dictamen.estado};

        Runnable actualizarBotonesModal = () -> {
            btnAprobar.setBackground(ContextCompat.getDrawable(this, "APROBADO".equals(estadoSeleccionado[0]) ? R.drawable.bg_button_primary : R.drawable.bg_green_chip));
            btnAprobar.setTextColor(ContextCompat.getColor(this, "APROBADO".equals(estadoSeleccionado[0]) ? R.color.white : R.color.krono_green_text));

            btnAjustar.setBackground(ContextCompat.getDrawable(this, "AJUSTADO".equals(estadoSeleccionado[0]) ? R.drawable.bg_button_primary : R.drawable.bg_chip));
            btnAjustar.setTextColor(ContextCompat.getColor(this, "AJUSTADO".equals(estadoSeleccionado[0]) ? R.color.white : R.color.krono_chip_text));

            btnRechazar.setBackground(ContextCompat.getDrawable(this, "RECHAZADO".equals(estadoSeleccionado[0]) ? R.drawable.bg_button_primary : R.drawable.bg_red_chip));
            btnRechazar.setTextColor(ContextCompat.getColor(this, "RECHAZADO".equals(estadoSeleccionado[0]) ? R.color.white : R.color.krono_red_text));
        };

        actualizarBotonesModal.run();

        btnAprobar.setOnClickListener(v -> {
            estadoSeleccionado[0] = "APROBADO";
            etCantidad.setText(dictamen.entity.cantidad);
            actualizarBotonesModal.run();
        });

        btnAjustar.setOnClickListener(v -> {
            estadoSeleccionado[0] = "AJUSTADO";
            actualizarBotonesModal.run();
        });

        btnRechazar.setOnClickListener(v -> {
            estadoSeleccionado[0] = "RECHAZADO";
            actualizarBotonesModal.run();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            dictamen.estado = estadoSeleccionado[0];
            String cant = etCantidad.getText().toString().trim();
            dictamen.cantidadAjustada = cant.isEmpty() ? dictamen.entity.cantidad : cant;
            dictamen.observacion = etObs.getText().toString().trim();
            dialog.dismiss();

            renderizarListaInsumosDictamen(container, txtResumen);
            Toast.makeText(this, "Dictamen actualizado: " + dictamen.estado, Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private String construirTextoResumenDictamen() {
        StringBuilder sb = new StringBuilder();
        for (InsumoDictamen d : dictamenList) {
            sb.append(d.entity.nombre).append(" [").append(d.estado).append(" - ").append(d.cantidadAjustada).append("]");
            if (d.observacion != null && !d.observacion.isEmpty()) {
                sb.append(" (").append(d.observacion).append(")");
            }
            sb.append(" | ");
        }
        return sb.toString();
    }

    private void guardarRevisionActual(PedidoEntity pedido, String farmName, String obsGeneral, List<InsumoPedidoEntity> fallbackItems) {
        String textoResumen = construirTextoResumenDictamen();

        if (pedido.id <= 0) {
            pedidoRepository.guardarPedido(pedido, fallbackItems != null ? fallbackItems : new ArrayList<>(), new PedidoRepository.GuardarPedidoCallback() {
                @Override
                public void onSuccess(long pedidoId) {
                    pedido.id = pedidoId;
                    RevisionPedidoEntity rev = new RevisionPedidoEntity(pedidoId, userId, textoResumen, obsGeneral, System.currentTimeMillis());
                    pedidoRepository.guardarRevision(rev, new PedidoRepository.GuardarPedidoCallback() {
                        @Override
                        public void onSuccess(long revId) {
                            auditRepository.auditar(userId, "REVISION_GUARDADA", "PEDIDO", pedido.codigo);
                            Toast.makeText(SupervisorActivity.this, "Revisión guardada en base de datos local SQLite.", Toast.LENGTH_SHORT).show();
                            abrirDetallePedido(pedido, farmName, fallbackItems);
                        }

                        @Override
                        public void onError(Exception error) {
                            Toast.makeText(SupervisorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SupervisorActivity.this, "Error al registrar pedido: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            RevisionPedidoEntity rev = new RevisionPedidoEntity(pedido.id, userId, textoResumen, obsGeneral, System.currentTimeMillis());
            pedidoRepository.guardarRevision(rev, new PedidoRepository.GuardarPedidoCallback() {
                @Override
                public void onSuccess(long revId) {
                    auditRepository.auditar(userId, "REVISION_GUARDADA", "PEDIDO", pedido.codigo);
                    Toast.makeText(SupervisorActivity.this, "Revisión guardada en base de datos local SQLite.", Toast.LENGTH_SHORT).show();
                    abrirDetallePedido(pedido, farmName, fallbackItems);
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SupervisorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void aprobarPedidoYPasarAPedidos(PedidoEntity pedido, String farmName, String obsGeneral, List<InsumoPedidoEntity> fallbackItems) {
        String textoResumen = construirTextoResumenDictamen();
        pedido.estado = "APROBADO";

        if (pedido.id <= 0) {
            pedidoRepository.guardarPedido(pedido, fallbackItems != null ? fallbackItems : new ArrayList<>(), new PedidoRepository.GuardarPedidoCallback() {
                @Override
                public void onSuccess(long pedidoId) {
                    pedido.id = pedidoId;
                    RevisionPedidoEntity rev = new RevisionPedidoEntity(pedidoId, userId, textoResumen, obsGeneral, System.currentTimeMillis());
                    pedidoRepository.guardarRevision(rev, new PedidoRepository.GuardarPedidoCallback() {
                        @Override
                        public void onSuccess(long revId) {
                            auditRepository.auditar(userId, "APROBADO", "PEDIDO", pedido.codigo);
                            Toast.makeText(SupervisorActivity.this, "✓ Pedido " + pedido.codigo + " aprobado y registrado en Historial.", Toast.LENGTH_SHORT).show();
                            loadHistorialView();
                        }

                        @Override
                        public void onError(Exception error) {
                            loadHistorialView();
                        }
                    });
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SupervisorActivity.this, "Error al aprobar pedido: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            pedidoRepository.actualizarEstado(pedido.id, "APROBADO", new PedidoRepository.GuardarPedidoCallback() {
                @Override
                public void onSuccess(long id) {
                    RevisionPedidoEntity rev = new RevisionPedidoEntity(pedido.id, userId, textoResumen, obsGeneral, System.currentTimeMillis());
                    pedidoRepository.guardarRevision(rev, new PedidoRepository.GuardarPedidoCallback() {
                        @Override
                        public void onSuccess(long revId) {
                            auditRepository.auditar(userId, "APROBADO", "PEDIDO", pedido.codigo);
                            Toast.makeText(SupervisorActivity.this, "✓ Pedido aprobado y registrado en Historial.", Toast.LENGTH_SHORT).show();
                            loadHistorialView();
                        }

                        @Override
                        public void onError(Exception error) {
                            loadHistorialView();
                        }
                    });
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SupervisorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void rechazarPedido(PedidoEntity pedido, String farmName, List<InsumoPedidoEntity> fallbackItems) {
        String textoResumen = construirTextoResumenDictamen();
        pedido.estado = "RECHAZADO";

        if (pedido.id <= 0) {
            pedidoRepository.guardarPedido(pedido, fallbackItems != null ? fallbackItems : new ArrayList<>(), new PedidoRepository.GuardarPedidoCallback() {
                @Override
                public void onSuccess(long pedidoId) {
                    pedido.id = pedidoId;
                    auditRepository.auditar(userId, "RECHAZADO", "PEDIDO", pedido.codigo);
                    Toast.makeText(SupervisorActivity.this, "Pedido marcado como Rechazado.", Toast.LENGTH_SHORT).show();
                    loadHistorialView();
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SupervisorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            pedidoRepository.actualizarEstado(pedido.id, "RECHAZADO", new PedidoRepository.GuardarPedidoCallback() {
                @Override
                public void onSuccess(long id) {
                    auditRepository.auditar(userId, "RECHAZADO", "PEDIDO", pedido.codigo);
                    Toast.makeText(SupervisorActivity.this, "Pedido marcado como Rechazado.", Toast.LENGTH_SHORT).show();
                    loadHistorialView();
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SupervisorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ==========================================
    // 4. PEDIDOS (Solo Revisión Jerárquica: Zona → Granja → Pedidos)
    // ==========================================
    public void loadPedidosView() {
        setContentView(R.layout.activity_sup_pedidos);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        setupPedidosClickListeners();
        cargarPedidosEnLayoutPedidos();
    }

    private void setupPedidosClickListeners() {
        View btnLogout = findViewById(R.id.btnBackLogin);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> finish());

        View tabZonas = findViewById(R.id.tabZonas);
        if (tabZonas != null) tabZonas.setOnClickListener(v -> loadZonasView());

        View tabPedidos = findViewById(R.id.tabPedidos);
        if (tabPedidos != null) {
            tabPedidos.setOnClickListener(v ->
                    Toast.makeText(this, "Sección actual: Pedidos (Solo Revisión)", Toast.LENGTH_SHORT).show()
            );
        }

        View tabHistorial = findViewById(R.id.tabHistorial);
        if (tabHistorial != null) tabHistorial.setOnClickListener(v -> loadHistorialView());

        // Botones "Ver Hoja"
        int[] verHojaIds = new int[]{R.id.btnVerHoja1, R.id.btnVerHoja2, R.id.btnVerHoja3, R.id.btnVerHoja4};
        for (int id : verHojaIds) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> loadHojaPedidoView("Finca San Isidro"));
            }
        }

        // Botones "Iniciar Despacho"
        int[] despachoIds = new int[]{R.id.btnIniciarDespacho1, R.id.btnIniciarDespacho2, R.id.btnIniciarDespacho3, R.id.btnIniciarDespacho4};
        for (int id : despachoIds) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v ->
                        Toast.makeText(this, "Proceso de despacho en rampa iniciado.", Toast.LENGTH_SHORT).show()
                );
            }
        }

        View btnConsolidar = findViewById(R.id.btnConsolidar);
        if (btnConsolidar != null) {
            btnConsolidar.setOnClickListener(v ->
                    Toast.makeText(this, "Consolidando pedidos de bodega para salida en ruta...", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void cargarPedidosEnLayoutPedidos() {
        LinearLayout container = findViewById(R.id.containerPedidosDinamicos);
        if (container == null) return;
        container.removeAllViews();

        pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
            @Override
            public void onSuccess(List<PedidoEntity> pedidos) {
                if (pedidos.isEmpty()) return;

                // Encapsular: Zona -> Granja -> Hojas de Pedido
                Map<String, Map<String, List<PedidoEntity>>> jerarquia = new LinkedHashMap<>();

                for (PedidoEntity p : pedidos) {
                    String zonaNombre = p.zona != null && !p.zona.trim().isEmpty() ? p.zona : "Zona Norte";
                    String granjaNombre = currentFarmName != null ? currentFarmName : "Finca San Isidro";

                    if (!jerarquia.containsKey(zonaNombre)) {
                        jerarquia.put(zonaNombre, new LinkedHashMap<>());
                    }
                    Map<String, List<PedidoEntity>> granjasDeZona = jerarquia.get(zonaNombre);
                    if (!granjasDeZona.containsKey(granjaNombre)) {
                        granjasDeZona.put(granjaNombre, new ArrayList<>());
                    }
                    granjasDeZona.get(granjaNombre).add(p);
                }

                for (Map.Entry<String, Map<String, List<PedidoEntity>>> zonaEntry : jerarquia.entrySet()) {
                    String zona = zonaEntry.getKey();
                    Map<String, List<PedidoEntity>> granjas = zonaEntry.getValue();

                    container.addView(crearEncabezadoZona(zona, granjas.size()));

                    for (Map.Entry<String, List<PedidoEntity>> granjaEntry : granjas.entrySet()) {
                        String granja = granjaEntry.getKey();
                        List<PedidoEntity> listaPedidos = granjaEntry.getValue();

                        for (PedidoEntity p : listaPedidos) {
                            container.addView(crearCardPedidoRevisionSolo(p, granja));
                        }
                    }
                }
            }

            @Override
            public void onError(Exception error) {}
        });
    }

    private View crearEncabezadoZona(String zona, int totalGranjas) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, dp(14), 0, dp(6));

        TextView tag = new TextView(this);
        tag.setText(zona.toUpperCase());
        tag.setTextSize(14);
        tag.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tag.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        header.addView(tag);

        TextView count = new TextView(this);
        count.setText(totalGranjas + (totalGranjas == 1 ? " Granja" : " Granjas"));
        count.setTextSize(11);
        count.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        count.setTextColor(ContextCompat.getColor(this, R.color.krono_chip_text));
        count.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip));
        count.setPadding(dp(6), dp(2), dp(6), dp(2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(dp(8), 0, 0, 0);
        header.addView(count, lp);

        return header;
    }

    private View crearCardPedidoRevisionSolo(PedidoEntity pedido, String granja) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(16));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        card.setStrokeWidth(dp(1));
        card.setCardElevation(0);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(14), dp(14), dp(14), dp(14));

        // Cabecera: Granja y badge de estado
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_tractor);
        icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        top.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setPadding(dp(10), 0, 0, 0);

        TextView farmName = new TextView(this);
        farmName.setText(granja);
        farmName.setTextSize(15);
        farmName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        farmName.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        titleCol.addView(farmName);

        TextView codeText = new TextView(this);
        codeText.setText((pedido.codigo != null ? pedido.codigo : "PED-" + pedido.id) + " • " + (pedido.operarioId != null ? pedido.operarioId : "Operario"));
        codeText.setTextSize(11);
        codeText.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        titleCol.addView(codeText);

        top.addView(titleCol, new LinearLayout.LayoutParams(0, -2, 1));

        TextView time = new TextView(this);
        time.setText(android.text.format.DateFormat.format("dd/MM HH:mm", pedido.creadoEn));
        time.setTextSize(11);
        time.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        time.setTextColor(ContextCompat.getColor(this, R.color.krono_chip_text));
        time.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip));
        time.setPadding(dp(8), dp(3), dp(8), dp(3));
        top.addView(time);

        layout.addView(top);

        // Barra de estado (Solo Revisión)
        LinearLayout statusBar = new LinearLayout(this);
        statusBar.setGravity(Gravity.CENTER_VERTICAL);
        statusBar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_cierre_box));
        statusBar.setPadding(dp(10), dp(6), dp(10), dp(6));
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(-1, -2);
        sbLp.setMargins(0, dp(10), 0, 0);

        ImageView checkIcon = new ImageView(this);
        checkIcon.setImageResource(R.drawable.ic_check_circle);
        checkIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        statusBar.addView(checkIcon, new LinearLayout.LayoutParams(dp(14), dp(14)));

        TextView statusText = new TextView(this);
        statusText.setText("ESTADO: " + (pedido.estado != null ? pedido.estado.toUpperCase() : "APROBADO"));
        statusText.setTextSize(11);
        statusText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        statusText.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        statusText.setPadding(dp(6), 0, 0, 0);
        statusBar.addView(statusText);

        layout.addView(statusBar, sbLp);

        // Insumos en lista (Solo Revisión)
        LinearLayout itemsContainer = new LinearLayout(this);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        itemsContainer.setPadding(0, dp(8), 0, 0);
        layout.addView(itemsContainer);

        pedidoRepository.obtenerInsumos(pedido.id, new PedidoRepository.InsumosCallback() {
            @Override
            public void onSuccess(List<InsumoPedidoEntity> items) {
                for (InsumoPedidoEntity it : items) {
                    TextView line = new TextView(SupervisorActivity.this);
                    line.setText("• " + it.nombre + " (" + it.cantidad + ")");
                    line.setTextSize(12);
                    line.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                    line.setPadding(0, dp(2), 0, dp(2));
                    itemsContainer.addView(line);
                }
            }

            @Override
            public void onError(Exception error) {}
        });

        card.addView(layout);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.setMargins(0, dp(8), 0, dp(8));
        card.setLayoutParams(cardLp);
        card.setOnClickListener(v -> loadHojaPedidoView(pedido.granja != null ? pedido.granja : "Granja Los Laureles"));
        return card;
    }

    // ==========================================
    // 5. HISTORIAL: RÉPLICA DE ZONAS Y GRANJAS CON EXPORTACIÓN POR ZONA Y GRANJA
    // ==========================================
    public void loadHistorialView() {
        setContentView(R.layout.activity_sup_historial);
        StatusBarUtils.applyStatusBarNavyHeader(this);
        StatusBarUtils.setupIntelligentKeyboard(this, findViewById(R.id.scrollViewHistorial));

        setupHistorialClickListeners();
        actualizarVistaHistorialZonaByName(currentHistorialZoneName);
    }

    private void setupHistorialClickListeners() {
        View btnLogout = findViewById(R.id.btnBackLogin);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> finish());

        // Selector desplegable de zona en Historial
        View btnDropdownHistorial = findViewById(R.id.btnZoneDropdownHistorial);
        if (btnDropdownHistorial != null) {
            btnDropdownHistorial.setOnClickListener(v -> mostrarDialogoSeleccionarZona(true));
        }

        // Chips de zonas en Historial
        TextView chipNorte = findViewById(R.id.chipHistorialNorte);
        TextView chipValle = findViewById(R.id.chipHistorialValle);
        TextView chipSur = findViewById(R.id.chipHistorialSur);
        TextView chipOccidente = findViewById(R.id.chipHistorialOccidente);

        if (chipNorte != null) chipNorte.setOnClickListener(v -> actualizarVistaHistorialZona(Zone.NORTE));
        if (chipValle != null) chipValle.setOnClickListener(v -> actualizarVistaHistorialZona(Zone.VALLE));
        if (chipSur != null) chipSur.setOnClickListener(v -> actualizarVistaHistorialZona(Zone.SUR));
        if (chipOccidente != null) chipOccidente.setOnClickListener(v -> actualizarVistaHistorialZona(Zone.OCCIDENTE));

        // Exportación de la zona seleccionada
        View btnCsv = findViewById(R.id.btnExportZonaCsv);
        if (btnCsv != null) {
            btnCsv.setOnClickListener(v -> exportarPedidosFiltrados(currentHistorialZone.shortName, null, false));
        }

        View btnPdf = findViewById(R.id.btnExportZonaPdf);
        if (btnPdf != null) {
            btnPdf.setOnClickListener(v -> exportarPedidosFiltrados(currentHistorialZone.shortName, null, true));
        }

        View tabZonas = findViewById(R.id.tabZonas);
        if (tabZonas != null) tabZonas.setOnClickListener(v -> loadZonasView());

        View tabHistorial = findViewById(R.id.tabHistorial);
        if (tabHistorial != null) {
            tabHistorial.setOnClickListener(v ->
                    Toast.makeText(this, "Sección actual: Historial Consolidado", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void actualizarVistaHistorialZona(Zone zone) {
        actualizarVistaHistorialZonaByName(zone.shortName);
    }

    private void actualizarVistaHistorialZonaByName(String zoneName) {
        if (zoneName == null || zoneName.isEmpty()) zoneName = "Zona Norte";
        currentHistorialZoneName = zoneName;

        for (Zone z : Zone.values()) {
            if (z.shortName.equalsIgnoreCase(zoneName) || z.title.equalsIgnoreCase(zoneName)) {
                currentHistorialZone = z;
                break;
            }
        }

        // Si veníamos de una vista de pedidos o detalle, restaurar el layout completo de Historial si es necesario
        View cardSelect = findViewById(R.id.cardSelectZoneHistorial);
        if (cardSelect == null) {
            loadHistorialView();
            return;
        }

        // Actualizar chips de zona activa
        actualizarChipHistorialStyle(R.id.chipHistorialNorte, zoneName.equalsIgnoreCase("Zona Norte"), "• Zona Norte (Activo)", "Zona Norte");
        actualizarChipHistorialStyle(R.id.chipHistorialValle, zoneName.equalsIgnoreCase("Zona Valle Central"), "• Zona Valle Central (Activo)", "Zona Valle Central");
        actualizarChipHistorialStyle(R.id.chipHistorialSur, zoneName.equalsIgnoreCase("Zona Sur"), "• Zona Sur (Activo)", "Zona Sur");
        actualizarChipHistorialStyle(R.id.chipHistorialOccidente, zoneName.equalsIgnoreCase("Zona Occidente"), "• Zona Occidente (Activo)", "Zona Occidente");

        // Cargar dinámicamente todos los chips de zonas en historial
        refrescarChipsDinamicosZonas(true, zoneName);

        // Actualizar textos de cabecera de la zona
        TextView txtTitulo = findViewById(R.id.txtSelectedZoneTitleHistorial);
        if (txtTitulo != null) txtTitulo.setText(zoneName);

        TextView txtTituloExport = findViewById(R.id.txtTituloExportZona);
        if (txtTituloExport != null) txtTituloExport.setText("Exportar Pedidos de " + zoneName);

        TextView txtHeaderGranjas = findViewById(R.id.txtHeaderGranjasHistorial);
        if (txtHeaderGranjas != null) txtHeaderGranjas.setText("Granjas Registradas en " + zoneName);

        final String finalZoneName = zoneName;
        View btnCsv = findViewById(R.id.btnExportZonaCsv);
        if (btnCsv != null) {
            btnCsv.setOnClickListener(v -> exportarPedidosFiltrados(finalZoneName, null, false));
        }

        View btnPdf = findViewById(R.id.btnExportZonaPdf);
        if (btnPdf != null) {
            btnPdf.setOnClickListener(v -> exportarPedidosFiltrados(finalZoneName, null, true));
        }

        TextView txtSub = findViewById(R.id.txtSelectedZoneSubHistorial);
        TextView txtVisibles = findViewById(R.id.txtCountVisiblesHistorial);

        auditRepository.listarGranjasPorZona(finalZoneName, new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override
            public void onSuccess(List<FarmEntity> granjas) {
                int totalGranjas = (granjas != null) ? granjas.size() : 0;
                pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
                    @Override
                    public void onSuccess(List<PedidoEntity> pedidos) {
                        int listas = 0;
                        if (pedidos != null) {
                            for (PedidoEntity p : pedidos) {
                                if (p.zona != null && p.zona.equalsIgnoreCase(finalZoneName) && "APROBADO".equalsIgnoreCase(p.estado)) {
                                    listas++;
                                }
                            }
                        }
                        if (txtSub != null) {
                            txtSub.setText(totalGranjas + (totalGranjas == 1 ? " Granja asignada" : " Granjas asignadas") +
                                    " • " + listas + (listas == 1 ? " lista" : " listas"));
                        }
                        if (txtVisibles != null) {
                            txtVisibles.setText(totalGranjas + (totalGranjas == 1 ? " granja" : " granjas"));
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        if (txtVisibles != null) txtVisibles.setText(totalGranjas + " granjas");
                    }
                });
            }

            @Override
            public void onError(Exception error) {}
        });

        // Renderizar granjas de la zona consultadas desde SQLite
        cargarGranjasHistorialDeZonaByName(finalZoneName);
    }

    private void actualizarChipHistorialStyle(int chipId, boolean active, String activeText, String inactiveText) {
        TextView chip = findViewById(chipId);
        if (chip == null) return;
        chip.setText(active ? activeText : inactiveText);
        chip.setBackgroundResource(active ? R.drawable.bg_button_primary : R.drawable.bg_chip);
        chip.setTextColor(ContextCompat.getColor(this, active ? R.color.white : R.color.krono_chip_text));
        chip.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void cargarGranjasHistorialDeZona(Zone zone) {
        cargarGranjasHistorialDeZonaByName(zone.shortName);
    }

    private void cargarGranjasHistorialDeZonaByName(String zoneName) {
        LinearLayout container = findViewById(R.id.containerGranjasHistorial);
        if (container == null) return;
        container.removeAllViews();

        auditRepository.listarGranjasPorZona(zoneName, new AuthRepository.ResultCallback<List<FarmEntity>>() {
            @Override
            public void onSuccess(List<FarmEntity> granjas) {
                container.removeAllViews();
                if (granjas == null || granjas.isEmpty()) {
                    MaterialCardView emptyCard = new MaterialCardView(SupervisorActivity.this);
                    emptyCard.setCardBackgroundColor(Color.WHITE);
                    emptyCard.setRadius(dp(14));
                    emptyCard.setStrokeColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_card_stroke));
                    emptyCard.setStrokeWidth(dp(1));
                    TextView txt = new TextView(SupervisorActivity.this);
                    txt.setText("No hay granjas registradas en " + zoneName + ".");
                    txt.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                    txt.setTextSize(13);
                    txt.setPadding(dp(16), dp(16), dp(16), dp(16));
                    emptyCard.addView(txt);
                    container.addView(emptyCard, params(-1, -2, 0, 10, 0, 0));
                    return;
                }

                for (FarmEntity farm : granjas) {
                    container.addView(crearCardGranjaHistorialConExport(farm.nombre, zoneName));
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(SupervisorActivity.this, "Error al cargar granjas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View crearCardGranjaHistorialConExport(String granjaNombre, String zonaNombre) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(16));
        card.setCardElevation(0);
        card.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        card.setStrokeWidth(dp(1));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(14), dp(12), dp(14), dp(12));

        // Fila 1: Icono Tractor, Nombre, Checkmark y Badge de Estado
        RelativeLayout topRow = new RelativeLayout(this);

        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setId(View.generateViewId());
        iconFrame.setBackgroundResource(R.drawable.bg_icon_container);
        ImageView iconTractor = new ImageView(this);
        iconTractor.setImageResource(R.drawable.ic_tractor);
        iconTractor.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(dp(22), dp(22));
        tLp.gravity = Gravity.CENTER;
        iconFrame.addView(iconTractor, tLp);

        RelativeLayout.LayoutParams fLp = new RelativeLayout.LayoutParams(dp(40), dp(40));
        fLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        fLp.addRule(RelativeLayout.CENTER_VERTICAL);
        topRow.addView(iconFrame, fLp);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView txtName = new TextView(this);
        txtName.setText(granjaNombre);
        txtName.setTextSize(15);
        txtName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        txtName.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        nameRow.addView(txtName);

        ImageView check = new ImageView(this);
        check.setImageResource(R.drawable.ic_check_circle);
        check.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(dp(16), dp(16));
        cLp.setMargins(dp(4), 0, 0, 0);
        nameRow.addView(check, cLp);
        titleCol.addView(nameRow);

        TextView txtFecha = new TextView(this);
        txtFecha.setText("Enviado: 13 Nov 2026 • 11:20 AM");
        txtFecha.setTextSize(11);
        txtFecha.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        txtFecha.setPadding(0, dp(2), 0, 0);
        titleCol.addView(txtFecha);

        RelativeLayout.LayoutParams tColLp = new RelativeLayout.LayoutParams(-1, -2);
        tColLp.addRule(RelativeLayout.END_OF, iconFrame.getId());
        tColLp.addRule(RelativeLayout.CENTER_VERTICAL);
        tColLp.setMargins(dp(10), 0, dp(10), 0);
        topRow.addView(titleCol, tColLp);

        layout.addView(topRow);

        // Fila 2: Botones de Acción para la Granja (Ver Pedidos y Exportar Granja)
        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.CENTER_VERTICAL);
        actionsRow.setPadding(0, dp(10), 0, 0);

        MaterialButton btnVer = new MaterialButton(this);
        btnVer.setText("Ver Hojas de Pedido");
        btnVer.setAllCaps(false);
        btnVer.setTextSize(12);
        btnVer.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
        btnVer.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_card_stroke)));
        btnVer.setStrokeWidth(dp(1));
        btnVer.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnVer.setCornerRadius(dp(8));
        btnVer.setOnClickListener(v -> mostrarHojasPedidoHistorial(granjaNombre, zonaNombre));
        actionsRow.addView(btnVer, new LinearLayout.LayoutParams(0, dp(38), 1));

        View space = new View(this);
        actionsRow.addView(space, new LinearLayout.LayoutParams(dp(8), 0));

        MaterialButton btnExpGranja = new MaterialButton(this);
        btnExpGranja.setText("📥 Exportar Granja");
        btnExpGranja.setAllCaps(false);
        btnExpGranja.setTextSize(12);
        btnExpGranja.setTextColor(Color.WHITE);
        btnExpGranja.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        btnExpGranja.setCornerRadius(dp(8));
        btnExpGranja.setOnClickListener(v -> mostrarDialogoExportarGranja(granjaNombre, zonaNombre));
        actionsRow.addView(btnExpGranja, new LinearLayout.LayoutParams(0, dp(38), 1));

        layout.addView(actionsRow);

        card.addView(layout);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.setMargins(0, dp(4), 0, dp(8));
        card.setLayoutParams(cardLp);
        card.setOnClickListener(v -> mostrarHojasPedidoHistorial(granjaNombre, zonaNombre));
        return card;
    }

    private void mostrarDialogoExportarGranja(String granjaNombre, String zonaNombre) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exportar_reporte, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView txtSub = dialogView.findViewById(R.id.txtDialogExportSub);
        if (txtSub != null) {
            txtSub.setText("¿En qué formato deseas exportar los pedidos de " + granjaNombre + "?");
        }

        View btnCsv = dialogView.findViewById(R.id.btnExportOpcionCsv);
        if (btnCsv != null) {
            btnCsv.setOnClickListener(v -> {
                dialog.dismiss();
                exportarPedidosFiltrados(zonaNombre, granjaNombre, false);
            });
        }

        View btnPdf = dialogView.findViewById(R.id.btnExportOpcionPdf);
        if (btnPdf != null) {
            btnPdf.setOnClickListener(v -> {
                dialog.dismiss();
                exportarPedidosFiltrados(zonaNombre, granjaNombre, true);
            });
        }

        View btnCancelar = dialogView.findViewById(R.id.btnDialogExportCancelar);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Muestra el modal para seleccionar una zona operativa (usado en Zonas y Granjas e Historial).
     */
    private void mostrarDialogoSeleccionarZona(boolean esHistorial) {
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

        if (container != null) {
            container.removeAllViews();
            String currentActiveName = esHistorial ? currentHistorialZoneName : currentZoneName;

            auditRepository.listarZonas(new AuthRepository.ResultCallback<List<ZoneEntity>>() {
                @Override
                public void onSuccess(List<ZoneEntity> zonas) {
                    List<ZoneEntity> displayList = new ArrayList<>();
                    if (zonas != null && !zonas.isEmpty()) {
                        displayList.addAll(zonas);
                    } else {
                        for (Zone z : Zone.values()) {
                            displayList.add(new ZoneEntity(z.shortName, z.title, true));
                        }
                    }

                    container.removeAllViews();
                    for (ZoneEntity z : displayList) {
                        MaterialCardView optionCard = new MaterialCardView(SupervisorActivity.this);
                        optionCard.setRadius(dp(12));
                        boolean isSelected = z.nombre.equalsIgnoreCase(currentActiveName);
                        optionCard.setCardBackgroundColor(isSelected ? ContextCompat.getColor(SupervisorActivity.this, R.color.krono_bg) : Color.WHITE);
                        optionCard.setStrokeColor(ContextCompat.getColor(SupervisorActivity.this, isSelected ? R.color.krono_navy : R.color.krono_card_stroke));
                        optionCard.setStrokeWidth(dp(isSelected ? 2 : 1));
                        optionCard.setCardElevation(0);
                        optionCard.setClickable(true);
                        optionCard.setFocusable(true);

                        LinearLayout row = new LinearLayout(SupervisorActivity.this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setGravity(Gravity.CENTER_VERTICAL);
                        row.setPadding(dp(14), dp(12), dp(14), dp(12));

                        ImageView icon = new ImageView(SupervisorActivity.this);
                        icon.setImageResource(R.drawable.ic_location);
                        icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(SupervisorActivity.this, isSelected ? R.color.krono_navy : R.color.krono_text_secondary)));
                        row.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));

                        LinearLayout col = new LinearLayout(SupervisorActivity.this);
                        col.setOrientation(LinearLayout.VERTICAL);
                        col.setPadding(dp(12), 0, dp(12), 0);

                        TextView title = new TextView(SupervisorActivity.this);
                        title.setText(z.nombre);
                        title.setTextSize(14);
                        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                        title.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_navy_dark));
                        col.addView(title);

                        TextView sub = new TextView(SupervisorActivity.this);
                        String desc = (z.descripcion != null && !z.descripcion.isEmpty()) ? z.descripcion : "Región productiva";
                        sub.setText(desc);
                        sub.setTextSize(11);
                        sub.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                        col.addView(sub);

                        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1));

                        if (isSelected) {
                            TextView badge = new TextView(SupervisorActivity.this);
                            badge.setText("Activa");
                            badge.setTextSize(10);
                            badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                            badge.setTextColor(Color.WHITE);
                            badge.setBackgroundResource(R.drawable.bg_button_primary);
                            badge.setPadding(dp(8), dp(3), dp(8), dp(3));
                            row.addView(badge);
                        }

                        optionCard.addView(row);

                        final String selectedName = z.nombre;
                        optionCard.setOnClickListener(v -> {
                            dialog.dismiss();
                            if (esHistorial) {
                                actualizarVistaHistorialZonaByName(selectedName);
                            } else {
                                selectZoneByName(selectedName);
                            }
                        });

                        container.addView(optionCard, params(-1, -2, 0, 4, 0, 4));
                    }
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SupervisorActivity.this, "Error al cargar zonas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    /**
     * Muestra la lista de hojas de pedidos de una granja dentro de la pestaña HISTORIAL (Solo Lectura).
     */
    private void mostrarHojasPedidoHistorial(String farmName, String zonaName) {
        LinearLayout contentLayout = findViewById(R.id.contentLayoutHistorial);
        if (contentLayout == null) return;
        contentLayout.removeAllViews();

        // 1. Botón píldora Volver
        MaterialButton btnVolver = new MaterialButton(this);
        btnVolver.setText("← Volver a Historial");
        btnVolver.setAllCaps(false);
        btnVolver.setTextSize(13);
        btnVolver.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
        btnVolver.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_card_stroke)));
        btnVolver.setStrokeWidth(dp(1));
        btnVolver.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnVolver.setCornerRadius(dp(20));
        btnVolver.setPadding(dp(16), dp(8), dp(16), dp(8));
        btnVolver.setOnClickListener(v -> actualizarVistaHistorialZona(currentHistorialZone));
        contentLayout.addView(btnVolver, params(-2, dp(40), 0, 0, 0, 14));

        // 2. Ruta / Breadcrumb
        TextView breadcrumb = new TextView(this);
        breadcrumb.setText("• " + zonaName + "  >  🚜 " + farmName);
        breadcrumb.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        breadcrumb.setTextSize(12);
        contentLayout.addView(breadcrumb, params(-1, -2, 0, 0, 0, 4));

        // 3. Título y Contador
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("Hojas de Pedidos");
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        titleRow.addView(title);

        TextView badgeCount = new TextView(this);
        badgeCount.setText("Solo Lectura");
        badgeCount.setTextSize(11);
        badgeCount.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badgeCount.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        badgeCount.setBackgroundResource(R.drawable.bg_chip);
        badgeCount.setPadding(dp(8), dp(3), dp(8), dp(3));
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(-2, -2);
        bLp.setMargins(dp(10), 0, 0, 0);
        titleRow.addView(badgeCount, bLp);

        contentLayout.addView(titleRow);

        TextView sub = new TextView(this);
        sub.setText("Consulta el historial de pedidos de esta granja. Toque un pedido para ver su detalle e insumos.");
        sub.setTextSize(12);
        sub.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        contentLayout.addView(sub, params(-1, -2, 0, 4, 0, 14));

        // 4. Contenedor de pedidos
        LinearLayout containerPedidos = new LinearLayout(this);
        containerPedidos.setOrientation(LinearLayout.VERTICAL);
        contentLayout.addView(containerPedidos, params(-1, -2, 0, 0, 0, 0));

        pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
            @Override
            public void onSuccess(List<PedidoEntity> pedidos) {
                containerPedidos.removeAllViews();
                List<PedidoEntity> filtrados = new ArrayList<>();
                for (PedidoEntity p : pedidos) {
                    if ("BORRADOR".equalsIgnoreCase(p.estado)) continue;
                    if (p.granja != null && p.granja.trim().equalsIgnoreCase(farmName.trim())) {
                        filtrados.add(p);
                    }
                }

                if (filtrados.isEmpty()) {
                    MaterialCardView emptyCard = new MaterialCardView(SupervisorActivity.this);
                    emptyCard.setCardBackgroundColor(Color.WHITE);
                    emptyCard.setRadius(dp(14));
                    emptyCard.setStrokeColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_card_stroke));
                    emptyCard.setStrokeWidth(dp(1));
                    TextView txtEmpty = new TextView(SupervisorActivity.this);
                    txtEmpty.setText("No hay pedidos registrados para " + farmName + ".");
                    txtEmpty.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                    txtEmpty.setTextSize(13);
                    txtEmpty.setPadding(dp(16), dp(16), dp(16), dp(16));
                    emptyCard.addView(txtEmpty);
                    containerPedidos.addView(emptyCard, params(-1, -2, 0, 4, 0, 0));
                    return;
                }

                badgeCount.setText(filtrados.size() + " Registros");

                for (PedidoEntity p : filtrados) {
                    View cardView = crearCardPedidoHistorialLectura(p, farmName, zonaName);
                    containerPedidos.addView(cardView, params(-1, -2, 0, 0, 0, 10));
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(SupervisorActivity.this, "Error al cargar pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View crearCardPedidoHistorialLectura(PedidoEntity pedido, String farmName, String zonaName) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(16));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        card.setStrokeWidth(dp(1));
        card.setCardElevation(dp(1));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(14), dp(16), dp(14));

        // Fila 1: Chip de estado a la izquierda + Fecha a la derecha
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView chipEstado = new TextView(this);
        String estado = (pedido.estado != null && !pedido.estado.isEmpty()) ? pedido.estado : "APROBADO";
        chipEstado.setText(estado.toUpperCase());
        chipEstado.setTextSize(11);
        chipEstado.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        chipEstado.setPadding(dp(10), dp(4), dp(10), dp(4));

        GradientDrawable bgChip = new GradientDrawable();
        if ("APROBADO".equalsIgnoreCase(estado)) {
            chipEstado.setTextColor(ContextCompat.getColor(this, R.color.krono_green_text));
            bgChip.setColor(Color.parseColor("#E6F4EA"));
        } else if ("RECHAZADO".equalsIgnoreCase(estado)) {
            chipEstado.setTextColor(ContextCompat.getColor(this, R.color.krono_red_text));
            bgChip.setColor(Color.parseColor("#FCE8E6"));
        } else {
            chipEstado.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
            bgChip.setColor(Color.parseColor("#E8EAF0"));
        }
        bgChip.setCornerRadius(dp(10));
        chipEstado.setBackground(bgChip);
        row1.addView(chipEstado);

        View spacer = new View(this);
        row1.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1));

        TextView tvFecha = new TextView(this);
        String f = android.text.format.DateFormat.format("dd MMM yyyy • HH:mm",
                pedido.creadoEn > 0 ? pedido.creadoEn : System.currentTimeMillis()).toString();
        tvFecha.setText(f);
        tvFecha.setTextSize(11);
        tvFecha.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        row1.addView(tvFecha);

        layout.addView(row1);

        // Fila 2: Folio en texto grande y chevron a la derecha
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER_VERTICAL);
        row2.setPadding(0, dp(8), 0, 0);

        TextView tvFolio = new TextView(this);
        String loteStr = (pedido.lote != null && !pedido.lote.isEmpty()) ? pedido.lote : "Lote 01";
        tvFolio.setText("Número de Lote: " + loteStr);
        tvFolio.setTextSize(17);
        tvFolio.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tvFolio.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        row2.addView(tvFolio, new LinearLayout.LayoutParams(0, -2, 1));

        FrameLayout circleBtn = new FrameLayout(this);
        circleBtn.setBackgroundResource(R.drawable.bg_icon_container);
        ImageView icChevron = new ImageView(this);
        icChevron.setImageResource(R.drawable.ic_arrow_forward);
        icChevron.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_navy)));
        FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(dp(18), dp(18));
        cLp.gravity = Gravity.CENTER;
        circleBtn.addView(icChevron, cLp);
        row2.addView(circleBtn, new LinearLayout.LayoutParams(dp(36), dp(36)));

        layout.addView(row2);

        // Fila 3: Subtítulo
        TextView tvSub = new TextView(this);
        String solicitante = pedido.operarioId != null ? pedido.operarioId : "Operario";
        tvSub.setText("Solicitado por " + solicitante + " • Toca para consultar dictamen");
        tvSub.setTextSize(12);
        tvSub.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        tvSub.setPadding(0, dp(4), 0, 0);
        layout.addView(tvSub);

        card.addView(layout);
        card.setOnClickListener(v -> mostrarDetallePedidoHistorial(pedido, farmName, zonaName));
        return card;
    }

    /**
     * Muestra la vista de detalle de un pedido en modo SOLO LECTURA dentro de Historial.
     */
    private void mostrarDetallePedidoHistorial(PedidoEntity pedido, String farmName, String zonaName) {
        LinearLayout contentLayout = findViewById(R.id.contentLayoutHistorial);
        if (contentLayout == null) return;
        contentLayout.removeAllViews();

        // 1. Botón píldora Volver a Hojas de Pedido
        MaterialButton btnVolver = new MaterialButton(this);
        btnVolver.setText("← Volver a Hojas de Pedido");
        btnVolver.setAllCaps(false);
        btnVolver.setTextSize(13);
        btnVolver.setTextColor(ContextCompat.getColor(this, R.color.krono_navy));
        btnVolver.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.krono_card_stroke)));
        btnVolver.setStrokeWidth(dp(1));
        btnVolver.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnVolver.setCornerRadius(dp(20));
        btnVolver.setPadding(dp(16), dp(8), dp(16), dp(8));
        btnVolver.setOnClickListener(v -> mostrarHojasPedidoHistorial(farmName, zonaName));
        contentLayout.addView(btnVolver, params(-2, dp(40), 0, 0, 0, 14));

        // 2. Encabezado con Título y Estado
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("Detalle del Pedido");
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        headerRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = new TextView(this);
        String est = (pedido.estado != null && !pedido.estado.isEmpty()) ? pedido.estado : "APROBADO";
        badge.setText(est.toUpperCase());
        badge.setTextSize(11);
        badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable bgChip = new GradientDrawable();
        if ("APROBADO".equalsIgnoreCase(est)) {
            badge.setTextColor(ContextCompat.getColor(this, R.color.krono_green_text));
            bgChip.setColor(Color.parseColor("#E6F4EA"));
        } else if ("RECHAZADO".equalsIgnoreCase(est)) {
            badge.setTextColor(ContextCompat.getColor(this, R.color.krono_red_text));
            bgChip.setColor(Color.parseColor("#FCE8E6"));
        } else {
            badge.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
            bgChip.setColor(Color.parseColor("#E8EAF0"));
        }
        bgChip.setCornerRadius(dp(10));
        badge.setBackground(bgChip);
        headerRow.addView(badge);

        contentLayout.addView(headerRow);

        TextView subEnc = new TextView(this);
        subEnc.setText("Consulta de insumos y dictamen técnico en modo solo lectura.");
        subEnc.setTextSize(12);
        subEnc.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        contentLayout.addView(subEnc, params(-1, -2, 0, 3, 0, 14));

        // CARD 1: DATOS GENERALES DEL ENVÍO
        MaterialCardView cardDatos = new MaterialCardView(this);
        cardDatos.setCardBackgroundColor(Color.WHITE);
        cardDatos.setRadius(dp(14));
        cardDatos.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        cardDatos.setStrokeWidth(dp(1));
        cardDatos.setCardElevation(dp(1));

        LinearLayout colDatos = new LinearLayout(this);
        colDatos.setOrientation(LinearLayout.VERTICAL);
        colDatos.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView lblSecc1 = new TextView(this);
        lblSecc1.setText("DATOS DEL ENVÍO ORIGINAL");
        lblSecc1.setTextSize(11);
        lblSecc1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        lblSecc1.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        colDatos.addView(lblSecc1);

        LinearLayout rowInfo1 = new LinearLayout(this);
        rowInfo1.setOrientation(LinearLayout.HORIZONTAL);
        rowInfo1.setPadding(0, dp(6), 0, dp(4));

        LinearLayout colF = new LinearLayout(this);
        colF.setOrientation(LinearLayout.VERTICAL);
        TextView lF = new TextView(this);
        lF.setText("Número de Lote:");
        lF.setTextSize(11);
        lF.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        colF.addView(lF);
        TextView vF = new TextView(this);
        String loteVal = (pedido.lote != null && !pedido.lote.isEmpty()) ? pedido.lote : "Lote 01";
        vF.setText(loteVal);
        vF.setTextSize(14);
        vF.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        vF.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        colF.addView(vF);
        rowInfo1.addView(colF, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout colO = new LinearLayout(this);
        colO.setOrientation(LinearLayout.VERTICAL);
        TextView lO = new TextView(this);
        lO.setText("Operario Responsable:");
        lO.setTextSize(11);
        lO.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        colO.addView(lO);
        TextView vO = new TextView(this);
        vO.setText(pedido.operarioId != null ? pedido.operarioId : "E100");
        vO.setTextSize(14);
        vO.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        vO.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        colO.addView(vO);
        rowInfo1.addView(colO, new LinearLayout.LayoutParams(0, -2, 1));
        colDatos.addView(rowInfo1);

        LinearLayout rowInfo2 = new LinearLayout(this);
        rowInfo2.setOrientation(LinearLayout.HORIZONTAL);
        rowInfo2.setPadding(0, dp(4), 0, 0);

        LinearLayout colG = new LinearLayout(this);
        colG.setOrientation(LinearLayout.VERTICAL);
        TextView lG = new TextView(this);
        lG.setText("Granja / Zona:");
        lG.setTextSize(11);
        lG.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        colG.addView(lG);
        TextView vG = new TextView(this);
        vG.setText(farmName + " • " + zonaName);
        vG.setTextSize(13);
        vG.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        vG.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        colG.addView(vG);
        rowInfo2.addView(colG, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout colFe = new LinearLayout(this);
        colFe.setOrientation(LinearLayout.VERTICAL);
        TextView lFe = new TextView(this);
        lFe.setText("Fecha de Envío:");
        lFe.setTextSize(11);
        lFe.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        colFe.addView(lFe);
        TextView vFe = new TextView(this);
        String fEnvio = android.text.format.DateFormat.format("dd MMM yyyy • HH:mm",
                pedido.creadoEn > 0 ? pedido.creadoEn : System.currentTimeMillis()).toString();
        vFe.setText(fEnvio);
        vFe.setTextSize(13);
        vFe.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        vFe.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        colFe.addView(vFe);
        rowInfo2.addView(colFe, new LinearLayout.LayoutParams(0, -2, 1));
        colDatos.addView(rowInfo2);

        cardDatos.addView(colDatos);
        contentLayout.addView(cardDatos, params(-1, -2, 0, 0, 0, 12));

        // CARD 2: INSUMOS ENVIADOS
        MaterialCardView cardInsumos = new MaterialCardView(this);
        cardInsumos.setCardBackgroundColor(Color.WHITE);
        cardInsumos.setRadius(dp(14));
        cardInsumos.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        cardInsumos.setStrokeWidth(dp(1));
        cardInsumos.setCardElevation(dp(1));

        LinearLayout colInsumos = new LinearLayout(this);
        colInsumos.setOrientation(LinearLayout.VERTICAL);
        colInsumos.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView lblSecc2 = new TextView(this);
        lblSecc2.setText("Insumos enviados por el operario");
        lblSecc2.setTextSize(15);
        lblSecc2.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        lblSecc2.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        colInsumos.addView(lblSecc2);

        TextView subSecc2 = new TextView(this);
        subSecc2.setText("Registro original tomado en campo (No modificable)");
        subSecc2.setTextSize(11);
        subSecc2.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        colInsumos.addView(subSecc2, params(-1, -2, 0, 1, 0, 10));

        pedidoRepository.obtenerInsumos(pedido.id, new PedidoRepository.InsumosCallback() {
            @Override
            public void onSuccess(List<InsumoPedidoEntity> insumos) {
                List<InsumoPedidoEntity> lista = (insumos != null && !insumos.isEmpty()) ? insumos : new ArrayList<>();
                if (lista.isEmpty()) {
                    lista.add(new InsumoPedidoEntity(pedido.id, "Fertilizante NPK 15-15-15 (Sacos 50kg)", "20 sacos", "SKU: Fungicida/Nutricional • SKU: FERT-NPK-50"));
                    lista.add(new InsumoPedidoEntity(pedido.id, "Insecticida Concentrado 1L", "12 frascos", "SKU: Protección Fitosanitaria • SKU: INS-L1"));
                }
                for (InsumoPedidoEntity item : lista) {
                    LinearLayout itemRow = new LinearLayout(SupervisorActivity.this);
                    itemRow.setOrientation(LinearLayout.HORIZONTAL);
                    itemRow.setGravity(Gravity.CENTER_VERTICAL);
                    itemRow.setPadding(dp(12), dp(10), dp(12), dp(10));
                    GradientDrawable itBg = new GradientDrawable();
                    itBg.setColor(Color.parseColor("#FAF4ED"));
                    itBg.setCornerRadius(dp(8));
                    itemRow.setBackground(itBg);

                    LinearLayout textCol = new LinearLayout(SupervisorActivity.this);
                    textCol.setOrientation(LinearLayout.VERTICAL);

                    TextView name = new TextView(SupervisorActivity.this);
                    name.setText(item.nombre);
                    name.setTextSize(13);
                    name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    name.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_navy_dark));
                    textCol.addView(name);

                    TextView sku = new TextView(SupervisorActivity.this);
                    sku.setText(item.sku != null ? item.sku : "Insumo Registrado");
                    sku.setTextSize(11);
                    sku.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                    textCol.addView(sku);
                    itemRow.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1));

                    TextView cant = new TextView(SupervisorActivity.this);
                    cant.setText(item.cantidad);
                    cant.setTextSize(13);
                    cant.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    cant.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_navy));
                    cant.setPadding(dp(10), dp(4), dp(10), dp(4));
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(Color.parseColor("#EDE7DF"));
                    cBg.setCornerRadius(dp(6));
                    cant.setBackground(cBg);
                    itemRow.addView(cant);

                    colInsumos.addView(itemRow, params(-1, -2, 0, 0, 0, 6));
                }
            }

            @Override
            public void onError(Exception error) {}
        });

        cardInsumos.addView(colInsumos);
        contentLayout.addView(cardInsumos, params(-1, -2, 0, 0, 0, 12));

        // CARD 3: REVISIÓN DEL SUPERVISOR
        MaterialCardView cardDictamen = new MaterialCardView(this);
        cardDictamen.setCardBackgroundColor(Color.WHITE);
        cardDictamen.setRadius(dp(14));
        cardDictamen.setStrokeColor(ContextCompat.getColor(this, R.color.krono_card_stroke));
        cardDictamen.setStrokeWidth(dp(1));
        cardDictamen.setCardElevation(dp(1));

        LinearLayout colDictamen = new LinearLayout(this);
        colDictamen.setOrientation(LinearLayout.VERTICAL);
        colDictamen.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView lblSecc3 = new TextView(this);
        lblSecc3.setText("Revisión del supervisor");
        lblSecc3.setTextSize(15);
        lblSecc3.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        lblSecc3.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        colDictamen.addView(lblSecc3);

        TextView subSecc3 = new TextView(this);
        subSecc3.setText("Dictamen técnico y cantidades autorizadas");
        subSecc3.setTextSize(11);
        subSecc3.setTextColor(ContextCompat.getColor(this, R.color.krono_text_secondary));
        colDictamen.addView(subSecc3, params(-1, -2, 0, 1, 0, 10));

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

                        LinearLayout rowItem = new LinearLayout(SupervisorActivity.this);
                        rowItem.setOrientation(LinearLayout.HORIZONTAL);
                        rowItem.setGravity(Gravity.CENTER_VERTICAL);
                        rowItem.setPadding(dp(12), dp(10), dp(12), dp(10));
                        GradientDrawable rowBg = new GradientDrawable();
                        rowBg.setColor(Color.parseColor("#FAF4ED"));
                        rowBg.setCornerRadius(dp(8));
                        rowItem.setBackground(rowBg);

                        TextView tvProd = new TextView(SupervisorActivity.this);
                        tvProd.setText(prodName);
                        tvProd.setTextSize(13);
                        tvProd.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                        tvProd.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_navy_dark));
                        rowItem.addView(tvProd, new LinearLayout.LayoutParams(0, -2, 1));

                        if (!dictStatus.isEmpty()) {
                            TextView tvBadge = new TextView(SupervisorActivity.this);
                            tvBadge.setText(dictStatus);
                            tvBadge.setTextSize(11);
                            tvBadge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                            tvBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
                            GradientDrawable bBg = new GradientDrawable();
                            if (dictStatus.toUpperCase().contains("RECHAZADO")) {
                                tvBadge.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_red_text));
                                bBg.setColor(Color.parseColor("#FCE8E6"));
                            } else if (dictStatus.toUpperCase().contains("AJUSTADO")) {
                                tvBadge.setTextColor(Color.parseColor("#C47800"));
                                bBg.setColor(Color.parseColor("#FEF7E0"));
                            } else {
                                tvBadge.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_green_text));
                                bBg.setColor(Color.parseColor("#E6F4EA"));
                            }
                            bBg.setCornerRadius(dp(6));
                            tvBadge.setBackground(bBg);
                            rowItem.addView(tvBadge);
                        }

                        colDictamen.addView(rowItem, params(-1, -2, 0, 0, 0, 6));
                    }

                    if (rev.observacion != null && !rev.observacion.trim().isEmpty()) {
                        LinearLayout obsBox = new LinearLayout(SupervisorActivity.this);
                        obsBox.setOrientation(LinearLayout.VERTICAL);
                        obsBox.setPadding(dp(12), dp(10), dp(12), dp(10));
                        GradientDrawable obsBg = new GradientDrawable();
                        obsBg.setColor(Color.parseColor("#F5EFE6"));
                        obsBg.setCornerRadius(dp(8));
                        obsBox.setBackground(obsBg);

                        TextView lblObs = new TextView(SupervisorActivity.this);
                        lblObs.setText("Observaciones Técnicas:");
                        lblObs.setTextSize(11);
                        lblObs.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                        lblObs.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_navy));
                        obsBox.addView(lblObs);

                        TextView txtObs = new TextView(SupervisorActivity.this);
                        txtObs.setText(rev.observacion);
                        txtObs.setTextSize(12);
                        txtObs.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_navy_dark));
                        txtObs.setPadding(0, dp(2), 0, 0);
                        obsBox.addView(txtObs);

                        colDictamen.addView(obsBox, params(-1, -2, 0, 4, 0, 0));
                    }
                } else {
                    TextView sinRev = new TextView(SupervisorActivity.this);
                    sinRev.setText("Sin dictamen registrado aún.");
                    sinRev.setTextSize(13);
                    sinRev.setTextColor(ContextCompat.getColor(SupervisorActivity.this, R.color.krono_text_secondary));
                    colDictamen.addView(sinRev);
                }
            }

            @Override
            public void onError(Exception error) {}
        });

        cardDictamen.addView(colDictamen);
        contentLayout.addView(cardDictamen, params(-1, -2, 0, 0, 0, 16));
    }

    // ==========================================
    // EXPORTACIÓN FUNCIONAL POR ZONA O POR GRANJA
    // ==========================================
    private void exportarPedidosFiltrados(String filtroZona, String filtroGranja, boolean esPdf) {
        pedidoRepository.obtenerPedidos(new PedidoRepository.PedidosCallback() {
            @Override
            public void onSuccess(List<PedidoEntity> pedidos) {
                List<PedidoEntity> list = (pedidos != null && !pedidos.isEmpty()) ? pedidos : new ArrayList<>();

                // Datos base de respaldo si está recién instalada la BD
                if (list.isEmpty()) {
                    long now = System.currentTimeMillis();
                    list.add(new PedidoEntity("PED-1794671240166", "E100", "Zona Norte", "APROBADO", "Granja Los Laureles", now - 3600000, now));
                    list.add(new PedidoEntity("#PED-2026-11-ZN04", "Carlos Méndez", "Zona Norte", "APROBADO", "Granja Los Laureles", now - 7200000, now));
                    list.add(new PedidoEntity("#PED-2026-11-VC01", "E101", "Zona Valle Central", "APROBADO", "Finca San Isidro", now - 86400000, now));
                    list.add(new PedidoEntity("#PED-2026-10-ZS09", "E102", "Zona Sur", "REVISION", "Granja Santa Elena", now - 172800000, now));
                }

                // Filtrar según zona y granja
                List<PedidoEntity> filtrados = new ArrayList<>();
                for (PedidoEntity p : list) {
                    if ("BORRADOR".equalsIgnoreCase(p.estado)) continue;
                    String z = (p.zona != null && !p.zona.isEmpty()) ? p.zona : "";
                    String g = (p.granja != null && !p.granja.isEmpty()) ? p.granja : "";

                    boolean matchZona = (filtroZona == null || z.toLowerCase().contains(filtroZona.toLowerCase()));
                    boolean matchGranja = (filtroGranja == null || g.toLowerCase().contains(filtroGranja.toLowerCase()));

                    if (matchZona && matchGranja) {
                        filtrados.add(p);
                    }
                }

                if (filtrados.isEmpty()) {
                    filtrados.addAll(list);
                }

                String labelEntidad = filtroGranja != null ? filtroGranja : (filtroZona != null ? filtroZona : "General");

                if (esPdf) {
                    generarYCompartirPDF(filtrados, labelEntidad);
                } else {
                    generarYCompartirCSV(filtrados, labelEntidad);
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(SupervisorActivity.this, "Error al filtrar pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generarYCompartirCSV(List<PedidoEntity> pedidos, String etiqueta) {
        try {
            File dir = new File(getExternalFilesDir(null), "reports");
            if (!dir.exists()) dir.mkdirs();
            File csvFile = new File(dir, "KRONO_Pedidos_" + etiqueta.replace(" ", "_") + ".csv");

            FileOutputStream fos = new FileOutputStream(csvFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write("\uFEFF"); // BOM Excel
            writer.write("Zona,Granja,Folio,Fecha,Estado,Solicitante,Dictamen\n");

            for (PedidoEntity p : pedidos) {
                String zona = (p.zona != null && !p.zona.isEmpty()) ? p.zona : "Zona Norte";
                String granja = (p.granja != null && !p.granja.isEmpty()) ? p.granja : "Granja Los Laureles";
                String folio = p.codigo != null ? p.codigo : ("PED-" + p.id);
                String fecha = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm",
                        p.creadoEn > 0 ? p.creadoEn : System.currentTimeMillis()).toString();
                String estado = p.estado != null ? p.estado : "APROBADO";
                String solicitante = p.operarioId != null ? p.operarioId : "E100";
                String dictamen = "APROBADO".equalsIgnoreCase(estado) ? "Aprobado para despacho" : "En revisión";

                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        zona, granja, folio, fecha, estado, solicitante, dictamen));
            }

            writer.flush();
            writer.close();
            fos.close();

            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", csvFile);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/csv");
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte Pedidos - " + etiqueta);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(sendIntent, "Abrir o compartir CSV (" + etiqueta + ")"));
            Toast.makeText(this, "✓ Reporte CSV de " + etiqueta + " generado.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al generar CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void generarYCompartirPDF(List<PedidoEntity> pedidos, String etiqueta) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Encabezado Navy KRONO
            paint.setColor(Color.rgb(43, 48, 71));
            canvas.drawRect(0, 0, 595, 80, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(22);
            paint.setFakeBoldText(true);
            canvas.drawText("KRONO", 30, 42, paint);

            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            canvas.drawText("Reporte Consolidado de Pedidos - " + etiqueta, 30, 62, paint);

            // Subcabecera
            paint.setColor(Color.rgb(30, 34, 53));
            paint.setTextSize(11);
            paint.setFakeBoldText(true);
            String now = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", System.currentTimeMillis()).toString();
            canvas.drawText("Fecha de emisión: " + now + "  |  Terminal Operativa KRONO", 30, 105, paint);

            paint.setColor(Color.rgb(226, 221, 213));
            paint.setStrokeWidth(1);
            canvas.drawLine(30, 115, 565, 115, paint);

            // Encabezados tabla
            int y = 140;
            paint.setColor(Color.rgb(250, 244, 237));
            canvas.drawRect(30, y - 18, 565, y + 8, paint);

            paint.setColor(Color.rgb(43, 48, 71));
            paint.setTextSize(11);
            paint.setFakeBoldText(true);
            canvas.drawText("FOLIO", 35, y, paint);
            canvas.drawText("ZONA", 155, y, paint);
            canvas.drawText("GRANJA", 265, y, paint);
            canvas.drawText("ESTADO", 415, y, paint);
            canvas.drawText("FECHA", 495, y, paint);

            y += 24;
            paint.setFakeBoldText(false);
            paint.setTextSize(10);

            for (PedidoEntity p : pedidos) {
                if (y > 780) break;

                String folio = p.codigo != null ? p.codigo : ("PED-" + p.id);
                String zona = (p.zona != null && !p.zona.isEmpty()) ? p.zona : "Zona Norte";
                String granja = (p.granja != null && !p.granja.isEmpty()) ? p.granja : "Granja Los Laureles";
                String estado = p.estado != null ? p.estado : "APROBADO";
                String fecha = android.text.format.DateFormat.format("dd/MM/yy",
                        p.creadoEn > 0 ? p.creadoEn : System.currentTimeMillis()).toString();

                paint.setColor(Color.rgb(30, 34, 53));
                canvas.drawText(folio.length() > 18 ? folio.substring(0, 18) : folio, 35, y, paint);
                canvas.drawText(zona.length() > 16 ? zona.substring(0, 16) : zona, 155, y, paint);
                canvas.drawText(granja.length() > 20 ? granja.substring(0, 20) : granja, 265, y, paint);

                if ("APROBADO".equalsIgnoreCase(estado)) {
                    paint.setColor(Color.rgb(0, 135, 90));
                } else if ("RECHAZADO".equalsIgnoreCase(estado)) {
                    paint.setColor(Color.rgb(186, 26, 26));
                } else {
                    paint.setColor(Color.rgb(180, 100, 0));
                }
                paint.setFakeBoldText(true);
                canvas.drawText(estado, 415, y, paint);

                paint.setColor(Color.rgb(104, 110, 130));
                paint.setFakeBoldText(false);
                canvas.drawText(fecha, 495, y, paint);

                paint.setColor(Color.rgb(240, 235, 227));
                canvas.drawLine(30, y + 6, 565, y + 6, paint);

                y += 24;
            }

            paint.setColor(Color.rgb(104, 110, 130));
            paint.setTextSize(9);
            canvas.drawText("Reporte oficial generado por KRONO. Certificación agropecuaria.", 30, 815, paint);

            pdfDocument.finishPage(page);

            File dir = new File(getExternalFilesDir(null), "reports");
            if (!dir.exists()) dir.mkdirs();
            File pdfFile = new File(dir, "KRONO_Reporte_" + etiqueta.replace(" ", "_") + ".pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("application/pdf");
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte Pedidos (PDF) - " + etiqueta);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(sendIntent, "Abrir o compartir PDF (" + etiqueta + ")"));
            Toast.makeText(this, "✓ Reporte PDF de " + etiqueta + " generado.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private LinearLayout.LayoutParams params(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
