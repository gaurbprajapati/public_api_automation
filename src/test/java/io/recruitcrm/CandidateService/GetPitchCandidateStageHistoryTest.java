package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetPitchCandidateStageHistoryTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    commanFunction commonFunc;
    AllCrudFunctions albatrossFunctions;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        commonFunc = new commanFunction();
        albatrossFunctions = new AllCrudFunctions();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateStageHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateStageHistorySuccess(String candidateSlug, int recordId, int contactId, int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contactId", String.valueOf(contactId));
        queryParams.put("candidateId", String.valueOf(candidateId));
        queryParams.put("recordId", String.valueOf(recordId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Pitch Candidate stage history fetched successfully."));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data should be an array", jp.get("data"), instanceOf(java.util.List.class));
        assertThat("Data array should not be empty", (Integer) jp.get("data.size()"), greaterThan(0));

        // Verify first data object structure
        assertThat("ID should not be null", jp.get("data[0].id"), notNullValue());
        assertThat("Status ID should not be null", jp.get("data[0].statusId"), notNullValue());
        assertThat("Candidate Status should not be null", jp.get("data[0].candidateStatus"), notNullValue());
        assertThat("Remark should not be null", jp.get("data[0].remark"), notNullValue());
        assertThat("Stage Date should not be null", jp.get("data[0].stageDate"), notNullValue());
        assertThat("Updated By should not be null", jp.get("data[0].updatedBy"), notNullValue());
        assertThat("Created On should not be null", jp.get("data[0].createdOn"), notNullValue());
        assertThat("Updated By Name should not be null", jp.get("data[0].updatedByName"), notNullValue());
        assertThat("Updated On should not be null", jp.get("data[0].updatedOn"), notNullValue());

        // Verify data types and values
        assertThat("ID should be a positive integer", (Integer) jp.get("data[0].id"), greaterThan(0));
        assertThat("Status ID should be a positive integer", (Integer) jp.get("data[0].statusId"), greaterThan(0));
        assertThat("Stage Date should be a positive integer", (Integer) jp.get("data[0].stageDate"), greaterThan(0));
        assertThat("Updated By should be a positive integer", (Integer) jp.get("data[0].updatedBy"), greaterThan(0));
        assertThat("Created On should be a positive integer", (Integer) jp.get("data[0].createdOn"), greaterThan(0));
        assertThat("Updated On should be a positive integer", (Integer) jp.get("data[0].updatedOn"), greaterThan(0));

        // Verify logical constraints
        assertThat("Updated On should be greater than or equal to Created On", 
                (Integer) jp.get("data[0].updatedOn"), greaterThanOrEqualTo((Integer) jp.get("data[0].createdOn")));

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/pitchCandidateStageHistory.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateStageHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateStageHistoryWithoutAuth(String candidateSlug, int recordId, int contactId, int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contactId", String.valueOf(contactId));
        queryParams.put("candidateId", String.valueOf(candidateId));
        queryParams.put("recordId", String.valueOf(recordId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/", 
                null, queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify data field
        assertThat("Data should contain error message", jp.get("data"), equalTo("Internal Server Error"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateStageHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateStageHistoryInvalidAuth(String candidateSlug, int recordId, int contactId, int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contactId", String.valueOf(contactId));
        queryParams.put("candidateId", String.valueOf(candidateId));
        queryParams.put("recordId", String.valueOf(recordId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/", 
                albatrossTkn + "invalid_token", queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateStageHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateStageHistoryInvalidRecordId(String candidateSlug, int recordId, int contactId, int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contactId", String.valueOf(contactId));
        queryParams.put("candidateId", String.valueOf(candidateId));
        queryParams.put("recordId", "999999999"); // Invalid record ID

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/", 
                albatrossTkn, queryParams, null, true);

        JsonPath jp = response.jsonPath();

        // Response code is 200 but data is empty 
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Data should be empty array", (Integer) jp.get("data.size()"), equalTo(0));

    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateStageHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateStageHistoryMissingContactId(String candidateSlug, int recordId, int contactId, int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));
        queryParams.put("recordId", String.valueOf(recordId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateStageHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateStageHistoryMissingCandidateId(String candidateSlug, int recordId, int contactId, int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contactId", String.valueOf(contactId));
        queryParams.put("recordId", String.valueOf(recordId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateStageHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateStageHistoryMissingRecordId(String candidateSlug, int recordId, int contactId, int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contactId", String.valueOf(contactId));
        queryParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/", 
                albatrossTkn, queryParams, null, true);

        // Response code is 200 but data is empty 
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Data should be empty array", (Integer) response.jsonPath().get("data.size()"), equalTo(0));
    }

    @DataProvider(name = "pitchCandidateStageHistoryTestData")
    public Object[][] getPitchCandidateStageHistoryTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1 & 2: Create candidate and company in parallel (independent operations)
            CompletableFuture<Response> candidateFuture = CompletableFuture.supplyAsync(() -> {
                Response response = commonFunc.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken);
                assertThat("Failed to create test candidate", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() -> {
                Response response = commonFunc.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
                assertThat("Failed to create test company", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both candidate and company to be created
            Response candidateResponse = candidateFuture.join();
            Response companyResponse = companyFuture.join();
            
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("slug");
            assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
            
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            assertThat("Company slug should not be null", companySlug, notNullValue());
            
            // Step 1.1 & 3: Fetch candidate details and create contact in parallel
            CompletableFuture<Integer> candidateIdFuture = CompletableFuture.supplyAsync(() -> {
                Response fetchCandidateResponse = albatrossFunctions.getCandidateResponse(albatrossURL, albatrossTkn, candidateSlug);
                assertThat("Failed to fetch candidate by slug", fetchCandidateResponse.getStatusCode(), equalTo(200));
                JsonPath fetchCandidateJp = fetchCandidateResponse.jsonPath();
                int candidateId = fetchCandidateJp.get("data.candidate.id");
                assertThat("Candidate ID should not be null", candidateId, notNullValue());
                return candidateId;
            }, executor);
            
            CompletableFuture<String> contactSlugFuture = CompletableFuture.supplyAsync(() -> {
                Response contactResponse = commonFunc.createNewContact_POST(baseURL, apiAuthToken, companySlug);
                assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
                JsonPath contactJp = contactResponse.jsonPath();
                String contactSlug = contactJp.get("slug");
                assertThat("Contact slug should not be null", contactSlug, notNullValue());
                return contactSlug;
            }, executor);
            
            // Wait for both operations to complete
            int candidateId = candidateIdFuture.join();
            String contactSlug = contactSlugFuture.join();
            
            // Step 3.1: Fetch contact by slug to get proper contact ID
            Map<String, String> contactParams = new HashMap<>();
            contactParams.put("contact", contactSlug);
            Response fetchContactResponse = RestClient.doGet("JSON", albatrossURL, "contacts/{contact}", albatrossTkn, null, contactParams, true);
            assertThat("Failed to fetch contact by slug", fetchContactResponse.getStatusCode(), equalTo(200));

            JsonPath fetchContactJp = fetchContactResponse.jsonPath();
            int contactId = Integer.parseInt(fetchContactJp.get("data.contact.id"));
            assertThat("Contact ID should not be null", contactId, notNullValue());
            
            // Step 4: Pitch candidate to contact
            String pitchPath = "pitch/{candidate}/contact/{contact}";
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);
            pathParameters.put("contact", contactSlug);
            
            Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, apiAuthToken, null, pathParameters, true, null);
            assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));
            
            // Step 5: Get record ID from Albatross widget endpoint
            String widgetPath = "widgets/{candidateId}/pitch-candidate-data";
            Map<String, String> widgetParams = new HashMap<>();
            widgetParams.put("candidateId", String.valueOf(candidateId));
            
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("entitytype", "5");
            
            Response widgetResponse = RestClient.doGet("JSON", albatrossURL, widgetPath, albatrossTkn, queryParams, widgetParams, true);
            assertThat("Failed to get widget data", widgetResponse.getStatusCode(), equalTo(200));
            
            JsonPath widgetJp = widgetResponse.jsonPath();
            int recordId = widgetJp.get("data.records[0].id");
            
            assertThat("Record ID should not be null", recordId, notNullValue());
            assertThat("Record ID should be positive", recordId, greaterThan(0));
            
            return new Object[][] { { candidateSlug, recordId, contactId, candidateId } };
        } finally {
            executor.shutdown();
        }
    }
}
