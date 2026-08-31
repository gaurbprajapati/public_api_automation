package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model;

public final class ContractorStatusFilterTestContext {
    public final FilterContractorStatusData assignedContractor;
    public final FilterContractorStatusData availableContractor;

    public ContractorStatusFilterTestContext(FilterContractorStatusData assignedContractor,
                                             FilterContractorStatusData availableContractor) {
        this.assignedContractor = assignedContractor;
        this.availableContractor = availableContractor;
    }
}
