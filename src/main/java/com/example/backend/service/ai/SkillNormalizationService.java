package com.example.backend.service.ai;

import com.example.backend.model.Skill;
import com.example.backend.repository.SkillRepository;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillNormalizationService {

    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "skills", key = "#rawSkill.toLowerCase()")
    public String normalize(String rawSkill) {
        if (StringUtils.isNullOrEmpty(rawSkill)) {
            return rawSkill;
        }

        String normalized = StringUtils.normalizeSkill(rawSkill);

        // Exact match on canonical name
        Optional<Skill> exactMatch = skillRepository.findByCanonicalNameIgnoreCase(normalized);
        if (exactMatch.isPresent()) {
            return exactMatch.get().getCanonicalName();
        }

        // Search by alias
        List<Skill> matches = skillRepository.findByCanonicalNameOrAlias(normalized);
        if (!matches.isEmpty()) {
            return matches.get(0).getCanonicalName();
        }

        // Fallback: return original
        return rawSkill.trim();
    }

    public List<String> normalizeAll(List<String> rawSkills) {
        if (rawSkills == null || rawSkills.isEmpty()) {
            return List.of();
        }

        return rawSkills.stream()
                .map(this::normalize)
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isKnownSkill(String skill) {
        String normalized = StringUtils.normalizeSkill(skill);
        return skillRepository.existsByCanonicalNameIgnoreCase(normalized);
    }
}