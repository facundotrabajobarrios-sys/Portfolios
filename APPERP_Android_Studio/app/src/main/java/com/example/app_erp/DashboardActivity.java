// DashboardActivity.java - CON DATOS DINÁMICOS DE LA BASE DE DATOS
package com.example.app_erp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView userNameText, navUserName, navUserEmail;
    private MaterialCardView cardInventory, cardAddProduct, cardReports;
    private View welcomeCard, statsContainer, quickActionsContainer, notificationIcon;

    // TextViews para los datos dinámicos
    private TextView totalProductsText, lowStockCountText, movementsText, reportsText;
    private ProgressBar statsProgressBar;

    private SharedPreferences prefs;
    private String userName, userLastName, userEmail, userRole;
    private ExecutorService executor;

    private static final String SUPABASE_URL = "https://ukcmmkpirlyzizvroswc.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVrY21ta3Bpcmx5eml6dnJvc3djIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQxNTI0NDUsImV4cCI6MjA2OTcyODQ0NX0.Qm9BFV20adYYaMrlgr0g77x26ROjl-P7rMjchZ_59fU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        loadUserData();
        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupCardActions();
        loadDashboardData();
        animateEntrance();
    }

    private void loadUserData() {
        prefs = getSharedPreferences("AppERPPrefs", MODE_PRIVATE);
        userName = prefs.getString("userName", "Usuario");
        userLastName = prefs.getString("userLastName", "");
        userEmail = prefs.getString("userEmail", "email@ejemplo.com");
        userRole = prefs.getString("userRole", "Usuario");
        executor = Executors.newSingleThreadExecutor();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        notificationIcon = findViewById(R.id.notificationIcon);

        userNameText = findViewById(R.id.userNameText);
        welcomeCard = findViewById(R.id.welcomeCard);
        statsContainer = findViewById(R.id.statsContainer);
        quickActionsContainer = findViewById(R.id.quickActionsContainer);

        cardInventory = findViewById(R.id.cardInventory);
        cardAddProduct = findViewById(R.id.cardAddProduct);
        cardReports = findViewById(R.id.cardReports);

        // Inicializar TextViews de estadísticas
        totalProductsText = findViewById(R.id.totalProductsText);
        lowStockCountText = findViewById(R.id.lowStockCountText);
        movementsText = findViewById(R.id.movementsText);
        reportsText = findViewById(R.id.reportsText);

        View headerView = navigationView.getHeaderView(0);
        navUserName = headerView.findViewById(R.id.navUserName);
        navUserEmail = headerView.findViewById(R.id.navUserEmail);

        userNameText.setText(userName + " " + userLastName);
        navUserName.setText(userName + " " + userLastName);
        navUserEmail.setText(userEmail);

        notificationIcon.setOnClickListener(v -> showNotifications());
    }

    private void loadDashboardData() {
        // Mostrar valores iniciales mientras carga
        totalProductsText.setText("...");
        lowStockCountText.setText("...");
        movementsText.setText("...");
        reportsText.setText("...");

        executor.execute(() -> {
            try {
                // Cargar productos
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto?prod_activo=eq.true&select=*");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONArray products = new JSONArray(response.toString());

                int totalProducts = products.length();
                int lowStock = 0;

                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);
                    int stock = product.getInt("prod_stock");
                    if (stock < 10) {
                        lowStock++;
                    }
                }

                int finalLowStock = lowStock;

                // Actualizar UI en el hilo principal
                runOnUiThread(() -> {
                    animateCounterUpdate(totalProductsText, totalProducts);
                    animateCounterUpdate(lowStockCountText, finalLowStock);
                    animateCounterUpdate(movementsText, totalProducts * 2); // Simulado: movimientos = productos * 2
                    animateCounterUpdate(reportsText, totalProducts / 5); // Simulado: reportes
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    totalProductsText.setText("0");
                    lowStockCountText.setText("0");
                    movementsText.setText("0");
                    reportsText.setText("0");
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void animateCounterUpdate(TextView textView, int targetValue) {
        textView.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    textView.setText(String.valueOf(targetValue));
                    textView.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void showNotifications() {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto?prod_activo=eq.true&prod_stock=lt.10&select=prod_nombre,prod_stock");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONArray lowStockProducts = new JSONArray(response.toString());
                StringBuilder message = new StringBuilder();

                if (lowStockProducts.length() == 0) {
                    message.append("✓ No hay productos con bajo stock");
                } else {
                    message.append("⚠️ Productos con bajo stock:\n\n");
                    for (int i = 0; i < Math.min(5, lowStockProducts.length()); i++) {
                        JSONObject product = lowStockProducts.getJSONObject(i);
                        message.append("• ").append(product.getString("prod_nombre"))
                                .append(" (Stock: ").append(product.getInt("prod_stock")).append(")\n");
                    }
                    if (lowStockProducts.length() > 5) {
                        message.append("\n+ ").append(lowStockProducts.length() - 5).append(" más...");
                    }
                }

                String finalMessage = message.toString();
                runOnUiThread(() -> {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("🔔 Notificaciones")
                            .setMessage(finalMessage)
                            .setPositiveButton("Ver Inventario", (dialog, which) -> navigateToInventory())
                            .setNegativeButton("Cerrar", null)
                            .show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al cargar notificaciones", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                View content = findViewById(R.id.mainContent);
                content.setTranslationX(slideOffset * drawerView.getWidth() / 3);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }

    private void setupCardActions() {
        cardInventory.setOnClickListener(v -> {
            animateCardClick(cardInventory);
            navigateToInventory();
        });

        cardAddProduct.setOnClickListener(v -> {
            animateCardClick(cardAddProduct);
            navigateToAddProduct();
        });

        cardReports.setOnClickListener(v -> {
            animateCardClick(cardReports);
            navigateToReports();
        });
    }

    private void navigateToInventory() {
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToAddProduct() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToReports() {
        Intent intent = new Intent(this, ReportsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void animateEntrance() {
        welcomeCard.setAlpha(0f);
        welcomeCard.setTranslationY(-50f);
        welcomeCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        statsContainer.setAlpha(0f);
        statsContainer.setTranslationY(30f);
        statsContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        quickActionsContainer.setAlpha(0f);
        quickActionsContainer.setTranslationY(30f);
        quickActionsContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateCardClick(View card) {
        card.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        card.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setInterpolator(new OvershootInterpolator())
                                .setDuration(200)
                                .start()
                )
                .start();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Ya estamos en home
        } else if (id == R.id.nav_inventory) {
            navigateToInventory();
        } else if (id == R.id.nav_products) {
            navigateToAddProduct();
        } else if (id == R.id.nav_reports) {
            navigateToReports();
        } else if (id == R.id.nav_settings) {
            navigateToSettings();
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que deseas salir?")
                .setPositiveButton("Salir", (dialog, which) -> logout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void logout() {
        View mainContent = findViewById(R.id.mainContent);
        mainContent.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        performLogout();
                    }
                })
                .start();
    }

    private void performLogout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Salir")
                    .setMessage("¿Deseas salir de la aplicación?")
                    .setPositiveButton("Sí", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
