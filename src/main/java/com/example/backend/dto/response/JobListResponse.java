package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobListResponse {

    private List<JobResponse> results;
    private Integer page;
    private Integer totalPages;
    private Integer totalCount;
    private Integer pageSize;
}