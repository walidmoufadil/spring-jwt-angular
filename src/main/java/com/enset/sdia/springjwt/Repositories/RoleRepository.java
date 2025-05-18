package com.enset.sdia.springjwt.Repositories;

import com.enset.sdia.springjwt.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);

    @Query("SELECT r FROM Role r WHERE r.roleName LIKE :kw")
    Role searchRoleByName(@Param("kw") String keyword);
}

