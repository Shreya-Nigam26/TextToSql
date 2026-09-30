package com.retail.texttosql.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiServiceTest {

    private GeminiService geminiService;

    @BeforeEach
    public void setUp() {
        geminiService = new GeminiService();
        ReflectionTestUtils.setField(geminiService, "projectId", "test-project");
        ReflectionTestUtils.setField(geminiService, "location", "us-central1");
        ReflectionTestUtils.setField(geminiService, "model", "gemini-1.5-flash");
        ReflectionTestUtils.setField(geminiService, "serviceAccountKeyPath", "");
    }

    @Test
    public void testConvertToSQL_ClientNotInitialized_ThrowsException() {
        // Arrange
        ReflectionTestUtils.setField(geminiService, "client", null);
        String naturalLanguageQuery = "Show me all products";

        // Act & Assert
        Exception exception = assertThrows(
            Exception.class,
            () -> geminiService.convertToSQL(naturalLanguageQuery)
        );
        assertTrue(exception.getMessage().contains("Gemini client not initialized"));
    }

    @Test
    public void testExtractSQLFromResponse_WithMarkdownSQL() {
        // Arrange
        String response = "```sql\nSELECT * FROM products WHERE price > 100 LIMIT 100\n```";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products WHERE price > 100 LIMIT 100", result);
    }

    @Test
    public void testExtractSQLFromResponse_WithSimpleMarkdown() {
        // Arrange
        String response = "```\nSELECT * FROM products\n```";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products", result);
    }

    @Test
    public void testExtractSQLFromResponse_NoMarkdown() {
        // Arrange
        String response = "SELECT * FROM products LIMIT 100";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products LIMIT 100", result);
    }

    @Test
    public void testExtractSQLFromResponse_WithTrailingText() {
        // Arrange
        String response = "SELECT * FROM products; Here is your query";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products;", result);
    }

    @Test
    public void testExtractSQLFromResponse_WithMarkdownAndTrailingText() {
        // Arrange
        String response = "```sql\nSELECT * FROM products;\n``` This is extra text";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products;", result);
    }

    @Test
    public void testExtractSQLFromResponse_OnlySemicolon() {
        // Arrange
        String response = "SELECT * FROM products;";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products;", result);
    }

    @Test
    public void testExtractSQLFromResponse_WithWhitespace() {
        // Arrange
        String response = "  SELECT * FROM products  ";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products", result);
    }

    @Test
    public void testBuildPrompt() {
        // Arrange
        String naturalLanguageQuery = "Show me all products";

        // Act
        String prompt = geminiService.buildPrompt(naturalLanguageQuery);

        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Database Schema"));
        assertTrue(prompt.contains("Show me all products"));
        assertTrue(prompt.contains("SQL expert"));
        assertTrue(prompt.contains("PostgreSQL"));
    }

    @Test
    public void testBuildPrompt_ComplexQuery() {
        // Arrange
        String naturalLanguageQuery = "Show total sales by customer region for last month";

        // Act
        String prompt = geminiService.buildPrompt(naturalLanguageQuery);

        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Database Schema"));
        assertTrue(prompt.contains("Show total sales by customer region for last month"));
        assertTrue(prompt.contains("categories"));
        assertTrue(prompt.contains("products"));
        assertTrue(prompt.contains("customers"));
        assertTrue(prompt.contains("sales"));
    }

    @Test
    public void testExtractSQLFromResponse_MultipleSemicolons() {
        // Arrange
        String response = "SELECT * FROM products; SELECT * FROM customers;";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products;", result);
    }

    @Test
    public void testExtractSQLFromResponse_NoSemicolon() {
        // Arrange
        String response = "SELECT * FROM products";

        // Act
        String result = geminiService.extractSQLFromResponse(response);

        // Assert
        assertEquals("SELECT * FROM products", result);
    }
}
