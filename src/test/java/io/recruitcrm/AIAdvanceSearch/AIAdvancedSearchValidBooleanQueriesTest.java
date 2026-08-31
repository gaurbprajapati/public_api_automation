package io.recruitcrm.AIAdvanceSearch;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
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
public class AIAdvancedSearchValidBooleanQueriesTest extends TestBase {

    private static final String AI_SEARCH_ENDPOINT = "ai-search";
    private static final String EXPECTED_DATA_KEY = "completion";
    private static final String EXPECTED_INPUT_TYPE = "boolean";
    private static final String EXPECTED_META_MESSAGE = "Boolean query generated successfully";
    private static final String EXPECTED_META_MESSAGE_TYPE = "is-success";
    private static final int EXPECTED_META_STATUS = 200;

    private String albatrossToken;

    @BeforeClass
    public void setup() {
        albatrossToken = ThreadManager.getOwnerAlbatrossToken();
        Assert.assertNotNull(albatrossToken, "Failed to acquire Albatross token");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "booleanQueryTestData")
    public void validateBooleanQueryGeneration_Test(String userQuery, String language) {

        Response response = postAISearch(userQuery, albatrossToken);
        Assert.assertEquals(response.getStatusCode(), 200, "[" + language + "] Expected status 200, Query: " + userQuery);

        JsonPath json = response.jsonPath();
        validateMeta(language, json);
        validateData(language, json, userQuery);
    }

    @Owner("Smit Patel")
    @Test
    public void unauthorizedUserCannotAccessAISearch_Test() {

        Response response = postAISearch(
                "(software engineer OR developer) AND (Java OR Python)",
                albatrossToken + "123"
        );

        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertTrue(
                response.jsonPath().getString("detail").toLowerCase().contains("unauthorized"),
                "Error message should indicate unauthorized access"
        );
    }

    @Owner("Akshaya Uppala")
    @Test
    public void missingUserQueryField_Test() {
        Response response = RestClient.doPost(
                "JSON", neptuneServiceURL, AI_SEARCH_ENDPOINT, albatrossToken, null, false, new JSONObject()
        );
        Assert.assertTrue(response.getStatusCode() >= 400);
    }

    @Owner("Sai Teja SG")
    @Test
    public void emptyUserQueryField_Test() {
        Response response = postAISearch("", albatrossToken);
        Assert.assertTrue(response.getStatusCode() >= 400);
    }

    @DataProvider(name = "booleanQueryTestData")
    public Object[][] booleanQueryTestData() {

        JSONArray cases = readJsonArrayFromFile("src/test/resources/AIAdvanceSearch/ai_advanced_search_boolean.json"
        );

        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < cases.length(); i++) {
            JSONObject obj = cases.getJSONObject(i);
            data.add(new Object[]{obj.getString("user_query"), obj.getString("language")});
        }
        return data.toArray(new Object[0][0]);
    }

    private void validateMeta(String language, JsonPath json) {
        Assert.assertEquals(json.getString("meta.message"), EXPECTED_META_MESSAGE, language);
        Assert.assertEquals(json.getString("meta.message_type"), EXPECTED_META_MESSAGE_TYPE, language);
        Assert.assertEquals(json.getInt("meta.status"), EXPECTED_META_STATUS, language);
    }

    private void validateData(String language, JsonPath json, String userQuery) {

        Assert.assertEquals(json.getString("data.key"), EXPECTED_DATA_KEY, language);
        Assert.assertEquals(json.getString("data.inputType"), EXPECTED_INPUT_TYPE, language);
        Assert.assertNotNull(json.getString("data.complete_message"), language);

        if ("english".equalsIgnoreCase(language)) {
            Assert.assertEquals(json.getString("data.booleanQuery"), userQuery, language);
        }
    }

    private Response postAISearch(String query, String token) {
        JSONObject payload = new JSONObject();
        if (query != null) payload.put("userQuery", query);

        return RestClient.doPost("JSON", neptuneServiceURL, AI_SEARCH_ENDPOINT, token, null, false, payload);
    }

    private JSONArray readJsonArrayFromFile(String path) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path))).trim();
            if (content.startsWith("[")) return new JSONArray(content);
            if (content.startsWith("{")) return new JSONObject(content).getJSONArray("root");
        } catch (Exception e) {
            Assert.fail("Failed to read JSON from " + path + " : " + e.getMessage());
        }
        return new JSONArray();
    }
}
