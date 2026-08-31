package io.recruitcrm.JobService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;


@AccountType("Business|AlbatrossTkn")
public class GetJobV2WidgetCountTest extends TestBase {

    private static final String BASE_PATH = "widget-count";

    String albatrossTkn;
    String apiAuthToken;
    commanFunction commanFunction;
    AllCrudFunctions allCrudFunctions;

    private String sharedJobId;
    private String sharedJobSlug;

    @BeforeClass
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        commanFunction = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        createSharedTestDataForAllData();
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_AllZeroCounts() {
        Response companyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Company creation should succeed", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");

        Response contactResponse = commanFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        assertThat("Contact creation should succeed", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");

        Response jobResponse = commanFunction.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug);
        assertThat("Job creation should succeed", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");

        Response jobDetailsResponse = allCrudFunctions.getJobResponse(albatrossURL, albatrossTkn, jobSlug);
        assertThat("Failed to get job details from albatross API", jobDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath jobDetailsJp = jobDetailsResponse.jsonPath();
        Object jobIdObj = jobDetailsJp.get("data.job.id");
        String jobId = jobIdObj != null ? jobIdObj.toString() : null;
        assertThat("Job ID should not be null", jobId, notNullValue());

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", jobId);
        queryParams.put("recordSlug", jobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertJobV2WidgetCountSuccessMeta(jp);
        assertThat("hotlists should be 0", jp.get("data.hotlists"), equalTo(0));
        assertThat("relatedDeals should be 0", jp.get("data.relatedDeals"), equalTo(0));
        assertThat("candidatePipeline should be 0", jp.get("data.candidatePipeline"), equalTo(0));
        assertThat("executiveSearchReport should be 0", jp.get("data.executiveSearchReport"), equalTo(0));
        assertThat("jobCampaigns should be 0", jp.get("data.jobCampaigns"), equalTo(0));
        assertThat("clientFeedback should be 0", jp.get("data.clientFeedback"), equalTo(0));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/jobV2WidgetCount.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_WithAllData() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", sharedJobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertJobV2WidgetCountSuccessMeta(jp);

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("hotlists should be at least 1", jp.get("data.hotlists"), equalTo(1));
        assertThat("relatedDeals should be at least 1", jp.get("data.relatedDeals"), equalTo(1));
        assertThat("candidatePipeline should be at least 1", jp.get("data.candidatePipeline"), equalTo(0));
        assertThat("executiveSearchReport should be 0", jp.get("data.executiveSearchReport"), equalTo(0));
        assertThat("jobCampaigns should be 0", jp.get("data.jobCampaigns"), equalTo(0));
        assertThat("clientFeedback should be 0", jp.get("data.clientFeedback"), equalTo(0));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "widgetKeysDataProvider", groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_WithWidgetKeys(String widgetKey) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", sharedJobSlug);
        queryParams.put("widgetKeys", widgetKey);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertJobV2WidgetCountSuccessMeta(jp);

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Widget key '" + widgetKey + "' should be present", jp.get("data." + widgetKey), notNullValue());
        assertThat("Widget key '" + widgetKey + "' should be non-negative", jp.get("data." + widgetKey), equalTo(1));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_WithMultipleWidgetKeys() {
        String widgetKeys = "hotlists,relatedDeals,candidatePipeline";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", sharedJobSlug);
        queryParams.put("widgetKeys", widgetKeys);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertJobV2WidgetCountSuccessMeta(jp);

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("hotlists should be 1", jp.get("data.hotlists"), equalTo(1));
        assertThat("relatedDeals should be 1", jp.get("data.relatedDeals"), equalTo(1));
        assertThat("candidatePipeline should be 1", jp.get("data.candidatePipeline"), equalTo(0));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_WithoutAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", sharedJobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, null, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_InvalidAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", sharedJobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn + "invalid", queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_MissingEntityType() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", sharedJobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_MissingRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordSlug", sharedJobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_MissingRecordSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", sharedJobId);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_InvalidRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", "999999999");
        queryParams.put("recordSlug", sharedJobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_InvalidRecordSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", "invalid-slug-12345");

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobV2WidgetCount_InvalidEntityType() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "invalidEntityType");
        queryParams.put("recordId", sharedJobId);
        queryParams.put("recordSlug", sharedJobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    private void assertJobV2WidgetCountSuccessMeta(JsonPath jp) {
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Widget Count fetched successfully"));
        assertThat("Response type context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response type code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
    }

    private void createSharedTestDataForAllData() {
        Response companyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Company creation should succeed", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");

        Response contactResponse = commanFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        assertThat("Contact creation should succeed", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");

        Response jobResponse = commanFunction.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug);
        assertThat("Job creation should succeed", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        sharedJobSlug = jobJp.get("slug");

        Response jobDetailsResponse = allCrudFunctions.getJobResponse(albatrossURL, albatrossTkn, sharedJobSlug);
        assertThat("Failed to get job details from albatross API", jobDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath jobDetailsJp = jobDetailsResponse.jsonPath();
        Object jobIdObj = jobDetailsJp.get("data.job.id");
        sharedJobId = jobIdObj != null ? jobIdObj.toString() : null;
        assertThat("Job ID should not be null", sharedJobId, notNullValue());

        Response dealResponse = commanFunction.createNewDealWithMandatoryFields(baseURL, apiAuthToken, companySlug, contactSlug, sharedJobSlug);
        assertThat("Deal creation should succeed", dealResponse.getStatusCode(), equalTo(200));

        Response hotlistResponse = commanFunction.createNewHotlist(baseURL, apiAuthToken, "job");
        assertThat("Hotlist creation should succeed", hotlistResponse.getStatusCode(), equalTo(200));
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        String hotlistId = hotlistJp.getString("id");

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(sharedJobSlug);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String hotlistBasePath = "hotlists/{hotlist}/add-record";
        Response addToHotlistResponse = RestClient.doPost1("JSON", baseURL, hotlistBasePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Add to hotlist should succeed", addToHotlistResponse.getStatusCode(), equalTo(200));

    }

    @DataProvider(name = "widgetKeysDataProvider")
    public Object[][] getWidgetKeysData() {
        return new Object[][] {
                { "hotlists" },
                { "relatedDeals" }
        };
    }
}
