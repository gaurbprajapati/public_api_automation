package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.albatross.offlimit.MarkOffLimit;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ReaperIntegration;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetCompanyOffLimitStatusTest extends TestBase {

    private int entityTypeId = 3; // Company entity type
    private int companyIdAccountA;
    private JavaFakerCompany faker = new JavaFakerCompany();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        // Create test data for Account A
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        // Step 1: Create a company using Account A (Public API)
        Company company = new Company();
        company.setCompany_name(faker.getCompanyName());
        company.setWebsite(faker.getUrl());
        company.setCity(faker.getCity());

        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", accountA_apiKey, null, true, company);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));

        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        assertThat("Company Slug should not be null", companySlug, notNullValue());

        // Get company ID from slug
        companyIdAccountA = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("company", companySlug).getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());

        // Step 2: Create off-limit status for Account A and get status Id
        createOffLimitStatusAccountA();

        // Get off-limit status ID from existing statuses
        Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", accountA_apiKey, null, null, false);
        JsonPath jp = response.jsonPath();
        assertThat("Response code must be 200", response.getStatusCode(), equalTo(200));
        int statusId = jp.getInt("[0].id");

        // Step 3: Mark company as off-limit in Account A
        markCompanyAsOffLimitAccountA(companyIdAccountA, statusId);
    }

    private void createOffLimitStatusAccountA() {
        // Create off-limit status for Account A
        OffLimitStatus.offLimitStatus offLimitStatus = new OffLimitStatus.offLimitStatus();
        offLimitStatus.setStatus_label("Test Off Limit Status Account A");
        offLimitStatus.setStatus_colour_id("A1");
        offLimitStatus.setSequence_no(1);
        offLimitStatus.setAccount_id(String.valueOf(accountA.getAccountId()));
        offLimitStatus.setDefaultStatus("0");
        offLimitStatus.setOfflimit_status_colour_id("A1");
        offLimitStatus.setBackground_color_hex("#FEF2F2");
        offLimitStatus.setText_color_hex("#B04C4C");
        offLimitStatus.setCount(0);

        OffLimitStatus offLimitStatusBody = new OffLimitStatus();
        offLimitStatusBody.setOffLimitStatus(new OffLimitStatus.offLimitStatus[]{offLimitStatus});

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/status", accountA_Token, null, null, true, offLimitStatusBody);

        assertThat("Failed to create off-limit status for Account A", response.getStatusCode(), equalTo(200));
    }

    private void markCompanyAsOffLimitAccountA(int companyId, int statusId) {
        // Mark company as off-limit in Account A
        MarkOffLimit markOffLimit = new MarkOffLimit();
        markOffLimit.setEntity_type_id(entityTypeId);
        markOffLimit.setEntity_ids(new int[]{companyId});
        markOffLimit.setStatus_id(statusId);
        markOffLimit.setStart_date(String.valueOf(System.currentTimeMillis() / 1000));
        markOffLimit.setEnd_date(String.valueOf(System.currentTimeMillis() / 1000 + 86400)); // 1 day later
        markOffLimit.setReason("Test reason for marking company as off-limit in Account A");

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit", accountA_Token, null, null, true, markOffLimit);

        assertThat("Failed to mark company as off-limit in Account A", response.getStatusCode(), equalTo(200));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "crossAccountOffLimitStatusTestData", groups = {"company_service", "nightly-build"})
    public void crossAccountOffLimitStatusOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_OFF_LIMIT_STATUS_CROSS_ACCOUNT":
                    // Account B tries to access Account A's off-limit status
                    response = getOffLimitStatus(entityTypeId, companyIdAccountA, token);
                    break;
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            // Handle expected exceptions
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getOffLimitStatus(int entityTypeId, int companyId, String token) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(companyId));

        return RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", token, null, pathParams, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "cross_account_isolation":
                 if (response.getStatusCode() == 200) {
                     // If 200 is returned, verify it's empty or doesn't contain Account A's data
                     JsonPath isolationJp = response.jsonPath();
                     assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                     assertThat("Meta status should be 200", (Integer) isolationJp.get("meta.status"), equalTo(200));
                     // Cross account isolation - should return empty data or no access to other account's data
                     assertThat("Data should be null", isolationJp.get("data"), nullValue());
                 }
                break;
        }
    }

    @DataProvider(name = "crossAccountOffLimitStatusTestData")
    public static Object[][] crossAccountOffLimitStatusTestData() {
        return new Object[][]{
                // SCENARIO: Cross account isolation - Account B tries to access Account A's off-limit status
                {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_OFF_LIMIT_STATUS_CROSS_ACCOUNT", "200", "cross_account_isolation", "Account B should not access Account A's off-limit status"},
        };
    }
}

