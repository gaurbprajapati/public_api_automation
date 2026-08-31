package io.rcrm.api.pojo.executiveSummary;

public class ExecutiveSearchReport {

	private int job_id;
	private String report_title;
	private int title_template_id;
	private int candidate_profile_template_id;
	private int show_candidates;
	private int show_collaborators;
	private String candidate_field_ids;
	private String selected_candidates_ids;
	private String selected_collaborators;
	private int esr_revamp;

	public int getEsr_revamp() {
		return esr_revamp;
	}

	public void setEsr_revamp(int esr_revamp) {
		this.esr_revamp = esr_revamp;
	}

	private String candidate_fields;
	private String report_content_json;

	public ExecutiveSearchReport() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ExecutiveSearchReport(int job_id, String report_title, int title_template_id,
			int candidate_profile_template_id, int show_candidates, int show_collaborators, String candidate_field_ids,
			String selected_candidates_ids, String selected_collaborators, String candidate_fields,
			String report_content_json) {
		super();
		this.job_id = job_id;
		this.report_title = report_title;
		this.title_template_id = title_template_id;
		this.candidate_profile_template_id = candidate_profile_template_id;
		this.show_candidates = show_candidates;
		this.show_collaborators = show_collaborators;
		this.candidate_field_ids = candidate_field_ids;
		this.selected_candidates_ids = selected_candidates_ids;
		this.selected_collaborators = selected_collaborators;
		this.candidate_fields = candidate_fields;
		this.report_content_json = report_content_json;
	}

	public int getJob_id() {
		return job_id;
	}

	public void setJob_id(int job_id) {
		this.job_id = job_id;
	}

	public String getReport_title() {
		return report_title;
	}

	public void setReport_title(String report_title) {
		this.report_title = report_title;
	}

	public int getTitle_template_id() {
		return title_template_id;
	}

	public void setTitle_template_id(int title_template_id) {
		this.title_template_id = title_template_id;
	}

	public int getShow_candidates() {
		return show_candidates;
	}

	public void setShow_candidates(int show_candidates) {
		this.show_candidates = show_candidates;
	}

	public String getCandidate_field_ids() {
		return candidate_field_ids;
	}

	public void setCandidate_field_ids(String candidate_field_ids) {
		this.candidate_field_ids = candidate_field_ids;
	}

	public int getCandidate_profile_template_id() {
		return candidate_profile_template_id;
	}

	public void setCandidate_profile_template_id(int candidate_profile_template_id) {
		this.candidate_profile_template_id = candidate_profile_template_id;
	}

	public String getSelected_candidates_ids() {
		return selected_candidates_ids;
	}

	public void setSelected_candidates_ids(String selected_candidates_ids) {
		this.selected_candidates_ids = selected_candidates_ids;
	}

	public int getShow_collaborators() {
		return show_collaborators;
	}

	public void setShow_collaborators(int show_collaborators) {
		this.show_collaborators = show_collaborators;
	}

	public String getSelected_collaborators() {
		return selected_collaborators;
	}

	public void setSelected_collaborators(String selected_collaborators) {
		this.selected_collaborators = selected_collaborators;
	}

	public String getCandidate_fields() {
		return candidate_fields;
	}

	public void setCandidate_fields(String candidate_fields) {
		this.candidate_fields = candidate_fields;
	}

	public String getReport_content_json() {
		return report_content_json;
	}

	public void setReport_content_json(String report_content_json) {
		this.report_content_json = report_content_json;
	}

}
