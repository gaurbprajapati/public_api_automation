package io.recruitcrm.albatross.company;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCompany;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.json.JSONArray;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.recruitcrm.albatross.company.CompanyJson;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACCompanyViewEditDeleteSecurityTest extends TestBase {

    private final JavaFakerCompany fakerCompany = new JavaFakerCompany();
    private final commanFunction function = new commanFunction();
    private final AllCrudFunctions crudFunctions = new AllCrudFunctions();

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> companySlugsMap;
    private Map<String, Boolean> companyCreatedMap;
    private Map<String, String> entityIdMap; // Cache for entity IDs
    private String publicToken;

    // Constants for expected messages
    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String COMPANY_UPDATED_MESSAGE = "Company Updated";
    private static final String COMPANY_DELETED_MESSAGE = "Company Deleted";
    private static final String FAILED_UPDATE_MESSAGE = "Failed to Update Company : Access Denied";

    @BeforeClass(alwaysRun = true)    public void setup() {
        initializeMaps();
        setupTokensAndUserIds();
        initializeCompanyTracking();
    }

    private void initializeMaps() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        companySlugsMap = new HashMap<>();
        companyCreatedMap = new HashMap<>();
        entityIdMap = new HashMap<>();
    }

    // Helper method to get entity ID with caching
    private String getEntityId(String entityType, String slug) {
        String cacheKey = entityType + ":" + slug;
        String entityId = entityIdMap.get(cacheKey);
        
        if (entityId == null) {
            // Fetch from ReaperIntegration if not in cache
            Response response = ReaperIntegration.getEntityIdFromSlug(entityType, slug);
            entityId = response.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim();
            entityIdMap.put(cacheKey, entityId);
        }
        
        return entityId;
    }

    private void setupTokensAndUserIds() {
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
    }

    private void initializeCompanyTracking() {
        String[] roles = {"AccountOwner", "Admin", "TeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleNothing"};
        for (String role : roles) {
            companyCreatedMap.put(role, false);
        }
    }

    // Generic validation method for all response types
    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, 
                                String successField, String successValue, String forbiddenField, String forbiddenValue) {
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + response.getStatusCode(), e);
        }

        if (expectedStatusCode == 200) {
            if (SUCCESS_MESSAGE.equals(expectedMessage)) {
                try {
                    if (successField != null && successValue != null) {
                        response.then().body(successField, Matchers.is(successValue));
                    }
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
                }
            }
        } else if (expectedStatusCode == 401) {
            if (FORBIDDEN_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body(forbiddenField, Matchers.is(forbiddenValue));
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected '" + forbiddenValue + "' but got: " + response.jsonPath().getString(forbiddenField), e);
                }
            }
        }
    }

    private void validateCompanyResponse(Response response, int expectedStatusCode, String expectedMessage, String companySlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.company.slug", companySlug, "message", ACCESS_DENIED_MESSAGE);
    }

    private void validateEditCompanyResponse(Response response, int expectedStatusCode, String expectedMessage, String companySlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", COMPANY_UPDATED_MESSAGE, "message", FAILED_UPDATE_MESSAGE);
    }

    private void validateDeleteCompanyResponse(Response response, int expectedStatusCode, String expectedMessage, String entityId, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", COMPANY_DELETED_MESSAGE, "message", ACCESS_DENIED_MESSAGE);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "companyViewAccessData", groups = {"role-based", "company-view-access"})
    public void viewCompany_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String companySlug = ensureCompanyCreated(creator);
        String executorToken = albatrossTknMap.get(executor);

        Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("companySlug", companySlug);
		String basePath = "companies/{companySlug}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParamters, true);
        
        validateCompanyResponse(response, expectedStatusCode, expectedMessage, companySlug, testDescription);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "companyEditAccessData", groups = {"role-based", "company-edit-access"})
    public void editCompany_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String companySlug = ensureCompanyCreated(creator);
        String executorToken = albatrossTknMap.get(executor);

        // Create company update request
        io.recruitcrm.albatross.company.Company albatrossCompany = createAlbatrossCompanyForEdit();
        CompanyJson companyJson = createCompanyJsonWrapper(albatrossCompany);

        // Execute edit request
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", companySlug);
        String basePath = "companies/{company}";
        
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true, companyJson);
        validateEditCompanyResponse(response, expectedStatusCode, expectedMessage, companySlug, testDescription);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "companyDeleteAccessData", groups = {"role-based", "company-delete-access"})
    public void deleteCompany_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create company for deletion
        String companySlug = createCompanyForDeletion(creator);
        String executorToken = albatrossTknMap.get(executor);

        // Get entity ID and execute deletion
        String entityId = getEntityIdFromSlug(companySlug);
        Response deleteResponse = executeCompanyDeletion(executorToken, entityId, companySlug);
        
        validateDeleteCompanyResponse(deleteResponse, expectedStatusCode, expectedMessage, entityId, testDescription);
    }

    private io.recruitcrm.albatross.company.Company createAlbatrossCompanyForEdit() {
        io.recruitcrm.albatross.company.Company company = new io.recruitcrm.albatross.company.Company();
        company.setCompanyname(fakerCompany.getCompanyName());
        company.setWebsite(fakerCompany.getUrl());
        company.setCity(fakerCompany.getCity());
        company.setIndustryid(fakerCompany.getIndustry_id());
        company.setAddress(fakerCompany.getAddress());
        return company;
    }

    private CompanyJson createCompanyJsonWrapper(io.recruitcrm.albatross.company.Company company) {
        CompanyJson companyJson = new CompanyJson();
        companyJson.setAddress_changed(true);
        companyJson.setCompany(company);
        return companyJson;
    }

    private String createCompanyForDeletion(String creator) {
        io.rcrm.api.pojo.Company apiCompany = new io.rcrm.api.pojo.Company(
            fakerCompany.getCompanyName(), 
            fakerCompany.getUrl(), 
            fakerCompany.getContactNumber(), 
            "", 
            fakerCompany.getIndustry_id(), 
            userIdsMap.get(creator), 
            userIdsMap.get(creator)
        );

        Response response = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, apiCompany);
        return response.jsonPath().get("slug");
    }

    private String getEntityIdFromSlug(String companySlug) {
        return getEntityId("company", companySlug);
    }

    private Response executeCompanyDeletion(String executorToken, String entityId, String companySlug) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("idsToDelete", new JSONArray().put(Integer.parseInt(entityId)));
        requestBody.put("slugsToDelete", new JSONArray().put(companySlug));
        requestBody.put("tableFlag", "company");

        String basePath = "global/delete-record";
        return RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, null, true, requestBody);
    }

    private String ensureCompanyCreated(String creatorRole) {
        Boolean isCreated = companyCreatedMap.get(creatorRole);
        System.out.println("isCreated: " + isCreated);

        if (isCreated == null || !isCreated) {
            createCompanyFromRole(creatorRole);
            companyCreatedMap.put(creatorRole, true);
        }
        return companySlugsMap.get(creatorRole);
    }

    private void createCompanyFromRole(String role) {
        io.rcrm.api.pojo.Company apiCompany = new io.rcrm.api.pojo.Company(
            fakerCompany.getCompanyName(), 
            fakerCompany.getUrl(), 
            fakerCompany.getContactNumber(), 
            "", 
            fakerCompany.getIndustry_id(), 
            userIdsMap.get(role), 
            userIdsMap.get(role)
        );

        Response response = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, apiCompany);
        companySlugsMap.put(role, response.jsonPath().get("slug"));
    }

    @DataProvider(name = "companyViewAccessData", parallel = true)
    public Object[][] companyViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "company");
    }

    @DataProvider(name = "companyEditAccessData", parallel = true)
    public Object[][] companyEditAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getEditAccessData(context, "company");
    }

    @DataProvider(name = "companyDeleteAccessData", parallel = true)
    public Object[][] companyDeleteAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getDeleteAccessData(context, "company");
    }
}
