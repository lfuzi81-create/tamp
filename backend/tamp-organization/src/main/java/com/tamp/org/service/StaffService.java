package com.tamp.org.service;

import com.tamp.org.entity.Staff;
import com.tamp.org.entity.Shop;
import com.tamp.org.repository.ShopRepository;
import com.tamp.org.repository.StaffRepository;
import com.tamp.auth.entity.Role;
import com.tamp.auth.entity.User;
import com.tamp.auth.repository.UserRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class StaffService {

    private static final Logger log = LoggerFactory.getLogger(StaffService.class);
    private static final String DEFAULT_INITIAL_PASSWORD = "123456";

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public StaffService(StaffRepository staffRepository, UserRepository userRepository,
                        ShopRepository shopRepository) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
    }

    public Page<Staff> listStaff(String keyword, String roleType, Long officeId, Pageable pageable) {
        if (officeId != null) {
            return staffRepository.findByOfficeIdAndDeleted(officeId, Integer.valueOf(0), pageable);
        }
        if (StringUtils.hasText(keyword) && StringUtils.hasText(roleType)) {
            return staffRepository.findByNameContainingAndRoleTypeAndDeleted(keyword, roleType, Integer.valueOf(0), pageable);
        } else if (StringUtils.hasText(keyword)) {
            return staffRepository.findByNameContainingAndDeleted(keyword, Integer.valueOf(0), pageable);
        } else if (StringUtils.hasText(roleType)) {
            return staffRepository.findByRoleTypeAndDeleted(roleType, Integer.valueOf(0), pageable);
        }
        return staffRepository.findByDeleted(Integer.valueOf(0), pageable);
    }

    public Page<Staff> listByOffice(Long officeId, String keyword, Integer status, Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return staffRepository.findByOfficeFilters(officeId, normalizedKeyword, status, pageable);
    }

    public Page<Staff> listByShop(Long shopId, Pageable pageable) {
        return staffRepository.findByShopIdAndDeleted(shopId, Integer.valueOf(0), pageable);
    }

    public Staff getStaff(Long id) {
        return staffRepository.findByIdAndDeleted(id, Integer.valueOf(0))
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_STAFF_NOT_FOUND));
    }

    @Transactional
    public Staff createStaff(Staff staff) {
        staff.setDeleted(Integer.valueOf(0));
        if (staff.getStatus() == null) {
            staff.setStatus(Integer.valueOf(0));
        }
        normalizeShopRole(staff);
        if ("SHOP_ADMIN".equals(staff.getRoleType()) && StringUtils.hasText(staff.getPhone())) {
            ensureShopAdminUser(staff);
        }
        Staff saved = staffRepository.save(staff);
        syncShopManagerFromStaff(saved);
        return saved;
    }

    private void normalizeShopRole(Staff staff) {
        if ("MANAGER".equals(staff.getRoleType())) {
            staff.setRoleType("SHOP_ADMIN");
        }
    }

    /**
     * 为店铺管理员成员同步 sys_user 登录账号。
     * 若手机号已有 SHOP_ADMIN 且未绑定其他店铺，则关联到当前店铺。
     */
    private void ensureShopAdminUser(Staff staff) {
        userRepository.findByPhone(staff.getPhone()).ifPresentOrElse(
                existing -> {
                    if (existing.getRole() != Role.SHOP_ADMIN) {
                        throw new BizException(ErrorCode.PARAM_INVALID.getCode(),
                                "手机号 " + staff.getPhone() + " 已被其他角色占用");
                    }
                    if (existing.getShopId() != null && !existing.getShopId().equals(staff.getShopId())) {
                        throw new BizException(ErrorCode.PARAM_INVALID.getCode(),
                                "手机号 " + staff.getPhone() + " 已绑定其他店铺");
                    }
                    existing.setRealName(staff.getName());
                    existing.setOfficeId(staff.getOfficeId());
                    existing.setShopId(staff.getShopId());
                    existing.setStatus(0);
                    userRepository.save(existing);
                },
                () -> createUserAccountIfAbsent(staff)
        );
    }

    /**
     * 为店铺管理员成员同步创建 sys_user 登录账号。
     * 若手机号已被占用，抛业务异常，避免产生孤立成员记录。
     */
    private void createUserAccountIfAbsent(Staff staff) {
        userRepository.findByPhone(staff.getPhone()).ifPresent(existing -> {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(),
                    "手机号 " + staff.getPhone() + " 已存在登录账号，请直接编辑或更换手机号");
        });
        User user = new User();
        user.setPhone(staff.getPhone());
        user.setRealName(staff.getName());
        user.setRole(Role.SHOP_ADMIN);
        user.setOfficeId(staff.getOfficeId());
        user.setShopId(staff.getShopId());
        user.setPassword(PasswordUtils.encode(DEFAULT_INITIAL_PASSWORD));
        user.setPasswordChanged(0);
        user.setStatus(0);
        userRepository.save(user);
        log.info("[createStaff] 已为店铺管理员 {} 同步创建登录账号，初始密码={}", staff.getPhone(), DEFAULT_INITIAL_PASSWORD);
    }

    @Transactional
    public Staff updateStaff(Long id, Staff staff) {
        Staff existing = getStaff(id);
        ensureEditable(existing);
        if (staff.getName() != null) {
            existing.setName(staff.getName());
        }
        if (staff.getPhone() != null) {
            existing.setPhone(staff.getPhone());
        }
        if (staff.getEmail() != null) {
            existing.setEmail(staff.getEmail());
        }
        if (staff.getRoleType() != null) {
            existing.setRoleType(staff.getRoleType());
        }
        if (staff.getOfficeId() != null) {
            existing.setOfficeId(staff.getOfficeId());
        }
        if (staff.getShopId() != null) {
            existing.setShopId(staff.getShopId());
        }
        if (staff.getStatus() != null) {
            existing.setStatus(staff.getStatus());
        }
        if (staff.getJoinDate() != null) {
            existing.setJoinDate(staff.getJoinDate());
        }
        if (staff.getRemark() != null) {
            existing.setRemark(staff.getRemark());
        }
        Staff saved = staffRepository.save(existing);
        syncShopManagerFromStaff(saved);
        return saved;
    }

    private void syncShopManagerFromStaff(Staff staff) {
        if (staff.getShopId() == null) {
            return;
        }
        if (!"SHOP_ADMIN".equals(staff.getRoleType()) && !"MANAGER".equals(staff.getRoleType())) {
            return;
        }
        shopRepository.findByIdAndDeleted(staff.getShopId(), 0).ifPresent(shop -> {
            shop.setManagerName(staff.getName());
            shop.setManagerPhone(staff.getPhone());
            shopRepository.save(shop);
        });
    }

    @Transactional
    public void toggleStaffStatus(Long id) {
        Staff staff = getStaff(id);
        ensureEditable(staff);
        Integer newStatus = staff.getStatus() == Integer.valueOf(0) ? Integer.valueOf(1) : Integer.valueOf(0);
        staff.setStatus(newStatus);
        staffRepository.save(staff);
        // 同步联动登录账号状态（离职禁用登录，复职恢复登录）
        if ("SHOP_ADMIN".equals(staff.getRoleType()) && StringUtils.hasText(staff.getPhone())) {
            userRepository.findByPhone(staff.getPhone()).ifPresent(user -> {
                user.setStatus(newStatus);
                userRepository.save(user);
            });
        }
    }

    @Transactional
    public void deleteStaff(Long id) {
        Staff staff = getStaff(id);
        ensureEditable(staff);
        staff.setDeleted(Integer.valueOf(1));
        staffRepository.save(staff);
        // 同步禁用登录账号（保留账号记录，避免历史数据孤立）
        if ("SHOP_ADMIN".equals(staff.getRoleType()) && StringUtils.hasText(staff.getPhone())) {
            userRepository.findByPhone(staff.getPhone()).ifPresent(user -> {
                user.setStatus(1);
                userRepository.save(user);
            });
        }
    }

    private void ensureEditable(Staff staff) {
        if ("TAMP_ADMIN".equals(staff.getRoleType()) || "OPERATOR".equals(staff.getRoleType())) {
            throw new BizException(ErrorCode.FORBIDDEN, "家办管理员运营账号请在人员管理中维护");
        }
    }
}
