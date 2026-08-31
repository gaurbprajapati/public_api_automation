package io.recruitcrm.comm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.comm.AssignNumber;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditAssignPhoneNumberTest extends TestBase {

    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    int phoneNumberId, assignedPhoneNumberId, userId1, userId2;


    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void editAssignPhoneNumberPOST_200() {
        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        JsonPath user = getUsers.jsonPath();
        userId1 = user.get("data.records[0].id");
        userId2 = user.get("data.records[1].id");

        ReaperIntegration.insertPurchasedNumber(ThreadManager.getAccount().getAccountId(),user.get("data.records[0].id"));

        phoneNumberId = albatrossFunctions.getPurchasedPhoneNumberId(commURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("phone_numbers[0].id");
        assignedPhoneNumberId = albatrossFunctions.assignPhoneNumber(commURL, ThreadManager.getOwnerAlbatrossToken(),userId1,phoneNumberId).jsonPath().get("data.id");

        String availabilityString = getAvailabilityString();

        String basePath = "phone-numbers/assign/{id}";

        Map<String, String> pathParamters = new HashMap<>();
        pathParamters.put("id", String.valueOf(assignedPhoneNumberId));

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id(String.valueOf(userId2));
        assignPhoneNumber.setPhone_number_id(String.valueOf(phoneNumberId));
        assignPhoneNumber.setNumber_title("Reassign Phone Number to Admin");
        assignPhoneNumber.setVoice_reply("The person you are calling is not available");
        assignPhoneNumber.setMasked_number("+1234567890");
        assignPhoneNumber.setAvailability(availabilityString);

        Response response = RestClient.doPut1("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,pathParamters, true, assignPhoneNumber);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-success");
        Assert.assertEquals(response.jsonPath().get("message"), "Assigned phone number configured!");
        Assert.assertEquals(response.jsonPath().getInt("data.phone_number_id"), phoneNumberId, "Phone number id is not as expected");
        Assert.assertEquals(response.jsonPath().getInt("data.assigned_user_id"), userId2, "User id is not as expected");
        response.then().body("data.id", Matchers.notNullValue());
    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"editAssignPhoneNumberPOST_200"}, groups = "nightly-build")
    public void editAssignPhoneNumberWithInvalidId() {

        String availabilityString = getAvailabilityString();

        String basePath = "phone-numbers/assign/{id}";

        Map<String, String> pathParamters = new HashMap<>();
        pathParamters.put("id", String.valueOf(assignedPhoneNumberId+123));

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id(String.valueOf(userId2));
        assignPhoneNumber.setPhone_number_id(String.valueOf(phoneNumberId));
        assignPhoneNumber.setNumber_title("Reassign Phone Number to Admin");
        assignPhoneNumber.setVoice_reply("The person you are calling is not available");
        assignPhoneNumber.setMasked_number("+1234567890");
        assignPhoneNumber.setAvailability(availabilityString);

        Response response = RestClient.doPut1("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,pathParamters, true, assignPhoneNumber);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger");
        Assert.assertEquals(response.jsonPath().get("status"), "fail");
        Assert.assertEquals(response.jsonPath().get("message"), "Number not found");
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void editAssignPhoneNumberPOST_401() {

        String availabilityString = getAvailabilityString();

        String basePath = "phone-numbers/assign/{id}";

        Map<String, String> pathParamters = new HashMap<>();
        pathParamters.put("id", String.valueOf(assignedPhoneNumberId+123));

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id(String.valueOf(userId2));
        assignPhoneNumber.setPhone_number_id(String.valueOf(phoneNumberId));
        assignPhoneNumber.setNumber_title("Reassign Phone Number to Admin");
        assignPhoneNumber.setVoice_reply("The person you are calling is not available");
        assignPhoneNumber.setMasked_number("+1234567890");
        assignPhoneNumber.setAvailability(availabilityString);

        Response response = RestClient.doPut1("JSON", commURL, basePath, ThreadManager.getOwnerAlbatrossToken()+123, null,pathParamters, true, assignPhoneNumber);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);
    }

    private static String getAvailabilityString() {
        try {
            Map<String, Object> startTime = new HashMap<>();
            startTime.put("id", "06:00 AM");
            startTime.put("value", 21600);

            Map<String, Object> endTime = new HashMap<>();
            endTime.put("id", "06:00 PM");
            endTime.put("value", 64800);

            Map<String, String> days = new HashMap<>();
            days.put("id", "Mon - Fri");
            days.put("value", "0,1,2,3,4");

            Map<String, Object> availabilityMap = new HashMap<>();
            availabilityMap.put("start_time", startTime);
            availabilityMap.put("end_time", endTime);
            availabilityMap.put("days", days);

            List<Map<String, Object>> availability = Collections.singletonList(availabilityMap);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(availability);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "[]"; // Return an empty JSON array as a fallback
        }
    }


}
