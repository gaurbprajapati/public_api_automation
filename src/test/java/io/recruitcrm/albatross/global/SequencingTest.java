package io.recruitcrm.albatross.global;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.SequencingState.SequencingState;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class SequencingTest extends TestBase {

    commanFunction function = new commanFunction();

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void saveSequencingState() throws IOException {
        String path = "global/save-sequencing-state";

        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        // Read file content
        String filePath = "src/main/java/io/rcrm/api/pojo/albatross/SequencingState/payload.txt"; // Update with actual file path
        String rawJson = new String(Files.readAllBytes(Paths.get(filePath)));

        String unescapedJson = StringEscapeUtils.unescapeJson(rawJson);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(unescapedJson);
        String cleanedJson = objectMapper.writeValueAsString(jsonNode);

        SequencingState sequencingState = new SequencingState();
        sequencingState.setEntity_name("job");
        sequencingState.setEntity_slug(jobSlug);
        sequencingState.setSequencing_data(cleanedJson);
        Response response = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, sequencingState);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Sequencing state saved successfully"));
        response.then().body("data.entity_slug", Matchers.containsString(jobSlug));
        response.then().body("data.entity_name", Matchers.containsString("job"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//global//saveSequencingState.json"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedSaveSequencingState() throws IOException {
        String path = "global/save-sequencing-state";

        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        // Read file content
        String filePath = "src/main/java/io/rcrm/api/pojo/albatross/SequencingState/payload.txt"; // Update with actual file path
        String rawJson = new String(Files.readAllBytes(Paths.get(filePath)));

        String unescapedJson = StringEscapeUtils.unescapeJson(rawJson);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(unescapedJson);
        String cleanedJson = objectMapper.writeValueAsString(jsonNode);

        SequencingState sequencingState = new SequencingState();
        sequencingState.setEntity_name("job");
        sequencingState.setEntity_slug(jobSlug);
        sequencingState.setSequencing_data(cleanedJson);
        Response response = RestClient.doPost("JSON", albatrossURL, path, null, null, true, sequencingState);


        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void getSequencingState() throws IOException {
        // Save the state first
        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        // Read file content
        String filePath = "src/main/java/io/rcrm/api/pojo/albatross/SequencingState/payload.txt"; // Update with actual file path
        String rawJson = new String(Files.readAllBytes(Paths.get(filePath)));

        String unescapedJson = StringEscapeUtils.unescapeJson(rawJson);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(unescapedJson);
        String cleanedJson = objectMapper.writeValueAsString(jsonNode);

        SequencingState sequencingState = new SequencingState();
        sequencingState.setEntity_name("job");
        sequencingState.setEntity_slug(jobSlug);
        sequencingState.setSequencing_data(cleanedJson);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-sequencing-state", ThreadManager.getOwnerAlbatrossToken(), null, true, sequencingState);

        // Validate getting the sequencing data back

        String path = "global/get-sequencing-state";
        response= RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, sequencingState);

        response.prettyPrint();
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Sequencing state fetched successfully"));
        response.then().body("data.entity_slug", Matchers.containsString(jobSlug));
        response.then().body("data.entity_name", Matchers.containsString("job"));
        response.then().body("data.sequencing_data", Matchers.not(Matchers.empty()));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//global//getSequencingState.json"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedGetSequencingState() {
        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
        String path = "global/get-sequencing-state";

        SequencingState sequencingState = new SequencingState();
        sequencingState.setEntity_name("job");
        sequencingState.setEntity_slug(jobSlug);
        sequencingState.setSequencing_data("");

        Response response= RestClient.doPost("JSON", albatrossURL, path, null, null, true, sequencingState);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteSequencingState() throws IOException {
        // Save the state first
        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        // Read file content
        String filePath = "src/main/java/io/rcrm/api/pojo/albatross/SequencingState/payload.txt"; // Update with actual file path
        String rawJson = new String(Files.readAllBytes(Paths.get(filePath)));

        String unescapedJson = StringEscapeUtils.unescapeJson(rawJson);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(unescapedJson);
        String cleanedJson = objectMapper.writeValueAsString(jsonNode);

        SequencingState sequencingState = new SequencingState();
        sequencingState.setEntity_name("job");
        sequencingState.setEntity_slug(jobSlug);
        sequencingState.setSequencing_data(cleanedJson);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-sequencing-state", ThreadManager.getOwnerAlbatrossToken(), null, true, sequencingState);


        // Validate deleting the sequencing data

        String path = "global/delete-sequencing-state";
        sequencingState.setSequencing_data(null);
        response= RestClient.doDeleteOnce("application/json", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, null, true, sequencingState);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Sequencing state deleted successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//global//deleteSequencingState.json"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedDeleteSequencingState() {
        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        SequencingState sequencingState = new SequencingState();
        sequencingState.setEntity_name("job");
        sequencingState.setEntity_slug(jobSlug);
        sequencingState.setSequencing_data("");

        String path = "global/delete-sequencing-state";
        sequencingState.setSequencing_data(null);
        Response response= RestClient.doDeleteOnce("application/json", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, null, true, sequencingState);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Sequencing state deleted successfully"));
    }
}
