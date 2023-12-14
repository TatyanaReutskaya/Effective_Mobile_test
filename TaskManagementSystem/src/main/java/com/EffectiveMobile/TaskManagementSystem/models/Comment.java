package com.EffectiveMobile.TaskManagementSystem.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "comment")
@Data
@Schema(description="Данные о комментарии")
public class Comment {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "owner")
    @Min(value=1)
    @Schema(description="id пользователя оставевшего комментарий")
    private int ownerId;

    @Column(name = "message")
    @NotEmpty
    private String message;

    @ManyToOne
    @JoinColumn(name = "task_id",referencedColumnName = "id")
    @NotNull
    @JsonIgnore
    @Schema(description="Задача к которой оставлен комментарий")
    private Task task;

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", message='" + message + '\'' +
                ", task=" + task +
                '}';
    }
}
