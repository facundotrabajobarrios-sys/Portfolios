// AddProductActivity.java - VERSIÓN CORREGIDA
package com.example.app_erp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";
    private MaterialToolbar toolbar;
    private TextInputLayout codeLayout, nameLayout, descLayout, priceLayout, stockLayout, categoryLayout;
    private TextInputEditText codeInput, nameInput, descInput, priceInput, stockInput, categoryInput;
    private MaterialButton btnSave, btnClear;
    private ProgressBar progressBar;

    private ExecutorService executor;

    private static final String SUPABASE_URL = "https://ukcmmkpirlyzizvroswc.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVrY21ta3Bpcmx5eml6dnJvc3djIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQxNTI0NDUsImV4cCI6MjA2OTcyODQ0NX0.Qm9BFV20adYYaMrlgr0g77x26ROjl-P7rMjchZ_59fU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initializeViews();
        setupToolbar();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);

        codeLayout = findViewById(R.id.codeInputLayout);
        nameLayout = findViewById(R.id.nameInputLayout);
        descLayout = findViewById(R.id.descInputLayout);
        priceLayout = findViewById(R.id.priceInputLayout);
        stockLayout = findViewById(R.id.stockInputLayout);
        categoryLayout = findViewById(R.id.categoryInputLayout);

        codeInput = findViewById(R.id.codeInput);
        nameInput = findViewById(R.id.nameInput);
        descInput = findViewById(R.id.descInput);
        priceInput = findViewById(R.id.priceInput);
        stockInput = findViewById(R.id.stockInput);
        categoryInput = findViewById(R.id.categoryInput);

        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        progressBar = findViewById(R.id.progressBar);

        executor = Executors.newSingleThreadExecutor();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveProduct();
            }
        });

        btnClear.setOnClickListener(v -> clearFields());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String code = codeInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String stockStr = stockInput.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            codeLayout.setError("Campo obligatorio");
            isValid = false;
        } else {
            codeLayout.setError(null);
        }

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Campo obligatorio");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        if (TextUtils.isEmpty(priceStr)) {
            priceLayout.setError("Campo obligatorio");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    priceLayout.setError("Debe ser mayor a 0");
                    isValid = false;
                } else {
                    priceLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                priceLayout.setError("Precio inválido");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(stockStr)) {
            stockLayout.setError("Campo obligatorio");
            isValid = false;
        } else {
            try {
                int stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    stockLayout.setError("No puede ser negativo");
                    isValid = false;
                } else {
                    stockLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                stockLayout.setError("Stock inválido");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveProduct() {
        setLoading(true);

        String code = codeInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String desc = descInput.getText().toString().trim();
        double price = Double.parseDouble(priceInput.getText().toString().trim());
        int stock = Integer.parseInt(stockInput.getText().toString().trim());
        String category = categoryInput.getText().toString().trim();

        if (category.isEmpty()) {
            category = "General";
        }

        String finalCategory = category;

        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
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
                data.put("prod_categoria", finalCategory);

                Log.d(TAG, "Enviando: " + data.toString());

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode >= 400) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
                    Log.e(TAG, "Error response: " + errorResponse.toString());
                }

                conn.disconnect();

                runOnUiThread(() -> {
                    setLoading(false);

                    if (responseCode == 201 || responseCode == 200) {
                        Toast.makeText(this, "✓ Producto guardado", Toast.LENGTH_SHORT).show();
                        clearFields();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: Código " + responseCode, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception: " + e.getMessage());
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void clearFields() {
        codeInput.setText("");
        nameInput.setText("");
        descInput.setText("");
        priceInput.setText("");
        stockInput.setText("");
        categoryInput.setText("");

        codeLayout.setError(null);
        nameLayout.setError(null);
        priceLayout.setError(null);
        stockLayout.setError(null);

        codeInput.requestFocus();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
        btnClear.setEnabled(!loading);

        codeInput.setEnabled(!loading);
        nameInput.setEnabled(!loading);
        descInput.setEnabled(!loading);
        priceInput.setEnabled(!loading);
        stockInput.setEnabled(!loading);
        categoryInput.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
