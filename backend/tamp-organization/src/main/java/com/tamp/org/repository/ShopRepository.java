package com.tamp.org.repository;

import com.tamp.org.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByOfficeIdAndDeletedOrderByCreatedAtDesc(Long officeId, Integer deleted);

    Page<Shop> findByNameContainingAndOfficeIdAndStatusAndDeleted(String name, Long officeId, Integer status, Integer deleted, Pageable pageable);

    Page<Shop> findByNameContainingAndOfficeIdAndDeleted(String name, Long officeId, Integer deleted, Pageable pageable);

    Page<Shop> findByNameContainingAndStatusAndDeleted(String name, Integer status, Integer deleted, Pageable pageable);

    Page<Shop> findByOfficeIdAndStatusAndDeleted(Long officeId, Integer status, Integer deleted, Pageable pageable);

    Page<Shop> findByNameContainingAndDeleted(String name, Integer deleted, Pageable pageable);

    Page<Shop> findByOfficeIdAndDeleted(Long officeId, Integer deleted, Pageable pageable);

    Page<Shop> findByStatusAndDeleted(Integer status, Integer deleted, Pageable pageable);

    Page<Shop> findByDeleted(Integer deleted, Pageable pageable);

    Optional<Shop> findByIdAndDeleted(Long id, Integer deleted);

    List<Shop> findByOfficeIdAndDeleted(Long officeId, Integer deleted);

    Long countByDeleted(Integer deleted);

    Long countByOfficeIdAndDeleted(Long officeId, Integer deleted);

    boolean existsByOfficeIdAndNameAndDeleted(Long officeId, String name, Integer deleted);

    Long countByStatusAndDeleted(Integer status, Integer deleted);
}
