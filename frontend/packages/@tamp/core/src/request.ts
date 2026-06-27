import axios from 'axios'
import type { Result } from './types/api'
import { STORAGE_KEYS, SESSION_DURATION } from './constants'

const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

const request = axios.create({
  baseURL: `${API_BASE}/api`,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
request.interceptors.request.use(config => {
  // 检查登录是否超过7天（请求层兜底，防止用户开着页面不跳转）
  const loginTime = localStorage.getItem(STORAGE_KEYS.LOGIN_TIME)
  if (loginTime) {
    const elapsed = Date.now() - parseInt(loginTime)
    if (elapsed > SESSION_DURATION) {
      // 超过7天，清除登录状态
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER_INFO)
      localStorage.removeItem(STORAGE_KEYS.LOGIN_TIME)
      window.location.href = '/login'
      return config
    }
  }

  const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
  if (token) config.headers.Authorization = `Bearer ${token}`

  // 自动携带当前店铺 ID（投资人端定向访问店铺）
  const shopId = localStorage.getItem(STORAGE_KEYS.SHOP_ID)
  if (shopId) config.headers['X-Shop-Id'] = shopId
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data as Result
    if (res.code !== 0) {
      console.warn(`[API Error] ${res.code}: ${res.message}`)
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER_INFO)
      localStorage.removeItem(STORAGE_KEYS.LOGIN_TIME)
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
