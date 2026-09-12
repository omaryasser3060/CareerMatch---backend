package com.example.backend.repository;

import com.example.backend.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, String> {

    Optional<Skill> findByCanonicalNameIgnoreCase(String canonicalName);

    boolean existsByCanonicalNameIgnoreCase(String canonicalName);

    List<Skill> findByCategory(String category);

    List<Skill> findByActiveTrue();

    @Query("SELECT s FROM Skill s WHERE " +
            "LOWER(s.canonicalName) = LOWER(:name) OR " +
            "LOWER(s.aliasesJson) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Skill> findByCanonicalNameOrAlias(@Param("name") String name);

    @Query("SELECT s FROM Skill s WHERE " +
            "LOWER(s.canonicalName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Skill> searchByCanonicalName(@Param("query") String query);
}