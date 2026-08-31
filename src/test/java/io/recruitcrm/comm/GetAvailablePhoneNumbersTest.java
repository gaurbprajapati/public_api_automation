package io.recruitcrm.comm;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
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
public class GetAvailablePhoneNumbersTest extends TestBase {

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getAvailableUSPhoneNumbersGET_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());
        String basePath = "phone-numbers/available";

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("search_number_term", "");
        queryParamters.put("search_locality_term", "");
        queryParamters.put("country", "US");
        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath json = response.jsonPath();
        Assert.assertFalse(json.getList("phone_numbers").isEmpty(), "Phone numbers array is empty");
        Assert.assertEquals(json.get("status"), "success");

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getAvailableUKPhoneNumbersGET_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());
        String basePath = "phone-numbers/available";

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("search_number_term", "");
        queryParamters.put("search_locality_term", "");
        queryParamters.put("country", "GB");
        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath json = response.jsonPath();
        Assert.assertFalse(json.getList("phone_numbers").isEmpty(), "Phone numbers array is empty");
        Assert.assertEquals(json.get("status"), "success");

    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"getAvailableUSPhoneNumbersGET_200"}, groups = "nightly-build")
    public void filterAvailablePhoneNumbersGET_200() {

        String basePath = "phone-numbers/available";

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("search_number_term", "****22****");
        queryParamters.put("country", "US");
        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath json = response.jsonPath();
        Assert.assertTrue(json.get("phone_numbers[0].phoneNumber").toString().matches(".*22.*"), "Phone number is not matching");
        Assert.assertEquals(json.get("status"), "success");

    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"getAvailableUSPhoneNumbersGET_200"}, groups = "nightly-build")
    public void getAvailablePhoneNumbersGET_422() {

        String basePath = "phone-numbers/available";

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("country", "");
        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 422);

        JsonPath json = response.jsonPath();
        Assert.assertEquals(json.get("country[0]"), "Country is not selected");

    }


    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getAvailablePhoneNumbersGET_401() {

        String basePath = "phone-numbers/available";

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("country", "US");
        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "123", queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);

    }

}
