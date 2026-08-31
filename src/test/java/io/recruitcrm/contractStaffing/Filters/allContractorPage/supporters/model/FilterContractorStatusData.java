package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model;

public final class FilterContractorStatusData {
    public final int candidateId;
    public final int expectedStatus;
    public final String statusLabel;

    public FilterContractorStatusData(int candidateId, int expectedStatus, String statusLabel) {
        this.candidateId = candidateId;
        this.expectedStatus = expectedStatus;
        this.statusLabel = statusLabel;
    }
}
