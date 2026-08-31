package io.rcrm.api.pojo.externalJobBoards;

public class ZipRecruiterProfile {
	
	private String executive_summary;
	private String mobile;
	private ZipRecruiterJobRecord job_records[];
	private String text_resume;
	
	public ZipRecruiterProfile() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getExecutive_summary() {
		return executive_summary;
	}
	public void setExecutive_summary(String executive_summary) {
		this.executive_summary = executive_summary;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public ZipRecruiterJobRecord[] getJob_records() {
		return job_records;
	}
	public void setJob_records(ZipRecruiterJobRecord[] job_records) {
		this.job_records = job_records;
	}
	public String getText_resume() {
		return text_resume;
	}
	public void setText_resume(String text_resume) {
		this.text_resume = text_resume;
	}

}
