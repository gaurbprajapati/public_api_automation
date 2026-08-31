package io.recruitcrm.albatross.candidate;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACCandidateAssignedJobsSecurityTest extends TestBase {

    private final commanFunction function = new commanFunction();
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private String publicToken;
    private String companySlug;
    private Map<String, Integer> candidateJobCombinationMap = new HashMap<>();
    private Map<String, String> jobSlugsMap = new HashMap<>();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this candidate's data";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        companySlug = function.getEntityResponse(baseURL, publicToken, "company");
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "candidateAssignedJobsViewAccessData", groups = {"role-based", "candidate-assigned-jobs-view-access", "candidate_service"})
    public void viewCandidateAssignedJobs_Test(String candidateCreator, String jobCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int candidateId = ensureCandidateWithJobCreator(candidateCreator, jobCreator);
        String executorToken = albatrossTknMap.get(executor);
        
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(candidateId, "", new ArrayList<>());
        Response response = RestClient.doPost("JSON", candidatesURL, "candidate-assigned-job/search/get", executorToken, queryParameters, true, searchPayload);
        
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
            response.then().body("meta.message", Matchers.containsString("Assigned jobs fetched successfully"));
            response.then().body("meta.status", Matchers.is(200));
        } else if (expectedStatusCode == 403 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            response.then().body("message", Matchers.is(ACCESS_DENIED_MESSAGE));
        }
    }

    private JSONObject createSearchPayload(Integer candidateId, String searchTerm, List<Map<String, Object>> sortPriorityList) {
        JSONObject searchPayload = new JSONObject();
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("searchTerm", searchTerm);
        searchPayload.put("candidateId", candidateId);
        return searchPayload;
    }

    private int ensureCandidateWithJobCreator(String candidateCreator, String jobCreator) {
        String cacheKey = candidateCreator + "_" + jobCreator;
        if (!candidateJobCombinationMap.containsKey(cacheKey)) {
            createCandidateWithSpecificJobCreator(candidateCreator, jobCreator, cacheKey);
        }
        return candidateJobCombinationMap.get(cacheKey);
    }

    private void createCandidateWithSpecificJobCreator(String candidateCreator, String jobCreator, String cacheKey) {
        Candidate candidate = new Candidate("RBAC", "Test", userIdsMap.get(candidateCreator), userIdsMap.get(candidateCreator));
        Response candidateResponse = RestClient.doPost("JSON", baseURL, "candidates", publicToken, null, true, candidate);
        
        String candidateSlug = candidateResponse.jsonPath().get("slug");
        int candidateId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateSlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        
        String jobSlug = ensureJobCreatedByRole(jobCreator);
        assignCandidateToJob(candidateSlug, jobSlug);
        candidateJobCombinationMap.put(cacheKey, candidateId);
    }

    private String ensureJobCreatedByRole(String jobCreator) {
        if (!jobSlugsMap.containsKey(jobCreator)) {
            Job job = new Job();
            job.setName("RBAC Test Job " + System.currentTimeMillis());
            job.setCompany_slug(companySlug);
            job.setJob_description_text("RBAC Test Job Description");
            job.setOwner_id(userIdsMap.get(jobCreator));
            job.setCreated_by(userIdsMap.get(jobCreator));
            
            Response jobResponse = RestClient.doPost("JSON", baseURL, "jobs", publicToken, null, true, job);
            jobSlugsMap.put(jobCreator, jobResponse.jsonPath().get("slug"));
        }
        return jobSlugsMap.get(jobCreator);
    }

    private void assignCandidateToJob(String candidateSlug, String jobSlug) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateSlug);

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("job_slug", jobSlug);

        RestClient.doPost1("JSON", baseURL, "candidates/{candidate}/assign", publicToken, queryParameters, pathParameters, true, null);
    }

    // Format: {candidateCreator, jobCreator, executor, expectedStatusCode, expectedMessage, testDescription}
    @DataProvider(name = "candidateAssignedJobsViewAccessData", parallel = true)
    public Object[][] candidateAssignedJobsViewAccessData(ITestContext context) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");
        Object[][] allTestCases = {
            {"AccountOwner", "AccountOwner", "AccountOwner", 200, "Success", "AccountOwner views own candidate with own job - TC001"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", "AccountOwner", 200, "Success", "AccountOwner views RestrictedTeamMember's candidate with CustomRoleTeamOnly's job - TC002"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", "AccountOwner", 200, "Success", "AccountOwner views CustomRoleTeamOnly's candidate with RestrictedTeamMember's job - TC003"},
            {"TeamMember", "RestrictedTeamMember", "Admin", 200, "Success", "Admin views TeamMember's candidate with RestrictedTeamMember's job - TC004"},
            {"RestrictedTeamMember", "AccountOwner", "Admin", 200, "Success", "Admin views RestrictedTeamMember's candidate with AccountOwner's job - TC005"},
            {"CustomRoleTeamOnly", "TeamMember", "Admin", 200, "Success", "Admin views CustomRoleTeamOnly's candidate with TeamMember's job - TC006"},
            {"AccountOwner", "CustomRoleTeamOnly", "TeamMember", 200, "Success", "TeamMember views AccountOwner's candidate with CustomRoleTeamOnly's job - TC007"},
            {"Admin", "RestrictedTeamMember", "TeamMember", 200, "Success", "TeamMember views Admin's candidate with RestrictedTeamMember's job - TC008"},
            {"RestrictedTeamMember", "Admin", "TeamMember", 200, "Success", "TeamMember views RestrictedTeamMember's candidate with Admin's job - TC009"},
            {"AccountOwner", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view AccountOwner's candidate - TC010"},
            {"Admin", "RestrictedTeamMember", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view Admin's candidate - TC011"},
            {"TeamMember", "AccountOwner", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view TeamMember's candidate - TC012"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", "RestrictedTeamMember", 403, "Forbidden", "RestrictedTeamMember cannot view CustomRoleTeamOnly's candidate - TC013"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember views own candidate with own job - TC014"},
            {"RestrictedTeamMember", "AccountOwner", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember views own candidate - job filtered - TC015"},
            {"RestrictedTeamMember", "Admin", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember views own candidate - job filtered - TC016"},
            {"RestrictedTeamMember", "TeamMember", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember views own candidate - job filtered - TC017"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", "RestrictedTeamMember", 200, "Success", "RestrictedTeamMember views own candidate - job filtered - TC018"},
            {"AccountOwner", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly cannot view AccountOwner's candidate - TC019"},
            {"Admin", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly cannot view Admin's candidate - TC020"},
            {"TeamMember", "AccountOwner", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly cannot view TeamMember's candidate - TC021"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 403, "Forbidden", "CustomRoleTeamOnly cannot view RestrictedTeamMember's candidate - TC022"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly views own candidate with own job - TC023"},
            {"CustomRoleTeamOnly", "AccountOwner", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly views own candidate - job filtered - TC024"},
            {"CustomRoleTeamOnly", "Admin", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly views own candidate - job filtered - TC025"},
            {"CustomRoleTeamOnly", "TeamMember", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly views own candidate - job filtered - TC026"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", "CustomRoleTeamOnly", 200, "Success", "CustomRoleTeamOnly views own candidate - job filtered - TC027"},
            {"AccountOwner", "AccountOwner", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view any candidate - TC028"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view any candidate - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "CustomRoleNothing", 403, "Forbidden", "CustomRoleNothing cannot view any candidate - TC030"},
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
