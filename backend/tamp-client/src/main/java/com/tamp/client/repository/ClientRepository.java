package com.tamp.client.repository;

import com.tamp.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Page<Client> findByShopIdAndDeleted(Long shopId, Integer deleted, Pageable pageable);

    Page<Client> findByOfficeIdAndDeleted(Long officeId, Integer deleted, Pageable pageable);

    List<Client> findByOfficeIdAndDeleted(Long officeId, Integer deleted);

    Page<Client> findByNameContainingAndShopIdAndDeleted(String name, Long shopId, Integer deleted, Pageable pageable);

    Page<Client> findByNameContainingAndOfficeIdAndDeleted(String name, Long officeId, Integer deleted, Pageable pageable);

    Page<Client> findByNameContainingAndDeleted(String name, Integer deleted, Pageable pageable);

    Page<Client> findByDeleted(Integer deleted, Pageable pageable);

    Optional<Client> findByIdAndDeleted(Long id, Integer deleted);

    Optional<Client> findByPhoneAndDeleted(String phone, Integer deleted);

    // 投资人端按店+手机号查（同一手机号可属多店，所以必须带 shopId）
    Optional<Client> findByShopIdAndPhoneAndDeleted(Long shopId, String phone, Integer deleted);

    // 投资人端「历史访问店铺列表」：按手机号查所有店
    List<Client> findByPhoneAndDeletedOrderByUpdatedAtDesc(String phone, Integer deleted);

    Long countByShopIdAndDeleted(Long shopId, Integer deleted);

    Long countByDeleted(Integer deleted);

    Long countByOfficeIdAndDeleted(Long officeId, Integer deleted);

    long countByOfficeIdAndCreatedAtAfterAndDeleted(Long officeId, LocalDateTime createdAt, Integer deleted);

    long countByShopIdAndCreatedAtAfterAndDeleted(Long shopId, LocalDateTime createdAt, Integer deleted);

    @Query("SELECT SUM(c.aumTotal) FROM Client c WHERE c.deleted = :deleted")
    BigDecimal sumAumTotalByDeleted(@Param("deleted") Integer deleted);

    @Query("SELECT SUM(c.aumTotal) FROM Client c WHERE c.shopId = :shopId AND c.deleted = :deleted")
    BigDecimal sumAumTotalByShopIdAndDeleted(@Param("shopId") Long shopId, @Param("deleted") Integer deleted);

    @Query("SELECT SUM(c.aumTotal) FROM Client c WHERE c.officeId = :officeId AND c.deleted = :deleted")
    BigDecimal sumAumTotalByOfficeIdAndDeleted(@Param("officeId") Long officeId, @Param("deleted") Integer deleted);

    @Query(value = "SELECT DATE(c.created_at) AS date, c.shop_id AS shopId, s.name AS shopName, COUNT(*) AS cnt " +
            "FROM biz_client c " +
            "LEFT JOIN biz_shop s ON c.shop_id = s.id " +
            "WHERE c.created_at >= :startDate AND c.created_at < :endDate AND c.is_deleted = 0 " +
            "GROUP BY DATE(c.created_at), c.shop_id " +
            "ORDER BY date ASC", nativeQuery = true)
    List<Object[]> countDailyNewClientsByShop(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}
