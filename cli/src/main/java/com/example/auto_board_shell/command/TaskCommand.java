package com.example.auto_board_shell.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import com.example.auto_board_shell.service.RequestService;
import com.example.auto_board_shell.service.FormatterService;
import com.example.auto_board_shell.service.APIResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.auto_board_shell.helpers.CurrentUser;

@ShellComponent
public class TaskCommand {

    private final RequestService requestService;
    private final FormatterService formatterService;

    @Autowired
    public TaskCommand(RequestService requestService, FormatterService formatterService) {
        this.requestService = requestService;
        this.formatterService = formatterService;
    }

    @ShellMethod(key = "task-create", value = "Create a new task for a project.")
    public void createTask(
            @ShellOption(value = "--project-id", help = "Project ID") String project_id) {
        try {
            formatterService.printInfo("Creating new task...");

            String title = formatterService.prompt("Enter task title: ");
            String description = formatterService.prompt("Enter task description: ");

            Map<String, Object> assignee = new HashMap<>();
            assignee.put("id", CurrentUser.getId());

            Map<String, Object> status = new HashMap<>();
            status.put("id", "1");

            Map<String, Object> task = new HashMap<>();
            task.put("title", title);
            task.put("description", description);
            task.put("status", status);
            task.put("assignee", assignee);
            task.put("project", project_id);

            APIResponse<Map<String, Object>> response = requestService.post(
                    "/tasks",
                    task,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getData() == null) {
                formatterService.printError("Failed to create task: " + response.getMessage());
                return;
            }

            Object data = response.getData();
            if (data instanceof Map) {
                Map<String, Object> taskResult = (Map<String, Object>) data;
                formatterService.printInfo("ID: " + taskResult.get("id"));
                formatterService.printInfo("Title: " + taskResult.get("title"));
            } else {
                formatterService.printError("Unexpected response type: " + data.getClass().getName());
            }

        } catch (Exception e) {
            formatterService.printError("Error creating task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-update", value = "Update a task.")
    public void updateTask(
            @ShellOption(value = "--id", help = "Task ID") String taskId) {
        try {
            formatterService.printInfo("Updating task...");

            APIResponse<List<Map<String, Object>>> response = requestService.get("/task-status", new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> statuses = response.getData();

            List<String> headers = new ArrayList<>(statuses.get(0).keySet());

            List<List<String>> data = statuses.stream()
                    .map(status -> headers.stream()
                            .map(key -> String.valueOf(status.getOrDefault(key, "N/A")))
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            formatterService.printTable(headers, data);

            String title = formatterService.prompt("Enter task title: ");
            String description = formatterService.prompt("Enter task description: ");
            String statusId = formatterService.prompt("Enter status Id: ");

            Map<String, Object> status = new HashMap<>();
            status.put("id", statusId);

            Map<String, Object> task = new HashMap<>();
            task.put("title", title);
            task.put("description", description);
            task.put("status", statusId);

            APIResponse<Map<String, Object>> taskResponse = requestService.put(
                    "/tasks/" + taskId,
                    task,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (taskResponse.getData() == null) {
                formatterService.printError("Failed to update task: " + taskResponse.getMessage());
                return;
            }

            if (taskResponse.getStatusCode() == 200) {
                formatterService.printSuccess("Update successful!");
            }

        } catch (Exception e) {
            formatterService.printError("Error updating task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-delete", value = "Delete a task.")
    public void deleteTask(
            @ShellOption(value = "--id", help = "Task ID") String taskId) {
        try {
            requestService.delete("/tasks/" + taskId);
            formatterService.printSuccess("Task deleted successfully!");
        } catch (Exception e) {
            formatterService.printError("Error deleting task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-list-all", value = "List all tasks from all projects where the user is a part of.")
    public void listAllTasks() {
        try {
            formatterService.printInfo("Fetching all tasks...");

            APIResponse<List<Map<String, Object>>> response = requestService.get("/tasks", new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> tasks = response.getData();

            if (tasks == null || tasks.isEmpty()) {
                formatterService.printWarning("No tasks found");
                return;
            }

            List<String> selectedHeaders = List.of("id", "title" , "description", "status", "assignee_id", "project_id");

            List<String> headers = new ArrayList<>(selectedHeaders);

            List<List<String>> data = tasks.stream()
                    .map(row -> selectedHeaders.stream()
                            .map(key -> {
                                if (key.equals("assignee_id")) {
                                    Map<String, Object> assignee = (Map<String, Object>) row.get("assignee");
                                    return assignee != null ? String.valueOf(assignee.getOrDefault("id", "N/A")) : "N/A";
                                } else if(key.equals("project_id")) {
                                    Map<String, Object> project = (Map<String, Object>) row.get("project");
                                    return project != null ? String.valueOf(project.getOrDefault("id", "N/A")) : "N/A"; 
                                }
                                else {
                                    return String.valueOf(row.getOrDefault(key, "N/A"));
                                }
                            })
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            formatterService.printTable(headers, data);

        } catch (Exception e) {
            formatterService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-list-assigned", value = "List all tasks assigned to the user.")
    public void listAssignedTasks() {
        try {
            formatterService.printInfo("Fetching assigned tasks...");

            APIResponse<List<Map<String, Object>>> response = requestService.get("/tasks/", new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> tasks = response.getData();

            if (tasks == null || tasks.isEmpty()) {
                formatterService.printWarning("No tasks found");
                return;
            }

            List<String> selectedHeaders = List.of("id", "name", "description", "status", "assignee_id", "project_id");

            List<String> headers = new ArrayList<>(selectedHeaders);

            List<List<String>> data = tasks.stream()
                    .map(row -> selectedHeaders.stream()
                            .map(key -> {
                                if (key.equals("assignee_id")) {
                                    Map<String, Object> assignee = (Map<String, Object>) row.get("assignee");
                                    return assignee != null ? String.valueOf(assignee.getOrDefault("id", "N/A")) : "N/A";
                                } else {
                                    return String.valueOf(row.getOrDefault(key, "N/A"));
                                }
                            })
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            formatterService.printTable(headers, data);

        } catch (Exception e) {
            formatterService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-list-project", value = "List all tasks for a specific project.")
    public void listTasksByProject(
            @ShellOption(value = "--project-id", help = "Project ID") String project_id) {
        try {
            formatterService.printInfo("Fetching tasks for project...");

            APIResponse<List<Map<String, Object>>> response = requestService.get("/tasks/" + project_id, new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> tasks = response.getData();

            if (tasks == null || tasks.isEmpty()) {
                formatterService.printWarning("No tasks found");
                return;
            }

            List<String> selectedHeaders = List.of("id", "name", "description", "status", "assignee_id", "project_id");

            List<String> headers = new ArrayList<>(selectedHeaders);

            List<List<String>> data = tasks.stream()
                    .map(row -> selectedHeaders.stream()
                            .map(key -> {
                                if (key.equals("assignee_id")) {
                                    Map<String, Object> assignee = (Map<String, Object>) row.get("assignee");
                                    return assignee != null ? String.valueOf(assignee.getOrDefault("id", "N/A")) : "N/A";
                                } else {
                                    return String.valueOf(row.getOrDefault(key, "N/A"));
                                }
                            })
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            formatterService.printTable(headers, data);

        } catch (Exception e) {
            formatterService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-assign", value = "Assign a task to a project member.")
    public void assignTask(
            @ShellOption(value = "--task-id", help = "Task ID") String taskId,
            @ShellOption(value = "--assignee-id", help = "Assignee ID") String assigneeId) {
        try {
            formatterService.printInfo("Assigning task...");

            Map<String, Object> assignee = new HashMap<>();
            assignee.put("id", CurrentUser.getId());

            Map<String, Object> task = new HashMap<>();
            task.put("assignee", assignee);

            APIResponse<Map<String, Object>> response = requestService.put(
                    "/tasks/" + taskId + "/assign",
                    task,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            if (response.getData() == null) {
                formatterService.printError("Failed to assign task: " + response.getMessage());
                return;
            }
            if (response.getStatusCode() == 200) {
                formatterService.printSuccess("Task assigned successfully!");
            }
        } catch (Exception e) {
            formatterService.printError("Error assigning task: " + e.getMessage());
        }
    }

}
