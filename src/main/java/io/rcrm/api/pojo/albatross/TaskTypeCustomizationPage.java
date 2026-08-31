package io.rcrm.api.pojo.albatross;

import java.util.ArrayList;

public class TaskTypeCustomizationPage {

    ArrayList<Object> customizedTaskTypes = new ArrayList<>();

    public TaskTypeCustomizationPage(ArrayList<Object> customizedTaskTypes) {
        super();
        this.customizedTaskTypes = customizedTaskTypes;
    }

    public TaskTypeCustomizationPage() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ArrayList<Object> getCustomizedTaskTypes() {
        return customizedTaskTypes;
    }

    public void setCustomizedTaskTypes(ArrayList<Object> customizedTaskTypes) {
        this.customizedTaskTypes = customizedTaskTypes;
    }

}
