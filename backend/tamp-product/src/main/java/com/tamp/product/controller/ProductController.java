package com.tamp.product.controller;

import com.tamp.product.entity.Product;
import com.tamp.product.entity.ProductCategory;
import com.tamp.product.service.ProductService;
import com.tamp.common.dto.PageResult;
import com.tamp.common.dto.Result;
import com.tamp.product.controller.vo.ProductVO;
import com.tamp.common.security.RequireRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 【产品控制器】—— 接收前端请求，调用 Service 处理，返回结果
 *
 * 作用：就像餐厅的服务员
 *   - 接待客人（接收 HTTP 请求）
 *   - 记录点了什么菜（提取请求参数）
 *   - 把菜单送到厨房（调用 Service）
 *   - 把做好的菜端上桌（返回 Result JSON）
 *
 * API 路径规则：
 *   所有接口都以 /api/products 开头
 *   GET    /api/products           → 查询产品列表
 *   POST   /api/products           → 创建产品
 *   PUT    /api/products/{id}      → 更新产品
 *   DELETE /api/products/{id}      → 删除产品
 *   PUT    /api/products/{id}/status → 切换上下架
 *   GET    /api/products/categories → 查询分类列表
 *   POST   /api/products/categories → 创建分类
 *   ...
 */
@RestController                    // 【标记】我是 REST 控制器，返回 JSON 而不是页面
@RequestMapping("/api/products")   // 所有接口的路径前缀
public class ProductController {

    private final ProductService productService;

    /**
     * 构造器注入 —— Spring 自动把 ProductService 传进来
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 查询产品列表 —— GET /api/products
     *
     * 前端调用示例：GET /api/products?keyword=稳健&categoryId=1&status=0&page=1&size=10
     *
     * 权限：所有已登录用户都能查看（没有 @RequireRole 注解）
     *
     * @param keyword    搜索关键字（可选）
     * @param categoryId 分类ID（可选）
     * @param status     状态（可选，0=上架，1=下架）
     * @param page       页码（默认第1页）
     * @param size       每页条数（默认10条）
     */
    @GetMapping
    public Result<PageResult<ProductVO>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> result = productService.listProducts(keyword, categoryId, status, pageable);

        List<Long> productIds = result.getContent().stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, Integer> shelfCountMap = productService.getShelfCountMap(productIds);
        Map<Long, BigDecimal> clientAumMap = productService.getClientAumMap(productIds);

        Page<ProductVO> voPage = result.map(p -> {
            ProductVO vo = new ProductVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setCategoryId(p.getCategoryId());
            vo.setType(p.getType());
            vo.setDescription(p.getDescription());
            vo.setAumMin(p.getAumMin());
            vo.setAumMax(p.getAumMax());
            vo.setExpectedReturn(p.getExpectedReturn());
            vo.setRiskLevel(p.getRiskLevel());
            vo.setDuration(p.getDuration());
            vo.setStatus(p.getStatus());
            vo.setCoverImage(p.getCoverImage());
            vo.setDetailUrl(p.getDetailUrl());
            vo.setPurchaseUrl(p.getPurchaseUrl());
            vo.setAttr1(p.getAttr1());
            vo.setAttr2(p.getAttr2());
            vo.setViewCount(p.getViewCount() != null ? p.getViewCount() : 0);
            vo.setCreatedAt(p.getCreatedAt());
            vo.setUpdatedAt(p.getUpdatedAt());
            vo.setShelfCount(shelfCountMap.getOrDefault(p.getId(), 0));
            vo.setClientAum(clientAumMap.getOrDefault(p.getId(), BigDecimal.ZERO));
            return vo;
        });
        return Result.ok(PageResult.of(voPage));
    }

    /**
     * 创建产品 —— POST /api/products
     *
     * 前端调用示例：POST /api/products，请求体 { "name": "稳健增值1号", "type": "FIXED", ... }
     *
     * 权限：只有 SUPER_ADMIN / PLATFORM_ADMIN / TAMP_ADMIN 才能创建
     *       店铺管理员和投资人不能创建产品
     *
     * @RequireRole 注解会被 RoleAuthorizationInterceptor 拦截校验
     */
    @PostMapping                                                    // POST 方法
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})   // 角色限制
    public Result<Product> createProduct(@RequestBody Product product) {  // @RequestBody 把请求体 JSON 转成 Product 对象
        return Result.ok(productService.createProduct(product));
    }

    /**
     * 更新产品 —— PUT /api/products/{id}
     *
     * 前端调用示例：PUT /api/products/5，请求体 { "name": "新名称", "riskLevel": 3 }
     *
     * @PathVariable 从 URL 路径里取参数（比如 /api/products/5 里的 5）
     * @RequestBody 从请求体里取 JSON 数据
     */
    @PutMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return Result.ok(productService.updateProduct(id, product));
    }

    /**
     * 删除产品 —— DELETE /api/products/{id}
     *
     * 注意：这是逻辑删除，不是物理删除
     * 数据库里不会真的删掉这行，只是 is_deleted 从 0 变成 1
     */
    @DeleteMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.ok();                                         // 删除成功，不需要返回数据
    }

    /**
     * 切换上下架状态 —— PUT /api/products/{id}/status
     *
     * 上架 → 下架，下架 → 上架
     */
    @PutMapping("/{id}/status")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> toggleStatus(@PathVariable Long id) {
        productService.toggleStatus(id);
        return Result.ok();
    }

    // ===== 以下是产品分类的接口 =====

    /** 查询分类列表 —— 所有登录用户可查看 */
    @GetMapping("/categories")
    public Result<List<ProductCategory>> listCategories() {
        return Result.ok(productService.listCategories());
    }

    /** 创建分类 —— 仅总部管理员 */
    @PostMapping("/categories")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<ProductCategory> createCategory(@RequestBody ProductCategory category) {
        return Result.ok(productService.createCategory(category));
    }

    /** 更新分类 —— 仅总部管理员 */
    @PutMapping("/categories/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<ProductCategory> updateCategory(@PathVariable Long id, @RequestBody ProductCategory category) {
        return Result.ok(productService.updateCategory(id, category));
    }

    /** 删除分类 —— 仅总部管理员（分类下有产品时不能删） */
    @DeleteMapping("/categories/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return Result.ok();
    }

    /**
     * 查询产品上架店铺详情 —— GET /api/products/{id}/shelf-shops
     *
     * 前端调用示例：GET /api/products/5/shelf-shops
     * 用途：hover 产品列表的"上架店铺"时展示每个店铺的详情卡片
     * 权限：所有登录用户可查看（没有 @RequireRole 注解）
     */
    @GetMapping("/{id}/shelf-shops")
    public Result<Map<String, Object>> getShelfShops(@PathVariable Long id) {
        return Result.ok(productService.getShelfShops(id));
    }

    /**
     * 投资人端推荐产品 —— GET /api/products/recommend
     *
     * shopId 来源优先级（防伪造）：
     * 1. JWT 内的 shopId（SecurityUtils.getCurrentShopId，登录时签发，不可伪造）
     * 2. X-Shop-Id 请求头（仅当 JWT 无 shopId 时回退，向后兼容非投资人端）
     * 3. 无 shopId 时全局查 6 条（兜底）
     */
    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> getRecommendProducts(
            @RequestHeader(value = "X-Shop-Id", required = false) Long headerShopId) {
        // 优先用 JWT 中的 shopId（防伪造），其次回退到请求头
        Long shopId = com.tamp.common.util.SecurityUtils.getCurrentShopId();
        if (shopId == null) {
            shopId = headerShopId;
        }
        List<Product> products;
        if (shopId != null) {
            List<Long> productIds = productService.listProductIdsByShop(shopId);
            if (productIds.isEmpty()) {
                return Result.ok(java.util.Collections.emptyList());
            }
            // 按货架返回的顺序展示，保留 sortOrder 顺序
            List<Product> fetched = productService.listByIds(productIds);
            java.util.Map<Long, Product> byId = fetched.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            products = new ArrayList<>();
            for (Long pid : productIds) {
                Product p = byId.get(pid);
                if (p != null) {
                    products.add(p);
                }
            }
        } else {
            Pageable pageable = PageRequest.of(0, 6);
            Page<Product> result = productService.listProducts(null, null, 0, pageable);
            products = result.getContent();
        }

        List<Map<String, Object>> list = products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("type", p.getType());
            map.put("description", p.getDescription());
            map.put("aumMin", p.getAumMin());
            map.put("expectedReturn", p.getExpectedReturn());
            map.put("riskLevel", p.getRiskLevel());
            map.put("duration", p.getDuration());
            map.put("coverImage", p.getCoverImage());
            map.put("attr1", p.getAttr1());
            map.put("attr2", p.getAttr2());
            map.put("viewCount", p.getViewCount() != null ? p.getViewCount() : 0);
            return map;
        }).collect(Collectors.toList());

        return Result.ok(list);
    }
}
