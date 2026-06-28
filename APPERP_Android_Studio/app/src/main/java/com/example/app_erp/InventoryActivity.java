// InventoryActivity.java - COMPLETO CON BUSCADOR
package com.example.app_erp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private static final String TAG = "InventoryActivity";
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private ExecutorService executor;

    private TextInputEditText searchInput;
    private ImageButton btnClearSearch;
    private TextView resultCountText;
    private View emptyStateLayout;

    private static final String SUPABASE_URL = "https://ukcmmkpirlyzizvroswc.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVrY21ta3Bpcmx5eml6dnJvc3djIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQxNTI0NDUsImV4cCI6MjA2OTcyODQ0NX0.Qm9BFV20adYYaMrlgr0g77x26ROjl-P7rMjchZ_59fU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        loadProducts();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchInput = findViewById(R.id.searchInput);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        resultCountText = findViewById(R.id.resultCountText);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        executor = Executors.newSingleThreadExecutor();
        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this, filteredProductList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.clearFocus();
        });
    }

    private void filterProducts(String query) {
        filteredProductList.clear();

        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery) ||
                        product.getCode().toLowerCase().contains(lowerCaseQuery) ||
                        product.getCategory().toLowerCase().contains(lowerCaseQuery) ||
                        product.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProductList.add(product);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateResultCount();

        if (filteredProductList.isEmpty() && !productList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateResultCount() {
        int count = filteredProductList.size();
        String text = count == 1 ? count + " producto" : count + " productos";
        resultCountText.setText(text);
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto?select=*&order=prod_activo.desc,prod_nombre.asc");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Load products response: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONArray jsonArray = new JSONArray(response.toString());
                productList.clear();
                filteredProductList.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Product product = new Product(
                            obj.getInt("prod_id"),
                            obj.getString("prod_codigo"),
                            obj.getString("prod_nombre"),
                            obj.getString("prod_descripcion"),
                            obj.getDouble("prod_precio"),
                            obj.getInt("prod_stock"),
                            obj.getString("prod_categoria"),
                            obj.getBoolean("prod_activo")
                    );
                    productList.add(product);
                    filteredProductList.add(product);
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    updateResultCount();

                    if (productList.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error loading products: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onEdit(Product product) {
        showEditDialog(product);
    }

    @Override
    public void onDelete(Product product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Deseas eliminar " + product.getName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onReactivate(Product product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reactivar Producto")
                .setMessage("¿Deseas reactivar " + product.getName() + "?")
                .setPositiveButton("Reactivar", (dialog, which) -> reactivateProduct(product))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void reactivateProduct(Product product) {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto?prod_id=eq." + product.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject data = new JSONObject();
                data.put("prod_activo", true);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (responseCode >= 200 && responseCode < 300) {
                        Toast.makeText(this, "✓ Producto reactivado", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(this, "Error: Código " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showEditDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);

        TextInputEditText codeInput = dialogView.findViewById(R.id.codeInput);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText descInput = dialogView.findViewById(R.id.descInput);
        TextInputEditText priceInput = dialogView.findViewById(R.id.priceInput);
        TextInputEditText stockInput = dialogView.findViewById(R.id.stockInput);
        TextInputEditText categoryInput = dialogView.findViewById(R.id.categoryInput);

        codeInput.setText(product.getCode());
        nameInput.setText(product.getName());
        descInput.setText(product.getDescription());
        priceInput.setText(String.valueOf(product.getPrice()));
        stockInput.setText(String.valueOf(product.getStock()));
        categoryInput.setText(product.getCategory());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Editar Producto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        String code = codeInput.getText().toString();
                        String name = nameInput.getText().toString();
                        String desc = descInput.getText().toString();
                        double price = Double.parseDouble(priceInput.getText().toString());
                        int stock = Integer.parseInt(stockInput.getText().toString());
                        String category = categoryInput.getText().toString();

                        updateProduct(product.getId(), code, name, desc, price, stock, category);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error en los datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateProduct(int id, String code, String name, String desc, double price, int stock, String category) {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto?prod_id=eq." + id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject data = new JSONObject();
                data.put("prod_codigo", code);
                data.put("prod_nombre", name);
                data.put("prod_descripcion", desc);
                data.put("prod_precio", price);
                data.put("prod_stock", stock);
                data.put("prod_categoria", category);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (responseCode >= 200 && responseCode < 300) {
                        Toast.makeText(this, "✓ Producto actualizado", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(this, "Error: Código " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteProduct(Product product) {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto?prod_id=eq." + product.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject data = new JSONObject();
                data.put("prod_activo", false);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (responseCode >= 200 && responseCode < 300) {
                        Toast.makeText(this, "✓ Producto eliminado", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(this, "Error: Código " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
