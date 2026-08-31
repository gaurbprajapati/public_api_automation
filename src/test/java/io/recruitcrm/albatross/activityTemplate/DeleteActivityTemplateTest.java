package io.recruitcrm.albatross.activityTemplate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteActivityTemplateTest extends TestBase {
    commanFunction function = new commanFunction();
    String albatrossTkn;
    String generatedString = RandomStringUtils.randomAlphabetic(4);

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void deleteActivityTemplate() {
        int id = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().get("data.activity_template.id");
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(id));
        String basePath = "activity-templates/{id}";

        Response response = RestClient.doDelete("JSON", albatrossURL, basePath, albatrossTkn, null, pathParameters, true);
        response.then().statusCode(200)
                .body("status", Matchers.equalTo("success"))
                .body("message", Matchers.equalTo("Activity Template Deleted Successfully"))
                .body("message_type", Matchers.equalTo("is-success"))
                .body("data.id", Matchers.equalTo(String.valueOf(id)));
    }


    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void deleteActivityTemplateInvalidId() {
        int id = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().get("data.activity_template.id");
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", id + "123");
        String basePath = "activity-templates/{id}";

        Response response = RestClient.doDelete("JSON", albatrossURL, basePath, albatrossTkn, null, pathParameters, true);
        response.then().statusCode(200)
                .body("message_type", Matchers.equalTo("is-danger"))
                .body("message", Matchers.equalTo("Failed To Delete Activity Template : Activity Template Not Found"))
                .body("data", Matchers.empty());
    }


    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void deleteActivityTemplateInvalidAuth() {
        int id = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().get("data.activity_template.id");
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(id));
        String basePath = "activity-templates/{id}";

        Response response = RestClient.doDelete("JSON", albatrossURL, basePath, albatrossTkn + "123", null, pathParameters, true);
        response.then().statusCode(401)
                .body("error", Matchers.equalTo("Unauthorized"))
                .body("data", Matchers.nullValue())
                .body("message", Matchers.nullValue())
                .body("message_type", Matchers.nullValue());
    }
}
