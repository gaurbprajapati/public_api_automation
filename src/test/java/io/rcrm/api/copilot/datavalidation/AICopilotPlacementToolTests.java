package io.rcrm.api.copilot.datavalidation;

import java.util.ArrayList;
import java.util.List;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Job;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AICopilotPlacementToolTests extends TestBase {
    
    private String albatrossAuthToken;
    private String apiAuthToken;
    private final Faker faker = new Faker();
    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private final io.rcrm.api.commanfunctions.commanFunction commonFunction = new io.rcrm.api.commanfunctions.commanFunction();
    private List<String> expectedCandidateSlugs;
    private List<String> expectedJobSlugs;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        expectedCandidateSlugs = new ArrayList<>();
        expectedJobSlugs = new ArrayList<>();
        createTestPlacementsWithAssignmentDates();
    }

    private void createTestPlacementsWithAssignmentDates() {
        String candidate1Slug = createCandidateAndGetSlug("John_Doe_Placement1");
        String company1Slug = createCompany("TechCorp_Placement1");
        String contact1Slug = createContact("Contact_Placement1", company1Slug);
        String job1Slug = createJobAndGetSlug("Software_Engineer_Job1", company1Slug, contact1Slug);
        assignCandidateToJob(candidate1Slug, job1Slug);
        
        String candidate2Slug = createCandidateAndGetSlug("Jane_Smith_Placement2");
        String company2Slug = createCompany("DataInc_Placement2");
        String contact2Slug = createContact("Contact_Placement2", company2Slug);
        String job2Slug = createJobAndGetSlug("Data_Analyst_Job2", company2Slug, contact2Slug);
        assignCandidateToJob(candidate2Slug, job2Slug);
        
        String candidate3Slug = createCandidateAndGetSlug("Bob_Johnson_Placement3");
        String company3Slug = createCompany("ManageCo_Placement3");
        String contact3Slug = createContact("Contact_Placement3", company3Slug);
        String job3Slug = createJobAndGetSlug("Project_Manager_Job3", company3Slug, contact3Slug);
        assignCandidateToJob(candidate3Slug, job3Slug);
        
        String candidate4Slug = createCandidateAndGetSlug("Alice_Brown_Placement4");
        String company4Slug = createCompany("DevOpsCo_Placement4");
        String contact4Slug = createContact("Contact_Placement4", company4Slug);
        String job4Slug = createJobAndGetSlug("DevOps_Engineer_Job4", company4Slug, contact4Slug);
        assignCandidateToJob(candidate4Slug, job4Slug);
        
        expectedCandidateSlugs.add(candidate1Slug);
        expectedCandidateSlugs.add(candidate2Slug);
        expectedCandidateSlugs.add(candidate3Slug);
        expectedCandidateSlugs.add(candidate4Slug);
        
        expectedJobSlugs.add(job1Slug);
        expectedJobSlugs.add(job2Slug);
        expectedJobSlugs.add(job3Slug);
        expectedJobSlugs.add(job4Slug);
    }

    private String createCandidateAndGetSlug(String candidateName) {
        Candidate candidate = new Candidate();
        candidate.setFirst_name(candidateName.split("_")[0]);
        candidate.setLast_name(candidateName.split("_")[1]);
        candidate.setEmail(faker.internet().emailAddress());
        candidate.setContact_number(faker.phoneNumber().phoneNumber());
        candidate.setSkill("Java, Python, SQL");

        Response createResponse = RestClient.doPost("JSON", baseURL, "candidates", apiAuthToken, null, true, candidate);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Failed to create candidate: " + candidateName);

        String candidateSlug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(candidateSlug, "Candidate slug should not be null");
        return candidateSlug;
    }

    private String createCompany(String companyName) {
        Response companyResponse = commonFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        Assert.assertEquals(companyResponse.getStatusCode(), 200, "Failed to create company: " + companyName);
        String companySlug = companyResponse.jsonPath().getString("slug");
        Assert.assertNotNull(companySlug, "Company slug should not be null");
        return companySlug;
    }

    private String createContact(String contactName, String companySlug) {
        Response contactResponse = commonFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        Assert.assertEquals(contactResponse.getStatusCode(), 200, "Failed to create contact: " + contactName);
        String contactSlug = contactResponse.jsonPath().getString("slug");
        Assert.assertNotNull(contactSlug, "Contact slug should not be null");
        return contactSlug;
    }

    private String createJobAndGetSlug(String jobName, String companySlug, String contactSlug) {
        Job job = new Job();
        job.setName(jobName);
        job.setCompany_slug(companySlug);
        job.setContact_slug(contactSlug);
        job.setJob_description_text("Test job description for " + jobName);
        job.setEnable_job_application_form(1);
        job.setCity(faker.address().city());
        job.setJob_type(1);
        job.setMinimum_experience(faker.number().numberBetween(2, 8));
        job.setJob_skill("Java, Python, SQL");
        job.setJob_status("1");

        Response createResponse = RestClient.doPost("JSON", baseURL, "jobs", apiAuthToken, null, true, job);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Failed to create job: " + jobName);

        String jobSlug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(jobSlug, "Job slug should not be null");
        return jobSlug;
    }

    private void assignCandidateToJob(String candidateSlug, String jobSlug) {
        java.util.Map<String, String> pathParameters = new java.util.HashMap<>();
        pathParameters.put("candidate", candidateSlug);
        
        java.util.Map<String, String> queryParameters = new java.util.HashMap<>();
        queryParameters.put("job_slug", jobSlug);
        
        String basePath = "candidates/{candidate}/assign";
        
        Response assignResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, queryParameters, pathParameters, true, null);
        Assert.assertEquals(assignResponse.getStatusCode(), 200, "Failed to assign candidate " + candidateSlug + " to job " + jobSlug);
        
        String assignedCandidateSlug = assignResponse.jsonPath().getString("candidate_slug");
        String assignedJobSlug = assignResponse.jsonPath().getString("job_slug");
        Assert.assertEquals(assignedCandidateSlug, candidateSlug, "Assigned candidate slug mismatch");
        Assert.assertEquals(assignedJobSlug, jobSlug, "Assigned job slug mismatch");
    }

    private Response sendAskCopilotRequest(String message) {
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("uuid", "");
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/ask", albatrossAuthToken, null, true, payload);
    }

    private List<String> extractJsonEvents(String responseBody) {
        List<String> events = new ArrayList<>();
        int depth = 0;
        int startIdx = -1;
        for (int i = 0; i < responseBody.length(); i++) {
            char c = responseBody.charAt(i);
            if (c == '{') {
                if (depth == 0) startIdx = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && startIdx != -1) {
                    String jsonStr = responseBody.substring(startIdx, i + 1);
                    events.add(jsonStr);
                    startIdx = -1;
                }
            }
        }
        return events;
    }

    private JsonPath extractToolCallEvent(Response response) {
        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "Response body should not be null");
        Assert.assertFalse(responseBody.trim().isEmpty(), "Response body should not be empty");
        List<String> events = extractJsonEvents(responseBody);
        Assert.assertTrue(events.size() > 0, "Expected at least 1 event in response. Found: " + events.size());
        
        for (String event : events) {
            JsonPath jp = new JsonPath(event);
            String eventName = jp.getString("data.event_name");
            if ("tools".equals(eventName) && jp.get("data.tool_name") != null) {
                return jp;
            }
        }
        Assert.fail("Tool call event with 'tools' event_name and tool_name not found in response");
        return null;
    }

    private JsonPath extractContextEvent(Response response) {
        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "Response body should not be null");
        Assert.assertFalse(responseBody.trim().isEmpty(), "Response body should not be empty");
        List<String> events = extractJsonEvents(responseBody);
        
        for (String event : events) {
            JsonPath jp = new JsonPath(event);
            String eventName = jp.getString("data.event_name");
            Object context = jp.get("data.context");
            Object records = jp.get("data.context.records");
            if ("tools".equals(eventName) && context != null && records != null && !"null".equals(String.valueOf(records))) {
                List<Object> recordsList = jp.getList("data.context.records");
                if (recordsList != null && !recordsList.isEmpty()) {
                    return jp;
                }
            }
        }
        Assert.fail("Context event with 'tools' event_name and non-null records not found in response");
        return null;
    }

    private JsonPath extractCompletionEvent(Response response) {
        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "Response body should not be null");
        Assert.assertFalse(responseBody.trim().isEmpty(), "Response body should not be empty");
        int completeMessageIndex = responseBody.lastIndexOf("\"complete_message\"");
        Assert.assertTrue(completeMessageIndex != -1, "Completion event with 'complete_message' not found in response");
        int dataIndex = responseBody.lastIndexOf("\"data\"", completeMessageIndex);
        int startIndex = responseBody.lastIndexOf("{", dataIndex - 1);
        int endIndex = responseBody.lastIndexOf("}") + 1;
        return new JsonPath(responseBody.substring(startIndex, endIndex));
    }

    private JsonPath extractChatTitleEvent(Response response) {
        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "Response body should not be null while validating chat title");
        Assert.assertTrue(responseBody.contains("chat_title_generated"), "Response should contain chat_title_generated event");
        List<String> events = extractJsonEvents(responseBody);
        JsonPath chatTitleJson = null;
        for (int i = events.size() - 1; i >= 0; i--) {
            JsonPath jp = new JsonPath(events.get(i));
            String eventName = jp.getString("data.event_name");
            if ("chat_title".equals(eventName)) {
                chatTitleJson = jp;
                break;
            }
        }
        Assert.assertNotNull(chatTitleJson, "Chat title event should be found in response");
        return chatTitleJson;
    }

    private void validateCompletionMessage(JsonPath completion) {
        Assert.assertEquals(completion.getString("data.event"), "completion", "Expected event to be 'completion'");
        Assert.assertEquals(completion.getString("data.event_name"), "complete_response", "Expected event_name to be 'complete_response'");
        String completeMessage = completion.getString("data.complete_message");
        Assert.assertNotNull(completeMessage, "Complete message should not be null");
        Assert.assertFalse(completeMessage.trim().isEmpty(), "Complete message should not be empty");
        Assert.assertEquals(completion.getString("meta.message"), "Complete message", "Meta message should be 'Complete message'");
        Assert.assertEquals(completion.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(completion.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(completion.getString("meta.request_UUID"), "Meta request_UUID should not be null");
    }

    private void validateChatTitle(JsonPath chatTitle) {
        Assert.assertEquals(chatTitle.getString("data.event"), "chat_title_generated", "Expected event to be 'chat_title_generated'");
        Assert.assertEquals(chatTitle.getString("data.event_name"), "chat_title", "Expected event_name to be 'chat_title'");
        String title = chatTitle.getString("data.chat_title");
        Assert.assertNotNull(title, "Chat title should not be null");
        Assert.assertFalse(title.trim().isEmpty(), "Chat title should not be empty");
        Assert.assertEquals(chatTitle.getString("meta.message"), "chat_title", "Meta message should be 'chat_title'");
        Assert.assertEquals(chatTitle.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(chatTitle.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(chatTitle.getString("meta.request_UUID"), "Meta request_UUID should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void validateAveragePlacementTime_GET() {
        String message = "Show me the average time it takes to place a candidate from the date they are assigned to a job for all jobs";
        Response response = sendAskCopilotRequest(message);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");

        JsonPath toolCall = extractToolCallEvent(response);
        JsonPath context = extractContextEvent(response);
        JsonPath completion = extractCompletionEvent(response);
        JsonPath chatTitle = extractChatTitleEvent(response);

        Object records = context.get("data.context.records");
        Assert.assertNotNull(records, "Records should not be null in context");

        String entity = context.getString("data.context.entity");
        
        if ("jobs".equals(entity)) {
            List<String> actualJobSlugs = context.getList("data.context.records.slug", String.class);
            Assert.assertNotNull(actualJobSlugs, "Job slugs should not be null");
            Assert.assertFalse(actualJobSlugs.isEmpty(), "Job slugs should not be empty");
            
            Assert.assertTrue(actualJobSlugs.size() >= expectedJobSlugs.size(), 
                "Should have at least " + expectedJobSlugs.size() + " jobs, but found: " + actualJobSlugs.size());
            Assert.assertTrue(actualJobSlugs.containsAll(expectedJobSlugs), 
                "All expected job slugs should be present in results. Expected: " + expectedJobSlugs + " | Actual: " + actualJobSlugs);
        } else {
            List<String> actualCandidateSlugs = context.getList("data.context.records.candidateslug", String.class);
            Assert.assertNotNull(actualCandidateSlugs, "Candidate slugs should not be null");
            Assert.assertFalse(actualCandidateSlugs.isEmpty(), "Candidate slugs should not be empty");
            
            Assert.assertTrue(actualCandidateSlugs.size() >= expectedCandidateSlugs.size(), 
                "Should have at least " + expectedCandidateSlugs.size() + " candidates, but found: " + actualCandidateSlugs.size());
            Assert.assertTrue(actualCandidateSlugs.containsAll(expectedCandidateSlugs), 
                "All expected candidate slugs should be present in results. Expected: " + expectedCandidateSlugs + " | Actual: " + actualCandidateSlugs);
        }

        validateCompletionMessage(completion);
        validateChatTitle(chatTitle);
    }
}
