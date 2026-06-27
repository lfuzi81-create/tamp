package com.tamp.user.controller;

import com.tamp.auth.entity.Role;
import com.tamp.auth.entity.User;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.org.entity.FamilyOffice;
import com.tamp.org.repository.FamilyOfficeRepository;
import com.tamp.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理控制器 — 用户账号 CRUD
 */
@RestController
@RequestMapping("/api/users")
@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
public class UserManagementController {

    private final UserService userService;
    private final FamilyOfficeRepository familyOfficeRepository;

    public UserManagementController(UserService userService,
                                    FamilyOfficeRepository familyOfficeRepository) {
        this.userService = userService;
        this.familyOfficeRepository = familyOfficeRepository;
    }

    /**
     * 用户列表
     */
    @GetMapping
    public Result<Map<String, Object>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(Math.max(0, page - 1), size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userService.listUsers(keyword, role, officeId, shopId, pageable);

        // 收集所有需要查询的家办ID（含 officeId 和 officeIds）
        Set<Long> allOfficeIds = new HashSet<>();
        for (User u : userPage.getContent()) {
            if (u.getOfficeId() != null) allOfficeIds.add(u.getOfficeId());
            if (u.getOfficeIds() != null && !u.getOfficeIds().isEmpty()) {
                for (String s : u.getOfficeIds().split(",")) {
                    try { allOfficeIds.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
                }
            }
        }
        Map<Long, String> officeNameMap = new HashMap<>();
        for (Long oid : allOfficeIds) {
            familyOfficeRepository.findByIdAndDeleted(oid, 0)
                    .ifPresent(o -> officeNameMap.put(o.getId(), o.getName()));
        }

        // 构建返回列表
        List<Map<String, Object>> list = userPage.getContent().stream().map(u -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("phone", u.getPhone());
            item.put("realName", u.getRealName());
            item.put("role", u.getRole() != null ? u.getRole().name() : null);
            item.put("roleName", u.getRole() != null ? u.getRole().getDescription() : null);
            item.put("officeId", u.getOfficeId());
            item.put("officeName", u.getOfficeId() != null ? officeNameMap.get(u.getOfficeId()) : null);
            boolean managedByOffice = false;
            if (u.getRole() == Role.OPERATOR
                    && u.getOfficeId() != null
                    && String.valueOf(u.getOfficeId()).equals(u.getOfficeIds())) {
                managedByOffice = familyOfficeRepository.findByIdAndDeleted(u.getOfficeId(), 0)
                        .map(office -> u.getPhone() != null && u.getPhone().equals(office.getContactPhone()))
                        .orElse(false);
            }
            item.put("managedByOffice", managedByOffice);
            // 运营人员可见家办范围
            List<Long> oidList = new ArrayList<>();
            List<String> oidNameList = new ArrayList<>();
            if (u.getOfficeIds() != null && !u.getOfficeIds().isEmpty()) {
                for (String s : u.getOfficeIds().split(",")) {
                    try {
                        Long oid = Long.parseLong(s.trim());
                        oidList.add(oid);
                        String name = officeNameMap.get(oid);
                        if (name != null) oidNameList.add(name);
                    } catch (NumberFormatException ignored) {}
                }
            }
            item.put("officeIds", oidList);
            item.put("officeNames", oidNameList);
            item.put("shopId", u.getShopId());
            item.put("avatar", u.getAvatar());
            item.put("status", u.getStatus());
            item.put("passwordChanged", u.getPasswordChanged());
            item.put("lastLoginTime", u.getLastLoginTime());
            item.put("lastLoginIp", u.getLastLoginIp());
            item.put("createdAt", u.getCreatedAt());
            item.put("updatedAt", u.getUpdatedAt());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", userPage.getTotalElements());
        data.put("totalPages", userPage.getTotalPages());
        data.put("pageNum", page);
        data.put("pageSize", size);
        data.put("pages", userPage.getTotalPages());
        data.put("page", page);
        data.put("size", size);

        return Result.ok(data);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<User> createUser(@RequestBody CreateUserRequest request) {
        String officeIdsStr = request.getOfficeIds() != null
                ? String.join(",", request.getOfficeIds().stream().map(String::valueOf).collect(Collectors.toList()))
                : null;
        User user = userService.createUser(
                request.getPhone(),
                request.getRealName(),
                request.getRole(),
                request.getOfficeId(),
                officeIdsStr,
                request.getShopId(),
                request.getPassword()
        );
        return Result.ok(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        String officeIdsStr = request.getOfficeIds() != null
                ? String.join(",", request.getOfficeIds().stream().map(String::valueOf).collect(Collectors.toList()))
                : null;
        User user = userService.updateUser(
                id,
                request.getPhone(),
                request.getRealName(),
                request.getRole(),
                request.getOfficeId(),
                officeIdsStr,
                request.getShopId(),
                request.getStatus()
        );
        return Result.ok(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.ok();
    }

    /**
     * 切换用户状态
     */
    @PatchMapping("/{id}/status")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userService.toggleStatus(id, body.get("status"));
        return Result.ok();
    }

    public static class CreateUserRequest {
        private String phone;
        private String realName;
        private String role;
        private Long officeId;
        private List<Long> officeIds;
        private Long shopId;
        private String password;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Long getOfficeId() { return officeId; }
        public void setOfficeId(Long officeId) { this.officeId = officeId; }
        public List<Long> getOfficeIds() { return officeIds; }
        public void setOfficeIds(List<Long> officeIds) { this.officeIds = officeIds; }
        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UpdateUserRequest {
        private String phone;
        private String realName;
        private String role;
        private Long officeId;
        private List<Long> officeIds;
        private Long shopId;
        private Integer status;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Long getOfficeId() { return officeId; }
        public void setOfficeId(Long officeId) { this.officeId = officeId; }
        public List<Long> getOfficeIds() { return officeIds; }
        public void setOfficeIds(List<Long> officeIds) { this.officeIds = officeIds; }
        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}
