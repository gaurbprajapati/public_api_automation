package io.recruitcrm.albatross.prePopulatedFields;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerTemplate;
import io.rcrm.api.pojo.albatross.CSVTemplate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CSVFileOperationsTest extends TestBase {

    public CSVFileOperationsTest(){
        super();
    }

    JavaFakerTemplate javaFakerTemplate = new JavaFakerTemplate();
    String path = "import/templates";

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void getImportTemplate() {
        Response response = RestClient.doGet("JSON", albatrossURL, "import/get-templates/5", ThreadManager.getOwnerAlbatrossToken(), null, null, true);


        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi//prePopulatedFields//importCSV.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void unauthorizedAccessToImportTemplate() {
        Response response = RestClient.doGet("JSON", albatrossURL, "import/get-templates/5", ThreadManager.getOwnerAlbatrossToken() + "12345", null, null, true);


        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void createNewImportTemplate_POST() {
        CSVTemplate csvTemplate = new CSVTemplate();
        csvTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvTemplate.setTemplate_content(templateContent);
        csvTemplate.setEntity_type(javaFakerTemplate.getEntityType());
        csvTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response response = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, csvTemplate);


        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi//prePopulatedFields//saveCSVTemplate.json"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void createInvalidImportTemplate_POST() {
        CSVTemplate csvTemplate = new CSVTemplate();
        csvTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvTemplate.setTemplate_content(templateContent);

        Response response = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, csvTemplate);


        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("message", Matchers.containsString("The entity type field is required."));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void createInvalidAuthTokenTemplate_POST() {
        CSVTemplate csvTemplate = new CSVTemplate();
        csvTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvTemplate.setTemplate_content(templateContent);
        csvTemplate.setEntity_type(javaFakerTemplate.getEntityType());
        csvTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response response = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, csvTemplate);


        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void editNewImportTemplateSpecific_POST() {
        CSVTemplate csvCreateTemplate = new CSVTemplate();
        csvCreateTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvCreateTemplate.setTemplate_content(templateContent);
        csvCreateTemplate.setEntity_type(javaFakerTemplate.getEntityType());
        csvCreateTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response createResponse = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, csvCreateTemplate);
        String jsonResponse = createResponse.asString();
        JsonPath responseBody = new JsonPath(jsonResponse);
        String id = responseBody.getString("data.id");

        CSVTemplate csvEditTemplate = new CSVTemplate();
        csvEditTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        csvEditTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response response = RestClient.doPost("JSON", albatrossURL, path + "/" + id, ThreadManager.getOwnerAlbatrossToken(), null, true, csvEditTemplate);


        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//prePopulatedFields//editCSVTemplate.json"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void editInvalidImportTemplateSpecific_POST() {
        CSVTemplate csvCreateTemplate = new CSVTemplate();
        csvCreateTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvCreateTemplate.setTemplate_content(templateContent);
        csvCreateTemplate.setEntity_type(javaFakerTemplate.getEntityType());
        csvCreateTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response createResponse = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, csvCreateTemplate);
        String jsonResponse = createResponse.asString();
        JsonPath responseBody = new JsonPath(jsonResponse);
        String id = responseBody.getString("data.id");

        CSVTemplate csvEditTemplate = new CSVTemplate();

        Response response = RestClient.doPost("JSON", albatrossURL, path + "/" + id, ThreadManager.getOwnerAlbatrossToken(), null, true, csvEditTemplate);


        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("message", Matchers.containsString("The template name field is required."));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void editInvalidAuthTokenTemplateSpecific_POST() {
        CSVTemplate csvCreateTemplate = new CSVTemplate();
        csvCreateTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvCreateTemplate.setTemplate_content(templateContent);
        csvCreateTemplate.setEntity_type(javaFakerTemplate.getEntityType());
        csvCreateTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response createResponse = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, csvCreateTemplate);

        String jsonResponse = createResponse.asString();
        JsonPath responseBody = new JsonPath(jsonResponse);
        String id = responseBody.getString("data.id");

        CSVTemplate csvEditTemplate = new CSVTemplate();

        Response response = RestClient.doPost("JSON", albatrossURL, path + "/" + id, ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, csvEditTemplate);


        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void deleteImportTemplateSpecific_DELETE() {
        CSVTemplate csvCreateTemplate = new CSVTemplate();
        csvCreateTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvCreateTemplate.setTemplate_content(templateContent);
        csvCreateTemplate.setEntity_type(javaFakerTemplate.getEntityType());
        csvCreateTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response createResponse = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, csvCreateTemplate);

        String jsonResponse = createResponse.asString();
        JsonPath responseBody = new JsonPath(jsonResponse);
        String id = responseBody.getString("data.id");

        Response response = RestClient.doDelete("JSON", albatrossURL, path + "/" + id, ThreadManager.getOwnerAlbatrossToken(), null, null, false);


        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.containsString("Template deleted successfully"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void deleteNonExistingImportTemplateSpecific_DELETE() {
        Response response = RestClient.doDelete("JSON", albatrossURL, path + "/Invalid_Id", ThreadManager.getOwnerAlbatrossToken(), null, null, false);


        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.containsString("Template not found"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void deleteImportTemplateInvalidAuthToken_DELETE() {
        CSVTemplate csvCreateTemplate = new CSVTemplate();
        csvCreateTemplate.setTemplate_name(javaFakerTemplate.getTemplateName());
        String templateContent = String.format("{\"firstname\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"locality\":\"%s\",\"note\":\"%s\"}",
                javaFakerTemplate.getFirstName(),
                javaFakerTemplate.getLastName(),
                javaFakerTemplate.getEmail(),
                javaFakerTemplate.getPhoneNumber(),
                javaFakerTemplate.getCity(),
                javaFakerTemplate.getLocality(),
                javaFakerTemplate.getNote());
        csvCreateTemplate.setTemplate_content(templateContent);
        csvCreateTemplate.setEntity_type(javaFakerTemplate.getEntityType());
        csvCreateTemplate.setSharewithteammates(javaFakerTemplate.getShareWithTeammates());

        Response createResponse = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, csvCreateTemplate);

        String jsonResponse = createResponse.asString();
        JsonPath responseBody = new JsonPath(jsonResponse);
        String id = responseBody.getString("data.id");

        Response response = RestClient.doDelete("JSON", albatrossURL, path + "/" + id, ThreadManager.getOwnerAlbatrossToken() + "12345", null, null, false);


        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }
}