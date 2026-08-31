package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class TimesheetFilterTestSupport {

    private TimesheetFilterTestSupport() {
    }

    public static List<Integer> extractTimesheetIds(JSONArray data) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            ids.add(data.getJSONObject(i).optInt("id", -1));
        }
        return ids;
    }

    public static List<Integer> parseBracketedIntList(String filterValue) {
        List<Integer> values = new ArrayList<>();
        if (filterValue == null || filterValue.isEmpty()) {
            return values;
        }
        String cleanValue = filterValue.replaceAll("[\\[\\]]", "");
        if (cleanValue.isEmpty()) {
            return values;
        }
        for (String part : cleanValue.split(",")) {
            try {
                values.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return values;
    }

    public static void assertTimesheetPresent(JSONArray data, Integer timesheetId, String testId) {
        if (timesheetId == null) {
            return;
        }
        assertThat(testId + ": Expected timesheet " + timesheetId + " in filter results",
                extractTimesheetIds(data), hasItem(timesheetId));
    }

    public static void assertTimesheetAbsent(JSONArray data, Integer timesheetId, String testId) {
        if (timesheetId == null) {
            return;
        }
        assertThat(testId + ": Timesheet " + timesheetId + " should be excluded from filter results",
                extractTimesheetIds(data), not(hasItem(timesheetId)));
    }

    public static void assertNoSeededTimesheetsPresent(JSONArray data, List<Integer> seededTimesheetIds,
                                                       String testId) {
        if (seededTimesheetIds == null || seededTimesheetIds.isEmpty()) {
            return;
        }
        List<Integer> resultIds = extractTimesheetIds(data);
        for (Integer seededId : seededTimesheetIds) {
            assertThat(testId + ": Seeded timesheet " + seededId + " should not appear in filter results",
                    resultIds, not(hasItem(seededId)));
        }
    }
}
