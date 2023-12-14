package com.EffectiveMobile.TaskManagementSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description="Для регистрации пользователя и авторизации")
public class PersonAuthDTO {
    @Email
    @NotEmpty
    private String email;
    @NotEmpty
    private String password;

    public PersonAuthDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public PersonAuthDTO() {
    }
}
