package com.EffectiveMobile.TaskManagementSystem.services;

import com.EffectiveMobile.TaskManagementSystem.models.Person;
import com.EffectiveMobile.TaskManagementSystem.models.Task;
import com.EffectiveMobile.TaskManagementSystem.repositories.PersonRepository;
import com.EffectiveMobile.TaskManagementSystem.security.JWTProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PersonService {
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTProvider jwtProvider;
    private final AuthenticationProvider authenticationProvider;
    @Autowired
    public PersonService(PersonRepository personRepository, PasswordEncoder passwordEncoder, JWTProvider jwtProvider, /*AuthenticationManager authenticationManager,*/ AuthenticationProvider authenticationProvider) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.authenticationProvider = authenticationProvider;
    }
    @Transactional
    public void registration(Person person){
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        personRepository.save(person);
    }

    public String login(Person person) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(person.getEmail(),person.getPassword());
            authenticationProvider.authenticate(authenticationToken);
            return jwtProvider.generateToken(person);
    }

    public String getToken(Person person) {
        return jwtProvider.generateToken(person);
    }

    public Person findByEmail(String email) {
        return personRepository.findByEmail(email).orElse(null);
    }
}
