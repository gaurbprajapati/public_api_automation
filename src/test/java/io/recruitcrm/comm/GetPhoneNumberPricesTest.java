package io.recruitcrm.comm;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetPhoneNumberPricesTest extends TestBase {

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void usPhoneNumberPricesGET_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        String basePath = "phone-numbers/prices";

        Map<String, String> queryParamters = new HashMap<>();
        queryParamters.put("country", "US");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void ukPhoneNumberPricesGET_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        String basePath = "phone-numbers/prices";

        Map<String, String> queryParamters = new HashMap<>();
        queryParamters.put("country", "GB");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"usPhoneNumberPricesGET_200"}, groups = "nightly-build")
    public void phoneNumberPricesGET_422() {

        String basePath = "phone-numbers/prices";

        Map<String, String> queryParamters = new HashMap<>();
        queryParamters.put("country", "USS");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken() , queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("country[0]", Matchers.is("Selected Country should be valid"));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void phoneNumberPricesGET_401() {

        String basePath = "phone-numbers/prices";

        Map<String, String> queryParamters = new HashMap<>();
        queryParamters.put("country", "US");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken() + 123, queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);

    }
}
