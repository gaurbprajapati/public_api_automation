package io.recruitcrm.albatross.candidate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.annotations.*;

import com.qa.api.util.reaper.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetCandidateWorkHistorySecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String WORK_HISTORY_FETCHED_MESSAGE = "Candiate Work History Fetched Successfully.";
    private static final String NO_ACCESS_MESSAGE = "You don't have access to read the candidate data";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> candidateIdsMap;
    private String publicToken;
    private commanFunction function = new commanFunction();
    private JavaFakerCandidate faker = new JavaFakerCandidate();
    private String basePath = "candidates/candidate-work/{id}";

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        candidateIdsMap = new HashMap<>();
        
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "workHistoryViewAccessData", groups = {"role-based", "work-history-view-access"})
    public void getCandidateWorkHistoryRBAC_Test(String candidateCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int candidateId = ensureCandidateWithWorkHistoryCreatedByRole(candidateCreator);
        String executorToken = albatrossTknMap.get(executor);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", String.valueOf(candidateId));

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParams, true);
        
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
            response.then().body("message", equalTo(WORK_HISTORY_FETCHED_MESSAGE));
            response.then().body("data", is(not(empty())));
        } else if (expectedStatusCode == 200 && !SUCCESS_MESSAGE.equals(expectedMessage)) {
            System.out.println("Message: " + testDescription);
            response.then().body("data", is(empty()));
            response.then().body("message", equalTo(NO_ACCESS_MESSAGE));
        } 
    }

    private int ensureCandidateWithWorkHistoryCreatedByRole(String creator) {
        if (!candidateIdsMap.containsKey(creator)) {
            createCandidateWithWorkHistoryFromRole(creator);
        }
        return candidateIdsMap.get(creator);
    }

    private void createCandidateWithWorkHistoryFromRole(String creator) {
        String creatorToken = albatrossTknMap.get(creator);
        
        Response candidateResponse = function.createEntityByRole(baseURL, publicToken, "candidate", userIdsMap.get(creator));
        String candidateSlug = candidateResponse.jsonPath().get("slug");
        assertThat("Candidate Slug should not be null", candidateSlug, notNullValue());
        
        int candidateId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateSlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        
        createWorkHistoryForCandidate(candidateId, candidateSlug, creatorToken);
        
        candidateIdsMap.put(creator, candidateId);
    }

    private void createWorkHistoryForCandidate(int candidateId, String candidateSlug, String creatorToken) {
        String workCompanyName = faker.getWorkCompanyName();
        String title = faker.getJobTitle();
        int employmentType = faker.getEmploymentType();
        int industryId = faker.getIndustryId();
        String workLocation = faker.getWorkLocation();
        int isCurrentlyWorking = faker.currentlyWorking();
        int workStartDate = faker.getStartDate();
        int workEndDate = faker.getEndDateWithReferenceDate(workStartDate);
        String workDescription = faker.getCandidateSummary();
        int salary = faker.getSalary();

        WorkHistory workHistory = new WorkHistory(candidateSlug, workCompanyName, title, employmentType, 
            industryId, workLocation, isCurrentlyWorking, workStartDate, workEndDate, workDescription, salary);
        workHistory.setCandidate_id(candidateId);

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-work/create", 
            creatorToken, null, null, true, workHistory);
        
        assertThat("Failed to create work history", response.getStatusCode(), equalTo(200));
        assertThat("Work history creation message", response.jsonPath().getString("message"), 
            equalTo("Candidate Work History is created successfully."));
    }

    @DataProvider(name = "workHistoryViewAccessData", parallel = true)
    public Object[][] workHistoryViewAccessData(ITestContext context) {
        return new Object[][] {
            {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can access candidate work history created by Account Owner - TC001"},
            {"AccountOwner", "Admin", 200, "Success", "Admin can access candidate work history created by Account Owner - TC002"},
            {"AccountOwner", "TeamMember", 200, "Success", "Team Member can access candidate work history created by Account Owner - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 200, "Fail", "Restricted Team Member can access candidate work history created by Account Owner - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 200, "Fail", "Custom Role Team Only can access candidate work history created by Account Owner - TC005"},
            {"AccountOwner", "CustomRoleNothing", 200, "Fail", "Custom Role Nothing can access candidate work history created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", 200, "Success", "Account Owner can access candidate work history created by Admin - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin can access candidate work history created by Admin - TC008"},
            {"Admin", "TeamMember", 200, "Success", "Team Member can access candidate work history created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", 200, "Fail", "Restricted Team Member can access candidate work history created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", 200, "Fail", "Custom Role Team Only can access candidate work history created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", 200, "Fail", "Custom Role Nothing can access candidate work history created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can access candidate work history created by Team Member - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin can access candidate work history created by Team Member - TC014"},
            {"TeamMember", "TeamMember", 200, "Success", "Team Member can access candidate work history created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", 200, "Fail", "Restricted Team Member can access candidate work history created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 200, "Fail", "Custom Role Team Only can access candidate work history created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", 200, "Fail", "Custom Role Nothing can access candidate work history created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can access candidate work history created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can access candidate work history created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", 200, "Success", "Team Member can access candidate work history created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access candidate work history created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 200, "Fail", "Custom Role Team Only can access candidate work history created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 200, "Fail", "Custom Role Nothing can access candidate work history created by Restricted Team Member - TC024"},

            // Custom Role Team Only created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can access candidate work history created by Custom Role Team Only - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can access candidate work history created by Custom Role Team Only - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 200, "Success", "Team Member can access candidate work history created by Custom Role Team Only - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 200, "Fail", "Restricted Team Member can access candidate work history created by Custom Role Team Only - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access candidate work history created by Custom Role Team Only - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 200, "Fail", "Custom Role Nothing can access candidate work history created by Custom Role Team Only - TC030"},
        };

    }
}

