// CategoryReportAdapter.java - COMPLETO CON IMPORTS
package com.example.app_erp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategoryReportAdapter extends RecyclerView.Adapter<CategoryReportAdapter.ViewHolder> {

    private Context context;
    private List<CategoryReport> reportList;

    public CategoryReportAdapter(Context context, List<CategoryReport> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryReport report = reportList.get(position);

        holder.categoryName.setText(report.getCategoryName());
        holder.productCount.setText(report.getProductCount() + " productos");

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PY"));
        holder.totalValue.setText(format.format(report.getTotalValue()));
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, productCount, totalValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            productCount = itemView.findViewById(R.id.productCount);
            totalValue = itemView.findViewById(R.id.totalValue);
        }
    }

    public static class CategoryReport {
        private String categoryName;
        private int productCount;
        private double totalValue;

        public CategoryReport(String categoryName, int productCount, double totalValue) {
            this.categoryName = categoryName;
            this.productCount = productCount;
            this.totalValue = totalValue;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public int getProductCount() {
            return productCount;
        }

        public void setProductCount(int productCount) {
            this.productCount = productCount;
        }

        public double getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(double totalValue) {
            this.totalValue = totalValue;
        }
    }
}
