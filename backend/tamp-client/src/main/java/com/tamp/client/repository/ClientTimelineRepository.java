package com.tamp.client.repository;

import com.tamp.client.entity.ClientTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClientTimelineRepository extends JpaRepository<ClientTimeline, Long> {

    List<ClientTimeline> findByClientIdOrderByCreatedAtDesc(Long clientId);

    @Query("SELECT t FROM ClientTimeline t, Client c WHERE t.clientId = c.id AND c.shopId = :shopId AND c.deleted = 0 ORDER BY t.createdAt DESC")
    List<ClientTimeline> findByShopIdOrderByCreatedAtDesc(@Param("shopId") Long shopId);

    @Query("SELECT COUNT(DISTINCT t.clientId) FROM ClientTimeline t, Client c WHERE t.clientId = c.id AND c.shopId = :shopId AND c.deleted = 0 AND t.createdAt >= :since")
    long countByShopIdAndCreatedAtAfter(@Param("shopId") Long shopId, @Param("since") LocalDateTime since);
}
