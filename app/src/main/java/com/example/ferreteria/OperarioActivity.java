package com.example.ferreteria;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OperarioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_operario);

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

        // Enviar Pedido
        findViewById(R.id.btnEnviarPedido).setOnClickListener(v ->
                Toast.makeText(this, "Pedido #PED-2024-10-ZN04 enviado exitosamente a revisión.", Toast.LENGTH_LONG).show()
        );

        // Agregar producto no listado
        findViewById(R.id.btnAddUnlisted).setOnClickListener(v ->
                Toast.makeText(this, "Apertura de formulario para producto fuera de catálogo.", Toast.LENGTH_SHORT).show()
        );

        // Tabs
        findViewById(R.id.tabPedido).setOnClickListener(v ->
                Toast.makeText(this, "Sección actual: Hoja de Pedido Granja", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.tabRevisiones).setOnClickListener(v ->
                Toast.makeText(this, "Cargando historial de revisiones del pedido...", Toast.LENGTH_SHORT).show()
        );
    }
}