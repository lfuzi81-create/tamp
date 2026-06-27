package com.tamp.product.repository;

import com.tamp.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 【产品仓库】—— 负责和数据库打交道，提供增删改查方法
 *
 * 作用：就像仓库管理员，你告诉他"我要什么"，他去数据库里找
 *       你不需要写 SQL，Spring Data JPA 会根据方法名自动生成 SQL
 *
 * 两种继承：
 *   JpaRepository<Product, Long>           → 提供基础方法：save / findById / findAll / delete
 *   JpaSpecificationExecutor<Product>      → 提供高级方法：动态条件查询（用于搜索+筛选+分页）
 *
 * 方法命名规则（Spring Data 会自动翻译成 SQL）：
 *   findBy → SELECT ... WHERE
 *   And    → AND
 *   Or     → OR
 *  Containing → LIKE '%xxx%'
 *   IgnoreCase → 不区分大小写
 *
 * 示例：findByCategoryIdAndStatusAndDeleted → WHERE category_id = ? AND status = ? AND is_deleted = ?
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * 按分类+状态查询产品列表（分页）
     *
     * 自动生成的 SQL：SELECT * FROM biz_product WHERE category_id = ? AND status = ? AND is_deleted = ? LIMIT ?, ?
     *
     * @param categoryId 分类ID（如 1=固收类）
     * @param status     状态（0=上架，1=下架）
     * @param deleted    逻辑删除（0=正常，1=已删除）
     * @param pageable   分页参数（第几页、每页几条）
     * @return 分页结果
     */
    Page<Product> findByCategoryIdAndStatusAndDeleted(Long categoryId, Integer status, Integer deleted, Pageable pageable);

    /**
     * 按 ID 查询单个产品（排除已删除的）
     *
     * 自动生成的 SQL：SELECT * FROM biz_product WHERE id = ? AND is_deleted = 0
     *
     * Optional 的意思是"可能有、也可能没有"：
     *   找到了 → Optional.of(product)
     *   没找到 → Optional.empty()
     *   用法：repository.findByIdAndDeleted(id, 0).orElseThrow(() -> new BizException(...))
     *
     * @param id      产品ID
     * @param deleted 逻辑删除标记（传 0 表示只查正常数据）
     * @return 可能为空的产品对象
     */
    Optional<Product> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按关键字搜索产品名称（不区分大小写，分页）
     *
     * 这个方法用了 @Query 自定义 JPQL（不是方法名派生），因为方法名太长了不好读
     * LOWER() 函数让搜索不区分大小写，CONCAT('%', :keyword, '%') 实现模糊匹配
     *
     * 生成的 SQL 效果：SELECT * FROM biz_product WHERE LOWER(name) LIKE '%关键字%' AND is_deleted = 0
     *
     * @param keyword  搜索关键字
     * @param deleted  逻辑删除标记
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.deleted = :deleted")
    Page<Product> findByNameContainingIgnoreCaseAndDeleted(@Param("keyword") String keyword, @Param("deleted") Integer deleted, Pageable pageable);

    /**
     * 统计某个分类下有多少个产品（用于删除分类前校验）
     *
     * 生成的 SQL：SELECT COUNT(*) FROM biz_product WHERE category_id = ? AND is_deleted = 0
     *
     * @param categoryId 分类ID
     * @param deleted    逻辑删除标记
     * @return 产品数量
     */
    Long countByCategoryIdAndDeleted(Long categoryId, Integer deleted);

    /**
     * 统计产品总数（用于仪表盘展示）
     *
     * 生成的 SQL：SELECT COUNT(*) FROM biz_product WHERE is_deleted = 0
     *
     * @param deleted 逻辑删除标记
     * @return 产品总数
     */
    Long countByDeleted(Integer deleted);
}
