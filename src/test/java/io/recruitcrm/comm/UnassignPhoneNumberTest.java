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
public class UnassignPhoneNumberTest extends TestBase {

    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    int phoneNumberId, assignedPhoneNumberId;

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void unassignPhoneNumberPOST_200() {
        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        JsonPath user = getUsers.jsonPath();
        int userId = user.get("data.records[0].id");

        ReaperIntegration.insertPurchasedNumber(ThreadManager.getAccount().getAccountId(), user.get("data.records[0].id"));

        phoneNumberId = albatrossFunctions.getPurchasedPhoneNumberId(commURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("phone_numbers[0].id");
        assignedPhoneNumberId = albatrossFunctions.assignPhoneNumber(commURL, ThreadManager.getOwnerAlbatrossToken(), userId, phoneNumberId).jsonPath().get("data.id");

        String basePath = "phone-numbers/assign/{id}";

        Map<String, String> pathParamters = new HashMap<>();
        pathParamters.put("id", String.valueOf(assignedPhoneNumberId));

        Response response = RestClient.doDelete("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-success");
        Assert.assertEquals(response.jsonPath().get("message"), "Teammate was Unassigned!");
        Assert.assertEquals(response.jsonPath().get("status"), "success");

    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"unassignPhoneNumberPOST_200"}, groups = "nightly-build")
    public void unassignPhoneNumberWithInvalidId() {

        String basePath = "phone-numbers/assign/{id}";

        Map<String, String> pathParamters = new HashMap<>();
        pathParamters.put("id", String.valueOf(assignedPhoneNumberId));

        Response response = RestClient.doDelete("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger");
        Assert.assertEquals(response.jsonPath().get("message"), "Assigned Number not found");
        Assert.assertEquals(response.jsonPath().get("status"), "fail");
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void unassignPhoneNumberPOST_401() {

        String basePath = "phone-numbers/assign/{id}";

        Map<String, String> pathParamters = new HashMap<>();
        pathParamters.put("id", String.valueOf(assignedPhoneNumberId));

        Response response = RestClient.doDelete("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken() + 123, null, pathParamters, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);

    }
}
