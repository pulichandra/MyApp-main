package com.example.myjwt.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.myjwt.models.Role;
import com.example.myjwt.models.enm.ERole;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Role findByName(ERole name);
}
