package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

public final class CompanyFilterTestContext {
    public final FilterCompanyData companyA;
    public final FilterCompanyData companyB;
    public final FilterCompanyData companyC;
    public final int orphanedTimesheetId;

    public CompanyFilterTestContext(FilterCompanyData companyA, FilterCompanyData companyB,
                                    FilterCompanyData companyC, int orphanedTimesheetId) {
        this.companyA = companyA;
        this.companyB = companyB;
        this.companyC = companyC;
        this.orphanedTimesheetId = orphanedTimesheetId;
    }
}
