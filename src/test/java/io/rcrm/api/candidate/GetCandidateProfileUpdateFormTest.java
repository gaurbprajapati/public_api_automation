package io.rcrm.api.candidate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import com.qa.api.util.reaper.ThreadManager;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetCandidateProfileUpdateFormTest extends TestBase {

    public GetCandidateProfileUpdateFormTest() {
        super();
    }

    String basePath = "candidates/profile-update-request-form";

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void getProfileUpdateForm_200() {
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, null, true);

        response.then().statusCode(200);
        response.then().body("message", Matchers.is("success"));
        response.then().body("data.profile_update_setting_form_data", Matchers.notNullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//getProfileUpdateForm.json"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = "nightly-build")
    public void getProfileUpdateForm_404_InvalidEndpoint() {
        Response response = RestClient.doGet("JSON", baseURL, basePath + "123", ThreadManager.getAccountApiKey(), null, null, true);

        response.then().statusCode(404);
        response.then().body("error", Matchers.is(true));
        response.then().body("errorCode", Matchers.is(404));
        response.then().body("errorMessage", Matchers.containsString("Candidate doesn't exist"));
    }

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void getProfileUpdateForm_401_Unauthorized() {
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"123", null, null, true);

        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }
}

