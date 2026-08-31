package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetJobWidgetCountSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this job's data";
    private static final String WIDGET_COUNT_SUCCESS_MESSAGE = "Widget Count fetched successfully";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> jobIdsMap;
    private Map<String, String> jobSlugsMap;

    private String publicToken;
    private String commonCompanySlug;
    private String commonContactSlug;

    @BeforeClass
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        jobIdsMap = new HashMap<>();
        jobSlugsMap = new HashMap<>();

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
    @Test(dataProvider = "jobWidgetCountViewAccessData", groups = {"role-based", "job-widget-count-view-access", "job_service"})
    public void getJobWidgetCount_Test(String jobCreator, String executor,
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int jobId = ensureJobCreatedByRole(jobCreator);
        String jobSlug = jobSlugsMap.get(jobCreator);
        String executorToken = albatrossTknMap.get(executor);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "jobs");
        queryParams.put("recordId", String.valueOf(jobId));
        queryParams.put("recordSlug", jobSlug);

        Response response = RestClient.doGet("JSON", jobServiceURL, "widget-count", executorToken, queryParams, null, true);

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
            response.then().body("meta.message", equalTo(WIDGET_COUNT_SUCCESS_MESSAGE));
            response.then().body("meta.status", equalTo(200));
            response.then().body("data", notNullValue());
        } else if (expectedStatusCode == 403 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            response.then().body("message", equalTo(ACCESS_DENIED_MESSAGE));
        }
    }

    private int ensureJobCreatedByRole(String creator) {
        if (!jobIdsMap.containsKey(creator)) {
            createJobFromRole(creator);
        }
        return jobIdsMap.get(creator);
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

        jobIdsMap.put(creator, jobId);
        jobSlugsMap.put(creator, jobSlug);
    }

    @DataProvider(name = "jobWidgetCountViewAccessData", parallel = true)
    public Object[][] jobWidgetCountViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "widget count for job");
    }
}
