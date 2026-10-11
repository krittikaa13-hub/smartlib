package com.smartlib.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health endpoint providing runtime diagnostic info and database connectivity check.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "SmartLib");
        health.put("stage", "Stage 1: Foundation");
        health.put("timestamp", Instant.now().toString());

        Map<String, Object> dbInfo = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            dbInfo.put("status", "UP");
            dbInfo.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            dbInfo.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
            dbInfo.put("catalog", connection.getCatalog());
            health.put("database", dbInfo);
            return ResponseEntity.ok(health);
        } catch (SQLException e) {
            dbInfo.put("status", "DOWN");
            dbInfo.put("error", e.getMessage());
            health.put("status", "DEGRADED");
            health.put("database", dbInfo);
            return ResponseEntity.status(503).body(health);
        }
    }
}
