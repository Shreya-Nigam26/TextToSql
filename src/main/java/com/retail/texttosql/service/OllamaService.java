//package com.retail.texttosql.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//
//@Service
//@Slf4j
//public class OllamaService {
//
//    @Value("${ollama.api.url:http://localhost:11434}")
//    private String apiUrl;
//
//    @Value("${ollama.model:mistral}")
//    private String model;
//
//    private final HttpClient httpClient = HttpClient.newHttpClient();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @PostConstruct
//    public void init() {
//        log.info("=== OllamaService initialized ===");
//        log.info("API URL: {}", apiUrl);
//        log.info("Model: {}", model);
//    }
//
//    private static final String DATABASE_SCHEMA = """
//            Database Schema:
//
//            Table: categories
//            - id (INTEGER, PRIMARY KEY)
//            - name (VARCHAR(100), NOT NULL)
//            - description (TEXT)
//
//            Table: products
//            - id (INTEGER, PRIMARY KEY)
//            - name (VARCHAR(200), NOT NULL)
//            - category_id (INTEGER, REFERENCES categories(id))
//            - price (DECIMAL(10,2), NOT NULL)
//            - stock_quantity (INTEGER, DEFAULT 0)
//            - created_at (TIMESTAMP)
//
//            Table: customers
//            - id (INTEGER, PRIMARY KEY)
//            - name (VARCHAR(200), NOT NULL)
//            - email (VARCHAR(255), UNIQUE)
//            - region (VARCHAR(50))
//            - phone (VARCHAR(20))
//            - created_at (TIMESTAMP)
//
//            Table: sales
//            - id (INTEGER, PRIMARY KEY)
//            - customer_id (INTEGER, REFERENCES customers(id))
//            - product_id (INTEGER, REFERENCES products(id))
//            - quantity (INTEGER, NOT NULL)
//            - unit_price (DECIMAL(10,2), NOT NULL)
//            - total_amount (DECIMAL(10,2), NOT NULL)
//            - sale_date (TIMESTAMP)
//            """;
//
//    public String convertToSQL(String naturalLanguageQuery) throws IOException {
//        log.info("Converting query to SQL using Ollama: {}", naturalLanguageQuery);
//
//        String prompt = buildPrompt(naturalLanguageQuery);
//
//        try {
//            String requestBody = buildRequestBody(prompt);
//
//            log.debug("Calling Ollama API at: {}/api/generate", apiUrl);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(apiUrl + "/api/generate"))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            log.info("Response status: {}", response.statusCode());
//
//            if (response.statusCode() != 200) {
//                log.error("Ollama API error: Status={}, Body={}", response.statusCode(), response.body());
//                throw new IOException("Ollama API request failed with status: " + response.statusCode() +
//                    ", body: " + response.body() + ". Make sure Ollama is running: ollama serve");
//            }
//
//            String generatedText = parseResponse(response.body());
//            return extractSQLFromResponse(generatedText);
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new IOException("Request interrupted", e);
//        } catch (Exception e) {
//            log.error("Error calling Ollama API: {}", e.getMessage(), e);
//            throw new IOException("Failed to convert query to SQL: " + e.getMessage(), e);
//        }
//    }
//
//    private String buildPrompt(String naturalLanguageQuery) {
//        return String.format("""
//                You are a SQL expert. Convert the following natural language query to a PostgreSQL SQL query.
//
//                %s
//
//                Natural Language Query: %s
//
//                Rules:
//                1. Generate only the SQL query, no explanations
//                2. Use proper PostgreSQL syntax
//                3. Use appropriate JOINs when needed
//                4. Only use SELECT statements
//                5. Limit results to 100 rows if not specified
//                6. Return only the raw SQL query without markdown
//
//                SQL Query:
//                """, DATABASE_SCHEMA, naturalLanguageQuery);
//    }
//
//    private String buildRequestBody(String prompt) throws IOException {
//        String json = String.format("""
//                {
//                  "model": "%s",
//                  "prompt": %s,
//                  "stream": false,
//                  "temperature": 0.2
//                }
//                """, model, objectMapper.writeValueAsString(prompt));
//        return json;
//    }
//
//    private String parseResponse(String responseBody) throws IOException {
//        JsonNode root = objectMapper.readTree(responseBody);
//        String generatedText = root.path("response").asText();
//
//        if (generatedText.isEmpty()) {
//            throw new IOException("No response in Ollama API response");
//        }
//
//        log.info("Generated text: {}", generatedText);
//        return generatedText;
//    }
//
//    private String extractSQLFromResponse(String response) {
//        String sql = response.trim();
//
//        // Remove markdown code blocks
//        if (sql.startsWith("```sql")) {
//            sql = sql.substring(6);
//        } else if (sql.startsWith("```")) {
//            sql = sql.substring(3);
//        }
//
//        if (sql.endsWith("```")) {
//            sql = sql.substring(0, sql.length() - 3);
//        }
//
//        // Remove any text after the SQL query
//        int semicolonIndex = sql.indexOf(';');
//        if (semicolonIndex > 0) {
//            sql = sql.substring(0, semicolonIndex + 1);
//        }
//
//        return sql.trim();
//    }
//}
