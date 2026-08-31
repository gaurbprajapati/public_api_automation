package io.recruitcrm.contractStaffing.Filters.allContractorPage.statusFilter;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.*;
import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model.*;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.*;

import static io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.ContractorFilterTestSupport.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ContractorStatusFilterTest extends ContractorStatusFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureContractorStatusFilterTestData();
    }

    @Test(dataProvider = "contractorStatusFilterData")
    public void contractorStatusFilterTest(String testId, String filterValue, String filterBarLabel,
                                         Integer verifyContractorId, Integer excludeContractorId,
                                         int expectedStatus) {
        JSONObject payload = new ContractorFilterPayloadBuilder()
                .addStatus(filterValue, filterBarLabel)
                .build();

        Response response = postContractorSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Contractors fetched successfully"));

        JSONArray data = getFilteredData(response);
        assertThat(testId + ": Should return contractors", data.length(), greaterThan(0));
        assertContractorPresent(data, verifyContractorId, testId);
        assertContractorAbsent(data, excludeContractorId, testId);

        for (int i = 0; i < data.length(); i++) {
            JSONObject contractor = data.getJSONObject(i);
            assertThat(testId + ": Contractor status should match filter",
                    resolveContractorStatus(contractor), equalTo(expectedStatus));
        }
    }

    @DataProvider(name = "contractorStatusFilterData", parallel = true)
    public Object[][] contractorStatusFilterDataProvider() {
        ensureContractorStatusFilterTestData();
        ContractorStatusFilterTestContext ctx = contractorStatusFilterContext;

        return new Object[][] {
                {"CON-S001", "1", ctx.assignedContractor.statusLabel,
                        ctx.assignedContractor.candidateId, ctx.availableContractor.candidateId, 1},
                {"CON-S002", "0", ctx.availableContractor.statusLabel,
                        ctx.availableContractor.candidateId, ctx.assignedContractor.candidateId, 0}
        };
    }
}
