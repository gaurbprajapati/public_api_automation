package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.jobNameFilter;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.*;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.*;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.util.*;

import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetFilterTestSupport.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimesheetJobNameFilterTest extends JobFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureJobFilterTestData();
    }

    @Test(dataProvider = "timesheetJobNameFilterData")
    public void timesheetJobNameFilterTest(String testId, String filterType, String filterValue,
                                           String filterBarLabel, String expectedResult,
                                           Integer verifyTimesheetId, Integer excludeTimesheetId,
                                           Map<Integer, String> jobIdToName) {
        JSONObject payload = new TimesheetFilterPayloadBuilder()
                .addJobName(filterType, filterValue, filterBarLabel)
                .buildForTimesheetListPage();

        Response response = postTimesheetSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Timesheets fetched successfully"));

        JSONArray data = getFilteredData(response);
        validateJobNameFilteredData(data, filterType, filterValue, expectedResult, testId,
                verifyTimesheetId, excludeTimesheetId, jobIdToName);
    }

    @DataProvider(name = "timesheetJobNameFilterData", parallel = true)
    public Object[][] timesheetJobNameFilterDataProvider() {
        ensureJobFilterTestData();
        JobFilterTestContext ctx = jobFilterContext;
        Map<Integer, String> jobIdToName = buildJobIdToNameMap();
        int nonExistentJobId = getNonExistentEntityId();

        return new Object[][] {
                {"JN001", "is", buildJobFilterValue(ctx.jobA.id), ctx.jobA.name, "NonEmpty",
                        ctx.jobA.timesheetId, null, jobIdToName},
                {"JN002", "is", buildJobFilterValue(ctx.jobB.id), ctx.jobB.name, "NonEmpty",
                        ctx.jobB.timesheetId, null, jobIdToName},
                {"JN003", "is", buildJobFilterValue(ctx.jobC.id), ctx.jobC.name, "NonEmpty",
                        ctx.jobC.timesheetId, null, jobIdToName},
                {"JN004", "is_not", buildJobFilterValue(ctx.jobA.id), ctx.jobA.name, "NonEmpty",
                        null, ctx.jobA.timesheetId, jobIdToName},
                {"JN005", "is_not", buildJobFilterValue(ctx.jobB.id), ctx.jobB.name, "NonEmpty",
                        null, ctx.jobB.timesheetId, jobIdToName},
                {"JN006", "is_not", buildJobFilterValue(ctx.jobC.id), ctx.jobC.name, "NonEmpty",
                        null, ctx.jobC.timesheetId, jobIdToName},
                {"JN007", "contains_at_least_one",
                        buildJobFilterValue(ctx.jobA.id, ctx.jobB.id),
                        buildJobFilterBarLabel(ctx.jobA.name, ctx.jobB.name), "NonEmpty",
                        ctx.jobA.timesheetId, null, jobIdToName},
                {"JN008", "contains_at_least_one",
                        buildJobFilterValue(ctx.jobB.id, ctx.jobC.id),
                        buildJobFilterBarLabel(ctx.jobB.name, ctx.jobC.name), "NonEmpty",
                        ctx.jobB.timesheetId, null, jobIdToName},
                {"JN009", "contains_at_least_one",
                        buildJobFilterValue(ctx.jobA.id, ctx.jobB.id, ctx.jobC.id),
                        buildJobFilterBarLabel(ctx.jobA.name, ctx.jobB.name, ctx.jobC.name),
                        "NonEmpty", ctx.jobA.timesheetId, null, jobIdToName},
                {"JN010", "does_not_contain", buildJobFilterValue(ctx.jobA.id), ctx.jobA.name, "NonEmpty",
                        null, ctx.jobA.timesheetId, jobIdToName},
                {"JN011", "does_not_contain",
                        buildJobFilterValue(ctx.jobA.id, ctx.jobB.id),
                        buildJobFilterBarLabel(ctx.jobA.name, ctx.jobB.name), "NonEmpty",
                        null, ctx.jobA.timesheetId, jobIdToName},
                {"JN012", "does_not_contain",
                        buildJobFilterValue(ctx.jobB.id, ctx.jobC.id),
                        buildJobFilterBarLabel(ctx.jobB.name, ctx.jobC.name), "NonEmpty",
                        null, ctx.jobB.timesheetId, jobIdToName},
                {"JN013", "has_any_value", "", "", "NonEmpty", null, null, jobIdToName},
                {"JN014", "is_empty", "", "", "NonEmpty", ctx.orphanedTimesheetId, null, jobIdToName},
                {"JN015", "is", buildJobFilterValue(nonExistentJobId), "NonExistent", "Empty",
                        null, null, jobIdToName},
                {"JN016", "contains_at_least_one", buildJobFilterValue(nonExistentJobId),
                        "NonExistent", "Empty", null, null, jobIdToName}
        };
    }

    private void validateJobNameFilteredData(JSONArray data, String filterType, String filterValue,
                                             String expectedResult, String testId,
                                             Integer verifyTimesheetId, Integer excludeTimesheetId,
                                             Map<Integer, String> jobIdToName) {
        if ("Empty".equals(expectedResult)) {
            assertThat(testId + ": Should return no data", data.length(), equalTo(0));
            return;
        }

        assertThat(testId + ": Should return timesheets", data.length(), greaterThan(0));
        assertTimesheetPresent(data, verifyTimesheetId, testId);
        assertTimesheetAbsent(data, excludeTimesheetId, testId);

        if ("has_any_value".equals(filterType)) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject timesheet = data.getJSONObject(i);
                assertThat(testId + ": jobName should exist", isJobNameEmpty(timesheet), is(false));
            }
            return;
        }

        if ("is_empty".equals(filterType)) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject timesheet = data.getJSONObject(i);
                assertThat(testId + ": is_empty should return timesheets with empty job name",
                        isJobNameEmpty(timesheet), is(true));
            }
            return;
        }

        List<Integer> expectedJobIds = parseBracketedIntList(filterValue);
        Set<String> expectedJobNames = jobNamesForIds(expectedJobIds, jobIdToName);
        assertThat(testId + ": Filter job IDs should map to job names",
                expectedJobNames.isEmpty(), is(false));

        for (int i = 0; i < data.length(); i++) {
            JSONObject timesheet = data.getJSONObject(i);
            if (isJobNameEmpty(timesheet) && isExclusionFilterType(filterType)) {
                continue;
            }
            String jobName = resolveJobName(timesheet);
            assertThat(testId + ": jobName should exist", jobName, not(emptyOrNullString()));

            switch (filterType) {
                case "is":
                    assertThat(testId + ": Job name should match filter",
                            expectedJobNames, hasItem(jobName));
                    break;
                case "is_not":
                    assertThat(testId + ": Job name should not match filter",
                            expectedJobNames, not(hasItem(jobName)));
                    break;
                case "contains_at_least_one":
                    assertThat(testId + ": Job name should be in filter list",
                            expectedJobNames, hasItem(jobName));
                    break;
                case "does_not_contain":
                    assertThat(testId + ": Job name should not be in filter list",
                            expectedJobNames, not(hasItem(jobName)));
                    break;
                default:
                    throw new AssertionError(testId + ": Unsupported filter type: " + filterType);
            }
        }
    }
}
