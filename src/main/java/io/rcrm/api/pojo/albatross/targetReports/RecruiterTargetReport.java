package io.rcrm.api.pojo.albatross.targetReports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterTargetReport {
	private List<Integer> recruiter_ids;
	private List<Kpi> kpi_lists;
	private long from_date;
	private List<Integer> team_ids;
	private List<Integer> role_ids;
	private boolean company_wide;
	private long to_date;
	private String interval;
	private int targetId;
	private boolean team_selected;
	private boolean teammate_selected;
	private List<Kpi> kpis_included;
	private long started_on;
	private long ended_on;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Kpi {
		private String label;
		private String value;
		private String target;
		private boolean checked;
	}
}