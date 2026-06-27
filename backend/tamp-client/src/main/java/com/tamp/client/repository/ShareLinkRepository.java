package com.tamp.client.repository;

import com.tamp.client.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    Optional<ShareLink> findByShortCodeAndDeleted(String shortCode, Integer deleted);

    Optional<ShareLink> findByIdAndDeleted(Long id, Integer deleted);

    List<ShareLink> findBySharerUserIdAndDeletedOrderByCreatedAtDesc(Long sharerUserId, Integer deleted);

    Long countBySharerUserIdAndDeleted(Long sharerUserId, Integer deleted);
}
