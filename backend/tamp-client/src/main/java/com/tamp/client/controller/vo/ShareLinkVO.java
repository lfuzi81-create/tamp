package com.tamp.client.controller.vo;

import com.tamp.client.entity.ShareLink;

/**
 * 分享链接创建结果 VO
 */
public class ShareLinkVO {

    private ShareLink shareLink;
    private String shareUrl;

    public ShareLink getShareLink() { return shareLink; }
    public void setShareLink(ShareLink shareLink) { this.shareLink = shareLink; }
    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }
}
