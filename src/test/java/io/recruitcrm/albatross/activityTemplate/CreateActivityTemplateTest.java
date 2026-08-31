package io.recruitcrm.albatross.activityTemplate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.ActivityTemplatePage;
import io.rcrm.api.pojo.albatross.New_activity_templatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateActivityTemplateTest extends TestBase {

    String generatedString = RandomStringUtils.randomAlphabetic(4);
    String albatrossTkn;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "getActivityTemplateData", groups = "nightly-build")
    public void createActivityTemplate(String activityTypeName, int activityTypeId, String relatedToTypeName, int relatedToTypeId) {
        New_activity_templatePage template = new New_activity_templatePage();
        template.setName(relatedToTypeName + " Activity Template " + generatedString);
        template.setRelatedToTypeId(relatedToTypeId);
        template.setActivityType(activityTypeId);
        template.setTemplateBody("Template body " + activityTypeName + " " + generatedString);
        template.setIsShared("1");

        ActivityTemplatePage activityTemplatePage = new ActivityTemplatePage();
        activityTemplatePage.setActivityTemplate(template);
        Response response = RestClient.doPost("JSON", albatrossURL, "activity-templates", albatrossTkn, null, true, activityTemplatePage);
        response.then().statusCode(200)
                .body("status", Matchers.containsString("success"))
                .body("message", Matchers.equalTo("Activity Template Saved Successfully"))
                .body("message_type", Matchers.equalTo("is-success"))
                .body("data.activity_template.name", Matchers.containsString(generatedString))
                .body("data.activity_template.template_body", Matchers.containsString(activityTypeName))
                .body("data.activity_template.relatedto_type_id", Matchers.equalTo(relatedToTypeId))
                .body("data.activity_template.activity_type", Matchers.equalTo(activityTypeId))
                .body("data.activity_template.is_shared", Matchers.equalTo("1"))
                .body("data.activity_template.id", Matchers.notNullValue());
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "getInvalidActivityTemplateData", groups = "nightly-build")
    public void createActivityTemplate_422(String activityTypeName, int activityTypeId, String relatedToTypeName, int relatedToTypeId) {
        New_activity_templatePage template = new New_activity_templatePage();
        template.setName(relatedToTypeName + " Activity Template " + generatedString);
        template.setRelatedToTypeId(relatedToTypeId);
        template.setActivityType(activityTypeId + 123);
        template.setTemplateBody("Template body " + activityTypeName + " " + generatedString);
        template.setIsShared("123");

        ActivityTemplatePage activityTemplatePage = new ActivityTemplatePage();
        activityTemplatePage.setActivityTemplate(template);
        Response response = RestClient.doPost("JSON", albatrossURL, "activity-templates", albatrossTkn, null, true, activityTemplatePage);
        response.then().statusCode(422)
                .body("data", Matchers.empty())
                .body("message_type", Matchers.equalTo("is-danger"))
                .body("message", Matchers.containsString("The selected Activity Type is invalid."))
                .body("message", Matchers.containsString("The selected Share With Teammates Toggle is invalid."));
    }


    @Owner("Suhel Bhadane")
    @Test(dataProvider = "getInvalidActivityTemplateData", groups = "nightly-build")
    public void createActivityTemplateWithInvalidAuth(String activityTypeName, int activityTypeId, String relatedToTypeName, int relatedToTypeId) {
        New_activity_templatePage template = new New_activity_templatePage();
        template.setName(relatedToTypeName + " Activity Template " + generatedString);
        template.setRelatedToTypeId(relatedToTypeId);
        template.setActivityType(activityTypeId);
        template.setTemplateBody("Template body " + activityTypeName + " " + generatedString);
        template.setIsShared("1");

        ActivityTemplatePage activityTemplatePage = new ActivityTemplatePage();
        activityTemplatePage.setActivityTemplate(template);
        Response response = RestClient.doPost("JSON", albatrossURL, "activity-templates", albatrossTkn + "123", null, true, activityTemplatePage);
        response.then().statusCode(401)
                .body("error", Matchers.equalTo("Unauthorized"))
                .body("data", Matchers.nullValue())
                .body("message", Matchers.nullValue())
                .body("message_type", Matchers.nullValue());
    }


    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void createActivityTemplateWithEmptyFields() {
        New_activity_templatePage template = new New_activity_templatePage();
        template.setRelatedToTypeId(null);
        template.setActivityType(null);
        template.setName("");
        template.setTemplateBody("");
        template.setIsShared("");

        ActivityTemplatePage payload = new ActivityTemplatePage();
        payload.setActivityTemplate(template);
        Response response = RestClient.doPost("JSON", albatrossURL, "activity-templates", albatrossTkn, null, true, payload);
        response.then().statusCode(422)
                .body("data", Matchers.empty())
                .body("message_type", Matchers.equalTo("is-danger"))
                .body("message", Matchers.containsString("The Template Name field is required."))
                .body("message", Matchers.containsString("The Template Body field is required."))
                .body("message", Matchers.containsString("The Share With Teammates Toggle field is required."))
                .body("message", Matchers.containsString("The Activity Type field is required."))
                .body("message", Matchers.containsString("The Related To Type field is required."));
    }

    @DataProvider(parallel = true)
    public Object[][] getActivityTemplateData() {
        return new Object[][]{
                {"Note", 0, "Candidate", 5},
                {"Note", 0, "Contact", 2},
                {"Note", 0, "Jobs", 4},
                {"Task", 0, "Deal", 11},
                {"Call Log", 3, "Company", 3}
        };
    }

    @DataProvider
    public Object[][] getInvalidActivityTemplateData() {
        return new Object[][]{
                {"Note", 0, "Candidate", 5}
        };
    }
}
