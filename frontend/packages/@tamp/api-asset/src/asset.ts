import { request } from '@tamp/core'
import type { Result, PageResult, PaginateParams } from '@tamp/core'

/** 资产 */
export interface Asset {
  id: number | string
  clientId?: number | string
  productName?: string
  amount?: number
  status?: string
  isAuthorized?: boolean
  authScope?: string
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 资产汇总 VO */
export interface AssetSummaryVO {
  totalAmount?: number
  authorizedAmount?: number
  unauthorizedAmount?: number
  count?: number
  authorizedCount?: number
  [key: string]: any
}

/** 资产查询参数 */
export interface AssetQueryParams extends PaginateParams {}

/** 资产授权请求体 */
export interface AssetAuthBody {
  isAuthorized: boolean
  authScope?: string
}

/** 全局授权请求体 */
export interface AssetGlobalAuthBody {
  authorized: boolean
}

/** 分页查询资产 */
export function getAssets(params: AssetQueryParams) {
  return request.get<any, Result<PageResult<Asset>>>('/assets', { params })
}

/** 获取资产汇总 */
export function getAssetSummary() {
  return request.get<any, Result<AssetSummaryVO>>('/assets/summary')
}

/** 获取资产详情 */
export function getAssetDetail(id: number | string) {
  return request.get<any, Result<Asset>>(`/assets/${id}`)
}

/** 新增资产 */
export function createAsset(data: Partial<Asset>) {
  return request.post<any, Result<Asset>>('/assets', data)
}

/** 更新资产 */
export function updateAsset(id: number | string, data: Partial<Asset>) {
  return request.put<any, Result<Asset>>(`/assets/${id}`, data)
}

/** 删除资产 */
export function deleteAsset(id: number | string) {
  return request.delete<any, Result<void>>(`/assets/${id}`)
}

/** 更新资产授权 */
export function updateAssetAuth(id: number | string, data: AssetAuthBody) {
  return request.put<any, Result<void>>(`/assets/${id}/auth`, data)
}

/** 授权资产（快捷方法） */
export function authorizeAsset(id: number | string, authScope?: string) {
  return updateAssetAuth(id, { isAuthorized: true, authScope })
}

/** 拒绝资产授权（快捷方法） */
export function rejectAsset(id: number | string) {
  return updateAssetAuth(id, { isAuthorized: false })
}

/** 切换全局授权 */
export function updateGlobalAuth(data: AssetGlobalAuthBody) {
  return request.put<any, Result<void>>('/assets/global-auth', data)
}
