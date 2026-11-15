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
     * ocultando la barra inferior mientras se escribe y eliminando la franja beige.
     */
    public static void setupIntelligentKeyboard(Activity activity, ScrollView scrollView) {
        if (activity == null || scrollView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            boolean isKeyboardVisible = ime.bottom > 0;

            // Ocultar barra de navegación inferior cuando el teclado está abierto para no quitar espacio
            View bottomNav = activity.findViewById(R.id.bottomNav);
            if (bottomNav != null) {
                bottomNav.setVisibility(isKeyboardVisible ? View.GONE : View.VISIBLE);
            }

            // Margen inferior limpio: NUNCA sumar ime.bottom (que creaba la franja beige de 1100px)
            float density = v.getResources().getDisplayMetrics().density;
            int bottomPadding = isKeyboardVisible ? (int) (16 * density) : navBars.bottom;

            v.setPadding(
                    v.getPaddingLeft(),
                    0,
                    v.getPaddingRight(),
                    bottomPadding
            );

            if (isKeyboardVisible) {
                scrollView.postDelayed(() -> {
                    View currentFocused = activity.getCurrentFocus();
                    if (currentFocused != null) {
                        scrollToFocusedView(scrollView, currentFocused);
                    }
                }, 100);
            }

            return insets;
        });
    }

    private static View resolveTargetContainer(ScrollView scrollView, View focusedView) {
        if (scrollView == null || focusedView == null) return focusedView;

        View root = scrollView.getRootView();
        int viewId = focusedView.getId();

        // En el login se desplaza el bloque completo para conservar visible
        // tanto la etiqueta como el campo de entrada.
        if (viewId == R.id.etUserId) {
            View container = root.findViewById(R.id.layoutUserId);
            return container != null ? container : focusedView;
        }
        if (viewId == R.id.etPassword) {
            View container = root.findViewById(R.id.layoutPassword);
            return container != null ? container : focusedView;
        }
        return focusedView;
    }

    public static void scrollToFocusedView(ScrollView scrollView, View focusedView) {
        if (scrollView == null || focusedView == null) return;

        final View targetView = resolveTargetContainer(scrollView, focusedView);
        scrollView.post(() -> {
            try {
                android.graphics.Rect rect = new android.graphics.Rect();
                targetView.getDrawingRect(rect);
                scrollView.offsetDescendantRectToMyCoords(targetView, rect);

                int scrollY = scrollView.getScrollY();
                int visibleHeight = scrollView.getHeight()
                        - scrollView.getPaddingTop() - scrollView.getPaddingBottom();
                if (visibleHeight <= 0) return;

                // Recupera el margen de seguridad del login original.
                int margin = (int) (36 * scrollView.getResources().getDisplayMetrics().density);

                if (rect.bottom + margin > scrollY + visibleHeight) {
                    int targetScrollY = (rect.bottom + margin) - visibleHeight;
                    scrollView.smoothScrollTo(0, targetScrollY);
                } else if (rect.top - margin < scrollY) {
                    int targetScrollY = Math.max(0, rect.top - margin);
                    scrollView.smoothScrollTo(0, targetScrollY);
                }
            } catch (Exception ignored) {
                int extra = (int) (24 * scrollView.getResources().getDisplayMetrics().density);
                focusedView.requestRectangleOnScreen(
                        new android.graphics.Rect(0, 0, focusedView.getWidth(), focusedView.getHeight() + extra),
                        true
                );
            }
        });
    }

    public static void attachFocusAutoScroll(ScrollView scrollView, View inputView) {
        if (scrollView == null || inputView == null) return;
        inputView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> scrollToFocusedView(scrollView, v), 100);
            }
        });
        inputView.setOnClickListener(v -> {
            scrollView.postDelayed(() -> scrollToFocusedView(scrollView, v), 100);
        });
    }
}
