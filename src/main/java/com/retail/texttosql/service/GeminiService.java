package com.retail.texttosql.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

@Service
@Slf4j
public class GeminiService {

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.location}")
    private String location;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${gcp.service.account.key.path:}")
    private String serviceAccountKeyPath;

    private Client client;

    @PostConstruct
    public void init() {
        log.info("=== GeminiService initialized ===");
        log.info("Project ID: {}", projectId);
        log.info("Location: {}", location);
        log.info("Model: {}", model);
        
        try {
            // Load credentials from service account key if provided
            if (serviceAccountKeyPath != null && !serviceAccountKeyPath.isEmpty()) {
                // Remove quotes if present
                String cleanPath = serviceAccountKeyPath.replace("\"", "");
                log.info("Using service account key from: {}", cleanPath);
                
                // Load service account credentials with Vertex AI scope
                ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(cleanPath))
                    .toBuilder()
                    .setScopes(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"))
                    .build();
                
                client = Client.builder()
                    .enterprise(true)
                    .credentials(credentials)
                    .project(projectId)
                    .location(location)
                    .build();
            } else {
                // Use default credentials (ADC)
                client = Client.builder()
                    .enterprise(true)
                    .project(projectId)
                    .location(location)
                    .build();
            }
            
            log.info("Gemini Vertex AI client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Gemini client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Gemini client", e);
        }
    }

    private static final String DATABASE_SCHEMA = """
            Database Schema:
            
            Table: categories
            - id (INTEGER, PRIMARY KEY)
            - name (VARCHAR(100), NOT NULL)
            - description (TEXT)
            
            Table: products
            - id (INTEGER, PRIMARY KEY)
            - name (VARCHAR(200), NOT NULL)
            - category_id (INTEGER, REFERENCES categories(id))
            - price (DECIMAL(10,2), NOT NULL)
            - stock_quantity (INTEGER, DEFAULT 0)
            - created_at (TIMESTAMP)
            
            Table: customers
            - id (INTEGER, PRIMARY KEY)
            - name (VARCHAR(200), NOT NULL)
            - email (VARCHAR(255), UNIQUE)
            - region (VARCHAR(50))
            - phone (VARCHAR(20))
            - created_at (TIMESTAMP)
            
            Table: sales
            - id (INTEGER, PRIMARY KEY)
            - customer_id (INTEGER, REFERENCES customers(id))
            - product_id (INTEGER, REFERENCES products(id))
            - quantity (INTEGER, NOT NULL)
            - unit_price (DECIMAL(10,2), NOT NULL)
            - total_amount (DECIMAL(10,2), NOT NULL)
            - sale_date (TIMESTAMP)
            """;

    @Cacheable(value = "sqlQueries", key = "#naturalLanguageQuery")
    public String convertToSQL(String naturalLanguageQuery) throws IOException {
        log.info("Converting query to SQL using Gemini: {}", naturalLanguageQuery);
        
        if (client == null) {
            throw new IOException("Gemini client not initialized. Check API key configuration.");
        }
        
        String prompt = buildPrompt(naturalLanguageQuery);
        
        try {
            log.debug("Calling Gemini API with model: {}", model);
            
            GenerateContentResponse response = client.models.generateContent(model, prompt, null);
            
            if (response == null || response.text() == null || response.text().isEmpty()) {
                throw new IOException("No response from Gemini API");
            }
            
            String generatedText = response.text();
            log.info("Generated text: {}", generatedText);
            
            return extractSQLFromResponse(generatedText);
            
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new IOException("Failed to convert query to SQL: " + e.getMessage(), e);
        }
    }

    String buildPrompt(String naturalLanguageQuery) {
        return String.format("""
                You are a SQL expert. Convert the following natural language query to a PostgreSQL SQL query.
                
                %s
                
                Natural Language Query: %s
                
                Rules:
                1. Generate only the SQL query, no explanations
                2. Use proper PostgreSQL syntax
                3. Use appropriate JOINs when needed
                4. Only use SELECT statements
                5. Limit results to 100 rows if not specified
                6. Return only the raw SQL query without markdown
                
                SQL Query:
                """, DATABASE_SCHEMA, naturalLanguageQuery);
    }

    String extractSQLFromResponse(String response) {
        String sql = response.trim();
        
        // Remove markdown code blocks
        if (sql.startsWith("```sql")) {
            sql = sql.substring(6);
        } else if (sql.startsWith("```")) {
            sql = sql.substring(3);
        }
        
        if (sql.endsWith("```")) {
            sql = sql.substring(0, sql.length() - 3);
        }
        
        // Remove any text after the SQL query
        int semicolonIndex = sql.indexOf(';');
        if (semicolonIndex > 0) {
            sql = sql.substring(0, semicolonIndex + 1);
        }
        
        return sql.trim();
    }
}
