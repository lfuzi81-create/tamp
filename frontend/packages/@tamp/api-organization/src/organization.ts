import { request } from '@tamp/core'
import type { Result, PageResult, PaginateParams } from '@tamp/core'

/** 家办 */
export interface FamilyOffice {
  id: number | string
  name: string
  contactName?: string
  contactPhone?: string
  address?: string
  status?: number | string
  shopCount?: number
  staffCount?: number
  clientCount?: number
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 家办详情 VO */
export interface OfficeDetailVO extends FamilyOffice {
  shops?: Shop[]
  staff?: Staff[]
  [key: string]: any
}

/** 店铺 */
export interface Shop {
  id: number | string
  name: string
  officeId: number | string
  officeName?: string
  contactName?: string
  contactPhone?: string
  address?: string
  status?: number | string
  staffCount?: number
  clientCount?: number
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 员工 */
export interface Staff {
  id: number | string
  phone: string
  realName?: string
  roleType?: string
  officeId?: number | string
  officeName?: string
  shopId?: number | string
  shopName?: string
  status?: number | string
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 家办查询参数 */
export interface OfficeQueryParams extends PaginateParams {}

/** 店铺查询参数 */
export interface ShopQueryParams extends PaginateParams {
  officeId?: number | string
  status?: number | string
}

/** 员工查询参数 */
export interface StaffQueryParams extends PaginateParams {
  roleType?: string
  officeId?: number | string
}

// ===== 家办 =====

/** 分页查询家办 */
export function getOffices(params: OfficeQueryParams) {
  return request.get<any, Result<PageResult<FamilyOffice>>>('/offices', { params })
}

/** 获取家办详情（基础） */
export function getOfficeDetail(id: number | string) {
  return request.get<any, Result<FamilyOffice>>(`/offices/${id}`)
}

/** 获取家办详情（含店铺/员工） */
export function getOfficeDetailVO(id: number | string) {
  return request.get<any, Result<OfficeDetailVO>>(`/offices/${id}/detail`)
}

/** 创建家办 */
export function createOffice(data: Partial<FamilyOffice>) {
  return request.post<any, Result<FamilyOffice>>('/offices', data)
}

/** 更新家办 */
export function updateOffice(id: number | string, data: Partial<FamilyOffice>) {
  return request.put<any, Result<FamilyOffice>>(`/offices/${id}`, data)
}

/** 切换家办状态 */
export function updateOfficeStatus(id: number | string) {
  return request.put<any, Result<void>>(`/offices/${id}/status`)
}

/** 获取家办下店铺列表 */
export function getOfficeShops(id: number | string) {
  return request.get<any, Result<Shop[]>>(`/offices/${id}/shops`)
}

// ===== 店铺 =====

/** 分页查询店铺 */
export function getShops(params: ShopQueryParams) {
  return request.get<any, Result<PageResult<Shop>>>('/shops', { params })
}

/** 获取店铺详情 */
export function getShopDetail(id: number | string) {
  return request.get<any, Result<Shop>>(`/shops/${id}`)
}

/** 创建店铺 */
export function createShop(data: Partial<Shop>) {
  return request.post<any, Result<Shop>>('/shops', data)
}

/** 更新店铺 */
export function updateShop(id: number | string, data: Partial<Shop>) {
  return request.put<any, Result<Shop>>(`/shops/${id}`, data)
}

/** 切换店铺状态 */
export function updateShopStatus(id: number | string) {
  return request.put<any, Result<void>>(`/shops/${id}/status`)
}

// ===== 员工 =====

/** 分页查询员工 */
export function getStaffList(params: StaffQueryParams) {
  return request.get<any, Result<PageResult<Staff>>>('/staff', { params })
}

/** 按家办查询员工 */
export function getStaffByOffice(officeId: number | string, params?: PaginateParams) {
  return request.get<any, Result<PageResult<Staff>>>(`/staff/office/${officeId}`, { params })
}

/** 按店铺查询员工 */
export function getStaffByShop(shopId: number | string, params?: PaginateParams) {
  return request.get<any, Result<PageResult<Staff>>>(`/staff/shop/${shopId}`, { params })
}

/** 创建员工 */
export function createStaff(data: Partial<Staff>) {
  return request.post<any, Result<Staff>>('/staff', data)
}

/** 更新员工 */
export function updateStaff(id: number | string, data: Partial<Staff>) {
  return request.put<any, Result<Staff>>(`/staff/${id}`, data)
}

/** 切换员工状态 */
export function updateStaffStatus(id: number | string) {
  return request.put<any, Result<void>>(`/staff/${id}/status`)
}

/** 删除员工 */
export function deleteStaff(id: number | string) {
  return request.delete<any, Result<void>>(`/staff/${id}`)
}
