package com.EffectiveMobile.TaskManagementSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDTO {
    @NotEmpty
    private String message;
    @NotNull
    @Min(value = 1)
    private Integer taskId;
}

