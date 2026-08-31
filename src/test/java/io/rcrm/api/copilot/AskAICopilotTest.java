package io.rcrm.api.copilot;

import java.util.List;
import java.util.UUID;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
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
public class AskAICopilotTest extends TestBase {

    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;
    Faker faker = new Faker();

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
    }

    private JsonPath getCompletionEvent(Response response) {
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

    private void validateMetaFields(JsonPath jp) {
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta request_UUID should not be null");
    }

    private void validateCompletionEvent(JsonPath jp) {
        Assert.assertEquals(jp.getString("data.event"), "completion", "Expected data.event to be 'completion'");
        Assert.assertEquals(jp.getString("data.event_name"), "complete_response", "Expected event_name to be 'complete_response'");

        String completeMessage = jp.getString("data.complete_message");
        Assert.assertNotNull(completeMessage, "Complete message should not be null");
        Assert.assertFalse(completeMessage.isEmpty(), "Complete message should not be empty");

        Assert.assertEquals(jp.getString("meta.message"), "Complete message", "Meta message should be 'Complete message'");
        validateMetaFields(jp);
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

    private Response sendAskCopilotRequest(String message, String uuid, String token) {
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("uuid", uuid);
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/ask", token, null, true, payload);
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

    private void assertValidCompletionResponse(Response response) {
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status 200");
        JsonPath chatTitle = extractChatTitleEvent(response);
        validateChatTitle(chatTitle);
        JsonPath completionEvent = getCompletionEvent(response);
        validateCompletionEvent(completionEvent);
    }

    @Owner("Sai Teja SG")
    @Test
    public void askCopilotWithValidMessage_POST() {
        Response response = sendAskCopilotRequest("hello", "", albatrossAuthToken);
        assertValidCompletionResponse(response);
    }

    @Owner("Smit Patel")
    @Test
    public void askCopilotWithCustomMessage_POST() {
        Response response = sendAskCopilotRequest("How can I help with recruiting tasks today?", UUID.randomUUID().toString(), albatrossAuthToken);
        assertValidCompletionResponse(response);
    }

    @Owner("Akshaya Uppala")
    @Test
    public void askCopilotWithEmptyMessage_POST() {
        Response response = sendAskCopilotRequest("", "", albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected HTTP 400 for empty message");
    }

    @Owner("Sai Teja SG")
    @Test
    public void askCopilotWithMaxLengthMessage_POST() {
        String maxMessage = faker.lorem().characters(1000);
        Response response = sendAskCopilotRequest(maxMessage, UUID.randomUUID().toString(), albatrossAuthToken);
        assertValidCompletionResponse(response);
    }

    @Owner("Smit Patel")
    @Test
    public void askCopilotWithExceedMaxLengthMessage_POST() {
        String exceededMessage = faker.lorem().characters(1001);
        Response response = sendAskCopilotRequest(exceededMessage, UUID.randomUUID().toString(), albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected HTTP 400 for exceeding max message length");
    }

    @Owner("Akshaya Uppala")
    @Test
    public void askCopilotWithNewChatSession_POST() {
        Response response = sendAskCopilotRequest("Start a new chat about recruiting", "", albatrossAuthToken);
        assertValidCompletionResponse(response);
        JsonPath completionEvent = getCompletionEvent(response);
        Assert.assertNotNull(completionEvent.getString("meta.request_UUID"), "Meta request_UUID should not be null for new chat session");
    }

    @Owner("Sai Teja SG")
    @Test
    public void askCopilotWithInvalidAuth_POST() {
        Response response = sendAskCopilotRequest("hello", "", "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for invalid token");
    }

    @Owner("Smit Patel")
    @Test
    public void askCopilotWithMissingMessage_POST() {
        JSONObject payload = new JSONObject();
        payload.put("uuid", UUID.randomUUID().toString());
        Response response = RestClient.doPost("JSON", neptuneServiceURL, "copilot/ask", albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected HTTP 400 when message is missing");
    }

    @Owner("Akshaya Uppala")
    @Test
    public void askCopilotWithExistingChatSession_POST() {
        Response firstResponse = sendAskCopilotRequest("First message in chat", "", albatrossAuthToken);
        Assert.assertEquals(firstResponse.getStatusCode(), 200, "Expected HTTP 200 for first message");
        JsonPath firstCompletion = getCompletionEvent(firstResponse);
        validateCompletionEvent(firstCompletion);
        String sessionUUID = firstCompletion.getString("meta.request_UUID");
        Response secondResponse = sendAskCopilotRequest("Continue conversation in same session", sessionUUID, albatrossAuthToken);
        Assert.assertEquals(secondResponse.getStatusCode(), 200, "Expected HTTP 200 for existing session");
        JsonPath secondCompletion = getCompletionEvent(secondResponse);
        validateCompletionEvent(secondCompletion);
        Assert.assertEquals(secondCompletion.getString("meta.request_UUID"), sessionUUID, "Session UUID should remain same across messages");
    }

    @Owner("Sai Teja SG")
    @Test
    public void askCopilotWithRecruitingQuery_POST() {
        Response response = sendAskCopilotRequest("Help me find candidates with Java experience", "", albatrossAuthToken);
        assertValidCompletionResponse(response);
        String completeMessage = getCompletionEvent(response).getString("data.complete_message");
        Assert.assertFalse(completeMessage.isEmpty(), "Complete message should not be empty for recruiting query");
    }
}
