package com.EffectiveMobile.TaskManagementSystem.dto;

import com.EffectiveMobile.TaskManagementSystem.models.Person;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TaskDTO {
    @NotEmpty
    @Size(max = 100, min = 2, message ="Title should be from 2 to 100 characters")
    private String title;

    @NotEmpty
    @Size(max = 100, min = 2, message ="Description should be from 2 to 100 characters")
    private String description;

    @Min(value = 1, message = "The task has the highest priority (1)")
    @Max(value = 3, message = "The task has the lowest priority (3)")
    private int priority;

    @NotNull
    private Person executor;
}
