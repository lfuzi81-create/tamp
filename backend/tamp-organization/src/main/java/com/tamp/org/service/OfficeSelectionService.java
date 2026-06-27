package com.tamp.org.service;

import com.tamp.org.entity.OfficeSelection;
import com.tamp.org.repository.OfficeSelectionRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfficeSelectionService {

    private final OfficeSelectionRepository officeSelectionRepository;

    public OfficeSelectionService(OfficeSelectionRepository officeSelectionRepository) {
        this.officeSelectionRepository = officeSelectionRepository;
    }

    /**
     * 获取家办所有选品
     */
    public List<OfficeSelection> getByOfficeId(Long officeId) {
        return officeSelectionRepository.findByOfficeIdAndDeletedOrderBySortOrderAsc(officeId, 0);
    }

    /**
     * 按类型获取家办选品
     */
    public List<OfficeSelection> getByOfficeIdAndType(Long officeId, String itemType) {
        return officeSelectionRepository.findByOfficeIdAndItemTypeAndDeletedOrderBySortOrderAsc(officeId, itemType, 0);
    }

    /**
     * 添加选品
     */
    @Transactional
    public OfficeSelection addSelection(Long officeId, String itemType, Long itemId) {
        officeSelectionRepository.findByOfficeIdAndItemTypeAndItemIdAndDeleted(officeId, itemType, itemId, 0)
                .ifPresent(s -> {
                    throw new BizException(ErrorCode.BIZ_OPERATION_FAILED.getCode(), "该选品已存在");
                });
        OfficeSelection selection = new OfficeSelection();
        selection.setOfficeId(officeId);
        selection.setItemType(itemType);
        selection.setItemId(itemId);
        selection.setDeleted(0);
        return officeSelectionRepository.save(selection);
    }

    /**
     * 移除选品
     */
    @Transactional
    public void removeSelection(Long officeId, String itemType, Long itemId) {
        OfficeSelection selection = officeSelectionRepository
                .findByOfficeIdAndItemTypeAndItemIdAndDeleted(officeId, itemType, itemId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        selection.setDeleted(1);
        officeSelectionRepository.save(selection);
    }

    /**
     * 批量更新选品
     */
    @Transactional
    public void batchUpdate(Long officeId, String itemType, List<Long> itemIds) {
        // 先软删除该家办该类型的所有选品
        List<OfficeSelection> existing = officeSelectionRepository
                .findByOfficeIdAndItemTypeAndDeletedOrderBySortOrderAsc(officeId, itemType, 0);
        for (OfficeSelection s : existing) {
            s.setDeleted(1);
            officeSelectionRepository.save(s);
        }
        // 重新添加
        int sortOrder = 0;
        for (Long itemId : itemIds) {
            OfficeSelection selection = new OfficeSelection();
            selection.setOfficeId(officeId);
            selection.setItemType(itemType);
            selection.setItemId(itemId);
            selection.setSortOrder(sortOrder++);
            selection.setDeleted(0);
            officeSelectionRepository.save(selection);
        }
    }

    /**
     * 切换推荐状态
     */
    @Transactional
    public OfficeSelection toggleRecommend(Long id) {
        OfficeSelection selection = officeSelectionRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        selection.setIsRecommended(!selection.getIsRecommended());
        return officeSelectionRepository.save(selection);
    }
}
