package com.tamp.org.repository;

import com.tamp.org.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Page<Staff> findByNameContainingAndRoleTypeAndDeleted(String name, String roleType, Integer deleted, Pageable pageable);

    Page<Staff> findByNameContainingAndDeleted(String name, Integer deleted, Pageable pageable);

    Page<Staff> findByRoleTypeAndDeleted(String roleType, Integer deleted, Pageable pageable);

    Page<Staff> findByDeleted(Integer deleted, Pageable pageable);

    Optional<Staff> findByIdAndDeleted(Long id, Integer deleted);

    Page<Staff> findByOfficeIdAndDeleted(Long officeId, Integer deleted, Pageable pageable);

    List<Staff> findByOfficeIdAndDeleted(Long officeId, Integer deleted);

    Page<Staff> findByShopIdAndDeleted(Long shopId, Integer deleted, Pageable pageable);

    long countByOfficeIdAndDeleted(Long officeId, Integer deleted);

    @Query("SELECT s FROM Staff s WHERE s.officeId = :officeId AND s.deleted = 0 "
            + "AND (:status IS NULL OR s.status = :status) "
            + "AND (:keyword IS NULL OR :keyword = '' OR s.name LIKE CONCAT('%', :keyword, '%') "
            + "OR s.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<Staff> findByOfficeFilters(@Param("officeId") Long officeId,
                                    @Param("keyword") String keyword,
                                    @Param("status") Integer status,
                                    Pageable pageable);
}
