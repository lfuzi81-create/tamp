package com.tamp.client.controller.vo;

/**
 * 分享统计 VO
 */
public class ShareStatsVO {

    private Long linkCount;
    private long totalClicks;

    public Long getLinkCount() { return linkCount; }
    public void setLinkCount(Long linkCount) { this.linkCount = linkCount; }
    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }
}
