package io.rcrm.api.pojo;

public class enrollInSequence {
	private int sequence_id, enrolled_by;
	 private String prospect_slug;

	public enrollInSequence() {
		super();
		// TODO Auto-generated constructor stub
	}

	public enrollInSequence(int sequence_id, int enrolled_by, String prospect_slug) {
		super();
		this.sequence_id = sequence_id;
		this.enrolled_by = enrolled_by;
		this.prospect_slug = prospect_slug;
	}

	public int getsequence_id() {
		return sequence_id;
	}

	public void setSequence_id(int sequence_id) {
		this.sequence_id = sequence_id;
	}

	public int getenrolled_by() {
		return enrolled_by;
	}

	public void setEnrolled_by(int enrolled_by) {
		this.enrolled_by = enrolled_by;
	}

	public String getprospect_slug() {
		return prospect_slug;
	}

	public void setProspect_slug(String prospect_slug) {
		this.prospect_slug = prospect_slug;
	}

}
