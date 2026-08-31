package io.recruitcrm.report.pojo;

import java.util.ArrayList;

public class CandidateLifecycleReport {

    public CandidateLifecycleReport(){

    }

	private ArrayList<Object> kpi_lists;
	private String from_date;
	private String to_date;
	
	public ArrayList<Object> getKpi_lists() {
		return kpi_lists;
	}
	public void setKpi_lists(ArrayList<Object> kpi_lists) {
		this.kpi_lists = kpi_lists;
	}
	public String getFrom_date() {
		return from_date;
	}
	public void setFrom_date(String from_date) {
		this.from_date = from_date;
	}
	public String getTo_date() {
		return to_date;
	}
	public void setTo_date(String to_date) {
		this.to_date = to_date;
	}
    
}
