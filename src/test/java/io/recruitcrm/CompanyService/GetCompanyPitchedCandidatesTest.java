package io.recruitcrm.CompanyService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCompanyPitchedCandidatesTest extends TestBase {

    String accountApiKey;
    String albatrossTkn;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        accountApiKey = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "companyPitchedCandidatesSuccessTestData", groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesSuccess(int companyId, String companyName, int candidateId, String candidateName, int contactId, String contactName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(companyId));

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Pitch candidates fetched successfully."));

        // Verify data structure exists
        assertThat("Data should be an array", jp.get("data"), instanceOf(java.util.List.class));
        int dataSize = jp.get("data.size()");
        assertThat("Should have exactly one pitched candidate", dataSize, equalTo(1));

        // Validate the created candidate and contact in the response (directly access data[0])
        Integer responseCandidateId = jp.get("data[0].candidateId");
        Integer responseContactId = jp.get("data[0].contactId");

        // Validate candidate data matches
        assertThat("Candidate ID in response should match created candidate ID", responseCandidateId, equalTo(candidateId));
        String responseCandidateName = jp.get("data[0].name");
        assertThat("Candidate name should match created candidate name", responseCandidateName, equalTo(candidateName));

        Integer responseCandidateIdFromObject = jp.get("data[0].candidate.candidateId");
        assertThat("Candidate ID in candidate object should match created candidate ID", responseCandidateIdFromObject, equalTo(candidateId));

        String responseCandidateNameFromObject = jp.get("data[0].candidate.name");
        assertThat("Candidate name in object should match created candidate name", responseCandidateNameFromObject, equalTo(candidateName));

        // Validate contact data matches
        assertThat("Contact ID in response should match created contact ID", responseContactId, equalTo(contactId));
        String responseContactName = jp.get("data[0].contactName");
        assertThat("Contact name should match created contact name", responseContactName, equalTo(contactName));

        Integer responseContactIdFromObject = jp.get("data[0].contact.contactId");
        assertThat("Contact ID in contact object should match created contact ID", responseContactIdFromObject, equalTo(contactId));

        String responseContactNameFromObject = jp.get("data[0].contact.name");
        assertThat("Contact name in object should match created contact name", responseContactNameFromObject, equalTo(contactName));

        // Validate pitch stage is "Pitched" for newly created pitch
        String pitchStage = jp.get("data[0].pitchStage");
        assertThat("Pitch stage should be 'Pitched' for newly created pitch", pitchStage, equalTo("Pitched"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/company/companyPitchedCandidates.json"));
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "companyPitchedCandidatesTestData", groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesWithoutAuth(int companyId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(companyId));

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                null, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "companyPitchedCandidatesTestData", groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesInvalidAuth(int companyId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(companyId));

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                albatrossTkn + "invalid_token", queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
    }


    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesInvalidCompanyId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", "99999999");

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "missingOrNullCompanyIdTestData", groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesMissingOrNullCompanyId(String companyId, String description, int expectedStatusCode) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", companyId);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get", albatrossTkn, queryParams, pathParams, true, requestBody.toString());
        assertThat("Expected status code " + expectedStatusCode + " for " + description + " but got " + response.getStatusCode(), response.getStatusCode(), equalTo(expectedStatusCode));
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "companyPitchedCandidatesTestData", groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesMalformedPayload(int companyId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        String malformedJson = "{\"searchTerm\": \"\", \"sortPriorityList\": null";

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(companyId));

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, malformedJson);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "invalidPageAndSizeTestData", groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesInvalidPageAndSize(int companyId, String companyName, String page, String size, String description) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", page);
        queryParams.put("size", size);

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(companyId));

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());
        assertThat("Expected status code 400 for " + description + " but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(400));
    }

    private static class TestData {
        int companyId;
        String companyName;
        String companySlug;
        String contactSlug;
        String candidateSlug;
    }

    private TestData setupCompanyWithPitchedCandidateCore() {
        TestData data = new TestData();
        
        // Step 1: Create a company using public API
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        data.companySlug = companyJp.get("slug");
        data.companyName = companyJp.get("company_name");
        assertThat("Company slug should not be null", data.companySlug, notNullValue());
        assertThat("Company name should not be null", data.companyName, notNullValue());

        // Get company ID from albatross API using slug (response structure: data.company.id)
        Response companyDetailsResponse = albatrossFunctions.getCompanyResponse(albatrossURL, albatrossTkn, data.companySlug);
        assertThat("Failed to get company details from albatross API", companyDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath companyDetailsJp = companyDetailsResponse.jsonPath();
        data.companyId = companyDetailsJp.get("data.company.id");
        assertThat("Company ID should not be null", data.companyId, notNullValue());

        // Step 2: Create a contact
        Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, data.companySlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        data.contactSlug = contactJp.get("slug");

        // Step 3: Create a candidate
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        data.candidateSlug = candidateJp.get("data.candidate.slug");

        // Step 4: Pitch candidate to contact
        String pitchPath = "pitch/{candidate}/contact/{contact}";
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", data.candidateSlug);
        pathParameters.put("contact", data.contactSlug);

        Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
        assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));

        return data;
    }


    private Object[] setupCompanyWithPitchedCandidate() {
        TestData data = setupCompanyWithPitchedCandidateCore();
        return new Object[] { data.companyId, data.companyName };
    }

    @DataProvider(name = "missingOrNullCompanyIdTestData")
    public Object[][] getMissingOrNullCompanyIdTestData() {
        return new Object[][] {
            { "", "missing companyId", 404 },
            { null, "null companyId", 400 }
        };
    }

    @DataProvider(name = "companyPitchedCandidatesTestData")
    public Object[][] getCompanyPitchedCandidatesTestData() {
        Object[] testData = setupCompanyWithPitchedCandidate();
        return new Object[][] { { testData[0], testData[1] } };
    }

    @DataProvider(name = "invalidPageAndSizeTestData")
    public Object[][] getInvalidPageAndSizeTestData() {
        Object[] testData = setupCompanyWithPitchedCandidate();
        int companyId = (Integer) testData[0];
        String companyName = (String) testData[1];

        return new Object[][] {
            { companyId, companyName, "0", "25", "page zero" },
            { companyId, companyName, "-1", "25", "page negative" },
            { companyId, companyName, "1", "0", "size zero" },
            { companyId, companyName, "1", "-1", "size negative" },
            { companyId, companyName, "0", "0", "both page and size zero" },
            { companyId, companyName, "-1", "-1", "both page and size negative" }
        };
    }

    private Object[] setupCompanyWithPitchedCandidateDetailed() {
        TestData data = setupCompanyWithPitchedCandidateCore();

        // Get contact details from albatross to get ID and name
        Response contactDetailsResponse = albatrossFunctions.getContactResponse(albatrossURL, albatrossTkn, data.contactSlug);
        assertThat("Failed to get contact details from albatross API", contactDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath contactDetailsJp = contactDetailsResponse.jsonPath();
        int contactId = contactDetailsJp.getInt("data.contact.id");
        String contactFirstName = contactDetailsJp.getString("data.contact.firstname");
        String contactLastName = contactDetailsJp.getString("data.contact.lastname");
        String contactName = contactFirstName + " " + contactLastName;

        // Get candidate details from albatross to get ID and name
        Response candidateDetailsResponse = albatrossFunctions.getCandidateResponse(albatrossURL, albatrossTkn, data.candidateSlug);
        assertThat("Failed to get candidate details from albatross API", candidateDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath candidateDetailsJp = candidateDetailsResponse.jsonPath();
        int candidateId = candidateDetailsJp.getInt("data.candidate.id");
        String candidateFirstName = candidateDetailsJp.getString("data.candidate.firstname");
        String candidateLastName = candidateDetailsJp.getString("data.candidate.lastname");
        String candidateName = candidateFirstName + " " + candidateLastName;

        return new Object[] { data.companyId, data.companyName, candidateId, candidateName, contactId, contactName };
    }

    @DataProvider(name = "companyPitchedCandidatesSuccessTestData")
    public Object[][] getCompanyPitchedCandidatesSuccessTestData() {
        Object[] testData = setupCompanyWithPitchedCandidateDetailed();
        return new Object[][] { { testData[0], testData[1], testData[2], testData[3], testData[4], testData[5] } };
    }
}

