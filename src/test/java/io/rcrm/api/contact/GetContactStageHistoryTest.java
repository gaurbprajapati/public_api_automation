package io.rcrm.api.contact;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetContactStageHistoryTest extends TestBase {

    commanFunction function = new commanFunction();

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getContactStageHistoryWithInvalidAuth() {
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug = jsonContact.getString("slug");
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("contact", contactSlug);
        String basePath = "contact/get-stage-history/{contact}";
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey() + "123", null, pathParams, true);
        assert response != null : "Response is null";
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void getContactStageWithInvalidContactSlug() {
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("contact", "invalidSlug");
        String basePath = "contact/get-stage-history/{contact}";
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParams, true);
        assert response != null : "Response is null";
        response.then().statusCode(404);
        response.then().body("errorMessage", Matchers.is("contact not found"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void getContactStageWithInvalidEntityType() {
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug = jsonContact.getString("slug");
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("contact", contactSlug);
        String basePath = "candidate/get-stage-history/{contact}";
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParams, true);
        assert response != null : "Response is null";
        response.then().statusCode(422);
        response.then().body("errorMessage", Matchers.is("Invalid Entity"));
    }

}
