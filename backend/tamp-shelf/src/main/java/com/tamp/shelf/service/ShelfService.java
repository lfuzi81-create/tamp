package com.tamp.shelf.service;

import com.tamp.content.entity.Content;
import com.tamp.content.repository.ContentRepository;
import com.tamp.product.entity.Product;
import com.tamp.product.repository.ProductRepository;
import com.tamp.shelf.entity.ShelfItem;
import com.tamp.shelf.repository.ShelfItemRepository;
import com.tamp.shelf.controller.vo.ShelfContentVO;
import com.tamp.shelf.controller.vo.ShelfProductVO;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 货架管理服务
 */
@Service
public class ShelfService {

    private static final Logger log = LoggerFactory.getLogger(ShelfService.class);

    private final ShelfItemRepository shelfItemRepository;
    private final ProductRepository productRepository;
    private final ContentRepository contentRepository;

    public ShelfService(ShelfItemRepository shelfItemRepository, ProductRepository productRepository,
                        ContentRepository contentRepository) {
        this.shelfItemRepository = shelfItemRepository;
        this.productRepository = productRepository;
        this.contentRepository = contentRepository;
    }

    // ==================== 便捷方法 ====================

    /**
     * 查询指定店铺上架的产品 ID 列表（置顶优先，然后按 sortOrder 升序）
     *
     * 供投资人端按 X-Shop-Id 查推荐产品使用。
     */
    public List<Long> listProductIdsByShop(Long shopId) {
        return shelfItemRepository
                .findByShopIdAndItemTypeAndDeletedOrderByIsTopDescTopTimeDescSortOrderAsc(shopId, "PRODUCT", 0)
                .stream()
                .map(ShelfItem::getItemId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询指定店铺上架的内容 ID 列表（置顶优先，然后按 sortOrder 升序）
     *
     * 供投资人端按 X-Shop-Id 查推荐内容使用。
     */
    public List<Long> listContentIdsByShop(Long shopId) {
        return shelfItemRepository
                .findByShopIdAndItemTypeAndDeletedOrderByIsTopDescTopTimeDescSortOrderAsc(shopId, "CONTENT", 0)
                .stream()
                .map(ShelfItem::getItemId)
                .collect(java.util.stream.Collectors.toList());
    }

    // ==================== 产品货架 ====================

    /**
     * 查询产品货架列表
     */
    public List<ShelfProductVO> listProductShelf(Long shopId) {
        // SHOP_ADMIN 只能查看自己店铺的货架；INVESTOR 使用传入的 shopId
        String role = SecurityUtils.getCurrentUserRole();
        if ("SHOP_ADMIN".equals(role)) {
            Long currentUserShopId = SecurityUtils.getCurrentUserShopId();
            if (currentUserShopId != null) {
                shopId = currentUserShopId;
            }
        }
        // INVESTOR 角色直接使用前端传入的 shopId，不做覆盖
        List<ShelfItem> items = shelfItemRepository.findByShopIdAndItemTypeAndDeletedOrderByIsTopDescTopTimeDescSortOrderAsc(
                shopId, "PRODUCT", 0);
        return buildProductShelfList(items);
    }

    /**
     * 添加产品到货架
     */
    @Transactional
    public void addProductToShelf(Long shopId, Long productId, String tags) {
        // 校验产品是否存在
        Product product = productRepository.findByIdAndDeleted(productId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND));
        // 校验产品是否已上架
        if (shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "PRODUCT", productId, 0).isPresent()) {
            throw new BizException(ErrorCode.BIZ_SHELF_ITEM_EXISTS);
        }

        ShelfItem item = new ShelfItem();
        item.setShopId(shopId);
        item.setItemType("PRODUCT");
        item.setItemId(productId);
        item.setSortOrder(getNextSortOrder(shopId, "PRODUCT"));
        item.setIsTop(0);
        item.setTags(tags);
        item.setAddedAt(LocalDateTime.now());
        shelfItemRepository.save(item);
    }

    /**
     * 批量更新产品排序号（只更新非置顶项，置顶项保持原有排序值）
     */
    @Transactional
    public void updateProductOrder(Long shopId, List<Long> productIds) {
        // 查询置顶项数量，非置顶项的 sort_order 从置顶项数量+1开始编号
        List<ShelfItem> allItems = shelfItemRepository.findByShopIdAndItemTypeAndDeletedOrderByIsTopDescTopTimeDescSortOrderAsc(shopId, "PRODUCT", 0);
        int pinnedCount = (int) allItems.stream().filter(item -> item.getIsTop() == 1).count();
        
        final int[] order = {pinnedCount + 1};
        for (Long itemId : productIds) {
            shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "PRODUCT", itemId, 0)
                    .ifPresent(innerItem -> {
                        // 只更新非置顶项的 sort_order
                        if (innerItem.getIsTop() == 0) {
                            innerItem.setSortOrder(order[0]);
                            shelfItemRepository.save(innerItem);
                            order[0]++;
                        }
                    });
        }
    }

    /**
     * 切换产品置顶状态
     * 并发处理：置顶优先（如果同时有置顶和取消置顶请求，置顶生效）
     */
    @Transactional
    public void toggleProductRecommend(Long shopId, Long productId) {
        ShelfItem item = shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "PRODUCT", productId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND));
        // 并发处理：置顶优先
        // 如果当前已是置顶状态，且收到置顶请求（而非取消），保持置顶
        // 如果当前非置顶，收到置顶请求，则置顶
        if (item.getIsTop() == 0) {
            // 当前非置顶，设置置顶，记录置顶时间
            item.setIsTop(1);
            item.setTopTime(LocalDateTime.now());
            shelfItemRepository.save(item);
        }
        // 如果当前已置顶，取消置顶请求会被忽略（置顶优先）
        // 这样处理并发时，置顶请求总是生效，取消置顶请求可能被忽略
    }

    /**
     * 更新产品标签
     */
    @Transactional
    public void updateProductTags(Long shopId, Long productId, String tags) {
        ShelfItem item = shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "PRODUCT", productId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND));
        item.setTags(tags);
        shelfItemRepository.save(item);
    }

    /**
     * 更新内容标签
     */
    @Transactional
    public void updateContentTags(Long shopId, Long contentId, String tags) {
        ShelfItem item = shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "CONTENT", contentId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_CONTENT_NOT_FOUND));
        item.setTags(tags);
        shelfItemRepository.save(item);
    }

    /**
     * 从货架移除产品
     */
    @Transactional
    public void removeProductFromShelf(Long shopId, Long productId) {
        ShelfItem item = shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "PRODUCT", productId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND));
        item.setDeleted(1);
        shelfItemRepository.save(item);
    }

    // ==================== 内容推荐 ====================

    /**
     * 查询内容推荐列表
     */
    public List<ShelfContentVO> listContentShelf(Long shopId) {
        // SHOP_ADMIN 只能查看自己店铺的内容推荐；INVESTOR 使用传入的 shopId
        String role = SecurityUtils.getCurrentUserRole();
        if ("SHOP_ADMIN".equals(role)) {
            Long currentUserShopId = SecurityUtils.getCurrentUserShopId();
            if (currentUserShopId != null) {
                shopId = currentUserShopId;
            }
        }
        // INVESTOR 角色直接使用前端传入的 shopId，不做覆盖
        List<ShelfItem> items = shelfItemRepository.findByShopIdAndItemTypeAndDeletedOrderByIsTopDescTopTimeDescSortOrderAsc(
                shopId, "CONTENT", 0);
        return buildContentShelfList(items);
    }

    /**
     * 添加内容到推荐
     */
    @Transactional
    public void addContentToShelf(Long shopId, Long contentId, String tags) {
        // 校验内容是否存在
        Content content = contentRepository.findByIdAndDeleted(contentId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_CONTENT_NOT_FOUND));
        // 校验内容是否已上架
        if (shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "CONTENT", contentId, 0).isPresent()) {
            throw new BizException(ErrorCode.BIZ_SHELF_CONTENT_EXISTS);
        }

        ShelfItem item = new ShelfItem();
        item.setShopId(shopId);
        item.setItemType("CONTENT");
        item.setItemId(contentId);
        item.setSortOrder(getNextSortOrder(shopId, "CONTENT"));
        item.setIsTop(0);
        item.setTags(tags);
        item.setAddedAt(LocalDateTime.now());
        shelfItemRepository.save(item);
    }

    /**
     * 批量更新内容排序号（只更新非置顶项，置顶项保持原有排序值）
     */
    @Transactional
    public void updateContentOrder(Long shopId, List<Long> contentIds) {
        // 查询置顶项数量，非置顶项的 sort_order 从置顶项数量+1开始编号
        List<ShelfItem> allItems = shelfItemRepository.findByShopIdAndItemTypeAndDeletedOrderByIsTopDescTopTimeDescSortOrderAsc(shopId, "CONTENT", 0);
        int pinnedCount = (int) allItems.stream().filter(item -> item.getIsTop() == 1).count();
        
        final int[] order = {pinnedCount + 1};
        for (Long itemId : contentIds) {
            shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "CONTENT", itemId, 0)
                    .ifPresent(innerItem -> {
                        // 只更新非置顶项的 sort_order
                        if (innerItem.getIsTop() == 0) {
                            innerItem.setSortOrder(order[0]);
                            shelfItemRepository.save(innerItem);
                            order[0]++;
                        }
                    });
        }
    }

    /**
     * 切换内容置顶状态
     */
    @Transactional
    public void toggleContentRecommend(Long shopId, Long contentId) {
        ShelfItem item = shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "CONTENT", contentId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_CONTENT_NOT_FOUND));
        if (item.getIsTop() == 1) {
            // 取消置顶
            item.setIsTop(0);
            item.setTopTime(null);
        } else {
            // 设置置顶，记录置顶时间
            item.setIsTop(1);
            item.setTopTime(LocalDateTime.now());
        }
        shelfItemRepository.save(item);
    }

    /**
     * 从推荐移除内容
     */
    @Transactional
    public void removeContentFromShelf(Long shopId, Long contentId) {
        ShelfItem item = shelfItemRepository.findByShopIdAndItemTypeAndItemIdAndDeleted(shopId, "CONTENT", contentId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_CONTENT_NOT_FOUND));
        item.setDeleted(1);
        shelfItemRepository.save(item);
    }

    // ==================== 内部方法 ====================

    private List<ShelfProductVO> buildProductShelfList(List<ShelfItem> items) {
        List<Long> itemIds = items.stream().map(ShelfItem::getItemId).collect(java.util.stream.Collectors.toList());
        Map<Long, Product> productMapById = itemIds.isEmpty() ? java.util.Collections.emptyMap()
                : productRepository.findAllById(itemIds).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<ShelfProductVO> result = new ArrayList<>();
        for (ShelfItem item : items) {
            ShelfProductVO vo = new ShelfProductVO();
            vo.setId(item.getId());
            vo.setShelfItemId(item.getId());
            vo.setShopId(item.getShopId());
            vo.setItemId(item.getItemId());
            vo.setItemType(item.getItemType());
            vo.setSortOrder(item.getSortOrder());
            vo.setIsTop(item.getIsTop());
            vo.setTags(item.getTags());

            Product product = productMapById.get(item.getItemId());
            if (product != null) {
                ShelfProductVO.ProductInfo info = new ShelfProductVO.ProductInfo();
                info.setId(product.getId());
                info.setName(product.getName());
                info.setType(product.getType());
                info.setDescription(product.getDescription());
                info.setAumMin(product.getAumMin());
                info.setAumMax(product.getAumMax());
                info.setExpectedReturn(product.getExpectedReturn());
                info.setRiskLevel(product.getRiskLevel());
                info.setDuration(product.getDuration());
                info.setStatus(product.getStatus());
                info.setCoverImage(product.getCoverImage());
                info.setDetailUrl(product.getDetailUrl());
                info.setAttr1(product.getAttr1());
                info.setAttr2(product.getAttr2());
                info.setViewCount(product.getViewCount() != null ? product.getViewCount() : 0);
                vo.setProduct(info);
            } else {
                vo.setProduct(null);
            }

            result.add(vo);
        }
        return result;
    }

    private List<ShelfContentVO> buildContentShelfList(List<ShelfItem> items) {
        List<Long> itemIds = items.stream().map(ShelfItem::getItemId).collect(java.util.stream.Collectors.toList());
        Map<Long, Content> contentMapById = itemIds.isEmpty() ? java.util.Collections.emptyMap()
                : contentRepository.findAllById(itemIds).stream()
                .collect(java.util.stream.Collectors.toMap(Content::getId, c -> c));

        List<ShelfContentVO> result = new ArrayList<>();
        for (ShelfItem item : items) {
            ShelfContentVO vo = new ShelfContentVO();
            vo.setId(item.getId());
            vo.setShelfItemId(item.getId());
            vo.setShopId(item.getShopId());
            vo.setItemId(item.getItemId());
            vo.setItemType(item.getItemType());
            vo.setSortOrder(item.getSortOrder());
            vo.setIsTop(item.getIsTop());
            vo.setTags(item.getTags());

            Content content = contentMapById.get(item.getItemId());
            if (content != null) {
                ShelfContentVO.ContentInfo info = new ShelfContentVO.ContentInfo();
                info.setId(content.getId());
                info.setTitle(content.getTitle());
                info.setType(content.getType());
                info.setSummary(content.getSummary());
                info.setCoverImage(content.getCoverImage());
                info.setContentUrl(content.getContentUrl());
                info.setViewCount(content.getViewCount());
                info.setSource(content.getSource());
                info.setStatus(content.getStatus());
                info.setCreatedAt(content.getCreatedAt());
                info.setUpdatedAt(content.getUpdatedAt());
                info.setPublishedAt(content.getPublishedAt());
                vo.setContent(info);
            } else {
                vo.setContent(null);
            }

            result.add(vo);
        }
        return result;
    }

    private int getNextSortOrder(Long shopId, String itemType) {
        List<ShelfItem> existing = shelfItemRepository.findByShopIdAndItemTypeAndDeletedOrderBySortOrderAsc(
                shopId, itemType, 0);
        return existing.size() + 1;
    }
}
