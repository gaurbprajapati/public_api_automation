package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.dealNameFilter;

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
public class TimesheetDealNameFilterTest extends DealFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureDealFilterTestData();
    }

    @Test(dataProvider = "timesheetDealNameFilterData")
    public void timesheetDealNameFilterTest(String testId, String filterType, String filterValue,
                                            String filterBarLabel, String expectedResult,
                                            Integer verifyTimesheetId, Integer excludeTimesheetId,
                                            Map<Integer, String> dealIdToName) {
        JSONObject payload = new TimesheetFilterPayloadBuilder()
                .addDealName(filterType, filterValue, filterBarLabel)
                .buildForTimesheetListPage();

        Response response = postTimesheetSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Timesheets fetched successfully"));

        JSONArray data = getFilteredData(response);
        validateDealNameFilteredData(data, filterType, filterValue, expectedResult, testId,
                verifyTimesheetId, excludeTimesheetId, dealIdToName);
    }

    @DataProvider(name = "timesheetDealNameFilterData", parallel = true)
    public Object[][] timesheetDealNameFilterDataProvider() {
        ensureDealFilterTestData();
        DealFilterTestContext ctx = dealFilterContext;
        Map<Integer, String> dealIdToName = buildDealIdToNameMap();
        int nonExistentDealId = getNonExistentEntityId();

        return new Object[][] {
                {"DN001", "is", buildDealFilterValue(ctx.dealA.id), ctx.dealA.name, "NonEmpty",
                        ctx.dealA.timesheetId, null, dealIdToName},
                {"DN002", "is", buildDealFilterValue(ctx.dealB.id), ctx.dealB.name, "NonEmpty",
                        ctx.dealB.timesheetId, null, dealIdToName},
                {"DN003", "is", buildDealFilterValue(ctx.dealC.id), ctx.dealC.name, "NonEmpty",
                        ctx.dealC.timesheetId, null, dealIdToName},
                {"DN004", "is_not", buildDealFilterValue(ctx.dealA.id), ctx.dealA.name, "NonEmpty",
                        null, ctx.dealA.timesheetId, dealIdToName},
                {"DN005", "is_not", buildDealFilterValue(ctx.dealB.id), ctx.dealB.name, "NonEmpty",
                        null, ctx.dealB.timesheetId, dealIdToName},
                {"DN006", "is_not", buildDealFilterValue(ctx.dealC.id), ctx.dealC.name, "NonEmpty",
                        null, ctx.dealC.timesheetId, dealIdToName},
                {"DN007", "contains_at_least_one",
                        buildDealFilterValue(ctx.dealA.id, ctx.dealB.id),
                        buildDealFilterBarLabel(ctx.dealA.name, ctx.dealB.name), "NonEmpty",
                        ctx.dealA.timesheetId, null, dealIdToName},
                {"DN008", "contains_at_least_one",
                        buildDealFilterValue(ctx.dealB.id, ctx.dealC.id),
                        buildDealFilterBarLabel(ctx.dealB.name, ctx.dealC.name), "NonEmpty",
                        ctx.dealB.timesheetId, null, dealIdToName},
                {"DN009", "contains_at_least_one",
                        buildDealFilterValue(ctx.dealA.id, ctx.dealB.id, ctx.dealC.id),
                        buildDealFilterBarLabel(ctx.dealA.name, ctx.dealB.name, ctx.dealC.name),
                        "NonEmpty", ctx.dealA.timesheetId, null, dealIdToName},
                {"DN010", "does_not_contain", buildDealFilterValue(ctx.dealA.id), ctx.dealA.name, "NonEmpty",
                        null, ctx.dealA.timesheetId, dealIdToName},
                {"DN011", "does_not_contain",
                        buildDealFilterValue(ctx.dealA.id, ctx.dealB.id),
                        buildDealFilterBarLabel(ctx.dealA.name, ctx.dealB.name), "NonEmpty",
                        null, ctx.dealA.timesheetId, dealIdToName},
                {"DN012", "does_not_contain",
                        buildDealFilterValue(ctx.dealB.id, ctx.dealC.id),
                        buildDealFilterBarLabel(ctx.dealB.name, ctx.dealC.name), "NonEmpty",
                        null, ctx.dealB.timesheetId, dealIdToName},
                {"DN013", "has_any_value", "", "", "NonEmpty", null, null, dealIdToName},
                {"DN014", "is_empty", "", "", "NonEmpty", ctx.orphanedTimesheetId, null, dealIdToName},
                {"DN015", "is", buildDealFilterValue(nonExistentDealId), "NonExistent", "Empty",
                        null, null, dealIdToName},
                {"DN016", "contains_at_least_one", buildDealFilterValue(nonExistentDealId),
                        "NonExistent", "Empty", null, null, dealIdToName}
        };
    }

    private void validateDealNameFilteredData(JSONArray data, String filterType, String filterValue,
                                              String expectedResult, String testId,
                                              Integer verifyTimesheetId, Integer excludeTimesheetId,
                                              Map<Integer, String> dealIdToName) {
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
                assertThat(testId + ": dealName should exist", isDealNameEmpty(timesheet), is(false));
            }
            return;
        }

        if ("is_empty".equals(filterType)) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject timesheet = data.getJSONObject(i);
                assertThat(testId + ": is_empty should return timesheets with empty deal name",
                        isDealNameEmpty(timesheet), is(true));
            }
            return;
        }

        List<Integer> expectedDealIds = parseBracketedIntList(filterValue);
        Set<String> expectedDealNames = dealNamesForIds(expectedDealIds, dealIdToName);
        assertThat(testId + ": Filter deal IDs should map to deal names",
                expectedDealNames.isEmpty(), is(false));

        for (int i = 0; i < data.length(); i++) {
            JSONObject timesheet = data.getJSONObject(i);
            if (isDealNameEmpty(timesheet) && isExclusionFilterType(filterType)) {
                continue;
            }
            String dealName = resolveDealName(timesheet);
            assertThat(testId + ": dealName should exist", dealName, not(emptyOrNullString()));

            switch (filterType) {
                case "is":
                    assertThat(testId + ": Deal name should match filter",
                            expectedDealNames, hasItem(dealName));
                    break;
                case "is_not":
                    assertThat(testId + ": Deal name should not match filter",
                            expectedDealNames, not(hasItem(dealName)));
                    break;
                case "contains_at_least_one":
                    assertThat(testId + ": Deal name should be in filter list",
                            expectedDealNames, hasItem(dealName));
                    break;
                case "does_not_contain":
                    assertThat(testId + ": Deal name should not be in filter list",
                            expectedDealNames, not(hasItem(dealName)));
                    break;
                default:
                    throw new AssertionError(testId + ": Unsupported filter type: " + filterType);
            }
        }
    }
}
