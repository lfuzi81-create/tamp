package com.tamp.content.controller;

import com.tamp.content.entity.KnowledgeArticle;
import com.tamp.content.entity.KnowledgeCategory;
import com.tamp.content.service.KnowledgeService;
import com.tamp.common.dto.PageResult;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/articles")
    public Result<PageResult<KnowledgeArticle>> listArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<KnowledgeArticle> result = knowledgeService.listArticles(keyword, categoryId, pageable);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/articles/{id}")
    public Result<KnowledgeArticle> getArticle(@PathVariable Long id) {
        boolean countView = "INVESTOR".equals(SecurityUtils.getCurrentUserRole());
        return Result.ok(knowledgeService.getArticle(id, countView));
    }

    @PostMapping("/articles")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<KnowledgeArticle> createArticle(@RequestBody KnowledgeArticle article) {
        return Result.ok(knowledgeService.createArticle(article));
    }

    @PutMapping("/articles/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<KnowledgeArticle> updateArticle(@PathVariable Long id, @RequestBody KnowledgeArticle article) {
        return Result.ok(knowledgeService.updateArticle(id, article));
    }

    @DeleteMapping("/articles/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> deleteArticle(@PathVariable Long id) {
        knowledgeService.deleteArticle(id);
        return Result.ok();
    }

    @PatchMapping("/articles/{id}/status")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        knowledgeService.toggleStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<KnowledgeCategory>> listCategories() {
        return Result.ok(knowledgeService.listCategories());
    }

    @PostMapping("/categories")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<KnowledgeCategory> createCategory(@RequestBody KnowledgeCategory category) {
        normalizeKnowledgeCategoryDefaults(category);
        return Result.ok(knowledgeService.createCategory(category));
    }

    @PutMapping("/categories/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<KnowledgeCategory> updateCategory(@PathVariable Long id, @RequestBody KnowledgeCategory category) {
        return Result.ok(knowledgeService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<Void> deleteCategory(@PathVariable Long id) {
        knowledgeService.deleteCategory(id);
        return Result.ok();
    }

    @GetMapping("/articles/{id}/shelf-shops")
    public Result<Map<String, Object>> getShelfShops(@PathVariable Long id) {
        return Result.ok(knowledgeService.getShelfShops(id));
    }

    private void normalizeKnowledgeCategoryDefaults(KnowledgeCategory category) {
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
