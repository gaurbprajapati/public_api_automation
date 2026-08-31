package io.rcrm.api.pojo.albatross;

import java.util.List;

public class AssignedCandInJob {

	private List<Integer> jobId;
	private List<Integer> candidates;

	public AssignedCandInJob() {
		super();
	}

	public AssignedCandInJob(List<Integer> jobId, List<Integer> candidates) {
		this.jobId = jobId;
		this.candidates = candidates;
	}

	public List<Integer> getJobId() {
		return jobId;
	}

	public void setJobId(List<Integer> jobId) {
		this.jobId = jobId;
	}

	public List<Integer> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<Integer> candidates) {
		this.candidates = candidates;
	}

}
