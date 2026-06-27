import { request } from '@tamp/core'
import type { Result, LoginResult } from '@tamp/core'

/** 密码登录（账号统一为手机号） */
export function loginByPassword(data: { phone: string; password: string }) {
  return request.post<any, Result<LoginResult>>('/auth/login', {
    phone: data.phone,
    password: data.password
  })
}

/** 短信验证码登录 */
export function loginBySms(data: { phone: string; smsCode: string; shopId?: number | string }) {
  return request.post<any, Result<LoginResult>>('/auth/sms-login', data)
}

/** 发送短信验证码 */
export function sendSmsCode(data: { phone: string }) {
  return request.post<any, Result<void>>('/auth/sms-code', data)
}

/** 修改密码 */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put<any, Result<void>>('/auth/change-password', data)
}

/** 找回密码 */
export function resetPassword(data: { phone: string; smsCode: string; newPassword: string }) {
  return request.post<any, Result<void>>('/auth/reset-password', data)
}

/** 登出 */
export function logout() {
  return request.post<any, Result<void>>('/auth/logout')
}

/** 刷新 Token */
export function refreshToken(data: { refreshToken: string }) {
  return request.post<any, Result<{ accessToken: string; refreshToken: string }>>('/auth/refresh-token', data)
}
