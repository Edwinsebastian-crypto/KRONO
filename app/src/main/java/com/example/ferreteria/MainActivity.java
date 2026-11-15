package com.example.ferreteria;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.example.ferreteria.data.AuthRepository;
import com.example.ferreteria.data.entity.UserEntity;

public class MainActivity extends AppCompatActivity {

    public enum UserRole {
        OPERARIO("Operario de Granja (Campo)"),
        SUPERVISOR("Supervisor / Revisor (Control)"),
        ADMINISTRADOR("Administrador General (Global)");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private UserRole selectedRole = UserRole.SUPERVISOR;

    private ScrollView scrollViewMain;
    private int lastImeHeight = 0;

    private MaterialCardView cardRoleOperario;
    private MaterialCardView cardRoleSupervisor;
    private MaterialCardView cardRoleAdmin;

    private ImageView imgRadioOperario;
    private ImageView imgRadioSupervisor;
    private ImageView imgRadioAdmin;

    private EditText etUserId;
    private EditText etPassword;
    private AuthRepository authRepository;
    private boolean databaseReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initEdgeToEdge(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupWindowInsets();
        setupRoleSelection();
        setupFocusAutoScroll();
        setupClickListeners();
        authRepository = new AuthRepository(this);
        authRepository.inicializarDatos(new AuthRepository.ResultCallback<Void>() {
            @Override public void onSuccess(Void ignored) { databaseReady = true; }
            @Override public void onError(Exception error) { Toast.makeText(MainActivity.this, "No se pudo preparar la base de datos: " + error.getMessage(), Toast.LENGTH_LONG).show(); }
        });
    }

    private void initViews() {
        scrollViewMain = findViewById(R.id.scrollViewMain);

        cardRoleOperario = findViewById(R.id.cardRoleOperario);
        cardRoleSupervisor = findViewById(R.id.cardRoleSupervisor);
        cardRoleAdmin = findViewById(R.id.cardRoleAdmin);

        imgRadioOperario = findViewById(R.id.imgRadioOperario);
        imgRadioSupervisor = findViewById(R.id.imgRadioSupervisor);
        imgRadioAdmin = findViewById(R.id.imgRadioAdmin);

        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
    }

    private void setupWindowInsets() {
        StatusBarUtils.applyStatusBarNavyHeader(this);

        ViewCompat.setOnApplyWindowInsetsListener(scrollViewMain, (v, insets) -> {
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            lastImeHeight = ime.bottom;

            int bottomInset = (ime.bottom > 0) ? 0 : navBars.bottom;

            v.setPadding(
                    0,
                    0,
                    0,
                    bottomInset
            );

            // If soft keyboard is visible, position the focused view right above it
            if (ime.bottom > 0) {
                scrollViewMain.postDelayed(() -> {
                    View currentFocused = getCurrentFocus();
                    if (currentFocused != null) {
                        scrollToFocusedView(currentFocused);
                    }
                }, 100);
            }

            return insets;
        });
    }

    private void setupFocusAutoScroll() {
        View.OnFocusChangeListener focusChangeListener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollViewMain.postDelayed(() -> scrollToFocusedView(v), 100);
            }
        };

        View.OnClickListener clickListener = v ->
                scrollViewMain.postDelayed(() -> scrollToFocusedView(v), 100);

        View[] inputs = new View[]{etUserId, etPassword};
        for (View input : inputs) {
            input.setOnFocusChangeListener(focusChangeListener);
            input.setOnClickListener(clickListener);
        }
    }

    private View resolveTargetContainer(View view) {
        if (view == null) return null;
        int viewId = view.getId();
        if (viewId == R.id.etUserId) {
            View layoutUserId = findViewById(R.id.layoutUserId);
            return layoutUserId != null ? layoutUserId : view;
        } else if (viewId == R.id.etPassword) {
            View layoutPassword = findViewById(R.id.layoutPassword);
            return layoutPassword != null ? layoutPassword : view;
        }
        return view;
    }

    private void scrollToFocusedView(View view) {
        if (scrollViewMain == null || view == null) return;
        final View finalTarget = resolveTargetContainer(view);
        scrollViewMain.post(() -> {
            int extra = (int) (24 * getResources().getDisplayMetrics().density);
            finalTarget.requestRectangleOnScreen(
                    new android.graphics.Rect(0, 0, finalTarget.getWidth(), finalTarget.getHeight() + extra),
                    true
            );
        });
    }

    private void setupRoleSelection() {
        updateRoleUI(UserRole.OPERARIO);

        cardRoleOperario.setOnClickListener(v -> updateRoleUI(UserRole.OPERARIO));
        cardRoleSupervisor.setOnClickListener(v -> updateRoleUI(UserRole.SUPERVISOR));
        cardRoleAdmin.setOnClickListener(v -> updateRoleUI(UserRole.ADMINISTRADOR));
    }

    private void updateRoleUI(UserRole role) {
        selectedRole = role;

        int selectedStrokeColor = ContextCompat.getColor(this, R.color.krono_card_stroke_selected);
        int unselectedStrokeColor = ContextCompat.getColor(this, R.color.krono_card_stroke);

        int selectedStrokeWidth = (int) (2 * getResources().getDisplayMetrics().density);
        int unselectedStrokeWidth = (int) (1 * getResources().getDisplayMetrics().density);

        // Operario Card
        boolean isOperario = (role == UserRole.OPERARIO);
        cardRoleOperario.setStrokeColor(isOperario ? selectedStrokeColor : unselectedStrokeColor);
        cardRoleOperario.setStrokeWidth(isOperario ? selectedStrokeWidth : unselectedStrokeWidth);
        imgRadioOperario.setImageResource(isOperario ? R.drawable.ic_radio_checked : R.drawable.ic_radio_unchecked);

        // Supervisor Card
        boolean isSupervisor = (role == UserRole.SUPERVISOR);
        cardRoleSupervisor.setStrokeColor(isSupervisor ? selectedStrokeColor : unselectedStrokeColor);
        cardRoleSupervisor.setStrokeWidth(isSupervisor ? selectedStrokeWidth : unselectedStrokeWidth);
        imgRadioSupervisor.setImageResource(isSupervisor ? R.drawable.ic_radio_checked : R.drawable.ic_radio_unchecked);

        // Admin Card
        boolean isAdmin = (role == UserRole.ADMINISTRADOR);
        cardRoleAdmin.setStrokeColor(isAdmin ? selectedStrokeColor : unselectedStrokeColor);
        cardRoleAdmin.setStrokeWidth(isAdmin ? selectedStrokeWidth : unselectedStrokeWidth);
        imgRadioAdmin.setImageResource(isAdmin ? R.drawable.ic_radio_checked : R.drawable.ic_radio_unchecked);

        if (role == UserRole.SUPERVISOR) {
            if (etUserId != null && (etUserId.getText().toString().equals("E100") || etUserId.getText().toString().equals("A100") || etUserId.getText().toString().isEmpty())) {
                etUserId.setText("S100");
            }
        } else if (role == UserRole.ADMINISTRADOR) {
            if (etUserId != null && (etUserId.getText().toString().equals("E100") || etUserId.getText().toString().equals("S100") || etUserId.getText().toString().isEmpty())) {
                etUserId.setText("A100");
            }
        } else if (role == UserRole.OPERARIO) {
            if (etUserId != null && (etUserId.getText().toString().equals("S100") || etUserId.getText().toString().equals("A100") || etUserId.getText().toString().isEmpty())) {
                etUserId.setText("E100");
            }
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String userId = etUserId.getText() == null ? "" : etUserId.getText().toString().trim();
            String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
            if (userId.isEmpty()) { etUserId.setError("Ingresa tu código"); return; }
            if (password.isEmpty()) { etPassword.setError("Ingresa tu contraseña"); return; }
            if (!databaseReady) { Toast.makeText(this, "Preparando la base de datos. Intenta de nuevo en unos segundos.", Toast.LENGTH_SHORT).show(); return; }
            final UserRole requestedRole = selectedRole;
            String role = requestedRole.name();
            authRepository.autenticar(userId, password, role, new AuthRepository.ResultCallback<UserEntity>() {
                @Override public void onSuccess(UserEntity user) {
                    if (user == null) { etPassword.setError("Credenciales incorrectas o rol no autorizado"); return; }
                    Class<?> destination = requestedRole == UserRole.OPERARIO ? OperarioActivity.class :
                            requestedRole == UserRole.SUPERVISOR ? SupervisorActivity.class : AdminActivity.class;
                    Intent intent = new Intent(MainActivity.this, destination);
                    intent.putExtra("user_id", user.userId);
                    intent.putExtra("farm_name", user.nombre);
                    intent.putExtra("user_name", user.nombre);
                    startActivity(intent);
                }
                @Override public void onError(Exception error) { Toast.makeText(MainActivity.this, "Error al iniciar sesión: " + error.getMessage(), Toast.LENGTH_LONG).show(); }
            });
        });
    }
}