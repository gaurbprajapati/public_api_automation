package io.recruitcrm.albatross.deal;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.PrivateApiCommonFunctions;
import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;

import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.albatross.deal.CreateDeal;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACDealViewEditDeleteSecurityTest extends TestBase {

    private final JavaFakerDeal fakerDeal = new JavaFakerDeal();
    PrivateApiCommonFunctions privateApiCommonFunctions = new PrivateApiCommonFunctions();

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> dealSlugsMap;
    private Map<String, Boolean> dealCreatedMap;
    private Map<String, String> entityIdMap; // Cache for entity IDs
    
    // Common deal data
    private String dealName = "Test Deal " + System.currentTimeMillis();
    private String publicToken;

    private String ownerAlbatrossToken;

    // Constants for expected messages
    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String DEAL_UPDATED_MESSAGE = "Update Deal Successful ";
    private static final String DEAL_DELETED_MESSAGE = "Delete Deals Successful ";
    private static final String FAILED_UPDATE_MESSAGE = "Failed To Update Deal : Access Denied";

    @BeforeClass(alwaysRun = true)    public void setupToken() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        dealSlugsMap = new HashMap<>();
        dealCreatedMap = new HashMap<>();
        entityIdMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        ownerAlbatrossToken = albatrossTknMap.get("AccountOwner");
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
    
    @DataProvider(name = "dealViewAccessData", parallel = true)
    public Object[][] getDealViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getDealViewAccessData(context);
    }

    @DataProvider(name = "dealEditAccessData", parallel = true)
    public Object[][] getDealEditAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getDealEditAccessData(context);
    }

    @DataProvider(name = "dealDeleteAccessData", parallel = true)
    public Object[][] getDealDeleteAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getDealDeleteAccessData(context);
    }

    // Generic validation method for all response types
    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, 
                                String successField, Object successValue, String forbiddenField, String forbiddenValue) {
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

    private void validateDealResponse(Response response, int expectedStatusCode, String expectedMessage, String dealSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.deal.slug", dealSlug, "message", ACCESS_DENIED_MESSAGE);
    }

    private void validateEditDealResponse(Response response, int expectedStatusCode, String expectedMessage, String dealSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.deal.slug", dealSlug, "message", FAILED_UPDATE_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", DEAL_UPDATED_MESSAGE, "message", FAILED_UPDATE_MESSAGE);
    }

    private void validateDeleteDealResponse(Response response, int expectedStatusCode, String expectedMessage, String entityId, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.id[0]", Integer.parseInt(entityId), "message", ACCESS_DENIED_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", DEAL_DELETED_MESSAGE, "message", ACCESS_DENIED_MESSAGE);
    }

    private Response viewDealHelper(String executorToken, String dealSlug) {
        Map<String, String> authTokenMap = new HashMap<String, String>();
        authTokenMap.put("Authorization", "Bearer " + executorToken);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("deal", dealSlug);
        String basePath = "deals/{deal}";
        
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, authTokenMap, null, pathParameters, true);
        return response;
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "dealViewAccessData", groups = {"role-based", "deal-view-access"})
    public void viewDeal_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String dealSlug = ensureDealCreated(creator);
        String executorToken = albatrossTknMap.get(executor);
        
        Response response = viewDealHelper(executorToken, dealSlug);
        validateDealResponse(response, expectedStatusCode, expectedMessage, dealSlug, testDescription);
        if(creator.equals("AccountOwner") && ((executor.equals("TeamMember") || executor.equals("RestrictedTeamMember") || executor.equals("CustomRoleTeamOnly") || executor.equals("CustomRoleNothing")))) {
            System.out.println("entered here for adding collaborator");
            Response response1 = viewDealHelper(ownerAlbatrossToken, dealSlug);
            privateApiCommonFunctions.addCollaboratorToDeal(albatrossURL, ownerAlbatrossToken, userIdsMap.get(executor), 2, response1);
            Response response2 = viewDealHelper(executorToken, dealSlug);
            switch (executor) {
                case "TeamMember":
                    validateDealResponse(response2, 200, SUCCESS_MESSAGE, dealSlug, "TeamMember should be able to view the deal once added as collaborator");
                    break;
                case "RestrictedTeamMember":
                    validateDealResponse(response2, 200, SUCCESS_MESSAGE, dealSlug, "RestrictedTeamMember should be able to view the deal once added as collaborator");
                    break;
                case "CustomRoleTeamOnly":
                    validateDealResponse(response2, 200, SUCCESS_MESSAGE, dealSlug, "CustomRoleTeamOnly should be able to view the deal once added as collaborator");
                    break;
                case "CustomRoleNothing":
                    validateDealResponse(response2, 401, ACCESS_DENIED_MESSAGE, dealSlug, "CustomRoleNothing should not be able to view the deal once added as collaborator");
                    break;
                default:
                    break;
            }
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "dealEditAccessData", groups = {"role-based", "deal-edit-access"})
    public void editDeal_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String dealSlug = ensureDealCreated(creator);
        String executorToken = albatrossTknMap.get(executor);

        String dealId = getEntityId("deals", dealSlug);
        
        // Get the original deal name for updating
        String originalDealName = dealName;
        
        // Create deal update request using the updated CreateDeal POJO
        CreateDeal.Deal dealData = CreateDeal.Deal.builder()
                .id(Integer.parseInt(dealId))
                .name("Updated " + originalDealName)
                .dealstage(1)
                .dealvalue(String.valueOf(fakerDeal.getDealValue()))
                .closedate(System.currentTimeMillis() / 1000 + 86400)
                .slug(dealSlug)
                .build();
                
        CreateDeal.SelectedOwner selectedOwner = CreateDeal.SelectedOwner.builder()
                .id(userIdsMap.get(executor))
                .build();
                
        CreateDeal.SelectedDealType selectedDealType = CreateDeal.SelectedDealType.builder()
                .id(1)
                .build();
                
        CreateDeal.SelectedDealStage selectedDealStage = CreateDeal.SelectedDealStage.builder()
                .id(1)
                .percentage("100")
                .build();
        
        CreateDeal deal = CreateDeal.builder()
                .deal(dealData)
                .selectedcandidates(new Object[]{})
                .selectedcompanies(new Object[]{})
                .selectedcontacts(new Object[]{})
                .selectedjobs(new Object[]{})
                .selectedOwner(selectedOwner)
                .selectedDealType(selectedDealType)
                .selectedDealStage(selectedDealStage)
                .build();


        // Execute edit request
        String basePath = "deals/" + dealId;

        System.out.println("basePath: " + basePath);
        Map<String, String> authTokenMap = new HashMap<String, String>();
        authTokenMap.put("Authorization", "Bearer " + executorToken);
        
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, deal);


        validateEditDealResponse(response, expectedStatusCode, expectedMessage, dealSlug, testDescription);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "dealDeleteAccessData", groups = {"role-based", "deal-delete-access"})
    public void deleteDeal_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create deal for deletion
        String dealSlug = createDealFromRole(creator);
        String executorToken = albatrossTknMap.get(executor);
        
        // Get deal ID for validation
        String dealId = getEntityId("deals", dealSlug);
        
        // Execute delete request
        Map<String, String> authTokenMap = new HashMap<String, String>();
        authTokenMap.put("Authorization", "Bearer " + executorToken);

        JSONObject requestBody = new JSONObject();
        requestBody.put("idsToDelete", new JSONArray().put(Integer.parseInt(dealId)));
        requestBody.put("slugsToDelete", new JSONArray().put(dealSlug));
        requestBody.put("tableFlag", "deals");

        Response response = RestClient.doPost1("JSON", albatrossURL, "global/delete-record", authTokenMap, null, null, true, requestBody);
        validateDeleteDealResponse(response, expectedStatusCode, expectedMessage, dealId, testDescription);
    }

    private String ensureDealCreated(String creatorRole) {
        Boolean isCreated = dealCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createDealFromRole(creatorRole);
            dealCreatedMap.put(creatorRole, true);
        }
        return dealSlugsMap.get(creatorRole);
    }

    private String createDealFromRole(String role) {
        // Create deal using the Albatross API CreateDeal POJO

        Deal deal = new Deal();
        deal.setName(dealName);
        deal.setDeal_stage("1");
        deal.setDeal_value(fakerDeal.getDealValue());
        deal.setClose_date(fakerDeal.getDealDate());
        deal.setDeal_type("1");
        deal.setOwner_id(String.valueOf(userIdsMap.get(role)));
        deal.setCreated_by(userIdsMap.get(role));


        Response response = RestClient.doPost("JSON", baseURL, "deals", publicToken, null, true, deal);
        response.then().statusCode(200);
        String dealSlug = response.jsonPath().get("slug");
        
        dealSlugsMap.put(role, dealSlug);
        dealCreatedMap.put(role, true);
        
        return dealSlug;
    }
}
