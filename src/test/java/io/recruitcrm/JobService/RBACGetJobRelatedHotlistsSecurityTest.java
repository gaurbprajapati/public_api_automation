package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetJobRelatedHotlistsSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String RELATED_HOTLISTS_SUCCESS_MESSAGE = "Related hotlists fetched successfully.";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> jobIdsMap;
    private Map<String, String> jobSlugsMap;
    private Map<String, Boolean> hotlistCreatedMap;

    private String publicToken;
    private String commonCompanySlug;
    private String commonContactSlug;

    @BeforeClass
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        jobIdsMap = new HashMap<>();
        jobSlugsMap = new HashMap<>();
        hotlistCreatedMap = new HashMap<>();

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
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "jobViewAccessData", groups = {"role-based", "job-related-hotlists-access", "job_service"})
    public void getJobRelatedHotlists_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String jobSlug = ensureJobCreated(creator);

        ensureHotlistCreated(jobSlug);

        Integer jobId = jobIdsMap.get(jobSlug);
        if (jobId == null) {
            jobId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("job", jobSlug)
                    .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
            jobIdsMap.put(jobSlug, jobId);
        }

        String executorToken = albatrossTknMap.get(executor);

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "jobs");
        requestBody.put("recordId", jobId);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "hotlists/related-hotlists/search/get", executorToken, null, null, true, requestBody.toString());

        int expectedStatusCodeUpdated = expectedStatusCode == 401 ? 403 : expectedStatusCode;
        String expectedMessageUpdated = expectedMessage.equals("Forbidden") ? "Access Denied" : expectedMessage;
        validateRelatedHotlistsResponse(response, expectedStatusCodeUpdated, expectedMessageUpdated, testDescription);
    }

    private void ensureHotlistCreated(String jobSlug) {
        Boolean isCreated = hotlistCreatedMap.get(jobSlug);
        if (isCreated == null || !isCreated) {
            createHotlistForJob(jobSlug);
            hotlistCreatedMap.put(jobSlug, true);
        }
    }

    private void createHotlistForJob(String jobSlug) {
        JSONObject hotlistPayload = new JSONObject();
        hotlistPayload.put("name", "RBAC Test Hotlist " + System.currentTimeMillis());
        hotlistPayload.put("related_to_type", "job");
        hotlistPayload.put("shared", 1);

        Response hotlistResponse = RestClient.doPost1("JSON", baseURL, "hotlists", publicToken, null, null, true, hotlistPayload.toString());
        int hotlistStatusCode = hotlistResponse.getStatusCode();
        if (hotlistStatusCode != 200) {
            String errorBody = hotlistResponse.getBody().asString();
            throw new AssertionError("Failed to create hotlist - Expected status 200 but got " + hotlistStatusCode
                    + ". Response body: " + errorBody);
        }

        int hotlistId = hotlistResponse.jsonPath().getInt("id");

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(jobSlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, publicToken, null, pathParameters, true, hotlistRelated);
        addResponse.then().statusCode(200);
    }

    private String ensureJobCreated(String creator) {
        if (!jobSlugsMap.containsKey(creator)) {
            createJobFromRole(creator);
        }
        return jobSlugsMap.get(creator);
    }

    private void createJobFromRole(String creator) {
        JavaFakerJob faker = new JavaFakerJob();
        Job job = new Job(
            faker.getJobName(),
            commonCompanySlug,
            commonContactSlug,
            userIdsMap.get(creator),
            userIdsMap.get(creator),
            faker.getJobCity(),
            faker.getJobDescriptionText()
        );

        Response jobResponse = RestClient.doPost("JSON", baseURL, "jobs", publicToken, null, true, job);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));

        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");
        assertThat("Job Slug should not be null", jobSlug, notNullValue());

        int jobId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("job", jobSlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());

        jobIdsMap.put(jobSlug, jobId);
        jobSlugsMap.put(creator, jobSlug);
    }

    private void validateRelatedHotlistsResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            response.then().body("meta.message", equalTo(RELATED_HOTLISTS_SUCCESS_MESSAGE));
            response.then().body("meta.status", equalTo(200));
            response.then().body("data", notNullValue());
            response.then().body("data", instanceOf(java.util.List.class));
        } else if (expectedStatusCode == 403 && ACCESS_DENIED_MESSAGE.equals(expectedMessage)) {
            response.then().body("errors[0].message", anyOf(
                equalTo("Access Denied: User is not authorized to view this job's data"),
                containsString("Access Denied"),
                containsString("not authorized")
            ));
        }
    }

    @DataProvider(name = "jobViewAccessData", parallel = true)
    public Object[][] jobViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "related hotlists for job");
    }
}
