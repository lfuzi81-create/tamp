package com.tamp.user.repository;

import com.tamp.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByUserId(Long userId);
}
