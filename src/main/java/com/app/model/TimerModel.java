package com.app.model;

import com.app.util.Action;

import java.util.HashMap;

public class TimerModel {
    private HashMap<String, String[]> plans;
    private String active;

    public TimerModel(String active, HashMap<String, String[]> plans) {
        this.plans = plans;
        this.active = active;
    }

    public HashMap<String, String[]> getPlans() {
        return plans;
    }
    public String getActive() {
        return active;
    }

    public void setActive(String activeUpdated) {
        active = activeUpdated;
    }

    @SafeVarargs
    public final void editPlans(Action action, String plan, HashMap<String, String>... planDataOptional) {
        if (action == Action.ADD) {
            if (planDataOptional.length == 0) return;

            HashMap<String, String> planData = planDataOptional[0];
            plans.put(plan, new String[]{
                    planData.get("focus_time"),
                    planData.get("short_break"),
                    planData.get("long_break"),
                    planData.get("cycles")
            });
        }
        if (action == Action.REMOVE) plans.remove(plan);
    }

    public void editPlan(String plan, HashMap<String, String> planData) {
        String title = planData.get("title");
        String shortBreak = planData.get("short_break");
        String longBreak = planData.get("long_break");
        String focusTime = planData.get("focus_time");
        String cycles = planData.get("cycles");

        String[] planDataArray = new String[]{focusTime, shortBreak, longBreak, cycles};

        plans.remove(plan);
        plans.put(title, planDataArray);
    }

    public String toConfigFormat() {
        String property = "[TIMER]";
        String activePlan = "ACTIVE_PLAN=" + active;
        String plansString = "";
        String timerConfig = "";

        for (HashMap.Entry<String, String[]> entry: plans.entrySet()) {
            plansString += entry.getKey() + "=" + String.join(", ", entry.getValue()) + "\n";
        }

        timerConfig = property + "\n" + activePlan + "\n" + plansString;

        return timerConfig;
    }
}
