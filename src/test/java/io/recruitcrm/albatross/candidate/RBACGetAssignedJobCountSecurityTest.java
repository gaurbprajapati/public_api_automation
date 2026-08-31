package io.recruitcrm.albatross.candidate;

import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.*;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetAssignedJobCountSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this candidate's data";
    private static final String CANDIDATE_NOT_FOUND_MESSAGE = "Candidate not found";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> candidateIdsMap;
    private String publicToken;
    String companySlug;
    private commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        candidateIdsMap = new HashMap<>();
        
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        companySlug = function.getEntityResponse(baseURL, publicToken, "company");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "assignedJobCountViewAccessData", groups = {"role-based", "assigned-job-count-view-access"})
    public void getAssignedJobCountSecurity_Test(String candidateCreator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        int candidateId = ensureCandidateWithJobCreatedByRole(candidateCreator);
        String executorToken = albatrossTknMap.get(executor);

        String endpoint = "candidates/" + candidateId + "/jobs-assigned-count/get";
        Response response = RestClient.doPost("JSON", albatrossURL, endpoint, executorToken, null, true, null);
        
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
            response.then().body("status", Matchers.containsString("success"));
            response.then().body("message_type", Matchers.containsString("is-success"));
            response.then().body("data.count", notNullValue());
            response.then().body("data.count", greaterThanOrEqualTo(0));
        } else if (expectedStatusCode == 200 && !SUCCESS_MESSAGE.equals(expectedMessage)) {
            // For cases where candidate is not found (returns 200 with empty data)
            response.then().body("data", Matchers.empty());
            response.then().body("message", Matchers.containsString(CANDIDATE_NOT_FOUND_MESSAGE));
            response.then().body("message_type", Matchers.containsString("is-danger"));
        } else if (expectedStatusCode == 401 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            // Some endpoints return 401 for forbidden
            try {
                response.then().body("message", Matchers.anyOf(
                    equalTo(ACCESS_DENIED_MESSAGE),
                    Matchers.containsString("Access Denied"),
                    Matchers.containsString("Unauthorized")
                ));
            } catch (AssertionError e) {
                throw e;
            }
        }
    }

    private int ensureCandidateWithJobCreatedByRole(String creator) {
        if (!candidateIdsMap.containsKey(creator)) {
            createCandidateWithJobFromRole(creator);
        }
        return candidateIdsMap.get(creator);
    }

    private void createCandidateWithJobFromRole(String creator) {
        Response response = function.createEntityByRole(baseURL, publicToken, "candidate", userIdsMap.get(creator));
        String candidateSlug = response.jsonPath().get("slug");
        int candidateId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateSlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        Response jobResponse = function.createEntityByRole(baseURL, publicToken, "job", userIdsMap.get(creator));
        String jobSlug = jobResponse.jsonPath().get("slug");
        function.assignJobToCandidate(baseURL, publicToken, candidateSlug, jobSlug);
        candidateIdsMap.put(creator, candidateId);
    }

    @DataProvider(name = "assignedJobCountViewAccessData", parallel = true)
    public Object[][] assignedJobCountViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "assigned job count for candidate");
    }
}

