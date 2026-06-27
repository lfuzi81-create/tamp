-- ============================================================
-- TAMP (TAMP) 种子数据脚本
-- 在 schema.sql 执行后执行此脚本
-- ============================================================

USE `tamp`;

-- ============================================================
-- 产品分类初始数据
-- ============================================================
INSERT INTO `biz_product_category` (`name`, `sort_order`, `status`) VALUES
('固定收益类', 1, 0),
('权益类', 2, 0),
('海外配置', 3, 0),
('保险保障', 4, 0),
('现金管理', 5, 0);

-- ============================================================
-- 内容分类初始数据
-- ============================================================
INSERT INTO `biz_content_category` (`name`, `sort_order`, `status`) VALUES
('市场资讯', 1, 0),
('行业研究', 2, 0),
('政策解读', 3, 0),
('投资策略', 4, 0),
('投资者教育', 5, 0);

-- ============================================================
-- 知识库分类初始数据
-- ============================================================
INSERT INTO `biz_knowledge_category` (`name`, `sort_order`, `status`) VALUES
('产品知识', 1, 0),
('合规指引', 2, 0),
('话术模板', 3, 0),
('常见问题', 4, 0);

-- ============================================================
-- 测试用户数据
-- 密码统一为: Abc@123456 （BCrypt 加密值）
--
-- 各端登录方式:
--   总部管理端 (hq-admin):   手机号+密码
--   店铺管理端 (shop-admin): 手机号+密码
--   投资人端   (investor-app): 验证码+微信
--
-- 测试账号:
--   超级管理员:  手机 13800000001 / 密码 Abc@123456
--   平台管理员:  手机 13800000010 / 密码 Abc@123456
--   tamp管理员: 手机 13800000011 / 密码 Abc@123456
--   店铺管理员:  手机 13800000002 / 密码 Abc@123456
--   投资人:     手机 13800000003 / 验证码登录
-- ============================================================
INSERT INTO `sys_user` (`password`, `phone`, `real_name`, `role`, `office_id`, `shop_id`, `status`, `password_changed`, `is_deleted`, `created_at`, `updated_at`) VALUES
('$2a$10$fvSRIvOnz2IMQpSKTspoLOCvX3ZDerB25fPmFXvhT8YXqqY6Sqi52', '13800000001', '超级管理员', 'SUPER_ADMIN', NULL, NULL, 0, 1, 0, NOW(), NOW()),
('$2a$10$fvSRIvOnz2IMQpSKTspoLOCvX3ZDerB25fPmFXvhT8YXqqY6Sqi52', '13800000010', '平台管理员', 'PLATFORM_ADMIN', NULL, NULL, 0, 1, 0, NOW(), NOW()),
('$2a$10$fvSRIvOnz2IMQpSKTspoLOCvX3ZDerB25fPmFXvhT8YXqqY6Sqi52', '13800000011', 'tamp管理员', 'TAMP_ADMIN', 1, NULL, 0, 1, 0, NOW(), NOW()),
('$2a$10$fvSRIvOnz2IMQpSKTspoLOCvX3ZDerB25fPmFXvhT8YXqqY6Sqi52', '13800000002', '张店长', 'SHOP_ADMIN', 1, 1, 0, 1, 0, NOW(), NOW()),
('$2a$10$fvSRIvOnz2IMQpSKTspoLOCvX3ZDerB25fPmFXvhT8YXqqY6Sqi52', '13800000003', '李投资人', 'INVESTOR', 1, NULL, 0, 1, 0, NOW(), NOW());

-- ============================================================
-- 测试家办数据
-- ============================================================
INSERT INTO `biz_family_office` (`name`, `contact_person`, `contact_phone`, `member_count`, `status`) VALUES
('盛世家办', '王总', '13900001001', 12, 0),
('瑞丰家办', '赵总', '13900001002', 8, 0),
('鸿运家办', '刘总', '13900001003', 15, 0);

-- 给测试用户分配家办
UPDATE `sys_user` SET `office_id` = 1 WHERE `phone` = '13800000002';
UPDATE `sys_user` SET `office_id` = 1 WHERE `phone` = '13800000003';

-- ============================================================
-- 测试店铺数据
-- ============================================================
INSERT INTO `biz_shop` (`name`, `office_id`, `manager_name`, `manager_phone`, `status`) VALUES
('盛世家办·北京朝阳店', 1, '张店长', '13800000002', 0),
('盛世家办·上海浦东店', 1, '陈店长', '13800000004', 0),
('盛世家办·深圳南山店', 1, '林店长', '13800000005', 0);

-- 给测试店铺管理员分配店铺
UPDATE `sys_user` SET `shop_id` = 1 WHERE `phone` = '13800000002';

-- ============================================================
-- 测试产品数据
-- ============================================================
INSERT INTO `biz_product` (`name`, `category_id`, `type`, `description`, `aum_min`, `aum_max`, `expected_return`, `risk_level`, `duration`, `status`) VALUES
('稳健增利一年定开', 1, 'FIXED', '中低风险固定收益类产品，适合追求稳定收益的投资者。', 100.00, 1000.00, '4.2%-4.8%', 2, '12个月', 0),
('鑫享增强债券基金', 1, 'FIXED', '通过多策略增强收益的债券型基金，波动较低。', 50.00, 500.00, '5.0%-6.5%', 2, '开放式', 0),
('成长优选混合基金', 2, 'EQUITY', '精选优质成长股，分享中国经济成长红利。', 100.00, 5000.00, '8%-15%', 4, '开放式', 0),
('全球配置美元基金', 3, 'OVERSEA', '全球化资产配置，分散单一市场风险。', 500.00, 10000.00, '6%-10%', 3, '36个月', 0),
('终身寿险·尊享版', 4, 'OTHER', '终身保障与财富传承相结合的高端保险计划。', 200.00, null, null, 1, '终身', 0),
('日添利货币基金', 5, 'FIXED', '流动性极佳的低风险理财工具，随存随取。  ', 1.00, 50000.00, '2.0%-2.5%', 1, 'T+0', 0);

-- ============================================================
-- 测试内容数据
-- ============================================================
INSERT INTO `biz_content` (`title`, `category_id`, `type`, `summary`, `cover_image`, `view_count`, `status`, `published_at`) VALUES
('2024年第四季度宏观经济展望', 1, 'ARTICLE', '深入分析当前经济形势，展望未来一个季度的宏观走势。', '', 156, 0, NOW()),
('私募股权投资策略报告', 2, 'ARTICLE', '全面解读PE/VC市场的最新趋势和投资机会。', '', 89, 0, NOW()),
('新《公司法》修订要点解读', 3, 'ARTICLE', '重点梳理本次修订对企业和投资者的关键影响。', '', 234, 0, NOW()),
('家庭资产配置黄金法则', 4, 'ARTICLE', '科学的资产配置方法，帮助您实现财富保值增值。', '', 312, 0, NOW()),
('如何识别金融诈骗？', 5, 'ARTICLE', '常见金融诈骗手段及防范指南，保护您的财产安全。', '', 567, 0, NOW());

-- ============================================================
-- 测试知识库文章数据
-- ============================================================
INSERT INTO `biz_knowledge_article` (`title`, `category_id`, `summary`, `view_count`, `status`, `published_at`) VALUES
('固定收益类产品介绍及适用场景', 1, '详细介绍各类固收产品的特点和适合人群。', 45, 0, NOW()),
('合规销售十项禁令', 2, '销售人员必须遵守的合规红线。', 128, 0, NOW()),
('新客户首次沟通话术模板', 3, '标准化的首次接触话术，帮助建立信任。', 89, 0, NOW()),
('客户常见问题FAQ汇总', 4, '收集整理客户最常问的问题及标准回答。', 234, 0, NOW());

-- ============================================================
-- 测试客户数据
-- ============================================================
INSERT INTO `biz_client` (`name`, `phone`, `gender`, `company`, `position`, `aum_total`, `source`, `shop_id`, `office_id`, `status`) VALUES
('王先生', '13901001001', 1, '科技有限公司', 'CEO', 5000.00, '转介绍', 1, 1, 0),
('李女士', '13901001002', 2, '投资咨询公司', '财务总监', 3000.00, '活动获客', 1, 1, 0),
('赵总', '13901001003', 1, '房地产集团', '董事长', 8000.00, '老客户推荐', 1, 1, 0),
('孙小姐', '13901001004', 2, '律师事务所', '合伙人', 1500.00, '线上渠道', 1, 1, 0);

-- ============================================================
-- 权限组初始数据（9个权限组，为平台管理员默认开启）
-- ============================================================
INSERT INTO `biz_permission` (`user_id`, `resource_code`, `allowed`, `group_code`, `is_group_switch`) VALUES
(2, 'dashboard', 1, 'dashboard', 1),
(2, 'staff_management', 1, 'staff_management', 1),
(2, 'tamp_management', 1, 'tamp_management', 1),
(2, 'product_library', 1, 'product_library', 1),
(2, 'content_library', 1, 'content_library', 1),
(2, 'knowledge_library', 1, 'knowledge_library', 1),
(2, 'client_management', 1, 'client_management', 1),
(2, 'permission_management', 1, 'permission_management', 1),
(2, 'system_config', 1, 'system_config', 1);

-- ============================================================
-- 角色权限模板初始数据（权限管理页使用）
-- ============================================================
INSERT INTO `biz_role_permission_template` (`role`, `resource_code`, `allowed`) VALUES
-- PLATFORM_ADMIN: 全部开放，仅 permission_management 关闭
('PLATFORM_ADMIN', 'dashboard', 1),
('PLATFORM_ADMIN', 'staff_management', 1),
('PLATFORM_ADMIN', 'tamp_management', 1),
('PLATFORM_ADMIN', 'product_library', 1),
('PLATFORM_ADMIN', 'content_library', 1),
('PLATFORM_ADMIN', 'knowledge_library', 1),
('PLATFORM_ADMIN', 'client_management', 1),
('PLATFORM_ADMIN', 'permission_management', 0),
-- TAMP_ADMIN: 基础权限 + 客户管理，产品/内容/知识/权限关闭
('TAMP_ADMIN', 'dashboard', 1),
('TAMP_ADMIN', 'staff_management', 1),
('TAMP_ADMIN', 'tamp_management', 1),
('TAMP_ADMIN', 'product_library', 0),
('TAMP_ADMIN', 'content_library', 0),
('TAMP_ADMIN', 'knowledge_library', 0),
('TAMP_ADMIN', 'client_management', 1),
('TAMP_ADMIN', 'permission_management', 0);
