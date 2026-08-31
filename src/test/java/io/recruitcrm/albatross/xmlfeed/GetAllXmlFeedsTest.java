package io.recruitcrm.albatross.xmlfeed;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.albatross.xmlfeed.ListXmlFeeds;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllXmlFeedsTest extends TestBase {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void getAllXmlFeedWithInvalidAuth() {
        allCrudFunctions.createCustomXmlFeed(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken());
        ListXmlFeeds listXmlFeeds = new ListXmlFeeds("updated_on", "asc");
        String basePath = "custom-xml/list";
        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true, listXmlFeeds);
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

}
