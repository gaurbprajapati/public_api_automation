package io.recruitcrm.CompanyService;

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
public class CompanyRelatedHotlistsTest extends TestBase {

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
    @Test(dataProvider = "hotlistSearchData", groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_Success(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");
        requestBody.put("recordId", recordId);
        requestBody.put("searchTerm", "");
        requestBody.put("sortOrder", JSONObject.NULL);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/company/getRelatedHotlists.json"));

        JsonPath jp = response.jsonPath();
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Related hotlists fetched successfully."));
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Data array should not be empty", jp.get("data.size()"), equalTo(1));

        assertThat("Created hotlist should be found in search results", jp.get("data[0].id"), equalTo(hotlistId));
        assertThat("Created hotlist should be found in search results", jp.get("data[0].entityName"), equalTo("companies"));
        assertThat("Created hotlist should be found in search results", jp.get("data[0].shared"), equalTo(1));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_WithoutAuth(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get", "", queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 for unauthorized request", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Missing bearer token in header"));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_InvalidAuth(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn+"invalid_token", queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 for invalid token", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_InvalidEntityName(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "InvalidEntity");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get",
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
     @Test(dataProvider = "hotlistSearchData", groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_CrossEntityName(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Entity name must be 'companies'"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_InvalidRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");
        requestBody.put("recordId", 99999999); 

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(404));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should contain expected pattern", jp.get("errors[0].message"), containsString("Entity companies with ID"));
        assertThat("Error message should contain 'not found'", jp.get("errors[0].message"), containsString("not found"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchData", groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_MissingEntityName(int hotlistId, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get",
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
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_MissingRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get",
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
    @Test(dataProvider = "pageAndSizeInvalidData", groups = {"company_service", "nightly-build"})
    public void testCompanyRelatedHotlistsSearch_InvalidPageAndSize(int recordId, String page, String size, String description) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", size);
        queryParams.put("page", page);

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");
        requestBody.put("recordId", recordId);
        requestBody.put("searchTerm", "");
        requestBody.put("sortOrder", JSONObject.NULL);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get", albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 400 for " + description + " but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_Success(int hotlistId, int recordId) {
        JSONObject searchRequestBody = new JSONObject();
        searchRequestBody.put("entityName", "companies");
        searchRequestBody.put("recordId", recordId);

        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "companies");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response deleteResponse = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

        assertThat("Delete should succeed", deleteResponse.getStatusCode(), equalTo(200));

        JsonPath deleteJp = deleteResponse.jsonPath();
        assertThat("Meta should not be null", deleteJp.get("meta"), notNullValue());
        assertThat("Message should match expected", deleteJp.get("meta.message"), equalTo("Related hotlists removed successfully"));
        assertThat("Status should be 200", deleteJp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", deleteJp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", deleteJp.get("meta.timestamp"), notNullValue());
        assertThat("ResponseType should not be null", deleteJp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", deleteJp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", deleteJp.get("meta.responseType.code"), equalTo(103));
        assertThat("Data should be null", deleteJp.get("data"), nullValue());


        Response searchAfterDeleteResponse = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, searchRequestBody.toString());

        assertThat("Search after delete should succeed", searchAfterDeleteResponse.getStatusCode(), equalTo(200));

        JsonPath searchAfterDeleteJp = searchAfterDeleteResponse.jsonPath();
        assertThat("Should have empty data array after deletion", searchAfterDeleteJp.get("data.size()"), equalTo(0));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_WithoutAuth(int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "companies");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                "", null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 401 for unauthorized delete request", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Missing bearer token in header"));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_InvalidAuth(int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "companies");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                albatrossTkn + "invalid_token", null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 401 for invalid token delete request", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_InvalidRecordId(int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", 99999999);
        deleteRequestBody.put("entityName", "companies");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 404", jp.get("meta.status"), equalTo(404));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should contain expected pattern", jp.get("errors[0].message"), containsString("Entity companies with ID"));
        assertThat("Error message should contain 'not found'", jp.get("errors[0].message"), containsString("not found"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_InvalidEntityName(int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "InvalidEntity");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

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

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_MissingRecordId(int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("entityName", "companies");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(404));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Entity companies with ID null not found"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_MissingEntityName(int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Entity name is required"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "hotlistSearchData")
    public void testCompanyRelatedHotlistsDelete_MissingHotlistIds(int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "companies");

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match expected", jp.get("errors[0].message"), equalTo("Hotlist ids cannot be null"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @DataProvider(name = "hotlistSearchData", parallel = true)
    public Object[][] getHotlistSearchData() {

        JsonPath json = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String companySlug = json.get("slug");

        Response companyResponse = albatrossFunctions.getCompanyResponse(albatrossURL, albatrossTkn, companySlug);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath jp = companyResponse.jsonPath();
        int recordId = jp.get("data.company.id");

        assertThat("Company slug should not be null", companySlug, notNullValue());
        assertThat("Record ID should not be null", recordId, notNullValue());

        Response hotlistResponse = function.createNewHotlist(baseURL, apiAuthToken, "company");
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));

        JsonPath hotlistJp = hotlistResponse.jsonPath();
        int hotlistId = hotlistJp.getInt("id");

        assertThat("Hotlist ID should not be null", hotlistId, notNullValue());

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(companySlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);

        assertThat("Failed to add company to hotlist", addResponse.getStatusCode(), equalTo(200));

        return new Object[][]{{hotlistId, recordId}};
    }

    @DataProvider(name = "pageAndSizeInvalidData", parallel = true)
    public Object[][] getPageAndSizeInvalidData() {
        JsonPath json = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String companySlug = json.get("slug");

        Response companyResponse = albatrossFunctions.getCompanyResponse(albatrossURL, albatrossTkn, companySlug);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath jp = companyResponse.jsonPath();
        int recordId = jp.get("data.company.id");

        assertThat("Company slug should not be null", companySlug, notNullValue());
        assertThat("Record ID should not be null", recordId, notNullValue());

        Response hotlistResponse = function.createNewHotlist(baseURL, apiAuthToken, "company");
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        int hotlistId = hotlistJp.getInt("id");

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(companySlug);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";
        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Failed to add company to hotlist", addResponse.getStatusCode(), equalTo(200));

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

