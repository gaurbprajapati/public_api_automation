package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model;

import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.FilterJobData;

public final class ContractorJobFilterTestContext {
    public final FilterJobData jobA;
    public final FilterJobData jobB;
    public final FilterJobData jobC;
    public final int availableCandidateId;

    public ContractorJobFilterTestContext(FilterJobData jobA, FilterJobData jobB, FilterJobData jobC,
                                          int availableCandidateId) {
        this.jobA = jobA;
        this.jobB = jobB;
        this.jobC = jobC;
        this.availableCandidateId = availableCandidateId;
    }
}
