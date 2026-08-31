package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

public final class FilterJobData {
    public final int id;
    public final int srno;
    public final String name;
    public final String city;
    public final String slug;
    public final int candidateId;
    public final int timesheetId;

    public FilterJobData(int id, int srno, String name, String city, String slug, int candidateId, int timesheetId) {
        this.id = id;
        this.srno = srno;
        this.name = name;
        this.city = city;
        this.slug = slug;
        this.candidateId = candidateId;
        this.timesheetId = timesheetId;
    }
}
