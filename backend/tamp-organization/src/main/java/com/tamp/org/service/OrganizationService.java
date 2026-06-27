package com.tamp.org.service;

import com.tamp.auth.entity.Role;
import com.tamp.auth.entity.User;
import com.tamp.auth.repository.UserRepository;
import com.tamp.client.entity.Client;
import com.tamp.client.service.ClientService;
import com.tamp.org.controller.vo.OfficeDetailVO;
import com.tamp.common.util.PasswordUtils;
import com.tamp.org.entity.FamilyOffice;
import com.tamp.org.entity.Shop;
import com.tamp.org.entity.Staff;
import com.tamp.org.repository.FamilyOfficeRepository;
import com.tamp.org.repository.ShopRepository;
import com.tamp.client.repository.ClientRepository;
import com.tamp.org.repository.StaffRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);

    private final FamilyOfficeRepository familyOfficeRepository;
    private final ShopRepository shopRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    public OrganizationService(FamilyOfficeRepository familyOfficeRepository, ShopRepository shopRepository,
                               ClientRepository clientRepository, ClientService clientService,
                               StaffRepository staffRepository,
                               UserRepository userRepository, SmsService smsService) {
        this.familyOfficeRepository = familyOfficeRepository;
        this.shopRepository = shopRepository;
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.smsService = smsService;
    }

    // ===== 家办相关方法 =====

    public Page<FamilyOffice> listOffices(String keyword, Integer status, Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasStatus = status != null;

        Page<FamilyOffice> page;
        if (hasKeyword && hasStatus) {
            page = familyOfficeRepository.findByNameContainingAndStatusAndDeleted(keyword, status, 0, pageable);
        } else if (hasKeyword) {
            page = familyOfficeRepository.findByNameContainingAndDeleted(keyword, 0, pageable);
        } else if (hasStatus) {
            page = familyOfficeRepository.findByStatusAndDeleted(status, 0, pageable);
        } else {
            page = familyOfficeRepository.findByDeleted(0, pageable);
        }
        return page.map(this::enrichOfficeStats);
    }

    private FamilyOffice enrichOfficeStats(FamilyOffice office) {
        if (office == null || office.getId() == null) {
            return office;
        }
        office.setMemberCount((int) staffRepository.countByOfficeIdAndDeleted(office.getId(), 0));
        office.setShopCount(shopRepository.countByOfficeIdAndDeleted(office.getId(), 0).intValue());
        office.setClientCount(clientRepository.countByOfficeIdAndDeleted(office.getId(), 0).intValue());
        return office;
    }

    public FamilyOffice getOffice(Long id) {
        FamilyOffice office = familyOfficeRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_OFFICE_NOT_FOUND));
        return enrichOfficeStats(office);
    }

    @Transactional
    public FamilyOffice createOffice(FamilyOffice office) {
        office.setDeleted(0);
        office.setStatus(office.getStatus() != null ? office.getStatus() : 0);

        if (userRepository.existsByPhone(office.getContactPhone())) {
            throw new BizException(ErrorCode.AUTH_PHONE_EXISTS);
        }

        FamilyOffice saved = familyOfficeRepository.save(office);
        syncOfficeOperator(saved, null, null);
        return saved;
    }

    private String getInitialPassword(String phone) {
        return "123456";
    }

    @Transactional
    public FamilyOffice updateOffice(Long id, FamilyOffice office) {
        FamilyOffice existing = getOffice(id);
        String oldContactPerson = existing.getContactPerson();
        String oldContactPhone = existing.getContactPhone();
        if (office.getName() != null) existing.setName(office.getName());
        if (office.getContactPerson() != null) existing.setContactPerson(office.getContactPerson());
        if (office.getContactPhone() != null) existing.setContactPhone(office.getContactPhone());
        if (office.getIntro() != null) existing.setIntro(office.getIntro());
        if (office.getLogoUrl() != null) existing.setLogoUrl(office.getLogoUrl());
        if (office.getStatus() != null) existing.setStatus(office.getStatus());
        if (office.getMemberCount() != null) existing.setMemberCount(office.getMemberCount());
        if (office.getShopCount() != null) existing.setShopCount(office.getShopCount());
        if (office.getClientCount() != null) existing.setClientCount(office.getClientCount());
        if (office.getTotalAum() != null) existing.setTotalAum(office.getTotalAum());
        FamilyOffice saved = familyOfficeRepository.save(existing);
        syncOfficeOperator(saved, oldContactPerson, oldContactPhone);
        return saved;
    }

    @Transactional
    public void toggleOfficeStatus(Long id) {
        FamilyOffice office = getOffice(id);
        office.setStatus(office.getStatus() == 0 ? 1 : 0);
        familyOfficeRepository.save(office);

        String contactPhone = normalize(office.getContactPhone());
        if (contactPhone != null) {
            userRepository.findByPhone(contactPhone).ifPresent(user -> {
                if (user.getRole() == Role.OPERATOR && isSingleOfficeOperator(user, office.getId())) {
                    user.setStatus(office.getStatus() == 0 ? 0 : 1);
                    userRepository.save(user);
                }
            });
            for (Staff staff : staffRepository.findByOfficeIdAndDeleted(id, 0)) {
                if (Role.OPERATOR.name().equals(staff.getRoleType()) && contactPhone.equals(staff.getPhone())) {
                    staff.setStatus(office.getStatus() == 0 ? 0 : 1);
                    staffRepository.save(staff);
                }
            }
        }
    }

    private void syncOfficeOperator(FamilyOffice office, String oldContactPerson, String oldContactPhone) {
        String contactPerson = normalize(office.getContactPerson());
        String contactPhone = normalize(office.getContactPhone());
        if (contactPerson == null || contactPhone == null) {
            return;
        }

        boolean phoneChanged = oldContactPhone != null && !Objects.equals(oldContactPhone, contactPhone);
        if (phoneChanged) {
            detachManagedOfficeOperator(office.getId(), oldContactPhone);
        }

        User operator = userRepository.findByPhone(contactPhone).orElse(null);
        boolean isNewUser = operator == null;
        if (operator == null) {
            operator = new User();
            operator.setPhone(contactPhone);
            operator.setPassword(PasswordUtils.encode(getInitialPassword(contactPhone)));
            operator.setPasswordChanged(0);
        } else if (!canReuseAsOfficeOperator(operator, office.getId())) {
            throw new BizException(ErrorCode.AUTH_PHONE_EXISTS, "该手机号已绑定其他家办");
        }

        operator.setRealName(contactPerson);
        operator.setRole(Role.OPERATOR);
        operator.setOfficeId(office.getId());
        operator.setOfficeIds(String.valueOf(office.getId()));
        operator.setShopId(null);
        operator.setStatus(0);
        userRepository.save(operator);

        upsertOfficeOperatorMember(office.getId(), contactPerson, contactPhone);

        if (isNewUser) {
            smsService.sendInitialPassword(contactPhone, getInitialPassword(contactPhone));
        }
    }

    private boolean canReuseAsOfficeOperator(User user, Long officeId) {
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            return false;
        }
        if (user.getOfficeId() != null && !officeId.equals(user.getOfficeId())) {
            return false;
        }
        List<Long> officeIds = parseOfficeIds(user.getOfficeIds());
        return officeIds.isEmpty() || (officeIds.size() == 1 && officeIds.contains(officeId));
    }

    private void detachManagedOfficeOperator(Long officeId, String oldContactPhone) {
        String oldPhone = normalize(oldContactPhone);
        if (oldPhone == null) {
            return;
        }

        userRepository.findByPhone(oldPhone).ifPresent(user -> {
            if (user.getRole() != Role.OPERATOR || !isSingleOfficeOperator(user, officeId)) {
                return;
            }
            user.setOfficeId(null);
            user.setOfficeIds(null);
            user.setStatus(1);
            userRepository.save(user);
        });

        for (Staff staff : staffRepository.findByOfficeIdAndDeleted(officeId, 0)) {
            if (Role.OPERATOR.name().equals(staff.getRoleType()) && oldPhone.equals(staff.getPhone())) {
                staff.setDeleted(1);
                staffRepository.save(staff);
            }
        }
    }

    private boolean isSingleOfficeOperator(User user, Long officeId) {
        List<Long> officeIds = parseOfficeIds(user.getOfficeIds());
        return officeId.equals(user.getOfficeId()) && officeIds.size() == 1 && officeIds.contains(officeId);
    }

    private void upsertOfficeOperatorMember(Long officeId, String contactPerson, String contactPhone) {
        Staff member = null;
        for (Staff staff : staffRepository.findByOfficeIdAndDeleted(officeId, 0)) {
            if (Role.OPERATOR.name().equals(staff.getRoleType()) && contactPhone.equals(staff.getPhone())) {
                member = staff;
                break;
            }
        }
        if (member == null) {
            member = new Staff();
            member.setOfficeId(officeId);
            member.setJoinDate(LocalDate.now());
        }
        member.setName(contactPerson);
        member.setPhone(contactPhone);
        member.setRoleType(Role.OPERATOR.name());
        member.setShopId(null);
        member.setStatus(0);
        member.setDeleted(0);
        staffRepository.save(member);
    }

    private List<Long> parseOfficeIds(String officeIds) {
        if (officeIds == null || officeIds.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(officeIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public List<Shop> listShopsByOffice(Long officeId) {
        getOffice(officeId);
        return shopRepository.findByOfficeIdAndDeletedOrderByCreatedAtDesc(officeId, 0)
                .stream()
                .map(this::enrichShopDisplay)
                .toList();
    }

    /**
     * tamp 详情聚合接口（4Tab 数据：概览、成员、店铺、客户）
     * 返回扁平结构，前端直接使用 officeDetail.xxx
     */
    public OfficeDetailVO getOfficeDetail(Long id) {
        FamilyOffice office = getOffice(id);
        List<Shop> shops = shopRepository.findByOfficeIdAndDeleted(id, 0);
        List<Client> clients = clientRepository.findByOfficeIdAndDeleted(id, 0).stream()
                .peek(clientService::enrichClientAuthSummary)
                .toList();
        List<Staff> members = staffRepository.findByOfficeIdAndDeleted(id, 0);

        OfficeDetailVO detail = new OfficeDetailVO();
        detail.setId(office.getId());
        detail.setName(office.getName());
        detail.setContactPerson(office.getContactPerson());
        detail.setContactPhone(office.getContactPhone());
        detail.setIntro(office.getIntro());
        detail.setLogoUrl(office.getLogoUrl());
        detail.setStatus(office.getStatus());
        detail.setCreatedAt(office.getCreatedAt());
        detail.setUpdatedAt(office.getUpdatedAt());
        detail.setMemberCount(members.size());
        detail.setShopCount(shops.size());
        detail.setClientCount(clients.size());
        detail.setTotalAum(clientService.computeOfficeAuthorizedAum(id));
        detail.setMembers(members);
        detail.setShops(shops.stream().map(this::enrichShopDisplay).toList());
        detail.setClients(clients);
        return detail;
    }

    // ===== 店铺相关方法 =====

    public Page<Shop> listShops(String keyword, Long officeId, Integer status, Pageable pageable) {
        Page<Shop> page;
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasOfficeId = officeId != null;
        boolean hasStatus = status != null;

        if (hasKeyword && hasOfficeId && hasStatus) {
            page = shopRepository.findByNameContainingAndOfficeIdAndStatusAndDeleted(keyword, officeId, status, 0, pageable);
        } else if (hasKeyword && hasOfficeId) {
            page = shopRepository.findByNameContainingAndOfficeIdAndDeleted(keyword, officeId, 0, pageable);
        } else if (hasKeyword && hasStatus) {
            page = shopRepository.findByNameContainingAndStatusAndDeleted(keyword, status, 0, pageable);
        } else if (hasOfficeId && hasStatus) {
            page = shopRepository.findByOfficeIdAndStatusAndDeleted(officeId, status, 0, pageable);
        } else if (hasKeyword) {
            page = shopRepository.findByNameContainingAndDeleted(keyword, 0, pageable);
        } else if (hasOfficeId) {
            page = shopRepository.findByOfficeIdAndDeleted(officeId, 0, pageable);
        } else if (hasStatus) {
            page = shopRepository.findByStatusAndDeleted(status, 0, pageable);
        } else {
            page = shopRepository.findByDeleted(0, pageable);
        }
        return page.map(this::enrichShopDisplay);
    }

    private Shop enrichShopDisplay(Shop shop) {
        if (shop == null || shop.getId() == null) {
            return shop;
        }
        if (!StringUtils.hasText(shop.getManagerName()) || !StringUtils.hasText(shop.getManagerPhone())) {
            staffRepository.findByShopIdAndDeleted(shop.getId(), 0, Pageable.ofSize(20))
                    .getContent()
                    .stream()
                    .filter(s -> "SHOP_ADMIN".equals(s.getRoleType()) || "MANAGER".equals(s.getRoleType()))
                    .findFirst()
                    .ifPresent(admin -> {
                        if (!StringUtils.hasText(shop.getManagerName())) {
                            shop.setManagerName(admin.getName());
                        }
                        if (!StringUtils.hasText(shop.getManagerPhone())) {
                            shop.setManagerPhone(admin.getPhone());
                        }
                    });
        }
        shop.setClientCount(clientRepository.countByShopIdAndDeleted(shop.getId(), 0).intValue());
        shop.setTotalAum(clientService.computeShopAuthorizedAum(shop.getId()));
        return shop;
    }

    public Shop getShop(Long id) {
        return shopRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_SHOP_NOT_FOUND));
    }

    @Transactional
    public Shop createShop(Shop shop) {
        if (shop.getOfficeId() != null) {
            getOffice(shop.getOfficeId());
        }
        validateShopCreateInput(shop);

        shop.setDeleted(0);
        shop.setStatus(shop.getStatus() != null ? shop.getStatus() : 0);
        Shop saved = shopRepository.save(shop);
        provisionShopAdmin(saved, shop.getManagerName(), shop.getManagerPhone());
        return saved;
    }

    private void validateShopCreateInput(Shop shop) {
        if (!StringUtils.hasText(shop.getName())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "店铺名称不能为空");
        }
        if (shop.getOfficeId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "所属家办不能为空");
        }
        if (!StringUtils.hasText(shop.getManagerName()) || !StringUtils.hasText(shop.getManagerPhone())) {
            throw new BizException(ErrorCode.BIZ_SHOP_ADMIN_REQUIRED);
        }
        if (shopRepository.existsByOfficeIdAndNameAndDeleted(shop.getOfficeId(), shop.getName().trim(), 0)) {
            throw new BizException(ErrorCode.BIZ_SHOP_DUPLICATE);
        }
    }

    private void provisionShopAdmin(Shop shop, String managerName, String managerPhone) {
        String phone = managerPhone.trim();
        String name = managerName.trim();

        userRepository.findByPhone(phone).ifPresentOrElse(
                existing -> linkExistingShopAdmin(existing, shop, name, phone),
                () -> createNewShopAdmin(shop, name, phone)
        );
        upsertShopAdminStaff(shop, name, phone);
    }

    private void linkExistingShopAdmin(User user, Shop shop, String managerName, String managerPhone) {
        if (user.getRole() != Role.SHOP_ADMIN) {
            throw new BizException(ErrorCode.AUTH_PHONE_EXISTS, "该手机号已被其他角色占用");
        }
        if (user.getShopId() != null && !user.getShopId().equals(shop.getId())) {
            throw new BizException(ErrorCode.AUTH_PHONE_EXISTS, "该手机号已绑定其他店铺");
        }
        user.setRealName(managerName);
        user.setOfficeId(shop.getOfficeId());
        user.setShopId(shop.getId());
        user.setStatus(0);
        userRepository.save(user);
        log.info("[createShop] 关联已有店铺管理员账号 phone={}, shopId={}", managerPhone, shop.getId());
    }

    private void createNewShopAdmin(Shop shop, String managerName, String managerPhone) {
        String initialPassword = getInitialPassword(managerPhone);

        User shopAdmin = new User();
        shopAdmin.setPassword(PasswordUtils.encode(initialPassword));
        shopAdmin.setPhone(managerPhone);
        shopAdmin.setRealName(managerName);
        shopAdmin.setRole(Role.SHOP_ADMIN);
        shopAdmin.setOfficeId(shop.getOfficeId());
        shopAdmin.setShopId(shop.getId());
        shopAdmin.setStatus(0);
        shopAdmin.setPasswordChanged(0);
        userRepository.save(shopAdmin);

        smsService.sendInitialPassword(managerPhone, initialPassword);
    }

    private void upsertShopAdminStaff(Shop shop, String managerName, String managerPhone) {
        List<Staff> existing = staffRepository.findByShopIdAndDeleted(shop.getId(), 0, Pageable.ofSize(20))
                .getContent()
                .stream()
                .filter(s -> "SHOP_ADMIN".equals(s.getRoleType()) || "MANAGER".equals(s.getRoleType()))
                .toList();
        if (!existing.isEmpty()) {
            Staff staff = existing.get(0);
            staff.setName(managerName);
            staff.setPhone(managerPhone);
            staff.setRoleType(Role.SHOP_ADMIN.name());
            staff.setOfficeId(shop.getOfficeId());
            staffRepository.save(staff);
            return;
        }

        Staff adminStaff = new Staff();
        adminStaff.setName(managerName);
        adminStaff.setPhone(managerPhone);
        adminStaff.setRoleType(Role.SHOP_ADMIN.name());
        adminStaff.setOfficeId(shop.getOfficeId());
        adminStaff.setShopId(shop.getId());
        adminStaff.setStatus(0);
        adminStaff.setDeleted(0);
        adminStaff.setJoinDate(LocalDate.now());
        staffRepository.save(adminStaff);
    }

    @Transactional
    public Shop updateShop(Long id, Shop shop) {
        Shop existing = getShop(id);
        if (shop.getOfficeId() != null && !shop.getOfficeId().equals(existing.getOfficeId())) {
            getOffice(shop.getOfficeId());
        }
        if (shop.getName() != null) existing.setName(shop.getName());
        if (shop.getOfficeId() != null) existing.setOfficeId(shop.getOfficeId());
        if (shop.getManagerName() != null) existing.setManagerName(shop.getManagerName());
        if (shop.getManagerPhone() != null) existing.setManagerPhone(shop.getManagerPhone());
        if (shop.getAddress() != null) existing.setAddress(shop.getAddress());
        if (shop.getStatus() != null) existing.setStatus(shop.getStatus());
        if (shop.getClientCount() != null) existing.setClientCount(shop.getClientCount());
        if (shop.getTotalAum() != null) existing.setTotalAum(shop.getTotalAum());
        return shopRepository.save(existing);
    }

    @Transactional
    public void toggleShopStatus(Long id) {
        Shop shop = getShop(id);
        shop.setStatus(shop.getStatus() == 0 ? 1 : 0);
        shopRepository.save(shop);
    }
}
