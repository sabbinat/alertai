package com.becoder.user;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sbact1.model.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void passwordValida_noDeberiaTenerViolaciones() {
        User user = new User();
        user.setPassword("Abcdef1!"); // cumple: mayúscula, minúscula, número, símbolo y largo>=8

        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, "password");
        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordInvalida_deberiaTenerViolaciones() {
        User user = new User();
        user.setPassword("abc123"); // no cumple (sin mayúscula, sin símbolo, corto)

        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, "password");
        assertFalse(violations.isEmpty());

        boolean tieneMensajeEsperado = violations.stream()
            .anyMatch(v -> v.getMessage().contains("mayúscula") || v.getMessage().contains("caracteres"));
        assertTrue(tieneMensajeEsperado);
    }
}

