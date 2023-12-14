package com.EffectiveMobile.TaskManagementSystem.repositories;

import com.EffectiveMobile.TaskManagementSystem.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Integer> {
}
