package io.recruitcrm.albatross.job;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetJobStatusTest extends TestBase {

    public GetJobStatusTest() {
        super();
    }

    @Owner("Sai Teja SG")
    @Test
    public void getJobStatusByAccount() {
        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-status-by-account/get", ThreadManager.getOwnerAlbatrossToken(), null, true, null);


        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//jobStatus//getJobStatusByAccount.json"));
    }

    @Owner("Smit Patel")
    @Test
    public void getJobStatusByAccountInvalidEndpoint() {
        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-status-by-account/get12345", ThreadManager.getOwnerAlbatrossToken(), null, true, null);


        Assert.assertEquals(response.getStatusCode(), 404);
        response.then().body("message", Matchers.is(""));
    }

    @Owner("Akshaya Uppala")
    @Test
    public void getJobStatusByAccountUnauthorized() {
        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-status-by-account/get", ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, null);


        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }
}
