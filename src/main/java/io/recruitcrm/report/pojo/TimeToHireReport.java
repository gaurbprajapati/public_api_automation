package io.recruitcrm.report.pojo;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeToHireReport {
    // Default constructor
    public TimeToHireReport() {
    }

    // List of KPIs
    private ArrayList<Object> kpiLists;

    // List of job IDs
    private ArrayList<Integer> jobIds;

    // Getter for jobIds
    public ArrayList<Integer> getJob_ids() {
        return jobIds;
    }

    // Setter for jobIds
    public void setJob_ids(ArrayList<Integer> jobIds) {
        this.jobIds = jobIds;
    }

    // Getter for kpiLists
    public ArrayList<Object> getKpi_lists() {
        return kpiLists;
    }

    // Setter for kpiLists
    public void setKpi_lists(ArrayList<Object> kpiLists) {
        this.kpiLists = kpiLists;
    }
}