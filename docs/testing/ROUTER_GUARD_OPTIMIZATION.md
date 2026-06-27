# 投资人端路由守卫优化方案

> **创建时间**: 2026-06-27
> **问题**: Mock Token 设置后可能被路由守卫拦截，导致前端页面无法正确渲染
> **解决方案**: 优化投资人端路由守卫逻辑

---

## 一、问题分析

### 1.1 当前现象

**测试发现**: 
- 后端 API 正确返回标签数据 ✅
- 前端页面未渲染产品卡片 ⚠️
- 可能原因：路由守卫拦截了 Mock Token 设置后的页面跳转

### 1.2 路由守卫逻辑分析

**投资人端路由配置**: [router/index.js](file:///Users/pro/Documents/project/tamp/frontend/investor-app/src/router/index.js)

**典型路由守卫逻辑**:

```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  
  if (to.meta.requiresAuth && !token) {
    // 未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录，跳转到首页
    next('/')
  } else {
    // 正常跳转
    next()
  }
})
```

---

## 二、优化方案

### 2.1 方案 A：延迟路由跳转（推荐）

**原理**: 在设置 Token 后，添加延迟让路由守卫重新检查

**代码修改**: [scripts/run_e2e_tests_github_actions.py](file:///Users/pro/Documents/project/tamp/scripts/run_e2e_tests_github_actions.py)

```python
# 优化 Mock Token 设置逻辑
def set_token_with_retry(page, token, user_info):
    """设置 Token 并等待路由守卫生效"""
    
    page.evaluate("""
        localStorage.setItem('accessToken', '{}');
        localStorage.setItem('userInfo', '{}');
    """.format(token, json.dumps(user_info, ensure_ascii=False)))
    
    # 等待 2 秒，让路由守卫检测到 Token
    page.wait_for_timeout(2000)
    
    # 强制刷新页面
    page.reload()
    page.wait_for_load_state('networkidle')
```

---

### 2.2 方案 B：直接导航到目标页面

**原理**: 绕过路由守卫，直接导航到产品页面

**代码修改**:

```python
def test_investor_product_tags(self):
    """测试投资人端产品标签展示"""
    
    # 1. 先访问登录页
    page.goto('http://localhost:3003')
    page.wait_for_load_state('networkidle')
    
    # 2. 设置 Token
    page.evaluate(...)
    
    # 3. 直接导航到产品页（不刷新）
    page.goto('http://localhost:3003/products', wait_until='networkidle')
    
    # 4. 截图验证
    page.screenshot(...)
```

---

### 2.3 方案 C：使用 Vue Router API

**原理**: 使用 Vue Router API 强制跳转

**代码修改**:

```python
def test_investor_product_tags(self):
    """测试投资人端产品标签展示"""
    
    # 1. 访问投资人端
    page.goto('http://localhost:3003')
    page.wait_for_load_state('networkidle')
    
    # 2. 设置 Token
    page.evaluate("""
        localStorage.setItem('accessToken', '{}');
        localStorage.setItem('userInfo', '{}');
        
        // 强制触发路由跳转
        window.location.reload();
    """.format(token, user_info))
    
    # 3. 等待页面重新加载
    page.wait_for_load_state('networkidle', timeout=15000)
    
    # 4. 导航到产品页
    page.click('text=产品')  # 点击导航菜单
    page.wait_for_load_state('networkidle')
```

---

## 三、前端代码优化建议

### 3.1 优化路由守卫逻辑

**修改位置**: [frontend/investor-app/src/router/index.js](file:///Users/pro/Documents/project/tamp/frontend/investor-app/src/router/index.js)

**优化建议**:

```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  const userInfo = localStorage.getItem('userInfo')
  
  // 增加 userInfo 检查（避免仅有 token 但无用户信息）
  const isLoggedIn = token && userInfo
  
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && isLoggedIn) {
    next('/')
  } else {
    next()
  }
})
```

### 3.2 增加登录状态监听

**修改位置**: [frontend/investor-app/src/App.vue](file:///Users/pro/Documents/project/tamp/frontend/investor-app/src/App.vue)

**优化建议**:

```javascript
mounted() {
  // 监听 localStorage 变化（支持 Mock Token 设置）
  window.addEventListener('storage', (e) => {
    if (e.key === 'accessToken') {
      // Token 变化，触发路由跳转
      this.$router.push(e.newValue ? '/' : '/login')
    }
  })
}
```

---

## 四、测试验证

### 4.1 本地测试

```bash
# 1. 运行 Mock 登录
python3 scripts/mock_login_github_actions.py

# 2. 运行 E2E 测试
TEST_SCOPE=critical python3 scripts/run_e2e_tests_github_actions.py

# 3. 查看截图
open test-results/screenshots/investor_products.png
```

### 4.2 GitHub Actions 测试

访问: https://github.com/lfuzi81-create/tamp/actions

查看 Playwright E2E 测试步骤的日志和截图。

---

## 五、常见问题

### 5.1 设置 Token 后页面仍然跳转到登录页

**原因**: 路由守卫检测时机过早，Token 未生效

**解决**: 
1. 增加延迟（`wait_for_timeout(2000)`）
2. 强制刷新页面（`page.reload()`）
3. 直接导航到目标页面（绕过路由守卫）

### 5.2 截图显示空白页面

**原因**: 页面渲染时机过早，数据未加载完成

**解决**: 
1. 增加 `wait_for_load_state('networkidle')`
2. 等待特定元素出现（`page.wait_for_selector('.product-card')`）
3. 增加超时时间（`timeout=15000`）

---

## 六、联系支持

如有问题，请联系：
- **技术支持**: 查看 GitHub Issues: https://github.com/lfuzi81-create/tamp/issues
- **文档参考**: [GitHub Actions E2E Test Guide](file:///Users/pro/Documents/project/tamp/docs/testing/GITHUB_ACTIONS_E2E_TEST_GUIDE.md)

---

**创建者**: AI Agent
**创建时间**: 2026-06-27