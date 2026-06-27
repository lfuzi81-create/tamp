package com.tamp.content.controller;

import com.tamp.content.entity.Content;
import com.tamp.content.entity.ContentCategory;
import com.tamp.content.service.ContentService;
import com.tamp.content.controller.vo.ContentVO;
import com.tamp.common.dto.PageResult;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public Result<PageResult<ContentVO>> listContents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Content> result = contentService.listContents(keyword, categoryId, pageable);
        Page<ContentVO> voPage = result.map(this::toContentVO);
        return Result.ok(PageResult.of(voPage));
    }

    @PostMapping
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Content> createContent(@RequestBody Content content) {
        return Result.ok(contentService.createContent(content));
    }

    @PutMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Content> updateContent(@PathVariable Long id, @RequestBody Content content) {
        return Result.ok(contentService.updateContent(id, content));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return Result.ok();
    }

    @PatchMapping("/{id}/status")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        contentService.toggleStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<ContentCategory>> listCategories() {
        return Result.ok(contentService.listCategories());
    }

    @PostMapping("/categories")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<ContentCategory> createCategory(@RequestBody ContentCategory category) {
        normalizeCategoryDefaults(category);
        return Result.ok(contentService.createCategory(category));
    }

    @PutMapping("/categories/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<ContentCategory> updateCategory(@PathVariable Long id, @RequestBody ContentCategory category) {
        return Result.ok(contentService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> deleteCategory(@PathVariable Long id) {
        contentService.deleteCategory(id);
        return Result.ok();
    }

    /**
     * 投资人端推荐内容 —— GET /api/contents/recommend
     *
     * shopId 来源优先级（防伪造）：
     * 1. JWT 内的 shopId（SecurityUtils.getCurrentShopId，登录时签发，不可伪造）
     * 2. X-Shop-Id 请求头（仅当 JWT 无 shopId 时回退，向后兼容非投资人端）
     * 3. 无 shopId 时全局查 6 条（兜底）
     */
    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> getRecommendContents(
            @RequestHeader(value = "X-Shop-Id", required = false) Long headerShopId) {
        // 优先用 JWT 中的 shopId（防伪造），其次回退到请求头
        Long shopId = com.tamp.common.util.SecurityUtils.getCurrentShopId();
        if (shopId == null) {
            shopId = headerShopId;
        }
        List<Content> contents;
        if (shopId != null) {
            List<Long> contentIds = contentService.listContentIdsByShop(shopId);
            if (contentIds.isEmpty()) {
                return Result.ok(java.util.Collections.emptyList());
            }
            // 按货架返回的顺序展示，保留 sortOrder 顺序
            List<Content> fetched = contentService.listByIds(contentIds);
            java.util.Map<Long, Content> byId = fetched.stream()
                    .collect(Collectors.toMap(Content::getId, c -> c));
            contents = new ArrayList<>();
            for (Long cid : contentIds) {
                Content c = byId.get(cid);
                if (c != null) {
                    contents.add(c);
                }
            }
        } else {
            Pageable pageable = PageRequest.of(0, 6);
            Page<Content> result = contentService.listContents(null, null, pageable);
            contents = result.getContent();
        }

        List<Map<String, Object>> list = contents.stream()
                .map(this::toContentMap)
                .collect(Collectors.toList());

        return Result.ok(list);
    }

    private ContentVO toContentVO(Content c) {
        ContentVO vo = new ContentVO();
        vo.setId(c.getId());
        vo.setTitle(c.getTitle());
        vo.setCategoryId(c.getCategoryId());
        vo.setType(c.getType());
        vo.setSummary(c.getSummary());
        vo.setCoverImage(c.getCoverImage());
        vo.setContentUrl(c.getContentUrl());
        vo.setViewCount(c.getViewCount() != null ? c.getViewCount() : 0);
        vo.setSource(c.getSource());
        vo.setStatus(c.getStatus());
        vo.setPublishedAt(c.getPublishedAt());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());

        String type = c.getType() != null ? c.getType() : "ARTICLE";
        vo.setCategory(type);
        vo.setCategoryValue(type.toLowerCase());
        vo.setTags(c.getSource() != null
                ? java.util.Collections.singletonList(c.getSource())
                : java.util.Collections.singletonList("推荐"));
        vo.setIsNew(c.getPublishedAt() != null
                && c.getPublishedAt().isAfter(java.time.LocalDateTime.now().minusDays(7)));
        vo.setMeta(c.getSource() != null ? c.getSource() : "TAMP");
        vo.setInfo(c.getSummary());
        vo.setActionText("前往查看");
        return vo;
    }

    private Map<String, Object> toContentMap(Content c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("title", c.getTitle());
        map.put("categoryId", c.getCategoryId());
        map.put("type", c.getType());
        map.put("summary", c.getSummary());
        map.put("coverImage", c.getCoverImage());
        map.put("contentUrl", c.getContentUrl());
        map.put("viewCount", c.getViewCount() != null ? c.getViewCount() : 0);
        map.put("source", c.getSource());
        map.put("status", c.getStatus());
        map.put("publishedAt", c.getPublishedAt());
        map.put("createdAt", c.getCreatedAt());
        map.put("updatedAt", c.getUpdatedAt());

        // 前端 normalizeContent 期望的派生字段
        String type = c.getType() != null ? c.getType() : "ARTICLE";
        map.put("category", type);
        map.put("categoryValue", type.toLowerCase());
        map.put("tags", c.getSource() != null ? java.util.Collections.singletonList(c.getSource()) : java.util.Collections.singletonList("推荐"));
        map.put("isNew", c.getPublishedAt() != null && c.getPublishedAt().isAfter(java.time.LocalDateTime.now().minusDays(7)));
        map.put("meta", c.getSource() != null ? c.getSource() : "TAMP");
        map.put("info", c.getSummary());
        map.put("actionText", "前往查看");
        return map;
    }

    private void normalizeCategoryDefaults(ContentCategory category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getDeleted() == null) {
            category.setDeleted(0);
        }
    }
}
