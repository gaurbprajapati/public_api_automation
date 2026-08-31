package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetCandidateRelatedDealsSecurityTest extends TestBase {

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> candidateSlugsMap;
    private Map<String, Boolean> candidateCreatedMap;
    private Map<String, String> dealSlugsMap;
    private Map<String, Boolean> dealCreatedMap;

    private String publicToken;

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String SUCCESS_EMPTY_DEALS = "SuccessEmptyDeals";
    private static final String RELATED_DEALS_SUCCESS_MESSAGE = "Related deals fetched successfully";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        candidateSlugsMap = new HashMap<>();
        candidateCreatedMap = new HashMap<>();
        dealSlugsMap = new HashMap<>();
        dealCreatedMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();

        initializeTracking();
    }

    private void initializeTracking() {
        String[] roles = {"AccountOwner", "Admin", "TeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleNothing"};
        for (String role : roles) {
            candidateCreatedMap.put(role, false);
            dealCreatedMap.put(role, false);
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateRelatedDealsViewAccessData", groups = {"role-based", "candidate-related-deals-access", "candidate_service"})
    public void getCandidateRelatedDeals_Test(String candidateCreator, String dealCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        
        // Step 1: Create candidate with specified owner
        String candidateSlug = ensureCandidateCreated(candidateCreator);
        
        // Step 2: Create deal linked to candidate with specified owner
        ensureDealCreatedForCandidate(dealCreator, candidateSlug);
        
        // Step 3: Get executor's token for API call
        String executorToken = albatrossTknMap.get(executor);
        
        // Step 4: Execute GET related deals API
        Response response = getCandidateRelatedDeals(executorToken, candidateSlug);
        
        // Step 5: Validate response based on expected access level
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private Response getCandidateRelatedDeals(String token, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        
        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        sortOrder.put(sort1);
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        return RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                token, queryParams, pathParams, true, requestBody.toString());
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + actualStatusCode, e);
        }

        JsonPath jp = response.jsonPath();

        if (actualStatusCode == 200) {
            try {
                response.then().body("meta.message", equalTo(RELATED_DEALS_SUCCESS_MESSAGE));
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription 
                        + " - Expected meta.message: '" + RELATED_DEALS_SUCCESS_MESSAGE 
                        + "' but got: '" + jp.getString("meta.message") + "'", e);
            }
            
            try {
                response.then().body("data", notNullValue());
                response.then().body("data.deals", notNullValue());
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription 
                        + " - Data or deals array is null", e);
            }

            int dealsSize = jp.getInt("data.deals.size()");
            if (SUCCESS_MESSAGE.equals(expectedMessage)) {
                if (dealsSize < 1) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected deals count >= 1 but got: " + dealsSize);
                }
            } else if (SUCCESS_EMPTY_DEALS.equals(expectedMessage)) {
                if (dealsSize != 0) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected deals count: 0 but got: " + dealsSize);
                }
            }
        } else if (actualStatusCode == 403) {
            String metaMessage = jp.getString("meta.message");
            String message = jp.getString("message");
            String errorMessage = metaMessage != null ? metaMessage : message;
            
            if (errorMessage != null) {
                boolean isValidMessage = errorMessage.equals("Unauthorised access") 
                        || errorMessage.equals("Access Denied") 
                        || errorMessage.equals("Forbidden")
                        || errorMessage.contains("not authorized");
                if (!isValidMessage) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected error message but got: '" + errorMessage + "'");
                }
            }
        }
    }

    private String ensureCandidateCreated(String creatorRole) {
        Boolean isCreated = candidateCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createCandidateFromRole(creatorRole);
            candidateCreatedMap.put(creatorRole, true);
        }
        return candidateSlugsMap.get(creatorRole);
    }

    private void createCandidateFromRole(String role) {
        Candidate candidate = new Candidate("RBAC Test", "Candidate", userIdsMap.get(role), userIdsMap.get(role));
        Response response = RestClient.doPost("JSON", baseURL, "candidates", publicToken, null, true, candidate);
        assertThat("Failed to create candidate for role: " + role + ". Status: " + response.getStatusCode() + 
                ", Response: " + response.getBody().asString(), 
                response.getStatusCode(), equalTo(200));
        String candidateSlug = response.jsonPath().get("slug");
        candidateSlugsMap.put(role, candidateSlug);
    }

    private void ensureDealCreatedForCandidate(String dealCreatorRole, String candidateSlug) {
        String key = dealCreatorRole + "_" + candidateSlug;
        Boolean isCreated = dealCreatedMap.get(key);
        if (isCreated == null || !isCreated) {
            createDealForCandidate(dealCreatorRole, candidateSlug);
            dealCreatedMap.put(key, true);
        }
    }

    private void createDealForCandidate(String role, String candidateSlug) {
        Deal deal = new Deal();
        deal.setName("RBAC Test Deal - " + System.currentTimeMillis());
        deal.setDeal_value(10000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setCandidate_slug(candidateSlug);
        deal.setOwner_id(String.valueOf(userIdsMap.get(role)));
        deal.setCreated_by(userIdsMap.get(role));

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals", publicToken, null, true, deal);
        assertThat("Failed to create deal for role: " + role + ". Status: " + dealResponse.getStatusCode() + ", Response: " + dealResponse.getBody().asString(), dealResponse.getStatusCode(), equalTo(200));
        
        String key = role + "_" + candidateSlug;
        String dealSlug = dealResponse.jsonPath().get("slug");
        dealSlugsMap.put(key, dealSlug);
    }

    @DataProvider(name = "candidateRelatedDealsViewAccessData", parallel = false)
    public Object[][] candidateRelatedDealsViewAccessData() {
        return new Object[][] {
            // ==================== ACCOUNT OWNER CREATED CANDIDATE & DEAL (Everything access) ====================
            {"AccountOwner", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner can view candidate (Everything) and deals (Everything) - TC001"},
            {"AccountOwner", "AccountOwner", "Admin", 200, "Success", "Admin can view candidate (Everything) and deals (Everything) - TC002"},
            {"AccountOwner", "AccountOwner", "TeamMember", 200, "SuccessEmptyDeals", "TeamMember can view candidate (Everything) but NOT deals (Owned Only) - TC003"},
            {"AccountOwner", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view candidate (Owned Only) - TC004"},
            {"AccountOwner", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly can view candidate (Team Only) and deals (Team Only) - TC005"},
            {"AccountOwner", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view candidate (Nothing) - TC006"},

            // ==================== TEAM MEMBER CREATED CANDIDATE & DEAL (Owned Only deal access) ====================
            {"TeamMember", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view candidate (Everything) and deals (Everything) - TC007"},
            {"TeamMember", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view candidate (Everything) and own deals (Owned Only) - TC008"},
            {"TeamMember", "TeamMember", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view candidate (Owned Only) - TC009"},

            // ==================== RESTRICTED TEAM MEMBER CREATED CANDIDATE & DEAL (Owned Only both) ====================
            {"RestrictedTeamMember", "RestrictedTeamMember", "AccountOwner", 200, "Success", "AccountOwner can view candidate (Everything) and deals (Everything) - TC010"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "TeamMember", 200, "SuccessEmptyDeals", "TeamMember can view candidate (Everything) but NOT deals (Owned Only) - TC011"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember can view own candidate and own deals - TC012"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly can view candidate (Team Only) and deals (Team Only) - TC013"},

            // ==================== CUSTOM ROLE TEAM ONLY CREATED CANDIDATE & DEAL (Team Only both) ====================
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "AccountOwner", 200, "Success", "AccountOwner can view candidate (Everything) and deals (Everything) - TC014"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "TeamMember", 200, "SuccessEmptyDeals", "TeamMember can view candidate (Everything) but NOT deals (Owned Only) - TC015"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view candidate (Owned Only) - TC016"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly can view own candidate and own deals (Team Only) - TC017"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view candidate (Nothing) - TC018"},

            // ==================== CROSS-CREATOR SCENARIOS (Deal visibility edge cases) ====================
            // AccountOwner candidate + TeamMember deal: Test deal ownership visibility
            {"AccountOwner", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view candidate and TeamMember's deal (Everything) - TC019"},
            {"AccountOwner", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view candidate (Everything) and own deal (Owned Only) - TC020"},

            // RestrictedTeamMember candidate + AccountOwner deal: Test candidate ownership + deal visibility
            {"RestrictedTeamMember", "AccountOwner", "RestrictedTeamMember", 200, "SuccessEmptyDeals", "RestrictedTeamMember can view own candidate but NOT AccountOwner's deal - TC021"},
            {"RestrictedTeamMember", "AccountOwner", "TeamMember", 200, "SuccessEmptyDeals", "TeamMember can view candidate but NOT AccountOwner's deal (Owned Only) - TC022"},
        };
    }
}