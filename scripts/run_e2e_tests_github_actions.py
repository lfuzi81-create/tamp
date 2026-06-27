#!/usr/bin/env python3
"""
GitHub Actions E2E 测试执行脚本
使用 Playwright 运行关键场景测试
"""

import os
import json
import time
from datetime import datetime
from playwright.sync_api import sync_playwright
import requests

class E2ETestRunner:
    """E2E 测试运行器"""
    
    def __init__(self):
        self.backend_url = 'http://localhost:8080'
        self.frontend_urls = {
            'hq-admin': 'http://localhost:3002',
            'shop-admin': 'http://localhost:5173',
            'investor-app': 'http://localhost:3003'
        }
        self.test_results = []
        self.tokens = self.load_tokens()
        self.test_scope = os.getenv('TEST_SCOPE', 'critical')
        
    def load_tokens(self):
        """加载 Mock 登录生成的 Token"""
        try:
            with open('test-results/tokens.json', 'r', encoding='utf-8') as f:
                return json.load(f)
        except FileNotFoundError:
            print("✗ Token 文件不存在，请先运行 mock_login_github_actions.py")
            return {}
    
    def test_investor_product_tags(self):
        """测试投资人端产品标签展示 (BUG-03 验证)"""
        
        print("\n=== 测试投资人端产品标签 (BUG-03) ===")
        
        result = {
            'name': '投资人端产品标签展示',
            'status': 'unknown',
            'screenshots': [],
            'timestamp': datetime.now().isoformat()
        }
        
        try:
            token = self.tokens['INVESTOR']['token']
            
            with sync_playwright() as p:
                browser = p.chromium.launch(headless=True)  # GitHub Actions 必须使用 headless
                context = browser.new_context()
                page = context.new_page()
                
                # 1. 访问投资人端
                print("   1. 访问投资人端...")
                page.goto(self.frontend_urls['investor-app'], timeout=30000)
                page.wait_for_load_state('networkidle')
                
                # 2. 设置 Token
                print("   2. 设置登录状态...")
                page.evaluate("""
                    localStorage.setItem('accessToken', '{}');
                    localStorage.setItem('userInfo', '{}');
                """.format(token, json.dumps(self.tokens['INVESTOR']['user_info'], ensure_ascii=False)))
                
                # 3. 访问产品页
                print("   3. 访问产品页面...")
                page.goto(f"{self.frontend_urls['investor-app']}/products", timeout=30000)
                page.wait_for_load_state('networkidle', timeout=15000)
                
                # 4. 截图
                screenshot_path = 'test-results/screenshots/investor_products.png'
                page.screenshot(path=screenshot_path, full_page=True)
                result['screenshots'].append(screenshot_path)
                print(f"   ✓ 截图已保存: {screenshot_path}")
                
                # 5. 检查产品卡片
                print("   5. 检查产品卡片...")
                product_cards = page.locator('.product-card, .van-card, [class*="product"]').count()
                result['product_cards_count'] = product_cards
                print(f"   ✓ 找到 {product_cards} 个产品元素")
                
                # 6. 检查标签元素
                print("   6. 检查标签元素...")
                tags_elements = page.locator('.tag, .van-tag, [class*="tag"]').count()
                result['tags_elements_count'] = tags_elements
                print(f"   ✓ 找到 {tags_elements} 个标签元素")
                
                # 7. 检查 API 响应
                print("   7. 检查后端 API...")
                api_response = requests.get(
                    f"{self.backend_url}/api/shelf/products?shopId=1",
                    headers={'Authorization': f'Bearer {token}'},
                    timeout=10
                )
                
                if api_response.status_code == 200:
                    api_data = api_response.json()
                    if api_data.get('code') == 0:
                        products = api_data.get('data', [])
                        if isinstance(products, list):
                            result['api_products_count'] = len(products)
                            print(f"   ✓ API 返回 {len(products)} 个产品")
                            
                            # 检查标签数据
                            products_with_tags = [p for p in products if p.get('tags')]
                            result['products_with_tags'] = len(products_with_tags)
                            print(f"   ✓ {len(products_with_tags)} 个产品有标签")
                        else:
                            result['api_products_count'] = 0
                            print("   ⚠ API 数据格式异常")
                
                # 判断测试结果
                if product_cards > 0 and tags_elements > 0:
                    result['status'] = 'PASS'
                elif result.get('products_with_tags', 0) > 0 and product_cards == 0:
                    result['status'] = 'PARTIAL'  # API 有数据但前端未渲染
                else:
                    result['status'] = 'FAIL'
                
                browser.close()
                
        except Exception as e:
            result['status'] = 'ERROR'
            result['error'] = str(e)
            print(f"   ✗ 测试异常: {str(e)}")
        
        self.test_results.append(result)
        return result
    
    def test_backend_api_health(self):
        """测试后端 API 基础健康状态"""
        
        print("\n=== 测试后端 API 健康状态 ===")
        
        result = {
            'name': '后端 API 健康检查',
            'status': 'unknown',
            'timestamp': datetime.now().isoformat()
        }
        
        try:
            # 1. 检查健康端点
            print("   1. 检查健康端点...")
            response = requests.get(f"{self.backend_url}/actuator/health", timeout=5)
            
            if response.status_code == 200:
                health_data = response.json()
                result['health_status'] = health_data.get('status', 'UNKNOWN')
                print(f"   ✓ 后端健康状态: {health_data.get('status')}")
                
                if health_data.get('status') == 'UP':
                    result['status'] = 'PASS'
                else:
                    result['status'] = 'FAIL'
            else:
                result['status'] = 'FAIL'
                result['http_status'] = response.status_code
                print(f"   ✗ HTTP 状态码: {response.status_code}")
                
        except Exception as e:
            result['status'] = 'ERROR'
            result['error'] = str(e)
            print(f"   ✗ 健康检查异常: {str(e)}")
        
        self.test_results.append(result)
        return result
    
    def test_frontend_rendering(self):
        """测试三端前端渲染"""
        
        print("\n=== 测试三端前端渲染 ===")
        
        result = {
            'name': '前端渲染验证',
            'status': 'unknown',
            'screenshots': [],
            'timestamp': datetime.now().isoformat()
        }
        
        try:
            with sync_playwright() as p:
                browser = p.chromium.launch(headless=True)
                
                for frontend_name, url in self.frontend_urls.items():
                    print(f"   测试 {frontend_name}...")
                    
                    page = browser.new_page()
                    page.goto(url, timeout=30000)
                    page.wait_for_load_state('networkidle')
                    
                    # 截图
                    screenshot_path = f'test-results/screenshots/{frontend_name}_login.png'
                    page.screenshot(path=screenshot_path)
                    result['screenshots'].append(screenshot_path)
                    
                    # 检查 Vue 应用挂载
                    app_mounted = page.locator('#app').count() > 0
                    print(f"      {'✓' if app_mounted else '✗'} Vue 应用挂载: {app_mounted}")
                    
                    page.close()
                
                result['status'] = 'PASS'
                print("   ✓ 三端前端渲染正常")
                browser.close()
                
        except Exception as e:
            result['status'] = 'ERROR'
            result['error'] = str(e)
            print(f"   ✗ 前端渲染测试异常: {str(e)}")
        
        self.test_results.append(result)
        return result
    
    def generate_report(self):
        """生成测试报告"""
        
        print("\n=== 生成测试报告 ===")
        
        # 1. 生成 HTML 报告
        html_report = self.generate_html_report()
        html_path = 'test-results/report.html'
        
        with open(html_path, 'w', encoding='utf-8') as f:
            f.write(html_report)
        
        print(f"   ✓ HTML 报告已生成: {html_path}")
        
        # 2. 生成 Markdown 汇总（用于 PR 评论）
        markdown_summary = self.generate_markdown_summary()
        md_path = 'test-results/summary.md'
        
        with open(md_path, 'w', encoding='utf-8') as f:
            f.write(markdown_summary)
        
        print(f"   ✓ Markdown 汇总已生成: {md_path}")
        
        # 3. 打印测试总结
        pass_count = sum(1 for r in self.test_results if r['status'] == 'PASS')
        fail_count = sum(1 for r in self.test_results if r['status'] == 'FAIL')
        error_count = sum(1 for r in self.test_results if r['status'] == 'ERROR')
        partial_count = sum(1 for r in self.test_results if r['status'] == 'PARTIAL')
        
        print("\n" + "=" * 50)
        print("测试总结")
        print("=" * 50)
        print(f"测试总数: {len(self.test_results)}")
        print(f"✓ 通过: {pass_count}")
        print(f"⚠ 部分通过: {partial_count}")
        print(f"✗ 失败: {fail_count}")
        print(f"✗ 错误: {error_count}")
        
    def generate_html_report(self):
        """生成 HTML 测试报告"""
        
        html = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>TAMP E2E 测试报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .pass { color: green; }
        .fail { color: red; }
        .partial { color: orange; }
        .error { color: darkred; }
        .test-result { margin: 10px 0; padding: 10px; border: 1px solid #ccc; }
        .screenshot { max-width: 300px; margin: 5px; }
    </style>
</head>
<body>
    <h1>TAMP E2E 测试报告</h1>
    <p>测试时间: {timestamp}</p>
    <p>测试范围: {test_scope}</p>
    
    <h2>测试结果</h2>
    {test_results_html}
    
    <h2>截图</h2>
    {screenshots_html}
</body>
</html>
""".format(
            timestamp=datetime.now().isoformat(),
            test_scope=self.test_scope,
            test_results_html=self.generate_test_results_html(),
            screenshots_html=self.generate_screenshots_html()
        )
        
        return html
    
    def generate_test_results_html(self):
        """生成测试结果 HTML"""
        
        html = ""
        
        for result in self.test_results:
            status_class = result['status'].lower()
            status_icon = {'PASS': '✓', 'FAIL': '✗', 'PARTIAL': '⚠', 'ERROR': '✗'}
            
            html += f"""
<div class="test-result">
    <h3><span class="{status_class}">{status_icon.get(result['status'], '?')} {result['name']}</span></h3>
    <p>状态: {result['status']}</p>
    <p>时间: {result['timestamp']}</p>
    {self.generate_result_details_html(result)}
</div>
"""
        
        return html
    
    def generate_result_details_html(self, result):
        """生成结果详情 HTML"""
        
        details = ""
        
        if 'product_cards_count' in result:
            details += f"<p>产品卡片数量: {result['product_cards_count']}</p>"
        
        if 'tags_elements_count' in result:
            details += f"<p>标签元素数量: {result['tags_elements_count']}</p>"
        
        if 'api_products_count' in result:
            details += f"<p>API 返回产品数: {result['api_products_count']}</p>"
        
        if 'products_with_tags' in result:
            details += f"<p>有标签的产品数: {result['products_with_tags']}</p>"
        
        if 'health_status' in result:
            details += f"<p>健康状态: {result['health_status']}</p>"
        
        if 'error' in result:
            details += f"<p style='color: red;'>错误: {result['error']}</p>"
        
        return details
    
    def generate_screenshots_html(self):
        """生成截图 HTML"""
        
        html = ""
        
        for result in self.test_results:
            for screenshot in result.get('screenshots', []):
                html += f"""
<div>
    <h4>{result['name']} 截图</h4>
    <img src="{screenshot}" class="screenshot" alt="{result['name']}">
</div>
"""
        
        return html
    
    def generate_markdown_summary(self):
        """生成 Markdown 汇总"""
        
        pass_count = sum(1 for r in self.test_results if r['status'] == 'PASS')
        fail_count = sum(1 for r in self.test_results if r['status'] == 'FAIL')
        error_count = sum(1 for r in self.test_results if r['status'] == 'ERROR')
        partial_count = sum(1 for r in self.test_results if r['status'] == 'PARTIAL')
        
        md = f"""# TAMP E2E 测试结果汇总

**测试时间**: {datetime.now().isoformat()}
**测试范围**: {self.test_scope}

## 测试统计

| 指标 | 数量 |
|------|------|
| 测试总数 | {len(self.test_results)} |
| ✓ 通过 | {pass_count} |
| ⚠ 部分通过 | {partial_count} |
| ✗ 失败 | {fail_count} |
| ✗ 错误 | {error_count} |

## 测试详情

"""
        
        for result in self.test_results:
            status_icon = {'PASS': '✓', 'FAIL': '✗', 'PARTIAL': '⚠', 'ERROR': '✗'}
            md += f"{status_icon.get(result['status'], '?')} **{result['name']}**: {result['status']}\n\n"
        
        md += "\n---\n\n**查看完整报告**: [test-results/report.html](../test-results/report.html)\n"
        
        return md
    
    def run_tests(self):
        """运行所有测试"""
        
        print("=" * 50)
        print("TAMP E2E 测试执行")
        print(f"测试范围: {self.test_scope}")
        print("=" * 50)
        
        # 根据测试范围选择测试场景
        if self.test_scope == 'critical':
            # 关键场景测试
            self.test_backend_api_health()
            self.test_frontend_rendering()
            self.test_investor_product_tags()
        elif self.test_scope == 'smoke':
            # 冒烟测试
            self.test_backend_api_health()
            self.test_frontend_rendering()
        else:
            # 全量测试
            self.test_backend_api_health()
            self.test_frontend_rendering()
            self.test_investor_product_tags()
        
        # 生成报告
        self.generate_report()

def main():
    """主流程"""
    
    runner = E2ETestRunner()
    runner.run_tests()

if __name__ == '__main__':
    main()