package com.example.autoboard.controller;

import com.example.autoboard.repository.TaskRepository;
import com.example.autoboard.entity.Task;
import com.example.autoboard.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;

    @GetMapping("")
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @PostMapping("")
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

}
