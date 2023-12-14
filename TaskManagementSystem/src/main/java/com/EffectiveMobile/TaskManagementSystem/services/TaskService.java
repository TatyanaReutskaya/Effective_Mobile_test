package com.EffectiveMobile.TaskManagementSystem.services;

import com.EffectiveMobile.TaskManagementSystem.exception.NotFoundException;
import com.EffectiveMobile.TaskManagementSystem.exception.ValidationException;
import com.EffectiveMobile.TaskManagementSystem.models.Comment;
import com.EffectiveMobile.TaskManagementSystem.models.Person;
import com.EffectiveMobile.TaskManagementSystem.models.Task;
import com.EffectiveMobile.TaskManagementSystem.repositories.CommentRepository;
import com.EffectiveMobile.TaskManagementSystem.repositories.PersonRepository;
import com.EffectiveMobile.TaskManagementSystem.repositories.TaskRepository;
import com.EffectiveMobile.TaskManagementSystem.security.PersonDetails;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TaskService {
    private final PersonRepository personRepository;
    private final TaskRepository taskRepository;
    private  final CommentRepository commentRepository;
    private final int DEFAULT_PROGRESS = 1;
    private final int PAGE_SIZE = 2;
    private final EntityManager entityManager;
    @Autowired
    public TaskService(PersonRepository personRepository, TaskRepository taskRepository, CommentRepository commentRepository, EntityManager entityManager) {
        this.personRepository = personRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void createNewTask(Task task) {
        task.setId(0);
        task.setProgress(DEFAULT_PROGRESS);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Person owner = personDetails.getPerson();
        Person executor = personRepository.findByEmail(task.getExecutor().getEmail()).orElse(null);
        if (executor==null) {
            throw new ValidationException("The executor is not exist");
        }
        task.setExecutor(executor);
        task.setOwner(owner);
        taskRepository.save(task);
    }
    @Transactional
    public List<Task> getTasks(String emailOwner, String emailexecutor, Integer priority, Integer progress, Integer page) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder hql = new StringBuilder("select t from Task t left join t.comments where ");
        List<Task> result=new ArrayList<>();
        Query<Task> query;
        int idOwner;
        int idExecutor;
        if (emailexecutor==null) {
            if (emailOwner==null) {
                if (priority==null) {
                    if(progress==null){
                        return Collections.emptyList();
                    }
                    else {
                        hql.append("t.progress = :progress");
                        query = session.createQuery(hql.toString())
                                .setParameter("progress", progress);
                    }
                }
                else {
                    if (progress==null) {
                        hql.append("t.priority = :priority");
                        query = session.createQuery(hql.toString())
                                .setParameter("priority", priority);
                    }
                    else {
                        hql.append("t.priority = :priority and t.progress = :progress");
                        query = session.createQuery(hql.toString())
                                .setParameter("priority", priority)
                                .setParameter("progress", progress);
                    }
                }
                if (page!=null) {
                    query.setFirstResult(page*PAGE_SIZE);
                    query.setMaxResults(PAGE_SIZE);
                }
                return query.getResultList();
            }
            else {
                Person owner = personRepository.findByEmail(emailOwner).orElse(null);
                if (owner==null) {
                    return Collections.emptyList();
                }
                idOwner = owner.getId();
                hql.append("t.owner.id = :ownerId");
                query = session.createQuery(hql.toString())
                        .setParameter("ownerId", idOwner);
            }
        }
        else {
            if (emailOwner==null) {
                Person executor = personRepository.findByEmail(emailexecutor).orElse(null);
                if (executor==null) {
                    return Collections.emptyList();
                }
                hql.append("t.executor.id = :executorId");
                idExecutor = executor.getId();
                query = session.createQuery(hql.toString())
                        .setParameter("executorId", idExecutor);
            }
            else {
                Person owner = personRepository.findByEmail(emailOwner).orElse(null);
                Person executor = personRepository.findByEmail(emailexecutor).orElse(null);
                if (owner!=null && executor!=null) {
                    hql.append("t.owner.id = :ownerId and t.executor.id = :executorId");
                    query = session.createQuery(hql.toString())
                            .setParameter("ownerId", owner.getId())
                            .setParameter("executorId", executor.getId());
                }
                else {
                    return Collections.emptyList();
                }
            }
        }
        if (page!=null) {
            query.setFirstResult(page*PAGE_SIZE);
            query.setMaxResults(PAGE_SIZE);
        }
        result = query.getResultList();

        if(priority==null) {
            if (progress==null) {
                return result;
            }
            else {
                return result.stream().filter(t->t.getProgress()==progress).toList();
            }
        }
        else {
            if (progress==null) {
                return result.stream().filter(t->t.getPriority()==priority).toList();
            }
            else return result.stream().filter(t->(t.getPriority()==priority)&&(t.getProgress()==progress)).toList();
        }
    }

    @Transactional
    public void edit(int id, Task task){
        Task taskChange = taskRepository.findById(id).orElse(null);
        if (taskChange==null) {
            throw new NotFoundException("Not foud task whith id = "+task.getId());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Person owner = personRepository.findByEmail(personDetails.getUsername()).orElse(null);
        if (owner.getTasksOwner().contains(taskChange)) {
            Person executor = personRepository.findByEmail(task.getExecutor().getEmail()).orElse(null);
            if (executor==null){
                throw new ValidationException("Executor "+task.getExecutor().getEmail()+"not found");
            }
            task.setId(id);
            task.setProgress(taskChange.getProgress());
            task.setOwner(owner);
            task.setExecutor(executor);
            taskRepository.save(task);
        }
        else {
            throw new NotFoundException("Not foud task whith id = "+id+" for person "+owner.getEmail());
        }
    }

    @Transactional
    public void edit(int id, int progress){
        Task taskChange = taskRepository.findById(id).orElse(null);
        if (taskChange==null) {
            throw new NotFoundException("Not foud task whith id = "+id);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Person executor = personRepository.findByEmail(personDetails.getUsername()).orElse(null);
        if(executor.getTasksExecutor().contains(taskChange)) {
            taskChange.setProgress(progress);
            taskRepository.save(taskChange);
        }
        else {
            throw new NotFoundException("Not foud task whith id = "+id+" for person "+executor.getEmail()+" to execute");
        }
    }
    /*@Transactional
    public void edit(Task task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Person person = personRepository.findByEmail(personDetails.getUsername()).orElse(null);
        Task taskChange = taskRepository.findById(task.getId()).orElse(null);
        if (taskChange==null) {
            throw new NotFoundException("Not foud task whith id = "+task.getId());
        }
        if (person.getTasksOwner().contains(task)) {
            Person executor = personRepository.findByEmail(task.getExecutor().getEmail()).orElse(null);
            if (executor==null) {
                throw new ValidationException("Executor "+task.getExecutor().getEmail()+"not found");
            }
            task.setOwner(person);
            task.setExecutor(executor);
            task.setProgress(taskChange.getProgress());
            person.getTasksOwner().removeIf(t ->t.getId()==task.getId());
            person.getTasksOwner().add(task);
            taskRepository.save(task);
        } else if (person.getTasksExecutor().contains(task)) {
            if (task.getProgress()==0) {
                throw new ValidationException("Progress should be from 1 (pending task) to 3 (completed task)");
            }
            taskChange.setProgress(task.getProgress());
            person.getTasksExecutor().removeIf(t ->t.getId()==taskChange.getId());
            person.getTasksExecutor().add(taskChange);
            taskRepository.save(taskChange);
        }
                else {
                    throw new NotFoundException("Not foud task whith id = "+task.getId()+" for person "+person.getEmail());
                }
    }*/
    @Transactional
    public void delete(int id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task==null) {
         throw new NotFoundException("Not foud task whith id = "+id);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Person person = personRepository.findByEmail(personDetails.getUsername()).orElse(null);
        if (person.getTasksOwner().contains(task)) {
         taskRepository.delete(task);
        }
        else {
            throw new NotFoundException("Not foud task whith id = "+id+"for person "+person.getEmail());
        }
    }

    public void addComment(Comment comment) {
        Task task = taskRepository.findById(comment.getTask().getId()).orElse(null);
        if (task==null) {
            throw new NotFoundException("Not foud task whith id = "+comment.getTask().getId());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Person owner = personDetails.getPerson();
        comment.setTask(task);
        comment.setOwnerId(owner.getId());
        comment.setId(0);
        List<Comment> comments = task.getComments();
        if(comments==null) {
            task.setComments(new ArrayList<>(Collections.singletonList(comment)));
        }
        else {
            comments.add(comment);
        }
        commentRepository.save(comment);
    }
}
