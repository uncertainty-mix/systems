package com.qcby.pos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRecord {
    private String recordId;
    private LocalDateTime purchaseTime;
    private List<PurchaseItem> items;
    private BigDecimal totalAmount;

    public PurchaseRecord() {
        this.items = new ArrayList<>();
        this.purchaseTime = LocalDateTime.now();
        this.recordId = generateRecordId();
        this.totalAmount = BigDecimal.ZERO;
    }

    public PurchaseRecord(String recordId, LocalDateTime purchaseTime, List<PurchaseItem> items, BigDecimal totalAmount) {
        this.recordId = recordId;
        this.purchaseTime = purchaseTime;
        this.items = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    private String generateRecordId() {
        return "PUR" + System.currentTimeMillis();
    }

    public void addItem(Integer goodsId, String goodsName, Integer quantity, BigDecimal unitPrice) {
        PurchaseItem item = new PurchaseItem(goodsId, goodsName, quantity, unitPrice);
        items.add(item);
        totalAmount = totalAmount.add(item.getSubtotal());
    }

    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return purchaseTime.format(formatter);
    }

    public void printRecord() {
        System.out.println("=========================");
        System.out.println("购物记录 ID: " + recordId);
        System.out.println("购买时间: " + getFormattedTime());
        System.out.println("购买商品:");
        for (PurchaseItem item : items) {
            System.out.println("  - " + item.getGoodsName() + 
                " x" + item.getQuantity() + 
                " @" + item.getUnitPrice().toPlainString() + "元" +
                " = " + item.getSubtotal().toPlainString() + "元");
        }
        System.out.println("总金额: " + totalAmount.toPlainString() + "元");
        System.out.println("=========================");
    }

    // Getters and Setters
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public LocalDateTime getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(LocalDateTime purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    // 购买项目的内部类
    public static class PurchaseItem {
        private Integer goodsId;
        private String goodsName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public PurchaseItem() {}

        public PurchaseItem(Integer goodsId, String goodsName, Integer quantity, BigDecimal unitPrice) {
            this.goodsId = goodsId;
            this.goodsName = goodsName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
        }

        // Getters and Setters
        public Integer getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(Integer goodsId) {
            this.goodsId = goodsId;
        }

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }
}