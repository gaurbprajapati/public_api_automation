package io.rcrm.api.pojo.albatross.targetReports;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TargetReport {
	private String title;
	private String assignee_type;
	private String assignee_id;
	private String frequency;
	private long start_date;
	private long end_date;
	private String kpi_list;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int id;

	public void setKpiListObject(KpiList kpiList) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		this.kpi_list = objectMapper.writeValueAsString(kpiList);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class KpiList {
		private List<Recruiter> recruiters;
		private List<String> recruiter_teams;
		private List<String> roles;
		private List<Kpi> kpis;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Recruiter {
		private String id;
		private String name;
		private boolean checked;
		private boolean includeInTarget;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Kpi {
		private String value;
		private String label;
		private boolean checked;
		private boolean includeInTarget;
		private String target;
	}
}