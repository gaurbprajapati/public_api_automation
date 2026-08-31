package io.recruitcrm.contractStaffing.Filters.allContractorPage.dealNameFilter;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.*;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.*;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.util.*;

import static io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.ContractorFilterTestSupport.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ContractorDealNameFilterTest extends ContractorDealFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureDealFilterTestData();
    }

    @Test(dataProvider = "contractorDealNameFilterData")
    public void contractorDealNameFilterTest(String testId, String filterType, String filterValue,
                                             String filterBarLabel, String expectedResult,
                                             Integer verifyContractorId, Integer excludeContractorId,
                                             Map<Integer, String> dealIdToName) {
        JSONObject payload = new ContractorFilterPayloadBuilder()
                .addDealName(filterType, filterValue, filterBarLabel)
                .build();

        Response response = postContractorSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Contractors fetched successfully"));

        JSONArray data = getFilteredData(response);
        validateDealNameFilteredContractors(data, filterType, filterValue, expectedResult, testId,
                verifyContractorId, excludeContractorId, dealIdToName);
    }

    @DataProvider(name = "contractorDealNameFilterData", parallel = true)
    public Object[][] contractorDealNameFilterDataProvider() {
        ensureDealFilterTestData();
        DealFilterTestContext ctx = dealFilterContext;
        Map<Integer, String> dealIdToName = buildDealIdToNameMap();
        int nonExistentDealId = getNonExistentEntityId();

        return new Object[][] {
                {"CON-DN001", "is", buildDealFilterValue(ctx.dealA.id), ctx.dealA.name, "NonEmpty",
                        ctx.dealA.candidateId, null, dealIdToName},
                {"CON-DN002", "is", buildDealFilterValue(ctx.dealB.id), ctx.dealB.name, "NonEmpty",
                        ctx.dealB.candidateId, null, dealIdToName},
                {"CON-DN003", "is", buildDealFilterValue(ctx.dealC.id), ctx.dealC.name, "NonEmpty",
                        ctx.dealC.candidateId, null, dealIdToName},
                {"CON-DN004", "is_not", buildDealFilterValue(ctx.dealA.id), ctx.dealA.name, "NonEmpty",
                        null, ctx.dealA.candidateId, dealIdToName},
                {"CON-DN005", "is_not", buildDealFilterValue(ctx.dealB.id), ctx.dealB.name, "NonEmpty",
                        null, ctx.dealB.candidateId, dealIdToName},
                {"CON-DN006", "is_not", buildDealFilterValue(ctx.dealC.id), ctx.dealC.name, "NonEmpty",
                        null, ctx.dealC.candidateId, dealIdToName},
                {"CON-DN007", "contains_at_least_one",
                        buildDealFilterValue(ctx.dealA.id, ctx.dealB.id),
                        buildDealFilterBarLabel(ctx.dealA.name, ctx.dealB.name), "NonEmpty",
                        ctx.dealA.candidateId, null, dealIdToName},
                {"CON-DN008", "contains_at_least_one",
                        buildDealFilterValue(ctx.dealB.id, ctx.dealC.id),
                        buildDealFilterBarLabel(ctx.dealB.name, ctx.dealC.name), "NonEmpty",
                        ctx.dealB.candidateId, null, dealIdToName},
                {"CON-DN009", "contains_at_least_one",
                        buildDealFilterValue(ctx.dealA.id, ctx.dealB.id, ctx.dealC.id),
                        buildDealFilterBarLabel(ctx.dealA.name, ctx.dealB.name, ctx.dealC.name),
                        "NonEmpty", ctx.dealA.candidateId, null, dealIdToName},
                {"CON-DN010", "does_not_contain", buildDealFilterValue(ctx.dealA.id), ctx.dealA.name, "NonEmpty",
                        null, ctx.dealA.candidateId, dealIdToName},
                {"CON-DN011", "does_not_contain",
                        buildDealFilterValue(ctx.dealA.id, ctx.dealB.id),
                        buildDealFilterBarLabel(ctx.dealA.name, ctx.dealB.name), "NonEmpty",
                        null, ctx.dealA.candidateId, dealIdToName},
                {"CON-DN012", "does_not_contain",
                        buildDealFilterValue(ctx.dealB.id, ctx.dealC.id),
                        buildDealFilterBarLabel(ctx.dealB.name, ctx.dealC.name), "NonEmpty",
                        null, ctx.dealB.candidateId, dealIdToName},
                {"CON-DN013", "has_any_value", "", "", "NonEmpty", null, null, dealIdToName},
                {"CON-DN014", "is_empty", "", "", "NonEmpty", ctx.orphanedCandidateId, null, dealIdToName},
                {"CON-DN015", "is", buildDealFilterValue(nonExistentDealId), "NonExistent", "Empty",
                        null, null, dealIdToName},
                {"CON-DN016", "contains_at_least_one", buildDealFilterValue(nonExistentDealId),
                        "NonExistent", "Empty", null, null, dealIdToName}
        };
    }

    private void validateDealNameFilteredContractors(JSONArray data, String filterType, String filterValue,
                                                     String expectedResult, String testId,
                                                     Integer verifyContractorId, Integer excludeContractorId,
                                                     Map<Integer, String> dealIdToName) {
        if ("Empty".equals(expectedResult)) {
            assertThat(testId + ": Should return no data", data.length(), equalTo(0));
            return;
        }

        assertThat(testId + ": Should return contractors", data.length(), greaterThan(0));
        assertContractorPresent(data, verifyContractorId, testId);
        assertContractorAbsent(data, excludeContractorId, testId);

        for (int i = 0; i < data.length(); i++) {
            JSONObject contractor = data.getJSONObject(i);
            if (hasEmptyDeals(contractor) && isExclusionFilterType(filterType)) {
                continue;
            }
            assertThat(testId + ": Contractor deals should match filter",
                    contractorDealsMatchFilter(contractor, filterType, filterValue), is(true));
        }
    }
}
