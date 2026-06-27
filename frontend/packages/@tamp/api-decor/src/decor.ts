import { request } from '@tamp/core'
import type { Result } from '@tamp/core'

/** 装修模块 */
export interface DecorModule {
  key: string
  name?: string
  visible?: boolean
  sortOrder?: number
  [key: string]: any
}

/** 装修导航项 */
export interface DecorNavigation {
  key: string
  name?: string
  icon?: string
  path?: string
  visible?: boolean
  sortOrder?: number
  [key: string]: any
}

/** 装修货架选择 */
export interface DecorShelfSelection {
  type?: 'product' | 'content'
  targetId?: number | string
  targetName?: string
  [key: string]: any
}

/** 装修配置 */
export interface DecorConfig {
  id: number | string
  scopeType?: 'investor' | 'shop' | 'office'
  scopeId?: number | string
  modules?: DecorModule[]
  navigation?: DecorNavigation[]
  shelfSelections?: DecorShelfSelection[]
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 装修配置更新请求体 */
export interface DecorConfigUpdateBody {
  scopeType?: 'investor' | 'shop' | 'office'
  scopeId?: number | string
  modules?: DecorModule[]
  navigation?: DecorNavigation[]
  shelfSelections?: DecorShelfSelection[]
  [key: string]: any
}

/** 获取投资人装修配置 */
export function getInvestorDecorConfig() {
  return request.get<any, Result<DecorConfig>>('/decor-config/investor')
}

/** 获取店铺装修配置 */
export function getShopDecorConfig(shopId: number | string) {
  return request.get<any, Result<DecorConfig>>(`/decor-config/shop/${shopId}`)
}

/** 公开获取店铺装修配置（无需登录，用于店铺预览页） */
export function getPublicShopDecorConfig(shopId: number | string) {
  return request.get<any, Result<DecorConfig>>(`/shop-preview/${shopId}/decor-config`)
}

/** 获取家办装修配置 */
export function getOfficeDecorConfig(officeId: number | string) {
  return request.get<any, Result<DecorConfig>>(`/decor-config/office/${officeId}`)
}

/** 创建装修配置 */
export function createDecorConfig(data: DecorConfigUpdateBody) {
  return request.post<any, Result<DecorConfig>>('/decor-config', data)
}

/** 更新装修配置 */
export function updateDecorConfig(id: number | string, data: DecorConfigUpdateBody) {
  return request.put<any, Result<DecorConfig>>(`/decor-config/${id}`, data)
}

/** 更新装修模块 */
export function updateDecorModules(id: number | string, modules: DecorModule[]) {
  return request.put<any, Result<DecorConfig>>(`/decor-config/${id}/modules`, { modules })
}

/** 更新装修导航 */
export function updateDecorNavigation(id: number | string, navigation: DecorNavigation[]) {
  return request.put<any, Result<DecorConfig>>(`/decor-config/${id}/navigation`, { navigation })
}

/** 更新装修货架选择 */
export function updateDecorShelfSelections(id: number | string, shelfSelections: DecorShelfSelection[]) {
  return request.put<any, Result<DecorConfig>>(`/decor-config/${id}/shelf-selections`, { shelfSelections })
}
