package com.tamp.org.controller.vo;

import com.tamp.client.entity.Client;
import com.tamp.org.entity.Shop;
import com.tamp.org.entity.Staff;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 家办详情聚合 VO（4Tab：概览、成员、店铺、客户）
 */
public class OfficeDetailVO {

    private Long id;
    private String name;
    private String contactPerson;
    private String contactPhone;
    private String intro;
    private String logoUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int memberCount;
    private int shopCount;
    private int clientCount;
    private BigDecimal totalAum;

    private List<Staff> members;
    private List<Shop> shops;
    private List<Client> clients;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public int getShopCount() { return shopCount; }
    public void setShopCount(int shopCount) { this.shopCount = shopCount; }
    public int getClientCount() { return clientCount; }
    public void setClientCount(int clientCount) { this.clientCount = clientCount; }
    public BigDecimal getTotalAum() { return totalAum; }
    public void setTotalAum(BigDecimal totalAum) { this.totalAum = totalAum; }
    public List<Staff> getMembers() { return members; }
    public void setMembers(List<Staff> members) { this.members = members; }
    public List<Shop> getShops() { return shops; }
    public void setShops(List<Shop> shops) { this.shops = shops; }
    public List<Client> getClients() { return clients; }
    public void setClients(List<Client> clients) { this.clients = clients; }
}
