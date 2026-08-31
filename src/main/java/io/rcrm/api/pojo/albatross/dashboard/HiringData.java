package io.rcrm.api.pojo.albatross.dashboard;

public class HiringData {
	private int hiring_stage_one;
	private int hiring_stage_two;
	private int hiring_stage_three;
	private int hiring_stage_four;
	private int hiring_stage_five;
	private int ownerid;
	private int offset;
	private String search_text = "";
	private String jobstatusfiltervalues = "";

	// Getters
	public int getHiring_stage_one() {
		return hiring_stage_one;
	}

	public int getHiring_stage_two() {
		return hiring_stage_two;
	}

	public int getHiring_stage_three() {
		return hiring_stage_three;
	}

	public int getHiring_stage_four() {
		return hiring_stage_four;
	}

	public int getHiring_stage_five() {
		return hiring_stage_five;
	}

	public int getOwnerid() {
		return ownerid;
	}

	public int getOffset() {
		return offset;
	}

	public String getSearch_text() {
		return search_text;
	}

	public String getJobstatusfiltervalues() {
		return jobstatusfiltervalues;
	}

	// Setters
	public void setHiring_stage_one(int hiring_stage_one) {
		this.hiring_stage_one = hiring_stage_one;
	}

	public void setHiring_stage_two(int hiring_stage_two) {
		this.hiring_stage_two = hiring_stage_two;
	}

	public void setHiring_stage_three(int hiring_stage_three) {
		this.hiring_stage_three = hiring_stage_three;
	}

	public void setHiring_stage_four(int hiring_stage_four) {
		this.hiring_stage_four = hiring_stage_four;
	}

	public void setHiring_stage_five(int hiring_stage_five) {
		this.hiring_stage_five = hiring_stage_five;
	}

	public void setOwnerid(int ownerid) {
		this.ownerid = ownerid;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setSearch_text(String search_text) {
		this.search_text = search_text;
	}

	public void setJobstatusfiltervalues(String jobstatusfiltervalues) {
		this.jobstatusfiltervalues = jobstatusfiltervalues;
	}

}