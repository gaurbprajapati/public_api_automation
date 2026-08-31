package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.companyNameFilter;

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
public class TimesheetCompanyNameFilterTest extends CompanyFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureCompanyFilterTestData();
    }

    @Test(dataProvider = "timesheetCompanyNameFilterData")
    public void timesheetCompanyNameFilterTest(String testId, String filterType, String filterValue,
                                               String filterBarLabel, String expectedResult,
                                               Integer verifyTimesheetId, Integer excludeTimesheetId,
                                               Map<Integer, String> companyIdToName) {
        JSONObject payload = new TimesheetFilterPayloadBuilder()
                .addCompanyName(filterType, filterValue, filterBarLabel)
                .buildForTimesheetListPage();

        Response response = postTimesheetSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Timesheets fetched successfully"));

        JSONArray data = getFilteredData(response);
        validateCompanyNameFilteredData(data, filterType, filterValue, expectedResult, testId,
                verifyTimesheetId, excludeTimesheetId, companyIdToName);
    }

    @DataProvider(name = "timesheetCompanyNameFilterData", parallel = true)
    public Object[][] timesheetCompanyNameFilterDataProvider() {
        ensureCompanyFilterTestData();
        CompanyFilterTestContext ctx = companyFilterContext;
        Map<Integer, String> companyIdToName = buildCompanyIdToNameMap();
        int nonExistentCompanyId = getNonExistentEntityId();

        return new Object[][] {
                {"CN001", "is", buildCompanyFilterValue(ctx.companyA.id), ctx.companyA.name, "NonEmpty",
                        ctx.companyA.timesheetId, null, companyIdToName},
                {"CN002", "is", buildCompanyFilterValue(ctx.companyB.id), ctx.companyB.name, "NonEmpty",
                        ctx.companyB.timesheetId, null, companyIdToName},
                {"CN003", "is", buildCompanyFilterValue(ctx.companyC.id), ctx.companyC.name, "NonEmpty",
                        ctx.companyC.timesheetId, null, companyIdToName},
                {"CN004", "is_not", buildCompanyFilterValue(ctx.companyA.id), ctx.companyA.name, "NonEmpty",
                        null, ctx.companyA.timesheetId, companyIdToName},
                {"CN005", "is_not", buildCompanyFilterValue(ctx.companyB.id), ctx.companyB.name, "NonEmpty",
                        null, ctx.companyB.timesheetId, companyIdToName},
                {"CN006", "is_not", buildCompanyFilterValue(ctx.companyC.id), ctx.companyC.name, "NonEmpty",
                        null, ctx.companyC.timesheetId, companyIdToName},
                {"CN007", "contains_at_least_one",
                        buildCompanyFilterValue(ctx.companyA.id, ctx.companyB.id),
                        buildCompanyFilterBarLabel(ctx.companyA.name, ctx.companyB.name), "NonEmpty",
                        ctx.companyA.timesheetId, null, companyIdToName},
                {"CN008", "contains_at_least_one",
                        buildCompanyFilterValue(ctx.companyB.id, ctx.companyC.id),
                        buildCompanyFilterBarLabel(ctx.companyB.name, ctx.companyC.name), "NonEmpty",
                        ctx.companyB.timesheetId, null, companyIdToName},
                {"CN009", "contains_at_least_one",
                        buildCompanyFilterValue(ctx.companyA.id, ctx.companyB.id, ctx.companyC.id),
                        buildCompanyFilterBarLabel(ctx.companyA.name, ctx.companyB.name, ctx.companyC.name),
                        "NonEmpty", ctx.companyA.timesheetId, null, companyIdToName},
                {"CN010", "does_not_contain", buildCompanyFilterValue(ctx.companyA.id), ctx.companyA.name, "NonEmpty",
                        null, ctx.companyA.timesheetId, companyIdToName},
                {"CN011", "does_not_contain",
                        buildCompanyFilterValue(ctx.companyA.id, ctx.companyB.id),
                        buildCompanyFilterBarLabel(ctx.companyA.name, ctx.companyB.name), "NonEmpty",
                        null, ctx.companyA.timesheetId, companyIdToName},
                {"CN012", "does_not_contain",
                        buildCompanyFilterValue(ctx.companyB.id, ctx.companyC.id),
                        buildCompanyFilterBarLabel(ctx.companyB.name, ctx.companyC.name), "NonEmpty",
                        null, ctx.companyB.timesheetId, companyIdToName},
                {"CN013", "has_any_value", "", "", "NonEmpty", null, null, companyIdToName},
                {"CN014", "is_empty", "", "", "NonEmpty", ctx.orphanedTimesheetId, null, companyIdToName},
                {"CN015", "is", buildCompanyFilterValue(nonExistentCompanyId), "NonExistent", "Empty",
                        null, null, companyIdToName},
                {"CN016", "contains_at_least_one", buildCompanyFilterValue(nonExistentCompanyId),
                        "NonExistent", "Empty", null, null, companyIdToName}
        };
    }

    private void validateCompanyNameFilteredData(JSONArray data, String filterType, String filterValue,
                                                 String expectedResult, String testId,
                                                 Integer verifyTimesheetId, Integer excludeTimesheetId,
                                                 Map<Integer, String> companyIdToName) {
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
                assertThat(testId + ": companyName should exist", isCompanyNameEmpty(timesheet), is(false));
            }
            return;
        }

        if ("is_empty".equals(filterType)) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject timesheet = data.getJSONObject(i);
                assertThat(testId + ": is_empty should return timesheets with empty company",
                        isCompanyNameEmpty(timesheet), is(true));
            }
            return;
        }

        List<Integer> expectedCompanyIds = parseCompanyIds(filterValue);
        Set<String> expectedCompanyNames = companyNamesForIds(expectedCompanyIds, companyIdToName);
        assertThat(testId + ": Filter company IDs should map to company names",
                expectedCompanyNames.isEmpty(), is(false));

        for (int i = 0; i < data.length(); i++) {
            JSONObject timesheet = data.getJSONObject(i);
            if (isCompanyNameEmpty(timesheet) && isExclusionFilterType(filterType)) {
                continue;
            }
            String companyName = resolveCompanyName(timesheet);
            assertThat(testId + ": companyName should exist", companyName, not(emptyOrNullString()));

            switch (filterType) {
                case "is":
                    assertThat(testId + ": Company name should match filter",
                            expectedCompanyNames, hasItem(companyName));
                    break;
                case "is_not":
                    assertThat(testId + ": Company name should not match filter",
                            expectedCompanyNames, not(hasItem(companyName)));
                    break;
                case "contains_at_least_one":
                    assertThat(testId + ": Company name should be in filter list",
                            expectedCompanyNames, hasItem(companyName));
                    break;
                case "does_not_contain":
                    assertThat(testId + ": Company name should not be in filter list",
                            expectedCompanyNames, not(hasItem(companyName)));
                    break;
                default:
                    throw new AssertionError(testId + ": Unsupported filter type: " + filterType);
            }
        }
    }
}
