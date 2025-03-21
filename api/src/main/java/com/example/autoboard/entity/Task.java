package com.example.autoboard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks") // Specifies the table name
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long task_id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 300)
    private String description;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "task_status_id", nullable = false)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assignee_id", referencedColumnName = "user_id", nullable = true)
    private User assignee;

    public Task() {
    }

    public Task(Long task_id) {
        this.task_id = task_id;
    }

    public Long getId() {
        return task_id;
    }

    public void setId(Long task_id) {
        this.task_id = task_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

}
