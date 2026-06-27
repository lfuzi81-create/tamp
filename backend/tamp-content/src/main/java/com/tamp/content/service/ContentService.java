package com.tamp.content.service;

import com.tamp.content.entity.Content;
import com.tamp.content.entity.ContentCategory;
import com.tamp.content.repository.ContentRepository;
import com.tamp.content.repository.ContentCategoryRepository;
import com.tamp.common.exception.BizException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentCategoryRepository contentCategoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public ContentService(ContentRepository contentRepository, ContentCategoryRepository contentCategoryRepository, JdbcTemplate jdbcTemplate) {
        this.contentRepository = contentRepository;
        this.contentCategoryRepository = contentCategoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<Content> listContents(String keyword, Long categoryId, Pageable pageable) {
        if (StringUtils.hasText(keyword)) {
            return contentRepository.findByNameContainingIgnoreCaseAndDeleted(keyword, 0, pageable);
        } else if (categoryId != null) {
            return contentRepository.findByCategoryIdAndDeleted(categoryId, 0, pageable);
        }
        return contentRepository.findAll(pageable);
    }

    /**
     * 按 ID 列表批量查询内容（用于投资人端按店铺货架查推荐内容）
     *
     * 不分页、不做 keyword 过滤，仅按 id 集合返回未删除的内容。
     * 调用方负责按入参顺序自行排序。
     */
    public List<Content> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return contentRepository.findAllById(ids).stream()
                .filter(c -> c.getDeleted() != null && c.getDeleted() == 0)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询指定店铺上架的内容 ID 列表（按 sortOrder 升序）
     *
     * 供投资人端按 X-Shop-Id 查推荐内容使用。
     * 直接查 biz_shelf_item 表，避免依赖 tamp-shelf 模块（否则会形成循环依赖）。
     */
    public List<Long> listContentIdsByShop(Long shopId) {
        if (shopId == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT item_id FROM biz_shelf_item WHERE shop_id = ? AND item_type = 'CONTENT' AND is_deleted = 0 ORDER BY is_top DESC, top_time DESC, sort_order ASC, id ASC";
        return jdbcTemplate.queryForList(sql, Long.class, shopId);
    }

    public Content getContent(Long id) {
        return contentRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(com.tamp.common.constants.ErrorCode.BIZ_CONTENT_NOT_FOUND));
    }

    @Transactional
    public Content createContent(Content content) {
        content.setDeleted(0);
        if (content.getStatus() == null) {
            content.setStatus(1);
        }
        return contentRepository.save(content);
    }

    @Transactional
    public Content updateContent(Long id, Content content) {
        Content existing = getContent(id);
        if (content.getTitle() != null) existing.setTitle(content.getTitle());
        if (content.getCategoryId() != null) existing.setCategoryId(content.getCategoryId());
        if (content.getType() != null) existing.setType(content.getType());
        if (content.getSummary() != null) existing.setSummary(content.getSummary());
        if (content.getCoverImage() != null) existing.setCoverImage(content.getCoverImage());
        if (content.getContentUrl() != null) existing.setContentUrl(content.getContentUrl());
        if (content.getViewCount() != null) existing.setViewCount(content.getViewCount());
        if (content.getSource() != null) existing.setSource(content.getSource());
        if (content.getStatus() != null) existing.setStatus(content.getStatus());
        if (content.getPublishedAt() != null) existing.setPublishedAt(content.getPublishedAt());
        return contentRepository.save(existing);
    }

    @Transactional
    public void deleteContent(Long id) {
        Content content = getContent(id);
        content.setDeleted(1);
        contentRepository.save(content);
    }

    @Transactional
    public void toggleStatus(Long id, Integer status) {
        Content content = getContent(id);
        content.setStatus(status);
        contentRepository.save(content);
    }

    public List<ContentCategory> listCategories() {
        return contentCategoryRepository.findAll();
    }

    @Transactional
    public ContentCategory createCategory(ContentCategory category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        category.setDeleted(0);
        return contentCategoryRepository.save(category);
    }

    @Transactional
    public ContentCategory updateCategory(Long id, ContentCategory category) {
        ContentCategory existing = contentCategoryRepository.findById(id)
                .orElseThrow(() -> new BizException(com.tamp.common.constants.ErrorCode.NOT_FOUND));
        if (category.getName() != null) {
            existing.setName(category.getName());
        }
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        if (category.getStatus() != null) {
            existing.setStatus(category.getStatus());
        }
        return contentCategoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        contentCategoryRepository.deleteById(id);
    }

    public Map<String, Object> getShelfShops(Long contentId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> shops = new ArrayList<>();

        String sql = """
            SELECT
                s.id AS shopId,
                s.name AS shopName,
                s.total_aum AS shopAum,
                COUNT(DISTINCT t.client_id) AS customerCount
            FROM biz_shelf_item si
            JOIN biz_shop s ON si.shop_id = s.id AND s.is_deleted = 0
            LEFT JOIN biz_client_timeline t ON t.event_type = 'VIEW_CONTENT'
              AND t.target_id = ?
              AND EXISTS (
                SELECT 1 FROM biz_client c
                WHERE c.id = t.client_id AND c.shop_id = s.id AND c.is_deleted = 0
              )
            WHERE si.item_type = 'CONTENT'
              AND si.item_id = ?
              AND si.is_deleted = 0
            GROUP BY s.id, s.name, s.total_aum
            ORDER BY si.sort_order ASC, s.id ASC
            """;

        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> shop = new HashMap<>();
            shop.put("shopId", rs.getLong("shopId"));
            shop.put("shopName", rs.getString("shopName"));
            BigDecimal aum = rs.getBigDecimal("shopAum");
            shop.put("shopAum", aum != null ? aum.setScale(2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO.setScale(2));
            shop.put("customerCount", rs.getInt("customerCount"));
            shops.add(shop);
        }, contentId, contentId);

        int totalCustomers = shops.stream()
                .mapToInt(s -> (Integer) s.get("customerCount"))
                .sum();
        BigDecimal totalAum = shops.stream()
                .map(s -> (BigDecimal) s.get("shopAum"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("shops", shops);
        result.put("totalCustomers", totalCustomers);
        result.put("totalAum", totalAum);
        result.put("shopCount", shops.size());

        return result;
    }
}
