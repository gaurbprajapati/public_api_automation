package io.recruitcrm.albatross.job;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.PrivateApiCommonFunctions;
import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.albatross.jobs.JobUpdateData;
import io.rcrm.api.pojo.albatross.jobs.UpdateJobRequest;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.json.JSONArray;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;


@AccountType("RBAC")
public class RBACJobViewEditDeleteSecurityTest extends TestBase {

    private final JavaFakerCompany fakerCompany = new JavaFakerCompany();
    private final JavaFakerContact fakerContact = new JavaFakerContact();
    private final JavaFakerMails mailsFaker = new JavaFakerMails();
    PrivateApiCommonFunctions privateApiCommonFunctions = new PrivateApiCommonFunctions();


    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> jobSlugsMap;
    private Map<String, Boolean> jobCreatedMap;
    private Map<String, String> entityIdMap; // Cache for entity IDs
    private String publicToken;
    private String ownerAlbatrossToken;
    
    // Common company and contact data
    private String commonCompanySlug;
    private String commonCompanyId;
    private String commonContactSlug;
    private String commonContactId;
    
    // Common job data
    private String jobTitle = "Test Job " + System.currentTimeMillis();

    // Constants for expected messages
    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String JOB_UPDATED_MESSAGE = "Job Updated";
    private static final String JOB_DELETED_MESSAGE = "Job Deleted";
    private static final String FAILED_UPDATE_MESSAGE = "Access Denied";

    @BeforeClass(alwaysRun = true)    public void setup() {
        initializeMaps();
        setupTokensAndUserIds();
        initializeJobTracking();
        createCommonCompany();
        createCommonContact();
    }

    private void initializeMaps() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        jobSlugsMap = new HashMap<>();
        jobCreatedMap = new HashMap<>();
        entityIdMap = new HashMap<>();
    }

    private void setupTokensAndUserIds() {
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        ownerAlbatrossToken = albatrossTknMap.get("AccountOwner");

        // Debug: print role, token and user id
        albatrossTknMap.entrySet().forEach(entry -> 
        System.out.println("Role: " + entry.getKey() + ", Token: " + entry.getValue() + ", User ID: " + userIdsMap.get(entry.getKey())));
    }

    private void initializeJobTracking() {
        String[] roles = {"AccountOwner", "Admin", "TeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleNothing"};
        for (String role : roles) {
            jobCreatedMap.put(role, false);
        }
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

    private void createCommonCompany() {
        io.rcrm.api.pojo.Company apiCompany = new io.rcrm.api.pojo.Company(
            fakerCompany.getCompanyName(), 
            fakerCompany.getUrl(), 
            fakerCompany.getContactNumber(), 
            ""
        );
        apiCompany.setIndustry_id(fakerCompany.getIndustry_id());

        Response response = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, apiCompany);
        commonCompanySlug = response.jsonPath().get("slug");
        commonCompanyId = getEntityId("company", commonCompanySlug);
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
        commonContactId = getEntityId("contact", commonContactSlug);
    }

    // Generic validation method for all response types
    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, 
                                String successField, Object successValue, String forbiddenField, String forbiddenValue) {
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

    private void validateJobResponse(Response response, int expectedStatusCode, String expectedMessage, String jobSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.job.slug", jobSlug, "message", ACCESS_DENIED_MESSAGE);
    }

    private void validateEditJobResponse(Response response, int expectedStatusCode, String expectedMessage, String jobSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.job.slug", jobSlug, "message", FAILED_UPDATE_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", JOB_UPDATED_MESSAGE, "message", FAILED_UPDATE_MESSAGE);
    }

    private void validateDeleteJobResponse(Response response, int expectedStatusCode, String expectedMessage, String entityId, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.id[0]", Integer.parseInt(entityId), "message", ACCESS_DENIED_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", JOB_DELETED_MESSAGE, "message", ACCESS_DENIED_MESSAGE);
    }

    private Response viewJobHelper(String executorToken, String jobSlug) {
        Map<String, String> authTokenMap = new HashMap<String, String>();
        authTokenMap.put("Authorization", "Bearer " + executorToken);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("jobSlug", jobSlug);
        String basePath = "jobs/{jobSlug}/get";

        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, authTokenMap, null, pathParameters, true, null);
        return response;
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "jobViewAccessData", groups = {"role-based", "job-view-access"})
    public void viewJob_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String jobSlug = ensureJobCreated(creator);
        String executorToken = albatrossTknMap.get(executor);

        Response response = viewJobHelper(executorToken, jobSlug);
        validateJobResponse(response, expectedStatusCode, expectedMessage, jobSlug, testDescription);
        if(creator.equals("AccountOwner") && ((executor.equals("TeamMember") || executor.equals("RestrictedTeamMember") || executor.equals("CustomRoleTeamOnly") || executor.equals("CustomRoleNothing")))) {
            System.out.println("entered here for adding collaborator");
            Response response1 = viewJobHelper(ownerAlbatrossToken, jobSlug);
            privateApiCommonFunctions.addCollaboratorToJob(albatrossURL, ownerAlbatrossToken, userIdsMap.get(executor), 2, response1);
            Response response2 = viewJobHelper(executorToken, jobSlug);
            switch (executor) {
                case "TeamMember":
                    validateJobResponse(response2, 200, SUCCESS_MESSAGE, jobSlug, "TeamMember should be able to view the job once added as collaborator");
                    break;
                case "RestrictedTeamMember":
                    validateJobResponse(response2, 200, SUCCESS_MESSAGE, jobSlug, "RestrictedTeamMember should be able to view the job once added as collaborator");
                    break;
                case "CustomRoleTeamOnly":
                    validateJobResponse(response2, 200, SUCCESS_MESSAGE, jobSlug, "CustomRoleTeamOnly should be able to view the job once added as collaborator");
                    break;
                case "CustomRoleNothing":
                    validateJobResponse(response2, 401, ACCESS_DENIED_MESSAGE, jobSlug, "CustomRoleNothing should not be able to view the job once added as collaborator");
                    break;
                default:
                    break;
            }
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "jobEditAccessData", groups = {"role-based", "job-edit-access"})
    public void editJob_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String jobSlug = ensureJobCreated(creator);
        String executorToken = albatrossTknMap.get(executor);
        
        // Get the original job name for updating
        String originalJobName = jobTitle;

        String jobId = getEntityId("job", jobSlug);
        
        // Create job update request using JobUpdateData structure
        JobUpdateData jobData = JobUpdateData.builder()
                .slug(jobSlug)
                .name("Updated " + originalJobName)
                .description("")
                .noofopenings(1)
                .qualificationid(0)
                .specialization("")
                .minexperienceinyears(0)
                .maxexperienceinyears(0)
                .annualsalarymin(0)
                .annualsalarymax(0)
                .salarytype("monthly")
                .job_type("parttime")
                .locality("")
                .city("")
                .country("")
                .postalcode(null)
                .state("")
                .address("")
                .currencyid(53)
                .companyid(Integer.parseInt(commonCompanyId))
                .contactid(Integer.parseInt(commonContactId))
                .details(null)
                .detailfilename(null)
                .allowapply(0)
                .jobcode(null)
                .showcompany(1)
                .showaccountname(0)
                .jobstatus(1)
                .collaborator("")
                .ownerid(userIdsMap.get(executor))
                .jobquestions("")
                .jdtext("Test Job Description")
                .job_category("")
                .job_skill("")
                .pay_rate(0)
                .bill_rate(0)
                .id(Integer.parseInt(jobId))
                .jobpostingstatus(0)
                .jobpostingdate(0)
                .hiring_pipeline_id(0)
                .mapped_pending_job_id(null)
                .build();

        UpdateJobRequest updateJobRequest = UpdateJobRequest.builder()
                .job(jobData)
                .address_changed(false)
                .filesInfo(new Object[]{})
                .deleteJobKey("")
                .secondaryContacts(new Object[]{})
                .xml_feeds(new Object[]{})
                .jobParserData(new Object[]{})
                .collaborator(null) // Set to null instead of new Object()
                .build();

        // Execute edit request
        String basePath = "jobs/" + jobSlug;
        
        Map<String, String> authTokenMap = new HashMap<String, String>();
        authTokenMap.put("Authorization", "Bearer " + executorToken);
        
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, updateJobRequest);
        validateEditJobResponse(response, expectedStatusCode, expectedMessage, jobSlug, testDescription);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "jobDeleteAccessData", groups = {"role-based", "job-delete-access"})
    public void deleteJob_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create job for deletion
        String jobSlug = createJobFromRole(creator);
        String executorToken = albatrossTknMap.get(executor);

        // Get entity ID and execute deletion
        String entityId = getEntityIdFromSlug(jobSlug);

        JSONObject requestBody = new JSONObject();
        requestBody.put("idsToDelete", new JSONArray().put(Integer.parseInt(entityId)));
        requestBody.put("slugsToDelete", new JSONArray().put(jobSlug));
        requestBody.put("tableFlag", "job");

        String basePath = "global/delete-record";
        Response deleteResponse = RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, null, true, requestBody);
        validateDeleteJobResponse(deleteResponse, expectedStatusCode, expectedMessage, entityId, testDescription);
    }

    private String getEntityIdFromSlug(String jobSlug) {
        return getEntityId("job", jobSlug);
    }

    private String ensureJobCreated(String creatorRole) {
        Boolean isCreated = jobCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createJobFromRole(creatorRole);
            jobCreatedMap.put(creatorRole, true);
        }
        return jobSlugsMap.get(creatorRole);
    }

    private String createJobFromRole(String role) {
        // Create job using the public API Job POJO
        io.rcrm.api.pojo.Job job = new io.rcrm.api.pojo.Job(
            jobTitle,           // name
            commonCompanySlug,  // company_slug
            commonContactSlug,  // contact_slug
            1,                  // number_of_openings
            0                   // enable_job_application_form
        );
        job.setJob_description_text("Test Job Description");
        job.setCreated_by(userIdsMap.get(role));
        job.setOwner_id(userIdsMap.get(role));
        
        Response response = RestClient.doPost("JSON", baseURL, "jobs", publicToken, null, true, job);
        response.then().statusCode(200);
        String jobSlug = response.jsonPath().get("slug");
        
        jobSlugsMap.put(role, jobSlug);
        jobCreatedMap.put(role, true);
        return jobSlug;
    }

    @DataProvider(name = "jobViewAccessData", parallel = true)
    public Object[][] jobViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "job");
    }

    @DataProvider(name = "jobEditAccessData", parallel = true)
    public Object[][] jobEditAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getEditAccessData(context, "job");
    }

    @DataProvider(name = "jobDeleteAccessData", parallel = true)
    public Object[][] jobDeleteAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getDeleteAccessData(context, "job");
    }
}
