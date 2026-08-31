package io.recruitcrm.contractStaffing.Filters.allContractorPage.jobNameFilter;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.*;
import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model.*;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.util.*;

import static io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.ContractorFilterTestSupport.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ContractorJobNameFilterTest extends ContractorJobFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureContractorJobFilterTestData();
    }

    @Test(dataProvider = "contractorJobNameFilterData")
    public void contractorJobNameFilterTest(String testId, String filterType, String filterValue,
                                            String filterBarLabel, String expectedResult,
                                            Integer verifyContractorId, Integer excludeContractorId,
                                            Map<Integer, String> jobIdToName) {
        JSONObject payload = new ContractorFilterPayloadBuilder()
                .addJobName(filterType, filterValue, filterBarLabel)
                .build();

        Response response = postContractorSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Contractors fetched successfully"));

        JSONArray data = getFilteredData(response);
        validateJobNameFilteredContractors(data, filterType, filterValue, expectedResult, testId,
                verifyContractorId, excludeContractorId, jobIdToName);
    }

    @DataProvider(name = "contractorJobNameFilterData", parallel = true)
    public Object[][] contractorJobNameFilterDataProvider() {
        ensureContractorJobFilterTestData();
        ContractorJobFilterTestContext ctx = contractorJobFilterContext;
        Map<Integer, String> jobIdToName = buildJobIdToNameMap();
        int nonExistentJobId = getNonExistentEntityId();

        return new Object[][] {
                {"CON-JN001", "is", buildJobFilterValue(ctx.jobA.id), ctx.jobA.name, "NonEmpty",
                        ctx.jobA.candidateId, null, jobIdToName},
                {"CON-JN002", "is", buildJobFilterValue(ctx.jobB.id), ctx.jobB.name, "NonEmpty",
                        ctx.jobB.candidateId, null, jobIdToName},
                {"CON-JN003", "is", buildJobFilterValue(ctx.jobC.id), ctx.jobC.name, "NonEmpty",
                        ctx.jobC.candidateId, null, jobIdToName},
                {"CON-JN004", "is_not", buildJobFilterValue(ctx.jobA.id), ctx.jobA.name, "NonEmpty",
                        null, ctx.jobA.candidateId, jobIdToName},
                {"CON-JN005", "is_not", buildJobFilterValue(ctx.jobB.id), ctx.jobB.name, "NonEmpty",
                        null, ctx.jobB.candidateId, jobIdToName},
                {"CON-JN006", "is_not", buildJobFilterValue(ctx.jobC.id), ctx.jobC.name, "NonEmpty",
                        null, ctx.jobC.candidateId, jobIdToName},
                {"CON-JN007", "contains_at_least_one",
                        buildJobFilterValue(ctx.jobA.id, ctx.jobB.id),
                        buildJobFilterBarLabel(ctx.jobA.name, ctx.jobB.name), "NonEmpty",
                        ctx.jobA.candidateId, null, jobIdToName},
                {"CON-JN008", "contains_at_least_one",
                        buildJobFilterValue(ctx.jobB.id, ctx.jobC.id),
                        buildJobFilterBarLabel(ctx.jobB.name, ctx.jobC.name), "NonEmpty",
                        ctx.jobB.candidateId, null, jobIdToName},
                {"CON-JN009", "contains_at_least_one",
                        buildJobFilterValue(ctx.jobA.id, ctx.jobB.id, ctx.jobC.id),
                        buildJobFilterBarLabel(ctx.jobA.name, ctx.jobB.name, ctx.jobC.name),
                        "NonEmpty", ctx.jobA.candidateId, null, jobIdToName},
                {"CON-JN010", "does_not_contain", buildJobFilterValue(ctx.jobA.id), ctx.jobA.name, "NonEmpty",
                        null, ctx.jobA.candidateId, jobIdToName},
                {"CON-JN011", "does_not_contain",
                        buildJobFilterValue(ctx.jobA.id, ctx.jobB.id),
                        buildJobFilterBarLabel(ctx.jobA.name, ctx.jobB.name), "NonEmpty",
                        null, ctx.jobA.candidateId, jobIdToName},
                {"CON-JN012", "does_not_contain",
                        buildJobFilterValue(ctx.jobB.id, ctx.jobC.id),
                        buildJobFilterBarLabel(ctx.jobB.name, ctx.jobC.name), "NonEmpty",
                        null, ctx.jobB.candidateId, jobIdToName},
                {"CON-JN013", "has_any_value", "", "", "NonEmpty", null, null, jobIdToName},
                {"CON-JN014", "is_empty", "", "", "NonEmpty", ctx.availableCandidateId, ctx.jobA.candidateId, jobIdToName},
                {"CON-JN015", "is", buildJobFilterValue(nonExistentJobId), "NonExistent", "Empty",
                        null, null, jobIdToName},
                {"CON-JN016", "contains_at_least_one", buildJobFilterValue(nonExistentJobId),
                        "NonExistent", "Empty", null, null, jobIdToName}
        };
    }

    private void validateJobNameFilteredContractors(JSONArray data, String filterType, String filterValue,
                                                    String expectedResult, String testId,
                                                    Integer verifyContractorId, Integer excludeContractorId,
                                                    Map<Integer, String> jobIdToName) {
        if ("Empty".equals(expectedResult)) {
            assertThat(testId + ": Should return no data", data.length(), equalTo(0));
            return;
        }

        assertThat(testId + ": Should return contractors", data.length(), greaterThan(0));
        assertContractorPresent(data, verifyContractorId, testId);
        assertContractorAbsent(data, excludeContractorId, testId);

        for (int i = 0; i < data.length(); i++) {
            JSONObject contractor = data.getJSONObject(i);
            if (hasEmptyAssignedJobs(contractor) && isExclusionFilterType(filterType)) {
                continue;
            }
            assertThat(testId + ": Contractor assigned jobs should match filter",
                    contractorAssignedJobsMatchFilter(contractor, filterType, filterValue), is(true));
        }
    }
}
