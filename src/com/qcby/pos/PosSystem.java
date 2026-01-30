package com.qcby.pos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PosSystem {
    private final ArrayList<Goods> allGoods;
    private Integer googsIndex;
    private Integer goodsTotal;
    private final String dataFilePath;
    private final String recordsFilePath;
    private final Gson gson;
    private final List<PurchaseRecord> purchaseRecords;

    public PosSystem(int goodsTotal, int googsIndex) {
        this.allGoods = new ArrayList<>();
        this.goodsTotal = goodsTotal;
        this.googsIndex = googsIndex;
        this.dataFilePath = "data/goods_data.json";
        this.recordsFilePath = "data/purchase_records.json";
        this.gson = createGsonWithDateTimeSupport();
        this.purchaseRecords = new ArrayList<>();
        loadFromFile();
        loadPurchaseRecords();
    }

    public PosSystem(int goodsTotal, int googsIndex, String dataFilePath) {
        this.allGoods = new ArrayList<>();
        this.goodsTotal = goodsTotal;
        this.googsIndex = googsIndex;
        this.dataFilePath = dataFilePath;
        this.recordsFilePath = dataFilePath.replace(".json", "_records.json");
        this.gson = createGsonWithDateTimeSupport();
        this.purchaseRecords = new ArrayList<>();
        loadFromFile();
        loadPurchaseRecords();
    }

    private Gson createGsonWithDateTimeSupport() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(LocalDateTime.class, 
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                        context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, 
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
    }

    public Boolean addGoods(Goods goods){
        Integer newId = generateNextId();
        goods.setId(newId);
        
        if(this.googsIndex >= this.goodsTotal){
            System.out.println("商品已满,新增失败");
            return false;
        }
        this.allGoods.add(goods);
        this.googsIndex++;
        saveToFile();
        return true;
    }
    
    private Integer generateNextId() {
        Integer maxId = 0;
        for (Goods goods : allGoods) {
            if (goods.getId() > maxId) {
                maxId = goods.getId();
            }
        }
        return maxId + 1;
    }

    public ArrayList<Goods> getAllGoods() {
        return new ArrayList<>(allGoods.subList(0, googsIndex));
    }

    public ArrayList<Goods> queryGoods(String name) {
        ArrayList<Goods> result = new ArrayList<>();
        for (Goods goods : allGoods) {
            if (goods.getGoodsName().contains(name)) {
                result.add(goods);
            }
        }
        return result;
    }

    public ArrayList<Goods> queryAvailableGoods(String name) {
        ArrayList<Goods> result = new ArrayList<>();
        for (Goods goods : allGoods) {
            if (goods.getGoodsName().contains(name) && goods.getGoodsNum() > 0) {
                result.add(goods);
            }
        }
        return result;
    }

    public void removeGoods(Integer id) {
        for (int i = 0; i < this.googsIndex; i++) {
            if (this.allGoods.get(i).getId().equals(id)) {
                this.allGoods.remove(i);
                this.googsIndex--;
                saveToFile();
                return;
            }
        }
        System.out.println("SYSTEM ERROR: 未找到指定id的商品，删除失败");
        throw new RuntimeException();
    }

    public Goods findGoodsById(Integer id) {
        for (Goods goods : allGoods) {
            if (goods.getId().equals(id)) {
                return goods;
            }
        }
        return null;
    }

    public boolean buyGoods(Integer id, Integer quantity) {
        for (int i = 0; i < this.googsIndex; i++) {
            Goods goods = this.allGoods.get(i);
            if (goods.getId().equals(id)) {
                if (goods.getGoodsNum() < quantity) {
                    System.out.println("库存不足！当前库存：" + goods.getGoodsNum() + "，需要：" + quantity);
                    return false;
                }
                goods.setGoodsNum(goods.getGoodsNum() - quantity);
                if (goods.getGoodsNum() == 0) {
                    // 如果数量为0，从列表中移除
                    this.allGoods.remove(i);
                    this.googsIndex--;
                }
                saveToFile();
                return true;
            }
        }
        System.out.println("SYSTEM ERROR: 未找到指定id的商品，购买失败");
        return false;
    }

    public boolean updateGoods(Integer id, Goods updatedGoods) {
        for (int i = 0; i < this.googsIndex; i++) {
            Goods goods = this.allGoods.get(i);
            if (goods.getId().equals(id)) {
                goods.setGoodsName(updatedGoods.getGoodsName());
                goods.setGoodsPrice(updatedGoods.getGoodsPrice());
                goods.setGoodsNum(updatedGoods.getGoodsNum());
                saveToFile();
                return true;
            }
        }
        System.out.println("SYSTEM ERROR: 未找到指定id的商品，修改失败");
        return false;
    }


    public Integer getGoogsIndex() {
        return googsIndex;
    }

    public void setGoogsIndex(Integer googsIndex) {
        this.googsIndex = googsIndex;
    }

    public Integer getGoodsTotal() {
        return goodsTotal;
    }

    public void setGoodsTotal(Integer goodsTotal) {
        this.goodsTotal = goodsTotal;
    }

    // 保存数据到JSON文件
    private void saveToFile() {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(dataFilePath), "UTF-8")) {
            PersistenceData data = new PersistenceData();
            data.goods = new ArrayList<>(allGoods.subList(0, googsIndex));
            data.googsIndex = googsIndex;
            data.goodsTotal = goodsTotal;
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    // 从JSON文件加载数据
    private void loadFromFile() {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            System.out.println("数据文件不存在，使用空库存初始化");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(dataFilePath), "UTF-8")) {
            Type dataType = new TypeToken<PersistenceData>(){}.getType();
            PersistenceData data = gson.fromJson(reader, dataType);
            
            if (data != null && data.goods != null) {
                allGoods.clear();
                allGoods.addAll(data.goods);
                googsIndex = data.googsIndex != null ? data.googsIndex : data.goods.size();
                // goodsTotal保持构造函数中设置的值
                System.out.println("成功加载 " + googsIndex + " 件商品");
            }
        } catch (IOException e) {
            System.err.println("加载数据失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("解析数据失败: " + e.getMessage());
        }
    }

    // 手动保存数据（可选，用于程序退出时确保数据保存）
    public void forceSave() {
        saveToFile();
        savePurchaseRecords();
    }

    // 创建购物记录
    public PurchaseRecord createPurchaseRecord() {
        return new PurchaseRecord();
    }

    // 添加购物记录
    public void addPurchaseRecord(PurchaseRecord record) {
        purchaseRecords.add(record);
        savePurchaseRecords();
    }

    // 获取所有购物记录
    public List<PurchaseRecord> getAllPurchaseRecords() {
        return new ArrayList<>(purchaseRecords);
    }

    // 根据日期范围查询购物记录
    public List<PurchaseRecord> getPurchaseRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<PurchaseRecord> result = new ArrayList<>();
        for (PurchaseRecord record : purchaseRecords) {
            if (record.getPurchaseTime().isAfter(startDate) && record.getPurchaseTime().isBefore(endDate)) {
                result.add(record);
            }
        }
        return result;
    }

    // 保存购物记录到JSON文件
    private void savePurchaseRecords() {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(recordsFilePath), "UTF-8")) {
            gson.toJson(purchaseRecords, writer);
        } catch (IOException e) {
            System.err.println("保存购物记录失败: " + e.getMessage());
        }
    }

    // 从JSON文件加载购物记录
    private void loadPurchaseRecords() {
        File file = new File(recordsFilePath);
        if (!file.exists()) {
            System.out.println("购物记录文件不存在，使用空记录初始化");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(recordsFilePath), "UTF-8")) {
            Type recordsType = new TypeToken<List<PurchaseRecord>>(){}.getType();
            List<PurchaseRecord> records = gson.fromJson(reader, recordsType);
            
            if (records != null) {
                purchaseRecords.clear();
                purchaseRecords.addAll(records);
                System.out.println("成功加载 " + purchaseRecords.size() + " 条购物记录");
            }
        } catch (IOException e) {
            System.err.println("加载购物记录失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("解析购物记录失败: " + e.getMessage());
        }
    }

    // 数据持久化的内部类
    private static class PersistenceData {
        ArrayList<Goods> goods;
        Integer googsIndex;
        Integer goodsTotal;
    }
}
