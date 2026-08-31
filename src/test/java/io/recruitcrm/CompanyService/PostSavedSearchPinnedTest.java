package io.recruitcrm.CompanyService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.pojo.albatross.SavedSearch;
import io.rcrm.api.pojo.albatross.SavedSearchRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PostSavedSearchPinnedTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "savedSearchData", groups = {"company_service", "nightly-build"})
    public void testSavedSearchPin_Success(int savedSearchId, String savedSearchName) {
        Response response = pinSavedSearch(savedSearchId, albatrossTkn);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Saved search pinned successfully."));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        assertThat("Data should be null for pin operation", jp.get("data"), nullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/company/savedSearchPinned.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "savedSearchData", groups = {"company_service", "nightly-build"})
    public void testSavedSearchUnpin_Success(int savedSearchId, String savedSearchName) {
        Response pinResponse = pinSavedSearch(savedSearchId, albatrossTkn);
        assertThat("Pin should succeed first", pinResponse.getStatusCode(), equalTo(200));
        Response response = unpinSavedSearch(savedSearchId, albatrossTkn);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Saved search unpinned successfully."));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        assertThat("Data should be null for unpin operation", jp.get("data"), nullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/company/savedSearchUnpinned.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "savedSearchData", groups = {"company_service", "nightly-build"})
    public void testSavedSearchPinUnpin_Workflow(int savedSearchId, String savedSearchName) {
        Response pinResponse = pinSavedSearch(savedSearchId, albatrossTkn);
        assertThat("Pin should succeed", pinResponse.getStatusCode(), equalTo(200));
        assertThat("Pin message should match", pinResponse.jsonPath().get("meta.message"), equalTo("Saved search pinned successfully."));
        Response pinAgainResponse = pinSavedSearch(savedSearchId, albatrossTkn);
        assertThat("Pin again should return 409", pinAgainResponse.getStatusCode(), equalTo(409));
        assertThat("Pin again should indicate already pinned", 
                pinAgainResponse.jsonPath().getString("errors[0].message"), anyOf(containsString("already pinned"), notNullValue()));
        Response unpinResponse = unpinSavedSearch(savedSearchId, albatrossTkn);
        assertThat("Unpin should succeed", unpinResponse.getStatusCode(), equalTo(200));
        assertThat("Unpin message should match", unpinResponse.jsonPath().get("meta.message"), equalTo("Saved search unpinned successfully."));
        Response unpinAgainResponse = unpinSavedSearch(savedSearchId, albatrossTkn);
        assertThat("Unpin again should be handled appropriately", unpinAgainResponse.getStatusCode(), equalTo(409));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchPin_WithoutAuth() {
        Response response = pinSavedSearch(17370, null);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchUnpin_WithoutAuth() {
        Response response = unpinSavedSearch(17370, null);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchPin_InvalidAuth() {
        Response response = pinSavedSearch(17370, albatrossTkn + "123");

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchUnpin_InvalidAuth() {
        Response response = unpinSavedSearch(17370, albatrossTkn + "123");

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchPin_InvalidSavedSearchId() {
        Response response = pinSavedSearch(9999999, albatrossTkn);

        assertThat("Expected status code 500 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchUnpin_InvalidSavedSearchId() {
        Response response = unpinSavedSearch(9999999, albatrossTkn);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchPin_MissingSavedSearchId() {
        String basePath = "saved-searches/pinned-saved-search";

        Response response = RestClient.doPost1("JSON", companyServiceURL, basePath, albatrossTkn, null, null, true, null);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchUnpin_MissingSavedSearchId() {
        String basePath = "saved-searches/pinned-saved-search";

        Response response = RestClient.doDelete("JSON", companyServiceURL, basePath, albatrossTkn, null, null, true);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @DataProvider(name = "savedSearchData")
    public Object[][] getSavedSearchData() {
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossTkn);

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName("PinnedSavedSearch_" + System.currentTimeMillis());
        savedSearch.setEntitytype("companies");
        savedSearch.setJson("{\"filters\":[],\"sort\":[]}");
        savedSearch.setUserid(null);
        savedSearch.setAccountid(null);
        savedSearch.setShare_with_teammates(0);
        savedSearch.setPost_search_revamp(1);
        savedSearch.setCollaborator_id(new ArrayList<>());
        savedSearch.setCollaborator_type(new ArrayList<>());

        SavedSearchRequest request = new SavedSearchRequest();
        request.setSave_searches(savedSearch);
        request.setUpdateUserObj(false);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "saved-searches", authTokenMap, null, true, request);
        assertThat("Failed to create saved search", createResponse.getStatusCode(), equalTo(200));

        JsonPath jp = createResponse.jsonPath();
        int savedSearchId = jp.getInt("data.id");
        String name = jp.getString("data.name");

        assertThat("Saved search ID should not be 0", savedSearchId, greaterThan(0));
        assertThat("Saved search name should not be null", name, notNullValue());

        return new Object[][] { { savedSearchId, name } };
    }

    private Map<String, String> getAuthTokenMap(Object authToken) {
        Map<String, String> tokenMap;
        if (authToken instanceof Map) {
            tokenMap = (Map<String, String>) authToken;
        } else {
            tokenMap = new HashMap<>();
            tokenMap.put("Authorization", "Bearer " + authToken);
        }
        return tokenMap;
    }

    private Response pinSavedSearch(int savedSearchId, Object token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("savedSearch", String.valueOf(savedSearchId));
        String basePath = "saved-searches/{savedSearch}/pinned-saved-search";
        return RestClient.doPost1("JSON", companyServiceURL, basePath, token, null, pathParameters, true, null);
    }

    private Response unpinSavedSearch(int savedSearchId, Object token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("savedSearch", String.valueOf(savedSearchId));
        String basePath = "saved-searches/{savedSearch}/pinned-saved-search";
        return RestClient.doDelete("JSON", companyServiceURL, basePath, token, null, pathParameters, true);
    }
}


