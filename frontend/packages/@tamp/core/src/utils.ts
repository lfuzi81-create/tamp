import dayjs from 'dayjs'
import { STORAGE_KEYS } from './constants'

// 格式化日期
export function formatDate(date: string | Date, fmt = 'YYYY-MM-DD HH:mm:ss'): string {
  return dayjs(date).format(fmt)
}

// 存储 Token
export function setToken(token: string): void {
  localStorage.setItem(STORAGE_KEYS.TOKEN, token)
}

// 获取 Token
export function getToken(): string | null {
  return localStorage.getItem(STORAGE_KEYS.TOKEN)
}

// 清除 Token
export function clearAuth(): void {
  localStorage.removeItem(STORAGE_KEYS.TOKEN)
  localStorage.removeItem(STORAGE_KEYS.USER_INFO)
  localStorage.removeItem(STORAGE_KEYS.LOGIN_TIME)
  localStorage.removeItem(STORAGE_KEYS.SHOP_ID)
}

// 存储用户信息
export function setUserInfo(info: object): void {
  localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(info))
}

// 获取用户信息
export function getUserInfo(): Record<string, any> | null {
  const raw = localStorage.getItem(STORAGE_KEYS.USER_INFO)
  return raw ? JSON.parse(raw) : null
}

// 存储登录时间
export function setLoginTime(): void {
  localStorage.setItem(STORAGE_KEYS.LOGIN_TIME, String(Date.now()))
}

// 获取登录时间
export function getLoginTime(): number | null {
  const raw = localStorage.getItem(STORAGE_KEYS.LOGIN_TIME)
  return raw ? parseInt(raw) : null
}
