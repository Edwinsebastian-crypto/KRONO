package com.example.ferreteria;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SupervisorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_supervisor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back to Login
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());

        // Aprobar y Despachar
        findViewById(R.id.btnAprobarDespachar).setOnClickListener(v ->
                Toast.makeText(this, "Requerimiento #REQ-8829 Aprobado y enviado a despacho.", Toast.LENGTH_LONG).show()
        );

        // Pedir Aclaración
        findViewById(R.id.btnPedirAclaracion).setOnClickListener(v ->
                Toast.makeText(this, "Solicitud de aclaración enviada a Carlos Méndez (Operario).", Toast.LENGTH_SHORT).show()
        );

        // Tabs
        findViewById(R.id.tabPedido).setOnClickListener(v -> {
            Intent intent = new Intent(this, OperarioActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tabRevisiones).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Revisiones de Pedidos - Zona Norte", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.tabVentanas).setOnClickListener(v ->
                Toast.makeText(this, "Cargando gestión de ventanas de entrega por zona...", Toast.LENGTH_SHORT).show()
        );
    }
}