package com.example.ferreteria;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

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

        // Send Push Notification
        findViewById(R.id.btnPushNotif).setOnClickListener(v ->
                Toast.makeText(this, "Notificación Push enviada exitosamente a las 20 granjas habilitadas.", Toast.LENGTH_LONG).show()
        );

        // Auditoria & Export
        findViewById(R.id.cardAuditoria).setOnClickListener(v ->
                Toast.makeText(this, "Apertura de panel de Auditoría de Insumos No Catalogados...", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.cardExportar).setOnClickListener(v ->
                Toast.makeText(this, "Generando reporte de cierre mensual en formato PDF/CSV...", Toast.LENGTH_SHORT).show()
        );

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