package io.recruitcrm.CompanyService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.pojo.offlimit.MarkCompanyOffLimit;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCompanyQuickViewCountTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    commanFunction commanFunction;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        commanFunction = new commanFunction();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyQuickViewCount_Success() {
        Response response = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Company quick view data fetched successfully"));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data array
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data array should not be empty", (Integer) jp.get("data.size()"), greaterThan(0));

        // Verify all required fields in data
        assertThat("allCompanies should not be null", jp.get("data[0].allCompanies"), notNullValue());
        assertThat("myCompanies should not be null", jp.get("data[0].myCompanies"), notNullValue());
        assertThat("notInAnyHotlist should not be null", jp.get("data[0].notInAnyHotlist"), notNullValue());
        assertThat("offLimitCompanies should not be null", jp.get("data[0].offLimitCompanies"), notNullValue());

        // Verify all values are non-negative integers
        assertThat("allCompanies should be non-negative", (Integer) jp.get("data[0].allCompanies"), greaterThanOrEqualTo(0));
        assertThat("myCompanies should be non-negative", (Integer) jp.get("data[0].myCompanies"), greaterThanOrEqualTo(0));
        assertThat("notInAnyHotlist should be non-negative", (Integer) jp.get("data[0].notInAnyHotlist"), greaterThanOrEqualTo(0));
        assertThat("offLimitCompanies should be non-negative", (Integer) jp.get("data[0].offLimitCompanies"), greaterThanOrEqualTo(0));

        // Verify logical constraints
        assertThat("myCompanies should not exceed allCompanies", 
                (Integer) jp.get("data[0].myCompanies"), lessThanOrEqualTo((Integer) jp.get("data[0].allCompanies")));
        assertThat("notInAnyHotlist should not exceed allCompanies", 
                (Integer) jp.get("data[0].notInAnyHotlist"), lessThanOrEqualTo((Integer) jp.get("data[0].allCompanies")));
        assertThat("offLimitCompanies should not exceed allCompanies", 
                (Integer) jp.get("data[0].offLimitCompanies"), lessThanOrEqualTo((Integer) jp.get("data[0].allCompanies")));

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/company/companyQuickViewCount.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyQuickViewCount_WithoutAuth() {
        Response response = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                null, null, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanyQuickViewCount_InvalidAuth() {
        Response response = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn + "invalid-token-123", null, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testAllCompaniesCount_AfterCreatingCompany() {
        // Get initial count
        Response initialResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialAllCompanies = (Integer) initialJp.get("data[0].allCompanies");

        String companySlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "company");
        assertThat("Company slug should not be null", companySlug, notNullValue());

        // Get count after creating company
        Response afterCreateResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After create response should succeed", afterCreateResponse.getStatusCode(), equalTo(200));
        JsonPath afterCreateJp = afterCreateResponse.jsonPath();
        int afterCreateAllCompanies = (Integer) afterCreateJp.get("data[0].allCompanies");

        // Verify allCompanies count increased
        assertThat("All companies count should increase after creating company", 
                afterCreateAllCompanies, equalTo(initialAllCompanies+1));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companyCountData", groups = {"company_service", "nightly-build"})
    public void testMyCompaniesCount_AfterOwnershipChange(String companySlug, int companyId, String companyName) {
        // Get initial myCompanies count
        Response initialResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);

        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialAllCompanies = (Integer) initialJp.get("data[0].allCompanies");
        int initialMyCompanies = (Integer) initialJp.get("data[0].myCompanies");
        int initialNotInAnyHotlist = (Integer) initialJp.get("data[0].notInAnyHotlist");
        int initialOffLimitCompanies = (Integer) initialJp.get("data[0].offLimitCompanies");

        // Get a different user to transfer ownership to
        Response usersResponse = commanFunction.getUsers(baseURL, apiAuthToken);
        assertThat("Users response should succeed", usersResponse.getStatusCode(), equalTo(200));
        JsonPath usersJp = usersResponse.jsonPath();
        int newOwnerId = usersJp.get("[0].id");
        assertThat("New owner ID should not be null", newOwnerId, notNullValue());

        transferCompanyOwnership(companySlug, newOwnerId);

        // Get count after ownership change
        Response afterChangeResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);

        assertThat("After change response should succeed", afterChangeResponse.getStatusCode(), equalTo(200));
        JsonPath afterCreateJp = afterChangeResponse.jsonPath();
        int afterCreateAllCompanies = (Integer) afterCreateJp.get("data[0].allCompanies");
        int afterCreateMyCompanies = (Integer) afterCreateJp.get("data[0].myCompanies");
        int afterCreateNotInAnyHotlist = (Integer) afterCreateJp.get("data[0].notInAnyHotlist");
        int afterCreateOffLimitCompanies = (Integer) afterCreateJp.get("data[0].offLimitCompanies");

        // Verify allContacts count remains same
        assertThat("All contacts count should increase after creating contact",
                afterCreateAllCompanies, equalTo(initialAllCompanies));

        // Verify myContacts count not increased
        assertThat("My contacts count should not increase after creating contact",
                afterCreateMyCompanies, equalTo(initialMyCompanies));

        // Verify notInAnyHotlist count remains same
        assertThat("Not in any hotlist count should increase after creating contact",
                afterCreateNotInAnyHotlist, equalTo(initialNotInAnyHotlist));

        // Verify offLimitContacts count remains the same
        assertThat("Off-limit contacts count should remain the same after creating contact",
                afterCreateOffLimitCompanies, equalTo(initialOffLimitCompanies));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companyCountData", groups = {"company_service", "nightly-build"})
    public void testNotInAnyHotlistCount_AfterAddingToHotlist(String companySlug, int companyId, String companyName) {
        // Get initial notInAnyHotlist count
        Response initialResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialNotInHotlist = (Integer) initialJp.get("data[0].notInAnyHotlist");

        // Create a hotlist and add company to it
        Response hotlistResponse = commanFunction.createNewHotlist(baseURL, apiAuthToken, "company");
        assertThat("Hotlist creation should succeed", hotlistResponse.getStatusCode(), equalTo(200));
        
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        String hotlistId = hotlistJp.getString("id");
        assertThat("Hotlist ID should not be null", hotlistId, notNullValue());

        // Add company to hotlist
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(companySlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Add to hotlist should succeed", addResponse.getStatusCode(), equalTo(200));

        // Get count after adding to hotlist
        Response afterAddResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After add response should succeed", afterAddResponse.getStatusCode(), equalTo(200));
        JsonPath afterAddJp = afterAddResponse.jsonPath();
        int afterAddNotInHotlist = (Integer) afterAddJp.get("data[0].notInAnyHotlist");

        // Verify notInAnyHotlist count decreased (or stayed same if company was already in a hotlist)
        assertThat("Not in any hotlist count should not increase after adding to hotlist", 
                afterAddNotInHotlist, equalTo(initialNotInHotlist-1));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companyCountData", groups = {"company_service", "nightly-build"})
    public void testOffLimitCompaniesCount_AfterSettingOffLimit(String companySlug, int companyId, String companyName) {
        // Get initial offLimitCompanies count
        Response initialResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialOffLimitCompanies = (Integer) initialResponse.jsonPath().get("data[0].offLimitCompanies");

        // Get off-limit status ID
        int statusId = getOffLimitStatus();
        assertThat("Status ID should not be null", statusId, notNullValue());

        // Mark company as off-limit
        markCompanyAsOffLimit(companySlug, statusId);

        // Get count after marking as off-limit
        Response afterMarkResponse = RestClient.doGet("JSON", companyServiceURL, "companies/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After mark response should succeed", afterMarkResponse.getStatusCode(), equalTo(200));
        JsonPath afterMarkJp = afterMarkResponse.jsonPath();
        int afterMarkOffLimitCompanies = (Integer) afterMarkJp.get("data[0].offLimitCompanies");
        int allCompanies = (Integer) afterMarkJp.get("data[0].allCompanies");

        // Verify off-limit companies count increased
        assertThat("Off-limit companies count should increase after marking company as off-limit", 
                afterMarkOffLimitCompanies, equalTo(initialOffLimitCompanies + 1));
        assertThat("Off-limit companies should not exceed all companies", 
                afterMarkOffLimitCompanies, lessThanOrEqualTo(allCompanies));
    }

    @DataProvider(name = "companyCountData")
    public Object[][] getCompanyCountData() {
        // Create test company using function
        String companySlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "company");
        assertThat("Company slug should not be null", companySlug, notNullValue());
        
        // Get company details to extract ID and name
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", companySlug);
        String basePath = "companies/{company}";
        
        Response companyResponse = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true);
        assertThat("Company details should be retrieved", companyResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = companyResponse.jsonPath();
        int companyId = jp.get("id");
        String companyName = jp.get("company_name");
        
        assertThat("Company ID should not be null", companyId, notNullValue());
        assertThat("Company name should not be null", companyName, notNullValue());
        
        return new Object[][] { { companySlug, companyId, companyName } };
    }

    private void transferCompanyOwnership(String companySlug, Integer newOwnerId) {
        // Get company details from albatross API
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", companySlug);
        String basePath = "companies/{company}";
        Response companyResponse = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, null, pathParameters, true);
        assertThat("Failed to get company details from albatross API", companyResponse.getStatusCode(), equalTo(200));
        
        JsonPath companyJsonPath = companyResponse.jsonPath();
        Map<String, Object> companyMap = companyJsonPath.get("data.company");
        JSONObject companyData = new JSONObject(companyMap);
        
        // Create transfer ownership payload
        JSONObject transferPayload = new JSONObject();
        transferPayload.put("relatedtotypeid", 3); // Company type ID
        transferPayload.put("selectedowner", newOwnerId);
        JSONArray selectedRows = new JSONArray();
        selectedRows.put(companyData);
        transferPayload.put("selectedrows", selectedRows);
        
        // Transfer ownership
        String transferEndpoint = "users/transfer-ownership/" + newOwnerId;
        Response transferResponse = RestClient.doPost("JSON", albatrossURL, transferEndpoint, albatrossTkn, null, true, transferPayload.toString());
        assertThat("Failed to transfer company ownership to team member", transferResponse.getStatusCode(), equalTo(200));
    }

    private int getOffLimitStatus() {
        Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", apiAuthToken, null, null, false);
        assertThat("Failed to get off-limit status", response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();
        assertThat("Off-limit status should not be null", jp.get("[0].id"), notNullValue());
        return jp.getInt("[0].id");
    }

    private void markCompanyAsOffLimit(String companySlug, int statusId) {
        MarkCompanyOffLimit markCompanyOffLimit = new MarkCompanyOffLimit();
        markCompanyOffLimit.setCompany_slugs(companySlug);
        markCompanyOffLimit.setStatus_id(String.valueOf(statusId));
        markCompanyOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
        markCompanyOffLimit.setReason("Test off-limit reason for quick view count test");
        markCompanyOffLimit.setMark_candidate_off_limit(false);
        markCompanyOffLimit.setMark_contact_off_limit(false);

        Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-off-limit", apiAuthToken,
                null, null, false, markCompanyOffLimit);

        assertThat("Failed to mark company as off-limit", response.getStatusCode(), equalTo(200));
    }
}
