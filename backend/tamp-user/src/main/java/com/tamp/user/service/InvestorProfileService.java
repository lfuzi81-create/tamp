package com.tamp.user.service;

import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.user.entity.InvestorProfile;
import com.tamp.user.repository.InvestorProfileRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestorProfileService {

    private final InvestorProfileRepository investorProfileRepository;
    private final JdbcTemplate jdbcTemplate;

    public InvestorProfileService(InvestorProfileRepository investorProfileRepository,
                                  JdbcTemplate jdbcTemplate) {
        this.investorProfileRepository = investorProfileRepository;
        this.jdbcTemplate = jdbcTemplate;
    }


    private void syncClientDisplayName(Long userId, String nickname) {
        if (!StringUtils.hasText(nickname)) {
            return;
        }
        jdbcTemplate.update(
                "UPDATE sys_user SET real_name = ?, updated_at = NOW() WHERE id = ? AND is_deleted = 0",
                nickname,
                userId
        );
        String phone = jdbcTemplate.query(
                "SELECT phone FROM sys_user WHERE id = ? AND is_deleted = 0",
                rs -> rs.next() ? rs.getString(1) : null,
                userId
        );
        if (phone != null) {
            jdbcTemplate.update(
                    "UPDATE biz_client SET name = ?, updated_at = NOW() WHERE phone = ? AND is_deleted = 0",
                    nickname,
                    phone
            );
        }
    }


    public InvestorProfile getByUserId(Long userId) {
        return investorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
    }

    @Transactional
    public InvestorProfile createOrUpdate(Long userId, String nickname, String avatarUrl) {
        InvestorProfile profile = investorProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    InvestorProfile p = new InvestorProfile();
                    p.setUserId(userId);
                    return p;
                });

        if (nickname != null) {
            profile.setNickname(nickname);
        }
        if (avatarUrl != null) {
            profile.setAvatarUrl(avatarUrl);
        }

        // 检查资料是否完善
        profile.setProfileCompleted(
                profile.getNickname() != null && !profile.getNickname().isBlank()
                && profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()
        );

        InvestorProfile saved = investorProfileRepository.save(profile);
        if (nickname != null && !nickname.isBlank()) {
            syncClientDisplayName(userId, nickname);
        }
        return saved;
    }

    @Transactional
    public void completeFirstLogin(Long userId) {
        InvestorProfile profile = investorProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    InvestorProfile p = new InvestorProfile();
                    p.setUserId(userId);
                    return p;
                });

        profile.setFirstLoginDone(true);
        investorProfileRepository.save(profile);
    }

    public boolean checkProfileCompleted(Long userId) {
        return investorProfileRepository.findByUserId(userId)
                .map(InvestorProfile::getProfileCompleted)
                .orElse(false);
    }
}
