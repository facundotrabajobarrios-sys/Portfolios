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

public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> lowStockList;

    public LowStockAdapter(Context context, List<Product> lowStockList) {
        this.context = context;
        this.lowStockList = lowStockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_low_stock_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = lowStockList.get(position);

        holder.productName.setText(product.getProd_nombre());
        holder.productStock.setText(String.valueOf(product.getProd_stock()));

        // Opcional: mostrar código del producto
        holder.productCode.setText(product.getProd_codigo());

        // Opcional: mostrar precio
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PY"));
        holder.productPrice.setText(format.format(product.getProd_precio()));
    }

    @Override
    public int getItemCount() {
        return lowStockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productStock;
        TextView productCode;
        TextView productPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productNameTextView);
            productStock = itemView.findViewById(R.id.productStockTextView);
            productCode = itemView.findViewById(R.id.productCodeTextView);
            productPrice = itemView.findViewById(R.id.productPriceTextView);
        }
    }
}
