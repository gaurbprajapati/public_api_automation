package io.recruitcrm.report.pojo.SavePerference;

import java.util.ArrayList;

public class Settings {
	
	
	private ArrayList<Integer> recruiter_ids;
	private String kpi_lists;
	private String from_date;
	private String to_date;
	private String date_format;
	private ArrayList<Integer> team_ids;

	
	public Settings(ArrayList<Integer> recruiter_ids, ArrayList<Integer> team_ids, String kpi_lists,
			String from_date, String to_date, String date_format) {
		super();
		this.recruiter_ids = recruiter_ids;
		this.kpi_lists = kpi_lists;
		this.from_date = from_date;
		this.to_date = to_date;
		this.date_format = date_format;
		this.team_ids = team_ids;
	}

	public Settings() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public ArrayList<Integer> getRecruiter_ids() {
		return recruiter_ids;
	}
	public void setRecruiter_ids(ArrayList<Integer> recruiter_ids) {
		this.recruiter_ids = recruiter_ids;
	}
	public String getKpi_lists() {
		return kpi_lists;
	}
	public void setKpi_lists(String kpi_lists) {
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
	
	public String getDate_format() {
		return date_format;
	}

	public void setDate_format(String date_format) {
		this.date_format = date_format;
	}
	public ArrayList<Integer> getTeam_ids() {
		return team_ids;
	}
	public void setTeam_ids(ArrayList<Integer> team_ids) {
		this.team_ids = team_ids;
	}


	

	
}
