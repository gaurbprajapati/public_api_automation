package io.recruitcrm.albatross.xmlfeed;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerXmlFeed;
import io.rcrm.api.pojo.albatross.xmlfeed.SaveCustomXmlFeed;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditCustomXmlFeedTest extends TestBase {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    JavaFakerXmlFeed javaFakerXmlFeed = new JavaFakerXmlFeed();

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void editCustomXmlFeedWithInvalidAuth() {
        JsonPath jsonFeed = allCrudFunctions.createCustomXmlFeed(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed();
        saveCustomXmlFeed.setTitle(javaFakerXmlFeed.getXmlFeedTitle());
        String basePath = "custom-xml/update-feed/"+jsonFeed.getInt("data.id");
        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true, saveCustomXmlFeed);
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

}
