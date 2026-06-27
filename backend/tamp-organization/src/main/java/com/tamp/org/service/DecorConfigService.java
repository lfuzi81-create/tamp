package com.tamp.org.service;

import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.org.entity.DecorConfig;
import com.tamp.org.repository.DecorConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DecorConfigService {

    private final DecorConfigRepository decorConfigRepository;

    public DecorConfigService(DecorConfigRepository decorConfigRepository) {
        this.decorConfigRepository = decorConfigRepository;
    }

    public DecorConfig getByShopId(Long shopId) {
        return decorConfigRepository.findByShopIdAndDeleted(shopId, 0).orElse(null);
    }

    public DecorConfig getByOfficeId(Long officeId) {
        return decorConfigRepository.findByOfficeIdAndDeleted(officeId, 0).orElse(null);
    }

    @Transactional
    public DecorConfig createOrUpdate(DecorConfig config) {
        config.setDeleted(0);
        return decorConfigRepository.save(config);
    }

    @Transactional
    public DecorConfig updateModules(Long id, String modulesJson) {
        DecorConfig config = getConfig(id);
        config.setModules(modulesJson);
        return decorConfigRepository.save(config);
    }

    @Transactional
    public DecorConfig updateNavigation(Long id, String navigationJson) {
        DecorConfig config = getConfig(id);
        config.setNavigation(navigationJson);
        return decorConfigRepository.save(config);
    }

    @Transactional
    public DecorConfig updateShelfSelections(Long id, String selectionsJson) {
        DecorConfig config = getConfig(id);
        config.setShelfSelections(selectionsJson);
        return decorConfigRepository.save(config);
    }

    private DecorConfig getConfig(Long id) {
        return decorConfigRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_DECOR_CONFIG_NOT_FOUND));
    }
}
