package io.recruitcrm.CandidateService;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.reaper.ReaperIntegration;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetPitchCandidateContactsSecurityTest extends TestBase {

    private final commanFunction function = new commanFunction();
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> candidateIdsMap;
    private Map<String, String> contactSlugsMap;
    private Map<String, Boolean> pitchCreatedMap;

    private String publicToken;
    private String accountApiKey;
    private String commonCompanySlug;

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this candidate's data";
    private static final String PITCH_CONTACTS_SUCCESS_MESSAGE = "Pitch contacts fetched successfully.";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        candidateIdsMap = new HashMap<>();
        contactSlugsMap = new HashMap<>();
        pitchCreatedMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        accountApiKey = ThreadManager.getAccountApiKey();
        commonCompanySlug = function.getEntityResponse(baseURL, publicToken, "company");
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "pitchCandidateContactsViewAccessData", groups = {"role-based", "pitch-candidate-contacts-view-access", "candidate_service"})
    public void getPitchCandidateContacts_Test(String candidateCreator, String contactCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int candidateId = ensureCandidateWithContactCreator(candidateCreator, contactCreator);
        String executorToken = albatrossTknMap.get(executor);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                executorToken, queryParams, pathParams, true, requestBody.toString());

        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            response.then().body("meta.message", equalTo(PITCH_CONTACTS_SUCCESS_MESSAGE));
            response.then().body("meta.status", equalTo(200));
            response.then().body("data", notNullValue());
            response.then().body("data", instanceOf(java.util.List.class));
        } else if (expectedStatusCode == 403 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            response.then().body("message", equalTo(ACCESS_DENIED_MESSAGE));
        }
    }

    private int ensureCandidateWithContactCreator(String candidateCreator, String contactCreator) {
        String cacheKey = candidateCreator + "_" + contactCreator;
        if (!candidateIdsMap.containsKey(cacheKey)) {
            createCandidateWithSpecificContactCreator(candidateCreator, contactCreator, cacheKey);
        }
        return candidateIdsMap.get(cacheKey);
    }

    private void createCandidateWithSpecificContactCreator(String candidateCreator, String contactCreator, String cacheKey) {
        Candidate candidate = new Candidate("RBAC", "Test", userIdsMap.get(candidateCreator), userIdsMap.get(candidateCreator));
        Response candidateResponse = RestClient.doPost("JSON", baseURL, "candidates", publicToken, null, true, candidate);

        String candidateSlug = candidateResponse.jsonPath().get("slug");
        int candidateId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateSlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());

        String contactSlug = ensureContactCreatedByRole(contactCreator);
        createPitchRelationship(candidateSlug, contactSlug, candidateCreator, contactCreator);
        candidateIdsMap.put(cacheKey, candidateId);
    }

    private String ensureContactCreatedByRole(String contactCreator) {
        if (!contactSlugsMap.containsKey(contactCreator)) {
            Contact contact = new Contact("RBAC", "Test", "rbac.test@example.com", "1234567890", 
                    commonCompanySlug, userIdsMap.get(contactCreator), userIdsMap.get(contactCreator));
            Response contactResponse = RestClient.doPost("JSON", baseURL, "contacts", publicToken, null, true, contact);
            contactSlugsMap.put(contactCreator, contactResponse.jsonPath().get("slug"));
        }
        return contactSlugsMap.get(contactCreator);
    }

    private void createPitchRelationship(String candidateSlug, String contactSlug, String candidateCreator, String contactCreator) {
        String pitchKey = candidateCreator + "_" + contactCreator;
        if (pitchCreatedMap.containsKey(pitchKey)) {
            return;
        }

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateSlug);
        pathParameters.put("contact", contactSlug);

        Response pitchResponse = RestClient.doPost1("JSON", baseURL, "pitch/{candidate}/contact/{contact}", 
                accountApiKey, null, pathParameters, true, null);
        if (pitchResponse.getStatusCode() == 200 || pitchResponse.getStatusCode() == 409) {
            pitchCreatedMap.put(pitchKey, true);
        }
    }

    @DataProvider(name = "pitchCandidateContactsViewAccessData", parallel = true)
    public Object[][] pitchCandidateContactsViewAccessData(ITestContext context) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");
        Object[][] allTestCases = {
            // Format: {candidateCreator, contactCreator, executor, expectedStatusCode, expectedMessage, testDescription}
            
            // ==================== ACCOUNT OWNER CREATED CANDIDATE (Everything access) ====================
            {"AccountOwner", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner views own candidate with own contact - TC001"},
            {"AccountOwner", "AccountOwner", "Admin", 200, "Success", "Admin (Everything) views AccountOwner's candidate - TC002"},
            {"AccountOwner", "AccountOwner", "TeamMember", 200, "Success", "TeamMember (Everything) views AccountOwner's candidate - TC003"},
            {"AccountOwner", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember (Owned Only) cannot view AccountOwner's candidate - TC004"},
            {"AccountOwner", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly (Team Only) cannot view AccountOwner's candidate - TC005"},
            {"AccountOwner", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing (Nothing) cannot view AccountOwner's candidate - TC006"},

            // ==================== RESTRICTED TEAM MEMBER CREATED CANDIDATE (Owned Only access) ====================
            {"RestrictedTeamMember", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner (Everything) views RestrictedTeamMember's candidate - TC007"},
            {"RestrictedTeamMember", "AccountOwner", "Admin", 200, "Success", "Admin (Everything) views RestrictedTeamMember's candidate - TC008"},
            {"RestrictedTeamMember", "AccountOwner", "TeamMember", 200, "Success", "TeamMember (Everything) views RestrictedTeamMember's candidate - TC009"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember views own candidate with own contact - TC010"},
            {"RestrictedTeamMember", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view other's candidate (Owned Only) - TC011"},
            {"RestrictedTeamMember", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly (Team Only) cannot view RestrictedTeamMember's candidate - TC012"},
            {"RestrictedTeamMember", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing (Nothing) cannot view RestrictedTeamMember's candidate - TC013"},

            // ==================== CUSTOM ROLE TEAM ONLY CREATED CANDIDATE (Team Only access) ====================
            {"CustomRoleTeamOnly", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner (Everything) views CustomRoleTeamOnly's candidate - TC014"},
            {"CustomRoleTeamOnly", "AccountOwner", "Admin", 200, "Success", "Admin (Everything) views CustomRoleTeamOnly's candidate - TC015"},
            {"CustomRoleTeamOnly", "AccountOwner", "TeamMember", 200, "Success", "TeamMember (Everything) views CustomRoleTeamOnly's candidate - TC016"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly views own candidate with own contact - TC017"},
            {"CustomRoleTeamOnly", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember (Owned Only) cannot view CustomRoleTeamOnly's candidate - TC018"},
            {"CustomRoleTeamOnly", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing (Nothing) cannot view CustomRoleTeamOnly's candidate - TC019"},

            // ==================== CROSS-CREATOR SCENARIOS (Testing contact ownership filtering) ====================
            {"AccountOwner", "RestrictedTeamMember", "AccountOwner", 200, "Success", "AccountOwner views candidate - contact filtered by RestrictedTeamMember ownership - TC020"},
            {"AccountOwner", "RestrictedTeamMember", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view AccountOwner's candidate - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "AccountOwner", 200, "Success", "AccountOwner views RestrictedTeamMember's candidate - contact filtered - TC022"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "AccountOwner", 200, "Success", "AccountOwner views CustomRoleTeamOnly's candidate - contact filtered - TC023"}
        };
        return filterTestCases(allTestCases, roleParam);
    }

    private Object[][] filterTestCases(Object[][] allTestCases, String roleParam) {
        if (roleParam == null || roleParam.equals("all")) {
            return allTestCases;
        }

        List<Object[]> filtered = new ArrayList<>();
        if (roleParam.contains("-")) {
            String[] parts = roleParam.split("-");
            if (parts.length == 2) {
                for (Object[] row : allTestCases) {
                    if (row[0].equals(parts[0]) && row[2].equals(parts[1])) {
                        filtered.add(row);
                    }
                }
            }
        } else {
            for (Object[] row : allTestCases) {
                if (row[2].equals(roleParam)) {
                    filtered.add(row);
                }
            }
        }
        return filtered.toArray(new Object[0][]);
    }
}