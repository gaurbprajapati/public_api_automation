package io.recruitcrm.comm;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetPurchasedPhoneNumbersTest extends TestBase {

    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getPurchasedPhoneNumbersGET_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        JsonPath user = getUsers.jsonPath();

        ReaperIntegration.insertPurchasedNumber(ThreadManager.getAccount().getAccountId(),user.get("data.records[0].id"));

        String basePath = "phone-numbers/purchased";

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("page","1");
        queryParamters.put("page_size","10");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getPurchasedPhoneNumbersGET_401() {

        String basePath = "phone-numbers/purchased";

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("page","1");
        queryParamters.put("page_size","10");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken()+123, queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);

    }
}
