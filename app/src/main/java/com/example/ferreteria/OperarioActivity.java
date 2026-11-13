package com.example.ferreteria;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OperarioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_operario);

        View statusBarBg = findViewById(R.id.statusBarBg);
        if (statusBarBg != null) {
            ViewCompat.setOnApplyWindowInsetsListener(statusBarBg, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp != null && lp.height != systemBars.top) {
                    lp.height = systemBars.top;
                    v.setLayoutParams(lp);
                }
                return insets;
            });
        }

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