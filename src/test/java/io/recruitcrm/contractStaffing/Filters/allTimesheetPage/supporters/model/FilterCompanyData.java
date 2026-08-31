package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

public final class FilterCompanyData {
    public final int id;
    public final int srno;
    public final String name;
    public final String city;
    public final String slug;
    public final int timesheetId;

    public FilterCompanyData(int id, int srno, String name, String city, String slug, int timesheetId) {
        this.id = id;
        this.srno = srno;
        this.name = name;
        this.city = city;
        this.slug = slug;
        this.timesheetId = timesheetId;
    }
}
