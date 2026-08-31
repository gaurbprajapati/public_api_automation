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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AssignPhoneNumberTest extends TestBase {

    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    int phoneNumberId, assignedPhoneNumberId, userId1, userId2;
    String albatrossAuthToken;

    @BeforeClass(alwaysRun = true)    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void assignPhoneNumberPOST_200() {

        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

        Response getUsers = albatrossFunctions.getUsers(albatrossURL, albatrossAuthToken);
        JsonPath user = getUsers.jsonPath();
        userId1 = user.get("data.records[0].id");
        userId2 = user.get("data.records[1].id");

        ReaperIntegration.insertPurchasedNumber(ThreadManager.getAccount().getAccountId(), user.get("data.records[0].id"));

        phoneNumberId = albatrossFunctions.getPurchasedPhoneNumberId(commURL, albatrossAuthToken).jsonPath().get("phone_numbers[0].id");

        String availabilityString = getAvailabilityString();

        String basePath = "phone-numbers/assign";

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id(String.valueOf(userId1));
        assignPhoneNumber.setPhone_number_id(String.valueOf(phoneNumberId));
        assignPhoneNumber.setNumber_title("Owner Phone Number");
        assignPhoneNumber.setVoice_reply("The person you are calling is not available");
        assignPhoneNumber.setMasked_number("+1234567890");
        assignPhoneNumber.setAvailability(availabilityString);

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, assignPhoneNumber);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-success");
        Assert.assertEquals(response.jsonPath().get("message"), "Phone number assigned!");
        Assert.assertEquals(response.jsonPath().getInt("data.phone_number_id"), phoneNumberId, "Phone number id is not as expected");
        Assert.assertEquals(response.jsonPath().getInt("data.assigned_user_id"), userId1, "User id is not as expected");
        response.then().body("data.id", Matchers.notNullValue());

        assignedPhoneNumberId = response.jsonPath().get("data.id");
    }


    @Owner("Harika")
    @Test(dependsOnMethods = "assignPhoneNumberPOST_200", groups = "nightly-build")
    public void assignNumberToAssignedUserId() {

        String availabilityString = getAvailabilityString();

        String basePath = "phone-numbers/assign";

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id(String.valueOf(userId1));
        assignPhoneNumber.setPhone_number_id(String.valueOf(phoneNumberId));
        assignPhoneNumber.setNumber_title("Owner Phone Number");
        assignPhoneNumber.setVoice_reply("The person you are calling is not available");
        assignPhoneNumber.setMasked_number("+1234567890");
        assignPhoneNumber.setAvailability(availabilityString);

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, assignPhoneNumber);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger");
        Assert.assertEquals(response.jsonPath().get("status"), "fail");
        Assert.assertEquals(response.jsonPath().get("message"), "User already assigned to another number");

    }

    @Owner("Harika")
    @Test(dependsOnMethods = "assignPhoneNumberPOST_200", groups = "nightly-build")
    public void assignSameNumberToAnotherUser() {

        String availabilityString = getAvailabilityString();

        String basePath = "phone-numbers/assign";

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id(String.valueOf(userId2));
        assignPhoneNumber.setPhone_number_id(String.valueOf(phoneNumberId));
        assignPhoneNumber.setNumber_title("Owner Phone Number");
        assignPhoneNumber.setVoice_reply("The person you are calling is not available");
        assignPhoneNumber.setMasked_number("+1234567890");
        assignPhoneNumber.setAvailability(availabilityString);

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, assignPhoneNumber);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger");
        Assert.assertEquals(response.jsonPath().get("status"), "fail");
        Assert.assertEquals(response.jsonPath().get("message"), "Number already assigned to another user");
    }

    @Owner("Harika")
    @Test(dataProvider = "getDataForAssignNumber", groups = "nightly-build")
    public void assignPhoneNumberPOST_422(String userId, String numberId, String title, String message) {

        String basePath = "phone-numbers/assign";

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id(userId);
        assignPhoneNumber.setPhone_number_id(numberId);
        assignPhoneNumber.setNumber_title(title);
        assignPhoneNumber.setVoice_reply(message);
        assignPhoneNumber.setMasked_number("");

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, assignPhoneNumber);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 422);

        if (userId.isEmpty()) {
            Assert.assertEquals(response.jsonPath().get("phone_number_id[0]"), "The phone number id field is required.");
            Assert.assertEquals(response.jsonPath().get("user_id[0]"), "The user id field is required.");
            Assert.assertEquals(response.jsonPath().get("number_title[0]"), "The number title field is required.");
            Assert.assertEquals(response.jsonPath().get("voice_reply[0]"), "The voice reply field is required.");
        } else if (userId.equals("abc")) {
            Assert.assertEquals(response.jsonPath().get("phone_number_id[0]"), "The phone number id must be an integer.");
            Assert.assertEquals(response.jsonPath().get("user_id[0]"), "The user id must be an integer.");
        } else {
            Assert.assertEquals(response.jsonPath().get("phone_number_id[0]"), "The selected phone number id is invalid.");
            Assert.assertEquals(response.jsonPath().get("user_id[0]"), "The selected user id is invalid.");
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void assignPhoneNumberPOST_401() {

        String basePath = "phone-numbers/assign";

        AssignNumber assignPhoneNumber = new AssignNumber();
        assignPhoneNumber.setUser_id("id");
        assignPhoneNumber.setPhone_number_id("id");
        assignPhoneNumber.setNumber_title("Number title");
        assignPhoneNumber.setVoice_reply("Voice reply");
        assignPhoneNumber.setMasked_number("");

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken + 123, null, true, assignPhoneNumber);

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

    @DataProvider(parallel = true)
    public Object[][] getDataForAssignNumber() {
        return new Object[][]{{"", "", "", ""}, {"abc", "abc", "Number Title", "Voice Reply"}, {"123", phoneNumberId + "123", "Number Title", "Voice Reply"}};
    }
}
