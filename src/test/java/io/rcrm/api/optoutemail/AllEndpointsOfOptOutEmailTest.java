package io.rcrm.api.optoutemail;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.OptOutEmail;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndpointsOfOptOutEmailTest extends TestBase {
    commanFunction function = new commanFunction();
    String basePathPost = "email/opt-out/status";
    String basePathGet = "email/opted-out";
    String candidateSlug = null;
    public AllEndpointsOfOptOutEmailTest() {
        super();
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void optOutEmail(){
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        candidateSlug = jsonCandidate.get("slug");

        OptOutEmail optOutemail = new OptOutEmail();
        optOutemail.setRelated_to_type("candidate");
        optOutemail.setRelated_to(candidateSlug);
        optOutemail.setOpt_out("1");

        Response response = RestClient.doPost("JSON", baseURL, basePathPost, ThreadManager.getAccountApiKey(), null, true, optOutemail);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("is_email_opted_out", Matchers.is("true"));
        response.then().body("email_opt_out_source", Matchers.notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(priority = 1, groups = "nightly-build")
    public void optInEmail(){
        OptOutEmail optOutemail = new OptOutEmail();
        optOutemail.setRelated_to_type("candidate");
        optOutemail.setRelated_to(candidateSlug);
        optOutemail.setOpt_out("0");

        Response response = RestClient.doPost("JSON", baseURL, basePathPost, ThreadManager.getAccountApiKey(), null, true, optOutemail);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("is_email_opted_out", Matchers.is("false"));
        response.then().body("email_opt_out_source", Matchers.nullValue());
    }

    @Owner("Harika")
    @Test(dependsOnMethods = "optOutEmail", groups = "nightly-build")
    public void getAllOptedOutEntities(){
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("related_to_type", "candidate");
        queryParameters.put("limit", "100");
        queryParameters.put("page", "1");
        queryParameters.put("sort_by", "updatedon");
        queryParameters.put("sort_order", "desc");

        Response response = RestClient.doGet("JSON", baseURL, basePathGet, ThreadManager.getAccountApiKey(),
                queryParameters, null, true);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("current_page", Matchers.is(1));
        response.then().body("data[0].id", Matchers.notNullValue());
        response.then().body("data[0].is_email_opted_out", Matchers.is("true"));
        response.then().body("data[0].email_opt_out_source", Matchers.notNullValue());

    }
}
