package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PeriodFilterTestContext {

    public final LocalDate referenceDate;
    public final FilterPeriodTimesheetData currentPeriod;
    public final FilterPeriodTimesheetData lastWeekPeriod;
    public final FilterPeriodTimesheetData lastMonthPeriod;
    public final FilterPeriodTimesheetData lastQuarterPeriod;
    public final FilterPeriodTimesheetData lastYearPeriod;
    public final FilterPeriodTimesheetData futurePeriod;
    public final FilterPeriodTimesheetData distantPastPeriod;
    public final List<FilterPeriodTimesheetData> allTimesheets;
    public final List<Integer> searchableTimesheetIds;
    public final long equalToPeriodStartEpoch;
    public final long beforeFilterDateEpoch;
    public final long beforePeriodStartBoundaryEpoch;
    public final long afterFilterDateEpoch;
    public final String betweenFilterValue;
    public final String betweenFilterBarLabel;
    public final String exactCurrentPeriodBetweenValue;
    public final String exactCurrentPeriodBetweenBarLabel;

    public PeriodFilterTestContext(LocalDate referenceDate,
                                   FilterPeriodTimesheetData currentPeriod,
                                   FilterPeriodTimesheetData lastWeekPeriod,
                                   FilterPeriodTimesheetData lastMonthPeriod,
                                   FilterPeriodTimesheetData lastQuarterPeriod,
                                   FilterPeriodTimesheetData lastYearPeriod,
                                   FilterPeriodTimesheetData futurePeriod,
                                   FilterPeriodTimesheetData distantPastPeriod,
                                   List<Integer> searchableTimesheetIds,
                                   long equalToPeriodStartEpoch,
                                   long beforeFilterDateEpoch,
                                   long beforePeriodStartBoundaryEpoch,
                                   long afterFilterDateEpoch,
                                   String betweenFilterValue,
                                   String betweenFilterBarLabel,
                                   String exactCurrentPeriodBetweenValue,
                                   String exactCurrentPeriodBetweenBarLabel) {
        this.referenceDate = referenceDate;
        this.currentPeriod = currentPeriod;
        this.lastWeekPeriod = lastWeekPeriod;
        this.lastMonthPeriod = lastMonthPeriod;
        this.lastQuarterPeriod = lastQuarterPeriod;
        this.lastYearPeriod = lastYearPeriod;
        this.futurePeriod = futurePeriod;
        this.distantPastPeriod = distantPastPeriod;
        this.searchableTimesheetIds = List.copyOf(searchableTimesheetIds);
        this.equalToPeriodStartEpoch = equalToPeriodStartEpoch;
        this.beforeFilterDateEpoch = beforeFilterDateEpoch;
        this.beforePeriodStartBoundaryEpoch = beforePeriodStartBoundaryEpoch;
        this.afterFilterDateEpoch = afterFilterDateEpoch;
        this.betweenFilterValue = betweenFilterValue;
        this.betweenFilterBarLabel = betweenFilterBarLabel;
        this.exactCurrentPeriodBetweenValue = exactCurrentPeriodBetweenValue;
        this.exactCurrentPeriodBetweenBarLabel = exactCurrentPeriodBetweenBarLabel;

        Map<String, FilterPeriodTimesheetData> byLabel = new LinkedHashMap<>();
        byLabel.put(currentPeriod.label, currentPeriod);
        byLabel.put(lastWeekPeriod.label, lastWeekPeriod);
        byLabel.put(lastMonthPeriod.label, lastMonthPeriod);
        byLabel.put(lastQuarterPeriod.label, lastQuarterPeriod);
        byLabel.put(lastYearPeriod.label, lastYearPeriod);
        byLabel.put(futurePeriod.label, futurePeriod);
        byLabel.put(distantPastPeriod.label, distantPastPeriod);
        this.allTimesheets = List.copyOf(byLabel.values());
    }
}
