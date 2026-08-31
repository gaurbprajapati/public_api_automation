package io.recruitcrm.albatross.job;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.xmlfeed.BulkUpdateXmlFeedField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Collections;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class JobUpdateFieldsTest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void bulkUpdateJobsXmlFeedWithInvalidAuth() {
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().getString("slug");
        String jobSlug1 = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath().getString("slug");
        int jobId1 = allCrudFunctions.getJobResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), jobSlug1).jsonPath().getInt("data.job.id");
        allCrudFunctions.createCustomXmlFeed(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken());
        JsonPath jsonFeeds = allCrudFunctions.getXmlFeedsList(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        String xmlFeeds = jsonFeeds.getString("data.default_job_boards[0].id") + "-" + jsonFeeds.getString("data.default_job_boards[0].jobboard_type") +
                "," + jsonFeeds.getString("data.default_job_boards[1].id") + "-" + jsonFeeds.getString("data.default_job_boards[1].jobboard_type") +
                "," + jsonFeeds.getString("data.records.data[0].id") + "-" + jsonFeeds.getString("data.records.data[0].jobboard_type");
        BulkUpdateXmlFeedField bulkUpdateXmlFeedField = new BulkUpdateXmlFeedField("xml_feeds", xmlFeeds, "jobboard_job_association_t", new ArrayList<>(Collections.singletonList(jobId1)), true);
        String basePath = "global/update-fields";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "123", null, true, bulkUpdateXmlFeedField);
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

}
