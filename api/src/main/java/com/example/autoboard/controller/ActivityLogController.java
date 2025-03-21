package com.example.autoboard.controller;

import com.example.autoboard.entity.ActivityLog;
import com.example.autoboard.service.ActivityLogService;
import com.google.auth.oauth2.TokenVerifier.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.autoboard.helpers.TokenHelper;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/activity-log")
public class ActivityLogController {

    @Value("${google.client.id}")
    private String clientId;

    private final ActivityLogService activityLogService;

    @Autowired
    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<ActivityLog>> getLogsByTaskId(@PathVariable Long taskId,
            @RequestHeader("Authorization") String token
    ) throws VerificationException {
        // Validate token
        if (!TokenHelper.isValidIdToken(clientId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of()); // Unauthorized
        }
        // Extract user ID
        String userId = TokenHelper.extractUserIdFromToken(token);
        // Get activity logs for a task
        List<ActivityLog> logs = activityLogService.getLogsByTaskId(taskId, userId);
        if (logs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of()); // Not Found
        }
        return ResponseEntity.ok(logs); // Success
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ActivityLog>> getActivityLogsByProjectId(@PathVariable Long projectId,
            @RequestHeader("Authorization") String token
    ) throws VerificationException {
        // Validate token
        if (!TokenHelper.isValidIdToken(clientId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of()); // Unauthorized
        }
        // Extract user ID
        String userId = TokenHelper.extractUserIdFromToken(token);
        // Get activity logs for all tasks within a project
        List<ActivityLog> logs = activityLogService.getActivityLogsByProjectId(projectId, userId);
        if (logs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of()); // Not Found
        }
        return ResponseEntity.ok(logs); // Success
    }

}