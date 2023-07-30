package com.app.model;

import com.app.util.Action;

import java.util.*;

public class RoutineModel {
    private HashMap<String, String[]> plans;
    private String active;

    public RoutineModel(String active, HashMap<String, String[]> plans) {
        this.plans = plans;
        this.active = active;
    }

    public HashMap<String, String[]> getPlans() {
        return plans;
    }
    public String getActive() {
        return active;
    }

    public void setPlans(HashMap<String, String[]> plansUpdated) {
        plans = plansUpdated;
    }

    public void setActive(String activeUpdated) {
        active = activeUpdated;
    }

    public void editPlans(Action action, String plan, String... planRenamed) {
        if (action == Action.ADD) plans.put(plan, new String[]{""});
        if (action == Action.REMOVE) plans.remove(plan);
        if (action == Action.RENAME) {
            plans.put(String.join("", planRenamed), plans.get(plan));
            plans.remove(plan);
        }
    }

    public void editPlan(Action action, String plan, String item) {
        List<String> planList = new ArrayList<>(Arrays.asList(plans.get(plan)));

        if (action == Action.ADD) planList.add(item);
        if (action == Action.REMOVE) planList.remove(item);

        String[] planArray = planList.toArray(new String[0]);

        HashMap<String, String[]> plansUpdated = plans;
        plansUpdated.put(plan, planArray);

        setPlans(plansUpdated);
    }

    public String toConfigFormat() {
        String property = "[ROUTINES]";
        String activePlan = "ACTIVE_PLAN=" + active;
        String plansString = "";
        String routinesConfig = "";

        for (HashMap.Entry<String, String[]> entry: plans.entrySet()) {
            plansString += entry.getKey() + "=" + String.join(", ", entry.getValue()) + "\n";
        }

        routinesConfig = property + "\n" + activePlan + "\n" + plansString;

        return routinesConfig;
    }
}
