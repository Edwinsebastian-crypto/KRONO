package com.example.ferreteria;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public final class StatusBarUtils {

    private static int sStatusBarHeight = 0;

    private StatusBarUtils() {
    }

    /**
     * Configura EdgeToEdge con barra de estado oscura (iconos blancos/claros)
     * para que coincida exactamente con la cinta azul marino (krono_navy).
     */
    public static void initEdgeToEdge(ComponentActivity activity) {
        EdgeToEdge.enable(activity, SystemBarStyle.dark(Color.TRANSPARENT));
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
    }

    /**
     * Obtiene la altura exacta del status bar del sistema en píxeles.
     */
    public static int getStatusBarHeight(Context context) {
        if (sStatusBarHeight > 0) {
            return sStatusBarHeight;
        }

        // Intento 1: Dimen oficial de recursos de Android
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            sStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }

        // Intento 2: Fallback seguro según densidad de pantalla (~38dp)
        if (sStatusBarHeight <= 0) {
            sStatusBarHeight = (int) (38 * context.getResources().getDisplayMetrics().density);
        }

        return sStatusBarHeight;
    }

    /**
     * Aplica la altura correcta a la vista de la cinta azul (R.id.statusBarBg)
     * de forma síncrona y mediante listener para evitar bugs al cambiar de vistas.
     */
    public static void applyStatusBarNavyHeader(Activity activity) {
        View decorView = activity.getWindow().getDecorView();

        // 1. Intentar leer WindowInsets actuales si la ventana ya está en pantalla
        WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(decorView);
        if (rootInsets != null) {
            Insets systemBars = rootInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (systemBars.top > 0) {
                sStatusBarHeight = systemBars.top;
            }
        }

        int height = getStatusBarHeight(activity);

        // 2. Aplicar inmediatamente al statusBarBg actual
        View statusBarBg = activity.findViewById(R.id.statusBarBg);
        if (statusBarBg != null && height > 0) {
            ViewGroup.LayoutParams lp = statusBarBg.getLayoutParams();
            if (lp != null && lp.height != height) {
                lp.height = height;
                statusBarBg.setLayoutParams(lp);
            }
        }

        // 3. Listener persistente en decorView para capturar cambios de insets
        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (systemBars.top > 0) {
                sStatusBarHeight = systemBars.top;
                View currentSb = activity.findViewById(R.id.statusBarBg);
                if (currentSb != null) {
                    ViewGroup.LayoutParams lp = currentSb.getLayoutParams();
                    if (lp != null && lp.height != sStatusBarHeight) {
                        lp.height = sStatusBarHeight;
                        currentSb.setLayoutParams(lp);
                    }
                }
            }
            return insets;
        });

        ViewCompat.requestApplyInsets(decorView);
    }

    /**
     * Ajusta los insets de navegación y realiza scroll automático suave cuando
     * aparece el teclado virtual para que los campos de texto nunca queden cubiertos,
     * sin añadir doble padding que cause una franja vacía del color de fondo.
     */
    public static void setupIntelligentKeyboard(Activity activity, ScrollView scrollView) {
        if (activity == null || scrollView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Con windowSoftInputMode="adjustResize", Android ya reduce la ventana al abrir el teclado.
            // Si sumamos ime.bottom al padding, se duplica el espacio creando una barra beige vacía.
            // Por lo tanto, solo aplicamos padding de la barra de navegación cuando el teclado está cerrado.
            int bottomPadding = (ime.bottom > 0) ? 0 : navBars.bottom;

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottomPadding
            );

            if (ime.bottom > 0) {
                scrollView.postDelayed(() -> {
                    View currentFocused = activity.getCurrentFocus();
                    if (currentFocused != null) {
                        int extra = (int) (20 * v.getResources().getDisplayMetrics().density);
                        currentFocused.requestRectangleOnScreen(
                                new android.graphics.Rect(0, 0, currentFocused.getWidth(), currentFocused.getHeight() + extra),
                                true
                        );
                    }
                }, 100);
            }

            return insets;
        });
    }

    public static void scrollToFocusedView(ScrollView scrollView, View focusedView, int imeHeight) {
        if (scrollView == null || focusedView == null) return;
        int extra = (int) (20 * scrollView.getResources().getDisplayMetrics().density);
        focusedView.requestRectangleOnScreen(
                new android.graphics.Rect(0, 0, focusedView.getWidth(), focusedView.getHeight() + extra),
                true
        );
    }
}
