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
public class AICopilotJobPipelineToolTests extends TestBase {
    
    private String albatrossAuthToken;
    private String apiAuthToken;
    private final Faker faker = new Faker();
    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private final io.rcrm.api.commanfunctions.commanFunction commonFunction = new io.rcrm.api.commanfunctions.commanFunction();
    private List<Integer> expectedJobIds;
    private List<String> expectedJobNames;
    private List<Integer> expectedCandidateIds;
    private List<String> expectedPipelineStages;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        expectedJobIds = new ArrayList<>();
        expectedJobNames = new ArrayList<>();
        expectedCandidateIds = new ArrayList<>();
        expectedPipelineStages = new ArrayList<>();
        createTestJobPipelineData();
    }

    private void createTestJobPipelineData() {
        String company1Slug = createCompany("TechCorp_Pipeline1");
        String contact1Slug = createContact("John_Contact_Pipeline1", company1Slug);
        int job1 = createJob("Software_Engineer_Pipeline1", company1Slug, contact1Slug);
        int candidate1 = createCandidate("John_Doe_Pipeline1");
        int candidate2 = createCandidate("Jane_Smith_Pipeline1");
        int candidate3 = createCandidate("Bob_Johnson_Pipeline1");
        
        String company2Slug = createCompany("DataInc_Pipeline2");
        String contact2Slug = createContact("Jane_Contact_Pipeline2", company2Slug);
        int job2 = createJob("Data_Analyst_Pipeline2", company2Slug, contact2Slug);
        int candidate4 = createCandidate("Alice_Brown_Pipeline2");
        int candidate5 = createCandidate("Charlie_Wilson_Pipeline2");
        
        String company3Slug = createCompany("ManageCo_Pipeline3");
        String contact3Slug = createContact("Bob_Contact_Pipeline3", company3Slug);
        int job3 = createJob("Project_Manager_Pipeline3", company3Slug, contact3Slug);
        int candidate6 = createCandidate("David_Lee_Pipeline3");
        
        expectedJobIds.add(job1);
        expectedJobIds.add(job2);
        expectedJobIds.add(job3);
        
        expectedJobNames.add("Software_Engineer_Pipeline1");
        expectedJobNames.add("Data_Analyst_Pipeline2");
        expectedJobNames.add("Project_Manager_Pipeline3");
        
        expectedCandidateIds.add(candidate1);
        expectedCandidateIds.add(candidate2);
        expectedCandidateIds.add(candidate3);
        expectedCandidateIds.add(candidate4);
        expectedCandidateIds.add(candidate5);
        expectedCandidateIds.add(candidate6);
        
        expectedPipelineStages.add("Applied");
        expectedPipelineStages.add("Screening");
        expectedPipelineStages.add("Interview");
        expectedPipelineStages.add("Final Review");
        expectedPipelineStages.add("Offered");
        expectedPipelineStages.add("Hired");
    }

    private int createCandidate(String candidateName) {
        Candidate candidate = new Candidate();
        candidate.setFirst_name(candidateName.split("_")[0]);
        candidate.setLast_name(candidateName.split("_")[1]);
        candidate.setEmail(faker.internet().emailAddress());
        candidate.setContact_number(faker.phoneNumber().phoneNumber());
        candidate.setSkill("Java, Python, SQL, Data Analysis, Project Management");

        Response createResponse = RestClient.doPost("JSON", baseURL, "candidates", apiAuthToken, null, true, candidate);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Failed to create candidate: " + candidateName);

        String candidateSlug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(candidateSlug, "Candidate slug should not be null");

        // Fetch from Albatross to get the Albatross ID
        Response albatrossResponse = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossAuthToken, candidateSlug);
        Assert.assertEquals(albatrossResponse.getStatusCode(), 200, "Failed to fetch candidate from Albatross");
        int albatrossCandidateId = albatrossResponse.jsonPath().getInt("data.candidate.id");
        Assert.assertTrue(albatrossCandidateId > 0, "Albatross candidate ID should be valid");
        return albatrossCandidateId;
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

    private int createJob(String jobName, String companySlug, String contactSlug) {
        Job job = new Job();
        job.setName(jobName);
        job.setCompany_slug(companySlug);
        job.setContact_slug(contactSlug);
        job.setJob_description_text("Test job description for " + jobName + " - Looking for qualified candidates");
        job.setEnable_job_application_form(1);
        job.setCity(faker.address().city());
        job.setJob_type(1);
        job.setMinimum_experience(faker.number().numberBetween(2, 8));
        job.setJob_skill("Java, Python, SQL, Data Analysis, Project Management");
        job.setJob_status("1");
        job.setNumber_of_openings(faker.number().numberBetween(1, 3));

        Response createResponse = RestClient.doPost("JSON", baseURL, "jobs", apiAuthToken, null, true, job);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Failed to create job: " + jobName);

        String jobSlug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(jobSlug, "Job slug should not be null");

        Response albatrossResponse = allCrudFunctions.getJobResponse(albatrossURL, albatrossAuthToken, jobSlug);
        Assert.assertEquals(albatrossResponse.getStatusCode(), 200, "Failed to fetch job from Albatross");
        int albatrossJobId = albatrossResponse.jsonPath().getInt("data.job.id");
        Assert.assertTrue(albatrossJobId > 0, "Albatross job ID should be valid");
        return albatrossJobId;
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
    public void validateJobPipelineAnalysis_GET() {
        String message = "Show me the pipeline for the open job positions";
        Response response = sendAskCopilotRequest(message);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");

        JsonPath toolCall = extractToolCallEvent(response);
        JsonPath context = extractContextEvent(response);
        JsonPath completion = extractCompletionEvent(response);
        JsonPath chatTitle = extractChatTitleEvent(response);

        List<String> toolNames = toolCall.getList("data.tool_name");
        Assert.assertNotNull(toolNames, "Tool names should not be null");
        Assert.assertTrue(toolNames.contains("get_job_statistics_report"), "Expected tool 'get_jobs'");

        String entity = context.getString("data.context.entity");
        Assert.assertEquals(entity, "job_statistics_report", "Expected entity 'jobs'");

        Object records = context.get("data.context.records");
        Assert.assertNotNull(records, "Records should not be null in context");

        List<Integer> actualJobIds = context.getList("data.context.records.id", Integer.class);
        Assert.assertNotNull(actualJobIds, "Job IDs should not be null");
        Assert.assertFalse(actualJobIds.isEmpty(), "Job IDs should not be empty");
        validateCompletionMessage(completion);
        validateChatTitle(chatTitle);
    }
}
