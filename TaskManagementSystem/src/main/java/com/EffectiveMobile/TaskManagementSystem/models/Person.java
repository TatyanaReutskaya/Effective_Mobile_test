package com.EffectiveMobile.TaskManagementSystem.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


@Entity
@Data
@Table(name = "person")
@Schema(description="Данные о пользователе")
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    @NotEmpty(message = "Email shouldn`t be empty")
    @Email
    private String email;

    @JsonIgnore
    @Column(name = "password",length = 200)
    private String password;

    @JsonIgnore
    @OneToMany (mappedBy = "owner", cascade = { CascadeType.REMOVE})
    @Schema(description="Список созданных пользователем задач")
    private List<Task> tasksOwner;

    @JsonIgnore
    @OneToMany (mappedBy = "executor", cascade = { CascadeType.REMOVE})
    @Schema(description="Список задач к исполнению")
    private List<Task> tasksExecutor;

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
