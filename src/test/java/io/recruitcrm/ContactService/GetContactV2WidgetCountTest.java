package io.recruitcrm.ContactService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetContactV2WidgetCountTest extends TestBase {

    private static final String BASE_PATH = "widget-count";
    String albatrossTkn;
    String apiAuthToken;
    commanFunction commanFunction;
    AllCrudFunctions allCrudFunctions;

    private String sharedContactId;
    private String sharedContactSlug;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        commanFunction = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        createSharedTestDataForAllData();
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_AllZeroCounts() {
        Response companyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Company creation should succeed", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");

        Response contactResponse = commanFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        assertThat("Contact creation should succeed", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");

        Response contactDetailsResponse = allCrudFunctions.getContactResponse(albatrossURL, albatrossTkn, contactSlug);
        assertThat("Failed to get contact details from albatross API", contactDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath contactDetailsJp = contactDetailsResponse.jsonPath();
        Object contactIdObj = contactDetailsJp.get("data.contact.id");
        String contactId = contactIdObj != null ? contactIdObj.toString() : null;
        assertThat("Contact ID should not be null", contactId, notNullValue());

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", contactId);
        queryParams.put("recordSlug", contactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertThat("jobs should be 0", jp.get("data.jobs"), equalTo(0));
        assertThat("hotlists should be 0", jp.get("data.hotlists"), equalTo(0));
        assertThat("relatedDeals should be 0", jp.get("data.relatedDeals"), equalTo(0));
        assertThat("candidatesPitched should be 0", jp.get("data.candidatesPitched"), equalTo(0));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/contactV2WidgetCount.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_WithAllData() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", sharedContactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Response type code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Widget Count fetched successfully"));

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("jobs should be at least 1", jp.get("data.jobs"), equalTo(1));
        assertThat("hotlists should be at least 1", jp.get("data.hotlists"), equalTo(1));
        assertThat("relatedDeals should be at least 1", jp.get("data.relatedDeals"), equalTo(1));
        assertThat("candidatesPitched should be at least 1", jp.get("data.candidatesPitched"), equalTo(1));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "widgetKeysDataProvider", groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_WithWidgetKeys(String widgetKey) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", sharedContactSlug);
        queryParams.put("widgetKeys", widgetKey);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Response type code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Widget Count fetched successfully"));

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Widget key '" + widgetKey + "' should be present", jp.get("data." + widgetKey), notNullValue());
        assertThat("Widget key '" + widgetKey + "' should be non-negative", jp.get("data." + widgetKey), equalTo(1));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_WithMultipleWidgetKeys() {
        String widgetKeys = "jobs,hotlists,relatedDeals";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", sharedContactSlug);
        queryParams.put("widgetKeys", widgetKeys);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Widget Count fetched successfully"));
        assertThat("Data should not be null", jp.get("data"), notNullValue());

        assertThat("jobs should be present", jp.get("data.jobs"), notNullValue());
        assertThat("hotlists should be present", jp.get("data.hotlists"), notNullValue());
        assertThat("relatedDeals should be present", jp.get("data.relatedDeals"), notNullValue());
        assertThat("jobs should be non-negative", jp.get("data.jobs"), equalTo(1));
        assertThat("hotlists should be non-negative", jp.get("data.hotlists"), equalTo(1));
        assertThat("relatedDeals should be non-negative", jp.get("data.relatedDeals"), equalTo(1));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_WithoutAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", sharedContactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, null, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 500 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(500));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_InvalidAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", sharedContactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn + "invalid", queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_MissingEntityType() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", sharedContactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_MissingRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordSlug", sharedContactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_MissingRecordSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", sharedContactId);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_InvalidRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", "999999999");
        queryParams.put("recordSlug", sharedContactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_InvalidRecordSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", "invalid-slug-12345");

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactV2WidgetCount_InvalidEntityType() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "invalidEntityType");
        queryParams.put("recordId", sharedContactId);
        queryParams.put("recordSlug", sharedContactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    private void createSharedTestDataForAllData() {
        Response companyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Company creation should succeed", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");

        Response contactResponse = commanFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        assertThat("Contact creation should succeed", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        sharedContactSlug = contactJp.get("slug");

        Response contactDetailsResponse = allCrudFunctions.getContactResponse(albatrossURL, albatrossTkn, sharedContactSlug);
        assertThat("Failed to get contact details from albatross API", contactDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath contactDetailsJp = contactDetailsResponse.jsonPath();
        Object contactIdObj = contactDetailsJp.get("data.contact.id");
        sharedContactId = contactIdObj != null ? contactIdObj.toString() : null;
        assertThat("Contact ID should not be null", sharedContactId, notNullValue());

        Response jobResponse = commanFunction.createNewJob(baseURL, apiAuthToken, companySlug, sharedContactSlug);
        assertThat("Job creation should succeed", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");

        Response dealResponse = commanFunction.createNewDealWithMandatoryFields(baseURL, apiAuthToken, companySlug, sharedContactSlug, jobSlug);
        assertThat("Deal creation should succeed", dealResponse.getStatusCode(), equalTo(200));

        Response hotlistResponse = commanFunction.createNewHotlist(baseURL, apiAuthToken, "contact");
        assertThat("Hotlist creation should succeed", hotlistResponse.getStatusCode(), equalTo(200));
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        String hotlistId = hotlistJp.getString("id");

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(sharedContactSlug);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String hotlistBasePath = "hotlists/{hotlist}/add-record";
        Response addToHotlistResponse = RestClient.doPost1("JSON", baseURL, hotlistBasePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Add to hotlist should succeed", addToHotlistResponse.getStatusCode(), equalTo(200));

        Response candidateResponse = allCrudFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Candidate creation should succeed", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        String candidateSlug = candidateJp.get("data.candidate.slug");

        String pitchPath = "pitch/{candidate}/contact/{contact}";
        Map<String, String> pitchPathParams = new HashMap<>();
        pitchPathParams.put("candidate", candidateSlug);
        pitchPathParams.put("contact", sharedContactSlug);
        Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, apiAuthToken, null, pitchPathParams, true, null);
        assertThat("Pitch candidate to contact should succeed", pitchResponse.getStatusCode(), equalTo(200));
    }

    @DataProvider(name = "widgetKeysDataProvider")
    public Object[][] getWidgetKeysData() {
        return new Object[][]{
                {"jobs"},
                {"hotlists"},
                {"relatedDeals"},
                {"candidatesPitched"}
        };
    }
}
