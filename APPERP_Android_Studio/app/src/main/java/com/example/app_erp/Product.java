package com.example.app_erp;

public class Product {
    private int prod_id;
    private String prod_codigo;
    private String prod_nombre;
    private String prod_descripcion;
    private double prod_precio;
    private int prod_stock;
    private String prod_categoria;
    private boolean prod_activo;

    // Constructor
    public Product(int prod_id, String prod_codigo, String prod_nombre, String prod_descripcion,
                   double prod_precio, int prod_stock, String prod_categoria, boolean prod_activo) {
        this.prod_id = prod_id;
        this.prod_codigo = prod_codigo;
        this.prod_nombre = prod_nombre;
        this.prod_descripcion = prod_descripcion;
        this.prod_precio = prod_precio;
        this.prod_stock = prod_stock;
        this.prod_categoria = prod_categoria;
        this.prod_activo = prod_activo;
    }

    // Getters originales (estilo base de datos)
    public int getProd_id() {
        return prod_id;
    }

    public String getProd_codigo() {
        return prod_codigo;
    }

    public String getProd_nombre() {
        return prod_nombre;
    }

    public String getProd_descripcion() {
        return prod_descripcion;
    }

    public double getProd_precio() {
        return prod_precio;
    }

    public int getProd_stock() {
        return prod_stock;
    }

    public String getProd_categoria() {
        return prod_categoria;
    }

    public boolean isProd_activo() {
        return prod_activo;
    }

    // Getters alias (estilo Java convencional) - para compatibilidad
    public int getId() {
        return prod_id;
    }

    public String getCode() {
        return prod_codigo;
    }

    public String getName() {
        return prod_nombre;
    }

    public String getDescription() {
        return prod_descripcion;
    }

    public double getPrice() {
        return prod_precio;
    }

    public int getStock() {
        return prod_stock;
    }

    public String getCategory() {
        return prod_categoria;
    }

    public boolean isActive() {
        return prod_activo;
    }

    // Setters (si son necesarios)
    public void setProd_stock(int prod_stock) {
        this.prod_stock = prod_stock;
    }

    public void setProd_precio(double prod_precio) {
        this.prod_precio = prod_precio;
    }

    public void setProd_activo(boolean prod_activo) {
        this.prod_activo = prod_activo;
    }

    // Método auxiliar para verificar stock bajo
    public boolean isLowStock() {
        return prod_stock < 10; // Considera stock bajo si es menor a 10
    }
}
