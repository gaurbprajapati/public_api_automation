package io.rcrm.api.pojo.externalJobBoards;

public class ZipRecruiterJobBoard {

	private String response_id;
	private int job_id;
	private String name;
	private String first_name;
	private String last_name;
	private String email;
	private String phone;
	private String resume;
	private ZipRecruiterProfile profile;
	private boolean great_match;

	public ZipRecruiterJobBoard() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getResponse_id() {
		return response_id;
	}

	public void setResponse_id(String response_id) {
		this.response_id = response_id;
	}

	public int getJob_id() {
		return job_id;
	}

	public void setJob_id(int job_id) {
		this.job_id = job_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public ZipRecruiterProfile getProfile() {
		return profile;
	}

	public void setProfile(ZipRecruiterProfile profile) {
		this.profile = profile;
	}

	public boolean isGreat_match() {
		return great_match;
	}

	public void setGreat_match(boolean great_match) {
		this.great_match = great_match;
	}

}
