package com.example.app_erp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout nameLayout, lastNameLayout, emailLayout, phoneLayout, newPassLayout;
    private TextInputEditText nameInput, lastNameInput, emailInput, phoneInput, newPassInput;
    private MaterialButton btnSave;
    private ProgressBar progressBar;

    private SharedPreferences prefs;
    private ExecutorService executor;
    private int userId;

    private static final String SUPABASE_URL = "https://vmjlvvyzkcwzxwijoljx.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZtamx2dnl6a2N3enh3aWpvbGp4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQzMzE2MTAsImV4cCI6MjA3OTkwNzYxMH0.ZbLoRGUCBZDh7SVclrZzEPP7IFfLe9YMm-d5asIbZvc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupToolbar();
        loadUserData();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        nameLayout = findViewById(R.id.nameInputLayout);
        lastNameLayout = findViewById(R.id.lastNameInputLayout);
        emailLayout = findViewById(R.id.emailInputLayout);
        phoneLayout = findViewById(R.id.phoneInputLayout);
        newPassLayout = findViewById(R.id.newPassInputLayout);

        nameInput = findViewById(R.id.nameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        newPassInput = findViewById(R.id.newPassInput);

        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        prefs = getSharedPreferences("AppERPPrefs", MODE_PRIVATE);
        executor = Executors.newSingleThreadExecutor();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuración");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        userId = prefs.getInt("userId", 0);
        nameInput.setText(prefs.getString("userName", ""));
        lastNameInput.setText(prefs.getString("userLastName", ""));
        emailInput.setText(prefs.getString("userEmail", ""));
    }

    private void saveChanges() {
        String name = nameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String newPass = newPassInput.getText().toString().trim();

        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/usuario?usu_id=eq." + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject data = new JSONObject();
                data.put("usu_nombre", name);
                data.put("usu_apellido", lastName);
                data.put("usu_correo", email);
                if (!phone.isEmpty()) {
                    data.put("usu_tel", phone);
                }
                if (!newPass.isEmpty()) {
                    data.put("usu_pass", newPass);
                }

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (responseCode >= 200 && responseCode < 300) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("userName", name);
                        editor.putString("userLastName", lastName);
                        editor.putString("userEmail", email);
                        editor.apply();

                        Toast.makeText(this, "✓ Datos actualizados", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
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
