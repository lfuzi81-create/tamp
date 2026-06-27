import { request } from '@tamp/core'
import type { Result, PageResult, PaginateParams } from '@tamp/core'

/** 客户 */
export interface Client {
  id: number | string
  name: string
  phone?: string
  officeId?: number | string
  officeName?: string
  shopId?: number | string
  shopName?: string
  tags?: string[]
  remark?: string
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 客户时间线事件 */
export interface ClientTimeline {
  id: number | string
  clientId: number | string
  eventType?: string
  content?: string
  operator?: string
  eventTime?: string
  createdAt?: string
  [key: string]: any
}

/** 客户资产 */
export interface ClientAsset {
  id: number | string
  clientId: number | string
  productName?: string
  amount?: number
  status?: string
  isAuthorized?: boolean
  authScope?: string
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 客户标签 */
export interface ClientTag {
  tagName: string
  tagColor?: string
}

/** 客户查询参数 */
export interface ClientQueryParams extends PaginateParams {
  officeId?: number | string
  shopId?: number | string
  tag?: string
}

/** 分页查询客户 */
export function getClients(params: ClientQueryParams) {
  return request.get<any, Result<PageResult<Client>>>('/clients', { params })
}

/** 获取所有客户标签 */
export function getAllClientTags() {
  return request.get<any, Result<string[]>>('/clients/tags')
}

/** 获取客户详情 */
export function getClientDetail(id: number | string) {
  return request.get<any, Result<Client>>(`/clients/${id}`)
}

/** 创建客户 */
export function createClient(data: Partial<Client>) {
  return request.post<any, Result<Client>>('/clients', data)
}

/** 更新客户 */
export function updateClient(id: number | string, data: Partial<Client>) {
  return request.put<any, Result<Client>>(`/clients/${id}`, data)
}

/** 删除客户 */
export function deleteClient(id: number | string) {
  return request.delete<any, Result<void>>(`/clients/${id}`)
}

/** 获取客户时间线 */
export function getClientTimeline(id: number | string, params?: PaginateParams) {
  return request.get<any, Result<ClientTimeline[]>>(`/clients/${id}/timeline`, { params })
}

/** 新增客户时间线事件 */
export function addClientTimeline(id: number | string, data: Partial<ClientTimeline>) {
  return request.post<any, Result<ClientTimeline>>(`/clients/${id}/timeline`, data)
}

/** 获取客户资产（分页） */
export function getClientAssets(id: number | string, params?: PaginateParams) {
  return request.get<any, Result<PageResult<ClientAsset>>>(`/clients/${id}/assets`, { params })
}

/** 新增客户资产 */
export function createClientAsset(clientId: number | string, data: Partial<ClientAsset>) {
  return request.post<any, Result<ClientAsset>>(`/clients/${clientId}/assets`, data)
}

/** 更新客户资产 */
export function updateClientAsset(clientId: number | string, assetId: number | string, data: Partial<ClientAsset>) {
  return request.put<any, Result<ClientAsset>>(`/clients/${clientId}/assets/${assetId}`, data)
}

/** 删除客户资产 */
export function deleteClientAsset(clientId: number | string, assetId: number | string) {
  return request.delete<any, Result<void>>(`/clients/${clientId}/assets/${assetId}`)
}

/** 获取客户标签列表 */
export function getClientTags(id: number | string) {
  return request.get<any, Result<ClientTag[]>>(`/clients/${id}/tags`)
}

/** 新增客户标签 */
export function addClientTag(clientId: number | string, tagName: string, tagColor?: string) {
  return request.post<any, Result<void>>(`/clients/${clientId}/tags`, { tagName, tagColor })
}

/** 删除客户标签 */
export function removeClientTag(clientId: number | string, tagIdx: number) {
  return request.delete<any, Result<void>>(`/clients/${clientId}/tags/${tagIdx}`)
}
