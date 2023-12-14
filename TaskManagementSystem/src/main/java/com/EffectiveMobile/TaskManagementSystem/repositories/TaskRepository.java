package com.EffectiveMobile.TaskManagementSystem.repositories;

import com.EffectiveMobile.TaskManagementSystem.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Integer> {
}
