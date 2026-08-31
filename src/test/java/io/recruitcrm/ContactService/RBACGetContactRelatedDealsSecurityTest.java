package io.recruitcrm.ContactService;

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

import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetContactRelatedDealsSecurityTest extends TestBase {

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> contactSlugsMap;
    private Map<String, Boolean> contactCreatedMap;
    private Map<String, String> dealSlugsMap;
    private Map<String, Boolean> dealCreatedMap;

    private String publicToken;
    private String commonCompanySlug;

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String RELATED_DEALS_SUCCESS_MESSAGE = "Related deals fetched successfully";

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        contactSlugsMap = new HashMap<>();
        contactCreatedMap = new HashMap<>();
        dealSlugsMap = new HashMap<>();
        dealCreatedMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();

        // Create a common company for contacts and deals
        io.rcrm.api.pojo.Company company = new io.rcrm.api.pojo.Company();
        company.setCompany_name("RBAC Test Company " + System.currentTimeMillis());
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        commonCompanySlug = companyResponse.jsonPath().get("slug");

        initializeTracking();
    }

    private void initializeTracking() {
        String[] roles = {"AccountOwner", "Admin", "TeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleNothing"};
        for (String role : roles) {
            contactCreatedMap.put(role, false);
            dealCreatedMap.put(role, false);
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "contactRelatedDealsViewAccessData", groups = {"role-based", "contact-related-deals-access"})
    public void getContactRelatedDeals_Test(String contactCreator, String dealCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        
        // Step 1: Create contact with specified owner
        String contactSlug = ensureContactCreated(contactCreator);
        
        // Step 2: Create deal linked to contact with specified owner
        ensureDealCreatedForContact(dealCreator, contactSlug);
        
        // Step 3: Get executor's token for API call
        String executorToken = albatrossTknMap.get(executor);
        
        // Step 4: Execute GET related deals API
        Response response = getContactRelatedDeals(executorToken, contactSlug);
        
        // Step 5: Validate response based on expected access level
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private Response getContactRelatedDeals(String token, String contactSlug) {
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
        pathParams.put("contactSlug", contactSlug);

        return RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactSlug}/related-deals", 
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
            }
        } else if (actualStatusCode == 403) {
            response.then().body("errors[0].message", anyOf(
                equalTo("Access Denied: User is not authorized to view this contact's data"),
                containsString("Access Denied"),
                containsString("not authorized")
            ));
        }
    }

    private String ensureContactCreated(String creatorRole) {
        Boolean isCreated = contactCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createContactFromRole(creatorRole);
            contactCreatedMap.put(creatorRole, true);
        }
        return contactSlugsMap.get(creatorRole);
    }

    private void createContactFromRole(String role) {
        io.rcrm.api.javafaker.JavaFakerContact faker = new io.rcrm.api.javafaker.JavaFakerContact();
        Contact contact = new Contact(
            faker.getFirstName(),
            faker.getLastName(),
            faker.getEmailID(),
            faker.getContactNumber(),
            commonCompanySlug,
            userIdsMap.get(role),
            userIdsMap.get(role)
        );
        Response response = RestClient.doPost("JSON", baseURL, "contacts", publicToken, null, true, contact);
        assertThat("Failed to create contact for role: " + role + ". Status: " + response.getStatusCode() + 
                ", Response: " + response.getBody().asString(), 
                response.getStatusCode(), equalTo(200));
        String contactSlug = response.jsonPath().get("slug");
        contactSlugsMap.put(role, contactSlug);
    }

    private void ensureDealCreatedForContact(String dealCreatorRole, String contactSlug) {
        String key = dealCreatorRole + "_" + contactSlug;
        Boolean isCreated = dealCreatedMap.get(key);
        if (isCreated == null || !isCreated) {
            createDealForContact(dealCreatorRole, contactSlug);
            dealCreatedMap.put(key, true);
        }
    }

    private void createDealForContact(String role, String contactSlug) {
        Deal deal = new Deal();
        deal.setName("RBAC Test Deal - " + System.currentTimeMillis());
        deal.setDeal_value(10000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setContact_slugs(contactSlug);
        deal.setCompany_slug(commonCompanySlug);
        deal.setOwner_id(String.valueOf(userIdsMap.get(role)));
        deal.setCreated_by(userIdsMap.get(role));

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals", publicToken, null, true, deal);
        assertThat("Failed to create deal for role: " + role + ". Status: " + dealResponse.getStatusCode() + ", Response: " + dealResponse.getBody().asString(), dealResponse.getStatusCode(), equalTo(200));
        
        String key = role + "_" + contactSlug;
        String dealSlug = dealResponse.jsonPath().get("slug");
        dealSlugsMap.put(key, dealSlug);
    }

    @DataProvider(name = "contactRelatedDealsViewAccessData", parallel = false)
    public Object[][] contactRelatedDealsViewAccessData() {
        return new Object[][] {
            // ==================== ACCOUNT OWNER CREATED CONTACT & DEAL (Everything access) ====================
            {"AccountOwner", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner can view contact (Everything) and deals (Everything) - TC001"},
            {"AccountOwner", "AccountOwner", "Admin", 200, "Success", "Admin can view contact (Everything) and deals (Everything) - TC002"},
            {"AccountOwner", "AccountOwner", "TeamMember", 200, "Success", "TeamMember can view contact (Everything) and deals - TC003"},
            {"AccountOwner", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view contact (Owned Only) - TC004"},
            {"AccountOwner", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly can view contact (Team Only) and deals (Team Only) - TC005"},
            {"AccountOwner", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view contact (Nothing) - TC006"},

            // ==================== TEAM MEMBER CREATED CONTACT & DEAL (Owned Only deal access) ====================
            {"TeamMember", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view contact (Everything) and deals (Everything) - TC007"},
            {"TeamMember", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view contact (Everything) and own deals (Owned Only) - TC008"},
            {"TeamMember", "TeamMember", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view contact (Owned Only) - TC009"},

            // ==================== RESTRICTED TEAM MEMBER CREATED CONTACT & DEAL (Owned Only both) ====================
            {"RestrictedTeamMember", "RestrictedTeamMember", "AccountOwner", 200, "Success", "AccountOwner can view contact (Everything) and deals (Everything) - TC010"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "TeamMember", 200, "Success", "TeamMember can view contact (Everything) and deals - TC011"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember can view own contact and own deals - TC012"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly can view contact (Team Only) and deals (Team Only) - TC013"},

            // ==================== CUSTOM ROLE TEAM ONLY CREATED CONTACT & DEAL (Team Only both) ====================
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "AccountOwner", 200, "Success", "AccountOwner can view contact (Everything) and deals (Everything) - TC014"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "TeamMember", 200, "Success", "TeamMember can view contact (Everything) and deals - TC015"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view contact (Owned Only) - TC016"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly can view own contact and own deals (Team Only) - TC017"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view contact (Nothing) - TC018"},

            // ==================== CROSS-CREATOR SCENARIOS (Deal visibility edge cases) ====================
            // AccountOwner contact + TeamMember deal: Test deal ownership visibility
            {"AccountOwner", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view contact and TeamMember's deal (Everything) - TC019"},
            {"AccountOwner", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view contact (Everything) and own deal (Owned Only) - TC020"},

            // RestrictedTeamMember contact + AccountOwner deal: Test contact ownership + deal visibility
            {"RestrictedTeamMember", "AccountOwner", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember can view own contact and AccountOwner's deal - TC021"},
            {"RestrictedTeamMember", "AccountOwner", "TeamMember", 200, "Success", "TeamMember can view contact and AccountOwner's deal - TC022"},
        };
    }
}
