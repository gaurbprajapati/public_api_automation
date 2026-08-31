package io.recruitcrm.contractStaffing.Filters.common;

import java.util.HashMap;
import java.util.Map;

public final class FilterTestConfigFactory {

    private FilterTestConfigFactory() {
    }

    public static Map<String, Object> createDefaultTimesheetConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("dayPattern", "[mon,tue,wed,thu,fri]");
        config.put("regularHours", "9:00-17:00");
        config.put("rulesApplied", "Regular Hours: 1x");
        config.put("payRate", 20.0);
        config.put("billRate", 30.0);
        config.put("breakBillable", "No");
        config.put("jobStartDate", 1751328000L);
        config.put("jobEndDate", 1753920000L);
        config.put("timesheetFrequency", 2);
        config.put("timesheetStartDay", 1);
        config.put("payCurrencyId", 53);
        config.put("billCurrencyId", 53);
        config.put("breakTimeThreshold", 0);
        return config;
    }

    public static Map<String, Object> createTimesheetConfigWithJobDates(long jobStartDate, long jobEndDate) {
        Map<String, Object> config = createDefaultTimesheetConfig();
        config.put("jobStartDate", jobStartDate);
        config.put("jobEndDate", jobEndDate);
        return config;
    }
}
