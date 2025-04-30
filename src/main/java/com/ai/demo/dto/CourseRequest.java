package com.ai.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Course name is required")
    private String name;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotBlank(message = "Level is required")
    private String level;
    
    @NotNull(message = "Number of students is required")
    @Min(value = 1, message = "Number of students must be at least 1")
    private Integer studentsCount = 1;
    
    private String description;
}