package io.rcrm.api.copilot.datavalidation;

import java.util.*;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
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
public class AICopilotJobToolTests extends TestBase {

    private String albatrossAuthToken, apiAuthToken;
    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private final commanFunction function = new commanFunction();
    private List<Integer> expectedOpenJobsLast30Days;
    private List<String> expectedJobNames, expectedCompanyNames;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        expectedOpenJobsLast30Days = new ArrayList<>();
        expectedJobNames = new ArrayList<>();
        expectedCompanyNames = new ArrayList<>();
        createOpenJob();
        createOpenJob();
    }

    private void createOpenJob() {
        Response companyRes = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        String companySlug = companyRes.jsonPath().getString("slug");
        String companyName = companyRes.jsonPath().getString("company_name");
        Response contactRes = function.createNewContact_POST(baseURL, apiAuthToken, companySlug);
        String contactSlug = contactRes.jsonPath().getString("slug");
        Response jobRes = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug);
        String jobSlug = jobRes.jsonPath().getString("slug");
        String jobName = jobRes.jsonPath().getString("name");
        Response albRes = allCrudFunctions.getJobResponse(albatrossURL, albatrossAuthToken, jobSlug);
        Assert.assertEquals(albRes.getStatusCode(), 200);
        int albJobId = albRes.jsonPath().getInt("data.job.id");
        Assert.assertTrue(albJobId > 0);
        expectedOpenJobsLast30Days.add(albJobId);
        expectedJobNames.add(jobName);
        expectedCompanyNames.add(companyName);
    }

    private Response sendAskCopilotRequest(String msg) {
        JSONObject payload = new JSONObject();
        payload.put("message", msg);
        payload.put("uuid", "");
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/ask", albatrossAuthToken, null, true, payload);
    }

    private List<String> extractJsonEvents(String body) {
        List<String> events = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && --depth == 0 && start != -1) {
                events.add(body.substring(start, i + 1));
                start = -1;
            }
        }
        return events;
    }

    private JsonPath extractToolCallEvent(Response res) {
        String body = res.getBody().asString();
        List<String> ev = extractJsonEvents(body);
        JsonPath jp = new JsonPath(ev.get(1));
        Assert.assertEquals(jp.getString("data.event_name"), "tools");
        Assert.assertNotNull(jp.getList("data.tool_name"));
        return jp;
    }

    private JsonPath extractContextEvent(Response res) {
        String body = res.getBody().asString();
        List<String> ev = extractJsonEvents(body);
        JsonPath jp = new JsonPath(ev.get(2));
        Assert.assertEquals(jp.getString("data.event_name"), "tools");
        Assert.assertNotNull(jp.get("data.context"));
        return jp;
    }

    private JsonPath extractCompletionEvent(Response res) {
        String body = res.getBody().asString();
        int idx = body.lastIndexOf("\"complete_message\"");
        int dataIdx = body.lastIndexOf("\"data\"", idx);
        int start = body.lastIndexOf("{", dataIdx - 1);
        int end = body.lastIndexOf("}") + 1;
        return new JsonPath(body.substring(start, end));
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
    public void validateClientsWithOpenJobsLast30Days_GET() {
        Response res = sendAskCopilotRequest("Clients with 1 or more open jobs in last 30 days");
        Assert.assertEquals(res.getStatusCode(), 200);
        JsonPath tool = extractToolCallEvent(res);
        JsonPath ctx = extractContextEvent(res);
        JsonPath chatTitle = extractChatTitleEvent(res);
        Assert.assertTrue(tool.getList("data.tool_name").contains("get_jobs"));
        Assert.assertEquals(ctx.getString("data.context.entity"), "jobs");
        List<Integer> actualIds = ctx.getList("data.context.records.id", Integer.class);
        List<String> actualJobNames = ctx.getList("data.context.records.name", String.class);
        List<String> actualCompanyNames = ctx.getList("data.context.records.companyname", String.class);
        Assert.assertTrue(actualIds.containsAll(expectedOpenJobsLast30Days), "Missing job IDs: " + expectedOpenJobsLast30Days + " | Actual: " + actualIds);
        Assert.assertTrue(actualJobNames.containsAll(expectedJobNames), "Missing job names: " + expectedJobNames + " | Actual: " + actualJobNames);
        Assert.assertTrue(actualCompanyNames.containsAll(expectedCompanyNames), "Missing companies: " + expectedCompanyNames + " | Actual: " + actualCompanyNames);
        JsonPath completion = extractCompletionEvent(res);
        String completeMsg = completion.getString("data.complete_message");
        Assert.assertNotNull(completeMsg);
        Assert.assertTrue(completeMsg.toLowerCase().contains("client") || completeMsg.toLowerCase().contains("job"));
        validateChatTitle(chatTitle);
    }
}
