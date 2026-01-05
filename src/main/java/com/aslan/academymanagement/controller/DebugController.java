package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.service.student.StudentManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final ApplicationContext context;
    private final StudentManagementService studentManagementService;

    @GetMapping("/beans")
    public ResponseEntity<Map<String, Object>> getBeans() {
        Map<String, Object> result = new HashMap<>();

        Map<String, StudentManagementService> beans =
                context.getBeansOfType(StudentManagementService.class);

        result.put("count", beans.size());
        result.put("beans", beans.keySet());
        result.put("currentImplementation", studentManagementService.getClass().getSimpleName());
        result.put("divisionType", studentManagementService.getDivisionType());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/profiles")
    public ResponseEntity<Map<String, Object>> getProfiles() {
        Map<String, Object> result = new HashMap<>();

        result.put("activeProfiles", context.getEnvironment().getActiveProfiles());
        result.put("defaultProfiles", context.getEnvironment().getDefaultProfiles());

        return ResponseEntity.ok(result);
    }
}
