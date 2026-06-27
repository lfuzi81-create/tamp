import { request } from '@tamp/core'
import type { Result } from '@tamp/core'

/** 获取个人信息 */
export function getProfile() {
  return request.get<any, Result<any>>('/user/profile')
}

/** 编辑个人资料 */
export function updateProfile(data: { realName?: string; avatar?: string }) {
  return request.put<any, Result<void>>('/user/profile', data)
}

/** 修改手机号 */
export function changePhone(data: { newPhone: string }) {
  return request.put<any, Result<void>>('/user/phone', data)
}

/** 店铺管理员获取店铺信息 */
export function getShopInfo() {
  return request.get<any, Result<any>>('/user/shop-info')
}

/** 投资人获取家办信息 */
export function getInvestorOfficeInfo() {
  return request.get<any, Result<any>>('/user/investor-office')
}

/** 获取当前用户权限列表 */
export function getPermissions() {
  return request.get<any, Result<any[]>>('/permissions')
}

/** 提交咨询 */
export function submitConsult(data: {
  name: string
  phone: string
  content: string
  productName?: string
}) {
  return request.post<any, Result<void>>('/user/consult', data)
}
