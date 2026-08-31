package io.recruitcrm.JobService;

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

import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetJobRelatedDealsSecurityTest extends TestBase {

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> jobSlugsMap;
    private Map<String, Boolean> jobCreatedMap;
    private Map<String, String> dealSlugsMap;
    private Map<String, Boolean> dealCreatedMap;

    private String publicToken;
    private String commonCompanySlug;
    private String commonContactSlug;

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String RELATED_DEALS_SUCCESS_MESSAGE = "Related deals fetched successfully";

    @BeforeClass
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        jobSlugsMap = new HashMap<>();
        jobCreatedMap = new HashMap<>();
        dealSlugsMap = new HashMap<>();
        dealCreatedMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();

        io.rcrm.api.pojo.Company company = new io.rcrm.api.pojo.Company();
        company.setCompany_name("RBAC Test Company " + System.currentTimeMillis());
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        commonCompanySlug = companyResponse.jsonPath().get("slug");

        io.rcrm.api.javafaker.JavaFakerContact contactFaker = new io.rcrm.api.javafaker.JavaFakerContact();
        io.rcrm.api.pojo.Contact contact = new io.rcrm.api.pojo.Contact(
            contactFaker.getFirstName(), contactFaker.getLastName(),
            contactFaker.getEmailID(), contactFaker.getContactNumber(),
            commonCompanySlug, userIdsMap.get("AccountOwner"), userIdsMap.get("AccountOwner")
        );
        Response contactResponse = RestClient.doPost("JSON", baseURL, "contacts", publicToken, null, true, contact);
        commonContactSlug = contactResponse.jsonPath().get("slug");

        initializeTracking();
    }

    private void initializeTracking() {
        String[] roles = {"AccountOwner", "Admin", "TeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleNothing"};
        for (String role : roles) {
            jobCreatedMap.put(role, false);
            dealCreatedMap.put(role, false);
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "jobRelatedDealsViewAccessData", groups = {"role-based", "job-related-deals-access", "job_service"})
    public void getJobRelatedDeals_Test(String jobCreator, String dealCreator, String executor,
            int expectedStatusCode, String expectedMessage, String testDescription) {

        String jobSlug = ensureJobCreated(jobCreator);

        ensureDealCreatedForJob(dealCreator, jobSlug);

        String executorToken = albatrossTknMap.get(executor);

        Response response = getJobRelatedDeals(executorToken, jobSlug);

        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private Response getJobRelatedDeals(String token, String jobSlug) {
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
        pathParams.put("jobSlug", jobSlug);

        return RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
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

    private String ensureJobCreated(String creatorRole) {
        Boolean isCreated = jobCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createJobFromRole(creatorRole);
            jobCreatedMap.put(creatorRole, true);
        }
        return jobSlugsMap.get(creatorRole);
    }

    private void createJobFromRole(String role) {
        JavaFakerJob faker = new JavaFakerJob();
        Job job = new Job(
            faker.getJobName(),
            commonCompanySlug,
            commonContactSlug,
            userIdsMap.get(role),
            userIdsMap.get(role),
            faker.getJobCity(),
            faker.getJobDescriptionText()
        );

        Response response = RestClient.doPost("JSON", baseURL, "jobs", publicToken, null, true, job);
        assertThat("Failed to create job for role: " + role + ". Status: " + response.getStatusCode()
                + ", Response: " + response.getBody().asString(),
                response.getStatusCode(), equalTo(200));
        String jobSlug = response.jsonPath().get("slug");
        jobSlugsMap.put(role, jobSlug);
    }

    private void ensureDealCreatedForJob(String dealCreatorRole, String jobSlug) {
        String key = dealCreatorRole + "_" + jobSlug;
        Boolean isCreated = dealCreatedMap.get(key);
        if (isCreated == null || !isCreated) {
            createDealForJob(dealCreatorRole, jobSlug);
            dealCreatedMap.put(key, true);
        }
    }

    private void createDealForJob(String role, String jobSlug) {
        Deal deal = new Deal();
        deal.setName("RBAC Test Deal - " + System.currentTimeMillis());
        deal.setDeal_value(10000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setJob_slug(jobSlug);
        deal.setCompany_slug(commonCompanySlug);
        deal.setOwner_id(String.valueOf(userIdsMap.get(role)));
        deal.setCreated_by(userIdsMap.get(role));

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals", publicToken, null, true, deal);
        assertThat("Failed to create deal for role: " + role + ". Status: " + dealResponse.getStatusCode()
                + ", Response: " + dealResponse.getBody().asString(),
                dealResponse.getStatusCode(), equalTo(200));

        String key = role + "_" + jobSlug;
        String dealSlug = dealResponse.jsonPath().get("slug");
        dealSlugsMap.put(key, dealSlug);
    }

    @DataProvider(name = "jobRelatedDealsViewAccessData", parallel = false)
    public Object[][] jobRelatedDealsViewAccessData() {
        return new Object[][] {
            // ==================== ACCOUNT OWNER CREATED JOB & DEAL (Everything access) ====================
            {"AccountOwner", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner can view job (Everything) and deals (Everything) - TC001"},
            {"AccountOwner", "AccountOwner", "Admin", 200, "Success", "Admin can view job (Everything) and deals (Everything) - TC002"},
            {"AccountOwner", "AccountOwner", "TeamMember", 200, "Success", "TeamMember can view job (Everything) and deals - TC003"},
            {"AccountOwner", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view job (Owned Only) - TC004"},
            {"AccountOwner", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly cannot view job (Team Only) - TC005"},
            {"AccountOwner", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view job (Nothing) - TC006"},

            // ==================== TEAM MEMBER CREATED JOB & DEAL (Owned Only deal access) ====================
            {"TeamMember", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view job (Everything) and deals (Everything) - TC007"},
            {"TeamMember", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view job (Everything) and own deals (Owned Only) - TC008"},
            {"TeamMember", "TeamMember", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view job (Owned Only) - TC009"},

            // ==================== RESTRICTED TEAM MEMBER CREATED JOB & DEAL (Owned Only both) ====================
            {"RestrictedTeamMember", "RestrictedTeamMember", "AccountOwner", 200, "Success", "AccountOwner can view job (Everything) and deals (Everything) - TC010"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "TeamMember", 200, "Success", "TeamMember can view job (Everything) and deals - TC011"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember can view own job and own deals - TC012"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly cannot view job (Team Only) - TC013"},

            // ==================== CUSTOM ROLE TEAM ONLY CREATED JOB & DEAL (Team Only both) ====================
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "AccountOwner", 200, "Success", "AccountOwner can view job (Everything) and deals (Everything) - TC014"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "TeamMember", 200, "Success", "TeamMember can view job (Everything) and deals - TC015"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view job (Owned Only) - TC016"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly can view own job and own deals (Team Only) - TC017"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view job (Nothing) - TC018"},

            // ==================== CROSS-CREATOR SCENARIOS (Deal visibility edge cases) ====================
            {"AccountOwner", "TeamMember", "AccountOwner", 200, "Success", "AccountOwner can view job and TeamMember's deal (Everything) - TC019"},
            {"AccountOwner", "TeamMember", "TeamMember", 200, "Success", "TeamMember can view job (Everything) and own deal (Owned Only) - TC020"},

            {"RestrictedTeamMember", "AccountOwner", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember can view own job and AccountOwner's deal - TC021"},
            {"RestrictedTeamMember", "AccountOwner", "TeamMember", 200, "Success", "TeamMember can view job and AccountOwner's deal - TC022"},
        };
    }
}
