#!/usr/bin/env python3
"""
GitHub Actions Mock 登录脚本
为 E2E 测试准备登录 Token 和测试数据
"""

import redis
import requests
import os
import json
from datetime import datetime

def mock_login_for_testing():
    """为所有角色 Mock 登录"""
    
    print("\n=== Mock 登录准备 ===")
    
    # GitHub Actions 环境配置
    redis_host = 'localhost'
    redis_port = 6379
    backend_url = 'http://localhost:8080'
    
    # 测试用户配置
    test_users = {
        'HQ_ADMIN': '13800000001',
        'SHOP_ADMIN': '13800000002',
        'INVESTOR': '13800000003'
    }
    
    # 连接 Redis
    r = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
    
    tokens = {}
    
    for role, phone in test_users.items():
        print(f"\n[{role}] Mock 登录...")
        
        # 1. 在 Redis 中写入测试验证码
        test_code = '123456'
        sms_key = f'sms:code:{phone}'
        r.setex(sms_key, 300, test_code)
        
        # 2. 发送登录请求
        try:
            response = requests.post(
                f'{backend_url}/api/auth/sms-login',
                json={
                    'phone': phone,
                    'smsCode': test_code,
                    'shopId': 1 if role in ['SHOP_ADMIN', 'INVESTOR'] else None
                },
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('code') == 0:
                    token = data['data']['accessToken']
                    user_info = data['data']['userInfo']
                    
                    tokens[role] = {
                        'token': token,
                        'user_info': user_info
                    }
                    
                    print(f"✓ {role} 登录成功")
                    print(f"   用户ID: {user_info['userId']}")
                    print(f"   手机号: {user_info['phone']}")
                else:
                    print(f"✗ {role} 登录失败: {data.get('message')}")
            else:
                print(f"✗ {role} HTTP 错误: {response.status_code}")
                
        except Exception as e:
            print(f"✗ {role} 登录异常: {str(e)}")
    
    # 保存 Token 到文件（供后续测试使用）
    tokens_file = 'test-results/tokens.json'
    os.makedirs('test-results', exist_ok=True)
    
    with open(tokens_file, 'w', encoding='utf-8') as f:
        json.dump(tokens, f, ensure_ascii=False, indent=2)
    
    print(f"\n✓ Token 已保存到: {tokens_file}")
    
    # 创建环境变量文件（供后续测试使用）
    env_file = 'test-results/test_env.txt'
    
    with open(env_file, 'w', encoding='utf-8') as f:
        for role, data in tokens.items():
            f.write(f"{role}_TOKEN={data['token']}\n")
            f.write(f"{role}_USER_ID={data['user_info']['userId']}\n")
            f.write(f"{role}_PHONE={data['user_info']['phone']}\n")
    
    print(f"✓ 测试环境配置已保存到: {env_file}")
    
    return tokens

def prepare_test_data():
    """准备测试数据"""
    
    print("\n=== 测试数据准备 ===")
    
    # 测试数据已通过 GitHub Actions workflow 插入
    print("✓ 测试数据已通过 workflow 插入数据库")
    print("   - 测试用户: HQ_ADMIN, SHOP_ADMIN, INVESTOR")
    print("   - 测试店铺: Test Shop 1, Test Shop 2")
    print("   - 测试产品: Test Product 1 (带标签), Test Product 2 (带标签)")

def main():
    """主流程"""
    
    print("=" * 50)
    print("GitHub Actions Mock 登录准备")
    print("=" * 50)
    
    # 创建测试结果目录
    os.makedirs('test-results', exist_ok=True)
    os.makedirs('test-results/screenshots', exist_ok=True)
    os.makedirs('test-results/logs', exist_ok=True)
    
    # Mock 登录
    tokens = mock_login_for_testing()
    
    # 准备测试数据
    prepare_test_data()
    
    # 检查登录结果
    success_count = len(tokens)
    print(f"\n登录成功: {success_count}/3")
    
    if success_count == 3:
        print("✓ 所有角色登录成功，测试环境准备完成")
        return 0
    else:
        print("⚠ 部分角色登录失败，请检查后端服务")
        return 1

if __name__ == '__main__':
    exit(main())