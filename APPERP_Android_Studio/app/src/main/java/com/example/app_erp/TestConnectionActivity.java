// TestConnectionActivity.java - CREA ESTE PARA PROBAR LA CONEXIÓN
package com.example.app_erp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestConnectionActivity extends AppCompatActivity {

    private static final String TAG = "TestConnection";
    private TextView resultText;
    private ExecutorService executor;

    private static final String SUPABASE_URL = "https://ukcmmkpirlyzizvroswc.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVrY21ta3Bpcmx5eml6dnJvc3djIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQxNTI0NDUsImV4cCI6MjA2OTcyODQ0NX0.Qm9BFV20adYYaMrlgr0g77x26ROjl-P7rMjchZ_59fU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        resultText = findViewById(R.id.resultText);
        Button testButton = findViewById(R.id.testButton);

        executor = Executors.newSingleThreadExecutor();

        testButton.setOnClickListener(v -> testConnection());
    }

    private void testConnection() {
        resultText.setText("Conectando...\n");

        executor.execute(() -> {
            StringBuilder result = new StringBuilder();

            try {
                // Test 1: Conexión básica
                result.append("=== TEST CONEXIÓN ===\n");
                URL testUrl = new URL(SUPABASE_URL + "/rest/v1/");
                HttpURLConnection testConn = (HttpURLConnection) testUrl.openConnection();
                testConn.setRequestMethod("GET");
                testConn.setRequestProperty("apikey", SUPABASE_KEY);

                int testCode = testConn.getResponseCode();
                result.append("Response Code: ").append(testCode).append("\n");
                testConn.disconnect();

                // Test 2: Obtener productos
                result.append("\n=== TEST PRODUCTOS ===\n");
                URL url = new URL(SUPABASE_URL + "/rest/v1/producto?select=*");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

                int responseCode = conn.getResponseCode();
                result.append("Response Code: ").append(responseCode).append("\n\n");

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Log.d(TAG, "Response: " + response.toString());

                    JSONArray products = new JSONArray(response.toString());
                    result.append("Total productos: ").append(products.length()).append("\n\n");

                    if (products.length() > 0) {
                        result.append("Primeros productos:\n");
                        for (int i = 0; i < Math.min(3, products.length()); i++) {
                            result.append("- ").append(products.getJSONObject(i).getString("prod_nombre")).append("\n");
                        }
                    } else {
                        result.append("⚠️ NO HAY PRODUCTOS EN LA BD\n");
                        result.append("Ejecuta el SQL de inserción\n");
                    }
                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;

                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    result.append("ERROR: ").append(errorResponse.toString()).append("\n");
                }

                conn.disconnect();

                // Test 3: Verificar usuarios
                result.append("\n=== TEST USUARIOS ===\n");
                URL urlUser = new URL(SUPABASE_URL + "/rest/v1/usuario?select=count");
                HttpURLConnection connUser = (HttpURLConnection) urlUser.openConnection();
                connUser.setRequestMethod("GET");
                connUser.setRequestProperty("apikey", SUPABASE_KEY);
                connUser.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

                int userCode = connUser.getResponseCode();
                result.append("Response Code: ").append(userCode).append("\n");

                if (userCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connUser.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONArray users = new JSONArray(response.toString());
                    result.append("Total usuarios: ").append(users.length()).append("\n");
                }

                connUser.disconnect();

            } catch (Exception e) {
                result.append("\n❌ ERROR: ").append(e.getMessage()).append("\n");
                Log.e(TAG, "Error: ", e);
            }

            String finalResult = result.toString();
            runOnUiThread(() -> resultText.setText(finalResult));
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
