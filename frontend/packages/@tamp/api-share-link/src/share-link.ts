import { request } from '@tamp/core'
import type { Result } from '@tamp/core'

/** 分享链接 */
export interface ShareLink {
  id: number | string
  shortCode: string
  targetType?: string
  targetId?: number | string
  targetName?: string
  creatorId?: number | string
  clickCount?: number
  createdAt?: string
  expiredAt?: string
  [key: string]: any
}

/** 创建分享链接返回 VO */
export interface ShareLinkVO extends ShareLink {
  shareUrl?: string
  [key: string]: any
}

/** 创建分享链接请求体 */
export interface ShareLinkCreateBody {
  targetType?: string
  targetId?: number | string
  targetName?: string
}

/** 分享统计 VO */
export interface ShareStatsVO {
  totalLinks?: number
  totalClicks?: number
  myLinks?: number
  myClicks?: number
  [key: string]: any
}

/** 绑定推荐关系请求体 */
export interface BindReferralBody {
  shareId: number | string
}

/** 创建分享链接 */
export function createShareLink(data: ShareLinkCreateBody = {}) {
  return request.post<any, Result<ShareLinkVO>>('/share-links', data)
}

/** 解析短码 */
export function resolveShareLink(shortCode: string) {
  return request.get<any, Result<ShareLink>>(`/share-links/resolve/${shortCode}`)
}

/** 记录点击 */
export function clickShareLink(id: number | string) {
  return request.post<any, Result<void>>(`/share-links/${id}/click`)
}

/** 获取我的分享链接列表 */
export function getMyShareLinks() {
  return request.get<any, Result<ShareLink[]>>('/share-links/my')
}

/** 获取分享统计 */
export function getShareStats() {
  return request.get<any, Result<ShareStatsVO>>('/share-links/stats')
}

/** 绑定推荐关系 */
export function bindReferral(data: BindReferralBody) {
  return request.post<any, Result<void>>('/share-links/bind-referral', data)
}
