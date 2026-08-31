package io.recruitcrm.albatross.activityTemplate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.ActivityTemplatePage;
import io.rcrm.api.pojo.albatross.New_activity_templatePage;
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
public class EditActivityTemplateTest extends TestBase {

    commanFunction function = new commanFunction();
    String albatrossTkn;
    String generatedString = RandomStringUtils.randomAlphabetic(4);

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void editActivityTemplate() {
        int id = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().get("data.activity_template.id");

        ActivityTemplatePage updatedPayload = getUpdatedTemplatePayload();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", String.valueOf(id));
        String editPath = "activity-templates/{id}";

        Response response = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossTkn, null, pathParameters, true, updatedPayload);
        response.then().statusCode(200)
                .body("status", Matchers.containsString("success"))
                .body("message_type", Matchers.containsString("is-success"))
                .body("message", Matchers.equalTo("Activity Template Updated Successfully"))
                .body("data.activity_template.id", Matchers.equalTo(id))
                .body("data.activity_template.name", Matchers.containsString("Edited Template"))
                .body("data.activity_template.template_body", Matchers.containsString("Edited Template"))
                .body("data.activity_template.relatedto_type_id", Matchers.equalTo(2))
                .body("data.activity_template.activity_type", Matchers.equalTo(1))
                .body("data.activity_template.is_shared", Matchers.equalTo("1"));
    }


    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void editActivityTemplateInvalidId() {
        int id = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().get("data.activity_template.id");

        ActivityTemplatePage updatedPayload = getUpdatedTemplatePayload();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", id + "123");
        String editPath = "activity-templates/{id}";

        Response response = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossTkn, null, pathParameters, true, updatedPayload);
        response.then().statusCode(200)
                .body("message_type", Matchers.equalTo("is-danger"))
                .body("message", Matchers.equalTo("Failed To Update Activity Template : Activity Template Not Found"))
                .body("data", Matchers.empty());
    }


    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void editActivityTemplateInvalidAuth() {
        int id = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().getInt("data.activity_template.id");

        ActivityTemplatePage updatedPayload = getUpdatedTemplatePayload();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", String.valueOf(id));
        String editPath = "activity-templates/{id}";

        Response response = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossTkn + "123", null, pathParameters, true, updatedPayload);
        response.then().statusCode(401)
                .body("error", Matchers.equalTo("Unauthorized"))
                .body("data", Matchers.nullValue())
                .body("message", Matchers.nullValue())
                .body("message_type", Matchers.nullValue());
    }


    private ActivityTemplatePage getUpdatedTemplatePayload() {
        New_activity_templatePage updatedTemplate = new New_activity_templatePage();
        updatedTemplate.setName("Edited Template " + generatedString);
        updatedTemplate.setRelatedToTypeId(2);
        updatedTemplate.setActivityType(1);
        updatedTemplate.setTemplateBody("Edited Template body " + generatedString);
        updatedTemplate.setIsShared("1");

        ActivityTemplatePage updatedPayload = new ActivityTemplatePage();
        updatedPayload.setActivityTemplate(updatedTemplate);
        return updatedPayload;
    }
}
