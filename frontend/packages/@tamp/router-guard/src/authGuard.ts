import type { Router } from 'vue-router'
import { useUserStore } from '@tamp/stores'
import { SESSION_DURATION, clearAuth, getLoginTime } from '@tamp/core'

/**
 * 认证路由守卫 — 检查 Token 有效性与会话超时
 * 未登录或会话超过 SESSION_DURATION 则重定向到 /login
 */
export function setupAuthGuard(router: Router) {
  const whiteList = ['/login', '/s/']

  router.beforeEach((to, _from, next) => {
    const userStore = useUserStore()

    if (whiteList.some(prefix => to.path.startsWith(prefix))) {
      next()
      return
    }

    if (!userStore.isLoggedIn || !userStore.token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    // 会话超时检查（路由层，与 request.ts 请求层形成两层兜底）
    const loginTime = getLoginTime()
    if (loginTime === null || Date.now() - loginTime > SESSION_DURATION) {
      clearAuth()
      userStore.clearState()
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    next()
  })
}
