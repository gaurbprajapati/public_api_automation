package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

public final class FilterPeriodTimesheetData {
    public final String label;
    public final int timesheetId;
    public final long startDate;
    public final long endDate;

    public FilterPeriodTimesheetData(String label, int timesheetId, long startDate, long endDate) {
        this.label = label;
        this.timesheetId = timesheetId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
