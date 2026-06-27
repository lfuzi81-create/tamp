# 产品标签数据补充指导方案

> **创建时间**: 2026-06-27
> **问题根因**: BUG-03 是数据问题，数据库中大部分产品标签为空
> **解决方案**: 指导业务人员使用店铺端标签编辑功能

---

## 一、问题分析

### 1.1 BUG-03 根因

**症状**: 投资人端产品标签不展示

**根因**: 
- ✅ 后端代码正确（ShelfItem 实体包含 tags 字段，API 正确返回）
- ✅ 前端代码正确（String→Array 转换正确，展示逻辑正确）
- ✗ **数据问题**：数据库中 76.9% (10/13) 产品标签为 NULL 或空字符串

### 1.2 业务流程设计

```
产品上架流程：

店铺管理员 → 上架产品 → biz_shelf_item 表插入
    │
    ├─ 默认传递空标签 (tags = '')
    │
    └─ 需要后续手动编辑标签
```

---

## 二、补充方案

### 2.1 方案 A：店铺端标签编辑（推荐）

**适用场景**: 已上架的产品补充标签

**操作步骤**:

1. **登录店铺端**
   - URL: http://localhost:5173
   - 使用店铺管理员账号登录（如：13800000002）

2. **进入货架管理**
   - 导航到「货架管理」菜单
   - 选择「产品货架」标签页

3. **编辑产品标签**
   - 找到需要补充标签的产品
   - 点击「编辑」按钮
   - 在「标签」字段输入标签内容（如：`高收益,稳健型`）
   - 点击「保存」

4. **验证效果**
   - 登录投资人端（http://localhost:3003）
   - 进入「产品」页面
   - 确认产品标签已正确展示

---

### 2.2 方案 B：上架时直接填写标签

**适用场景**: 新上架产品（需代码优化）

**代码优化位置**: [ShelfService.java](file:///Users/pro/Documents/project/tamp/backend/tamp-shelf/src/main/java/com/tamp/shelf/service/ShelfService.java)

**修改建议**:

```java
// 上架产品时允许传递标签参数
public ShelfItem addProductToShelf(Long shopId, Long productId, String tags) {
    ShelfItem item = new ShelfItem();
    item.setShopId(shopId);
    item.setItemType("PRODUCT");
    item.setItemId(productId);
    item.setTags(tags != null ? tags : "");  // 允许传递标签
    item.setIsDeleted(0);
    return shelfItemRepository.save(item);
}
```

---

### 2.3 方案 C：批量更新标签

**适用场景**: 大量产品批量补充标签

**SQL 脚本**:

```sql
-- 批量更新产品标签
UPDATE biz_shelf_item 
SET tags = '高收益,稳健型' 
WHERE item_type = 'PRODUCT' 
  AND shop_id = 1 
  AND item_id IN (1, 2, 3);

-- 查看更新结果
SELECT id, shop_id, item_id, tags 
FROM biz_shelf_item 
WHERE item_type = 'PRODUCT' AND shop_id = 1;
```

---

## 三、标签填写规范

### 3.1 标签格式

- **分隔符**: 使用逗号 `,` 分隔多个标签
- **示例**: `高收益,稳健型` 或 `短期理财,低风险`
- **长度**: 每个标签建议不超过 10 个字符
- **数量**: 每个产品建议 2-4 个标签

### 3.2 常见标签词汇

| 产品类型 | 常见标签 |
|---------|---------|
| 固定收益类 | 高收益, 稳健型, 中长期 |
| 权益类 | 短期理财, 低风险, 推荐产品 |
| 混合类 | 灵活配置, 风险适中 |
| 结构化产品 | 保本型, 收益增强 |

---

## 四、验证检查

### 4.1 数据验证

```sql
-- 检查标签填充率
SELECT 
    COUNT(*) as total_products,
    SUM(CASE WHEN tags IS NOT NULL AND tags != '' THEN 1 ELSE 0 END) as products_with_tags,
    ROUND(SUM(CASE WHEN tags IS NOT NULL AND tags != '' THEN 1 ELSE 0 END) / COUNT(*) * 100, 1) as fill_rate
FROM biz_shelf_item 
WHERE item_type = 'PRODUCT' AND is_deleted = 0;
```

**目标**: 标签填充率达到 80% 以上

### 4.2 前端验证

- 登录投资人端，检查产品页面是否显示标签
- 检查标签样式是否正常（背景色、字体、间距）
- 检查标签点击是否可跳转（如有筛选功能）

---

## 五、常见问题

### 5.1 标签不展示怎么办？

**排查步骤**:
1. 检查数据库是否有标签数据
2. 检查后端 API 是否返回 tags 字段
3. 检查前端是否正确解析标签（String→Array）
4. 检查前端组件是否渲染标签元素

### 5.2 标签显示乱码怎么办？

**解决方案**:
- 确认数据库字符集为 `utf8mb4`
- 确认后端 API 返回编码为 UTF-8
- 确认前端页面编码为 UTF-8

---

## 六、联系支持

如有问题，请联系：
- **技术支持**: 查看 GitHub Issues: https://github.com/lfuzi81-create/tamp/issues
- **文档参考**: [BUG-03 根因分析](file:///Users/pro/Documents/project/tamp/docs/testing/FINAL_TEST_SUMMARY.md)

---

**创建者**: AI Agent
**创建时间**: 2026-06-27