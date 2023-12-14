package com.EffectiveMobile.TaskManagementSystem.filters;

import com.EffectiveMobile.TaskManagementSystem.security.JWTProvider;
import com.EffectiveMobile.TaskManagementSystem.security.PersonDetails;
import com.EffectiveMobile.TaskManagementSystem.services.PersonDetailsService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTProvider jwtProvider;
    private final PersonDetailsService personDetailsService;

    public JWTFilter(JWTProvider jwtProvider, PersonDetailsService personDetailsService) {
        this.jwtProvider = jwtProvider;
        this.personDetailsService = personDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader!=null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            if (jwt.isBlank()) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid JWT");
            }
            else {
                try {
                    String email = jwtProvider.claimFromJWT(jwt);
                    PersonDetails personDetails = (PersonDetails) personDetailsService.loadUserByUsername(email);
                    if (SecurityContextHolder.getContext().getAuthentication()==null) {
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(personDetails,personDetails.getPassword(),personDetails.getAuthorities())
                        );
                    }
                }
                catch (JWTVerificationException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid JWT");
                    return;
                }
            }
        }
        filterChain.doFilter(request,response);
    }
}
