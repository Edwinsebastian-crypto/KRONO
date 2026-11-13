package com.example.ferreteria;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
        EdgeToEdge.enable(this);
        loadZonasView();
    }

    public void loadZonasView() {
        setContentView(R.layout.activity_supervisor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });

        initZonasViews();
        setupZoneSelection();
        setupZonasClickListeners();
    }

    public void loadHojaPedidoView() {
        setContentView(R.layout.activity_sup_hojapedido);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });

        setupHojaPedidoClickListeners();
    }

    public void loadPedidosView() {
        setContentView(R.layout.activity_sup_pedidos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });

        setupPedidosClickListeners();
    }

    public void loadHistorialView() {
        setContentView(R.layout.activity_sup_historial);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });

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

        TextView btnVerPedido2 = findViewById(R.id.btnVerPedido2);
        if (btnVerPedido2 != null) {
            btnVerPedido2.setOnClickListener(v -> loadHojaPedidoView());
        }

        TextView btnVerPedido5 = findViewById(R.id.btnVerPedido5);
        if (btnVerPedido5 != null) {
            btnVerPedido5.setOnClickListener(v -> loadHojaPedidoView());
        }

        findViewById(R.id.tabZonas).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Zonas y Granjas Operativas", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.tabHojaPedido).setOnClickListener(v -> loadHojaPedidoView());
        findViewById(R.id.tabPedidos).setOnClickListener(v -> loadPedidosView());
        findViewById(R.id.tabHistorial).setOnClickListener(v -> loadHistorialView());
    }

    private void setupHojaPedidoClickListeners() {
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        findViewById(R.id.btnAprobarHoja).setOnClickListener(v -> {
            Toast.makeText(this, "Hoja #PED-2024-10-ZN01 aprobada y transferida a Pedidos (Pendiente de Despacho).", Toast.LENGTH_LONG).show();
            loadPedidosView();
        });

        findViewById(R.id.tabZonas).setOnClickListener(v -> loadZonasView());

        findViewById(R.id.tabHojaPedido).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Supervisión Técnica de Hoja de Pedido", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.tabPedidos).setOnClickListener(v -> loadPedidosView());
        findViewById(R.id.tabHistorial).setOnClickListener(v -> loadHistorialView());
    }

    private void setupPedidosClickListeners() {
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        // Ver Hoja buttons
        int[] verHojaIds = new int[]{R.id.btnVerHoja1, R.id.btnVerHoja2, R.id.btnVerHoja3, R.id.btnVerHoja4};
        for (int id : verHojaIds) {
            if (findViewById(id) != null) {
                findViewById(id).setOnClickListener(v -> loadHojaPedidoView());
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
        findViewById(R.id.tabHojaPedido).setOnClickListener(v -> loadHojaPedidoView());

        findViewById(R.id.tabPedidos).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Pedidos Aprobados para Despacho", Toast.LENGTH_SHORT).show()
        );

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
        findViewById(R.id.tabHojaPedido).setOnClickListener(v -> loadHojaPedidoView());
        findViewById(R.id.tabPedidos).setOnClickListener(v -> loadPedidosView());

        findViewById(R.id.tabHistorial).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Ventas e Historial Consolidado", Toast.LENGTH_SHORT).show()
        );
    }
}