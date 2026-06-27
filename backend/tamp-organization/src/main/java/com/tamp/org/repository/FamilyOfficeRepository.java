package com.tamp.org.repository;

import com.tamp.org.entity.FamilyOffice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyOfficeRepository extends JpaRepository<FamilyOffice, Long> {

    Page<FamilyOffice> findByNameContainingAndDeleted(String name, Integer deleted, Pageable pageable);

    Page<FamilyOffice> findByDeleted(Integer deleted, Pageable pageable);

    Optional<FamilyOffice> findByIdAndDeleted(Long id, Integer deleted);

    Page<FamilyOffice> findByStatus(Integer status, Pageable pageable);

    Page<FamilyOffice> findByStatusAndDeleted(Integer status, Integer deleted, Pageable pageable);

    Page<FamilyOffice> findByNameContainingAndStatusAndDeleted(String name, Integer status, Integer deleted, Pageable pageable);

    Long countByDeleted(Integer deleted);

    Long countByStatusAndDeleted(Integer status, Integer deleted);
}
