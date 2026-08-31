package io.recruitcrm.CompanyService;

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

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetCompanyRelatedDealsSecurityTest extends TestBase {

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> companySlugsMap;
    private Map<String, Boolean> companyCreatedMap;
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
        companySlugsMap = new HashMap<>();
        companyCreatedMap = new HashMap<>();
        dealSlugsMap = new HashMap<>();
        dealCreatedMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();

        initializeTracking();
    }

    private void initializeTracking() {
        String[] roles = {"AccountOwner", "Admin", "TeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleNothing"};
        for (String role : roles) {
            companyCreatedMap.put(role, false);
            dealCreatedMap.put(role, false);
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "companyRelatedDealsViewAccessData", groups = {"role-based", "company-related-deals-access"})
    public void getCompanyRelatedDeals_Test(String companyCreator, String dealCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {

        // Step 1: Create company with specified owner
        String companySlug = ensureCompanyCreated(companyCreator);

        // Step 2: Create deal linked to company with specified owner
        ensureDealCreatedForCompany(dealCreator, companySlug);
 
        // Step 3: Get executor's token for endpoint call
        String executorToken = albatrossTknMap.get(executor);

        // Step 4: Execute GET related deals endpoint
        Response response = getCompanyRelatedDeals(executorToken, companySlug);

        // Step 5: Validate response based on expected access level
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private Response getCompanyRelatedDeals(String token, String companySlug) {
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
        pathParams.put("Slug", companySlug);

        return RestClient.doPost1("JSON", companyServiceURL, "companies/{Slug}/related-deals", 
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

    private String ensureCompanyCreated(String creatorRole) {
        Boolean isCreated = companyCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createCompanyFromRole(creatorRole);
            companyCreatedMap.put(creatorRole, true);
        }
        return companySlugsMap.get(creatorRole);
    }

    private void createCompanyFromRole(String role) {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company(
            faker.getCompanyName(), 
            faker.getUrl(), 
            faker.getContactNumber(), 
            faker.getLogoURL(), 
            faker.getIndustry_id(), 
            userIdsMap.get(role), 
            userIdsMap.get(role)
        );
        company.setCity(faker.getCity());
        
        Response response = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        assertThat("Failed to create company for role: " + role + ". Status: " + response.getStatusCode() + 
                ", Response: " + response.getBody().asString(), 
                response.getStatusCode(), equalTo(200));
        String companySlug = response.jsonPath().get("slug");
        companySlugsMap.put(role, companySlug);
    }

    private void ensureDealCreatedForCompany(String dealCreatorRole, String companySlug) {
        String key = dealCreatorRole + "_" + companySlug;
        Boolean isCreated = dealCreatedMap.get(key);
        if (isCreated == null || !isCreated) {
            createDealForCompany(dealCreatorRole, companySlug);
            dealCreatedMap.put(key, true);
        }
    }

    private void createDealForCompany(String role, String companySlug) {
        Deal deal = new Deal();
        deal.setName("RBAC Test Deal - " + System.currentTimeMillis());
        deal.setDeal_value(10000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setCompany_slug(companySlug);
        deal.setOwner_id(String.valueOf(userIdsMap.get(role)));
        deal.setCreated_by(userIdsMap.get(role));

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals", publicToken, null, true, deal);
        assertThat("Failed to create deal for role: " + role + ". Status: " + dealResponse.getStatusCode() + ", Response: " + dealResponse.getBody().asString(), dealResponse.getStatusCode(), equalTo(200));

        String key = role + "_" + companySlug;
        String dealSlug = dealResponse.jsonPath().get("slug");
        dealSlugsMap.put(key, dealSlug);
    }

    @DataProvider(name = "companyRelatedDealsViewAccessData", parallel = true)
    public Object[][] companyRelatedDealsViewAccessData() {
        return new Object[][] {
            // ==================== ACCOUNT OWNER CREATED COMPANY & DEAL (Everything access) ====================
            {"AccountOwner", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner can view company (Everything) and deals (Everything) - TC001"},
            {"AccountOwner", "AccountOwner", "Admin", 200, "Success", "Admin can view company (Everything) and deals (Everything) - TC002"},
            {"AccountOwner", "AccountOwner", "TeamMember", 200, "SuccessEmptyDeals", "TeamMember can view company (Everything) but NOT deals (Owned Only) - TC003"},
            {"AccountOwner", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view company (Owned Only) - TC004"},
            {"AccountOwner", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly can view company (Everything) and deals (Everything) - TC005"},
            {"AccountOwner", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view company (Nothing) - TC006"},

            // ==================== TEAM MEMBER CREATED COMPANY & DEAL (Owned Only deal access) ====================
            {"TeamMember", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view company (Everything) and deals (Everything) - TC007"},
            {"TeamMember", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view company (Everything) and own deals (Owned Only) - TC008"},
            {"TeamMember", "TeamMember", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view company (Owned Only) - TC009"},

            // ==================== RESTRICTED TEAM MEMBER CREATED COMPANY & DEAL (Owned Only both) ====================
            {"RestrictedTeamMember", "RestrictedTeamMember", "AccountOwner", 200, "Success", "AccountOwner can view company (Everything) and deals (Everything) - TC010"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "TeamMember", 200, "SuccessEmptyDeals", "TeamMember can view company (Everything) but NOT deals (Owned Only) - TC011"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember can view own company and own deals - TC012"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly can view company (Team Only) and deals (Team Only) - TC013"},

            // ==================== CUSTOM ROLE TEAM ONLY CREATED COMPANY & DEAL (Team Only both) ====================
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "AccountOwner", 200, "Success", "AccountOwner can view company (Everything) and deals (Everything) - TC014"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "TeamMember", 200, "Success", "TeamMember can view company (Everything) but NOT deals (Owned Only) - TC015"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view company (Owned Only) - TC016"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly can view own company and own deals (Team Only) - TC017"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view company (Nothing) - TC018"},

            // ==================== CROSS-CREATOR SCENARIOS (Deal visibility edge cases) ====================
            {"AccountOwner", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view company and TeamMember's deal (Everything) - TC019"},
            {"AccountOwner", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view company (Everything) and own deal (Owned Only) - TC020"},

            // RestrictedTeamMember company + AccountOwner deal: Test company ownership + deal visibility
            {"RestrictedTeamMember", "AccountOwner", "RestrictedTeamMember", 200, "SuccessEmptyDeals", "RestrictedTeamMember can view own company but NOT AccountOwner's deal - TC021"},
            {"RestrictedTeamMember", "AccountOwner", "TeamMember", 200, "SuccessEmptyDeals", "TeamMember can view company but NOT AccountOwner's deal (Owned Only) - TC022"},
        };
    }
}