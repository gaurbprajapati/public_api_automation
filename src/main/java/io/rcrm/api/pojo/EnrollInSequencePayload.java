package io.rcrm.api.pojo;


public class EnrollInSequencePayload {
    private int id;
    private int start_at_step;
    private int[] enrollments;
    private EnrollmentSteps[] steps;
    private int linked_email_type;

    // Getters and Setters
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

    public int[] getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(int[] enrollments) {
        this.enrollments = enrollments;
    }

    public EnrollmentSteps[] getSteps() {
        return steps;
    }

    public void setSteps(EnrollmentSteps[] steps) {
        this.steps = steps;
    }

    public int getLinked_email_type() {
        return linked_email_type;
    }

    public void setLinked_email_type(int linked_email_type) {
        this.linked_email_type = linked_email_type;
    }
}

