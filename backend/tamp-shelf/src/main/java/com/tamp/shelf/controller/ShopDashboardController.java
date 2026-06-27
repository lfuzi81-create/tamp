package com.tamp.shelf.controller;

import com.tamp.client.entity.Client;
import com.tamp.client.entity.ClientTimeline;
import com.tamp.client.repository.ClientRepository;
import com.tamp.client.service.ClientService;
import com.tamp.client.repository.ClientTimelineRepository;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.util.SecurityUtils;
import com.tamp.content.entity.Content;
import com.tamp.content.repository.ContentRepository;
import com.tamp.product.entity.Product;
import com.tamp.product.repository.ProductRepository;
import com.tamp.shelf.entity.ShelfItem;
import com.tamp.shelf.repository.ShelfItemRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺端工作台控制器
 */
@RestController
@RequestMapping("/api/shop/dashboard")
@RequireRole("SHOP_ADMIN")
public class ShopDashboardController {

    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final ClientTimelineRepository clientTimelineRepository;
    private final ShelfItemRepository shelfItemRepository;
    private final ProductRepository productRepository;
    private final ContentRepository contentRepository;

    public ShopDashboardController(ClientRepository clientRepository,
                                   ClientService clientService,
                                   ClientTimelineRepository clientTimelineRepository,
                                   ShelfItemRepository shelfItemRepository,
                                   ProductRepository productRepository,
                                   ContentRepository contentRepository) {
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.clientTimelineRepository = clientTimelineRepository;
        this.shelfItemRepository = shelfItemRepository;
        this.productRepository = productRepository;
        this.contentRepository = contentRepository;
    }

    /**
     * 工作台汇总：店铺信息卡 + 四项指标（客户数、资产规模、产品数、内容数）
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary() {
        Long shopId = SecurityUtils.getCurrentShopId();
        Map<String, Object> summary = new HashMap<>();

        long clientCount = shopId != null ? clientRepository.countByShopIdAndDeleted(shopId, 0) : 0;
        summary.put("clientCount", clientCount);

        BigDecimal totalAum = shopId != null ? clientService.computeShopAuthorizedAum(shopId) : null;
        summary.put("totalAum", totalAum != null ? totalAum.doubleValue() : 0.0);

        long productCount = 0;
        long contentCount = 0;
        if (shopId != null) {
            List<ShelfItem> productItems = shelfItemRepository
                    .findByShopIdAndItemTypeAndDeletedOrderBySortOrderAsc(shopId, "PRODUCT", 0);
            productCount = productItems.size();
            List<ShelfItem> contentItems = shelfItemRepository
                    .findByShopIdAndItemTypeAndDeletedOrderBySortOrderAsc(shopId, "CONTENT", 0);
            contentCount = contentItems.size();
        }
        summary.put("productCount", productCount);
        summary.put("contentCount", contentCount);

        // 本月活跃客户数（最近30天有动态的客户）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long monthlyActive = shopId != null
                ? clientTimelineRepository.countByShopIdAndCreatedAtAfter(shopId, thirtyDaysAgo)
                : 0;
        summary.put("monthlyActive", monthlyActive);
        summary.put("activeThisMonth", monthlyActive);
        summary.put("activeClientCount", monthlyActive);

        // 本月新增客户数（最近30天创建的客户）
        long monthlyNew = shopId != null
                ? clientRepository.countByShopIdAndCreatedAtAfterAndDeleted(shopId, thirtyDaysAgo, 0)
                : 0;
        summary.put("monthlyNew", monthlyNew);
        summary.put("newThisMonth", monthlyNew);
        summary.put("newClientCount", monthlyNew);

        return Result.ok(summary);
    }

    /**
     * 待更新动态：查询最近7天未更新的货架项
     */
    @GetMapping("/pending-updates")
    public Result<List<Map<String, Object>>> getPendingUpdates() {
        Long shopId = SecurityUtils.getCurrentShopId();
        List<Map<String, Object>> result = new ArrayList<>();
        if (shopId == null) {
            return Result.ok(result);
        }

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<ShelfItem> pendingItems = shelfItemRepository
                .findByShopIdAndDeletedAndUpdatedAtBefore(shopId, 0, threshold);

        List<Long> productIds = pendingItems.stream()
                .filter(i -> "PRODUCT".equals(i.getItemType()))
                .map(ShelfItem::getItemId)
                .collect(Collectors.toList());
        List<Long> contentIds = pendingItems.stream()
                .filter(i -> "CONTENT".equals(i.getItemType()))
                .map(ShelfItem::getItemId)
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productIds.isEmpty() ? new HashMap<>()
                : productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Content> contentMap = contentIds.isEmpty() ? new HashMap<>()
                : contentRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(Content::getId, c -> c));

        for (ShelfItem item : pendingItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("itemType", item.getItemType());
            map.put("itemId", item.getItemId());
            map.put("updatedAt", item.getUpdatedAt());
            if ("PRODUCT".equals(item.getItemType())) {
                Product product = productMap.get(item.getItemId());
                if (product != null) {
                    map.put("name", product.getName());
                    map.put("status", product.getStatus());
                }
            } else if ("CONTENT".equals(item.getItemType())) {
                Content content = contentMap.get(item.getItemId());
                if (content != null) {
                    map.put("name", content.getTitle());
                    map.put("status", content.getStatus());
                }
            }
            result.add(map);
        }
        return Result.ok(result);
    }

    /**
     * 客户动态时间线：最近10条客户跟进记录
     */
    @GetMapping("/timeline")
    public Result<List<Map<String, Object>>> getTimeline() {
        Long shopId = SecurityUtils.getCurrentShopId();
        List<Map<String, Object>> result = new ArrayList<>();
        if (shopId == null) {
            return Result.ok(result);
        }

        List<ClientTimeline> timelines = clientTimelineRepository.findByShopIdOrderByCreatedAtDesc(shopId);
        int limit = Math.min(10, timelines.size());
        for (int i = 0; i < limit; i++) {
            ClientTimeline t = timelines.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("clientId", t.getClientId());
            map.put("eventType", t.getEventType());
            map.put("title", t.getTitle());
            map.put("content", t.getContent());
            map.put("eventTime", t.getEventTime());
            map.put("createdAt", t.getCreatedAt());
            map.put("createdBy", t.getCreatedBy());
            clientRepository.findById(t.getClientId())
                    .ifPresent(client -> map.put("clientName", clientService.resolveClientDisplayName(client)));
            if (!map.containsKey("clientName")) {
                map.put("clientName", "客户");
            }
            result.add(map);
        }
        return Result.ok(result);
    }
}
