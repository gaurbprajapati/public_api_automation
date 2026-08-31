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
public class OptOutEmail_GetTest extends TestBase {
    commanFunction function = new commanFunction();

    String basePath = "email/opted-out";

    public OptOutEmail_GetTest() {
        super();
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "optedOutGetDetails", groups = "nightly-build")
    public void getAllOptedOutEntities(String relatedToType, String sortBy, String sortOrder) {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("related_to_type", relatedToType);
        queryParameters.put("limit", "100");
        queryParameters.put("page", "1");
        queryParameters.put("sort_by", sortBy);
        queryParameters.put("sort_order", sortOrder);

        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
                queryParameters, null, true);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("current_page", Matchers.is(1));
        response.then().body("data[0].id", Matchers.notNullValue());
        response.then().body("data[0].is_email_opted_out", Matchers.is("true"));
        response.then().body("data[0].email_opt_out_source", Matchers.notNullValue());

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getAllOptedOutEntitiesWithInvalidFields() {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("related_to_type", "candidate123");
        queryParameters.put("sort_by", "abc");
        queryParameters.put("sort_order", "xyz");
        queryParameters.put("limit", "pqr");
        queryParameters.put("page", "1");

        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
                queryParameters, null, true);

        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("related_to_type[0]", Matchers.containsString("The selected related to type is invalid."));
        response.then().body("sort_by[0]", Matchers.containsString("The selected sort by is invalid."));
        response.then().body("sort_order[0]", Matchers.containsString("The selected sort order is invalid."));
        response.then().body("limit[0]", Matchers.containsString("The limit must be an integer."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getAllOptedOutEntitiesWithoutRequiredFields(){
        Map<String, String> queryParameters = new HashMap<String, String>();

        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
                queryParameters, null, true);

        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("related_to_type[0]", Matchers.containsString("The related to type field is required."));
    }

    @Owner("Harika")
    @Test(dataProvider = "getDependentFields", groups = "nightly-build")
    public void verifyDependencyOfFields(String dependentField) {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("related_to_type", "candidate");
        queryParameters.put("limit", "100");
        queryParameters.put("page", "1");

        if (dependentField.equals("sort_by")) {
            queryParameters.put("sort_by", "createdon");

            Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
                    queryParameters, null, true);

            Assert.assertEquals(response.getStatusCode(), 422);
            response.then().body("sort_order[0]", Matchers.containsString("The sort order field is required when sort by is present."));
        } else {
            queryParameters.put("sort_order", "desc");

            Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
                    queryParameters, null, true);

            Assert.assertEquals(response.getStatusCode(), 422);
            response.then().body("sort_by[0]", Matchers.containsString("The sort by field is required when sort order is present."));
        }
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void unAuthorizedUserCannotGetAllOptedOutEntities() {

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("related_to_type", "candidate");
        queryParameters.put("limit", "100");
        queryParameters.put("page", "1");
        queryParameters.put("sort_by", "updatedon");
        queryParameters.put("sort_order", "desc");

        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"1234",
                queryParameters, null, true);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.is("Unauthorized"));

    }

    @DataProvider
    public Object[][] optedOutGetDetails() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        String candidateSlug = jsonCandidate.get("slug");

        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        String companySlug = jsonCompany.get("slug");

        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug = jsonContact.get("slug");

        optOutEmail("candidate", candidateSlug);
        optOutEmail("contact", contactSlug);

        Object data[][] = {
                {"candidate", "updatedon", "desc"},
                {"candidate", "updatedon", "asc"},
                {"contact", "createdon", "desc"},
                {"contact", "createdon", "asc"}
        };

        return data;
    }

    @DataProvider
    public Object[][] getDependentFields() {
        Object data[][] = {
                {"sort_by"},
                {"sort_order"}
        };
        return data;
    }

    public void optOutEmail(String relatedToType, String entitySlug) {
        OptOutEmail optOutemail = new OptOutEmail();
        optOutemail.setRelated_to_type(relatedToType);
        optOutemail.setRelated_to(entitySlug);
        optOutemail.setOpt_out("1");

        Response response = RestClient.doPost("JSON", baseURL, "email/opt-out/status", ThreadManager.getAccountApiKey(), null, true, optOutemail);

        response.then().statusCode(200);
        response.then().body("is_email_opted_out", Matchers.is("true"));
        response.then().body("email_opt_out_source", Matchers.notNullValue());

    }
}
