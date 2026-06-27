export interface UserInfo {
  userId: number | string
  phone: string
  role: string
  realName: string
  avatar: string
  officeId: number | string
  shopId: number | string
  mustChangePassword?: boolean
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  needChangePassword: boolean
  userInfo: UserInfo
}
