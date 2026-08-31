package io.recruitcrm.CompanyService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.pojo.albatross.CompanyInheritance;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCompanyV2WidgetCountTest extends TestBase {

    private static final String BASE_PATH = "widget-count";
    String albatrossTkn;
    String apiAuthToken;
    commanFunction commanFunction;
    AllCrudFunctions allCrudFunctions;
    
    // Shared test data for testCompanyV2WidgetCount_WithAllData and testCompanyV2WidgetCount_WithWidgetKeys
    private int sharedCompanyId;
    private String sharedCompanySlug;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        commanFunction = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        createSharedTestDataForAllData();
    }


    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_AllZeroCounts() {
        // Create a company with no related data
        Response companyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Company creation should succeed", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        
        // Get company ID from albatross API using slug (response structure: data.company.id)
        Response companyDetailsResponse = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, companySlug);
        assertThat("Failed to get company details from albatross API", companyDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath companyDetailsJp = companyDetailsResponse.jsonPath();
        int companyId = companyDetailsJp.get("data.company.id");
        assertThat("Company ID should not be null", companyId, notNullValue());

        //widget count endpoint
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(companyId));
        queryParams.put("recordSlug", companySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        // Verify all values are zero for new company
        assertThat("jobs should be 0",  jp.get("data.jobs"), equalTo(0));
        assertThat("hotlists should be 0",  jp.get("data.hotlists"), equalTo(0));
        assertThat("relatedDeals should be 0", jp.get("data.relatedDeals"), equalTo(0));
        assertThat("subsidiaries should be 0", jp.get("data.subsidiaries"), equalTo(0));
        assertThat("candidatesPitched should be 0", jp.get("data.candidatesPitched"), equalTo(0));
        assertThat("candidatesEmployed should be 0", jp.get("data.candidatesEmployed"), equalTo(0));
        // JSON Schema Validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/companyV2WidgetCount.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_WithAllData() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", sharedCompanySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Response type code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Widget Count fetched successfully"));

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("jobs should be at least 1", jp.get("data.jobs"), equalTo(1));
        assertThat("hotlists should be at least 1", jp.get("data.hotlists"), equalTo(1));
        assertThat("relatedDeals should be at least 1", jp.get("data.relatedDeals"), equalTo(1));
        assertThat("subsidiaries should be at least 1", jp.get("data.subsidiaries"), equalTo(1));
        assertThat("candidatesPitched should be at least 1", jp.get("data.candidatesPitched"), equalTo(1));
        assertThat("candidatesEmployed should be 0", jp.get("data.candidatesEmployed"), equalTo(0));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "widgetKeysDataProvider", groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_WithWidgetKeys(String widgetKey) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", sharedCompanySlug);
        queryParams.put("widgetKeys", widgetKey);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Response type code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Widget Count fetched successfully"));

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        // Verify only the specified widget key is present in response
        assertThat("Widget key '" + widgetKey + "' should be present", jp.get("data." + widgetKey), notNullValue());
        assertThat("Widget key '" + widgetKey + "' should be non-negative", jp.get("data." + widgetKey), greaterThanOrEqualTo(0));

        // Verify other widget keys are not present
        String[] allWidgetKeys = {"jobs", "hotlists", "relatedDeals", "subsidiaries", "candidatesPitched", "candidatesEmployed"};
        for (String key : allWidgetKeys) {
            if (!key.equals(widgetKey)) {
                assertThat("Widget key '" + key + "' should not be present when only '" + widgetKey + "' is requested", jp.get("data." + key), nullValue());
            }
        }
    }
    
    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_WithMultipleWidgetKeys() {
        String widgetKeys = "jobs,hotlists,subsidiaries";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", sharedCompanySlug);
        queryParams.put("widgetKeys", widgetKeys);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Response type code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Widget Count fetched successfully"));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());

        // Verify only the specified widget keys are present
        assertThat("jobs should be present", jp.get("data.jobs"), notNullValue());
        assertThat("hotlists should be present", jp.get("data.hotlists"), notNullValue());
        assertThat("subsidiaries should be present", jp.get("data.subsidiaries"), notNullValue());
        
        // Verify values are non-negative
        assertThat("jobs should be non-negative", jp.get("data.jobs"), equalTo(1));
        assertThat("hotlists should be non-negative", jp.get("data.hotlists"), equalTo(1));
        assertThat("subsidiaries should be non-negative", jp.get("data.subsidiaries"), equalTo(1));
        
        // Verify other widget keys are not present
        assertThat("relatedDeals should not be present", jp.get("data.relatedDeals"), nullValue());
        assertThat("candidatesPitched should not be present", jp.get("data.candidatesPitched"), nullValue());
        assertThat("candidatesEmployed should not be present", jp.get("data.candidatesEmployed"), nullValue());
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_WithoutAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", sharedCompanySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, null, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_InvalidAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", sharedCompanySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn + "invalid", queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_MissingEntityType() {
        Map<String, String> queryParams = new HashMap<>();
        // Missing entityType parameter
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", sharedCompanySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_MissingRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        // Missing recordId parameter
        queryParams.put("recordSlug", sharedCompanySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_MissingRecordSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(sharedCompanySlug));
        // Missing recordSlug parameter

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_InvalidRecordId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", "999999999"); // Invalid record ID
        queryParams.put("recordSlug", sharedCompanySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_InvalidRecordSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", "invalid-slug-12345");

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyV2WidgetCount_InvalidEntityType() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "invalidEntityType"); // Invalid entity type
        queryParams.put("recordId", String.valueOf(sharedCompanyId));
        queryParams.put("recordSlug", sharedCompanySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossTkn, queryParams, null, true);
        assert response != null;
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    private void createSharedTestDataForAllData() {
        // Step 1: Create company
        Response companyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Company creation should succeed", companyResponse.getStatusCode(), equalTo(200));

        JsonPath companyJp = companyResponse.jsonPath();
        sharedCompanySlug = companyJp.get("slug");

        // Get company ID from albatross API
        Response companyDetailsResponse = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, sharedCompanySlug);
        assertThat("Failed to get company details from albatross API", companyDetailsResponse.getStatusCode(), equalTo(200));
        JsonPath companyDetailsJp = companyDetailsResponse.jsonPath();
        sharedCompanyId = companyDetailsJp.get("data.company.id");

        // Step 2: Create contact for the company
        Response contactResponse = commanFunction.createNewContact_POST(baseURL, apiAuthToken, sharedCompanySlug);
        assertThat("Contact creation should succeed", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String sharedContactSlug = contactJp.get("slug");

        // Step 3: Create job for the company
        Response jobResponse = commanFunction.createNewJob(baseURL, apiAuthToken, sharedCompanySlug, sharedContactSlug);
        assertThat("Job creation should succeed", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String sharedJobSlug = jobJp.get("slug");

        // Step 4: Create deal for the company
        Response dealResponse = commanFunction.createNewDealWithMandatoryFields(baseURL, apiAuthToken, sharedCompanySlug, sharedContactSlug, sharedJobSlug);
        assertThat("Deal creation should succeed", dealResponse.getStatusCode(), equalTo(200));

        // Step 5: Create hotlist and add company to it
        Response hotlistResponse = commanFunction.createNewHotlist(baseURL, apiAuthToken, "company");
        assertThat("Hotlist creation should succeed", hotlistResponse.getStatusCode(), equalTo(200));
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        String hotlistId = hotlistJp.getString("id");

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(sharedCompanySlug);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String hotlistBasePath = "hotlists/{hotlist}/add-record";
        Response addToHotlistResponse = RestClient.doPost1("JSON", baseURL, hotlistBasePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Add to hotlist should succeed", addToHotlistResponse.getStatusCode(), equalTo(200));

        // Step 6: Create candidate and pitch to contact
        Response candidateResponse = allCrudFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Candidate creation should succeed", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        String candidateSlug = candidateJp.get("data.candidate.slug");

        // Pitch candidate to contact
        String pitchPath = "pitch/{candidate}/contact/{contact}";
        Map<String, String> pitchPathParams = new HashMap<>();
        pitchPathParams.put("candidate", candidateSlug);
        pitchPathParams.put("contact", sharedContactSlug);
        Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, apiAuthToken, null, pitchPathParams, true, null);
        assertThat("Pitch candidate to contact should succeed", pitchResponse.getStatusCode(), equalTo(200));

        // Step 7: Create subsidiary company
        Response childCompanyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Child company creation should succeed", childCompanyResponse.getStatusCode(), equalTo(200));
        JsonPath childCompanyJp = childCompanyResponse.jsonPath();
        String childCompanySlug = childCompanyJp.get("slug");

        // Link child company to parent company
        CompanyInheritance companyInheritance = new CompanyInheritance();
        List<String> childCompanies = new ArrayList<>();
        childCompanies.add(childCompanySlug);
        companyInheritance.setChild_company_slugs(childCompanies);
        companyInheritance.setParent_company_slug(sharedCompanySlug);

        String linkBasePath = "companies/link-to-parent-company";
        Response linkResponse = RestClient.doPost("JSON", albatrossURL, linkBasePath, albatrossTkn, null, true, companyInheritance);
        assertThat("Link child to parent company should succeed", linkResponse.getStatusCode(), equalTo(200));
    }


    @DataProvider(name = "widgetKeysDataProvider")
    public Object[][] getWidgetKeysData() {
        return new Object[][] {
            { "jobs" },
            { "hotlists" },
            { "relatedDeals" },
            { "subsidiaries" },
            { "candidatesPitched" },
            { "candidatesEmployed" }
        };
    }
}

