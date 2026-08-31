package io.recruitcrm.albatross.global;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@AccountType("Business|AlbatrossTkn")
public class GlobalSearchEntitiesTest extends TestBase {

    private commanFunction function = new commanFunction();
    private Object apiAuthToken;
    private Object albatrossAuthToken;

    private static final String SEARCH_ENTITIES_PATH = "global/search-entities";
    private static final String DEAL_GLOBAL_SEARCH_PATH = "global/deal-global-search";

    @BeforeClass
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Test(dataProvider = "searchCompanyData")
    public void searchEntities_Company(String searchTerm, String entitySlug,
                                       String expectedTitle, String expectedCity,
                                       String expectedWebsite, String expectedEntityType,
                                       String expectedColor) {
        JSONObject request = new JSONObject();
        request.put("search", searchTerm);
        request.put("entity", "companies");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response searchResponse = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken, null, true, request);
        Assert.assertEquals(searchResponse.getStatusCode(), 200, "Search request failed");
        assertThat(searchResponse.jsonPath().getString("status"), is("success"));
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/searchEntitiesSuccess.json"));

        Map<String, Object> foundItem = findItemBySlug(searchResponse.jsonPath().getList("data"), entitySlug);
        assertThat("Created company should appear in search results", foundItem, notNullValue());
        assertThat(foundItem.get("title"), is(expectedTitle));
        assertThat(foundItem.get("slug"), is(entitySlug));
        assertThat(foundItem.get("city"), is(expectedCity));
        assertThat(foundItem.get("website"), is(expectedWebsite));
        assertThat(String.valueOf(foundItem.get("entitytype")), is(expectedEntityType));
        assertThat(foundItem.get("color"), is(expectedColor));
    }

    @Test(dataProvider = "searchContactData")
    public void searchEntities_Contact(String searchTerm, String entitySlug,
                                       String expectedTitle, String expectedEmail,
                                       String expectedEntityType, String expectedColor) {
        JSONObject request = new JSONObject();
        request.put("search", searchTerm);
        request.put("entity", "contacts");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response searchResponse = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken, null, true, request);
        Assert.assertEquals(searchResponse.getStatusCode(), 200, "Search request failed");
        assertThat(searchResponse.jsonPath().getString("status"), is("success"));
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/searchEntitiesSuccess.json"));

        Map<String, Object> foundItem = findItemBySlug(searchResponse.jsonPath().getList("data"), entitySlug);
        assertThat("Created contact should appear in search results", foundItem, notNullValue());
        assertThat(String.valueOf(foundItem.get("title")).trim(), is(expectedTitle.trim()));
        assertThat(foundItem.get("slug"), is(entitySlug));
        assertThat(foundItem.get("email"), is(expectedEmail));
        assertThat(String.valueOf(foundItem.get("entitytype")), is(expectedEntityType));
        assertThat(foundItem.get("color"), is(expectedColor));
    }

    @Test(dataProvider = "searchCandidateData")
    public void searchEntities_Candidate(String searchTerm, String entitySlug,
                                         String expectedTitle, String expectedEmail,
                                         String expectedEntityType, String expectedColor) {
        JSONObject request = new JSONObject();
        request.put("search", searchTerm);
        request.put("entity", "candidates");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response searchResponse = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken, null, true, request);
        Assert.assertEquals(searchResponse.getStatusCode(), 200, "Search request failed");
        assertThat(searchResponse.jsonPath().getString("status"), is("success"));
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/searchEntitiesSuccess.json"));

        Map<String, Object> foundItem = findItemBySlug(searchResponse.jsonPath().getList("data"), entitySlug);
        assertThat("Created candidate should appear in search results", foundItem, notNullValue());
        assertThat(String.valueOf(foundItem.get("title")).trim(), is(expectedTitle.trim()));
        assertThat(foundItem.get("slug"), is(entitySlug));
        assertThat(foundItem.get("email"), is(expectedEmail));
        assertThat(String.valueOf(foundItem.get("entitytype")), is(expectedEntityType));
        assertThat(foundItem.get("color"), is(expectedColor));
    }

    @Test(dataProvider = "searchJobData")
    public void searchEntities_Job(String searchTerm, String entitySlug,
                                   String expectedTitle, String expectedEntityType,
                                   String expectedColor) {
        JSONObject request = new JSONObject();
        request.put("search", searchTerm);
        request.put("entity", "jobs");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response searchResponse = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken, null, true, request);
        Assert.assertEquals(searchResponse.getStatusCode(), 200, "Search request failed");
        assertThat(searchResponse.jsonPath().getString("status"), is("success"));
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/searchEntitiesSuccess.json"));

        Map<String, Object> foundItem = findItemBySlug(searchResponse.jsonPath().getList("data"), entitySlug);
        assertThat("Created job should appear in search results", foundItem, notNullValue());
        assertThat(foundItem.get("title"), is(expectedTitle));
        assertThat(foundItem.get("slug"), is(entitySlug));
        assertThat(String.valueOf(foundItem.get("entitytype")), is(expectedEntityType));
        assertThat(foundItem.get("color"), is(expectedColor));
    }

    @Test(dataProvider = "searchDealData")
    public void searchEntities_Deal(String searchTerm, String entitySlug,
                                 String expectedTitle, Object expectedDealValue,
                                 String expectedEntityType, String expectedColor) {
        JSONObject request = new JSONObject();
        request.put("search", searchTerm);
        request.put("fromTopBarGlobalSearch", true);

        Response searchResponse = RestClient.doPost("JSON", albatrossURL, DEAL_GLOBAL_SEARCH_PATH, albatrossAuthToken, null, true, request);
        Assert.assertEquals(searchResponse.getStatusCode(), 200, "Deal search request failed");
        assertThat(searchResponse.jsonPath().getString("status"), is("success"));
        assertThat(searchResponse.jsonPath().getString("message"), containsString("Deals retrieved"));
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/globalSearchDeal.json"));

        Map<String, Object> foundItem = findItemBySlug(searchResponse.jsonPath().getList("data"), entitySlug);
        assertThat("Created deal should appear in search results", foundItem, notNullValue());
        assertThat(foundItem.get("title"), is(expectedTitle));
        assertThat(foundItem.get("slug"), is(entitySlug));
        assertThat("Deal value should match", String.valueOf(foundItem.get("dealvalue")), containsString(String.valueOf(expectedDealValue)));
        assertThat(String.valueOf(foundItem.get("entitytype")), is(expectedEntityType));
        assertThat(foundItem.get("color"), is(expectedColor));
    }


    @Test
    public void searchEntities_Company_401_InvalidToken() {
        JSONObject request = new JSONObject();
        request.put("search", "test");
        request.put("entity", "companies");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken + "invalid", null, true, request);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid token");
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Test
    public void searchEntities_Company_401_EmptyToken() {
        JSONObject request = new JSONObject();
        request.put("search", "test");
        request.put("entity", "companies");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, "", null, true, request);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for empty token");
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Test
    public void searchEntities_Contact_401_InvalidToken() {
        JSONObject request = new JSONObject();
        request.put("search", "test");
        request.put("entity", "contacts");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken + "invalid", null, true, request);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid token");
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Test
    public void searchEntities_Candidate_401_InvalidToken() {
        JSONObject request = new JSONObject();
        request.put("search", "test");
        request.put("entity", "candidates");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken + "invalid", null, true, request);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid token");
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Test
    public void searchEntities_Job_401_InvalidToken() {
        JSONObject request = new JSONObject();
        request.put("search", "test");
        request.put("entity", "jobs");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken + "invalid", null, true, request);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid token");
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Test
    public void searchEntities_Deal_401_InvalidToken() {
        JSONObject request = new JSONObject();
        request.put("search", "test");
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, DEAL_GLOBAL_SEARCH_PATH, albatrossAuthToken + "invalid", null, true, request);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid token");
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Test
    public void searchEntities_Deal_401_EmptyToken() {
        JSONObject request = new JSONObject();
        request.put("search", "test");
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, DEAL_GLOBAL_SEARCH_PATH, "", null, true, request);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for empty token");
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Test
    public void searchEntities_Company_200_EmptySearchReturnsEmptyData() {
        JSONObject request = new JSONObject();
        request.put("search", "xyznonexistent12345");
        request.put("entity", "companies");
        request.put("runOnSingleStore", true);
        request.put("fromTopBarGlobalSearch", true);

        Response response = RestClient.doPost("JSON", albatrossURL, SEARCH_ENTITIES_PATH, albatrossAuthToken, null, true, request);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for valid request with no results");
        assertThat(response.jsonPath().getString("status"), is("success"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/searchEntitiesSuccess.json"));
        assertThat(response.jsonPath().getList("data"), empty());
    }

    private Map<String, Object> findItemBySlug(List<Map<String, Object>> data, String slug) {
        if (data == null) return null;
        return data.stream()
                .filter(item -> slug.equals(item.get("slug")))
                .findFirst()
                .orElse(null);
    }

    @DataProvider
    public Object[][] searchCompanyData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        JsonPath jp = companyResponse.jsonPath();
        String searchTerm = jp.getString("company_name");
        String slug = jp.getString("slug");
        String expectedTitle = jp.getString("company_name");
        String expectedCity = jp.getString("city");
        String expectedWebsite = jp.getString("website");
        String expectedEntityType = "3";
        String expectedColor = "company";
        return new Object[][]{{searchTerm, slug, expectedTitle, expectedCity, expectedWebsite, expectedEntityType, expectedColor}};
    }

    @DataProvider
    public Object[][] searchContactData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        String companySlug = companyResponse.jsonPath().getString("slug");
        Response contactResponse = function.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        JsonPath jp = contactResponse.jsonPath();
        String searchTerm = jp.getString("first_name");
        String slug = jp.getString("slug");
        String expectedTitle = jp.getString("first_name") + " " + jp.getString("last_name");
        String expectedEmail = jp.getString("email");
        String expectedEntityType = "2";
        String expectedColor = "contact";
        return new Object[][]{{searchTerm, slug, expectedTitle, expectedEmail, expectedEntityType, expectedColor}};
    }

    @DataProvider
    public Object[][] searchCandidateData() {
        Response candidateResponse = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken);
        JsonPath jp = candidateResponse.jsonPath();
        String searchTerm = jp.getString("first_name");
        String slug = jp.getString("slug");
        String expectedTitle = jp.getString("first_name") + " " + jp.getString("last_name");
        String expectedEmail = jp.getString("email");
        String expectedEntityType = "5";
        String expectedColor = "candidate";
        return new Object[][]{{searchTerm, slug, expectedTitle, expectedEmail, expectedEntityType, expectedColor}};
    }

    @DataProvider
    public Object[][] searchJobData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        String companySlug = companyResponse.jsonPath().getString("slug");
        Response contactResponse = function.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        String contactSlug = contactResponse.jsonPath().getString("slug");
        Response jobResponse = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug);
        JsonPath jp = jobResponse.jsonPath();
        String searchTerm = jp.getString("name");
        String slug = jp.getString("slug");
        String expectedTitle = jp.getString("name");
        String expectedEntityType = "4";
        String expectedColor = "job";
        return new Object[][]{{searchTerm, slug, expectedTitle, expectedEntityType, expectedColor}};
    }

    @DataProvider
    public Object[][] searchDealData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        String companySlug = companyResponse.jsonPath().getString("slug");
        Response dealResponse = function.createNewDealWithMandatoryFields(baseURL, apiAuthToken, companySlug, "", "");
        JsonPath jp = dealResponse.jsonPath();
        String searchTerm = jp.getString("name");
        String slug = jp.getString("slug");
        String expectedTitle = jp.getString("name");
        Object expectedDealValue = jp.get("deal_value");
        String expectedEntityType = "11";
        String expectedColor = "deal";
        return new Object[][]{{searchTerm, slug, expectedTitle, expectedDealValue, expectedEntityType, expectedColor}};
    }
}
