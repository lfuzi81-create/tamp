package com.tamp.user.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;

/**
 * 投资人个人资料表
 */
@Entity
@Table(name = "biz_investor_profile")
public class InvestorProfile extends BaseEntity {

    /**
     * 关联sys_user的ID
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 昵称
     */
    @Column(length = 50)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatarUrl;

    /**
     * 资料是否完善
     */
    @Column(name = "profile_completed")
    private Boolean profileCompleted = false;

    /**
     * 是否完成首次登录引导
     */
    @Column(name = "first_login_done")
    private Boolean firstLoginDone = false;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public Boolean getFirstLoginDone() {
        return firstLoginDone;
    }

    public void setFirstLoginDone(Boolean firstLoginDone) {
        this.firstLoginDone = firstLoginDone;
    }
}
