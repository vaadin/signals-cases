package com.example.muc07;

import jakarta.annotation.PostConstruct;

import com.example.usecase18.ChatMessageData;
import com.example.usecase18.Task;
import org.springframework.stereotype.Component;

import com.vaadin.flow.signals.shared.SharedListSignal;

/**
 * Application-scoped signals for MUC07: LLM-Powered Shared Task List
 */
@Component
public class MUC07Signals {

    private final SharedListSignal<Task> llmTasksSignal = new SharedListSignal<>(
            Task.class);
    private final SharedListSignal<ChatMessageData> llmChatMessagesSignal = new SharedListSignal<>(
            ChatMessageData.class);

    public SharedListSignal<Task> getLlmTasksSignal() {
        return llmTasksSignal;
    }

    public SharedListSignal<ChatMessageData> getLlmChatMessagesSignal() {
        return llmChatMessagesSignal;
    }

    @PostConstruct
    public void initializeSampleLLMTasks() {
        if (llmTasksSignal.value().isEmpty()) {
            llmTasksSignal.insertLast(Task
                    .create("Review pull requests",
                            "Review and merge pending pull requests")
                    .withDueDate(java.time.LocalDate.now().plusDays(2)));
            llmTasksSignal.insertLast(Task
                    .create("Write unit tests",
                            "Add unit tests for new features")
                    .withStatus(Task.TaskStatus.IN_PROGRESS)
                    .withDueDate(java.time.LocalDate.now().plusDays(5)));
            llmTasksSignal.insertLast(Task
                    .create("Deploy to staging",
                            "Deploy latest changes to staging environment")
                    .withDueDate(java.time.LocalDate.now().plusDays(7)));
        }
    }
}
