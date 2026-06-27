package com.tamp.user.service;

import com.tamp.client.repository.ClientRepository;
import com.tamp.auth.entity.Role;
import com.tamp.auth.entity.User;
import com.tamp.auth.repository.UserRepository;
import com.tamp.org.entity.FamilyOffice;
import com.tamp.org.entity.Shop;
import com.tamp.org.entity.Staff;
import com.tamp.org.repository.FamilyOfficeRepository;
import com.tamp.org.repository.ShopRepository;
import com.tamp.org.repository.StaffRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.util.PasswordUtils;
import com.tamp.common.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final FamilyOfficeRepository familyOfficeRepository;
    private final StaffRepository staffRepository;
    private final ClientRepository clientRepository;

    public UserService(UserRepository userRepository, ShopRepository shopRepository,
                      FamilyOfficeRepository familyOfficeRepository,
                      StaffRepository staffRepository,
                      ClientRepository clientRepository) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.familyOfficeRepository = familyOfficeRepository;
        this.staffRepository = staffRepository;
        this.clientRepository = clientRepository;
    }

    public Object getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole() != null ? user.getRole().name() : null);
        profile.put("realName", user.getRealName());
        profile.put("avatar", user.getAvatar());
        profile.put("officeId", user.getOfficeId());
        profile.put("shopId", user.getShopId());

        return profile;
    }

    @Transactional
    public void updateProfile(Long userId, String realName, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));

        if (realName != null) {
            user.setRealName(realName);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        userRepository.save(user);
    }

    @Transactional
    public void changePhone(Long userId, String newPhone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));

        if (userRepository.findByPhone(newPhone).isPresent()) {
            throw new BizException(ErrorCode.AUTH_PHONE_EXISTS);
        }

        user.setPhone(newPhone);
        userRepository.save(user);
    }

    public Map<String, Object> getShopInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));

        Map<String, Object> result = new HashMap<>();

        if (user.getShopId() != null) {
            Shop shop = shopRepository.findByIdAndDeleted(user.getShopId(), 0)
                    .orElse(null);
            if (shop != null) {
                result.put("shopId", shop.getId());
                result.put("shopName", shop.getName());
                result.put("officeId", shop.getOfficeId());
                result.put("managerName", shop.getManagerName());
                result.put("address", shop.getAddress());
                result.put("status", shop.getStatus());
            }
        } else {
            result.put("shopId", null);
            result.put("shopName", null);
            result.put("officeId", null);
            result.put("managerName", null);
            result.put("address", null);
            result.put("status", null);
        }

        return result;
    }

    public Map<String, Object> getInvestorOfficeInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));

        Map<String, Object> result = new HashMap<>();

        if (user.getOfficeId() != null) {
            FamilyOffice office = familyOfficeRepository.findByIdAndDeleted(user.getOfficeId(), 0)
                    .orElse(null);
            if (office != null) {
                result.put("officeId", office.getId());
                result.put("officeName", office.getName());
                result.put("contactPerson", office.getContactPerson());
                result.put("contactPhone", office.getContactPhone());
                result.put("intro", office.getIntro());
                result.put("memberCount", staffRepository.countByOfficeIdAndDeleted(office.getId(), 0));
                result.put("logoUrl", office.getLogoUrl());
                java.math.BigDecimal totalAum = clientRepository.sumAumTotalByOfficeIdAndDeleted(office.getId(), 0);
                result.put("totalAum", totalAum != null ? totalAum : java.math.BigDecimal.ZERO);
                Long clientCount = clientRepository.countByOfficeIdAndDeleted(office.getId(), 0);
                result.put("clientCount", clientCount != null ? clientCount.intValue() : 0);
                Long shopCount = shopRepository.countByOfficeIdAndDeleted(office.getId(), 0);
                result.put("shopCount", shopCount != null ? shopCount.intValue() : 0);
                result.put("status", office.getStatus());
            }
        } else {
            result.put("officeId", null);
            result.put("officeName", null);
            result.put("contactPerson", null);
            result.put("contactPhone", null);
            result.put("intro", null);
            result.put("memberCount", null);
            result.put("logoUrl", null);
            result.put("totalAum", null);
            result.put("clientCount", null);
            result.put("shopCount", null);
            result.put("status", null);
        }

        return result;
    }

    /**
     * 分页查询用户列表（仅后台管理人员：SUPER_ADMIN / PLATFORM_ADMIN / OPERATOR）
     */
    public Page<User> listUsers(String keyword, String role, Long officeId, Long shopId, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), 0));

            // 仅查询后台管理账号：总部/平台/运营/家办管理员
            predicates.add(root.get("role").in(
                Role.SUPER_ADMIN, Role.PLATFORM_ADMIN, Role.OPERATOR, Role.TAMP_ADMIN
            ));

            List<Long> visibleOfficeIds = SecurityUtils.getCurrentUserOfficeIds();
            if (visibleOfficeIds != null) {
                predicates.add(root.get("officeId").in(visibleOfficeIds));
            }

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                    cb.like(root.get("phone"), like),
                    cb.like(root.get("realName"), like)
                ));
            }
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role"), Role.valueOf(role)));
            }
            if (officeId != null) {
                predicates.add(cb.equal(root.get("officeId"), officeId));
            }
            if (shopId != null) {
                predicates.add(cb.equal(root.get("shopId"), shopId));
            }

            // 排序：SUPER_ADMIN 优先，然后按 createdAt 降序
            query.orderBy(
                cb.asc(cb.selectCase()
                    .when(cb.equal(root.get("role"), Role.SUPER_ADMIN), 0)
                    .otherwise(1)),
                cb.desc(root.get("createdAt"))
            );
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable);
    }

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(String phone, String realName, String role,
                          Long officeId, String officeIds, Long shopId, String password) {
        if (phone != null && userRepository.findByPhone(phone).isPresent()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "手机号已存在");
        }

        User user = new User();
        user.setPhone(phone);
        user.setRealName(realName);
        user.setRole(Role.valueOf(role));
        user.setOfficeId(officeId);
        user.setOfficeIds(officeIds);
        user.setShopId(shopId);
        user.setPassword(PasswordUtils.encode(password != null ? password : getInitialPassword(phone)));
        user.setPasswordChanged(0);
        user.setStatus(0);
        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(Long id, String phone, String realName, String role,
                          Long officeId, String officeIds, Long shopId, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));

        if (phone != null && !phone.equals(user.getPhone())) {
            userRepository.findByPhone(phone)
                    .filter(existing -> !id.equals(existing.getId()))
                    .ifPresent(existing -> {
                        throw new BizException(ErrorCode.AUTH_PHONE_EXISTS);
                    });
        }

        FamilyOffice managedOffice = findManagedOffice(user);
        if (managedOffice != null) {
            String oldPhone = user.getPhone();
            if (phone != null) user.setPhone(phone);
            if (realName != null) user.setRealName(realName);
            user.setRole(Role.OPERATOR);
            user.setOfficeId(managedOffice.getId());
            user.setOfficeIds(String.valueOf(managedOffice.getId()));
            user.setShopId(null);
            if (status != null) user.setStatus(status);

            managedOffice.setContactPerson(user.getRealName());
            managedOffice.setContactPhone(user.getPhone());
            familyOfficeRepository.save(managedOffice);
            syncManagedOfficeMember(managedOffice.getId(), oldPhone, user);
            return userRepository.save(user);
        }

        if (phone != null) user.setPhone(phone);
        if (realName != null) user.setRealName(realName);
        if (role != null) user.setRole(Role.valueOf(role));
        if (officeId != null) user.setOfficeId(officeId);
        if (officeIds != null) user.setOfficeIds(officeIds);
        if (shopId != null) user.setShopId(shopId);
        if (status != null) user.setStatus(status);

        return userRepository.save(user);
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        user.setPassword(PasswordUtils.encode(getInitialPassword(user.getPhone())));
        user.setPasswordChanged(0);
        userRepository.save(user);
    }

    private String getInitialPassword(String phone) {
        return "123456";
    }

    private FamilyOffice findManagedOffice(User user) {
        if (user.getRole() != Role.OPERATOR || user.getOfficeId() == null) {
            return null;
        }
        if (!String.valueOf(user.getOfficeId()).equals(user.getOfficeIds())) {
            return null;
        }
        return familyOfficeRepository.findByIdAndDeleted(user.getOfficeId(), 0)
                .filter(office -> user.getPhone() != null && user.getPhone().equals(office.getContactPhone()))
                .orElse(null);
    }

    private void syncManagedOfficeMember(Long officeId, String oldPhone, User user) {
        Staff member = null;
        for (Staff staff : staffRepository.findByOfficeIdAndDeleted(officeId, 0)) {
            if (Role.OPERATOR.name().equals(staff.getRoleType())
                    && (user.getPhone().equals(staff.getPhone()) || oldPhone.equals(staff.getPhone()))) {
                member = staff;
                break;
            }
        }
        if (member == null) {
            member = new Staff();
            member.setOfficeId(officeId);
            member.setRoleType(Role.OPERATOR.name());
        }
        member.setName(user.getRealName());
        member.setPhone(user.getPhone());
        member.setShopId(null);
        member.setStatus(user.getStatus());
        member.setDeleted(0);
        staffRepository.save(member);
    }

    /**
     * 切换用户状态
     */
    @Transactional
    public void toggleStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        user.setStatus(status);
        userRepository.save(user);
    }

    /**
     * 逻辑删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        user.setDeleted(1);
        user.setStatus(1);
        userRepository.save(user);
    }
}
