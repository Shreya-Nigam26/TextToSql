package com.retail.texttosql.controller;

import com.retail.texttosql.dto.QueryRequest;
import com.retail.texttosql.dto.QueryResponse;
import com.retail.texttosql.service.SQLExecutionService;
import com.retail.texttosql.service.GeminiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
@Slf4j
@CrossOrigin(origins = "*")
public class QueryController {

    @Autowired
    private GeminiService geminiService;  // Converts text to SQL using Google Cloud Gemini

    @Autowired
    private SQLExecutionService sqlExecutionService; // Executes SQL queries

    @PostMapping
    public ResponseEntity<QueryResponse> executeNaturalLanguageQuery(@Valid @RequestBody QueryRequest request) {
        System.out.println("=== QueryController.executeNaturalLanguageQuery called ===");
        System.out.println("Request object: " + request);
        System.out.println("Request query: " + (request != null ? request.getQuery() : "request is null"));
        
        if (request == null) {
            System.out.println("ERROR: request is null!");
            return ResponseEntity.badRequest().body(QueryResponse.error(null, "Request is null"));
        }
        
        try {
            // Step 1: Convert natural language to SQL using Google Cloud Gemini
            String sqlQuery = geminiService.convertToSQL(request.getQuery());
            
            // Step 2: Execute the generated SQL query
            List<Map<String, Object>> results = sqlExecutionService.executeQuery(sqlQuery);
            
            // Step 3: Return the response
            QueryResponse response = QueryResponse.


                    success(request.getQuery(), sqlQuery, results);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error calling Ollama API", e);
            QueryResponse response = QueryResponse.error(request.getQuery(), 
                "Failed to convert query to SQL: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid query", e);
            QueryResponse response = QueryResponse.error(request.getQuery(), 
                "Invalid query: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Unexpected error processing query", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            QueryResponse response = QueryResponse.error(request.getQuery(), 
                "Unexpected error: " + errorMessage);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Text-to-SQL service is running");
    }
}
