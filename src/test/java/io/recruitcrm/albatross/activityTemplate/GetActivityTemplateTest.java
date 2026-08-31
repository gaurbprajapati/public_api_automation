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
public class GetActivityTemplateTest extends TestBase {

    commanFunction function = new commanFunction();
    String albatrossTkn;
    String generatedString = RandomStringUtils.randomAlphabetic(4);

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }


    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void getAllActivityTemplates() {
        createMultipleTemplates();

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sort_by", "updatedon");
        queryParams.put("sortOrder", "ASC");
        queryParams.put("page", "1");
        queryParams.put("page_size", "5");

        Response response = RestClient.doGet("JSON", albatrossURL, "activity-templates", albatrossTkn, queryParams, null, true);
        response.then().statusCode(200)
                .body("status", Matchers.equalTo("success"))
                .body("message_type", Matchers.equalTo("is-success"))
                .body("data.filtered_count", Matchers.equalTo(3))
                .body("data.records.size()", Matchers.greaterThanOrEqualTo(3));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void getActivityTemplatesInvalidAuth() {
        function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString);

        Response response = RestClient.doGet("JSON", albatrossURL, "activity-templates", albatrossTkn + "123", new HashMap<>(), null, true);
        response.then().statusCode(401)
                .body("error", Matchers.equalTo("Unauthorized"))
                .body("data", Matchers.nullValue())
                .body("message", Matchers.nullValue())
                .body("message_type", Matchers.nullValue());
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void getActivityTemplateById() {
        int id = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().getInt("data.activity_template.id");
        String path = "activity-templates/" + id;

        Response response = RestClient.doGet("JSON", albatrossURL, path, albatrossTkn, null, null, true);
        response.then().statusCode(200)
                .body("status", Matchers.equalTo("success"))
                .body("data.id", Matchers.equalTo(id));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void getActivityTemplateByInvalidId() {
        int validId = function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString).jsonPath().getInt("data.activity_template.id");
        String invalidPath = "activity-templates/" + (validId + 123);
        Response response = RestClient.doGet("JSON", albatrossURL, invalidPath, albatrossTkn, null, null, true);
        response.then().statusCode(200)
                .body("message", Matchers.equalTo("Activity Template Not Found"))
                .body("message_type", Matchers.equalTo("is-danger"))
                .body("data", Matchers.empty());
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void searchActivityTemplate() {
        generatedString = RandomStringUtils.randomAlphabetic(4);
        function.createActivityTemplate(5, 0, albatrossURL, albatrossTkn, true, generatedString);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("page_size", "1");
        queryParameters.put("search", generatedString);
        queryParameters.put("relatedto_type_id", "5");
        queryParameters.put("activity_type", "0");
        String basePath = "activity-templates";

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true);
        response.then().statusCode(200)
                .body("status", Matchers.containsString("success"))
                .body("message_type", Matchers.containsString("is-success"))
                .body("data.records[0].name", Matchers.containsString(generatedString))
                .body("data.filtered_count", Matchers.equalTo(1))
                .body("data.records[0].relatedto_type_id", Matchers.equalTo(5))
                .body("data.records[0].activity_type", Matchers.equalTo(0))
                .body("data.records[0].is_shared", Matchers.equalTo(1));
    }

    private void createMultipleTemplates() {
        int[][] templateData = {
                {0, 5},
                {1, 11},
                {3, 3}
        };

        for (int[] data : templateData) {
            function.createActivityTemplate(data[1], data[0], albatrossURL, albatrossTkn, true, generatedString);
        }
    }


}
