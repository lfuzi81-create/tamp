package com.tamp.org.repository;

import com.tamp.org.entity.OfficeSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfficeSelectionRepository extends JpaRepository<OfficeSelection, Long> {

    List<OfficeSelection> findByOfficeIdAndDeletedOrderBySortOrderAsc(Long officeId, Integer deleted);

    List<OfficeSelection> findByOfficeIdAndItemTypeAndDeletedOrderBySortOrderAsc(Long officeId, String itemType, Integer deleted);

    Optional<OfficeSelection> findByOfficeIdAndItemTypeAndItemIdAndDeleted(Long officeId, String itemType, Long itemId, Integer deleted);

    Optional<OfficeSelection> findByIdAndDeleted(Long id, Integer deleted);

    void deleteByOfficeIdAndItemTypeAndDeleted(Long officeId, String itemType, Integer deleted);
}
