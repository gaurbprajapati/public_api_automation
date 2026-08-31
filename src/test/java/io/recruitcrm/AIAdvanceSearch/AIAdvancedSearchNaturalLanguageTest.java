package io.recruitcrm.AIAdvanceSearch;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class AIAdvancedSearchNaturalLanguageTest extends TestBase {

    private static final String AI_SEARCH_ENDPOINT = "ai-search";
    private static final String EXPECTED_COMPLETION_KEY = "completion";
    private static final String EXPECTED_INPUT_TYPE = "nlp";
    private static final String EXPECTED_META_MESSAGE = "Boolean query generated successfully";
    private static final String EXPECTED_META_MESSAGE_TYPE = "is-success";
    private static final int EXPECTED_META_STATUS = 200;

    private String albatrossToken;

    @BeforeClass
    public void setup() {
        albatrossToken = ThreadManager.getOwnerAlbatrossToken();
        Assert.assertNotNull(albatrossToken, "Failed to acquire Albatross token");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "naturalLanguageQueryTestData")
    public void validateNLPQueryConversion_Test(String userQuery, String language, JSONArray expectedKeywords) {

        JSONObject payload = new JSONObject().put("userQuery", userQuery);
        Response response = RestClient.doPost(
                "JSON", neptuneServiceURL, AI_SEARCH_ENDPOINT, albatrossToken, null, false, payload
        );

        Assert.assertEquals(response.getStatusCode(), 200, "[" + language + "] Expected status 200");

        JSONObject completion = parseStreamingResponseForCompletion(
                response.getBody().asString(), language
        );

        validateCompletionDataStructure(language, completion);
        validateMetaFields(language, completion);

        validateKeywordsInBooleanQuery(
                language,
                userQuery,
                completion.getJSONObject("data").getString("booleanQuery"),
                expectedKeywords
        );
    }

    @DataProvider(name = "naturalLanguageQueryTestData")
    public Object[][] naturalLanguageQueryTestData() {

        JSONArray cases = readJsonArrayFromFile(
                "src/test/resources/AIAdvanceSearch/ai_advanced_search_nlp.json"
        );

        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < cases.length(); i++) {
            JSONObject obj = cases.getJSONObject(i);
            data.add(new Object[]{
                    obj.getString("user_query"),
                    obj.getString("language"),
                    obj.getJSONArray("expected_keywords")
            });
        }
        return data.toArray(new Object[0][0]);
    }

    private JSONObject parseStreamingResponseForCompletion(String body, String language) {

        Assert.assertNotNull(body, "[" + language + "] Response body is null");
        Assert.assertFalse(body.trim().isEmpty(), "[" + language + "] Response body is empty");

        int idx = body.lastIndexOf("\"complete_message\"");
        Assert.assertTrue(idx != -1, "[" + language + "] completion not found");

        int dataIdx = body.lastIndexOf("\"data\"", idx);
        int start = body.lastIndexOf("{", dataIdx - 1);
        int end = body.lastIndexOf("}") + 1;

        try {
            return new JSONObject(body.substring(start, end));
        } catch (Exception e) {
            Assert.fail("[" + language + "] Failed to parse completion JSON: " + e.getMessage());
            return null;
        }
    }

    private void validateCompletionDataStructure(String language, JSONObject completion) {

        Assert.assertNotNull(completion, "[" + language + "] completion is null");
        Assert.assertTrue(completion.has("data"), "[" + language + "] data missing");

        JSONObject data = completion.getJSONObject("data");

        Assert.assertTrue(data.has("complete_message"), language);
        Assert.assertTrue(data.has("booleanQuery"), language);
        Assert.assertTrue(data.has("inputType"), language);
        Assert.assertTrue(data.has("key"), language);

        Assert.assertEquals(data.getString("key"), EXPECTED_COMPLETION_KEY, language);
        Assert.assertEquals(data.getString("inputType"), EXPECTED_INPUT_TYPE, language);
    }

    private void validateMetaFields(String language, JSONObject completion) {

        Assert.assertTrue(completion.has("meta"), "[" + language + "] meta missing");
        JSONObject meta = completion.getJSONObject("meta");

        Assert.assertEquals(meta.getString("message"), EXPECTED_META_MESSAGE, language);
        Assert.assertEquals(meta.getString("message_type"), EXPECTED_META_MESSAGE_TYPE, language);
        Assert.assertEquals(meta.getInt("status"), EXPECTED_META_STATUS, language);
        Assert.assertTrue(meta.has("request_UUID"), language);
    }

    private void validateKeywordsInBooleanQuery(
            String language, String userQuery, String booleanQuery, JSONArray expectedKeywords) {

        List<String> missing = new ArrayList<>();
        List<String> found = new ArrayList<>();
        String queryLower = booleanQuery.toLowerCase();

        for (int i = 0; i < expectedKeywords.length(); i++) {
            String k = expectedKeywords.getString(i);
            if (queryLower.contains(k.toLowerCase())) found.add(k);
            else missing.add(k);
        }

        StringBuilder msg = new StringBuilder()
                .append("\n[").append(language).append("] Keyword validation")
                .append("\nUser Query: ").append(userQuery)
                .append("\nBoolean Query: ").append(booleanQuery)
                .append("\nFound: ").append(found);

        if (!missing.isEmpty()) msg.append("\nMissing: ").append(missing);

        Assert.assertTrue(missing.isEmpty(), msg.toString());
    }

    private JSONArray readJsonArrayFromFile(String path) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path))).trim();
            if (content.startsWith("[")) return new JSONArray(content);
            if (content.startsWith("{")) return new JSONObject(content).getJSONArray("root");
            Assert.fail("Invalid JSON format: " + path);
        } catch (Exception e) {
            Assert.fail("Failed to read JSON from " + path + " : " + e.getMessage());
        }
        return new JSONArray();
    }
}
