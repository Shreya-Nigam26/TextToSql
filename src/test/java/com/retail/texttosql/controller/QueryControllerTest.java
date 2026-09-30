package com.retail.texttosql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.texttosql.dto.QueryRequest;
import com.retail.texttosql.dto.QueryResponse;
import com.retail.texttosql.service.GeminiService;
import com.retail.texttosql.service.SQLExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueryController.class)
public class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeminiService geminiService;

    @MockBean
    private SQLExecutionService sqlExecutionService;

    @Test
    public void testExecuteNaturalLanguageQuery_Success() throws Exception {
        // Arrange
        QueryRequest request = new QueryRequest("Show me all products");
        String generatedSQL = "SELECT * FROM products LIMIT 100";
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Map.of("id", 1, "name", "Product A"));

        when(geminiService.convertToSQL(anyString())).thenReturn(generatedSQL);
        when(sqlExecutionService.executeQuery(anyString())).thenReturn(results);

        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.originalQuery").value("Show me all products"))
                .andExpect(jsonPath("$.generatedSQL").value(generatedSQL))
                .andExpect(jsonPath("$.rowCount").value(1));
    }

    @Test
    public void testExecuteNaturalLanguageQuery_EmptyResults() throws Exception {
        // Arrange
        QueryRequest request = new QueryRequest("Show products with zero stock");
        String generatedSQL = "SELECT * FROM products WHERE stock_quantity = 0 LIMIT 100";
        List<Map<String, Object>> results = new ArrayList<>();

        when(geminiService.convertToSQL(anyString())).thenReturn(generatedSQL);
        when(sqlExecutionService.executeQuery(anyString())).thenReturn(results);

        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.rowCount").value(0));
    }

    @Test
    public void testExecuteNaturalLanguageQuery_NullRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testExecuteNaturalLanguageQuery_BlankQuery() throws Exception {
        // Arrange
        QueryRequest request = new QueryRequest("");

        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testExecuteNaturalLanguageQuery_GeminiServiceError() throws Exception {
        // Arrange
        QueryRequest request = new QueryRequest("Invalid query");

        when(geminiService.convertToSQL(anyString()))
                .thenThrow(new RuntimeException("Failed to convert query to SQL"));

        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    public void testExecuteNaturalLanguageQuery_SQLExecutionError() throws Exception {
        // Arrange
        QueryRequest request = new QueryRequest("Show me products");
        String generatedSQL = "SELECT * FROM products";

        when(geminiService.convertToSQL(anyString())).thenReturn(generatedSQL);
        when(sqlExecutionService.executeQuery(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid SQL query"));

        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/query/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Text-to-SQL service is running"));
    }

    @Test
    public void testExecuteNaturalLanguageQuery_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testExecuteNaturalLanguageQuery_MissingQueryField() throws Exception {
        // Arrange
        String json = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testExecuteNaturalLanguageQuery_MultipleResults() throws Exception {
        // Arrange
        QueryRequest request = new QueryRequest("Show me all sales");
        String generatedSQL = "SELECT * FROM sales LIMIT 100";
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Map.of("id", 1, "total_amount", 100.0));
        results.add(Map.of("id", 2, "total_amount", 200.0));
        results.add(Map.of("id", 3, "total_amount", 300.0));

        when(geminiService.convertToSQL(anyString())).thenReturn(generatedSQL);
        when(sqlExecutionService.executeQuery(anyString())).thenReturn(results);

        // Act & Assert
        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.rowCount").value(3))
                .andExpect(jsonPath("$.results").isArray());
    }
}
