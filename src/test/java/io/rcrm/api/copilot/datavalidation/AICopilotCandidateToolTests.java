package io.rcrm.api.copilot.datavalidation;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.WorkHistory;
import org.json.JSONArray;
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
public class AICopilotCandidateToolTests extends TestBase {
    private String albatrossAuthToken;
    private String apiAuthToken;
    private final Faker faker = new Faker();
    private final AllCrudFunctions function = new AllCrudFunctions();
    private List<Integer> expectedCandidatesLast10Days;
    private List<Integer> expectedCandidatesWithJavaSkill;
    private List<Integer> expectedCandidatesWithGoogleOrApple;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createTestCandidatesForDateFilters();
        createTestCandidatesForSkillFilters();
        createTestCandidatesForCompanyFilters();
    }

    private void createTestCandidatesForDateFilters() {
        LocalDate today = LocalDate.now();
        int candidate1 = createCandidate("Test_10Days_1", today.minusDays(5), null, null);
        int candidate2 = createCandidate("Test_10Days_2", today.minusDays(8), null, null);
        expectedCandidatesLast10Days = Arrays.asList(candidate1, candidate2);
    }

    private void createTestCandidatesForSkillFilters() {
        LocalDate today = LocalDate.now();
        int javaCandidate1 = createCandidate("JavaDev_1", today.minusDays(2), "Java", null);
        int javaCandidate2 = createCandidate("JavaDev_2", today.minusDays(15), "Java, Python", null);
        expectedCandidatesWithJavaSkill = Arrays.asList(javaCandidate1, javaCandidate2);
    }

    private void createTestCandidatesForCompanyFilters() {
        LocalDate today = LocalDate.now();
        int googleCandidate = createCandidateWithWorkHistory("Google_Engineer", today.minusDays(5), "Java", "Google");
        int appleCandidate = createCandidateWithWorkHistory("Apple_Developer", today.minusDays(7), "Swift, Java", "Apple");
        expectedCandidatesWithGoogleOrApple = Arrays.asList(googleCandidate, appleCandidate);
    }

    private int createCandidate(String firstName, LocalDate createdDate, String skills, String company) {
        Candidate candidate = new Candidate();
        candidate.setFirst_name(firstName);
        candidate.setLast_name(faker.name().lastName());
        candidate.setEmail(faker.internet().emailAddress());
        candidate.setContact_number(faker.phoneNumber().phoneNumber());
        if (skills != null) candidate.setSkill(skills);
        Response createResponse = RestClient.doPost("JSON", baseURL, "candidates", apiAuthToken, null, true, candidate);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Failed to create candidate: " + firstName);
        String candidateSlug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(candidateSlug, "Candidate slug should not be null");
        Response albatrossResponse = function.getCandidateResponse(albatrossURL, albatrossAuthToken, candidateSlug);
        Assert.assertEquals(albatrossResponse.getStatusCode(), 200, "Failed to fetch candidate from Albatross");
        int albatrossCandidateId = albatrossResponse.jsonPath().getInt("data.candidate.id");
        Assert.assertTrue(albatrossCandidateId > 0, "Albatross candidate ID should be valid");
        return albatrossCandidateId;
    }

    private int createCandidateWithWorkHistory(String firstName, LocalDate createdDate, String skills, String companyName) {
        Candidate candidate = new Candidate();
        candidate.setFirst_name(firstName);
        candidate.setLast_name(faker.name().lastName());
        candidate.setEmail(faker.internet().emailAddress());
        candidate.setContact_number(faker.phoneNumber().phoneNumber());
        if (skills != null) candidate.setSkill(skills);
        Response createResponse = RestClient.doPost("JSON", baseURL, "candidates", apiAuthToken, null, true, candidate);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Failed to create candidate: " + firstName);
        String candidateSlug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(candidateSlug, "Candidate slug should not be null");
        Map<String, String> pathParams = new HashMap<>();
        String basePath = "candidates/work-history/create";
        WorkHistory workHistory = new WorkHistory();
        workHistory.setCandidate_slug(candidateSlug);
        workHistory.setWork_company_name(companyName);
        workHistory.setTitle("Software Engineer");
        workHistory.setEmployment_type(1);
        workHistory.setIndustry_id(1);
        workHistory.setWork_location("USA");
        workHistory.setIs_currently_working(0);
        workHistory.setWork_start_date((int) (System.currentTimeMillis() / 1000) - 365 * 24 * 60 * 60);
        workHistory.setWork_end_date((int) (System.currentTimeMillis() / 1000) - 180 * 24 * 60 * 60);
        workHistory.setSalary(100000);
        workHistory.setWork_description("Worked as Software Engineer");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(workHistory);
        Response workHistoryResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParams, true, jsonArray);
        Assert.assertEquals(workHistoryResponse.getStatusCode(), 200, "Failed to add work history for candidate: " + firstName + " at company: " + companyName);
        Response albatrossResponse = function.getCandidateResponse(albatrossURL, albatrossAuthToken, candidateSlug);
        Assert.assertEquals(albatrossResponse.getStatusCode(), 200, "Failed to fetch candidate from Albatross");
        int albatrossCandidateId = albatrossResponse.jsonPath().getInt("data.candidate.id");
        Assert.assertTrue(albatrossCandidateId > 0, "Albatross candidate ID should be valid");
        return albatrossCandidateId;
    }

    private Response sendAskCopilotRequest(String message) {
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("uuid", "");
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/ask", albatrossAuthToken, null, true, payload);
    }

    private List<String> extractJsonEvents(String responseBody) {
        List<String> events = new java.util.ArrayList<>();
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
        Assert.assertTrue(events.size() > 1, "Expected at least 2 events in response. Found: " + events.size());
        String toolCallJson = events.get(1);
        JsonPath jp = new JsonPath(toolCallJson);
        String eventName = jp.getString("data.event_name");
        Assert.assertEquals(eventName, "tools", "Expected event_name 'tools' in 2nd event (tool call)");
        List<String> toolNames = jp.getList("data.tool_name");
        Assert.assertNotNull(toolNames, "Tool name should exist in 2nd event");
        return jp;
    }

    private JsonPath extractContextEvent(Response response) {
        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "Response body should not be null");
        Assert.assertFalse(responseBody.trim().isEmpty(), "Response body should not be empty");
        List<String> events = extractJsonEvents(responseBody);
        Assert.assertTrue(events.size() > 2, "Expected at least 3 events in response. Found: " + events.size());
        String contextJson = events.get(2);
        JsonPath jp = new JsonPath(contextJson);
        String eventName = jp.getString("data.event_name");
        Assert.assertEquals(eventName, "tools", "Expected event_name 'tools' in 3rd event (context)");
        Object context = jp.get("data.context");
        Assert.assertNotNull(context, "Context should exist in 3rd event");
        return jp;
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

    @Owner("Smit Patel")
    @Test
    public void validateCandidatesWithJavaSkill_GET() {
        String message = "Find candidates with Java programming skills";
        Response response = sendAskCopilotRequest(message);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");
        JsonPath toolCall = extractToolCallEvent(response);
        JsonPath context = extractContextEvent(response);
        JsonPath completion = extractCompletionEvent(response);
        JsonPath chatTitle = extractChatTitleEvent(response);
        List<String> toolNames = toolCall.getList("data.tool_name");
        Assert.assertNotNull(toolNames, "Tool names should not be null");
        Assert.assertTrue(toolNames.contains("get_candidates"), "Expected tool 'get_candidates'");
        String entity = context.getString("data.context.entity");
        Assert.assertEquals(entity, "candidates", "Expected entity 'candidates'");
        List<Integer> actualIds = context.getList("data.context.records.id", Integer.class);
        Assert.assertNotNull(actualIds, "Record IDs should not be null");
        Assert.assertTrue(actualIds.containsAll(expectedCandidatesWithJavaSkill),"Expected candidate IDs were not found in results. Expected: " + expectedCandidatesLast10Days + " | Actual: " + actualIds);
        validateCompletionMessage(completion);
        validateChatTitle(chatTitle);
    }

    @Owner("Akshaya Uppala")
    @Test
    public void validateCandidatesWithJavaAndGoogleOrAppleExperience_GET() {
        String message = "Find candidates with Java programming skills who had an experience with google or apple";
        Response response = sendAskCopilotRequest(message);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");
        JsonPath toolCall = extractToolCallEvent(response);
        JsonPath context = extractContextEvent(response);
        JsonPath completion = extractCompletionEvent(response);
        JsonPath chatTitle = extractChatTitleEvent(response);
        List<String> toolNames = toolCall.getList("data.tool_name");
        Assert.assertTrue(toolNames.contains("get_candidates"), "Expected tool 'get_candidates'");
        String entity = context.getString("data.context.entity");
        Assert.assertEquals(entity, "candidates", "Expected entity 'candidates'");
        List<Integer> actualIds = context.getList("data.context.records.id", Integer.class);
        Assert.assertNotNull(actualIds, "Record IDs should not be null");
        Assert.assertTrue(actualIds.containsAll(expectedCandidatesWithGoogleOrApple),"Expected candidate IDs were not found in results. Expected: " + expectedCandidatesLast10Days + " | Actual: " + actualIds);
        validateCompletionMessage(completion);
        validateChatTitle(chatTitle);
    }

    @Owner("Sai Teja SG")
    @Test
    public void validateCandidatesAddedInLast10Days_GET() {
        String message = "show me candidates who added in last 10 days";
        Response response = sendAskCopilotRequest(message);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");
        JsonPath toolCall = extractToolCallEvent(response);
        JsonPath context = extractContextEvent(response);
        JsonPath completion = extractCompletionEvent(response);
        JsonPath chatTitle = extractChatTitleEvent(response);
        List<String> toolNames = toolCall.getList("data.tool_name");
        Assert.assertTrue(toolNames.contains("get_candidates"), "Expected tool 'get_candidates'");
        String dateRangeStart = toolCall.getString("data.mcp_input_payload.query.date_range_start");
        Assert.assertNotNull(dateRangeStart, "Date range start should not be null");
        String entity = context.getString("data.context.entity");
        Assert.assertEquals(entity, "candidates", "Expected entity 'candidates'");
        List<Integer> actualIds = context.getList("data.context.records.id", Integer.class);
        Assert.assertNotNull(actualIds, "Record IDs should not be null");
        Assert.assertTrue(actualIds.containsAll(expectedCandidatesLast10Days),"Expected candidate IDs were not found in results. Expected: " + expectedCandidatesLast10Days + " | Actual: " + actualIds);
        validateCompletionMessage(completion);
        validateChatTitle(chatTitle);
    }
}
