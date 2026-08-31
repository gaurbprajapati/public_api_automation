package io.recruitcrm.albatross.candidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.json.JSONArray;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.EducationHistory;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.pojo.albatross.AddCallLog;
import io.rcrm.api.pojo.albatross.CallLog;
import io.rcrm.api.pojo.albatross.GetActivityData;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACCandidateViewEditDeleteSecurityTest extends TestBase {

    private final JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    private final AllCrudFunctions crudFunctions = new AllCrudFunctions();

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> candidateSlugsMap;
    private Map<String, String> candidateIdsMap; // Cache for candidate IDs fetched from ReaperIntegration
    private Map<String, Boolean> candidateCreatedMap; // Track which roles have created candidates
    private Map<String, Boolean> callLogCreatedMap; // Track which candidates have call logs created
    private Map<String, Boolean> workExperienceCreatedMap; // Track which candidates have work experience created
    private Map<String, Boolean> educationHistoryCreatedMap; // Track which candidates have education history created
    private Map<String, Boolean> hotlistCreatedMap; // Track which candidates have hotlists created
    private Map<String, String> entityIdMap; // Cache for entity IDs
    private String publicToken;
    private int accountId;

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String CANDIDATE_UPDATED_MESSAGE = "Candidate Updated";
    private static final String CANDIDATE_FAILED_UPDATED_MESSAGE = "Update Candidate : Access Denied";
    private static final String CANDIDATE_DELETED_MESSAGE = "Candidate(s) Deleted";
    private static final String CANDIDATE_FAILED_DELETED_MESSAGE = "Access Denied";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        candidateSlugsMap = new HashMap<>();
        candidateIdsMap = new HashMap<>();
        candidateCreatedMap = new HashMap<>();
        callLogCreatedMap = new HashMap<>();
        workExperienceCreatedMap = new HashMap<>();
        educationHistoryCreatedMap = new HashMap<>();
        hotlistCreatedMap = new HashMap<>();
        entityIdMap = new HashMap<>();
        
        // Get account from ThreadManager - tokens and userIds are already available without login
        accountId = ThreadManager.getAccount().getAccountId();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        
        // Setup tokens and user IDs using TestBase method - populates local maps
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);

        // print the role, token and user id
        for (Map.Entry<String, String> entry : albatrossTknMap.entrySet()) {
            System.out.println("Role: " + entry.getKey() + ", Token: " + entry.getValue() + ", User ID: " + userIdsMap.get(entry.getKey()));
        }
        
        // Initialize candidate creation tracking
        initializeCandidateTracking();
    }

    private void initializeCandidateTracking() {
        // Mark all roles as not having created candidates yet
        candidateCreatedMap.put("AccountOwner", false);
        candidateCreatedMap.put("Admin", false);
        candidateCreatedMap.put("TeamMember", false);
        candidateCreatedMap.put("RestrictedTeamMember", false);
        candidateCreatedMap.put("CustomRoleTeamOnly", false);
        candidateCreatedMap.put("CustomRoleNothing", false);
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

    // Generic validation method for all response types
    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, String successField, Object successValue, String forbiddenField, String forbiddenValue) {
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

    private void validateCandidateResponse(Response response, int expectedStatusCode, String expectedMessage, String candidateSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.candidate.slug", candidateSlug, "message", ACCESS_DENIED_MESSAGE);
    }

    // Validation for v2 candidatesURL endpoint (GET /candidates?slug={slug})
    private void validateCandidateV2Response(Response response, int expectedStatusCode, String expectedMessage, String candidateSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.candidates.slug", candidateSlug, "meta.message", "Candidate not found");
    }

    // Validation for contact numbers endpoint (GET /conversation/contact-numbers)
    private void validateContactNumbersResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("primary", Matchers.notNullValue());
                response.then().body("custom", Matchers.notNullValue());
                response.then().body("historical", Matchers.notNullValue());
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
            }
        }
    }

    // Validation for widget count endpoint (GET /candidates/widget-count)
    private void validateWidgetCountResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        // Widget count endpoint returns 404 for RBAC restricted candidates instead of 401
        int expectedCode = expectedStatusCode == 401 ? 404 : expectedStatusCode;
        
        try {
            response.then().statusCode(expectedCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedCode 
                    + " but got: " + actualStatusCode, e);
        }

        if (expectedCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("data.relatedDealsCount", Matchers.notNullValue());
                response.then().body("data.hotlistCount", Matchers.notNullValue());
                response.then().body("data.assignedJobsCount", Matchers.notNullValue());
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
            }
        }
    }

    // Validation for activity data endpoint (POST /expand-activity/get-activity-data)
    private void validateActivityDataResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("status", Matchers.containsString("success"));
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
            }
        }
    }

    // Validation for education history endpoint (GET /candidates/{candidateId}/education-history)
    private void validateEducationHistoryResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("meta", Matchers.notNullValue());
                response.then().body("meta.message", Matchers.is("Education history fetched successfully."));
                response.then().body("data", Matchers.notNullValue());
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
            }
        } else if (expectedStatusCode == 401 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("meta.message", Matchers.is("Access Denied"));
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription 
                        + " - Expected 'Access Denied' but got: " + response.jsonPath().getString("meta.message"), e);
            }
        }
    }

    // Validation for work experience endpoint (GET /candidates/{candidateSlug}/work-history)
    private void validateWorkExperienceResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("$", Matchers.notNullValue());
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
            }
        } else if (expectedStatusCode == 401 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("message", Matchers.is(ACCESS_DENIED_MESSAGE));
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription 
                        + " - Expected 'Access Denied' but got: " + response.jsonPath().getString("message"), e);
            }
        }
    }

    private void validateEditCandidateResponse(Response response, int expectedStatusCode, String expectedMessage, String candidateSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.candidate.slug", candidateSlug, "message", CANDIDATE_FAILED_UPDATED_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", CANDIDATE_UPDATED_MESSAGE, "message", CANDIDATE_FAILED_UPDATED_MESSAGE);
    }

    // Validation for update social links endpoint (POST /candidates/update-default-social-links)
    private void validateUpdateSocialLinksResponse(Response response, int expectedStatusCode, String expectedMessage, int candidateId, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("message", Matchers.is("Social links updated successfully."));
                response.then().body("message_type", Matchers.is("is-success"));
                response.then().body("data", Matchers.notNullValue());
                response.then().body("data.id", Matchers.is(candidateId));
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
            }
        } else if (expectedStatusCode == 401 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("message", Matchers.containsString("Access Denied"));
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription 
                        + " - Expected 'Access Denied' but got: " + response.jsonPath().getString("message"), e);
            }
        }
    }

    // Validation for related hotlists endpoint (POST /hotlists/related-hotlists/search/get)
    private void validateRelatedHotlistsResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("meta", Matchers.notNullValue());
                response.then().body("meta.message", Matchers.is("Related hotlists fetched successfully."));
                response.then().body("data", Matchers.notNullValue());
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
            }
        } else if (expectedStatusCode == 401 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            try {
                response.then().body("meta.message", Matchers.containsString("Access Denied"));
            } catch (AssertionError e) {
                throw new AssertionError("Test Case FAILED: " + testDescription 
                        + " - Expected 'Access Denied' but got: " + response.jsonPath().getString("meta.message"), e);
            }
        }
    }

    private void validateDeleteCandidateResponse(Response response, int expectedStatusCode, String expectedMessage, String entityId, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.id[0]", Integer.parseInt(entityId), "message", CANDIDATE_FAILED_DELETED_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", CANDIDATE_DELETED_MESSAGE, "message", CANDIDATE_FAILED_DELETED_MESSAGE);
    }

    // check for view candidate access (albatross endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-view-access"})
    public void viewCandidate_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        String executorToken = albatrossTknMap.get(executor);
        
        Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candSlug", candidateSlug);
		String basePath = "candidates/{candSlug}/get";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true,null);
        
        validateCandidateResponse(response, expectedStatusCode, expectedMessage, candidateSlug, testDescription);
    }

    // check for view candidate access (v2 candidatesURL endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-view-access-v2", "candidate_service"})
    public void viewCandidateV2_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        String executorToken = albatrossTknMap.get(executor);
        
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("slug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates", executorToken, queryParams, null, true);
        
        validateCandidateV2Response(response, expectedStatusCode, expectedMessage, candidateSlug, testDescription);
    }

    // check for view candidate widget count access (candidatesURL endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-widget-count-access", "candidate_service"})
    public void viewCandidateWidgetCount_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        // Get candidate ID from cache or fetch from ReaperIntegration
        String candidateId = candidateIdsMap.get(candidateSlug);
        if (candidateId == null) {
            candidateId = getEntityId("candidate", candidateSlug);
            candidateIdsMap.put(candidateSlug, candidateId);
        }
        
        String executorToken = albatrossTknMap.get(executor);
        
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("candidateId", candidateId);
        queryParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count", executorToken, queryParams, null, true);
        
        int expectedStatusCodeUpdated = expectedStatusCode == 401 ? 403 : expectedStatusCode;
        String expectedMessageUpdated = expectedMessage == "Forbidden" ? "Access Denied: User is not authorized to view this candidate's data" : expectedMessage;
        validateWidgetCountResponse(response, expectedStatusCodeUpdated, expectedMessageUpdated, testDescription);
    }

    // check for view candidate contact numbers access (commURL endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-contact-numbers-access", "candidate_service"})
    public void getCandidateContactNumbers_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        String executorToken = albatrossTknMap.get(executor);
        
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("entityTypeId", "5"); // 5 = Candidate entity type
        queryParams.put("entitySlug", candidateSlug);

        Response response = RestClient.doGet("JSON", commURL, "conversation/contact-numbers", executorToken, queryParams, null, true);
        
        validateContactNumbersResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    // check for view candidate activity data access (albatross endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-activity-data-access", "candidate_service"})
    public void getCandidateActivityData_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        // Ensure a call log is created for the candidate to have activity data
        String creatorToken = albatrossTknMap.get(creator);
        ensureCallLogCreated(candidateSlug, creatorToken);
        
        String executorToken = albatrossTknMap.get(executor);
        
        String basePath = "expand-activity/get-activity-data";

        GetActivityData getActivityData = new GetActivityData();
        getActivityData.setType("0");
        getActivityData.setPage("detailspage");
        getActivityData.setOffset(0);
        getActivityData.setPagesize(15);
        getActivityData.setRelatedToSlug(candidateSlug);
        getActivityData.setRelatedtotypeid(5); // 5 = Candidate entity type

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, getActivityData);
        
        validateActivityDataResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    // check for view candidate education history access (candidatesURL endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-education-history-access", "candidate_service"})
    public void getCandidateEducationHistory_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        // Ensure education history is created for the candidate
        ensureEducationHistoryCreated(candidateSlug);
        
        // Get candidate ID from cache or fetch from ReaperIntegration
        String candidateId = candidateIdsMap.get(candidateSlug);
        if (candidateId == null) {
            candidateId = getEntityId("candidate", candidateSlug);
            candidateIdsMap.put(candidateSlug, candidateId);
        }
        
        String executorToken = albatrossTknMap.get(executor);
        
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", candidateId);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/{candidateId}/education-history", executorToken, null, pathParams, true);
        int expectedStatusCodeUpdated = expectedStatusCode == 401 ? 403 : expectedStatusCode;
        String expectedMessageUpdated = expectedMessage == "Forbidden" ? "Access Denied: User is not authorized to view this candidate's data" : expectedMessage;
        validateEducationHistoryResponse(response, expectedStatusCodeUpdated, expectedMessageUpdated, testDescription);
    }

    // check for view candidate work experience access (baseURL endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-work-experience-access", "candidate_service"})
    public void getCandidateWorkExperience_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        // Ensure work experience is created for the candidate
        ensureWorkExperienceCreated(candidateSlug);
        
        String executorToken = albatrossTknMap.get(executor);

        // Get candidate ID from cache or fetch from ReaperIntegration
        String candidateId = candidateIdsMap.get(candidateSlug);
        if (candidateId == null) {
            candidateId = getEntityId("candidate", candidateSlug);
            candidateIdsMap.put(candidateSlug, candidateId);
        }
        
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", candidateId);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/{candidateId}/work-history", executorToken, null, pathParams, true);
        
        int expectedStatusCodeUpdated = expectedStatusCode == 401 ? 403 : expectedStatusCode;
        String expectedMessageUpdated = expectedMessage == "Forbidden" ? "Access Denied: User is not authorized to view this candidate's data" : expectedMessage;
        validateWorkExperienceResponse(response, expectedStatusCodeUpdated, expectedMessageUpdated, testDescription);
    }

    // check for view candidate related hotlists access (candidatesURL endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateViewAccessData", groups = {"role-based", "candidate-related-hotlists-access", "candidate_service"})
    public void getCandidateRelatedHotlists_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        // Ensure hotlist is created and candidate is added to it
        ensureHotlistCreated(candidateSlug);
        
        // Get candidate ID from cache or fetch from ReaperIntegration
        String candidateId = candidateIdsMap.get(candidateSlug);
        if (candidateId == null) {
            candidateId = getEntityId("candidate", candidateSlug);
            candidateIdsMap.put(candidateSlug, candidateId);
        }
        
        String executorToken = albatrossTknMap.get(executor);
        
        // Create request body for related hotlists search
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", Integer.parseInt(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get", executorToken, null, null, true, requestBody.toString());
        
        int expectedStatusCodeUpdated = expectedStatusCode == 401 ? 403 : expectedStatusCode;
        String expectedMessageUpdated = expectedMessage == "Forbidden" ? "Access Denied" : expectedMessage;
        validateRelatedHotlistsResponse(response, expectedStatusCodeUpdated, expectedMessageUpdated, testDescription);
    }

    // check for edit candidate access
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateEditAccessData", groups = {"role-based", "candidate-edit-access"})
    public void editCandidate_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        String executorToken = albatrossTknMap.get(executor);

        String updatedFirstName = fakerCandidate.getFirstName();
        String updatedLastName = fakerCandidate.getLastName();
        String updatedEmail = fakerCandidate.getEmailID();

        JSONObject candidate = new JSONObject();
        candidate.put("firstname", updatedFirstName);
        candidate.put("lastname", updatedLastName);
        candidate.put("emailid", updatedEmail);

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidate", candidate);
        requestBody.put("address_changed", false);
        requestBody.put("filesInfo", new JSONObject());
        requestBody.put("deleteResumeKey", "");
        requestBody.put("deleteEducation", new JSONArray());
        requestBody.put("deleteWork", new JSONArray());
        requestBody.put("sovrenData", new JSONArray());

        String basePath = "candidates/" + candidateSlug;
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, null, true, requestBody);

        validateEditCandidateResponse(response, expectedStatusCode, expectedMessage, candidateSlug, testDescription);
    }

    // check for update default social links access (albatross endpoint)
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateEditAccessData", groups = {"role-based", "candidate-update-social-links-access", "candidate_service"})

    public void updateDefaultSocialLinks_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create candidate from creator role if not already created and get the slug
        String candidateSlug = ensureCandidateCreated(creator);
        
        // Get candidate ID from cache or fetch from ReaperIntegration
        String candidateIdStr = candidateIdsMap.get(candidateSlug);
        if (candidateIdStr == null) {
            candidateIdStr = getEntityId("candidate", candidateSlug);
            candidateIdsMap.put(candidateSlug, candidateIdStr);
        }
        int candidateId = Integer.parseInt(candidateIdStr);
        
        String executorToken = albatrossTknMap.get(executor);

        // Create request body for update social links
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com/rbac-test");
        requestBody.put("linkedinUrl", "https://www.linkedin.com/rbac-test");
        requestBody.put("twitterUrl", "https://www.twitter.com/rbac-test");
        requestBody.put("facebookUrl", "https://www.facebook.com/rbac-test");
        requestBody.put("githubUrl", "https://www.github.com/rbac-test");
        requestBody.put("candidateId", candidateId);
        requestBody.put("entityId", 5); // 5 = Candidate entity type
        requestBody.put("socialFieldUrls", new JSONArray());

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links", executorToken, null, null, true, requestBody.toString());

        validateUpdateSocialLinksResponse(response, expectedStatusCode, expectedMessage, candidateId, testDescription);
    }

    // check for delete candidate access
    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidateDeleteAccessData", groups = {"role-based", "candidate-delete-access"})
    public void deleteCandidate_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {

        Candidate candidate = new Candidate("Ajendra", "Singh", userIdsMap.get(creator), userIdsMap.get(creator));
		Response response1 = RestClient.doPost("JSON", baseURL, "candidates", publicToken, null,true, candidate);
        String candidateSlug = response1.jsonPath().get("slug");

        String executorToken = albatrossTknMap.get(executor);

        // Get entity ID from slug using cached method
        String entityId = getEntityId("candidate", candidateSlug);
        
        // Create request body for global delete-record endpoint
        JSONObject requestBody = new JSONObject();
        requestBody.put("idsToDelete", new JSONArray().put(Integer.parseInt(entityId))); // Include the entity ID
        requestBody.put("slugsToDelete", new JSONArray().put(candidateSlug)); // Include the slug as well
        requestBody.put("tableFlag", "candidate");

        // Use the global delete-record endpoint
        String basePath = "global/delete-record";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, null, true, requestBody);
        
        validateDeleteCandidateResponse(response, expectedStatusCode, expectedMessage, entityId, testDescription);
    }

    // Helper method to ensure a candidate is created from a specific role
    private String ensureCandidateCreated(String creatorRole) {
        Boolean isCreated = candidateCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createCandidateFromRole(creatorRole);
            candidateCreatedMap.put(creatorRole, true);
        }
        return candidateSlugsMap.get(creatorRole);
    }

    // Helper method to create a candidate from a role
    private void createCandidateFromRole(String role) {
        Candidate candidate = new Candidate("Ajendra", "Singh", userIdsMap.get(role), userIdsMap.get(role));
		Response response1 = RestClient.doPost("JSON", baseURL, "candidates", publicToken, null,true, candidate);
        candidateSlugsMap.put(role, response1.jsonPath().get("slug"));
    }

    // Helper method to ensure a call log is created for a candidate
    private void ensureCallLogCreated(String candidateSlug, String creatorToken) {
        Boolean isCreated = callLogCreatedMap.get(candidateSlug);
        if (isCreated == null || !isCreated) {
            createCallLogForCandidate(candidateSlug, creatorToken);
            callLogCreatedMap.put(candidateSlug, true);
        }
    }

    // Helper method to create a call log for a candidate
    private void createCallLogForCandidate(String candidateSlug, String creatorToken) {
        CallLog callLog = new CallLog();
        callLog.setCalltype("Outgoing call");
        callLog.setContactnumber("1234567890");
        callLog.setCallfrom("9090909090");
        callLog.setCallto("RBAC Test Candidate");
        callLog.setCallnotes("This is a test call for RBAC");
        callLog.setSubject("RBAC Test Call");
        callLog.setAccountid(accountId);
        callLog.setStartedon(System.currentTimeMillis());
        callLog.setRelatedcandidate(candidateSlug);
        callLog.setPin(0);
        callLog.setCustomcalltypeid(0);
        callLog.setType("3");
        callLog.setDuration("300");

        Map<String, List<Object>> associationsMap = new HashMap<>();
        associationsMap.put("2", new ArrayList<>());
        associationsMap.put("3", new ArrayList<>());
        associationsMap.put("4", new ArrayList<>());
        associationsMap.put("5", new ArrayList<>());
        associationsMap.put("11", new ArrayList<>());

        AddCallLog addCallLog = new AddCallLog();
        addCallLog.setCallLog(callLog);
        addCallLog.setAssociatedData(associationsMap);

        Response response = RestClient.doPost("JSON", albatrossURL, "call-logs", creatorToken, null, true, addCallLog);
        response.then().statusCode(200);
    }

    // Helper method to ensure work experience is created for a candidate
    private void ensureWorkExperienceCreated(String candidateSlug) {
        Boolean isCreated = workExperienceCreatedMap.get(candidateSlug);
        if (isCreated == null || !isCreated) {
            createWorkExperienceForCandidate(candidateSlug);
            workExperienceCreatedMap.put(candidateSlug, true);
        }
    }

    // Helper method to create work experience for a candidate
    private void createWorkExperienceForCandidate(String candidateSlug) {
        WorkHistory workHistory = new WorkHistory();
        workHistory.setCandidate_slug(candidateSlug);
        workHistory.setTitle("Software Engineer");
        workHistory.setWork_company_name("RBAC Test Company");
        workHistory.setEmployment_type(1);
        workHistory.setIndustry_id(1);
        workHistory.setWork_location("New York");
        workHistory.setSalary(100000);
        workHistory.setWork_start_date((int) (System.currentTimeMillis() / 1000) - 31536000); // 1 year ago
        workHistory.setWork_end_date((int) (System.currentTimeMillis() / 1000));
        workHistory.setWork_description("RBAC Test Work Experience");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject(workHistory));

        String basePath = "candidates/work-history/create";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, publicToken, null, null, true, jsonArray);
        response.then().statusCode(200);
    }

    // Helper method to ensure education history is created for a candidate
    private void ensureEducationHistoryCreated(String candidateSlug) {
        Boolean isCreated = educationHistoryCreatedMap.get(candidateSlug);
        if (isCreated == null || !isCreated) {
            createEducationHistoryForCandidate(candidateSlug);
            educationHistoryCreatedMap.put(candidateSlug, true);
        }
    }

    // Helper method to create education history for a candidate
    private void createEducationHistoryForCandidate(String candidateSlug) {
        EducationHistory educationHistory = new EducationHistory();
        educationHistory.setCandidate_slug(candidateSlug);
        educationHistory.setInstitute_name("RBAC Test University");
        educationHistory.setEducational_qualification("Bachelor of Science");
        educationHistory.setEducational_specialization("Computer Science");
        educationHistory.setGrade("A");
        educationHistory.setEducation_location("New York");
        educationHistory.setEducation_start_date((int) (System.currentTimeMillis() / 1000) - 126230400); // 4 years ago
        educationHistory.setEducation_end_date((int) (System.currentTimeMillis() / 1000));
        educationHistory.setEducation_description("RBAC Test Education History");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(educationHistory);

        String basePath = "candidates/education-history/create";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, publicToken, null, null, true, jsonArray);
        response.then().statusCode(200);
    }

    // Helper method to ensure hotlist is created and candidate is added to it
    private void ensureHotlistCreated(String candidateSlug) {
        Boolean isCreated = hotlistCreatedMap.get(candidateSlug);
        if (isCreated == null || !isCreated) {
            createHotlistForCandidate(candidateSlug);
            hotlistCreatedMap.put(candidateSlug, true);
        }
    }

    // Helper method to create a hotlist and add candidate to it
    private void createHotlistForCandidate(String candidateSlug) {
        // Create a hotlist using the public API
        JSONObject hotlistPayload = new JSONObject();
        hotlistPayload.put("name", "RBAC Test Hotlist " + System.currentTimeMillis());
        hotlistPayload.put("related_to_type", "candidate");
        hotlistPayload.put("shared", 1);

        Response hotlistResponse = RestClient.doPost1("JSON", baseURL, "hotlists", publicToken, null, null, true, hotlistPayload.toString());
        hotlistResponse.then().statusCode(200);

        int hotlistId = hotlistResponse.jsonPath().getInt("id");

        // Add candidate to the hotlist
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(candidateSlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, publicToken, null, pathParameters, true, hotlistRelated);
        addResponse.then().statusCode(200);
    }

    @DataProvider(name = "candidateViewAccessData", parallel = true)
    public Object[][] candidateViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "candidate");
    }

    @DataProvider(name = "candidateEditAccessData", parallel = true)
    public Object[][] candidateEditAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getEditAccessData(context, "candidate");
    }

    @DataProvider(name = "candidateDeleteAccessData", parallel = true)
    public Object[][] candidateDeleteAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getDeleteAccessData(context, "candidate");
    }
}