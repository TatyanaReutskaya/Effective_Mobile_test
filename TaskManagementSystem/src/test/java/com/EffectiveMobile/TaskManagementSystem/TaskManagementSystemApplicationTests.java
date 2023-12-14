package com.EffectiveMobile.TaskManagementSystem;

import com.EffectiveMobile.TaskManagementSystem.dto.PersonAuthDTO;
import com.EffectiveMobile.TaskManagementSystem.dto.TaskDTO;
import com.EffectiveMobile.TaskManagementSystem.models.Person;
import com.EffectiveMobile.TaskManagementSystem.models.Task;
import com.EffectiveMobile.TaskManagementSystem.repositories.PersonRepository;
import com.EffectiveMobile.TaskManagementSystem.repositories.TaskRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskManagementSystemApplicationTests {
	private final TaskRepository taskRepository;
	private final PersonRepository personRepository;
	private final TestRestTemplate restTemplate;
	private HttpHeaders headers;
	private final EntityManager entityManager;
	@Autowired
	TaskManagementSystemApplicationTests(TaskRepository taskRepository, PersonRepository personRepository, TestRestTemplate restTemplate, EntityManager entityManager) {
		this.taskRepository = taskRepository;
		this.personRepository = personRepository;
		this.restTemplate = restTemplate;
		this.entityManager = entityManager;
	}

	@BeforeEach
	public void setup() {
		ResponseEntity<String> loginResponse = restTemplate.postForEntity("/login", new PersonAuthDTO("person2@mail.ru", "123"), String.class);
		String token = loginResponse.getBody();
		headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
	}
	@Test
	public void testPersonInfo() {
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		ResponseEntity<Person> response = restTemplate.exchange("/personInfo", HttpMethod.GET, requestEntity, Person.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("person2@mail.ru", response.getBody().getEmail());
	}

	@Test
	public void testEditTask() {
		Task task = taskRepository.findById(2).orElse(null);
		TaskDTO taskDTO = new ModelMapper().map(task,TaskDTO.class);
		taskDTO.setTitle("change");
		HttpEntity<TaskDTO> requestEntity = new HttpEntity<>(taskDTO, headers);
		ResponseEntity<Void> response1 = restTemplate.exchange(
				"/edit?taskId=2",
				HttpMethod.POST,
				requestEntity,
				Void.class);
		assertEquals(HttpStatus.OK, response1.getStatusCode());

		HttpEntity<?> requestEntity1 = new HttpEntity<>(headers);
		ResponseEntity<Void> response2 = restTemplate.exchange(
				"/edit?taskId=3&progress=3",
				HttpMethod.POST,
				requestEntity1,
				Void.class);
		assertEquals(HttpStatus.OK, response2.getStatusCode());

		ResponseEntity<Void> response3 = restTemplate.exchange(
				"//edit?taskId=2&progress=3",
				HttpMethod.POST,
				requestEntity1,
				Void.class);
		assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode());
	}

	@Test
	public void testShowTask() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Person> query = cb.createQuery(Person.class);
		Root<Person> root = query.from(Person.class);
		root.fetch("tasksOwner", JoinType.LEFT);
		query.select(root).where(cb.equal(root.get("email"), "person2@mail.ru"));
		Person person = entityManager.createQuery(query).getSingleResult();
		List<Task> tasks = person.getTasksOwner();

		HttpEntity<TaskDTO> requestEntity = new HttpEntity<>(headers);
		ParameterizedTypeReference<List<Task>> responseType = new ParameterizedTypeReference<List<Task>>() {};
		ResponseEntity<List<Task>> response1 = restTemplate.exchange(
				"/tasks?owner=person2@mail.ru",
				HttpMethod.GET,
				requestEntity,
				responseType);
		assertEquals(HttpStatus.OK, response1.getStatusCode());
		assertEquals(tasks.size(), response1.getBody().size());

		ResponseEntity<List<Task>> response2 = restTemplate.exchange(
				"/tasks?owner=person2@mail.ru&executor=person3@mail.ru",
				HttpMethod.GET,
				requestEntity,
				responseType);
		assertEquals(HttpStatus.OK, response2.getStatusCode());
		assertEquals(tasks.stream().filter(task->task.getExecutor().getEmail().equals("person3@mail.ru")).toList().size(), response2.getBody().size());
	}
}
