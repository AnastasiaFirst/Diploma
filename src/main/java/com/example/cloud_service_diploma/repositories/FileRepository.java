package com.example.cloud_service_diploma.repositories;

import com.example.cloud_service_diploma.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findFileByUserEntityIdAndFileName(Long userId, String fileName);

    Optional<FileEntity> findFileByUserEntityId(Long userId);

    List<FileEntity> findByUserEntityId(Long userId, Pageable pageable);
}
