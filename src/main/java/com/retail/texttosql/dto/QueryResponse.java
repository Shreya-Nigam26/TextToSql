package com.retail.texttosql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    
    private String originalQuery;
    private String generatedSQL;
    private List<Map<String, Object>> results;
    private int rowCount;
    private String status;
    private String message;
    
    public static QueryResponse success(String originalQuery, String generatedSQL, List<Map<String, Object>> results) {
        QueryResponse response = new QueryResponse();
        response.setOriginalQuery(originalQuery);
        response.setGeneratedSQL(generatedSQL);
        response.setResults(results);
        response.setRowCount(results != null ? results.size() : 0);
        response.setStatus("success");
        response.setMessage("Query executed successfully");
        return response;
    }
    
    public static QueryResponse error(String originalQuery, String message) {
        QueryResponse response = new QueryResponse();
        response.setOriginalQuery(originalQuery);
        response.setStatus("error");
        response.setMessage(message);
        response.setRowCount(0);
        return response;
    }
}
