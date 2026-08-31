package io.rcrm.api.copilot;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteConversationAICopilotTest extends TestBase {

    String albatrossAuthToken, apiAuthToken;
    int ownerAccountID, actualUserId;
    Faker faker = new Faker();
    AllCrudFunctions function = new AllCrudFunctions();

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        getOwnerDetailsFromAPI();
    }

    private void getOwnerDetailsFromAPI() {
        Response getUsers = function.getUsers(albatrossURL, albatrossAuthToken);
        Assert.assertNotNull(getUsers);
        Assert.assertEquals(getUsers.getStatusCode(), 200);
        JsonPath jp = getUsers.jsonPath();
        actualUserId = jp.get("data.records[0].id");
        Assert.assertTrue(actualUserId > 0);
    }

    private String createConversation() {
        JSONObject payload = new JSONObject();
        payload.put("message", faker.lorem().sentence(5));
        payload.put("uuid", "");
        Response response = RestClient.doPost("JSON", neptuneServiceURL, "copilot/ask", albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create conversation for delete test");
        String responseBody = response.getBody().asString();
        String firstJson = responseBody.split("\\}\\{")[0] + "}";
        JsonPath jp = new JsonPath(firstJson);
        String uuid = jp.getString("meta.request_UUID");
        Assert.assertNotNull(uuid, "UUID should not be null");
        return uuid;
    }

    @DataProvider(name = "conversationProvider")
    public Object[][] conversationProvider() {
        String conversationId = createConversation();
        return new Object[][]{{conversationId}};
    }

    private Response deleteConversation(String conversationId, String token) {
        return RestClient.doDelete("JSON", neptuneServiceURL, "copilot/conversations/" + conversationId, token, null, null, true);
    }

    private void validateDeleteResponse(JsonPath jp) {
        Assert.assertNull(jp.get("data"));
        Assert.assertEquals(jp.getString("meta.message"), "Chat deleted successfully");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success");
        Assert.assertEquals(jp.getInt("meta.status"), 200);
        Assert.assertNotNull(jp.getString("meta.request_UUID"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "conversationProvider")
    public void deleteConversationWithValidId_DELETE(String conversationId) {
        Response response = deleteConversation(conversationId, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for valid conversation deletion");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/deleteConversationResponse.json"));
        validateDeleteResponse(response.jsonPath());
    }

    @Owner("Akshaya Uppala")
    @Test
    public void deleteConversationWithInvalidId_DELETE() {
        String invalidId = faker.internet().uuid();
        Response response = deleteConversation(invalidId, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected 404 for non-existent conversation ID");
    }

    @Owner("Sai Teja SG")
    @Test
    public void deleteConversationWithInvalidAuth_DELETE() {
        String conversationId = faker.internet().uuid();
        Response response = deleteConversation(conversationId, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid authentication");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "conversationProvider")
    public void deleteConversationTwice_DELETE(String conversationId) {
        Response response1 = deleteConversation(conversationId, albatrossAuthToken);
        Assert.assertEquals(response1.getStatusCode(), 200, "First deletion should succeed");
        response1.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/deleteConversationResponse.json"));
        validateDeleteResponse(response1.jsonPath());
        Response response2 = deleteConversation(conversationId, albatrossAuthToken);
        Assert.assertEquals(response2.getStatusCode(), 404, "Second deletion should return 404");
    }
}
