package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;

public class EnrollInSequencePage {

	private int id;
	private int start_at_step;

	private int linked_email_type;
	ArrayList<Integer> enrollments;
	ArrayList<Object> steps;

	public EnrollInSequencePage() {
		super();
		// TODO Auto-generated constructor stub
		linked_email_type = 1;
	}

	public EnrollInSequencePage(int id, int start_at_step, ArrayList<Integer> enrollments, ArrayList<Object> steps) {
		super();
		this.id = id;
		this.start_at_step = start_at_step;
		this.enrollments = enrollments;
		this.steps = steps;
		this.linked_email_type = 1;
	}

	public EnrollInSequencePage(int id, int start_at_step, ArrayList<Integer> enrollments, ArrayList<Object> steps, int linked_email_type) {
		super();
		this.id = id;
		this.start_at_step = start_at_step;
		this.enrollments = enrollments;
		this.steps = steps;
		this.linked_email_type = linked_email_type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStart_at_step() {
		return start_at_step;
	}

	public void setStart_at_step(int start_at_step) {
		this.start_at_step = start_at_step;
	}

	public ArrayList<Integer> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(ArrayList<Integer> enrollments) {
		this.enrollments = enrollments;
	}

	public ArrayList<Object> getSteps() {
		return steps;
	}

	public void setSteps(ArrayList<Object> steps) {
		this.steps = steps;
	}

	public int getLinked_email_type() {
		return linked_email_type;
	}

	public void setLinked_email_type(int linked_email_type) {
		this.linked_email_type = linked_email_type;
	}
}
