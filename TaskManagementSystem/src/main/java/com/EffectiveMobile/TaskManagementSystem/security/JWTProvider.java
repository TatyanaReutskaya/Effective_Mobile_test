package com.EffectiveMobile.TaskManagementSystem.security;

import com.EffectiveMobile.TaskManagementSystem.models.Person;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTProvider {
    @Value("{jwt_secret}")
    private String forSign;
    public String generateToken(Person person){
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());
        return JWT.create()
                .withSubject("Person details")
                .withClaim("email",person.getEmail())
                .withIssuedAt(new Date())
                .withIssuer("TaskManagementSystem")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(forSign));
    }
    public String claimFromJWT(String jwt) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(forSign))
                .withSubject("Person details")
                .withIssuer("TaskManagementSystem")
                .build();
        DecodedJWT token = verifier.verify(jwt);
        return token.getClaim("email").asString();
    }
}
