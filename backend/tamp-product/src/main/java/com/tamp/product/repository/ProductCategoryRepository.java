package com.tamp.product.repository;

import com.tamp.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByStatusOrderBySortOrderAsc(Integer status);
}
