package com.tamp.content.repository;

import com.tamp.content.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Page<KnowledgeArticle> findByTitleContainingIgnoreCaseAndDeleted(String title, Integer deleted, Pageable pageable);

    Optional<KnowledgeArticle> findByIdAndDeleted(Long id, Integer deleted);

    Page<KnowledgeArticle> findByCategoryIdAndDeleted(Long categoryId, Integer deleted, Pageable pageable);
}
