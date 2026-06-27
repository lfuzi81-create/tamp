package com.tamp.client.controller;

import com.tamp.client.entity.Client;
import com.tamp.client.entity.ClientAsset;
import com.tamp.client.entity.ClientTag;
import com.tamp.client.entity.ClientTimeline;
import com.tamp.client.service.ClientService;
import com.tamp.common.dto.PageResult;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public Result<PageResult<Client>> listClients(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String tag,
            Pageable pageable) {
        Page<Client> page = clientService.listClients(keyword, officeId, shopId, tag, pageable);
        return Result.ok(PageResult.of(page));
    }

    @GetMapping("/tags")
    public Result<List<String>> getAllTags() {
        return Result.ok(clientService.getAllTagNames());
    }

    @GetMapping("/{id:\\d+}")
    public Result<Client> getClient(@PathVariable Long id) {
        return Result.ok(clientService.getClient(id));
    }

    @PostMapping
    public Result<Client> createClient(@RequestBody Client client) {
        return Result.ok(clientService.createClient(client));
    }

    @PutMapping("/{id}")
    public Result<Client> updateClient(@PathVariable Long id, @RequestBody Client client) {
        return Result.ok(clientService.updateClient(id, client));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return Result.ok();
    }

    @GetMapping("/{id}/timeline")
    public Result<List<Map<String, Object>>> listTimeline(@PathVariable Long id) {
        return Result.ok(clientService.listTimelineEnriched(id));
    }

    @PostMapping("/{id}/timeline")
    public Result<ClientTimeline> addTimeline(@PathVariable Long id, @RequestBody ClientTimeline timeline) {
        timeline.setClientId(id);
        return Result.ok(clientService.addTimeline(timeline));
    }

    @GetMapping("/{id}/assets")
    public Result<PageResult<ClientAsset>> listAssets(@PathVariable Long id, Pageable pageable) {
        Page<ClientAsset> page = clientService.listAssets(id, pageable);
        return Result.ok(PageResult.of(page));
    }

    @PostMapping("/{id}/assets")
    public Result<ClientAsset> addAsset(@PathVariable Long id, @RequestBody ClientAsset asset) {
        return Result.ok(clientService.addAsset(id, asset));
    }

    @PutMapping("/{id}/assets/{assetId}")
    public Result<ClientAsset> updateAsset(@PathVariable Long id, @PathVariable Long assetId, @RequestBody ClientAsset asset) {
        return Result.ok(clientService.updateAsset(id, assetId, asset));
    }

    @DeleteMapping("/{id}/assets/{assetId}")
    public Result<Void> deleteAsset(@PathVariable Long id, @PathVariable Long assetId) {
        clientService.deleteAsset(id, assetId);
        return Result.ok();
    }

    @GetMapping("/{id}/tags")
    public Result<List<ClientTag>> listTags(@PathVariable Long id) {
        return Result.ok(clientService.listTags(id));
    }

    @PostMapping("/{id}/tags")
    public Result<Void> addTag(@PathVariable Long id, @RequestBody Map<String, String> body) {
        clientService.addTag(id, body.get("tagName"), body.get("tagColor"));
        return Result.ok();
    }

    @DeleteMapping("/{id}/tags/{tagIdx}")
    public Result<Void> deleteTag(@PathVariable Long id, @PathVariable Long tagIdx) {
        clientService.deleteTag(id, tagIdx);
        return Result.ok();
    }

    /**
     * 投资人端「历史访问店铺列表」
     * 方法级 @RequireRole 覆盖类级注解，允许 INVESTOR 访问。
     */
    @GetMapping("/my-shops")
    @RequireRole({"INVESTOR"})
    public Result<List<Map<String, Object>>> getMyShops() {
        String phone = com.tamp.common.util.SecurityUtils.getCurrentPhone();
        if (phone == null) {
            return Result.ok(java.util.Collections.emptyList());
        }
        return Result.ok(clientService.getMyShops(phone));
    }

    /**
     * 投资人端记录浏览行为
     * 记录投资人浏览产品或内容的行为，用于生成行为时间线
     */
    @PostMapping("/behavior")
    @RequireRole({"INVESTOR"})
    public Result<Void> recordBehavior(@RequestBody Map<String, Object> body) {
        Long userId = com.tamp.common.util.SecurityUtils.getCurrentUserId();
        Long shopId = Long.valueOf(body.get("shopId").toString());
        String actionType = body.get("actionType").toString(); // VIEW_PRODUCT, VIEW_CONTENT, VIEW_KNOWLEDGE
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String targetName = body.get("targetName") != null ? body.get("targetName").toString() : "";
        clientService.recordBehavior(userId, shopId, actionType, targetId, targetName);
        return Result.ok();
    }

    /**
     * 投资人端获取个人资料（昵称、头像、手机号）
     * 从 sys_user 表读取 real_name 和 avatar 字段。
     */
    @GetMapping("/investor-profile")
    @RequireRole({"INVESTOR"})
    public Result<Map<String, Object>> getInvestorProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> profile = clientService.getInvestorProfile(userId);
        return Result.ok(profile);
    }

    /**
     * 投资人端更新个人资料（昵称、头像）
     * 更新 sys_user 表的 real_name 和 avatar 字段。
     */
    @PutMapping("/investor-profile")
    @RequireRole({"INVESTOR"})
    public Result<Void> updateInvestorProfile(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        String nickname = body.get("nickname");
        String avatarUrl = body.get("avatarUrl");
        clientService.updateInvestorProfile(userId, nickname, avatarUrl);
        return Result.ok();
    }
}
