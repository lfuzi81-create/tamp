package com.tamp.client.controller;

import com.tamp.client.controller.vo.ShareLinkVO;
import com.tamp.client.controller.vo.ShareStatsVO;
import com.tamp.client.entity.ShareLink;
import com.tamp.client.service.ShareLinkService;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/share-links")
@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN", "INVESTOR"})
public class ShareLinkController {

    private final ShareLinkService shareLinkService;

    @Value("${tamp.share.domain:https://www.zgsmam.com}")
    private String shareDomain;

    @Value("${tamp.share.path:/s}")
    private String sharePath;

    public ShareLinkController(ShareLinkService shareLinkService) {
        this.shareLinkService = shareLinkService;
    }

    @PostMapping
    public Result<ShareLinkVO> createShareLink(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long officeId = SecurityUtils.getCurrentOfficeId();
        String targetType = (String) body.get("targetType");
        Long targetId = body.get("targetId") != null ? Long.valueOf(body.get("targetId").toString()) : null;
        String targetName = (String) body.get("targetName");

        ShareLink link = shareLinkService.createShareLink(userId, officeId, targetType, targetId, targetName);
        ShareLinkVO vo = new ShareLinkVO();
        vo.setShareLink(link);
        vo.setShareUrl(shareDomain + sharePath + "/" + link.getShortCode());
        return Result.ok(vo);
    }

    @GetMapping("/resolve/{shortCode}")
    public Result<ShareLink> resolveShareLink(@PathVariable String shortCode) {
        return Result.ok(shareLinkService.resolveShareLink(shortCode));
    }

    @PostMapping("/{id}/click")
    public Result<Void> recordClick(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body,
                                    HttpServletRequest request) {
        Long visitorUserId = null;
        String visitorPhone = null;
        Long sourceOfficeId = null;
        if (body != null) {
            visitorUserId = body.get("visitorUserId") != null ? Long.valueOf(body.get("visitorUserId").toString()) : null;
            visitorPhone = (String) body.get("visitorPhone");
            sourceOfficeId = body.get("sourceOfficeId") != null ? Long.valueOf(body.get("sourceOfficeId").toString()) : null;
        }
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        shareLinkService.recordClick(id, visitorUserId, visitorPhone, sourceOfficeId, ip, ua);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<List<ShareLink>> getMyShareLinks() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(shareLinkService.getMyShareLinks(userId));
    }

    @GetMapping("/stats")
    public Result<ShareStatsVO> getShareStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(shareLinkService.getShareStats(userId));
    }

    @PostMapping("/bind-referral")
    public Result<Void> bindReferral(@RequestBody Map<String, Object> body) {
        Long shareId = Long.valueOf(body.get("shareId").toString());
        Long inviteeId = SecurityUtils.getCurrentUserId();
        shareLinkService.bindReferral(shareId, inviteeId);
        return Result.ok();
    }
}
