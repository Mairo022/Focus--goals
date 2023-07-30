package com.app.model;

import com.app.util.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TasksModel {
    private String[] tasks;

    public TasksModel(String[] tasks) {
        this.tasks = tasks;
    }

    public String[] getTasks() {
        return tasks;
    }

    public void setTasks(String[] tasksUpdated) {
        tasks = tasksUpdated;
    }

    public void editTasks(Action action, String item) {
        List<String> tasksList = new ArrayList<>(Arrays.asList(tasks));

        if (action == Action.ADD) tasksList.add(item);
        if (action == Action.REMOVE) tasksList.remove(item);

        String[] tasksArray = tasksList.toArray(new String[0]);

        setTasks(tasksArray);
    }

    public String toConfigFormat() {
        String property = "[TASKS]";
        String tasksString = "tasks=" + String.join(", ", tasks);
        String tasksConfig = property + "\n" + tasksString;

        return tasksConfig;
    }
}
