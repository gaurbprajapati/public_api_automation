package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.albatross.offlimit.MarkOffLimit;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|automationForRevamp")
public class GetCompanyOffLimitStatusTest extends TestBase {

    // Schema paths
    private static final String SCHEMA_SUCCESS = "schemaValidation/privateApi/company/offLimitStatus.json";
    private static final String SCHEMA_UNAUTHORIZED = "schemaValidation/privateApi/company/offLimitStatusUnauthorized.json";
    private static final String SCHEMA_NULL_DATA = "schemaValidation/privateApi/company/offLimitStatusNullData.json";
    private static final String SCHEMA_BAD_REQUEST = "schemaValidation/privateApi/company/offLimitStatusBadRequest.json";
    private static final String SCHEMA_NOT_FOUND = "schemaValidation/privateApi/company/offLimitStatusNotFound.json";

    private String accountAPIKey;
    private String albatrossTkn;
    private int entityTypeId = 3; // Company entity type
    private int companyId;
    private String companySlug;
    private int offLimitStatusId;
    private JavaFakerCompany faker = new JavaFakerCompany();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        accountAPIKey = ThreadManager.getAccountApiKey();
        
        // Create test data
        createTestData();
    }
    
    private void createTestData() {
        // Step 1: Create a company using Public API
        Company company = new Company();
        company.setCompany_name(faker.getCompanyName());
        company.setWebsite(faker.getUrl());
        company.setCity(faker.getCity());
        
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", accountAPIKey, null, true, company);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        
        JsonPath companyJp = companyResponse.jsonPath();
        companySlug = companyJp.get("slug");
        assertThat("Company Slug should not be null", companySlug, notNullValue());

        companyId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("company", companySlug).getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        
        // Step 2: Create off-limit status and get status ID
        offLimitStatusId = createOffLimitStatusAndGetId();

        // Step 3: Mark company as off-limit
        int companyId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("company", companySlug).getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        
        // Step 4: Mark company as off-limit
        markCompanyAsOffLimit(companyId, offLimitStatusId);
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusSuccess() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(companyId));

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", albatrossTkn, null, pathParams, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("OffLimitStatus fetched successfully."));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        assertThat("Entity ID should match company ID", (Integer) jp.get("data.entityId"), equalTo(companyId));
        assertThat("Entity Type should be 3 (company)", (Integer) jp.get("data.entityType"), equalTo(entityTypeId));
        assertThat("Account ID should not be null", jp.get("data.accountId"), notNullValue());
        assertThat("Off Limit Status ID should not be null", jp.get("data.offLimitStatusId"), notNullValue());
        assertThat("Off Limit Status ID should match expected value", (Integer) jp.get("data.offLimitStatusId"), equalTo(offLimitStatusId));
        assertThat("Off Limit Reason should not be null", jp.get("data.offLimitReason"), notNullValue());
        assertThat("Created On should not be null", jp.get("data.createdOn"), notNullValue());
        assertThat("Created By should not be null", jp.get("data.createdBy"), notNullValue());
        assertThat("Marked By Name should not be null", jp.get("data.markedByName"), notNullValue());
        assertThat("Marked By Email should not be null", jp.get("data.markedByEmail"), notNullValue());
        assertThat("Status Label should not be null", jp.get("data.statusLabel"), notNullValue());
        assertThat("Background Color Hex should not be null", jp.get("data.backgroundColorHex"), notNullValue());
        assertThat("Text Color Hex should not be null", jp.get("data.textColorHex"), notNullValue());

        // JSON Schema Validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_SUCCESS));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusWithoutAuth() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(companyId));

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
                null, null, pathParams, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());

    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusInvalidAuth() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(companyId));

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", albatrossTkn + "invalid_token", null, pathParams, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusInvalidEntityType() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", "999"); // Invalid entity type
        pathParams.put("id", String.valueOf(companyId));

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", albatrossTkn, null, pathParams, true);

        // Response code is 200 for invalid entity Id but the data is null
        JsonPath jp = response.jsonPath();
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Data should be null", jp.get("data"), nullValue());

        // JSON Schema Validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_NULL_DATA));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusInvalidCompanyId() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", companyId + "999"); // Invalid company ID

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", albatrossTkn, null, pathParams, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        // JSON Schema Validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_BAD_REQUEST));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusMissingEntityTypeId() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", ""); // Missing/empty entityTypeId
        pathParams.put("id", String.valueOf(companyId));

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", albatrossTkn, null, pathParams, true);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        // JSON Schema Validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_NOT_FOUND));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusCompanyMissingId() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", ""); // Missing/empty id

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", albatrossTkn, null, pathParams, true);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        // JSON Schema Validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_NOT_FOUND));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetOffLimitStatusMissingBothParameters() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", ""); // Missing/empty entityTypeId
        pathParams.put("id", ""); // Missing/empty id

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", albatrossTkn, null, pathParams, true);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        // JSON Schema Validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_NOT_FOUND));
    }
    
    private int createOffLimitStatusAndGetId() {
        // Create off-limit status
        OffLimitStatus.offLimitStatus offLimitStatus = new OffLimitStatus.offLimitStatus();
        offLimitStatus.setStatus_label("Test Off Limit Status");
        offLimitStatus.setStatus_colour_id("A1");
        offLimitStatus.setSequence_no(1);
        offLimitStatus.setAccount_id(String.valueOf(ThreadManager.getAccount().getAccountId()));
        offLimitStatus.setDefaultStatus("0");
        offLimitStatus.setOfflimit_status_colour_id("A1");
        offLimitStatus.setBackground_color_hex("#FEF2F2");
        offLimitStatus.setText_color_hex("#B04C4C");
        offLimitStatus.setCount(0);

        OffLimitStatus offLimitStatusBody = new OffLimitStatus();
        offLimitStatusBody.setOffLimitStatus(new OffLimitStatus.offLimitStatus[] {offLimitStatus});

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/status", albatrossTkn, null, null, true, offLimitStatusBody);
        
        assertThat("Failed to create off-limit status", response.getStatusCode(), equalTo(200));

        Response getStatusResponse = RestClient.doGet("JSON", albatrossURL,"off-limit/status", albatrossTkn, null, null, true);

        assertThat("Failed to fetch off-limit statuses", getStatusResponse.getStatusCode(), equalTo(200));
        JsonPath statusJson = getStatusResponse.jsonPath();

        // Parse the response to find the status with the correct label from "offLimitStatus" array
        List<Map<String, Object>> statuses = statusJson.getList("data.offLimitStatus");
        assertThat("Off-limit statuses list should not be empty", statuses, notNullValue());
        Integer statusId = null;
        for (Map<String, Object> status : statuses) {
            if ("Test Off Limit Status".equals(status.get("status_label"))) {
                // The new API returns 'id' field instead of 'status_id'
                Object idObj = status.get("id");
                if (idObj instanceof Integer) {
                    statusId = (Integer) idObj;
                } else if (idObj instanceof Number) {
                    statusId = ((Number) idObj).intValue();
                }
                break;
            }
        }
        assertThat("Off-limit status with label 'Test Off Limit Status' not found", statusId, notNullValue());
        return statusId;
    }

    private void markCompanyAsOffLimit(int companyId, int statusId) {
        // Mark company as off-limit
        MarkOffLimit markOffLimit = new MarkOffLimit();
        markOffLimit.setEntity_type_id(entityTypeId);
        markOffLimit.setEntity_ids(new int[]{companyId});
        markOffLimit.setStatus_id(statusId);
        markOffLimit.setStart_date(String.valueOf(System.currentTimeMillis() / 1000));
        markOffLimit.setEnd_date(String.valueOf(System.currentTimeMillis() / 1000 + 86400)); // 1 day later
        markOffLimit.setReason("Test reason for marking company as off-limit");

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit", albatrossTkn, null, null, true, markOffLimit);
        
        assertThat("Failed to mark company as off-limit", response.getStatusCode(), equalTo(200));
    }
}
