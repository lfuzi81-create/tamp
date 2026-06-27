package com.tamp.client.service;

import com.tamp.client.controller.vo.ShareStatsVO;
import com.tamp.client.entity.ShareClick;
import com.tamp.client.entity.ShareLink;
import com.tamp.client.repository.ShareClickRepository;
import com.tamp.client.repository.ShareLinkRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
public class ShareLinkService {

    private final ShareLinkRepository shareLinkRepository;
    private final ShareClickRepository shareClickRepository;

    public ShareLinkService(ShareLinkRepository shareLinkRepository, ShareClickRepository shareClickRepository) {
        this.shareLinkRepository = shareLinkRepository;
        this.shareClickRepository = shareClickRepository;
    }

    /**
     * 创建分享链接，生成短码
     * targetType 支持：product / content / page / office
     */
    @Transactional
    public ShareLink createShareLink(Long userId, Long officeId, String targetType, Long targetId, String targetName) {
        // targetType 校验：不能为空且必须在支持的类型范围内
        if (targetType == null || targetType.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "targetType不能为空");
        }
        Set<String> validTypes = Set.of("product", "content", "page", "office");
        if (!validTypes.contains(targetType.toLowerCase())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "targetType类型不合法，仅支持: product, content, page, office");
        }

        // 构造 JSON 载荷
        String payload = String.format("{\"t\":\"%s\",\"i\":%d,\"o\":%d,\"ts\":%d}",
                targetType, targetId, officeId != null ? officeId : 0, System.currentTimeMillis());
        // Base64 编码
        String encoded = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        // 安全字符替换：+ → -，/ → _，= → 空
        String shortCode = encoded.replace("+", "-").replace("/", "_").replace("=", "");

        ShareLink link = new ShareLink();
        link.setShortCode(shortCode);
        link.setSharerUserId(userId);
        link.setSharerOfficeId(officeId);
        link.setTargetType(targetType);
        link.setTargetId(targetId);
        link.setTargetName(targetName);
        link.setClickCount(0);
        link.setDeleted(0);
        return shareLinkRepository.save(link);
    }

    /**
     * 解析短码，返回分享信息
     */
    public ShareLink resolveShareLink(String shortCode) {
        return shareLinkRepository.findByShortCodeAndDeleted(shortCode, 0)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
    }

    /**
     * 记录点击
     */
    @Transactional
    public void recordClick(Long shareId, Long visitorUserId, String visitorPhone,
                            Long sourceOfficeId, String ip, String ua) {
        ShareLink link = shareLinkRepository.findByIdAndDeleted(shareId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        link.setClickCount(link.getClickCount() + 1);
        shareLinkRepository.save(link);

        ShareClick click = new ShareClick();
        click.setShareId(shareId);
        click.setVisitorUserId(visitorUserId);
        click.setVisitorPhone(visitorPhone);
        click.setSourceOfficeId(sourceOfficeId);
        click.setIpAddress(ip);
        click.setUserAgent(ua);
        click.setDeleted(0);
        shareClickRepository.save(click);
    }

    /**
     * 获取我的分享链接
     */
    public List<ShareLink> getMyShareLinks(Long userId) {
        return shareLinkRepository.findBySharerUserIdAndDeletedOrderByCreatedAtDesc(userId, 0);
    }

    /**
     * 获取分享统计
     */
    public ShareStatsVO getShareStats(Long userId) {
        Long linkCount = shareLinkRepository.countBySharerUserIdAndDeleted(userId, 0);
        List<ShareLink> links = shareLinkRepository.findBySharerUserIdAndDeletedOrderByCreatedAtDesc(userId, 0);
        long totalClicks = links.stream().mapToLong(l -> l.getClickCount() != null ? l.getClickCount() : 0).sum();

        ShareStatsVO stats = new ShareStatsVO();
        stats.setLinkCount(linkCount);
        stats.setTotalClicks(totalClicks);
        return stats;
    }

    /**
     * MGM 绑定关系：被邀请人通过分享链接绑定推荐关系
     * 简化实现：记录一条 ShareClick，visitorUserId 设为 inviteeId
     */
    @Transactional
    public void bindReferral(Long shareId, Long inviteeId) {
        ShareLink link = shareLinkRepository.findByIdAndDeleted(shareId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        ShareClick click = new ShareClick();
        click.setShareId(shareId);
        click.setVisitorUserId(inviteeId);
        click.setSourceOfficeId(link.getSharerOfficeId());
        click.setDeleted(0);
        shareClickRepository.save(click);
    }
}
