export const API_BASE = '/api'

export const BRAND_NAME = 'TAMP'

export const STORAGE_KEYS = {
  TOKEN: 'tamp_token',
  USER_INFO: 'tamp_user_info',
  LOGIN_TIME: 'tamp_login_time',
  FIRST_LOGIN: 'tamp_first_login',
  SHARED_DATA: 'tamp_shared_data_v2',
  DECOR_CONFIG: 'tamp_decor_config',
  SHOP_ID: 'tamp_shop_id',
} as const

// 会话有效期：7 天（唯一来源，供 router-guard 与 request 共享）
export const SESSION_DURATION = 7 * 24 * 60 * 60 * 1000 // 7天

export enum ErrorCode {
  SUCCESS = 0,
  UNKNOWN_ERROR = 1000,
  PARAM_INVALID = 1001,
  NOT_FOUND = 1002,
  FORBIDDEN = 1003,
  UNAUTHORIZED = 1004,
  TOKEN_EXPIRED = 1005,
  AUTH_LOGIN_FAILED = 2000,
  AUTH_USER_DISABLED = 2001,
  BIZ_PRODUCT_NOT_FOUND = 3000,
  BIZ_CLIENT_NOT_FOUND = 3003,
  // ... 常用错误码
}
