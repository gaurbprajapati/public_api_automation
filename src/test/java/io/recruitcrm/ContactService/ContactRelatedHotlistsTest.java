package io.recruitcrm.ContactService;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactRelatedHotlistsTest extends TestBase {

    commanFunction function = new commanFunction();
    String apiAuthToken;
    String albatrossTkn;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_Success(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "contacts");
        requestBody.put("recordId", recordId);
        requestBody.put("searchTerm", "");
        requestBody.put("sortOrder", JSONObject.NULL);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/company/getRelatedHotlists.json"));


        JsonPath jp = response.jsonPath();
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Related hotlists fetched successfully."));
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Data array should not be empty", jp.get("data.size()"), equalTo(1));

        assertThat("Created hotlist should be found in search results", jp.get("data[0].id"), equalTo(hotlistId));
        assertThat("Created hotlist should be found in search results", jp.get("data[0].entityName"), equalTo("contacts"));
        assertThat("Created hotlist should be found in search results", jp.get("data[0].shared"), equalTo(1));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_WithoutAuth(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "contacts");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get", "", queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 for unauthorized request", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Missing bearer token in header"));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_InvalidAuth(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "contacts");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn+"invalid_token", queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 for invalid token", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_InvalidEntityName(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "InvalidEntity");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Entity name must be one of: companies, candidates, contacts, or jobs"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

     @Owner("Harika")
     @Test(dataProvider = "hotlistSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_CrossEntityName(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(404));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Entity name must be 'contacts'"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Harika")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_InvalidRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "contacts");
        requestBody.put("recordId", 99999999); 

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(404));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should contain expected pattern", jp.get("errors[0].message"), containsString("Entity contacts with ID"));
        assertThat("Error message should contain 'not found'", jp.get("errors[0].message"), containsString("not found"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_MissingEntityName(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Entity name is required"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_MissingRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "contacts");

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Record ID is required"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(dataProvider = "pageAndSizeInvalidData", groups = {"contact_service", "nightly-build"})
    public void testContactRelatedHotlistsSearch_InvalidPageAndSize(int recordId, String page, String size, String description) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", size);
        queryParams.put("page", page);

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "contacts");
        requestBody.put("recordId", recordId);
        requestBody.put("searchTerm", "");
        requestBody.put("sortOrder", JSONObject.NULL);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 400 for " + description + " but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

    }

    @DataProvider(name = "hotlistSearchData", parallel = true)
    public Object[][] getHotlistSearchData() {

        // Create a company first (contacts need a company)
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String companySlug = companyJson.get("slug");

        // Create a contact
        JsonPath contactJson = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
        String contactSlug = contactJson.get("slug");

        Response contactResponse = albatrossFunctions.getContactResponse(albatrossURL, albatrossTkn, contactSlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath jp = contactResponse.jsonPath();
        Object idObj = jp.get("data.contact.id");
        int recordId = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(String.valueOf(idObj));

        assertThat("Contact slug should not be null", contactSlug, notNullValue());
        assertThat("Record ID should not be null", recordId, notNullValue());

        Response hotlistResponse = function.createNewHotlist(baseURL, apiAuthToken, "contact");
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));

        JsonPath hotlistJp = hotlistResponse.jsonPath();
        int hotlistId = hotlistJp.getInt("id");

        assertThat("Hotlist ID should not be null", hotlistId, notNullValue());

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(contactSlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);

        assertThat("Failed to add contact to hotlist", addResponse.getStatusCode(), equalTo(200));

        return new Object[][]{{hotlistId, recordId}};
    }

    @DataProvider(name = "pageAndSizeInvalidData", parallel = true)
    public Object[][] getPageAndSizeInvalidData() {
        // Create a company first (contacts need a company)
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String companySlug = companyJson.get("slug");

        // Create a contact
        JsonPath contactJson = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
        String contactSlug = contactJson.get("slug");

        Response contactResponse = albatrossFunctions.getContactResponse(albatrossURL, albatrossTkn, contactSlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath jp = contactResponse.jsonPath();
        Object idObj = jp.get("data.contact.id");
        int recordId = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(String.valueOf(idObj));

        assertThat("Contact slug should not be null", contactSlug, notNullValue());
        assertThat("Record ID should not be null", recordId, notNullValue());

        Response hotlistResponse = function.createNewHotlist(baseURL, apiAuthToken, "contact");
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        int hotlistId = hotlistJp.getInt("id");

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(contactSlug);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";
        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Failed to add contact to hotlist", addResponse.getStatusCode(), equalTo(200));

        return new Object[][] {
            {recordId, "0", "25", "page zero"},
            {recordId, "-1", "25", "page negative"},
            {recordId, "1", "0", "size zero"},
            {recordId, "1", "-1", "size negative"},
            {recordId, "0", "0", "both page and size zero"},
            {recordId, "-1", "-1", "both page and size negative"},
        };
    }

}
