package com.example.backend.service.ai;

import com.example.backend.dto.ai.CandidateProfileDto;
import com.example.backend.dto.ai.JobRequirementsDto;
import com.example.backend.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PromptBuilder {

    public String buildCandidateProfilePrompt(String cvText) {

        return """
                You are an expert CV parser and career-profile analyst.
                
                Analyze the CV carefully and extract only information that is
                explicitly supported by the CV.
                
                Your goal is to build a structured candidate profile that can
                later be compared against complete job requirements.
                
                Return ONLY valid JSON in this exact format:
                
                {
                  "skills": ["skill1", "skill2"],
                  "experience_summary": "brief evidence-based summary",
                  "years_of_experience": 2,
                  "experience_months": 24,
                  "past_titles": ["title1", "title2"],
                  "projects": [
                    {
                      "title": "Project Name",
                      "description": "Brief description",
                      "skills_evidence": ["skill1", "skill2"],
                      "start_date": "2023-01",
                      "end_date": "2023-06",
                      "url": "https://github.com/..."
                    }
                  ],
                  "education": {
                    "degree": "Bachelor of Science",
                    "level": "BACHELOR",
                    "field": "Computer Science",
                    "institution": "University Name",
                    "graduation_year": 2023,
                    "gpa": 3.5
                  },
                  "certifications": ["cert1", "cert2"],
                  "languages": ["English", "Arabic"],
                  "confidence": "high"
                }
                
                RULES:
                
                1. DO NOT invent skills, experience, projects, education,
                   certifications, languages, achievements, or technologies.
                
                2. A skill should only be extracted when the CV provides
                   reasonable evidence that the candidate knows or used it.
                
                3. Distinguish between:
                   - skill explicitly listed
                   - skill demonstrated through experience
                   - skill demonstrated through a project
                
                4. Project skills_evidence must contain only skills actually
                   supported by that specific project.
                
                5. Calculate experience_months from explicit work experience
                   when dates are available.
                
                6. Do not include protected attributes such as age, gender,
                   ethnicity, religion, marital status, disability, or similar
                   sensitive personal attributes.
                
                7. If information is unclear, do not guess.
                
                8. confidence must be exactly one of:
                   "high", "medium", "low"
                
                9. Return ONLY JSON.
                
                10. No markdown.
                
                11. No explanation outside the JSON.
                
                CV Text:
                ---
                %s
                ---
                """.formatted(cvText);
    }

    public String buildJobRequirementsPrompt(String jobTitle, String jobDescription) {

        return """
                You are an expert technical recruiter and job-description
                analyst.
                
                Analyze the COMPLETE job posting below.

                Use the job title only as context. Do NOT infer a skill merely
                because it is common for that title. Requirements must be supported
                by the actual job description.
                
                Do NOT simply extract keywords.
                
                Your job is to understand what the employer is actually looking
                for and convert the job into a structured Job Profile.
                
                You must distinguish between:
                
                - mandatory requirements
                - preferred / nice-to-have requirements
                - experience requirements
                - responsibilities
                - education
                - certifications
                - languages
                - other important requirements
                
                A technology mentioned casually in the description must NOT
                automatically become a required skill.
                
                For example:
                
                "Experience with Java and Spring Boot is required."
                
                means Java and Spring Boot are REQUIRED.
                
                But:
                
                "Experience with Docker is a plus."
                
                means Docker is PREFERRED.
                
                Also consider the actual responsibilities of the position.
                A candidate should be evaluated based on whether their profile
                is relevant to performing those responsibilities, not only
                whether their CV contains identical skill names.
                
                Return ONLY valid JSON in this exact format:
                
                {
                  "required_skills": [
                    "Java",
                    "Spring Boot"
                  ],
                  "preferred_skills": [
                    "Docker",
                    "AWS"
                  ],
                  "experience_level": "mid",
                  "experience_months": 24,
                  "responsibilities": [
                    "Develop backend services",
                    "Design REST APIs"
                  ],
                  "education_requirements": [
                    "Bachelor's degree in Computer Science or related field"
                  ],
                  "certification_requirements": [],
                  "language_requirements": [
                    "English"
                  ],
                  "other_requirements": [
                    "Experience working in Agile teams"
                  ],
                  "confidence": "high"
                }
                
                RULES:
                
                1. required_skills:
                   Include technologies, frameworks, programming languages,
                   databases, platforms, tools, methodologies, or technical
                   competencies that are explicitly mandatory or clearly
                   required for the position.
                
                2. preferred_skills:
                   Include skills described as:
                   - preferred
                   - nice to have
                   - bonus
                   - plus
                   - desirable
                   - advantageous
                
                3. Never put a preferred skill into required_skills.
                
                4. Never put a required skill into preferred_skills.
                
                5. Do not infer a skill merely because it is common for the job
                   title.
                
                6. experience_level must be exactly one of:
                   "entry",
                   "mid",
                   "senior",
                   "lead"
                
                7. experience_months:
                   Convert explicit experience requirements into months.
                   
                   Examples:
                   - 1 year = 12
                   - 2 years = 24
                   - 3 years = 36
                   - 5 years = 60
                   
                   If no experience requirement is stated, use null.
                
                8. responsibilities:
                   Extract the major responsibilities and tasks that define
                   what the candidate will actually do.
                
                9. education_requirements:
                   Include education requirements only when explicitly stated
                   or clearly required.
                
                10. certification_requirements:
                    Include certifications only when explicitly required or
                    strongly preferred by the job.
                
                11. language_requirements:
                    Include languages only when explicitly required or
                    strongly preferred.
                
                12. other_requirements:
                    Include meaningful requirements that do not belong to the
                    previous categories, such as:
                    - Agile/Scrum experience
                    - leadership
                    - communication requirements
                    - security clearance
                    - domain-specific requirements
                
                13. responsibilities are NOT skills.
                
                14. Do not invent information that is not supported by the job
                    description.
                
                15. Preserve the meaning of the original job description.
                
                16. confidence must be exactly one of:
                    "high",
                    "medium",
                    "low"
                
                17. Return ONLY JSON.
                
                18. No markdown.
                
                19. No explanation outside the JSON.
                
                Job Title:
                ---
                %s
                ---

                Job Description:
                ---
                %s
                ---
                """.formatted(jobTitle, jobDescription);
    }

    public String buildRecommendationsPrompt(
            CandidateProfileDto candidate,
            JobRequirementsDto job,
            List<String> matchedRequired,
            List<String> missingRequired,
            List<String> matchedPreferred,
            List<String> missingPreferred,
            int matchScore
    ) {

        return """
                You are an expert technical recruiter and career advisor.
                
                Analyze the candidate against the COMPLETE job profile.
                
                The numeric Match Score has already been calculated by the
                application. You MUST NOT recalculate or change it.
                
                Your job is to explain what the score means and identify the
                strongest evidence, the most important gaps, and practical
                improvements.
                
                The candidate must be evaluated using more than skill names.
                
                Consider:
                
                - required skills
                - preferred skills
                - experience
                - project evidence
                - responsibilities
                - education
                - certifications
                - languages
                - other requirements
                - semantic relevance between the candidate profile and job
                
                IMPORTANT:
                
                A skill existing in the CV does NOT automatically mean the
                candidate is a strong match.
                
                Look for evidence of using the skill through:
                
                - professional experience
                - projects
                - responsibilities
                - technologies used
                - relevant achievements
                
                Required requirements are more important than preferred
                requirements.
                
                Do NOT invent anything.
                
                Match Score:
                %d/100
                
                Candidate Skills:
                %s
                
                Candidate Experience:
                %s
                
                Candidate Experience Months:
                %s
                
                Candidate Past Titles:
                %s
                
                Candidate Projects:
                %s
                
                Candidate Education:
                %s
                
                Candidate Certifications:
                %s
                
                Candidate Languages:
                %s
                
                Job Required Skills:
                %s
                
                Job Preferred Skills:
                %s
                
                Job Experience Requirement:
                %s months
                
                Job Experience Level:
                %s
                
                Job Responsibilities:
                %s
                
                Job Education Requirements:
                %s
                
                Job Certification Requirements:
                %s
                
                Job Language Requirements:
                %s
                
                Job Other Requirements:
                %s
                
                Matched Required Skills:
                %s
                
                Missing Required Skills:
                %s
                
                Matched Preferred Skills:
                %s
                
                Missing Preferred Skills:
                %s
                
                Return ONLY valid JSON in this exact format:
                
                {
                  "overall_assessment": "The candidate is a strong match for this role because ...",
                  "match_level": "STRONG",
                  "summary": "Short evidence-based explanation of the match.",
                  "strengths": [
                    "Strong evidence of backend development experience",
                    "Relevant Spring Boot project experience"
                  ],
                  "recommendations": [
                    {
                      "gap_name": "Docker",
                      "gap_category": "DEVOPS",
                      "required_or_preferred": "REQUIRED",
                      "importance_weight": 9,
                      "job_evidence": "Docker is required for containerized deployment.",
                      "cv_evidence": "No Docker experience or project evidence is present.",
                      "related_existing_strengths": [
                        "Spring Boot",
                        "REST APIs"
                      ],
                      "recommended_action": "Build and deploy a Spring Boot application using Docker.",
                      "deliverable": "GitHub repository containing a Dockerfile and deployment instructions.",
                      "estimated_effort": "1-2 weeks",
                      "expected_score_gain": 8,
                      "priority": "HIGH",
                      "resources": [
                        {
                          "title": "Docker Documentation",
                          "url": "https://docs.docker.com/",
                          "type": "DOCUMENTATION",
                          "description": "Official Docker documentation."
                        }
                      ],
                      "confidence": "high"
                    }
                  ]
                }
                
                RULES:
                
                1. overall_assessment:
                   Give a concise professional assessment explaining the
                   candidate's overall fit.
                
                2. match_level must be exactly one of:
                   "EXCELLENT",
                   "STRONG",
                   "MODERATE",
                   "WEAK",
                   "POOR"
                
                3. The match_level must be consistent with the supplied score:
                   
                   90-100 = EXCELLENT
                   75-89  = STRONG
                   60-74  = MODERATE
                   40-59  = WEAK
                   0-39   = POOR
                
                4. summary must explain the main reasons behind the score.
                
                5. strengths must contain only evidence supported by the
                   candidate profile.
                
                6. Do NOT claim that the candidate has experience simply
                   because the job requires it.
                
                7. Do NOT treat a matching skill name as proof of professional
                   experience unless the candidate profile provides evidence.
                
                8. Required gaps normally have higher importance than preferred
                   gaps.
                
                9. required_or_preferred must be exactly:
                   "REQUIRED"
                   or
                   "PREFERRED"
                
                10. importance_weight must be an integer from 1 to 10.
                
                11. Required gaps should normally have importance 6-10.
                
                12. Preferred gaps should normally have importance 1-6.
                
                13. gap_category must be one of:
                   "LANGUAGE",
                   "FRAMEWORK",
                   "DATABASE",
                   "DEVOPS",
                   "CLOUD",
                   "TOOL",
                   "SOFT_SKILL",
                   "EDUCATION",
                   "CERTIFICATION",
                   "EXPERIENCE",
                   "PROJECT",
                   "OTHER"
                
                14. priority must be one of:
                   "VERY_HIGH",
                   "HIGH",
                   "MEDIUM",
                   "LOW"
                
                15. expected_score_gain must be an integer from 0 to 100.
                
                16. job_evidence must be based ONLY on the supplied job profile.
                
                17. cv_evidence must be based ONLY on the supplied candidate
                    profile.
                
                18. related_existing_strengths must contain only evidence that
                    actually exists in the candidate profile.
                
                19. recommended_action must be practical and specific.
                
                20. deliverable should be a concrete output demonstrating the
                    missing requirement whenever practical.
                
                21. Education recommendations are allowed only when the job
                    explicitly requires a qualification the candidate lacks.
                
                22. Certification recommendations are allowed only when the
                    job explicitly requires or strongly prefers the
                    certification.
                
                23. Experience recommendations should focus on demonstrable
                    experience such as projects, internships, freelance work,
                    or relevant professional tasks.
                
                24. Project recommendations must directly demonstrate the
                    missing job requirement.
                
                25. Language recommendations are allowed only when the job
                    explicitly requires the language.
                
                26. Do NOT invent certification names, project history,
                    employment history, education, skills, achievements, or
                    experience.
                
                27. Do NOT hallucinate URLs.
                
                28. Return at most 5 recommendations.
                
                29. Prioritize mandatory and high-impact gaps.
                
                30. If there are no meaningful gaps, return an empty
                    recommendations array.
                
                31. Return ONLY JSON.
                
                32. No markdown.
                
                33. No explanation outside the JSON.
                
                Candidate Profile:
                %s
                
                Job Requirements:
                %s
                """.formatted(
                matchScore,
                candidate.getSkills(),
                candidate.getExperienceSummary(),
                candidate.getExperienceMonths(),
                candidate.getPastTitles(),
                JsonUtils.toJson(candidate.getProjects()),
                JsonUtils.toJson(candidate.getEducation()),
                JsonUtils.toJson(candidate.getCertifications()),
                JsonUtils.toJson(candidate.getLanguages()),
                job.getRequiredSkills(),
                job.getPreferredSkills(),
                job.getExperienceMonths(),
                job.getExperienceLevel(),
                job.getResponsibilities(),
                job.getEducationRequirements(),
                job.getCertificationRequirements(),
                job.getLanguageRequirements(),
                job.getOtherRequirements(),
                matchedRequired,
                missingRequired,
                matchedPreferred,
                missingPreferred,
                JsonUtils.toJson(candidate),
                JsonUtils.toJson(job)
        );
    }
}