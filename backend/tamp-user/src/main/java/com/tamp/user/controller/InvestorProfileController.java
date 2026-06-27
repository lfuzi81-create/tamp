package com.tamp.user.controller;

import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.user.entity.InvestorProfile;
import com.tamp.user.service.InvestorProfileService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/investor-profile")
@RequireRole("INVESTOR")
public class InvestorProfileController {

    private final InvestorProfileService investorProfileService;

    public InvestorProfileController(InvestorProfileService investorProfileService) {
        this.investorProfileService = investorProfileService;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (Long) principal;
    }

    @GetMapping
    public Result<InvestorProfile> getProfile() {
        Long userId = getCurrentUserId();
        return Result.ok(investorProfileService.getByUserId(userId));
    }

    @PutMapping
    public Result<InvestorProfile> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        InvestorProfile profile = investorProfileService.createOrUpdate(
                userId, body.get("nickname"), body.get("avatarUrl"));
        return Result.ok(profile);
    }

    @PutMapping("/first-login")
    public Result<Void> completeFirstLogin() {
        Long userId = getCurrentUserId();
        investorProfileService.completeFirstLogin(userId);
        return Result.ok();
    }
}
