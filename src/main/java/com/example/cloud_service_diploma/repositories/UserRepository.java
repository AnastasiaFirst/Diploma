package com.example.cloud_service_diploma.repositories;


import com.example.cloud_service_diploma.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.role WHERE u.login = :login")
    Optional<UserEntity> findUserByLogin(@Param("login") String login);

    Optional<UserEntity> findById(Long id);
}