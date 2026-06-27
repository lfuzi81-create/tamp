package com.tamp.client.repository;

import com.tamp.client.entity.ClientTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientTagRepository extends JpaRepository<ClientTag, Long> {

    List<ClientTag> findByClientIdOrderByCreatedAtDesc(Long clientId);

    void deleteByClientIdAndId(Long clientId, Long tagId);

    @Query("SELECT DISTINCT t.tagName FROM ClientTag t ORDER BY t.tagName")
    List<String> findAllDistinctTagNames();

    @Query("SELECT t.clientId FROM ClientTag t WHERE t.tagName = :tagName")
    List<Long> findClientIdsByTagName(String tagName);
}
