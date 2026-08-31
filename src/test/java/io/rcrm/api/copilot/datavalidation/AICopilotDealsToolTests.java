package io.rcrm.api.copilot.datavalidation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.Deal;
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
public class AICopilotDealsToolTests extends TestBase {
    
    private String albatrossAuthToken;
    private String apiAuthToken;
    private final Faker faker = new Faker();
    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private List<Integer> expectedDealsLast30Days;
    private List<Integer> expectedClosedDealsLast30Days;
    private List<String> expectedDealNames;
    private List<String> expectedClosedDealNames;
    private Response lastResponse; // Store last response for reporting

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        expectedDealsLast30Days = new ArrayList<>();
        expectedClosedDealsLast30Days = new ArrayList<>();
        expectedDealNames = new ArrayList<>();
        expectedClosedDealNames = new ArrayList<>();
        createTestDealsForLast30Days();
        createTestClosedDealsForLast30Days();
    }
    private void createTestDealsForLast30Days() {
        LocalDate today = LocalDate.now();
        int deal1 = createDeal("OpenDeal_30Days_1", today.minusDays(5), "1");
        int deal2 = createDeal("OpenDeal_30Days_2", today.minusDays(15), "1");
        int deal3 = createDeal("OpenDeal_30Days_3", today.minusDays(25), "1");
        expectedDealsLast30Days = Arrays.asList(deal1, deal2, deal3);
        expectedDealNames.addAll(Arrays.asList("OpenDeal_30Days_1", "OpenDeal_30Days_2", "OpenDeal_30Days_3"));
    }

    private void createTestClosedDealsForLast30Days() {
        LocalDate today = LocalDate.now();
        int closedDeal1 = createDeal("ClosedDeal_30Days_1", today.minusDays(10), "2");
        int closedDeal2 = createDeal("ClosedDeal_30Days_2", today.minusDays(20), "2");
        expectedClosedDealsLast30Days = Arrays.asList(closedDeal1, closedDeal2);
        expectedClosedDealNames.addAll(Arrays.asList("ClosedDeal_30Days_1", "ClosedDeal_30Days_2"));
    }

    private int createDeal(String dealName, LocalDate createdDate, String dealStage) {
        Deal deal = new Deal();
        deal.setName(dealName);
        deal.setDeal_value(faker.number().numberBetween(10000, 100000));
        deal.setClose_date(createdDate.plusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        deal.setDeal_stage(dealStage);
        deal.setDeal_type("1");

        Response createResponse = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Failed to create deal: " + dealName);
        String dealSlug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(dealSlug, "Deal slug should not be null");

        Response albatrossResponse = allCrudFunctions.getDealResponse(albatrossURL, albatrossAuthToken, dealSlug);
        Assert.assertEquals(albatrossResponse.getStatusCode(), 200, "Failed to fetch deal from Albatross");
        int albatrossDealId = albatrossResponse.jsonPath().getInt("data.deal.id");
        Assert.assertTrue(albatrossDealId > 0, "Albatross deal ID should be valid");
        return albatrossDealId;
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
    public void validateDealsAddedVsClosedLast30Days_GET() {
        String message = "Number of deals added in last 30 days vs closed";
        Response response = sendAskCopilotRequest(message);
        lastResponse = response;
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");
        
        JsonPath toolCall = extractToolCallEvent(response);
        JsonPath context = extractContextEvent(response);
        JsonPath completion = extractCompletionEvent(response);
        JsonPath chatTitle = extractChatTitleEvent(response);
        
        List<String> toolNames = toolCall.getList("data.tool_name");
        Assert.assertNotNull(toolNames, "Tool names should not be null");
        Assert.assertTrue(toolNames.contains("get_deals"), "Expected tool 'get_deals'");
        
        String entity = context.getString("data.context.entity");
        Assert.assertEquals(entity, "deals", "Expected entity 'deals'");
        
        Object records = context.get("data.context.records");
        Assert.assertNotNull(records, "Records should not be null in context");
        
        List<Integer> actualIds = context.getList("data.context.records.id", Integer.class);
        Assert.assertNotNull(actualIds, "Actual deal IDs should not be null");
        Assert.assertFalse(actualIds.isEmpty(), "Actual deal IDs should not be empty");
        
        Assert.assertTrue(actualIds.size() >= (expectedDealsLast30Days.size() + expectedClosedDealsLast30Days.size()), 
            "Should have at least " + (expectedDealsLast30Days.size() + expectedClosedDealsLast30Days.size()) + " deals, but found: " + actualIds.size());
        
        for (Integer expectedDealId : expectedDealsLast30Days) {
            Assert.assertTrue(actualIds.contains(expectedDealId), 
                "Expected open deal ID " + expectedDealId + " should be present in results. Actual: " + actualIds);
        }
        
        for (Integer expectedClosedDealId : expectedClosedDealsLast30Days) {
            Assert.assertTrue(actualIds.contains(expectedClosedDealId), 
                "Expected closed deal ID " + expectedClosedDealId + " should be present in results. Actual: " + actualIds);
        }
        
        Assert.assertTrue(actualIds.containsAll(expectedDealsLast30Days), 
            "All expected open deal IDs should be present in results. Expected: " + expectedDealsLast30Days + " | Actual: " + actualIds);
        Assert.assertTrue(actualIds.containsAll(expectedClosedDealsLast30Days), 
            "All expected closed deal IDs should be present in results. Expected: " + expectedClosedDealsLast30Days + " | Actual: " + actualIds);
        
        String completeMessage = completion.getString("data.complete_message");
        Assert.assertNotNull(completeMessage, "Complete message should not be null");
        Assert.assertTrue(completeMessage.toLowerCase().contains("deal") || completeMessage.toLowerCase().contains("30"), 
            "Complete message should contain deal-related content");
        
        validateCompletionMessage(completion);
        validateChatTitle(chatTitle);
    }
}
