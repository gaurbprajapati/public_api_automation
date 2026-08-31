package io.recruitcrm.albatross.contact;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.albatross.GlobalUpdateFields;
import io.rcrm.api.pojo.albatross.StageHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Arrays;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AlbatrossEndpointsContactTest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    JavaFakerContact javaFakerContact = new JavaFakerContact();

    String reason = javaFakerContact.getStageUpdateReason();
    String companySlug;
    String contactSlug;
    int contactId;
    int stageId;
    String stageLabel;

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void updateContactStage() {
        companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        contactSlug = jsonContact.getString("slug");
        contactId = allCrudFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), contactSlug).jsonPath().getInt("data.contact.id");
        JsonPath jsonContactStages = allCrudFunctions.getContactStages(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        stageId = jsonContactStages.getInt("data[0].id");
        stageLabel = jsonContactStages.getString("data[0].label");
        GlobalUpdateFields globalUpdateFields = new GlobalUpdateFields("stageid", stageId, "contact", contactId, reason, stageLabel);
        String basePath1 = "global/update-fields";
        Response response1 = RestClient.doPost("JSON", albatrossURL, basePath1, ThreadManager.getOwnerAlbatrossToken(), null, true, globalUpdateFields);
        response1.then().statusCode(200);
        response1.then().body("message", Matchers.is("Field Updated Successfully"));
        response1.then().body("message_type", Matchers.is("is-success"));
        response1.then().body("data.stageid", Matchers.is(stageId + ""));
    }

    @Owner("Sai Teja SG")
    @Test(dependsOnMethods = "updateContactStage", groups = "nightly-build")
    public void bulkUpdateContactStage() {
        JsonPath jsonContact2 = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug2 = jsonContact2.getString("slug");
        int contactId2 = allCrudFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), contactSlug2).jsonPath().getInt("data.contact.id");
        JsonPath jsonContactStages = allCrudFunctions.getContactStages(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        stageId = jsonContactStages.getInt("data[1].id");
        stageLabel = jsonContactStages.getString("data[1].label");
        GlobalUpdateFields globalUpdateFields1 = new GlobalUpdateFields("stageid", stageId, "contact", new ArrayList<>(Arrays.asList(contactId, contactId2)), reason, stageLabel);
        String basePath = "global/update-fields";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, globalUpdateFields1);
        response.then().statusCode(200);
        response.then().body("message", Matchers.is("Field Updated Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.stageid", Matchers.is(stageId + ""));
    }

    @Owner("Smit Patel")
    @Test(dependsOnMethods = "updateContactStage", groups = "nightly-build")
    public void getContactStageHistory() {
        StageHistory stageHistory = new StageHistory();
        stageHistory.setEntity_type(2);
        stageHistory.setEntity_id(contactId);
        String basePath = "global/get-stage-history";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, stageHistory);
        response.then().statusCode(200);
        response.then().body("data[0].stage_name", Matchers.is(stageLabel));
        response.then().body("data[0].reason", Matchers.is(reason));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//global//getStageHistory.json"));
    }

}
