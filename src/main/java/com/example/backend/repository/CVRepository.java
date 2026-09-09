package com.example.backend.repository;

import com.example.backend.model.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CVRepository extends JpaRepository<CV, String> {

    List<CV> findByUserIdOrderByUploadedAtDesc(String userId);

    Optional<CV> findByIdAndUserId(String id, String userId);

    List<CV> findByUserIdAndParsed(String userId, boolean parsed);

    boolean existsByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);
}