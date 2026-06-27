package com.tamp.user.repository;

import com.tamp.user.entity.RolePermissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionTemplateRepository extends JpaRepository<RolePermissionTemplate, Long> {

    List<RolePermissionTemplate> findByRole(String role);

    List<RolePermissionTemplate> findByRoleIn(List<String> roles);
}
