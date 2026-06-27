package com.tamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 【TAMP 应用启动入口】—— 整个后端就是从这里启动的
 *
 * 作用：这是整个系统的"总开关"，运行这个类的 main 方法，整个后端就启动了
 *
 * 为什么只有一个启动类？
 *   虽然项目有 12 个模块（common/auth/product/content/...），
 *   但它们最终都被 tamp-bootstrap 这个模块聚合在一起，作为一个整体运行。
 *   就像一辆汽车有很多零件（发动机、轮胎、方向盘...），但只有一个启动按钮。
 *
 * 两个关键注解：
 *   @SpringBootApplication(scanBasePackages = "com.tamp")
 *     → 告诉 Spring："去 com.tamp 包下面找所有的组件（Controller/Service/Repository...）"
 *     → 这样所有子模块的组件都会被自动发现和加载
 *
 *   @EnableJpaAuditing
 *     → 开启 JPA 审计功能，配合 BaseEntity 的 @CreatedDate/@CreatedBy 等注解
 *     → 这样创建/修改数据时，时间和操作人会自动填充
 */
@SpringBootApplication(scanBasePackages = "com.tamp")  // 扫描 com.tamp 下所有包（包括子模块）
@EnableJpaAuditing                                    // 开启自动审计（自动填充 createdAt/createdBy 等）
public class TampApplication {

    /**
     * main 方法 —— 程序入口
     * 运行这行代码，Spring Boot 就会：
     *   1. 加载配置文件 application.yml
     *   2. 扫描所有 com.tamp 包下的组件
     *   3. 连接数据库和 Redis
     *   4. 启动 Web 服务器（默认端口 8080）
     *   5. 注册所有 API 接口
     */
    public static void main(String[] args) {
        SpringApplication.run(TampApplication.class, args);
    }
}
