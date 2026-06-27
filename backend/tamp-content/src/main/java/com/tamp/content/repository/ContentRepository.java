package com.tamp.content.repository;

import com.tamp.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    Page<Content> findByCategoryIdAndDeleted(Long categoryId, Integer deleted, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.deleted = :deleted")
    Page<Content> findByNameContainingIgnoreCaseAndDeleted(@Param("keyword") String keyword, @Param("deleted") Integer deleted, Pageable pageable);

    Optional<Content> findByIdAndDeleted(Long id, Integer deleted);

    Long countByDeleted(Integer deleted);
}
