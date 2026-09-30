package com.retail.texttosql.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SQLExecutionServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SQLExecutionService sqlExecutionService;

    @Test
    public void testExecuteQuery_Success() {
        // Arrange
        String sqlQuery = "SELECT * FROM products";
        List<Map<String, Object>> expectedResults = new ArrayList<>();
        expectedResults.add(Map.of("id", 1, "name", "Product A"));
        
        when(jdbcTemplate.queryForList(anyString())).thenReturn(expectedResults);

        // Act
        List<Map<String, Object>> results = sqlExecutionService.executeQuery(sqlQuery);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(jdbcTemplate, times(1)).queryForList(contains("SELECT"));
    }

    @Test
    public void testExecuteQuery_WithLimit() {
        // Arrange
        String sqlQuery = "SELECT * FROM products";
        List<Map<String, Object>> expectedResults = new ArrayList<>();
        
        when(jdbcTemplate.queryForList(anyString())).thenReturn(expectedResults);

        // Act
        sqlExecutionService.executeQuery(sqlQuery);

        // Assert
        verify(jdbcTemplate, times(1)).queryForList(contains("LIMIT 100"));
    }

    @Test
    public void testExecuteQuery_ExistingLimit() {
        // Arrange
        String sqlQuery = "SELECT * FROM products LIMIT 50";
        List<Map<String, Object>> expectedResults = new ArrayList<>();
        
        when(jdbcTemplate.queryForList(anyString())).thenReturn(expectedResults);

        // Act
        sqlExecutionService.executeQuery(sqlQuery);

        // Assert
        verify(jdbcTemplate, times(1)).queryForList(argThat(sql -> !sql.contains("LIMIT 100")));
    }

    @Test
    public void testExecuteQuery_NonSelectQuery_ThrowsException() {
        // Arrange
        String sqlQuery = "UPDATE products SET name = 'New Name'";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("Only SELECT queries are allowed"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsDropKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products; DROP TABLE products;";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("DROP"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsDeleteKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products WHERE id = 1; DELETE FROM products;";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("DELETE"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsUpdateKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products; UPDATE products SET name = 'test';";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("UPDATE"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsInsertKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products; INSERT INTO products VALUES (1, 'test');";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("INSERT"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsAlterKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products; ALTER TABLE products ADD COLUMN test;";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("ALTER"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsTruncateKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products; TRUNCATE TABLE products;";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("TRUNCATE"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsGrantKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products; GRANT ALL ON products TO user;";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("GRANT"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_ContainsRevokeKeyword_ThrowsException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products; REVOKE ALL ON products FROM user;";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("REVOKE"));
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testExecuteQuery_DatabaseError_ThrowsRuntimeException() {
        // Arrange
        String sqlQuery = "SELECT * FROM products";
        when(jdbcTemplate.queryForList(anyString())).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> sqlExecutionService.executeQuery(sqlQuery)
        );
        assertTrue(exception.getMessage().contains("Failed to execute SQL query"));
    }
}
