package com.tamp.content.repository;

import com.tamp.content.entity.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long> {

    List<ContentCategory> findByStatusOrderBySortOrderAsc(Integer status);
}
