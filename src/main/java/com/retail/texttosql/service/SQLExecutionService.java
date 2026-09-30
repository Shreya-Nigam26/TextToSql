package com.retail.texttosql.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SQLExecutionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Cacheable(value = "queryResults", key = "#sqlQuery")
    public List<Map<String, Object>> executeQuery(String sqlQuery) {
        log.info("Executing SQL query: {}", sqlQuery);
        
        // Validate that it's a SELECT query only
        String upperQuery = sqlQuery.trim().toUpperCase();
        if (!upperQuery.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed for security reasons");
        }
        
        // Check for dangerous keywords
        String[] dangerousKeywords = {"DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "TRUNCATE", "GRANT", "REVOKE"};
        for (String keyword : dangerousKeywords) {
            if (upperQuery.contains(keyword)) {
                throw new IllegalArgumentException("Query contains dangerous keyword: " + keyword);
            }
        }
        
        try {
            // Add a limit if not present to prevent large result sets
            if (!upperQuery.contains("LIMIT")) {
                sqlQuery += " LIMIT 100";
            }
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            log.info("Query executed successfully, returned {} rows", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error executing SQL query", e);
            throw new RuntimeException("Failed to execute SQL query: " + e.getMessage(), e);
        }
    }
}
