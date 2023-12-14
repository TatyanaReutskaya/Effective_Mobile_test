package com.EffectiveMobile.TaskManagementSystem.controllers;

import com.EffectiveMobile.TaskManagementSystem.dto.*;
import com.EffectiveMobile.TaskManagementSystem.exception.NotFoundException;
import com.EffectiveMobile.TaskManagementSystem.exception.ValidationException;
import com.EffectiveMobile.TaskManagementSystem.models.Comment;
import com.EffectiveMobile.TaskManagementSystem.models.Person;
import com.EffectiveMobile.TaskManagementSystem.models.Task;
import com.EffectiveMobile.TaskManagementSystem.security.PersonDetails;
import com.EffectiveMobile.TaskManagementSystem.services.PersonService;
import com.EffectiveMobile.TaskManagementSystem.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MainController {
    private final PersonService personService;
    private final TaskService taskService;
    @Autowired
    public MainController(PersonService personService, TaskService taskService) {
        this.personService = personService;
        this.taskService = taskService;
    }

    @GetMapping("/personInfo")
    @Operation(summary = "Информация о пользователе")
    public Person info(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        return personDetails.getPerson();
    }
    @PostMapping("/registration")
    @Operation(summary = "Регистрация",
            responses = {
                    @ApiResponse (responseCode = "200", description = "Регистрация успешно завершена, возвращается jwt"),
                    @ApiResponse (responseCode = "400", description = "Не валидный объект, регистрация не выполнена")
            })
    public ResponseEntity<String> registration(@RequestBody @Valid PersonAuthDTO personAuthDTO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            throw new ValidationException(validateError(bindingResult));
        }
        Person person = (Person)dtoToModel(personAuthDTO,Person.class);
        personService.registration(person);
        return ResponseEntity.ok(personService.getToken(person));
    }
    @PostMapping ("/login")
    @Operation(summary = "Вход в приложение",
            responses = {
                    @ApiResponse (responseCode = "200", description = "Вход выолнен, возвращается jwt"),
                    @ApiResponse (responseCode = "400", description = "Не верный логин или пароль")
            })
    public String login(@RequestBody PersonAuthDTO personAuthDTO){
        return personService.login((Person)dtoToModel(personAuthDTO,Person.class));
    }
    @PostMapping("/createNewTask")
    @Operation(summary = "Добавление новой задачи",
    responses = {
            @ApiResponse (responseCode = "200", description = "Задача успешно добалена"),
            @ApiResponse (responseCode = "400", description = "Не валидный объект, задача не добавлена")
    })
    public ResponseEntity<HttpStatus> createNewTask(@RequestBody @Valid TaskDTO taskDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(validateError(bindingResult));
        }
        Task task = (Task)dtoToModel(taskDTO,Task.class);
        taskService.createNewTask(task);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/edit")
    @Operation(summary = "Редактирование задачи",
            description = "Создатель задачи может менять все кроме прогресса; исполнитель задачи может менять только прогресс",
            responses = {
                    @ApiResponse (responseCode = "200", description = "Задача успешно изменена"),
                    @ApiResponse (responseCode = "400", description = "Не валидный объект, задача не изменена"),
                    @ApiResponse (responseCode = "404", description = "Задача не существует, либо пользователь не является создателем или исполнителем задачи.")
            })
    public ResponseEntity<HttpStatus> editTask(@RequestParam (name = "taskId") int id,
                                               @RequestParam (name = "progress",required = false) Integer progress,
                                               @RequestBody(required = false) @Valid TaskDTO taskDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(validateError(bindingResult));
        }
        if ((progress==null && taskDTO==null) || (progress!=null && taskDTO!=null)) {
            throw new ValidationException("Некорректные входные данные");
        }
        if (progress==null) {
            Task task = (Task)dtoToModel(taskDTO, Task.class);
            taskService.edit(id,task);
        }
        else {
            if (progress<1 || progress>3){
                throw new ValidationException("Прогресс задачи должен быть от 1 (ожидание) до 3 (выполнено)");
            }
            taskService.edit(id,progress);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }
    @PostMapping("/deleteTask")
    @Operation(summary = "Удаление задачи",
            description = "Удалить задачу может только создатель",
            responses = {
                    @ApiResponse (responseCode = "200", description = "Задача успешно удалена"),
                    @ApiResponse (responseCode = "400", description = "Задача не найдена или пользователь не является создателем задачи")
            })
    public ResponseEntity<HttpStatus> deleteTask(@RequestParam(name = "taskId") int id){
        taskService.delete(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    @GetMapping("/tasks")
    @Operation(summary = "Получение списка задач по фильтру с пагинацией",
            description = "Для получения всех созданных задач человека указать параметр owner; " +
                    "для получения списка задач на исполнение - параметр executor;" +
                    "для фильтрации по приоритету - параметр priority;" +
                    "для фильтрации по прогрессу выполнения - progress;" +
                    "для пагинации - параметр page (номер страницы, нумерация с 0)." +
                    "Поддерживаются комбинации фильтров.",
            responses = {
                    @ApiResponse (responseCode = "200", description = "Операция завершена успешно.")})
    public ResponseEntity<List<Task>> getTasksWithFilter(@RequestParam(name = "owner",required = false) String emailOwner,
                                                            @RequestParam(name = "executor", required = false) String emailExecutor,
                                                            @RequestParam(name = "priority", required = false) Integer priority,
                                                            @RequestParam(name = "progress",required = false) Integer progress,
                                                            @RequestParam(name = "page",required = false) Integer page){
        return ResponseEntity.ok(taskService.getTasks(emailOwner,emailExecutor,priority,progress,page));
    }
    @PostMapping("/addComment")
    @Operation(summary = "Добавление комментария",
            responses = {
                    @ApiResponse (responseCode = "200", description = "Комментарий успешно добавлен"),
                    @ApiResponse (responseCode = "400", description = "Не валидный комментарий"),
            })
    public ResponseEntity<HttpStatus> addComment(@RequestBody @Valid CommentDTO commentDTO,BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            throw new ValidationException(validateError(bindingResult));
        }
        Comment comment = (Comment)dtoToModel(commentDTO, Comment.class);
        taskService.addComment(comment);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    private Object dtoToModel(Object objectDTO,Class classTo){
        return new ModelMapper().map(objectDTO, classTo);
    }
    private String validateError(BindingResult bindingResult) {
        StringBuilder errorMessage = new StringBuilder("Validation errors: {");
        bindingResult.getFieldErrors().forEach(er->errorMessage.append(er.getField()).append(": ").append(er.getDefaultMessage()).append("; "));
        errorMessage.append("}");
        return errorMessage.toString();
    }
@ExceptionHandler
    private ResponseEntity<String> validationFailed(ValidationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
}

    @ExceptionHandler
    private ResponseEntity<String> authenticationFailed1(BadCredentialsException e) {
        return ResponseEntity.badRequest().body("Incorrect email or password");
    }
    @ExceptionHandler
    private ResponseEntity<String> notFound(NotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
