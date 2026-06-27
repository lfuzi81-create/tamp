import { defineStore } from 'pinia'
import { setToken, getToken, clearAuth, setUserInfo, getUserInfo, setLoginTime } from '@tamp/core'
import type { UserInfo } from '@tamp/core'
import { loginByPassword, loginBySms, logout as apiLogout } from '@tamp/api-auth'
import { getProfile, getPermissions } from '@tamp/api-user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() as UserInfo | null,
    isLoggedIn: !!getToken(),
    permissions: [] as string[]
  }),

  actions: {
    /** 登录 */
    async login(loginData: { phone: string; password: string } | { phone: string; smsCode: string; shopId?: number | string }) {
      const isSms = 'smsCode' in loginData
      const res = isSms
        ? await loginBySms(loginData)
        : await loginByPassword(loginData)

      if (res.code === 0 && res.data) {
        this.token = res.data.accessToken
        this.userInfo = {
          ...res.data.userInfo,
          mustChangePassword: res.data.needChangePassword
        }
        this.isLoggedIn = true
        setToken(res.data.accessToken)
        setUserInfo(this.userInfo)
        setLoginTime()
        await this.fetchPermissions()
      }
      return res
    },

    /** 登出 */
    async logout() {
      try { await apiLogout() } catch {}
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false
      this.permissions = []
      clearAuth()
    },

    /** 刷新用户信息 */
    async fetchProfile() {
      const res = await getProfile()
      if (res.code === 0 && res.data) {
        this.userInfo = res.data
        setUserInfo(res.data)
      }
      await this.fetchPermissions()
    },

    /** 获取权限列表 */
    async fetchPermissions() {
      try {
        const res = await getPermissions()
        if (res.code === 0 && res.data) {
          this.permissions = res.data
            .filter((p: any) => p.allowed === 1 || p.allowed === true)
            .map((p: any) => p.resourceCode)
        }
      } catch (e) {
        this.permissions = []
      }
    },

    /** 清除状态 */
    clearState() {
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false
      this.permissions = []
      clearAuth()
    }
  }
})
