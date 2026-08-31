package io.recruitcrm.comm;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PhoneNumberStatusTest extends TestBase {

    @Owner("Harika")
    @Test(dataProvider = "getPhoneNumberType", groups = "nightly-build")
    public void phoneNumberStatusGET_200(String phoneNumberType) {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        String basePath = "regulatory/phone-number-status/type";
        Map<String, String> queryParamters = new HashMap<>();
        queryParamters.put("number-type", phoneNumberType);

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

        if (phoneNumberType.equals("both")) {
            response.then().body("local.bundle_created", Matchers.is(false));
            response.then().body("mobile.bundle_created", Matchers.is(false));
            response.then().body("local.bundle_sid", Matchers.is(false));
            response.then().body("mobile.bundle_sid", Matchers.is(false));
            response.then().assertThat().body(
                    JsonSchemaValidator.matchesJsonSchemaInClasspath("privateApi//comm//phoneNumberStatus.json"));
        } else {
            response.then().body("bundle_created", Matchers.is(false));
            response.then().body("bundle_sid", Matchers.is(false));
        }

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void phoneNumberStatusGET_422() {

        String basePath = "regulatory/phone-number-status/type";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("number-type", "test");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 422);

        response.then().body("error['number-type'][0]", Matchers.is("The selected number-type is invalid."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void phoneNumberStatusGET_401() {

        String basePath = "regulatory/phone-number-status/type";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("number-type", "both");

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.is("Unauthorized"));

    }

    @DataProvider
    public Object[][] getPhoneNumberType() {
        return new Object[][]{ { "both" }, { "local" }, { "mobile" }};
    }

}
