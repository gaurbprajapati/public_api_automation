package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

public final class DealFilterTestContext {
    public final FilterDealData dealA;
    public final FilterDealData dealB;
    public final FilterDealData dealC;
    public final int orphanedTimesheetId;
    public final int orphanedCandidateId;

    public DealFilterTestContext(FilterDealData dealA, FilterDealData dealB, FilterDealData dealC,
                                 int orphanedTimesheetId, int orphanedCandidateId) {
        this.dealA = dealA;
        this.dealB = dealB;
        this.dealC = dealC;
        this.orphanedTimesheetId = orphanedTimesheetId;
        this.orphanedCandidateId = orphanedCandidateId;
    }
}
