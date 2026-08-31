package io.rcrm.api.pojo;

import java.util.ArrayList;

public class AssignMultipleCandToMultipleJobs {

	private ArrayList<Job> jobs;
	private ArrayList<Integer> candidates;
	private boolean assignedcandidates;
	private boolean updateUserObj;

	public AssignMultipleCandToMultipleJobs() {
		super();
	}

	public AssignMultipleCandToMultipleJobs(ArrayList<Job> jobs, ArrayList<Integer> candidates,
			boolean assignedcandidates, boolean updateUserObj) {
		this.jobs = jobs;
		this.candidates = candidates;
		this.assignedcandidates = assignedcandidates;
		this.updateUserObj = updateUserObj;

	}

	public ArrayList<Job> getJobs() {
		return jobs;
	}

	public void setJobs(ArrayList<Job> jobs) {
		this.jobs = jobs;
	}

	public ArrayList<Integer> getCandidates() {
		return candidates;
	}

	public void setCandidates(ArrayList<Integer> candidates) {
		this.candidates = candidates;
	}

	public boolean isAssignedcandidates() {
		return assignedcandidates;
	}

	public void setAssignedcandidates(boolean assignedcandidates) {
		this.assignedcandidates = assignedcandidates;
	}

	public boolean isUpdateUserObj() {
		return updateUserObj;
	}

	public void setUpdateUserObj(boolean updateUserObj) {
		this.updateUserObj = updateUserObj;
	}

}
