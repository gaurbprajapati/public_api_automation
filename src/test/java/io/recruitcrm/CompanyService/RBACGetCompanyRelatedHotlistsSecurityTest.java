package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetCompanyRelatedHotlistsSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String RELATED_HOTLISTS_SUCCESS_MESSAGE = "Related hotlists fetched successfully.";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> companyIdsMap;
    private Map<String, String> companySlugsMap;
    private Map<String, Boolean> hotlistCreatedMap;
    
    private String publicToken;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        companyIdsMap = new HashMap<>();
        companySlugsMap = new HashMap<>();
        hotlistCreatedMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
    }

    // Check for view company related hotlists access
    @Owner("Ajendra Singh")
    @Test(dataProvider = "companyViewAccessData", groups = {"role-based", "company-related-hotlists-access"})
    public void getCompanyRelatedHotlists_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create company from creator role if not already created and get the slug
        String companySlug = ensureCompanyCreated(creator);

        // Ensure hotlist is created and company is added to it
        ensureHotlistCreated(companySlug);

        // Get company ID from cache or fetch from ReaperIntegration
        Integer companyId = companyIdsMap.get(companySlug);
        if (companyId == null) {
            companyId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("company", companySlug)
                    .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
            companyIdsMap.put(companySlug, companyId);
        }

        String executorToken = albatrossTknMap.get(executor);

        // Create request body for related hotlists search
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "companies");
        requestBody.put("recordId", companyId);

        Response response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get", executorToken, null, null, true, requestBody.toString());

        int expectedStatusCodeUpdated = expectedStatusCode == 401 ? 403 : expectedStatusCode;
        String expectedMessageUpdated = expectedMessage.equals("Forbidden") ? "Access Denied" : expectedMessage;
        validateRelatedHotlistsResponse(response, expectedStatusCodeUpdated, expectedMessageUpdated, testDescription);
    }

    // Helper method to ensure hotlist is created and company is added to it
    private void ensureHotlistCreated(String companySlug) {
        Boolean isCreated = hotlistCreatedMap.get(companySlug);
        if (isCreated == null || !isCreated) {
            createHotlistForCompany(companySlug);
            hotlistCreatedMap.put(companySlug, true);
        }
    }

    // Helper method to create a hotlist and add company to it
    private void createHotlistForCompany(String companySlug) {
        // Create a hotlist using the public API
        JSONObject hotlistPayload = new JSONObject();
        hotlistPayload.put("name", "RBAC Test Hotlist " + System.currentTimeMillis());
        hotlistPayload.put("related_to_type", "company");
        hotlistPayload.put("shared", 1);

        Response hotlistResponse = RestClient.doPost1("JSON", baseURL, "hotlists", publicToken, null, null, true, hotlistPayload.toString());
        hotlistResponse.then().statusCode(200);

        int hotlistId = hotlistResponse.jsonPath().getInt("id");

        // Add company to the hotlist
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(companySlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, publicToken, null, pathParameters, true, hotlistRelated);
        addResponse.then().statusCode(200);
    }

    private String ensureCompanyCreated(String creator) {
        if (!companySlugsMap.containsKey(creator)) {
            createCompanyFromRole(creator);
        }
        return companySlugsMap.get(creator);
    }

    private void createCompanyFromRole(String creator) {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company();
        company.setCompany_name(faker.getCompanyName());
        company.setWebsite(faker.getUrl());
        company.setCity(faker.getCity());
        
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        assertThat("Company Slug should not be null", companySlug, notNullValue());

        int companyId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("company", companySlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        
        companyIdsMap.put(companySlug, companyId);
        companySlugsMap.put(creator, companySlug);
    }

    private void validateRelatedHotlistsResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            response.then().body("meta.message", equalTo(RELATED_HOTLISTS_SUCCESS_MESSAGE));
            response.then().body("meta.status", equalTo(200));
            response.then().body("data", notNullValue());
            response.then().body("data", instanceOf(java.util.List.class));
        } else if (expectedStatusCode == 403 && ACCESS_DENIED_MESSAGE.equals(expectedMessage)) {
            response.then().body("errors[0].message", anyOf(
                equalTo("Access Denied: User is not authorized to view this company's data"),
                containsString("Access Denied"),
                containsString("not authorized")
            ));
        }
    }

    @DataProvider(name = "companyViewAccessData", parallel = true)
    public Object[][] companyViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "related hotlists for company");
    }
}
