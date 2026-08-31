package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.timesheetPeriodFilter;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.*;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.*;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;

import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetPeriodFilterDateUtils.startOfDayEpoch;
import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetPeriodFilterDateUtils.toLocalDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimesheetPeriodFilterTest extends PeriodFilterTestSupport {

    private static final String STRICT_NON_EMPTY = "STRICT_NON_EMPTY";
    private static final String SEEDED_EMPTY = "SEEDED_EMPTY";

    private static final long NON_MATCHING_DATE_EPOCH = 1577836800L;
    private static final String NON_MATCHING_BETWEEN_VALUE =
            "{\"start\":1577836800,\"end\":1578527999}";

    @BeforeClass
    public void setUp() {
        ensurePeriodFilterTestData();
    }

    @Test(dataProvider = "timesheetPeriodFilterData")
    public void timesheetPeriodFilterTest(String testId, String filterType, Object filterValue,
                                          String filterBarLabel, String validationMode) {
        JSONObject payload = new TimesheetFilterPayloadBuilder()
                .addTimesheetPeriod(filterType, filterValue, filterBarLabel)
                .buildForTimesheetListPage();

        Response response = postTimesheetSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Timesheets fetched successfully"));

        JSONArray data = getFilteredData(response);
        validateStrictPeriodFilteredData(data, filterType, filterValue, validationMode, testId);
    }

    @DataProvider(name = "timesheetPeriodFilterData", parallel = true)
    public Object[][] timesheetPeriodFilterDataProvider() {
        ensurePeriodFilterTestData();
        PeriodFilterTestContext ctx = periodFilterContext;

        List<Object[]> rows = new ArrayList<>();

        rows.add(periodFilterRow("TP-IS-001", "is", "all_time", "All Time", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-002", "is", "today", "Today", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-003", "is", "yesterday", "Yesterday", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-004", "is", "this_week", "This Week", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-005", "is", "last_week", "Last Week", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-006", "is", "this_month", "This Month", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-007", "is", "last_month", "Last Month", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-008", "is", "this_quarter", "This Quarter", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-009", "is", "last_quarter", "Last Quarter", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-010", "is", "this_year", "This Year", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-011", "is", "last_year", "Last Year", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-012", "is", "last_30", "Last 30 Days", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-013", "is", "last_60", "Last 60 Days", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-014", "is", "last_90", "Last 90 Days", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IS-015", "is", "last_365", "Last 365 Days", STRICT_NON_EMPTY));

        rows.add(periodFilterRow("TP-IET-001", "is_equal_to", ctx.equalToPeriodStartEpoch,
                "Current Period Start Equal", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IET-002", "is_equal_to", NON_MATCHING_DATE_EPOCH, "Non Matching Date", SEEDED_EMPTY));

        rows.add(periodFilterRow("TP-HAV-001", "has_any_value", "", "", STRICT_NON_EMPTY));

        rows.add(periodFilterRow("TP-IB-001", "is_before", ctx.beforeFilterDateEpoch, "Before Current Window", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IB-002", "is_before", NON_MATCHING_DATE_EPOCH, "Before Very Old Date", SEEDED_EMPTY));
        rows.add(periodFilterRow("TP-IA-001", "is_after", ctx.afterFilterDateEpoch, "After Recent Cutoff", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IA-002", "is_after", ctx.futurePeriod.endDate + 86400L, "After Future Period", SEEDED_EMPTY));

        rows.add(periodFilterRow("TP-IBT-001", "is_between", ctx.betweenFilterValue, ctx.betweenFilterBarLabel, STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IBT-002", "is_between", NON_MATCHING_BETWEEN_VALUE, "Non Matching Range", SEEDED_EMPTY));
        rows.add(periodFilterRow("TP-INBT-001", "is_not_between", ctx.betweenFilterValue, ctx.betweenFilterBarLabel, STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-INBT-002", "is_not_between", NON_MATCHING_BETWEEN_VALUE,
                "Exclude Non Matching Range", STRICT_NON_EMPTY));

        rows.add(periodFilterRow("TP-IMT-001", "is_mt", "30", "30", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IMT-002", "is_mt", "9999", "9999", SEEDED_EMPTY));
        rows.add(periodFilterRow("TP-ILT-001", "is_lt", "7", "7", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-ILT-002", "is_lt", "1", "1", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-IMT-003", "is_mt", "2", "2", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-ILT-003", "is_lt", "2", "2", STRICT_NON_EMPTY));

        rows.add(periodFilterRow("TP-BND-IET-001", "is_equal_to", ctx.equalToPeriodStartEpoch,
                "Period Start Equal", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-BND-IET-002", "is_equal_to",
                startOfDayEpoch(toLocalDate(ctx.currentPeriod.endDate)),
                "Period End Equal", SEEDED_EMPTY));
        rows.add(periodFilterRow("TP-BND-IET-003", "is_equal_to", ctx.currentPeriod.endDate + 1L,
                "After Period End Equal", SEEDED_EMPTY));

        rows.add(periodFilterRow("TP-BND-IB-001", "is_before", ctx.beforePeriodStartBoundaryEpoch,
                "Before Period Start Boundary", STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-BND-IA-001", "is_after", ctx.lastWeekPeriod.startDate,
                "After Last Week Start", STRICT_NON_EMPTY));

        rows.add(periodFilterRow("TP-BND-IBT-001", "is_between", ctx.exactCurrentPeriodBetweenValue,
                ctx.exactCurrentPeriodBetweenBarLabel, STRICT_NON_EMPTY));
        rows.add(periodFilterRow("TP-BND-INBT-001", "is_not_between", ctx.exactCurrentPeriodBetweenValue,
                ctx.exactCurrentPeriodBetweenBarLabel, STRICT_NON_EMPTY));

        rows.add(periodFilterRow("TP-BND-IB-002", "is_before", ctx.distantPastPeriod.endDate + 1L,
                "Before Day After Distant Past End", STRICT_NON_EMPTY));

        rows.add(periodFilterRow("TP-BND-IA-002", "is_after", ctx.futurePeriod.startDate - 1L,
                "After Day Before Future Start", STRICT_NON_EMPTY));

        return rows.toArray(new Object[0][]);
    }
}
