package com.app.util;

import com.app.model.RoutineModel;
import com.app.model.TasksModel;
import com.app.model.TimerModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

import static com.app.util.Print.print;

public class ConfigManager {
    private final String operatingSystem = System.getProperty("os.name");
    private final String configFolderPath = getConfigFolderPath();
    private final String configFilePath = getConfigFilePath();
    private static ConfigManager instance;

    public RoutineModel routines;
    public TasksModel tasksModel;
    public TimerModel timerModel;

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();

        return instance;
    }

    private ConfigManager() {
        loadConfigFile();
    }

    private void loadConfigFile() {
        try {
            List<String> configLines = Files.readAllLines(Path.of(configFilePath), StandardCharsets.UTF_8);
            HashMap<String, Object> propData = new HashMap<>();
            String propertyLast = "";

            int linesAmount = configLines.size();
            int linesCounter = 1;

            for (String line: configLines) {
                boolean isProperty = line.startsWith("[") && line.endsWith("]");
                boolean isLastLine = linesCounter == linesAmount;

                if (isProperty) {
                    if (!propData.isEmpty()) {
                        initialisePropClassesOnData(propertyLast, propData);
                        propData.clear();
                    }
                    propertyLast = line.substring(1, line.length() - 1);
                }

                if (!isProperty && !line.isEmpty()) {
                    String[] separatedValues = line.split("=");

                    String prop = separatedValues[0];
                    String[] propValues = separatedValues.length == 2 ? separatedValues[1].split(",") : new String[]{""};

                    propValues = Arrays.stream(propValues)
                            .map(String::trim)
                            .toList()
                            .toArray(new String[0]);

                    propData.put(prop, propValues);

                    if (isLastLine) {
                        initialisePropClassesOnData(propertyLast, propData);
                    }
                }
                linesCounter++;
            }
        } catch (NoSuchFileException e) {
            createConfigFile();
            loadConfigFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void initialisePropClassesOnData(String prop, HashMap<String, Object> propData) {
       if (prop.equals("ROUTINES")) {
            String active = ((String[]) propData.get("ACTIVE_PLAN"))[0];
            HashMap<String, String[]> plans = propData.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals("ACTIVE_PLAN"))
                    .collect(HashMap::new, (hashMap, entry) -> hashMap.put(entry.getKey(), (String[]) entry.getValue()), HashMap::putAll);

            routines = new RoutineModel(active, plans);
        }
        if (prop.equals("TIMER")) {
            String active = ((String[]) propData.get("ACTIVE_PLAN"))[0];
            HashMap<String, String[]> plans = propData.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals("ACTIVE_PLAN"))
                    .collect(HashMap::new, (hashMap, entry) -> hashMap.put(entry.getKey(), (String[]) entry.getValue()), HashMap::putAll);

            timerModel = new TimerModel(active, plans);
        }
        if (prop.equals("TASKS")) {
            String[] tasksData = (String[]) propData.get("tasks");
            tasksModel = new TasksModel(tasksData);
        }
    }

    public void saveConfig() {
        try {
            FileWriter configFile = new FileWriter(configFilePath, false);
            String config = routines.toConfigFormat() + timerModel.toConfigFormat() + tasksModel.toConfigFormat();

            configFile.write(config);
            configFile.close();
        } catch (IOException e) {
            print(e.getMessage());
        }
    }

    private void createConfigFile() {
        try {
            String routinesConfig = "[ROUTINES]\nACTIVE_PLAN=Default\nDefault=Add Activity\n";
            String timerConfig = "[TIMER]\nACTIVE_PLAN=Default\nDefault=1200,600,1200,4\n";
            String tasksConfig = "[TASKS]\ntasks=Add task\n";
            String config = routinesConfig + timerConfig + tasksConfig;

            File directory = new File(configFolderPath);
            directory.mkdir();

            File configFile = new File(configFilePath);
            FileWriter writer = new FileWriter(configFile);

            writer.write(config);
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getConfigFolderPath() {
        String userDir = System.getProperty("user.home");

        return operatingSystem.startsWith("Windows") ? userDir + "/Documents/FocusApp/"
                : operatingSystem.startsWith("Linux") ? userDir + "/.config/FocusApp/"
                : ".";
    }

    private String getConfigFilePath() {
        return getConfigFolderPath() + "app.ini";
    }
}

