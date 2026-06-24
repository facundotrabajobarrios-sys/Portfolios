// ProductAdapter.java - ACTUALIZADO CON REACTIVAR
package com.example.app_erp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onReactivate(Product product); // NUEVO
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productCode.setText("Código: " + product.getCode());
        holder.productCategory.setText(product.getCategory());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PY"));
        holder.productPrice.setText(format.format(product.getPrice()));
        holder.productStock.setText("Stock: " + product.getStock());

        // Mostrar/Ocultar badge de inactivo
        if (!product.isActive()) {
            holder.inactiveBadge.setVisibility(View.VISIBLE);
            holder.btnReactivate.setVisibility(View.VISIBLE);
            holder.cardView.setAlpha(0.6f); // Transparencia para productos inactivos
            holder.btnDelete.setVisibility(View.GONE); // Ocultar eliminar si ya está inactivo
        } else {
            holder.inactiveBadge.setVisibility(View.GONE);
            holder.btnReactivate.setVisibility(View.GONE);
            holder.cardView.setAlpha(1f);
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        // Color del indicador de stock
        if (product.isLowStock()) {
            holder.productStock.setTextColor(context.getColor(R.color.md_theme_error));
            holder.stockIndicator.setBackgroundColor(context.getColor(R.color.md_theme_error));
        } else {
            holder.productStock.setTextColor(context.getColor(R.color.md_theme_onSurfaceVariant));
            holder.stockIndicator.setBackgroundColor(context.getColor(R.color.md_theme_primary));
        }

        // Animación al hacer clic
        holder.cardView.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(product);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(product);
            }
        });

        holder.btnReactivate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReactivate(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView productName, productCode, productCategory, productPrice, productStock, inactiveBadge;
        ImageButton btnEdit, btnDelete;
        MaterialButton btnReactivate;
        View stockIndicator;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            productName = itemView.findViewById(R.id.productName);
            productCode = itemView.findViewById(R.id.productCode);
            productCategory = itemView.findViewById(R.id.productCategory);
            productPrice = itemView.findViewById(R.id.productPrice);
            productStock = itemView.findViewById(R.id.productStock);
            inactiveBadge = itemView.findViewById(R.id.inactiveBadge);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnReactivate = itemView.findViewById(R.id.btnReactivate);
            stockIndicator = itemView.findViewById(R.id.stockIndicator);
        }
    }
}
