package com.tamp.client.repository;

import com.tamp.client.entity.ClientAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClientAssetRepository extends JpaRepository<ClientAsset, Long>, JpaSpecificationExecutor<ClientAsset> {

    Optional<ClientAsset> findByIdAndDeleted(Long id, Integer deleted);

    Page<ClientAsset> findByClientIdAndDeleted(Long clientId, Integer deleted, Pageable pageable);

    Page<ClientAsset> findByInvestorIdAndDeleted(Long investorId, Integer deleted, Pageable pageable);

    @Query("SELECT SUM(c.amount) FROM ClientAsset c WHERE c.investorId = ?1 AND c.deleted = ?2")
    Double sumAmountByInvestorIdAndDeleted(Long investorId, Integer deleted);
}
