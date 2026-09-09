package com.example.backend.repository;

import com.example.backend.model.ImprovementRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImprovementRecommendationRepository extends JpaRepository<ImprovementRecommendation, String> {

    List<ImprovementRecommendation> findByMatchResultIdOrderByPriorityScoreDesc(String matchResultId);

    void deleteByMatchResultId(String matchResultId);
}