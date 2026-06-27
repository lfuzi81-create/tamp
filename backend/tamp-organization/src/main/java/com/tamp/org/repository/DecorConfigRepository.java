package com.tamp.org.repository;

import com.tamp.org.entity.DecorConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DecorConfigRepository extends JpaRepository<DecorConfig, Long> {

    Optional<DecorConfig> findByShopIdAndDeleted(Long shopId, Integer deleted);

    Optional<DecorConfig> findByOfficeIdAndDeleted(Long officeId, Integer deleted);

    Optional<DecorConfig> findByIdAndDeleted(Long id, Integer deleted);
}
