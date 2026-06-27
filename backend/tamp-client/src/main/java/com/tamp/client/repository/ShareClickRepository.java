package com.tamp.client.repository;

import com.tamp.client.entity.ShareClick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShareClickRepository extends JpaRepository<ShareClick, Long> {

    List<ShareClick> findByShareIdOrderByCreatedAtDesc(Long shareId);

    Long countByShareId(Long shareId);
}
