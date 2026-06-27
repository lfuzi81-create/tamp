package com.tamp.product.service;

import com.tamp.product.entity.Product;
import com.tamp.product.entity.ProductCategory;
import com.tamp.product.repository.ProductRepository;
import com.tamp.product.repository.ProductCategoryRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 【产品服务】—— 处理产品相关的业务逻辑
 *
 * 作用：就像厨师，接收"订单"（来自 Controller 的调用），按"菜谱"（业务规则）做菜
 *       不直接操作数据库，而是通过 Repository（仓库管理员）来存取数据
 *
 * 核心职责：
 *   1. 查询产品列表（支持搜索+筛选+分页）
 *   2. 查询单个产品详情
 *   3. 创建新产品
 *   4. 更新产品信息
 *   5. 删除产品（逻辑删除，不是真删）
 *   6. 上下架切换
 *   7. 产品分类的增删改查
 */
@Service   // 【标记】我是业务服务类，Spring 会自动创建实例并注入到 Controller 里
public class ProductService {

    private final ProductRepository productRepository;           // 产品仓库（操作产品表）
    private final ProductCategoryRepository productCategoryRepository;  // 分类仓库（操作分类表）
    private final JdbcTemplate jdbcTemplate;                     // 直接执行 SQL 的工具（用于跨表查询）

    /**
     * 构造器注入 —— Spring 自动把这三个依赖传进来
     * 不用 @Autowired 注解，构造器注入是更推荐的写法
     */
    public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, JdbcTemplate jdbcTemplate) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询产品被多少个店铺上架了（跨表统计）
     *
     * 这个方法直接写 SQL 查 biz_shelf_item 表（货架表），
     * 因为货架属于另一个模块（tamp-shelf），不方便注入它的 Repository
     *
     * 返回格式：{ 产品ID=1: 3家店铺, 产品ID=2: 1家店铺 }
     * 用途：在产品列表页显示"已上架3家店铺"
     */
    public Map<Long, Integer> getShelfCountMap(List<Long> productIds) {
        if (productIds.isEmpty()) return Collections.emptyMap();
        Map<Long, Integer> result = new HashMap<>();
        String placeholders = productIds.stream().map(p -> "?").collect(Collectors.joining(","));
        String sql = "SELECT item_id, COUNT(DISTINCT shop_id) as cnt FROM biz_shelf_item WHERE item_type = 'PRODUCT' AND is_deleted = 0 AND item_id IN (" + placeholders + ") GROUP BY item_id";
        jdbcTemplate.query(sql, rs -> {
            result.put(rs.getLong("item_id"), rs.getInt("cnt"));
        }, productIds.toArray());
        return result;
    }

    /**
     * 按产品汇总已授权客户资产 AUM（单位：万，与 biz_client_asset.amount 一致）
     */
    public Map<Long, BigDecimal> getClientAumMap(List<Long> productIds) {
        if (productIds.isEmpty()) return Collections.emptyMap();
        Map<Long, BigDecimal> result = new HashMap<>();
        String placeholders = productIds.stream().map(p -> "?").collect(Collectors.joining(","));
        String sql = """
            SELECT product_id, COALESCE(SUM(amount), 0) AS total_aum
            FROM biz_client_asset
            WHERE product_id IN (%s)
              AND is_deleted = 0
              AND is_authorized = 1
            GROUP BY product_id
            """.formatted(placeholders);
        jdbcTemplate.query(sql, rs -> {
            result.put(rs.getLong("product_id"), rs.getBigDecimal("total_aum"));
        }, productIds.toArray());
        return result;
    }

    /**
     * 查询产品列表（核心方法）—— 支持搜索+筛选+分页
     *
     * 参数说明：
     *   keyword    → 搜索关键字（模糊匹配产品名称）
     *   categoryId → 按分类筛选（null 表示不筛选）
     *   status     → 按状态筛选（null 表示不筛选）
     *   pageable   → 分页参数（第几页、每页几条）
     *
     * 使用 Specification 动态拼接查询条件：
     *   就像搭积木，有哪个条件就加哪块，没有就跳过
     *   最终拼成的 SQL 类似：
     *   SELECT * FROM biz_product WHERE is_deleted = 0 AND name LIKE '%稳健%' AND category_id = 1
     */
    public Page<Product> listProducts(String keyword, Long categoryId, Integer status, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), 0));   // 永远排除已删除的数据
            if (StringUtils.hasText(keyword)) {                   // 如果有关键字
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));  // 模糊搜索
            }
            if (categoryId != null) {                             // 如果有分类筛选
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (status != null) {                                 // 如果有状态筛选
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));  // 所有条件用 AND 连接
        };
        return productRepository.findAll(spec, pageable);         // 执行查询，返回分页结果
    }

    /**
     * 按 ID 列表批量查询产品（用于投资人端按店铺货架查推荐产品）
     *
     * 不分页、不做 keyword 过滤，仅按 id 集合返回未删除的产品。
     * 调用方负责按入参顺序自行排序。
     */
    public List<Product> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return productRepository.findAllById(ids).stream()
                .filter(p -> p.getDeleted() != null && p.getDeleted() == 0)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定店铺上架的产品 ID 列表（按 sortOrder 升序）
     *
     * 供投资人端按 X-Shop-Id 查推荐产品使用。
     * 直接查 biz_shelf_item 表，避免依赖 tamp-shelf 模块（否则会形成循环依赖）。
     */
    public List<Long> listProductIdsByShop(Long shopId) {
        if (shopId == null) {
            return Collections.emptyList();
        }
        String sql = "SELECT item_id FROM biz_shelf_item WHERE shop_id = ? AND item_type = 'PRODUCT' AND is_deleted = 0 ORDER BY is_top DESC, top_time DESC, sort_order ASC, id ASC";
        return jdbcTemplate.queryForList(sql, Long.class, shopId);
    }

    /**
     * 查询单个产品 —— 找不到就抛异常
     *
     * orElseThrow 的意思：找到了就返回，找不到就抛 BizException
     * 前端会收到：{ "code": 3000, "message": "产品不存在" }
     */
    public Product getProduct(Long id) {
        return productRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND));
    }

    /**
     * 创建新产品
     *
     * @Transactional 表示：这个方法里的数据库操作是一个"事务"
     * 要么全部成功，要么全部回滚（不会出现"创建了一半"的情况）
     */
    @Transactional
    public Product createProduct(Product product) {
        product.setDeleted(0);
        if (product.getViewCount() == null) product.setViewCount(0);
        return productRepository.save(product);
    }

    /**
     * 更新产品信息
     *
     * 只更新前端传了值的字段（非 null 的才更新），null 的字段保持不变
     * 这样前端可以只传需要修改的字段，不用把所有字段都传一遍
     */
    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existing = getProduct(id);        // 先找到现有的产品
        // 逐个字段判断：前端传了新值就用新值，没传就保持原值
        if (product.getName() != null) existing.setName(product.getName());
        if (product.getCategoryId() != null) existing.setCategoryId(product.getCategoryId());
        if (product.getType() != null) existing.setType(product.getType());
        if (product.getDescription() != null) existing.setDescription(product.getDescription());
        if (product.getAumMin() != null) existing.setAumMin(product.getAumMin());
        if (product.getAumMax() != null) existing.setAumMax(product.getAumMax());
        if (product.getExpectedReturn() != null) existing.setExpectedReturn(product.getExpectedReturn());
        if (product.getRiskLevel() != null) existing.setRiskLevel(product.getRiskLevel());
        if (product.getDuration() != null) existing.setDuration(product.getDuration());
        if (product.getStatus() != null) existing.setStatus(product.getStatus());
        if (product.getCoverImage() != null) existing.setCoverImage(product.getCoverImage());
        if (product.getDetailUrl() != null) existing.setDetailUrl(product.getDetailUrl());
        if (product.getPurchaseUrl() != null) existing.setPurchaseUrl(product.getPurchaseUrl());
        if (product.getAttr1() != null) existing.setAttr1(product.getAttr1());
        if (product.getAttr2() != null) existing.setAttr2(product.getAttr2());
        return productRepository.save(existing);  // 保存修改
    }

    /**
     * 删除产品（逻辑删除！不是真删！）
     *
     * 只是把 is_deleted 从 0 改成 1
     * 数据库里的数据还在，只是查询时会被过滤掉
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProduct(id);         // 先找到这个产品
        product.setDeleted(1);                    // 标记为"已删除"
        productRepository.save(product);          // 保存修改
    }

    /**
     * 切换上下架状态
     *
     * 上架(0) → 下架(1)
     * 下架(1) → 上架(0)
     * 就像一个开关，按一下切换
     */
    @Transactional
    public void toggleStatus(Long id) {
        Product product = getProduct(id);
        product.setStatus(product.getStatus() == null || product.getStatus() == 0 ? 1 : 0);
        productRepository.save(product);
    }

    // ===== 以下是产品分类的方法 =====

    /** 查询所有分类 */
    public List<ProductCategory> listCategories() {
        return productCategoryRepository.findAll();
    }

    /** 创建分类 */
    @Transactional
    public ProductCategory createCategory(ProductCategory category) {
        return productCategoryRepository.save(category);
    }

    /** 更新分类 */
    @Transactional
    public ProductCategory updateCategory(Long id, ProductCategory category) {
        category.setId(id);
        return productCategoryRepository.save(category);
    }

    /**
     * 删除分类 —— 有关联产品时不能删
     *
     * 业务规则：如果这个分类下还有产品，就不允许删除
     * 防止出现"分类没了，产品变成孤儿"的情况
     */
    @Transactional
    public void deleteCategory(Long id) {
        long count = productRepository.countByCategoryIdAndDeleted(id, 0);  // 统计该分类下有多少产品
        if (count > 0) {
            throw new BizException(ErrorCode.BIZ_CATEGORY_HAS_PRODUCTS);   // 有产品就不能删
        }
        productCategoryRepository.deleteById(id);  // 没有关联产品，可以安全删除
    }

    /**
     * 查询产品上架店铺详情（用于 hover 卡片展示）
     *
     * 跨模块 JOIN：biz_shelf_item（货架表）+ biz_shop（店铺表）+ biz_client（客户表）
     * 使用原生 SQL 因为涉及多个模块的表
     *
     * 返回格式：
     * {
     *   "shops": [ { "shopId":1, "shopName":"xxx", "orderCount":28, "shopAum":8600.00 }, ... ],
     *   "totalOrders": 43,
     *   "shopCount": 2
     * }
     */
    public Map<String, Object> getShelfShops(Long productId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> shops = new ArrayList<>();

        String sql = """
            SELECT
                s.id AS shopId,
                s.name AS shopName,
                COUNT(DISTINCT ca.id) AS orderCount,
                COALESCE(SUM(ca.amount), 0) AS shopAum
            FROM biz_shelf_item si
            JOIN biz_shop s ON si.shop_id = s.id AND s.is_deleted = 0
            LEFT JOIN biz_client_asset ca ON ca.product_id = ?
              AND ca.is_deleted = 0
              AND ca.is_authorized = 1
              AND (
                FIND_IN_SET(CAST(s.id AS CHAR) COLLATE utf8mb4_unicode_ci, ca.auth_scope) > 0
                OR EXISTS (
                  SELECT 1 FROM biz_client cx
                  WHERE cx.id = ca.client_id AND cx.shop_id = s.id AND cx.is_deleted = 0
                )
              )
            WHERE si.item_type = 'PRODUCT'
              AND si.item_id = ?
              AND si.is_deleted = 0
            GROUP BY s.id, s.name
            ORDER BY si.sort_order ASC, s.id ASC
            """;

        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> shop = new HashMap<>();
            shop.put("shopId", rs.getLong("shopId"));
            shop.put("shopName", rs.getString("shopName"));
            BigDecimal aum = rs.getBigDecimal("shopAum");
            shop.put("shopAum", aum != null ? aum.setScale(2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO.setScale(2));
            shop.put("orderCount", rs.getInt("orderCount"));
            shops.add(shop);
        }, productId, productId);

        int totalOrders = shops.stream()
                .mapToInt(s -> (Integer) s.get("orderCount"))
                .sum();

        result.put("shops", shops);
        result.put("totalOrders", totalOrders);
        result.put("shopCount", shops.size());

        return result;
    }
}
