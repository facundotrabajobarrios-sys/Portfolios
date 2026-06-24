package com.example.app_erp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private View statsContainer;

    private TextView totalProductsText, totalValueText, lowStockText, categoriesText;

    private RecyclerView categoryRecyclerView;
    private CategoryReportAdapter categoryAdapter;
    private List<CategoryReportAdapter.CategoryReport> categoryReportList;

    private RecyclerView lowStockRecyclerView;
    private LowStockAdapter lowStockAdapter;
    private List<Product> lowStockList;

    private TextView lowStockCountBadge;
    private View emptyLowStockLayout;

    private ExecutorService executor;

    private static final String SUPABASE_URL = "https://ukcmmkpirlyzizvroswc.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVrY21ta3Bpcmx5eml6dnJvc3djIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQxNTI0NDUsImV4cCI6MjA2OTcyODQ0NX0.Qm9BFV20adYYaMrlgr0g77x26ROjl-P7rMjchZ_59fU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        initializeViews();
        setupToolbar();
        setupRecyclerViews();
        loadReports();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        statsContainer = findViewById(R.id.statsContainer);

        totalProductsText = findViewById(R.id.totalProductsText);
        totalValueText = findViewById(R.id.totalValueText);
        lowStockText = findViewById(R.id.lowStockText);
        categoriesText = findViewById(R.id.categoriesText);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        lowStockRecyclerView = findViewById(R.id.lowStockRecyclerView);

        executor = Executors.newSingleThreadExecutor();
        categoryReportList = new ArrayList<>();
        lowStockList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryReportAdapter(this, categoryReportList);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setAdapter(categoryAdapter);

        lowStockAdapter = new LowStockAdapter(this, lowStockList);
        lowStockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lowStockRecyclerView.setAdapter(lowStockAdapter);
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        statsContainer.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
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

                JSONArray jsonArray = new JSONArray(response.toString());

                int totalProducts = jsonArray.length();
                double totalValue = 0;
                int lowStockCount = 0;
                Map<String, CategoryReportAdapter.CategoryReport> categoryMap = new HashMap<>();

                lowStockList.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    int stock = obj.getInt("prod_stock");
                    double price = obj.getDouble("prod_precio");
                    String category = obj.getString("prod_categoria");
                    boolean active = obj.getBoolean("prod_activo");

                    totalValue += (stock * price);

                    if (stock < 10) {
                        lowStockCount++;
                        Product product = new Product(
                                obj.getInt("prod_id"),
                                obj.getString("prod_codigo"),
                                obj.getString("prod_nombre"),
                                obj.getString("prod_descripcion"),
                                price,
                                stock,
                                category,
                                active
                        );
                        lowStockList.add(product);
                    }

                    if (!categoryMap.containsKey(category)) {
                        categoryMap.put(category, new CategoryReportAdapter.CategoryReport(category, 0, 0));
                    }
                    CategoryReportAdapter.CategoryReport catReport = categoryMap.get(category);
                    catReport.setProductCount(catReport.getProductCount() + 1);
                    catReport.setTotalValue(catReport.getTotalValue() + (stock * price));
                }

                categoryReportList.clear();
                categoryReportList.addAll(categoryMap.values());

                int finalTotalProducts = totalProducts;
                double finalTotalValue = totalValue;
                int finalLowStockCount = lowStockCount;
                int finalCategoriesCount = categoryMap.size();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statsContainer.setVisibility(View.VISIBLE);

                    totalProductsText.setText(String.valueOf(finalTotalProducts));

                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PY"));
                    totalValueText.setText(format.format(finalTotalValue));

                    lowStockText.setText(String.valueOf(finalLowStockCount));
                    categoriesText.setText(String.valueOf(finalCategoriesCount));

                    categoryAdapter.notifyDataSetChanged();
                    lowStockAdapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar reportes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    // CLASE INTERNA TEMPORAL - LowStockAdapter
    private static class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.ViewHolder> {

        private final Context context;
        private final List<Product> lowStockList;

        public LowStockAdapter(Context context, List<Product> lowStockList) {
            this.context = context;
            this.lowStockList = lowStockList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.two_line_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = lowStockList.get(position);

            holder.text1.setText(product.getProd_nombre() + " (" + product.getProd_codigo() + ")");

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PY"));
            String info = "Stock: " + product.getProd_stock() + " | " + format.format(product.getProd_precio());
            holder.text2.setText(info);
        }

        @Override
        public int getItemCount() {
            return lowStockList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1;
            TextView text2;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
