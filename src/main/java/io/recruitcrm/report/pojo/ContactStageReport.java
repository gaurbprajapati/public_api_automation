package io.recruitcrm.report.pojo;

import java.util.List;

public class ContactStageReport {
	private List<Integer> recruiter_ids;
	private KpiLists[] kpi_lists;
	private String from_date;
	private int[] team_ids;
	private String to_date;

	// Getters and setters
	public List<Integer> getRecruiter_ids() {
		return recruiter_ids;
	}

	public void setRecruiter_ids(List<Integer> ids) {
		this.recruiter_ids = ids;
	}

	public KpiLists[] getKpi_lists() {
		return kpi_lists;
	}

	public void setKpi_lists(KpiLists[] kpi_lists) {
		this.kpi_lists = kpi_lists;
	}

	public String getFrom_date() {
		return from_date;
	}

	public void setFrom_date(String from_date) {
		this.from_date = from_date;
	}

	public int[] getTeam_ids() {
		return team_ids;
	}

	public void setTeam_ids(int[] team_ids) {
		this.team_ids = team_ids;
	}

	public String getTo_date() {
		return to_date;
	}

	public void setTo_date(String to_date) {
		this.to_date = to_date;
	}
}
