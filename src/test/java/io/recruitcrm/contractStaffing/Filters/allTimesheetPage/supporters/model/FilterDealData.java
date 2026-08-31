package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

public final class FilterDealData {
    public final int id;
    public final int srno;
    public final String name;
    public final String stageName;
    public final String ownerName;
    public final int ownerUserId;
    public final String slug;
    public final int candidateId;
    public final int timesheetId;

    public FilterDealData(int id, int srno, String name, String stageName, String ownerName, int ownerUserId,
                          String slug, int candidateId, int timesheetId) {
        this.id = id;
        this.srno = srno;
        this.name = name;
        this.stageName = stageName;
        this.ownerName = ownerName;
        this.ownerUserId = ownerUserId;
        this.slug = slug;
        this.candidateId = candidateId;
        this.timesheetId = timesheetId;
    }
}
