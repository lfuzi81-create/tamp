package com.tamp.shelf.controller;

import com.tamp.shelf.service.ShelfService;
import com.tamp.shelf.controller.vo.ShelfContentVO;
import com.tamp.shelf.controller.vo.ShelfProductVO;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 货架管理控制器
 */
@RestController
@RequestMapping("/api/shelf")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    // ==================== 产品货架 ====================

    /**
     * 产品货架列表（店铺管理员 + 投资人只读）
     */
    @GetMapping("/products")
    @RequireRole({"SHOP_ADMIN", "INVESTOR"})
    public Result<List<ShelfProductVO>> listProductShelf(@RequestParam Long shopId) {
        return Result.ok(shelfService.listProductShelf(shopId));
    }

    /**
     * 添加产品到货架
     */
    @PostMapping("/products")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> addProductToShelf(@RequestBody AddShelfRequest request) {
        shelfService.addProductToShelf(request.getShopId(), request.getProductId(), request.getTags());
        return Result.ok();
    }

    /**
     * 更新产品排序
     */
    @PutMapping("/products/order")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> updateProductOrder(@RequestBody OrderRequest request) {
        shelfService.updateProductOrder(request.getShopId(), request.getProductIds());
        return Result.ok();
    }

    /**
     * 切换产品置顶状态
     */
    @PutMapping("/products/{id}/recommend")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> toggleProductRecommend(@RequestParam Long shopId, @PathVariable Long id) {
        shelfService.toggleProductRecommend(shopId, id);
        return Result.ok();
    }

    /**
     * 更新产品标签
     */
    @PutMapping("/products/{id}/tags")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> updateProductTags(@RequestParam Long shopId, @PathVariable Long id,
                                          @RequestBody TagsRequest request) {
        shelfService.updateProductTags(shopId, id, request.getTags());
        return Result.ok();
    }

    /**
     * 从货架移除产品
     */
    @DeleteMapping("/products/{id}")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> removeProductFromShelf(@RequestParam Long shopId, @PathVariable Long id) {
        shelfService.removeProductFromShelf(shopId, id);
        return Result.ok();
    }

    // ==================== 内容推荐 ====================

    /**
     * 内容推荐列表（店铺管理员 + 投资人只读）
     */
    @GetMapping("/contents")
    @RequireRole({"SHOP_ADMIN", "INVESTOR"})
    public Result<List<ShelfContentVO>> listContentShelf(@RequestParam Long shopId) {
        return Result.ok(shelfService.listContentShelf(shopId));
    }

    /**
     * 添加内容到推荐
     */
    @PostMapping("/contents")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> addContentToShelf(@RequestBody AddContentShelfRequest request) {
        shelfService.addContentToShelf(request.getShopId(), request.getContentId(), request.getTags());
        return Result.ok();
    }

    /**
     * 更新内容排序
     */
    @PutMapping("/contents/order")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> updateContentOrder(@RequestBody ContentOrderRequest request) {
        shelfService.updateContentOrder(request.getShopId(), request.getContentIds());
        return Result.ok();
    }

    /**
     * 切换内容置顶状态
     */
    @PutMapping("/contents/{id}/recommend")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> toggleContentRecommend(@RequestParam Long shopId, @PathVariable Long id) {
        shelfService.toggleContentRecommend(shopId, id);
        return Result.ok();
    }

    /**
     * 更新内容标签
     */
    @PutMapping("/contents/{id}/tags")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> updateContentTags(@RequestParam Long shopId, @PathVariable Long id,
                                           @RequestBody TagsRequest request) {
        shelfService.updateContentTags(shopId, id, request.getTags());
        return Result.ok();
    }

    /**
     * 从推荐移除内容
     */
    @DeleteMapping("/contents/{id}")
    @RequireRole("SHOP_ADMIN")
    public Result<Void> removeContentFromShelf(@RequestParam Long shopId, @PathVariable Long id) {
        shelfService.removeContentFromShelf(shopId, id);
        return Result.ok();
    }

    // ==================== 请求 DTO ====================

    public static class AddShelfRequest {
        private Long shopId;
        private Long productId;
        private String tags;

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }
    }

    public static class OrderRequest {
        private Long shopId;
        private List<Long> productIds;

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }

        public List<Long> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<Long> productIds) {
            this.productIds = productIds;
        }
    }

    public static class TagsRequest {
        private String tags;

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }
    }

    public static class AddContentShelfRequest {
        private Long shopId;
        private Long contentId;
        private String tags;

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }

        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }
    }

    public static class ContentOrderRequest {
        private Long shopId;
        private List<Long> contentIds;

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }

        public List<Long> getContentIds() {
            return contentIds;
        }

        public void setContentIds(List<Long> contentIds) {
            this.contentIds = contentIds;
        }
    }
}
