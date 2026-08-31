package io.rcrm.api.deals;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetDealStageHistoryTest extends TestBase {

    commanFunction function = new commanFunction();

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getDealStageHistoryWithInvalidAuth() {
        String dealSlug = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("deal", dealSlug);
        String basePath = "deal/get-stage-history/{deal}";
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey() + "123", null, pathParams, true);
        assert response != null : "Response is Null";
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void getDealStageWithInvalidDealSlug() {
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("deal", "invalidSlug");
        String basePath = "deal/get-stage-history/{deal}";
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParams, true);
        assert response != null : "Response is null";
        response.then().statusCode(404);
        response.then().body("errorMessage", Matchers.is("deal not found"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void getDealStageWithInvalidEntityType() {
        String dealSlug = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("deal", dealSlug);
        String basePath = "company/get-stage-history/{deal}";
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParams, true);
        assert response != null : "Response is null";
        response.then().statusCode(422);
        response.then().body("errorMessage", Matchers.is("Invalid Entity"));
    }

}
