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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class OptOutEmail_PostTest extends TestBase {
    commanFunction function = new commanFunction();

    String basePath = "email/opt-out/status";

    public OptOutEmail_PostTest() {
        super();
    }

    @Owner("Harika")
    @Test(dataProvider = "getOptOutPostDetails", groups = "nightly-build")
    public void optOutInEmail(String relatedToType, String entitySlug, String optOut) {
        OptOutEmail optOutemail = new OptOutEmail();
        optOutemail.setRelated_to_type(relatedToType);
        optOutemail.setRelated_to(entitySlug);
        optOutemail.setOpt_out(optOut);

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, optOutemail);

        verfiy200ForOptOutInEmail(response, optOut);

    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getInvalidFields", groups = "nightly-build")
    public void optOutEmailWithInvalidFields(String relatedToType, String entitySlug, String optOutField) {
        OptOutEmail optOutemail = new OptOutEmail();
        optOutemail.setRelated_to_type(relatedToType);
        optOutemail.setRelated_to(entitySlug);
        optOutemail.setOpt_out(optOutField);

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, optOutemail);

        if(entitySlug==null){
            //invalid related_to_type & opt_out
            Assert.assertEquals(response.getStatusCode(), 422);
            response.then().body("related_to_type[0]", Matchers.containsString("The selected related to type is invalid."));
            response.then().body("opt_out[0]", Matchers.containsString("The opt out field must be true or false."));
        }
        else{
            //invalid related_to (entitySlug)
            Assert.assertEquals(response.getStatusCode(), 404);
            response.then().body("errorMessage", Matchers.equalToIgnoringCase(relatedToType + " doesn't exist"));
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void optOutEmailWithoutRequiredFields() {
        OptOutEmail optOutemail = new OptOutEmail();

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, optOutemail);

        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("related_to_type[0]", Matchers.containsString("The related to type field is required."));
        response.then().body("related_to[0]", Matchers.containsString("The related to field is required."));
        response.then().body("opt_out[0]", Matchers.containsString("The opt out field is required."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void unAuthorizedUserCannotOptOut() {

        OptOutEmail optOutemail = new OptOutEmail();
        optOutemail.setRelated_to_type("candidate");
        optOutemail.setRelated_to("1234");
        optOutemail.setOpt_out("1");

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"1234", null, true, optOutemail);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @DataProvider
    public Object[][] getOptOutPostDetails() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        String candidateSlug = jsonCandidate.get("slug");

        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        String companySlug = jsonCompany.get("slug");

        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug = jsonContact.get("slug");

        Object data[][] = {
                {"candidate", candidateSlug, "1"},
                {"candidate", candidateSlug, "0"},
                {"contact", contactSlug, "1"},
                {"contact", contactSlug, "0"}
        };

        return data;
    }

    @DataProvider
    public Object[][] getInvalidFields() {

        Object data[][] = {
                {"candidate1", null, "2"}, //invalid related_to_type & opt_out
                {"candidate", "1234", "1"}, //invalid related_to
                {"contact", "1234", "1"}, //invalid related_to
        };
        return data;
    }

    public void verfiy200ForOptOutInEmail(Response response, String optOut) {

        response.then().statusCode(200);
        if (optOut.equals("1")) {
            response.then().body("is_email_opted_out", Matchers.is("true"));
            response.then().body("email_opt_out_source", Matchers.notNullValue());
        } else {
            response.then().body("is_email_opted_out", Matchers.is("false"));
            response.then().body("email_opt_out_source", Matchers.nullValue());
        }

    }

}
