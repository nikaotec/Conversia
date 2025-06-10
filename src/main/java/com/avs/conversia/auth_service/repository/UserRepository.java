package com.avs.conversia.auth_service.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avs.conversia.auth_service.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("SELECT t.id FROM Tenant t WHERE t.user.email = :email")
    Set<Long> findTenantIdsByEmail(@Param("email") String email);

}