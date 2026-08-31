package io.recruitcrm.comm;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetUnassignedUsersTest extends TestBase {

    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    int phoneNumberId;
    int assignedPhoneNumberId;
    int userId;

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void unAssignUserIdsGET_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        JsonPath user = getUsers.jsonPath();
        userId = user.get("data.records[0].id");

        ReaperIntegration.insertPurchasedNumber(ThreadManager.getAccount().getAccountId(),user.get("data.records[0].id"));

        phoneNumberId = albatrossFunctions.getPurchasedPhoneNumberId(commURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("phone_numbers[0].id");
        assignedPhoneNumberId = albatrossFunctions.assignPhoneNumber(commURL, ThreadManager.getOwnerAlbatrossToken(),userId,phoneNumberId).jsonPath().get("data.id");


        String basePath = "phone-numbers/unassigned-userids";

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        int arrayLength = jsonPath.getList("user_ids").size();
        boolean containsId = jsonPath.getList("user_ids").contains(userId);

        Assert.assertEquals(arrayLength, 3, "Length of user_ids array is incorrect");
        Assert.assertFalse(containsId, "user_ids array contains the assigned user id");
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void unAssignedUserIdsGET_401() {

        String basePath = "phone-numbers/unassigned-userids";

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken() + 123, null, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);

    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"unAssignUserIdsGET_200"}, groups = "nightly-build")
    public void assignedNumberDetailsGET_200() {

        String basePath = "phone-numbers/assign";

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getInt("assigned_number_id"), assignedPhoneNumberId, "Phone number id is not as expected");
        Assert.assertEquals(response.jsonPath().getInt("assigned_user_id"), userId, "User id is not as expected");

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void assignedNumberDetailsGET_401() {

        String basePath = "phone-numbers/assign";

        Response response = RestClient.doGet("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken()+123, null, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);

    }

}
