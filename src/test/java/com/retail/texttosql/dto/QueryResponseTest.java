package com.retail.texttosql.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QueryResponseTest {

    @Test
    public void testSuccessResponse() {
        // Arrange
        String originalQuery = "Show me all products";
        String generatedSQL = "SELECT * FROM products LIMIT 100";
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Map.of("id", 1, "name", "Product A"));

        // Act
        QueryResponse response = QueryResponse.success(originalQuery, generatedSQL, results);

        // Assert
        assertEquals(originalQuery, response.getOriginalQuery());
        assertEquals(generatedSQL, response.getGeneratedSQL());
        assertEquals(results, response.getResults());
        assertEquals(1, response.getRowCount());
        assertEquals("success", response.getStatus());
        assertEquals("Query executed successfully", response.getMessage());
    }

    @Test
    public void testSuccessResponse_EmptyResults() {
        // Arrange
        String originalQuery = "Show me products with zero stock";
        String generatedSQL = "SELECT * FROM products WHERE stock_quantity = 0";
        List<Map<String, Object>> results = new ArrayList<>();

        // Act
        QueryResponse response = QueryResponse.success(originalQuery, generatedSQL, results);

        // Assert
        assertEquals(originalQuery, response.getOriginalQuery());
        assertEquals(generatedSQL, response.getGeneratedSQL());
        assertEquals(results, response.getResults());
        assertEquals(0, response.getRowCount());
        assertEquals("success", response.getStatus());
        assertEquals("Query executed successfully", response.getMessage());
    }

    @Test
    public void testSuccessResponse_NullResults() {
        // Arrange
        String originalQuery = "Invalid query";
        String generatedSQL = "SELECT * FROM products";

        // Act
        QueryResponse response = QueryResponse.success(originalQuery, generatedSQL, null);

        // Assert
        assertEquals(originalQuery, response.getOriginalQuery());
        assertEquals(generatedSQL, response.getGeneratedSQL());
        assertNull(response.getResults());
        assertEquals(0, response.getRowCount());
        assertEquals("success", response.getStatus());
        assertEquals("Query executed successfully", response.getMessage());
    }

    @Test
    public void testErrorResponse() {
        // Arrange
        String originalQuery = "Invalid query";
        String errorMessage = "Failed to convert query to SQL";

        // Act
        QueryResponse response = QueryResponse.error(originalQuery, errorMessage);

        // Assert
        assertEquals(originalQuery, response.getOriginalQuery());
        assertEquals("error", response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertEquals(0, response.getRowCount());
        assertNull(response.getGeneratedSQL());
        assertNull(response.getResults());
    }

    @Test
    public void testErrorResponse_NullOriginalQuery() {
        // Arrange
        String errorMessage = "Request is null";

        // Act
        QueryResponse response = QueryResponse.error(null, errorMessage);

        // Assert
        assertNull(response.getOriginalQuery());
        assertEquals("error", response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertEquals(0, response.getRowCount());
    }

    @Test
    public void testSettersAndGetters() {
        // Arrange
        QueryResponse response = new QueryResponse();

        // Act
        response.setOriginalQuery("Test query");
        response.setGeneratedSQL("SELECT * FROM test");
        response.setResults(List.of(Map.of("id", 1)));
        response.setRowCount(5);
        response.setStatus("custom");
        response.setMessage("Custom message");

        // Assert
        assertEquals("Test query", response.getOriginalQuery());
        assertEquals("SELECT * FROM test", response.getGeneratedSQL());
        assertEquals(1, response.getResults().size());
        assertEquals(5, response.getRowCount());
        assertEquals("custom", response.getStatus());
        assertEquals("Custom message", response.getMessage());
    }
}
