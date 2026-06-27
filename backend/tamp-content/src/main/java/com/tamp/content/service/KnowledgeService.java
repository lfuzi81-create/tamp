package com.tamp.content.service;

import com.tamp.content.entity.KnowledgeArticle;
import com.tamp.content.entity.KnowledgeCategory;
import com.tamp.content.repository.KnowledgeArticleRepository;
import com.tamp.content.repository.KnowledgeCategoryRepository;
import com.tamp.common.constants.ErrorCode;
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
public class KnowledgeService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final KnowledgeCategoryRepository knowledgeCategoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public KnowledgeService(KnowledgeArticleRepository knowledgeArticleRepository,
                            KnowledgeCategoryRepository knowledgeCategoryRepository,
                            JdbcTemplate jdbcTemplate) {
        this.knowledgeArticleRepository = knowledgeArticleRepository;
        this.knowledgeCategoryRepository = knowledgeCategoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<KnowledgeArticle> listArticles(String keyword, Long categoryId, Pageable pageable) {
        if (StringUtils.hasText(keyword)) {
            return knowledgeArticleRepository.findByTitleContainingIgnoreCaseAndDeleted(keyword, Integer.valueOf(0), pageable);
        } else if (categoryId != null) {
            return knowledgeArticleRepository.findByCategoryIdAndDeleted(categoryId, Integer.valueOf(0), pageable);
        }
        return knowledgeArticleRepository.findAll(pageable);
    }

    public KnowledgeArticle getArticle(Long id, boolean countView) {
        KnowledgeArticle article = findArticle(id);
        if (countView) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            knowledgeArticleRepository.save(article);
        }
        return article;
    }

    private KnowledgeArticle findArticle(Long id) {
        return knowledgeArticleRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_KNOWLEDGE_NOT_FOUND));
    }

    @Transactional
    public KnowledgeArticle createArticle(KnowledgeArticle article) {
        article.setDeleted(0);
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        return knowledgeArticleRepository.save(article);
    }

    @Transactional
    public KnowledgeArticle updateArticle(Long id, KnowledgeArticle article) {
        KnowledgeArticle existing = knowledgeArticleRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_KNOWLEDGE_NOT_FOUND));
        if (article.getTitle() != null) existing.setTitle(article.getTitle());
        if (article.getCategoryId() != null) existing.setCategoryId(article.getCategoryId());
        if (article.getSummary() != null) existing.setSummary(article.getSummary());
        if (article.getCoverImage() != null) existing.setCoverImage(article.getCoverImage());
        if (article.getContentText() != null) existing.setContentText(article.getContentText());
        if (article.getAttachmentUrl() != null) existing.setAttachmentUrl(article.getAttachmentUrl());
        if (article.getViewCount() != null) existing.setViewCount(article.getViewCount());
        if (article.getStatus() != null) existing.setStatus(article.getStatus());
        if (article.getIsUpdated() != null) existing.setIsUpdated(article.getIsUpdated());
        if (article.getPublishedAt() != null) existing.setPublishedAt(article.getPublishedAt());
        return knowledgeArticleRepository.save(existing);
    }

    @Transactional
    public void deleteArticle(Long id) {
        KnowledgeArticle article = findArticle(id);
        article.setDeleted(1);
        knowledgeArticleRepository.save(article);
    }

    @Transactional
    public void toggleStatus(Long id, Integer status) {
        KnowledgeArticle article = findArticle(id);
        article.setStatus(status);
        knowledgeArticleRepository.save(article);
    }

    public List<KnowledgeCategory> listCategories() {
        return knowledgeCategoryRepository.findAll();
    }

    @Transactional
    public KnowledgeCategory createCategory(KnowledgeCategory category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        category.setDeleted(0);
        return knowledgeCategoryRepository.save(category);
    }

    @Transactional
    public KnowledgeCategory updateCategory(Long id, KnowledgeCategory category) {
        KnowledgeCategory existing = knowledgeCategoryRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        if (category.getName() != null) existing.setName(category.getName());
        if (category.getSortOrder() != null) existing.setSortOrder(category.getSortOrder());
        if (category.getStatus() != null) existing.setStatus(category.getStatus());
        return knowledgeCategoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        knowledgeCategoryRepository.deleteById(id);
    }

    public Map<String, Object> getShelfShops(Long articleId) {
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
            LEFT JOIN biz_client_timeline t ON t.event_type = 'VIEW_KNOWLEDGE'
              AND t.target_id = ?
              AND EXISTS (
                SELECT 1 FROM biz_client c
                WHERE c.id = t.client_id AND c.shop_id = s.id AND c.is_deleted = 0
              )
            WHERE si.item_type = 'KNOWLEDGE'
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
        }, articleId, articleId);

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
