package com.retail.texttosql.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class QueryRequestTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidQuery() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("Show me all products");

        // Act
        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testBlankQuery_ThrowsValidationError() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("");

        // Act
        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Query cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    public void testNullQuery_ThrowsValidationError() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery(null);

        // Act
        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Query cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    public void testWhitespaceOnlyQuery_ThrowsValidationError() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("   ");

        // Act
        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    public void testConstructor() {
        // Act
        QueryRequest request = new QueryRequest();

        // Assert
        assertNotNull(request);
        assertNull(request.getQuery());
    }

    @Test
    public void testAllArgsConstructor() {
        // Act
        QueryRequest request = new QueryRequest("Test query");

        // Assert
        assertEquals("Test query", request.getQuery());
    }

    @Test
    public void testSetterAndGetter() {
        // Arrange
        QueryRequest request = new QueryRequest();

        // Act
        request.setQuery("New query");

        // Assert
        assertEquals("New query", request.getQuery());
    }
}
