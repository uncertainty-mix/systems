package com.qcby.pos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.qcby.pos.Utils.*;

public class Ui {
    private final Scanner scanner;
    private final PosSystem posSystem;

    public Ui(Scanner scanner) {
        this.scanner = scanner;
        this.posSystem = new PosSystem(100, 0);
    }

    // 从用户输入创建商品
    public Goods createGoodsFromInput() {
        Goods goods = new Goods();
        
        String name = scanNonEmptyName(scanner, "请输入商品名称：");
        goods.setGoodsName(name);
        
        BigDecimal price = scanPrice(scanner, "请输入商品价格，单位：元，整数或最多2位小数（例：10 或 10.50）：");
        goods.setGoodsPrice(price);
        
        int num = scanPositiveInt(scanner, "请输入商品数量（正整数）：");
        goods.setGoodsNum(num);
        
        return goods;
    }

    // 从用户输入修改商品（不包括ID）
    public void updateGoodsFromInput(Goods goods) {
        String name = scanNameOptional(scanner, "请输入商品名称（回车则不修改）：", goods.getGoodsName());
        goods.setGoodsName(name);
        
        BigDecimal price = scanPriceOptional(scanner, "请输入商品价格（回车则不修改）：", goods.getGoodsPrice());
        goods.setGoodsPrice(price);
        
        int num = scanPositiveIntOptional(scanner, "请输入商品数量（回车则不修改）：", goods.getGoodsNum());
        goods.setGoodsNum(num);
    }

    // 选择商品的通用方法
    public Goods selectGoods(String actionName) {
        String goodsName = scanNonEmptyName(scanner, "请输入要" + actionName + "的商品名称：");
        ArrayList<Goods> results = posSystem.queryGoods(goodsName);

        if (results.isEmpty()) {
            System.out.println("您要" + actionName + "的商品不存在");
            return null;
        }

        Goods targetGoods;
        if (results.size() == 1) {
            System.out.println("找到1件商品：");
            targetGoods = results.get(0);
        } else {
            System.out.println("找到" + results.size() + "件商品：");
            for (Goods result : results) {
                printGoodsInfo(result);
            }
            int selectId = scanPositiveInt(scanner, "请输入商品id进行" + actionName + "：");
            
            // 在查询结果中查找选择的商品ID
            targetGoods = null;
            for (Goods result : results) {
                if (result.getId().equals(selectId)) {
                    targetGoods = result;
                    break;
                }
            }
            
            if (targetGoods == null) {
                System.out.println("未找到该id的商品，" + actionName + "失败");
                return null;
            }
        }

        printGoodsInfo(targetGoods);
        if (scanConfirm(scanner, "是否确认" + actionName + "？(y/n)")) {
            return targetGoods;
        } else {
            System.out.println(actionName + "取消");
            return null;
        }
    }

    // 专门用于购买的商品选择
    public Goods selectGoodsForPurchase() {
        String goodsName = scanNonEmptyName(scanner, "请输入要购买的商品名称：");
        ArrayList<Goods> results = posSystem.queryAvailableGoods(goodsName);

        if (results.isEmpty()) {
            System.out.println("您要购买的商品不存在或库存为0");
            return null;
        }

        Goods targetGoods;
        if (results.size() == 1) {
            System.out.println("找到1件商品：");
            targetGoods = results.get(0);
        } else {
            System.out.println("找到" + results.size() + "件商品：");
            for (Goods result : results) {
                printGoodsInfo(result);
            }
            int selectId = scanPositiveInt(scanner, "请输入商品id进行购买：");
            
            // 在查询结果中查找选择的商品ID
            targetGoods = null;
            for (Goods result : results) {
                if (result.getId().equals(selectId)) {
                    targetGoods = result;
                    break;
                }
            }
            
            if (targetGoods == null) {
                System.out.println("未找到该id的商品，购买失败");
                return null;
            }
        }

        printGoodsInfo(targetGoods);
        if (scanConfirm(scanner, "是否确认购买？(y/n)")) {
            return targetGoods;
        } else {
            System.out.println("购买取消");
            return null;
        }
    }

    // 购买流程
    public void startPurchaseProcess() {
        System.out.println("购买商品");
        boolean continueShopping = true;
        BigDecimal totalAmount = BigDecimal.ZERO;
        PurchaseRecord currentRecord = posSystem.createPurchaseRecord();
        
        while (continueShopping) {
            Goods toBuy = selectGoodsForPurchase();
            if (toBuy != null) {
                int purchaseQuantity = scanPurchaseQuantity(scanner, 
                    "请输入购买数量：", toBuy.getGoodsNum());
                
                if (purchaseQuantity > 0) {
                    boolean purchaseSuccess = posSystem.buyGoods(toBuy.getId(), purchaseQuantity);
                    if (purchaseSuccess) {
                        BigDecimal itemTotal = toBuy.getGoodsPrice().multiply(new BigDecimal(purchaseQuantity));
                        totalAmount = totalAmount.add(itemTotal);
                        
                        // 添加到购物记录
                        currentRecord.addItem(toBuy.getId(), toBuy.getGoodsName(), 
                                            purchaseQuantity, toBuy.getGoodsPrice());
                        
                        System.out.println("购买成功！");
                        System.out.println("商品：" + toBuy.getGoodsName() + 
                            "，数量：" + purchaseQuantity + 
                            "，单价：" + toBuy.getGoodsPriceText() + "元");
                        System.out.println("当前累积金额：" + 
                            totalAmount.setScale(2, RoundingMode.HALF_UP).toPlainString() + "元");
                    } else {
                        System.out.println("购买失败！");
                    }
                }
                // 如果purchaseQuantity为0，直接跳过购买，不显示任何额外信息
            }
            
            continueShopping = scanConfirm(scanner, "是否继续购买其他商品？(y/n)");
        }
        
        // 购买结束时显示总计并保存记录
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            posSystem.addPurchaseRecord(currentRecord);
            System.out.println("=========================");
            System.out.println("购买完成！");
            System.out.println("总计金额：" + 
                totalAmount.setScale(2, RoundingMode.HALF_UP).toPlainString() + "元");
            System.out.println("购物记录已保存，记录ID：" + currentRecord.getRecordId());
            System.out.println("=========================");
        } else {
            System.out.println("未购买任何商品。");
        }
    }

    // 打印商品信息
    public void printGoodsInfo(Goods goods) {
        System.out.println("商品id:" + goods.getId());
        System.out.println("商品名称:" + goods.getGoodsName());
        System.out.println("商品价格(元):" + goods.getGoodsPriceText());
        System.out.println("商品数量:" + goods.getGoodsNum());
        System.out.println("------------------------");
    }

    // 打印商品列表
    public void printGoodsList(ArrayList<Goods> goodsList) {
        System.out.println("商品列表");
        for (Goods goods : goodsList) {
            printGoodsInfo(goods);
        }
    }

    // 打印查询结果
    public void printQueryResults(ArrayList<Goods> results) {
        if (results.isEmpty()) {
            System.out.println("未找到匹配的商品");
        } else {
            System.out.println("查询结果（共" + results.size() + "条）：");
            for (Goods goods : results) {
                printGoodsInfo(goods);
            }
        }
    }

    // 显示主菜单
    public void showMainMenu() {
        System.out.println("收银系统启动");
        System.out.println("请选择你的操作");
        System.out.println("1.添加商品");
        System.out.println("2.查询商品");
        System.out.println("3.修改商品");
        System.out.println("4.下架商品");
        System.out.println("5.购买商品");
        System.out.println("6.查看购物记录");
        System.out.println("7.退出系统");
        System.out.println("请输入你的操作:");
    }

    // 获取用户选择的菜单选项
    public int getMenuOption() {
        int option = scanner.nextInt();
        scanner.nextLine();
        return option;
    }

    // 获取查询商品名称
    public String getQueryGoodsName() {
        return scanNonEmptyName(scanner, "请输入要查询的商品名称：");
    }

    // 查看购物记录
    public void viewPurchaseRecords() {
        List<PurchaseRecord> records = posSystem.getAllPurchaseRecords();
        
        if (records.isEmpty()) {
            System.out.println("暂无购物记录");
            return;
        }
        
        System.out.println("购物记录查看");
        System.out.println("1.查看所有记录");
        System.out.println("2.查看最近10条记录");
        System.out.println("请选择查看方式:");
        
        int option = getMenuOption();
        
        switch (option) {
            case 1:
                System.out.println("所有购物记录（共" + records.size() + "条）：");
                for (PurchaseRecord record : records) {
                    record.printRecord();
                }
                break;
            case 2:
                int startIndex = Math.max(0, records.size() - 10);
                System.out.println("最近10条购物记录：");
                for (int i = startIndex; i < records.size(); i++) {
                    records.get(i).printRecord();
                }
                break;
            default:
                System.out.println("输入错误");
                break;
        }
    }

    // 主程序逻辑
    public void start() {
        boolean running = true;
        
        while (running) {
            showMainMenu();
            int option = getMenuOption();
            
            switch (option) {
                case 1:
                    System.out.println("添加商品");
                    Goods goods = createGoodsFromInput();
                    boolean ifAdded = posSystem.addGoods(goods);
                    if (ifAdded) {
                        System.out.println("商品添加成功！自动分配ID: " + goods.getId());
                        printGoodsList(posSystem.getAllGoods());
                    } else {
                        System.out.println("商品添加失败！");
                    }
                    break;
                case 2:
                    String queryGoodsName = getQueryGoodsName();
                    printQueryResults(posSystem.queryGoods(queryGoodsName));
                    break;
                case 3:
                    System.out.println("修改商品");
                    Goods toModify = selectGoods("修改");
                    if (toModify != null) {
                        System.out.println("请输入修改后的商品信息：");
                        Goods modifiedGoods = new Goods(toModify);
                        updateGoodsFromInput(modifiedGoods);
                        if (modifiedGoods.ifSame(toModify)) {
                            System.out.println("商品信息未修改，修改取消");
                            break;
                        }
                        boolean ifModified = posSystem.updateGoods(toModify.getId(), modifiedGoods);
                        if (ifModified) {
                            System.out.println("修改成功！");
                        } else {
                            System.out.println("修改失败！");
                        }
                    }
                    break;
                case 4:
                    System.out.println("下架商品");
                    Goods toRemove = selectGoods("下架");
                    if (toRemove != null) {
                        posSystem.removeGoods(toRemove.getId());
                        System.out.println("下架成功！");
                    }
                    break;
                case 5:
                    startPurchaseProcess();
                    break;
                case 6:
                    viewPurchaseRecords();
                    break;
                case 7:
                    System.out.println("退出系统");
                    posSystem.forceSave(); // 确保数据保存
                    running = false;
                    break;
                default:
                    System.out.println("输入错误,请重新输入");
                    break;
            }
        }
    }
}