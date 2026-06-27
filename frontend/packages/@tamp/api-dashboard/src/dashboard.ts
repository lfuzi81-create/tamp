import { request } from '@tamp/core'
import type { Result } from '@tamp/core'

/** 仪表盘统计 VO */
export interface DashboardStatsVO {
  totalOffices?: number
  totalShops?: number
  totalStaff?: number
  totalClients?: number
  totalProducts?: number
  totalContents?: number
  [key: string]: any
}

/** 家办排名 VO */
export interface OfficeRankingVO {
  officeId: number | string
  officeName: string
  clientCount?: number
  shopCount?: number
  rank?: number
  [key: string]: any
}

/** 店铺排名 VO */
export interface ShopRankingVO {
  shopId: number | string
  shopName: string
  officeName?: string
  clientCount?: number
  rank?: number
  [key: string]: any
}

/** 客户趋势 VO */
export interface ClientTrendVO {
  dates?: string[]
  counts?: number[]
  total?: number
  [key: string]: any
}

/** 客户趋势查询参数 */
export interface ClientTrendQueryParams {
  startDate: string
  endDate: string
}

/** 排名查询参数 */
export interface RankingQueryParams {
  limit?: number
}

/** 获取仪表盘统计 */
export function getDashboardStats() {
  return request.get<any, Result<DashboardStatsVO>>('/dashboard/stats')
}

/** 获取家办排名 */
export function getOfficeRanking(params?: RankingQueryParams) {
  return request.get<any, Result<OfficeRankingVO[]>>('/dashboard/office-ranking', { params })
}

/** 获取店铺排名 */
export function getShopRanking(params?: RankingQueryParams) {
  return request.get<any, Result<ShopRankingVO[]>>('/dashboard/shop-ranking', { params })
}

/** 获取客户趋势 */
export function getClientTrend(params: ClientTrendQueryParams) {
  return request.get<any, Result<ClientTrendVO>>('/dashboard/client-trend', { params })
}

/** 导出报表（CSV blob） */
export function exportDashboardReport(params?: ClientTrendQueryParams) {
  return request.get<any, Blob>('/dashboard/export', { params, responseType: 'blob' })
}
