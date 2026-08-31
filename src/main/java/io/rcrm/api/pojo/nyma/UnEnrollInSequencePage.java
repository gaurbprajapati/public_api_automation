package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;

public class UnEnrollInSequencePage {

	boolean followup_task;
	ArrayList<Integer> enrollments;

	public UnEnrollInSequencePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UnEnrollInSequencePage(boolean followup_task, ArrayList<Integer> enrollments) {
		super();
		this.followup_task = followup_task;
		this.enrollments = enrollments;
	}

	public boolean isFollowup_task() {
		return followup_task;
	}

	public void setFollowup_task(boolean followup_task) {
		this.followup_task = followup_task;
	}

	public ArrayList<Integer> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(ArrayList<Integer> enrollments) {
		this.enrollments = enrollments;
	}

}
