package io.rcrm.api.pojo.albatross.targetReports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TargetReportTable {
	private List<Integer> recruiter_ids;
    private List<KPI> kpi_lists;
    private List<Integer> team_ids;
    private List<Integer> role_ids;
    private long started_on;
    private long ended_on;
    private long from_date;
    private long to_date;
    private String interval;
    private int targetId;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class KPI {
		private String value;
        private String label;
        private boolean checked;
        private boolean includeInTarget;
        private String target;
	}
}