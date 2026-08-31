package io.rcrm.api.pojo;

public class AcademicInformation {

	public AcademicInformation() {
		// TODO Auto-generated constructor stub
	}

	private int qualification_id = 4;
	private String specialization = "CSE";

	// Academic Information
	public AcademicInformation(int qualification_id, String specialization) {
		super();
		this.qualification_id = qualification_id;
		this.specialization = specialization;
	}

	public int getqualification_id() {
		return qualification_id;
	}

	public void setQualification_id(int qualification_id) {
		this.qualification_id = qualification_id;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

}
