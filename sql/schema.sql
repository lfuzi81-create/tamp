-- ============================================================
-- TAMP (TAMP) 数据库建表脚本
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS `tamp`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `tamp`;

-- ============================================================
-- 1. 系统用户表
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `password`        VARCHAR(128) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone`           VARCHAR(20)  NOT NULL COMMENT '手机号（即登录账号）',
    `real_name`       VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
    `role`            VARCHAR(20)  NOT NULL COMMENT '角色：SUPER_ADMIN/PLATFORM_ADMIN/OPERATOR/TAMP_ADMIN/SHOP_ADMIN/INVESTOR',
    `office_id`       BIGINT       DEFAULT NULL COMMENT '所属家办ID',
    `office_ids`      VARCHAR(512) DEFAULT NULL COMMENT '可见家办范围（运营人员用，逗号分隔）',
    `shop_id`         BIGINT       DEFAULT NULL COMMENT '所属店铺ID（仅SHOP_ADMIN）',
    `avatar`          VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=正常，1=禁用',
    `password_changed` TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已改初始密码：0=否，1=是',
    `last_login_time` DATETIME(3)  DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(64)  DEFAULT NULL COMMENT '最后登录IP',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `created_by`      VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `updated_by`      VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_office_id` (`office_id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ============================================================
-- 2. 产品分类表
-- ============================================================
DROP TABLE IF EXISTS `biz_product_category`;
CREATE TABLE `biz_product_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前）',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=启用，1=停用',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`  VARCHAR(64)  DEFAULT NULL,
    `updated_by`  VARCHAR(64)  DEFAULT NULL,
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品分类表';

-- ============================================================
-- 3. 产品表
-- ============================================================
DROP TABLE IF EXISTS `biz_product`;
CREATE TABLE `biz_product` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`            VARCHAR(128) NOT NULL COMMENT '产品名称',
    `category_id`     BIGINT       DEFAULT NULL COMMENT '所属分类ID',
    `type`            VARCHAR(32)  NOT NULL DEFAULT 'FIXED' COMMENT '类型：FIXED/EQUITY/OVERSEA/OTHER',
    `description`     TEXT         DEFAULT NULL COMMENT '产品描述',
    `aum_min`         DECIMAL(18,2) DEFAULT NULL COMMENT '最低投资金额（万）',
    `aum_max`         DECIMAL(18,2) DEFAULT NULL COMMENT '最高投资金额（万）',
    `expected_return` VARCHAR(32)  DEFAULT NULL COMMENT '预期收益率（如"6%-8%"）',
    `risk_level`      TINYINT      DEFAULT NULL COMMENT '风险等级：1-5（低到高）',
    `duration`        VARCHAR(64)  DEFAULT NULL COMMENT '投资期限（如"12个月"）',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=上架，1=下架',
    `cover_image`     VARCHAR(512) DEFAULT NULL COMMENT '封面图URL',
    `detail_url`      VARCHAR(512) DEFAULT NULL COMMENT '详情链接',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- ============================================================
-- 4. 内容分类表
-- ============================================================
DROP TABLE IF EXISTS `biz_content_category`;
CREATE TABLE `biz_content_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=启用，1=停用',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`  VARCHAR(64)  DEFAULT NULL,
    `updated_by`  VARCHAR(64)  DEFAULT NULL,
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容分类表';

-- ============================================================
-- 5. 内容表
-- ============================================================
DROP TABLE IF EXISTS `biz_content`;
CREATE TABLE `biz_content` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`           VARCHAR(256) NOT NULL COMMENT '内容标题',
    `category_id`     BIGINT       DEFAULT NULL COMMENT '所属分类ID',
    `type`            VARCHAR(32)  DEFAULT 'ARTICLE' COMMENT '类型：ARTICLE/VIDEO/LINK',
    `summary`         VARCHAR(512) DEFAULT NULL COMMENT '摘要',
    `cover_image`     VARCHAR(512) DEFAULT NULL COMMENT '封面图URL',
    `content_url`     VARCHAR(512) DEFAULT NULL COMMENT '内容链接/详情页地址',
    `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `source`          VARCHAR(128) DEFAULT NULL COMMENT '来源',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=发布，1=草稿',
    `published_at`    DATETIME(3)  DEFAULT NULL COMMENT '发布时间',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容表';

-- ============================================================
-- 6. 家办表
-- ============================================================
DROP TABLE IF EXISTS `biz_family_office`;
CREATE TABLE `biz_family_office` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`            VARCHAR(128) NOT NULL COMMENT '家办名称',
    `contact_person`  VARCHAR(64)  DEFAULT NULL COMMENT '联系人',
    `contact_phone`   VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `intro`           TEXT         DEFAULT NULL COMMENT '家办介绍',
    `member_count`    INT          NOT NULL DEFAULT 0 COMMENT '成员数量',
    `logo_url`        VARCHAR(512) DEFAULT NULL COMMENT 'Logo URL',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=启用，1=停用',
    `total_aum`       DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '总AUM（万元）',
    `client_count`    INT          NOT NULL DEFAULT 0 COMMENT '客户总数',
    `shop_count`      INT          NOT NULL DEFAULT 0 COMMENT '下属店铺数',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家办表';

-- ============================================================
-- 7. 店铺表
-- ============================================================
DROP TABLE IF EXISTS `biz_shop`;
CREATE TABLE `biz_shop` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`            VARCHAR(128) NOT NULL COMMENT '店铺名称',
    `office_id`       BIGINT       NOT NULL COMMENT '所属家办ID',
    `manager_name`    VARCHAR(64)  DEFAULT NULL COMMENT '负责人姓名',
    `manager_phone`   VARCHAR(20)  DEFAULT NULL COMMENT '负责人电话',
    `address`         VARCHAR(256) DEFAULT NULL COMMENT '地址',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=启用，1=停用',
    `client_count`    INT          NOT NULL DEFAULT 0 COMMENT '客户数',
    `total_aum`       DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '总AUM（万元）',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_office_id` (`office_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- ============================================================
-- 8. 客户表
-- ============================================================
DROP TABLE IF EXISTS `biz_client`;
CREATE TABLE `biz_client` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`            VARCHAR(64)  NOT NULL COMMENT '客户姓名',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `gender`          TINYINT      DEFAULT NULL COMMENT '性别：0=未知，1=男，2=女',
    `email`           VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `company`         VARCHAR(128) DEFAULT NULL COMMENT '公司',
    `position`        VARCHAR(64)  DEFAULT NULL COMMENT '职位',
    `aum_total`       DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '资产总额（万元）',
    `source`          VARCHAR(64)  DEFAULT NULL COMMENT '来源渠道',
    `remark`          TEXT         DEFAULT NULL COMMENT '备注',
    `shop_id`         BIGINT       DEFAULT NULL COMMENT '归属店铺ID',
    `office_id`       BIGINT       DEFAULT NULL COMMENT '归属家办ID',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=活跃，1=流失',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_office_id` (`office_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- ============================================================
-- 9. 客户时间线表
-- ============================================================
DROP TABLE IF EXISTS `biz_client_timeline`;
CREATE TABLE `biz_client_timeline` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_id`   BIGINT       NOT NULL COMMENT '客户ID',
    `event_type`  VARCHAR(32)  NOT NULL COMMENT '事件类型：CALL/MEETING/FOLLOWUP/DEAL/NOTE/OTHER',
    `title`       VARCHAR(256) NOT NULL COMMENT '事件标题',
    `content`     TEXT         DEFAULT NULL COMMENT '事件详情',
    `target_id`   BIGINT       DEFAULT NULL COMMENT '行为目标ID（产品/内容/知识文章）',
    `event_time`  DATETIME(3)  DEFAULT NULL COMMENT '事件发生时间',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `created_by`  VARCHAR(64)  DEFAULT NULL COMMENT '操作人',
    PRIMARY KEY (`id`),
    KEY `idx_client_id` (`client_id`),
    KEY `idx_event_time` (`event_time`),
    KEY `idx_event_target` (`event_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户时间线表';

-- ============================================================
-- 10. 客户标签表
-- ============================================================
DROP TABLE IF EXISTS `biz_client_tag`;
CREATE TABLE `biz_client_tag` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_id`   BIGINT       NOT NULL COMMENT '客户ID',
    `tag_name`    VARCHAR(64)  NOT NULL COMMENT '标签名称',
    `tag_color`   VARCHAR(16)  DEFAULT '#1890ff' COMMENT '标签颜色',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户标签表';

-- ============================================================
-- 11. 客户资产表
-- ============================================================
DROP TABLE IF EXISTS `biz_client_asset`;
CREATE TABLE `biz_client_asset` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_id`       BIGINT       DEFAULT NULL COMMENT '客户ID（店铺端录入时关联）',
    `investor_id`     BIGINT       DEFAULT NULL COMMENT '投资人ID（投资人端录入时关联）',
    `name`            VARCHAR(128) NOT NULL COMMENT '资产名称',
    `type`            VARCHAR(32)  NOT NULL COMMENT '资产类型：FIXED_FUND/EQUITY_FUND/INSURANCE/REAL_ESTATE/CASH/BOND/OTHER',
    `product_id`      BIGINT       DEFAULT NULL COMMENT '关联店铺产品ID（投资人从货架选品时填写）',
    `amount`          DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '金额（万元）',
    `risk_level`      TINYINT      DEFAULT NULL COMMENT '风险等级：1-5',
    `purchase_date`   DATE         DEFAULT NULL COMMENT '购入日期',
    `maturity_date`   DATE         DEFAULT NULL COMMENT '到期日期',
    `expected_return` VARCHAR(32)  DEFAULT NULL COMMENT '预期收益率',
    `institution`     VARCHAR(128) DEFAULT NULL COMMENT '持有机构',
    `is_authorized`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否授权家办查看：0=否，1=是',
    `auth_scope`      TEXT         DEFAULT NULL COMMENT '授权范围(JSON数组,存储officeId列表)',
    `remark`          TEXT         DEFAULT NULL COMMENT '备注',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_client_id` (`client_id`),
    KEY `idx_investor_id` (`investor_id`),
    KEY `idx_type` (`type`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户资产表';

-- ============================================================
-- 12. 知识库分类表
-- ============================================================
DROP TABLE IF EXISTS `biz_knowledge_category`;
CREATE TABLE `biz_knowledge_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=启用，1=停用',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`  VARCHAR(64)  DEFAULT NULL,
    `updated_by`  VARCHAR(64)  DEFAULT NULL,
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库分类表';

-- ============================================================
-- 13. 知识库文章表
-- ============================================================
DROP TABLE IF EXISTS `biz_knowledge_article`;
CREATE TABLE `biz_knowledge_article` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`           VARCHAR(256) NOT NULL COMMENT '文章标题',
    `category_id`     BIGINT       DEFAULT NULL COMMENT '所属分类ID',
    `summary`         VARCHAR(512) DEFAULT NULL COMMENT '摘要',
    `cover_image`     VARCHAR(512) DEFAULT NULL COMMENT '封面图',
    `content_text`    LONGTEXT     DEFAULT NULL COMMENT '正文内容（富文本/Markdown）',
    `attachment_url`  VARCHAR(512) DEFAULT NULL COMMENT '附件URL',
    `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=发布，1=草稿',
    `is_updated`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否有更新：0=否，1=是（用于红点提示）',
    `published_at`    DATETIME(3)  DEFAULT NULL COMMENT '发布时间',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文章表';

-- ============================================================
-- 14. 货架项表（店铺端专用）
-- ============================================================
DROP TABLE IF EXISTS `biz_shelf_item`;
CREATE TABLE `biz_shelf_item` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `shop_id`         BIGINT       NOT NULL COMMENT '所属店铺ID',
    `item_type`       VARCHAR(16)  NOT NULL COMMENT '类型：PRODUCT/CONTENT',
    `item_id`         BIGINT       NOT NULL COMMENT '产品ID或内容ID',
    `sort_order`      INT          NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前）',
    `is_top`          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否，1=是',
    `tags`            VARCHAR(256) DEFAULT NULL COMMENT '自定义标签（逗号分隔）',
    `added_at`        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '添加时间',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_type_item` (`shop_id`, `item_type`, `item_id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_sort_order` (`shop_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='货架项表';

-- ============================================================
-- 15. 人员表
-- ============================================================
DROP TABLE IF EXISTS `biz_staff`;
CREATE TABLE `biz_staff` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`            VARCHAR(64)  NOT NULL COMMENT '姓名',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`           VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `role_type`       VARCHAR(32)  NOT NULL COMMENT '角色类型：MANAGER/ADVISOR/ASSISTANT/OPERATOR',
    `office_id`       BIGINT       DEFAULT NULL COMMENT '所属家办ID',
    `shop_id`         BIGINT       DEFAULT NULL COMMENT '所属店铺ID',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=在职，1=离职',
    `join_date`       DATE         DEFAULT NULL COMMENT '入职日期',
    `remark`          TEXT         DEFAULT NULL COMMENT '备注',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_office_id` (`office_id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_role_type` (`role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员表';

-- ============================================================
-- 16. 权限表
-- ============================================================
DROP TABLE IF EXISTS `biz_permission`;
CREATE TABLE `biz_permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `resource_code`   VARCHAR(64)  NOT NULL COMMENT '资源编码（如 product:edit, client:view）',
    `allowed`         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否允许：0=禁止，1=允许',
    `group_code`      VARCHAR(64)  DEFAULT NULL COMMENT '权限组编码',
    `is_group_switch` TINYINT      DEFAULT 0 COMMENT '是否为组总开关 0否 1是',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_code`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================================
-- 18. 店铺装修配置表
-- ============================================================
DROP TABLE IF EXISTS `biz_decor_config`;
CREATE TABLE `biz_decor_config` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `office_id`        BIGINT       NOT NULL COMMENT '所属家办ID',
    `shop_id`          BIGINT       NOT NULL COMMENT '所属店铺ID',
    `display_name`     VARCHAR(128) DEFAULT NULL COMMENT '品牌显示名称，如"华港家族办公室"',
    `description`      TEXT         DEFAULT NULL COMMENT '品牌介绍描述',
    `modules`          TEXT         DEFAULT NULL COMMENT 'JSON: 模块显隐和排序配置 {brand_header:{visible:true,sort:1},...}',
    `navigation`       TEXT         DEFAULT NULL COMMENT 'JSON: 底部导航配置 {items:[...],sort_order:[...]}',
    `primary_color`    VARCHAR(16)  DEFAULT NULL COMMENT '主题色值，如 #C4954A',
    `shelf_selections` TEXT         DEFAULT NULL COMMENT 'JSON: 货架选品 {featured_products:[...],recommended_contents:[...]}',
    `created_at`       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`       VARCHAR(64)  DEFAULT NULL,
    `updated_by`       VARCHAR(64)  DEFAULT NULL,
    `is_deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_office_id` (`office_id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺装修配置表';

-- ============================================================
-- 17. 短信验证码表
-- ============================================================
DROP TABLE IF EXISTS `sms_verification_code`;
CREATE TABLE `sms_verification_code` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号',
    `code`        VARCHAR(8)   NOT NULL COMMENT '验证码',
    `type`        VARCHAR(16)  NOT NULL COMMENT '用途：LOGIN/RESET_PASSWORD',
    `expire_at`   DATETIME(3)  NOT NULL COMMENT '过期时间',
    `used`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已使用：0=否，1=是',
    `ip_address`  VARCHAR(64)  DEFAULT NULL COMMENT '请求IP',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码表';

-- ============================================================
-- 18. 家办选品表
-- ============================================================
DROP TABLE IF EXISTS `biz_office_selection`;
CREATE TABLE `biz_office_selection` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `office_id`       BIGINT       NOT NULL COMMENT '家办ID',
    `item_type`       VARCHAR(20)  NOT NULL COMMENT '项目类型：PRODUCT/CONTENT',
    `item_id`         BIGINT       NOT NULL COMMENT '项目ID',
    `sort_order`      INT          NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前）',
    `is_recommended`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否推荐：0=否，1=是',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_office_type_item` (`office_id`, `item_type`, `item_id`),
    KEY `idx_office_id` (`office_id`),
    KEY `idx_item_type` (`item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家办选品表';

-- ============================================================
-- 19. MGM分享链接表
-- ============================================================
DROP TABLE IF EXISTS `biz_share_link`;
CREATE TABLE `biz_share_link` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `short_code`        VARCHAR(32)  NOT NULL COMMENT '短码',
    `sharer_user_id`    BIGINT       NOT NULL COMMENT '分享者用户ID',
    `sharer_office_id`  BIGINT       DEFAULT NULL COMMENT '分享者家办ID',
    `target_type`       VARCHAR(20)  DEFAULT NULL COMMENT '目标类型：product/content/page',
    `target_id`         BIGINT       DEFAULT NULL COMMENT '目标ID',
    `target_name`       VARCHAR(200) DEFAULT NULL COMMENT '目标名称',
    `click_count`       INT          NOT NULL DEFAULT 0 COMMENT '点击次数',
    `created_at`        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`        VARCHAR(64)  DEFAULT NULL,
    `updated_by`        VARCHAR(64)  DEFAULT NULL,
    `is_deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_short_code` (`short_code`),
    KEY `idx_sharer_user_id` (`sharer_user_id`),
    KEY `idx_sharer_office_id` (`sharer_office_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MGM分享链接表';

-- ============================================================
-- 20. 分享点击记录表
-- ============================================================
DROP TABLE IF EXISTS `biz_share_click`;
CREATE TABLE `biz_share_click` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `share_id`          BIGINT       NOT NULL COMMENT '分享链接ID',
    `visitor_user_id`   BIGINT       DEFAULT NULL COMMENT '访问者用户ID',
    `visitor_phone`     VARCHAR(20)  DEFAULT NULL COMMENT '访问者手机号',
    `source_office_id`  BIGINT       DEFAULT NULL COMMENT '来源家办ID',
    `ip_address`        VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    `user_agent`        VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    `created_at`        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`        VARCHAR(64)  DEFAULT NULL,
    `updated_by`        VARCHAR(64)  DEFAULT NULL,
    `is_deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_share_id` (`share_id`),
    KEY `idx_visitor_user_id` (`visitor_user_id`),
    KEY `idx_source_office_id` (`source_office_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享点击记录表';

-- ============================================================
-- 21. MGM用户绑定关系表
-- ============================================================
DROP TABLE IF EXISTS `biz_user_referral`;
CREATE TABLE `biz_user_referral` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `inviter_id`  BIGINT       NOT NULL COMMENT '邀请人ID',
    `invitee_id`  BIGINT       NOT NULL COMMENT '被邀请人ID',
    `share_id`    BIGINT       DEFAULT NULL COMMENT '分享链接ID',
    `office_id`   BIGINT       DEFAULT NULL COMMENT '关联tamp ID',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invitee` (`invitee_id`),
    KEY `idx_inviter` (`inviter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MGM用户绑定关系';

CREATE TABLE IF NOT EXISTS `biz_investor_profile` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`           BIGINT       NOT NULL COMMENT '关联sys_user的ID',
    `nickname`          VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar_url`        VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `profile_completed` TINYINT      NOT NULL DEFAULT 0 COMMENT '资料是否完善：0=否，1=是',
    `first_login_done`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否完成首次登录引导：0=否，1=是',
    `created_at`        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `created_by`        VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `updated_by`        VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `is_deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资人个人资料';

-- ============================================================
-- 22. 角色权限模板表（权限管理页使用）
-- ============================================================
DROP TABLE IF EXISTS `biz_role_permission_template`;
CREATE TABLE `biz_role_permission_template` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role`            VARCHAR(32)  NOT NULL COMMENT '角色：PLATFORM_ADMIN/TAMP_ADMIN',
    `resource_code`   VARCHAR(64)  NOT NULL COMMENT '资源编码（如 product_library）',
    `allowed`         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否允许：0=禁止，1=允许',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `created_by`      VARCHAR(64)  DEFAULT NULL,
    `updated_by`      VARCHAR(64)  DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_resource` (`role`, `resource_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限模板表';
