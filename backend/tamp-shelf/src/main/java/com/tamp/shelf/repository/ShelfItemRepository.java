package com.tamp.shelf.repository;

import com.tamp.shelf.entity.ShelfItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShelfItemRepository extends JpaRepository<ShelfItem, Long> {

    // 列表查询：置顶优先，置顶项按置顶时间降序（最新置顶在最前），非置顶项按 sortOrder 升序
    List<ShelfItem> findByShopIdAndItemTypeAndDeletedOrderByIsTopDescTopTimeDescSortOrderAsc(Long shopId, String itemType, Integer deleted);

    // 原有的按 sortOrder 排序（保留用于排序号计算等场景）
    List<ShelfItem> findByShopIdAndItemTypeAndDeletedOrderBySortOrderAsc(Long shopId, String itemType, Integer deleted);

    Optional<ShelfItem> findByShopIdAndItemTypeAndItemIdAndDeleted(Long shopId, String itemType, Long itemId, Integer deleted);

    void deleteByShopIdAndItemTypeAndItemId(Long shopId, String itemType, Long itemId);

    List<ShelfItem> findByShopIdAndDeletedAndUpdatedAtBefore(Long shopId, Integer deleted, LocalDateTime updatedAt);

    List<ShelfItem> findByShopIdAndDeleted(Long shopId, Integer deleted);

    long countByShopIdAndItemTypeAndDeleted(Long shopId, String itemType, Integer deleted);
}
