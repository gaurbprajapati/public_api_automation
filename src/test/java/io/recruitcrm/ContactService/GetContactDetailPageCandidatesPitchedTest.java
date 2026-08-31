package io.recruitcrm.ContactService;

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
public class GetContactDetailPageCandidatesPitchedTest extends TestBase {

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
    @Test(dataProvider = "contactPitchedCandidatesSuccessTestData", groups = {"contact_service", "nightly-build"})
    public void testGetContactDetailPageCandidatesPitchedSuccess(int contactId, String contactName, int candidateId, String candidateName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", String.valueOf(contactId));

        Response response = RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Pitch candidates fetched successfully."));
        assertThat("Meta requestUuid should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Meta responseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Meta responseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Meta timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        assertThat("Data should be an array", jp.get("data"), instanceOf(java.util.List.class));
        int dataSize = jp.get("data.size()");
        assertThat("Should have exactly one pitched candidate", dataSize, equalTo(1));

        Integer responseCandidateId = jp.get("data[0].candidateId");
        Integer responseContactId = jp.get("data[0].contactId");

        assertThat("Candidate ID in response should match created candidate ID", responseCandidateId, equalTo(candidateId));
        String responseCandidateName = jp.get("data[0].name");
        assertThat("Candidate name should match created candidate name", responseCandidateName, equalTo(candidateName));

        assertThat("Candidate object should not be null", jp.get("data[0].candidate"), notNullValue());
        Integer responseCandidateIdFromObject = jp.get("data[0].candidate.candidateId");
        assertThat("Candidate ID in candidate object should match created candidate ID", responseCandidateIdFromObject, equalTo(candidateId));

        String responseCandidateNameFromObject = jp.get("data[0].candidate.name");
        assertThat("Candidate name in candidate object should match created candidate name", responseCandidateNameFromObject, equalTo(candidateName));

        assertThat("Contact ID in response should match created contact ID", responseContactId, equalTo(contactId));

        String pitchStage = jp.get("data[0].pitchStage");
        assertThat("Pitch stage should be 'Pitched' for newly created pitch", pitchStage, equalTo("Pitched"));

        assertThat("data[0].id should not be null", jp.get("data[0].id"), notNullValue());
        assertThat("data[0].candidateSlug should not be null", jp.get("data[0].candidateSlug"), notNullValue());
        assertThat("data[0].createdByName should not be null", jp.get("data[0].createdByName"), notNullValue());
        assertThat("data[0].createdOn should not be null", jp.get("data[0].createdOn"), notNullValue());
        assertThat("data[0].pitchHistory should not be null", jp.get("data[0].pitchHistory"), notNullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/contact/contactPitchedCandidates.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "contactPitchedCandidatesTestData", groups = {"contact_service", "nightly-build"})
    public void testGetContactDetailPageCandidatesPitchedWithoutAuth(int contactId, String contactName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", String.valueOf(contactId));

        Response response = RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get",
                null, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "contactPitchedCandidatesTestData", groups = {"contact_service", "nightly-build"})
    public void testGetContactDetailPageCandidatesPitchedInvalidAuth(int contactId, String contactName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", String.valueOf(contactId));

        Response response = RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get",
                albatrossTkn + "invalid_token", queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testGetContactDetailPageCandidatesPitchedInvalidContactId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", "99999999");

        Response response = RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "missingOrNullContactIdTestData", groups = {"contact_service", "nightly-build"})
    public void testGetContactDetailPageCandidatesPitchedMissingOrNullContactId(String contactId, String description, int expectedStatusCode) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", contactId);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get", albatrossTkn, queryParams, pathParams, true, requestBody.toString());
        assertThat("Expected status code " + expectedStatusCode + " for " + description + " but got " + response.getStatusCode(), response.getStatusCode(), equalTo(expectedStatusCode));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "contactPitchedCandidatesTestData", groups = {"contact_service", "nightly-build"})
    public void testGetContactDetailPageCandidatesPitchedMalformedPayload(int contactId, String contactName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        String malformedJson = "{\"searchTerm\": \"\", \"sortPriorityList\": null";

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", String.valueOf(contactId));

        Response response = RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, malformedJson);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "invalidPageAndSizeTestData", groups = {"contact_service", "nightly-build"})
    public void testGetContactDetailPageCandidatesPitchedInvalidPageAndSize(int contactId, String contactName, String page, String size, String description) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", page);
        queryParams.put("size", size);

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", String.valueOf(contactId));

        Response response = RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());
        assertThat("Expected status code 400 for " + description + " but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));
    }

    private static class TestData {
        int contactId;
        String contactName;
        String contactSlug;
        String companySlug;
        String candidateSlug;
    }

    private TestData setupContactWithPitchedCandidateCore() {
        TestData data = new TestData();

        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        data.companySlug = companyJp.get("slug");

        Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, data.companySlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        data.contactSlug = contactJp.get("slug");

        Response contactDetailsResponse = albatrossFunctions.getContactResponse(albatrossURL, albatrossTkn, data.contactSlug);
        assertThat("Failed to get contact details from albatross API", contactDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath contactDetailsJp = contactDetailsResponse.jsonPath();
        data.contactId = contactDetailsJp.getInt("data.contact.id");
        String contactFirstName = contactDetailsJp.getString("data.contact.firstname");
        String contactLastName = contactDetailsJp.getString("data.contact.lastname");
        data.contactName = contactFirstName + " " + contactLastName;
        assertThat("Contact ID should be greater than 0", data.contactId, greaterThan(0));

        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        data.candidateSlug = candidateJp.get("data.candidate.slug");

        String pitchPath = "pitch/{candidate}/contact/{contact}";
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", data.candidateSlug);
        pathParameters.put("contact", data.contactSlug);

        Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
        assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));

        return data;
    }

    private Object[] setupContactWithPitchedCandidate() {
        TestData data = setupContactWithPitchedCandidateCore();
        return new Object[]{data.contactId, data.contactName};
    }

    @DataProvider(name = "missingOrNullContactIdTestData")
    public Object[][] getMissingOrNullContactIdTestData() {
        return new Object[][]{
                {"", "missing contactId", 404},
                {null, "null contactId", 400}
        };
    }

    @DataProvider(name = "contactPitchedCandidatesTestData")
    public Object[][] getContactPitchedCandidatesTestData() {
        Object[] testData = setupContactWithPitchedCandidate();
        return new Object[][]{{testData[0], testData[1]}};
    }

    @DataProvider(name = "invalidPageAndSizeTestData")
    public Object[][] getInvalidPageAndSizeTestData() {
        Object[] testData = setupContactWithPitchedCandidate();
        int contactId = (Integer) testData[0];
        String contactName = (String) testData[1];

        return new Object[][]{
                {contactId, contactName, "0", "25", "page zero"},
                {contactId, contactName, "-1", "25", "page negative"},
                {contactId, contactName, "1", "0", "size zero"},
                {contactId, contactName, "1", "-1", "size negative"},
                {contactId, contactName, "0", "0", "both page and size zero"},
                {contactId, contactName, "-1", "-1", "both page and size negative"}
        };
    }

    private Object[] setupContactWithPitchedCandidateDetailed() {
        TestData data = setupContactWithPitchedCandidateCore();

        Response candidateDetailsResponse = albatrossFunctions.getCandidateResponse(albatrossURL, albatrossTkn, data.candidateSlug);
        assertThat("Failed to get candidate details from albatross API", candidateDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath candidateDetailsJp = candidateDetailsResponse.jsonPath();
        int candidateId = candidateDetailsJp.getInt("data.candidate.id");
        String candidateFirstName = candidateDetailsJp.getString("data.candidate.firstname");
        String candidateLastName = candidateDetailsJp.getString("data.candidate.lastname");
        String candidateName = candidateFirstName + " " + candidateLastName;

        return new Object[]{data.contactId, data.contactName, candidateId, candidateName};
    }

    @DataProvider(name = "contactPitchedCandidatesSuccessTestData")
    public Object[][] getContactPitchedCandidatesSuccessTestData() {
        Object[] testData = setupContactWithPitchedCandidateDetailed();
        return new Object[][]{{testData[0], testData[1], testData[2], testData[3]}};
    }
}
