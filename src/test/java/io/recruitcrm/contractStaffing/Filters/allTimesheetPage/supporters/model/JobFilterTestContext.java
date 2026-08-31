package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

public final class JobFilterTestContext {
    public final FilterJobData jobA;
    public final FilterJobData jobB;
    public final FilterJobData jobC;
    public final int orphanedTimesheetId;
    public final int orphanedCandidateId;

    public JobFilterTestContext(FilterJobData jobA, FilterJobData jobB, FilterJobData jobC,
                                int orphanedTimesheetId, int orphanedCandidateId) {
        this.jobA = jobA;
        this.jobB = jobB;
        this.jobC = jobC;
        this.orphanedTimesheetId = orphanedTimesheetId;
        this.orphanedCandidateId = orphanedCandidateId;
    }
}
