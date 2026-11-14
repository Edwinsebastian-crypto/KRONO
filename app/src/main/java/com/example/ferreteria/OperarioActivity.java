package com.example.ferreteria;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.ferreteria.data.PedidoRepository;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
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

    /**
     * Carga la vista de Revisiones de Hojas de Pedido (activity_ope_revision.xml)
     * con pedidos empaquetados que se despliegan al tocarlos
     */
    public void loadRevisionesView() {
        setContentView(R.layout.activity_ope_revision);
        StatusBarUtils.applyStatusBarNavyHeader(this);

        // Botón Cerrar Sesión (Flecha superior derecha)
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        // Si se envió un pedido nuevo, poblar dinámicamente sus productos en la tarjeta 1
        if (pedidoEnviado) {
            LinearLayout listRev1 = findViewById(R.id.listRevision1Products);
            if (listRev1 != null) {
                listRev1.removeAllViews();
                for (InsumoItem item : insumosList) {
                    addRevisionProductRow(listRev1, item.nombre, item.cantidad);
                }
            }
        }

        // Configurar comportamiento desplegable para cada tarjeta de pedido
        setupExpandableCard(R.id.cardRevision1, R.id.detailsRevision1, R.id.arrowRevision1);
        setupExpandableCard(R.id.cardRevision2, R.id.detailsRevision2, R.id.arrowRevision2);
        setupExpandableCard(R.id.cardRevision3, R.id.detailsRevision3, R.id.arrowRevision3);

        // Tabs de Navegación Inferior
        findViewById(R.id.tabPedido).setOnClickListener(v -> loadPedidoView());

        findViewById(R.id.tabRevisiones).setOnClickListener(v -> mostrarMisPedidos());
    }

    /**
     * Configura el comportamiento de desplegar/plegar cada tarjeta empaquetada
     */
    private void setupExpandableCard(int cardId, int detailsId, int arrowId) {
        View card = findViewById(cardId);
        View details = findViewById(detailsId);
        ImageView arrow = findViewById(arrowId);

        if (card != null && details != null) {
            card.setOnClickListener(v -> {
                boolean isExpanded = (details.getVisibility() == View.VISIBLE);
                details.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                if (arrow != null) {
                    arrow.animate().rotation(isExpanded ? 0f : 180f).setDuration(200).start();
                }
            });
        }
    }

    /**
     * Agrega una fila de producto dentro del detalle desplegado en revisiones
     */
    private void addRevisionProductRow(LinearLayout container, String nombre, String cantidad) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_cierre_box);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = (int) (6 * getResources().getDisplayMetrics().density);
        row.setLayoutParams(lp);

        int pad = (int) (10 * getResources().getDisplayMetrics().density);
        row.setPadding(pad, pad, pad, pad);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombre);
        tvNombre.setTextSize(13f);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNombre.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvNombre.setLayoutParams(nameParams);
        row.addView(tvNombre);

        TextView tvCantidad = new TextView(this);
        tvCantidad.setText(cantidad);
        tvCantidad.setTextSize(12f);
        tvCantidad.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCantidad.setTextColor(ContextCompat.getColor(this, R.color.krono_navy_dark));
        tvCantidad.setBackgroundResource(R.drawable.bg_qty_box);
        int padH = (int) (10 * getResources().getDisplayMetrics().density);
        int padV = (int) (4 * getResources().getDisplayMetrics().density);
        tvCantidad.setPadding(padH, padV, padH, padV);
        row.addView(tvCantidad);

        container.addView(row);
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
