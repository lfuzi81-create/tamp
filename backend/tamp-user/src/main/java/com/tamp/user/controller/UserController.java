package com.tamp.user.controller;

import com.tamp.user.service.UserService;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (Long) principal;
    }

    @GetMapping("/profile")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN", "INVESTOR"})
    public Result<Object> getUserProfile() {
        Long userId = getCurrentUserId();
        return Result.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN", "INVESTOR"})
    public Result<Void> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        userService.updateProfile(userId, body.get("realName"), body.get("avatar"));
        return Result.ok();
    }

    @PutMapping("/phone")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN", "INVESTOR"})
    public Result<Void> changePhone(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        userService.changePhone(userId, body.get("newPhone"));
        return Result.ok();
    }

    @GetMapping("/shop-info")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN"})
    public Result<Map<String, Object>> getShopInfo() {
        Long userId = getCurrentUserId();
        return Result.ok(userService.getShopInfo(userId));
    }

    @GetMapping("/investor-office")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "INVESTOR"})
    public Result<Map<String, Object>> getInvestorOfficeInfo() {
        Long userId = getCurrentUserId();
        return Result.ok(userService.getInvestorOfficeInfo(userId));
    }

    @PostMapping("/consult")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN", "INVESTOR"})
    public Result<Void> submitConsult(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        log.info("[咨询提交] userId={} | name={} | phone={} | productName={} | content={}",
                userId, body.get("name"), body.get("phone"),
                body.get("productName"), body.get("content"));
        return Result.ok();
    }
}
