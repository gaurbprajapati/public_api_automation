package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.albatross.offlimit.MarkOffLimit;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetContactOffLimitStatusTest extends TestBase {

    private int entityTypeId = 2; // Contact entity type
    private int contactIdAccountA;
    private JavaFakerContact contactFaker = new JavaFakerContact();
    private JavaFakerCompany companyFaker = new JavaFakerCompany();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        // Create test data for Account A
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        // Step 1: Create a company first (contacts need a company)
        io.rcrm.api.pojo.Company company = new io.rcrm.api.pojo.Company();
        company.setCompany_name(companyFaker.getCompanyName());
        company.setWebsite(companyFaker.getUrl());
        
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", accountA_apiKey, null, true, company);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        assertThat("Company Slug should not be null", companySlug, notNullValue());

        // Step 2: Create a contact using Account A (Public API)
        Contact contact = new Contact(
            contactFaker.getFirstName(),
            contactFaker.getLastName(),
            contactFaker.getEmailID(),
            contactFaker.getContactNumber(),
            companySlug
        );
        
        Response contactResponse = RestClient.doPost("JSON", baseURL, "contacts", accountA_apiKey, null, true, contact);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));

        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");
        assertThat("Contact Slug should not be null", contactSlug, notNullValue());

        // Get contact ID from slug
        contactIdAccountA = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("contact", contactSlug).getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());

        // Step 3: Create off-limit status for Account A and get status Id
        createOffLimitStatusAccountA();

        Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", accountA_apiKey, null, null, false);
        JsonPath jp = response.jsonPath();
        assertThat("Response code must be 200", response.getStatusCode(), equalTo(200));
        int statusId = jp.getInt("[0].id");

        // Step 4: Mark contact as off-limit in Account A
        markContactAsOffLimitAccountA(contactIdAccountA, statusId);
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

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/status",
                accountA_Token, null, null, true, offLimitStatusBody);

        assertThat("Failed to create off-limit status for Account A", response.getStatusCode(), equalTo(200));
    }

    private void markContactAsOffLimitAccountA(int contactId, int statusId) {
        // Mark contact as off-limit in Account A
        MarkOffLimit markOffLimit = new MarkOffLimit();
        markOffLimit.setEntity_type_id(entityTypeId);
        markOffLimit.setEntity_ids(new int[]{contactId});
        markOffLimit.setStatus_id(statusId);
        markOffLimit.setStart_date(String.valueOf(System.currentTimeMillis() / 1000));
        markOffLimit.setEnd_date(String.valueOf(System.currentTimeMillis() / 1000 + 86400)); // 1 day later
        markOffLimit.setReason("Test reason for marking contact as off-limit in Account A");

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit",
                accountA_Token, null, null, true, markOffLimit);

        assertThat("Failed to mark contact as off-limit in Account A", response.getStatusCode(), equalTo(200));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "crossAccountOffLimitStatusTestData", groups = {"contact_service", "nightly-build"})
    public void crossAccountOffLimitStatusOperations_Test(String testScenario, String accountType, String tokenType,
                                                          String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_OFF_LIMIT_STATUS_CROSS_ACCOUNT":
                    // Account B tries to access Account A's off-limit status
                    response = getOffLimitStatus(entityTypeId, contactIdAccountA, token);
                    break;
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            // Handle expected exceptions
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getOffLimitStatus(int entityTypeId, int contactId, String token) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(contactId));

        return RestClient.doGet("JSON", contactServiceURL, "off-limit/entity/{entityTypeId}/status/{id}",
                token, null, pathParams, true);
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
