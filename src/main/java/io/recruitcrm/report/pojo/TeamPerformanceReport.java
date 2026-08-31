package io.recruitcrm.report.pojo;

import java.util.ArrayList;

public class TeamPerformanceReport {

	public TeamPerformanceReport() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	private ArrayList<Integer> recruiter_ids;
	private ArrayList<Integer> team_ids;
	private ArrayList<Object> kpi_lists;
	private String from_date;
	private String to_date;
	
	
	public ArrayList<Integer> getRecruiter_ids() {
		return recruiter_ids;
	}
	public void setRecruiter_ids(ArrayList<Integer> recruiter_ids) {
		this.recruiter_ids = recruiter_ids;
	}
	public ArrayList<Integer> getTeam_ids() {
		return team_ids;
	}
	public void setTeam_ids(ArrayList<Integer> team_ids) {
		this.team_ids = team_ids;
	}

	
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
