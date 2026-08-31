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
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllowedCountriesTest extends TestBase{

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void allowedCountriesGET_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        String basePath = "phone-numbers/allowed-countries";

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        String[] countryCodes = {"US", "GB"};
        String[] countryNames = {"United States", "United Kingdom"};
        int[][] enabled = {{1, 0}, {1, 1}};

        for (int i = 0; i < countryCodes.length; i++) {
            response.then().body("[" + i + "].country_code", Matchers.is(countryCodes[i]));
            response.then().body("[" + i + "].country_name", Matchers.is(countryNames[i]));
            response.then().body("[" + i + "].local_enabled", Matchers.is(enabled[i][0]));
            response.then().body("[" + i + "].mobile_enabled", Matchers.is(enabled[i][1]));
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void allowedCountriesGET_401() {

        String basePath = "phone-numbers/allowed-countries";

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken()+123, null, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);

    }
}
