package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.TelegramUser;
import com.springboot.MyTodoList.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }
    public Optional<Task> findById(Long id){
        return taskRepository.findById(id);
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public Task markTaskAsFinished(Task task){
        task.setStatus("Finished");
        return taskRepository.save(task); // Save the updated task
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }
}
