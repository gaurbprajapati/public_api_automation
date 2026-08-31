package io.recruitcrm.albatross.contact;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.GlobalUpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Arrays;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactUpdateFieldsTest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void updateContactStageWithInvalidAuth() {
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug = jsonContact.getString("slug");
        int contactId = allCrudFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), contactSlug).jsonPath().getInt("data.contact.id");
        JsonPath jsonContactStages = allCrudFunctions.getContactStages(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int stageId = jsonContactStages.getInt("data[0].id");
        String stageLabel = jsonContactStages.getString("data[0].label");
        GlobalUpdateFields globalUpdateFields = new GlobalUpdateFields("stageid", stageId, "contact", contactId, "Stage Updated", stageLabel);
        String basePath1 = "global/update-fields";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath1, ThreadManager.getOwnerAlbatrossToken() + "123", null, true, globalUpdateFields);
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void bulkUpdateContactStageWithInvalidAuth() {
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        JsonPath jsonContact1 = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug1 = jsonContact1.getString("slug");
        JsonPath jsonContact2 = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug2 = jsonContact2.getString("slug");
        int contactId1 = allCrudFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), contactSlug1).jsonPath().getInt("data.contact.id");
        int contactId2 = allCrudFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), contactSlug2).jsonPath().getInt("data.contact.id");
        JsonPath jsonContactStages = allCrudFunctions.getContactStages(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int stageId = jsonContactStages.getInt("data[0].id");
        String stageLabel = jsonContactStages.getString("data[0].label");
        GlobalUpdateFields globalUpdateFields1 = new GlobalUpdateFields("stageid", stageId, "contact", new ArrayList<>(Arrays.asList(contactId1, contactId2)), "Stage Updated", stageLabel);
        String basePath = "global/update-fields";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "123", null, true, globalUpdateFields1);
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

}
