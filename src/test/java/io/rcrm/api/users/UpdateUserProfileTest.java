package io.rcrm.api.users;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;

import io.rcrm.api.pojo.UserProfileUpdateRequest;
import io.rcrm.api.pojo.CurrentUser;
import io.rcrm.api.pojo.CurrentUserDetails;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class UpdateUserProfileTest extends TestBase {

    int actualUserId;
    int actualUserDetailsId;
    String actualUserFirstName;
    String actualUserLastName;
    String actualUserEmail;
    String actualUserContactNumber;
    String actualUserCity;
    String actualUserCountry;
    String actualUserState;
    String actualUserLocale;
    int actualUserTimezone;

    // Cross account test fields
    private String tokenA;
    private String publicAPIKeyA;
    private String tokenB;
    private String publicAPIKeyB;

    private static final int DEFAULT_CURRENCY_ID = 53;
    private static final String SUCCESS_STATUS = "success";
    private static final String SUCCESS_MESSAGE = "User Updated";
    private static final String SUCCESS_MESSAGE_TYPE = "is-success";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        // Cross account setup
        tokenA = getTokenForAccount("AccountA", "valid");
        publicAPIKeyA = getAccountApiKey("AccountA");
        tokenB = getTokenForAccount("AccountB", "valid");
        publicAPIKeyB = getAccountApiKey("AccountB");

        // Get actual owner details
        getOwnerDetailsFromAPI();
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "timeFormatTestData", groups = "nightly-build")
    public void updateUserProfile_ValidTimeFormatTypes_Test(int timeFormatType, String formatDescription, int expectedStatusCode, String expectedStatus, String expectedMessage, String expectedMessageType) {
        CurrentUser currentUser = createCurrentUser();
        CurrentUserDetails currentUserDetails = createCurrentUserDetails(timeFormatType);
        UserProfileUpdateRequest requestPayload = new UserProfileUpdateRequest(currentUser, currentUserDetails);

        Response response = RestClient.doPost("JSON", albatrossURL, "users/update-profile/" + actualUserId, tokenA, null, true, requestPayload);
        validateSuccessfulResponse(response, formatDescription, expectedStatusCode, expectedStatus, expectedMessage, expectedMessageType);
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "invalidTimeFormatTestData", groups = "nightly-build")
    public void updateUserProfile_InvalidTimeFormatTypes_Test(int timeFormatType, String formatDescription, int expectedStatusCode, String expectedStatus, String expectedMessage, String expectedMessageType) {
        CurrentUser currentUser = createCurrentUser();
        CurrentUserDetails currentUserDetails = createCurrentUserDetails(timeFormatType);
        UserProfileUpdateRequest requestPayload = new UserProfileUpdateRequest(currentUser, currentUserDetails);

        Response response = RestClient.doPost("JSON", albatrossURL, "users/update-profile/" + actualUserId, tokenA, null, true, requestPayload);
        validateErrorResponse(response, expectedStatusCode, expectedStatus, expectedMessage, expectedMessageType);
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void updateUserProfile_MissingTimeFormatType_Test() {
        CurrentUser currentUser = createCurrentUser();

        // Create CurrentUserDetails without time_format_type
        CurrentUserDetails currentUserDetails = new CurrentUserDetails();
        currentUserDetails.setId(actualUserDetailsId);
        currentUserDetails.setTimezone(actualUserTimezone);
        currentUserDetails.setCurrencyid(DEFAULT_CURRENCY_ID);
        // time_format_type is intentionally not set

        UserProfileUpdateRequest requestPayload = new UserProfileUpdateRequest(currentUser, currentUserDetails);
        Response response = RestClient.doPost("JSON", albatrossURL, "users/update-profile/" + actualUserId, tokenA, null, true, requestPayload);

        validateErrorResponse(response, 422, "fail", "Failed to Update User : The time format field is required.", "is-danger");
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void updateUserProfile_UnauthorizedAccess_Test() {
        CurrentUser currentUser = createCurrentUser();
        CurrentUserDetails currentUserDetails = createCurrentUserDetails(1);
        UserProfileUpdateRequest requestPayload = new UserProfileUpdateRequest(currentUser, currentUserDetails);

        // Make POST request with invalid token
        Response response = RestClient.doPost("JSON", albatrossURL, "users/update-profile/" + actualUserId, tokenA + "123",
                null, true, requestPayload);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 for unauthorized request");
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void updateUserProfile_InvalidUserId_Test() {

        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(99999); // Invalid user ID
        currentUser.setFirstname(actualUserFirstName);
        currentUser.setLastname(actualUserLastName);
        currentUser.setEmail(actualUserEmail);
        currentUser.setContactnumber(actualUserContactNumber);
        currentUser.setCity(actualUserCity);
        currentUser.setCountry(actualUserCountry);
        currentUser.setState(actualUserState != null ? actualUserState : "");
        currentUser.setLocale(actualUserLocale);

        CurrentUserDetails currentUserDetails = new CurrentUserDetails();
        currentUserDetails.setId(99999); // Invalid user Details ID
        currentUserDetails.setTimezone(actualUserTimezone);
        currentUserDetails.setCurrencyid(DEFAULT_CURRENCY_ID);
        currentUserDetails.setTimeFormatType(1);

        UserProfileUpdateRequest requestPayload = new UserProfileUpdateRequest(currentUser, currentUserDetails);

        // Make POST request with invalid user ID
        Response response = RestClient.doPost("JSON", albatrossURL, "users/update-profile/99999",
                tokenA, null, true, requestPayload);

        Assert.assertEquals(response.getStatusCode(), 422, "Expected error status code for invalid user ID");
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void updateUserProfile_CrossAccountAccess_Test() {
        CurrentUser currentUser = createCurrentUser();
        CurrentUserDetails currentUserDetails = createCurrentUserDetails(1);
        UserProfileUpdateRequest requestPayload = new UserProfileUpdateRequest(currentUser, currentUserDetails);

        // Make POST request with AccountB token to update AccountA user profile
        Response response = RestClient.doPost("JSON", albatrossURL, "users/update-profile/" + actualUserId, tokenB, null, true, requestPayload);

        // API returns 200 but with failure details in response body
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for cross-account access");

        response.then().body("message", Matchers.containsString("Failed to Update User"));
        response.then().body("message", Matchers.containsString("User doesn't exist"));
        response.then().body("message_type", Matchers.is("is-danger"));
        response.then().body("silent_progress", Matchers.equalTo(false));
    }


    // Helper method to create CurrentUser object
    private CurrentUser createCurrentUser() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(actualUserId);
        currentUser.setFirstname(actualUserFirstName);
        currentUser.setLastname(actualUserLastName);
        currentUser.setEmail(actualUserEmail);
        currentUser.setContactnumber(actualUserContactNumber);
        currentUser.setCity(actualUserCity);
        currentUser.setCountry(actualUserCountry);
        currentUser.setState(actualUserState != null ? actualUserState : "");
        currentUser.setLocale(actualUserLocale);
        return currentUser;
    }

    // Helper method to create CurrentUserDetails object
    private CurrentUserDetails createCurrentUserDetails(int timeFormatType) {
        CurrentUserDetails currentUserDetails = new CurrentUserDetails();
        currentUserDetails.setId(actualUserDetailsId);
        currentUserDetails.setTimezone(actualUserTimezone);
        currentUserDetails.setCurrencyid(DEFAULT_CURRENCY_ID);
        currentUserDetails.setTimeFormatType(timeFormatType);
        return currentUserDetails;
    }

    private void validateSuccessfulResponse(Response response, String expectedTimeFormat, int expectedStatusCode, String expectedStatus, String expectedMessage, String expectedMessageType) {
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Expected status code 200 for " + expectedTimeFormat + " request");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("status"), expectedStatus, "Expected status to be " + expectedStatus);
        Assert.assertEquals(jsonPath.getString("message"), expectedMessage, "Expected message mismatch");
        Assert.assertEquals(jsonPath.getString("message_type"), expectedMessageType, "Expected message type mismatch");
        Assert.assertNotNull(jsonPath.get("actionid"), "Action ID should not be null");
    }

    private void validateErrorResponse(Response response, int expectedStatusCode, String expectedStatus, String expectedMessage, String expectedMessageType) {
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Expected status code " + expectedStatusCode + " for invalid request");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("status"), expectedStatus, "Expected status to be " + expectedStatus);
        Assert.assertEquals(jsonPath.getString("message"), expectedMessage, "Expected error message");
        Assert.assertEquals(jsonPath.getString("message_type"), expectedMessageType, "Expected message type to be " + expectedMessageType);
    }

    private void getOwnerDetailsFromAPI() {
        try {
            // Call the getAllUsers API to get actual owner details
            Response response = RestClient.doGet("JSON", baseURL, "users", publicAPIKeyA, null, null, true);
            assert response != null;
            if (response.getStatusCode() == 200) {
                JsonPath jsonPath = response.jsonPath();

                // Get the first user (Account Owner) details
                actualUserId = jsonPath.getInt("[0].id");
                actualUserFirstName = jsonPath.getString("[0].first_name");
                actualUserLastName = jsonPath.getString("[0].last_name");
                actualUserEmail = jsonPath.getString("[0].email");
                actualUserContactNumber = jsonPath.getString("[0].contact_number");
                actualUserCity = jsonPath.getString("[0].city");
                actualUserCountry = jsonPath.getString("[0].country");
                actualUserState = jsonPath.getString("[0].state");
                actualUserLocale = jsonPath.getString("[0].application_language");
                actualUserTimezone = jsonPath.getInt("[0].timezone");

                // Get the current user details ID from Reaper
                Response userDetailsResponse = ReaperIntegration.getCurrentUserDetailsId(actualUserId);
                JsonPath userDetailsJsonPath = userDetailsResponse.jsonPath();
                actualUserDetailsId = userDetailsJsonPath.get("userDetailsId");
            }

        } catch (Exception e) {
            throw new AssertionError("Error retrieving owner details from API: " + e.getMessage(), e);
        }
    }

    @DataProvider(name = "timeFormatTestData", parallel = true)
    public Object[][] timeFormatTestData() {
        return new Object[][]{
                {1, "24-hour format", 200, SUCCESS_STATUS, SUCCESS_MESSAGE, SUCCESS_MESSAGE_TYPE},
                {0, "12-hour format", 200, SUCCESS_STATUS, SUCCESS_MESSAGE, SUCCESS_MESSAGE_TYPE}
        };
    }

    @DataProvider(name = "invalidTimeFormatTestData", parallel = true)
    public Object[][] invalidTimeFormatTestData() {
        return new Object[][]{
                {2, "invalid value", 422, "fail", "Failed to Update User : The selected time format is invalid.", "is-danger"},
                {-1, "negative value", 422, "fail", "Failed to Update User : The selected time format is invalid.", "is-danger"}
        };
    }
}
