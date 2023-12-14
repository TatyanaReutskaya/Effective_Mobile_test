package com.EffectiveMobile.TaskManagementSystem.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "task")
@Data
@Schema(description="Данные о задаче.")
public class Task {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title")
    @NotEmpty
    @Size(max = 100, min = 2, message ="Title should be from 2 to 100 characters")
    private String title;

    @Column(name = "description")
    @NotEmpty
    @Size(max = 100, min = 2, message ="Description should be from 2 to 100 characters")
    private String description;

    @Column(name = "priority")
    @Min(value = 1, message = "The task has the highest priority (1)")
    @Max(value = 3, message = "The task has the lowest priority (3)")
    @Schema(description="Приоритет задачи. 1 - срочно, 2 - средний, 3 - не строчно",type = "int")
    private int priority;

    @Column (name = "progress")
    @Min(value = 1, message = "Pending task (1)")
    @Max(value = 3, message = "The task completed (3)")
    @Schema(description="Прогресс задачи. 1 - ожидание, 2 - в работе, 3 - выполнено",type = "int")
    private int progress;

    @ManyToOne
    @JoinColumn(name = "owner_id",referencedColumnName = "id")
    @Schema(description="Создатель задачи")
    private Person owner;

    @ManyToOne
    @JoinColumn(name = "executor_id",referencedColumnName = "id")
    @Schema(description="Исполнитель задачи")
    private Person executor;

    @OneToMany(mappedBy = "task",cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Comment> comments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return getId() == task.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", progress=" + progress +
                ", executor=" + executor +
                '}';
    }
}
