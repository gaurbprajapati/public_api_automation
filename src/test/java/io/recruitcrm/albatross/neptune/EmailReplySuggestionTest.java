package io.recruitcrm.albatross.neptune;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EmailReplySuggestionTest extends TestBase {

    private static final String BASE_PATH = "email-reply-suggestion/generate";
    private static final String CHUNK_META_MESSAGE = "Email reply suggestion generated successfully";
    private static final String COMPLETE_META_MESSAGE = "Complete message";

    private final Faker faker = new Faker(Locale.ENGLISH);
    private final JavaFakerMails mailFaker = new JavaFakerMails();
    private String albatrossToken;

    @BeforeClass
    public void setup() {
        albatrossToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "validData")
    public void generateAIReplySuggestionTest(String subject, String fromName, String threadBodyHtml, JSONArray recipients,
            JSONArray ccRecipients, Object signature) {
        JSONObject body = new JSONObject();
        body.put("subject", subject);
        body.put("signature", signature);
        JSONObject threadMsg = new JSONObject();
        threadMsg.put("body", threadBodyHtml);
        threadMsg.put("from", fromName);
        threadMsg.put("date", (int) (System.currentTimeMillis() / 1000L) - faker.number().numberBetween(0, 604800));
        JSONArray threadMessages = new JSONArray();
        threadMessages.put(threadMsg);
        body.put("thread_messages", threadMessages);
        body.put("recipients", recipients);
        body.put("cc_recipients", ccRecipients);

        Response response = post(body);
        Assert.assertEquals(response.getStatusCode(), 200);

        String raw = response.getBody().asString();
        List<String> events = extractJsonObjects(raw);
        Assert.assertFalse(events.isEmpty());

        int chunkCount = 0;
        for (String event : events) {
            JsonPath jp = new JsonPath(event);
            if (jp.get("data.generated_text") != null && CHUNK_META_MESSAGE.equals(jp.getString("meta.message"))) {
                chunkCount++;
            }
        }
        Assert.assertTrue(chunkCount > 0);

        JsonPath completion = findCompletionEvent(events);
        Assert.assertNotNull(completion.getString("data.complete_message"));
        Assert.assertFalse(completion.getString("data.complete_message").trim().isEmpty());
        Assert.assertEquals(completion.getString("meta.message"), COMPLETE_META_MESSAGE);
        Assert.assertEquals(completion.getString("meta.message_type"), "is-success");
        Assert.assertEquals(completion.getInt("meta.status"), 200);
        Assert.assertNotNull(completion.getString("meta.request_UUID"));
    }

    @DataProvider(parallel = false)
    public Object[][] validData() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Faker f = new Faker(Locale.ENGLISH);
            String from = f.name().fullName();
            String emailBody = mailFaker.getFakeEmailBody(f.number().numberBetween(2, 4));
            String threadHtml = "<p>" + emailBody.replace("\n", "</p><p>") + "</p>";

            JSONArray recipients = new JSONArray();
            int r = f.number().numberBetween(1, 3);
            for (int j = 0; j < r; j++) {
                recipients.put(f.name().fullName());
            }

            JSONArray cc = new JSONArray();
            int c = f.number().numberBetween(0, 2);
            for (int j = 0; j < c; j++) {
                cc.put(mailFaker.getFakeEmail());
            }

            Object signature = (i % 2 == 0) ? JSONObject.NULL
                    : "<p>" + f.name().fullName() + "<br/>" + f.company().name() + "</p>";

            rows.add(new Object[] {
                    "Re: " + mailFaker.getFakeEmailSubject(),
                    from,
                    threadHtml,
                    recipients,
                    cc,
                    signature
            });
        }
        return rows.toArray(new Object[0][]);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "invalidData")
    public void invalidPayloadTestForAIReplySuggestion(JSONObject body) {
        Response response = post(body);
        Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                "expected 4xx, got " + response.getStatusCode());
    }

    @DataProvider
    public Object[][] invalidData() {
        Faker f = new Faker(Locale.ENGLISH);
        JSONObject emptyThread = buildValidBody(f);
        emptyThread.put("thread_messages", new JSONArray());

        JSONObject nullBody = buildValidBody(f);
        nullBody.getJSONArray("thread_messages").getJSONObject(0).put("body", JSONObject.NULL);

        return new Object[][] {
                { emptyThread },
        };
    }

    @Owner("Sai Teja SG")
    @Test
    public void unauthorizedTestForAIReplySuggestion() {
        JSONObject body = buildValidBody(faker);
        Response response = RestClient.doPost("JSON", neptuneServiceURL, BASE_PATH, albatrossToken + "123", null, true, body);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("detail", Matchers.is("Unauthorized"));
        response.then().assertThat().body(
                JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));
    }

    private Response post(JSONObject body) {
        return RestClient.doPost("JSON", neptuneServiceURL, BASE_PATH, albatrossToken, null, true, body);
    }

    private JSONObject buildValidBody(Faker f) {
        String from = f.name().fullName();
        JSONObject threadMsg = new JSONObject();
        String emailBody = mailFaker.getFakeEmailBody(f.number().numberBetween(2, 4));
        threadMsg.put("body", "<p>" + emailBody.replace("\n", "</p><p>") + "</p>");
        threadMsg.put("from", from);
        threadMsg.put("date", (int) (System.currentTimeMillis() / 1000L));

        JSONArray threadMessages = new JSONArray();
        threadMessages.put(threadMsg);

        JSONArray recipients = new JSONArray();
        recipients.put(from);

        JSONArray cc = new JSONArray();
        cc.put(mailFaker.getFakeEmail());

        JSONObject body = new JSONObject();
        body.put("subject", "Re: " + mailFaker.getFakeEmailSubject());
        body.put("signature", JSONObject.NULL);
        body.put("thread_messages", threadMessages);
        body.put("recipients", recipients);
        body.put("cc_recipients", cc);
        return body;
    }

    private static List<String> extractJsonObjects(String responseBody) {
        Assert.assertNotNull(responseBody);
        Assert.assertFalse(responseBody.trim().isEmpty());
        List<String> events = new ArrayList<>();
        int depth = 0;
        int startIdx = -1;
        for (int i = 0; i < responseBody.length(); i++) {
            char c = responseBody.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    startIdx = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && startIdx != -1) {
                    events.add(responseBody.substring(startIdx, i + 1));
                    startIdx = -1;
                }
            }
        }
        return events;
    }

    private static JsonPath findCompletionEvent(List<String> events) {
        for (int i = events.size() - 1; i >= 0; i--) {
            String event = events.get(i);
            if (!event.contains("\"complete_message\"")) {
                continue;
            }
            JsonPath jp = new JsonPath(event);
            Object complete = jp.get("data.complete_message");
            if (complete != null && !JSONObject.NULL.equals(complete)) {
                return jp;
            }
        }
        Assert.fail("Completion event with data.complete_message not found");
        return null;
    }
}
