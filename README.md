# 管理系统项目

本项目包含两个独立的管理系统：

## 系统介绍

### 1. 成绩管理系统 (Grade Management System)
- **功能**：学生成绩和课程管理
- **用户角色**：老师（管理）、学生（查询）
- **特色功能**：Excel导出、JSON数据存储
- **详细说明**：请查看 [grade-system-README.md](grade-system-README.md)

### 2. POS收银系统 (Point of Sale System)
- **功能**：商品管理、销售记录
- **特色功能**：库存管理、购物记录、数据持久化
- **详细说明**：请查看 [pos-system-README.md](pos-system-README.md)

## 快速开始

### 编译打包
```bash
mvn clean package
```

### 运行系统
```bash
# 运行成绩管理系统
java -jar target/grade-system.jar

# 运行POS收银系统
java -jar target/pos-system.jar
```

## 系统要求

- Java 8 或更高版本
- Maven 3.6 或更高版本（仅编译时需要）

## 项目结构

```
项目根目录/
├── src/
│   ├── com/qcby/grade/     # 成绩管理系统源码
│   └── com/qcby/pos/       # POS收银系统源码
├── target/
│   ├── grade-system.jar   # 成绩管理系统可执行文件
│   └── pos-system.jar     # POS收银系统可执行文件
├── grade-system-README.md # 成绩管理系统说明文档
├── pos-system-README.md   # POS收银系统说明文档
└── README.md              # 项目总体说明（本文件）
```

## 运行时文件结构

运行jar包后，会在当前目录自动生成：

```
运行目录/
├── data/                  # 数据文件目录
│   ├── courses.json       # 课程数据
│   ├── users.json         # 用户数据
│   ├── grades_*.json      # 成绩数据
│   ├── goods_data.json    # 商品数据
│   └── purchase_records.json # 购买记录
└── exports/               # Excel导出目录（仅成绩系统）
    ├── 成绩查询结果_*.xlsx
    └── 课程查询结果_*.xlsx
```

## 技术特性

- **数据持久化**：JSON格式存储
- **用户体验**：控制台交互界面
- **数据导出**：Excel格式导出（成绩系统）
- **密码安全**：密码输入隐藏显示
- **自动初始化**：首次运行自动创建数据文件

## 开发技术栈

- **语言**：Java 8
- **构建工具**：Maven
- **JSON处理**：Gson
- **Excel处理**：Apache POI

## 许可证

本项目仅供学习和演示使用。