package com.qcby.pos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


public class Goods {

    private Integer id;
    private String goodsName;
    private Integer goodsNum;
    private BigDecimal goodsPrice;


    public Goods() {
    }

    public Goods(Integer id, String goodsName, Integer goodsNum, BigDecimal goodsPrice) {
        this.id = id;
        this.goodsName = goodsName;
        this.goodsNum = goodsNum;
        this.goodsPrice = goodsPrice;
    }

    public Goods(Goods goods) {
        this.id = goods.id;
        this.goodsName = goods.goodsName;
        this.goodsNum = goods.goodsNum;
        this.goodsPrice = goods.goodsPrice;
    }

    public String getGoodsPriceText() {
        return goodsPrice == null ? "0.00"
                : goodsPrice.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public Boolean ifSame(Goods goods) {
        if (goods == null) return false;
        return Objects.equals(goods.getId(), this.getId())
                && Objects.equals(goods.getGoodsName(), this.getGoodsName())
                && Objects.equals(goods.getGoodsNum(), this.getGoodsNum())
                && compareBigDecimal(goods.getGoodsPrice(), this.getGoodsPrice());
    }

    private static boolean compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return a == b;
        return a.compareTo(b) == 0;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Integer getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(Integer goodsNum) {
        this.goodsNum = goodsNum;
    }

    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }


}
