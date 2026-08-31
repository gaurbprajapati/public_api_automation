package io.recruitcrm.albatross.job;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC2LevelAccessDataProvider;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerMails;
import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.albatross.jobs.CreateJob;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACJobCreateSecurityTest extends TestBase {
    private final JavaFakerCompany fakerCompany = new JavaFakerCompany();
    private final JavaFakerContact fakerContact = new JavaFakerContact();
    private final JavaFakerMails mailsFaker = new JavaFakerMails();
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private String commonCompanySlug;
    private String commonCompanyId;
    private String commonContactSlug;
    private String publicToken;

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    
    // Common job data
    private String jobTitle = "Test Job " + System.currentTimeMillis();

    @BeforeClass(alwaysRun = true)    public void setupToken() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        
        // Create common company and contact for all jobs
        createCommonCompany();
        createCommonContact();
    }

    private void createCommonCompany() {
        Company apiCompany = new Company(
            fakerCompany.getCompanyName(), 
            fakerCompany.getUrl(), 
            fakerCompany.getContactNumber(), 
            ""
        );
        apiCompany.setIndustry_id(fakerCompany.getIndustry_id());

        Response response = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, apiCompany);
        commonCompanySlug = response.jsonPath().get("slug");
        commonCompanyId = ReaperIntegration.getEntityIdFromSlug("company", commonCompanySlug).getBody().asString().replace("Corresponding entity for the slug is : ", "").trim();
    }

    private void createCommonContact() {
        // Create contact using the public API Contact POJO
        io.rcrm.api.pojo.Contact contact = new io.rcrm.api.pojo.Contact(
            fakerContact.getFirstName(), 
            fakerContact.getLastName(), 
            mailsFaker.getFakeEmail(), 
            fakerContact.getContactNumber(), 
            commonCompanySlug,  // company_slug parameter
            userIdsMap.get("AccountOwner"),  // owner_id
            userIdsMap.get("AccountOwner")   // created_by
        );
        
        Response response = RestClient.doPost("JSON", baseURL, "contacts", publicToken, null, true, contact);
        commonContactSlug = response.jsonPath().get("slug");
    }

    private Response createJob(String token, String role) {
        // Create job using the CreateJob POJO for Albatross API
        CreateJob.Job job = new CreateJob.Job(
            jobTitle,           // name
            commonCompanyId,    // companyid (using ID instead of slug for Albatross)
            userIdsMap.get(role)// ownerid
        );
        
        // Set additional job properties
        job.setDescription("Test Job Description");
        
        CreateJob createJobRequest = new CreateJob(job);
        
        return RestClient.doPost("JSON", albatrossURL, "jobs", token, null, true, createJobRequest);
    }

    private void validateJobResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
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
                    response.then().body("data.job.slug", Matchers.notNullValue());
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
                }
            }
        } else if (expectedStatusCode == 401) {
            if (FORBIDDEN_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body("message", Matchers.is(ACCESS_DENIED_MESSAGE));
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected '" + ACCESS_DENIED_MESSAGE + "' but got: " + response.jsonPath().getString("message"), e);
                }
            }
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "job2LevelCreateAccessData", groups = {"role-based", "job-2level-create-access"})
    public void createJobSecurityTest(String role, String access, int expectedStatusCode, String expectedMessage, String testDescription) {
        String roleToken = albatrossTknMap.get(role);
        Response createResponse = createJob(roleToken, role);
        validateJobResponse(createResponse, expectedStatusCode, expectedMessage, testDescription);
    }

    @DataProvider(name = "job2LevelCreateAccessData", parallel = true)
    public Object[][] job2LevelCreateAccessData(ITestContext context) {
        return RBAC2LevelAccessDataProvider.getJobAccessData(context);
    }
}