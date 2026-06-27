# GitHub Actions E2E 自动化测试 - 快速开始

> **完全自动化，无需人工操作**

---

## 🚀 快速开始

### 方式 1: Push 触发（推荐）

```bash
# 1. 提交代码
git add .
git commit -m "Add new feature"
git push origin develop

# 2. GitHub Actions 自动运行测试
# 3. 查看测试结果: https://github.com/YOUR_REPO/YOUR_REPO/actions
```

### 方式 2: Pull Request 触发

```bash
# 1. 创建 PR
gh pr create --title "Fix bug" --body "Description"

# 2. GitHub Actions 自动运行测试并评论到 PR
# 3. 查看 PR 评论中的测试结果
```

### 方式 3: 手动触发

```bash
# 使用 gh CLI
gh workflow run e2e-test.yml -f test_scope=critical

# 或访问 GitHub Actions 页面手动运行
# https://github.com/YOUR_REPO/YOUR_REPO/actions/workflows/e2e-test.yml
```

---

## 📊 测试覆盖范围

### 关键场景测试 (critical)

- ✅ 后端 API 健康检查
- ✅ 三端前端渲染验证
- ✅ 投资人端产品标签展示 (BUG-03 验证)
- ✅ Mock 登录机制验证

### 冒烟测试 (smoke)

- ✅ 后端 API 健康检查
- ✅ 三端前端渲染验证

---

## 📦 测试报告查看

### GitHub Actions 页面

```
https://github.com/YOUR_REPO/YOUR_REPO/actions/runs/{RUN_ID}
```

### PR 评论（自动生成）

```
## E2E Test Results

# TAMP E2E 测试结果汇总

**测试时间**: 2026-06-27T21:35:00
**测试范围**: critical

## 测试统计

| 指标 | 数量 |
|------|------|
| 测试总数 | 3 |
| ✓ 通过 | 2 |
| ⚠ 部分通过 | 1 |
```

### 下载截图和报告

```bash
# 下载测试截图
gh run download {RUN_ID} -n playwright-screenshots

# 下载测试报告
gh run download {RUN_ID} -n e2e-test-report
```

---

## 🔧 本地模拟测试

```bash
# 1. Mock 登录
python3 scripts/mock_login_github_actions.py

# 2. 运行 E2E 测试
TEST_SCOPE=critical python3 scripts/run_e2e_tests_github_actions.py

# 3. 查看报告
open test-results/report.html
```

---

## 📄 详细文档

查看完整使用说明: [GITHUB_ACTIONS_E2E_TEST_GUIDE.md](file:///Users/pro/Documents/project/tamp/docs/testing/GITHUB_ACTIONS_E2E_TEST_GUIDE.md)

---

## 🎯 优势对比

| 传统人工测试 | GitHub Actions 自动化 |
|------------|---------------------|
| ✗ 需要人工登录三端 | ✓ 自动 Mock 登录 |
| ✗ 需要人工截图 | ✓ Playwright 自动截图 |
| ✗ 需要人工记录 | ✓ 自动生成报告 |
| ✗ 测试不易追溯 | ✓ PR 自动评论结果 |

---

**创建时间**: 2026-06-27
**自动化程度**: 完全自动化，无需人工干预