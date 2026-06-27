package com.tamp.content.repository;

import com.tamp.content.entity.KnowledgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeCategoryRepository extends JpaRepository<KnowledgeCategory, Long> {

    List<KnowledgeCategory> findByStatusOrderBySortOrderAsc(Integer status);
}
