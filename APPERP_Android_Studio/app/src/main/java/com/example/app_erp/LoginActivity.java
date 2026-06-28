// LoginActivity.java - CON TABLA "usuarios" (PLURAL)
package com.example.app_erp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private ProgressBar loginProgress;
    private View loginCard, logoContainer;
    private CheckBox rememberMeCheckbox;
    private TextView forgotPassword;

    private static final String SUPABASE_URL = "https://ukcmmkpirlyzizvroswc.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVrY21ta3Bpcmx5eml6dnJvc3djIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQxNTI0NDUsImV4cCI6MjA2OTcyODQ0NX0.Qm9BFV20adYYaMrlgr0g77x26ROjl-P7rMjchZ_59fU";

    private ExecutorService executor;
    private SharedPreferences prefs;

    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupAnimations();

        executor = Executors.newSingleThreadExecutor();
        prefs = getSharedPreferences("AppERPPrefs", MODE_PRIVATE);

        loadSavedCredentials();

        if (prefs.getBoolean("isLoggedIn", false)) {
            goToDashboard();
            return;
        }

        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                loginUser();
            }
        });

        forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        forgotPassword.setOnLongClickListener(v -> {
            Intent intent = new Intent(this, TestConnectionActivity.class);
            startActivity(intent);
            return true;
        });
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        loginProgress = findViewById(R.id.loginProgress);
        loginCard = findViewById(R.id.loginCard);
        logoContainer = findViewById(R.id.logoContainer);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        forgotPassword = findViewById(R.id.forgotPassword);
    }

    private void setupAnimations() {
        logoContainer.setAlpha(0f);
        logoContainer.setTranslationY(-50f);
        logoContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        loginCard.setAlpha(0f);
        loginCard.setTranslationY(50f);
        loginCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void loadSavedCredentials() {
        boolean rememberMe = prefs.getBoolean(KEY_REMEMBER, false);
        if (rememberMe) {
            String savedEmail = prefs.getString(KEY_SAVED_EMAIL, "");
            String savedPassword = prefs.getString(KEY_SAVED_PASSWORD, "");

            emailInput.setText(savedEmail);
            passwordInput.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = prefs.edit();

        if (rememberMeCheckbox.isChecked()) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }

        editor.apply();
    }

    private boolean validateInputs() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        boolean isValid = true;

        if (email.isEmpty()) {
            emailInputLayout.setError("Ingresa tu correo");
            shakeView(emailInputLayout);
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Correo inválido");
            shakeView(emailInputLayout);
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Ingresa tu contraseña");
            shakeView(passwordInputLayout);
            isValid = false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Mínimo 6 caracteres");
            shakeView(passwordInputLayout);
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        return isValid;
    }

    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        loginButton.setEnabled(false);
        loginProgress.setVisibility(View.VISIBLE);

        loginButton.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .start();

        executor.execute(() -> {
            try {
                String query = "usu_correo=eq." + email + "&usu_pass=eq." + password;
                URL url = new URL(SUPABASE_URL + "/rest/v1/usuario?" + query);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONArray jsonArray = new JSONArray(response.toString());

                    runOnUiThread(() -> {
                        loginProgress.setVisibility(View.GONE);
                        loginButton.setEnabled(true);

                        loginButton.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        if (jsonArray.length() > 0) {
                            try {
                                JSONObject user = jsonArray.getJSONObject(0);

                                saveCredentials(email, password);

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.putInt("userId", user.getInt("usu_id"));
                                editor.putString("userName", user.getString("usu_nombre"));
                                editor.putString("userLastName", user.getString("usu_apellido"));
                                editor.putString("userEmail", user.getString("usu_correo"));
                                editor.putString("userRole", user.getString("usu_rol"));
                                editor.apply();

                                successAnimation();

                                Toast.makeText(this,
                                        "¡Bienvenido " + user.getString("usu_nombre") + "!",
                                        Toast.LENGTH_SHORT).show();

                                loginCard.postDelayed(this::goToDashboard, 600);
                            } catch (Exception e) {
                                showError("Error al procesar datos");
                            }
                        } else {
                            showError("Correo o contraseña incorrectos");
                            shakeView(loginCard);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        loginProgress.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        loginButton.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        showError("Error de conexión");
                    });
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    loginProgress.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    loginButton.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    showError("Error: " + e.getMessage());
                });
            }
        });
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);

        TextInputLayout emailLayout = dialogView.findViewById(R.id.emailInputLayout);
        TextInputEditText emailInput = dialogView.findViewById(R.id.emailInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Recuperar Contraseña")
                .setMessage("Ingresa tu correo electrónico registrado")
                .setView(dialogView)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    verifyEmailAndShowResetDialog(email);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void verifyEmailAndShowResetDialog(String email) {
        executor.execute(() -> {
            try {
                String query = "usu_correo=eq." + email;
                URL url = new URL(SUPABASE_URL + "/rest/v1/usuario?" + query);

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

                JSONArray jsonArray = new JSONArray(response.toString());

                runOnUiThread(() -> {
                    if (jsonArray.length() > 0) {
                        showResetPasswordDialog(email);
                    } else {
                        Toast.makeText(this, "Correo no encontrado en el sistema", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al verificar correo", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showResetPasswordDialog(String email) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);

        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordInputLayout);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirmPasswordInputLayout);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Nueva Contraseña")
                .setMessage("Ingresa tu nueva contraseña para: " + email)
                .setView(dialogView)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();

                    if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updatePassword(email, newPassword);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updatePassword(String email, String newPassword) {
        executor.execute(() -> {
            try {
                String query = "usu_correo=eq." + email;
                URL url = new URL(SUPABASE_URL + "/rest/v1/usuario?" + query);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);

                JSONObject updateData = new JSONObject();
                updateData.put("usu_pass", newPassword);

                OutputStream os = conn.getOutputStream();
                os.write(updateData.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("¡Éxito!")
                                .setMessage("Tu contraseña ha sido actualizada correctamente.")
                                .setPositiveButton("Entendido", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Error al actualizar contraseña", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void successAnimation() {
        loginCard.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .withEndAction(() ->
                        loginCard.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start()
                )
                .start();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
