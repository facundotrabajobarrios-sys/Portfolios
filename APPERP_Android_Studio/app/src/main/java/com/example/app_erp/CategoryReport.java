package com.example.app_erp;

public class CategoryReport {
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
