package io.recruitcrm.albatross.xmlfeed;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerXmlFeed;
import io.rcrm.api.pojo.albatross.xmlfeed.SaveCustomXmlFeed;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateCustomXmlFeedTest extends TestBase {

    JavaFakerXmlFeed javaFakerXmlFeed = new JavaFakerXmlFeed();

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void createNewCustomXmlFeedWithInvalidAuth() {
        SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed(javaFakerXmlFeed.getXmlFeedTitle(), javaFakerXmlFeed.getXmlHeader(), javaFakerXmlFeed.getXmlBody(), javaFakerXmlFeed.getDecodeValue(), javaFakerXmlFeed.getPreselectValue(), javaFakerXmlFeed.getJobLastUpdatedOnLimit());
        String basePath = "custom-xml/save";
        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true, saveCustomXmlFeed);
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void createNewCustomXmlFeedWithEmptyHeader() {
        SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed(javaFakerXmlFeed.getXmlFeedTitle(), "", javaFakerXmlFeed.getXmlBody(), javaFakerXmlFeed.getDecodeValue(), javaFakerXmlFeed.getPreselectValue(), javaFakerXmlFeed.getJobLastUpdatedOnLimit());
        String basePath = "custom-xml/save";
        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, saveCustomXmlFeed);
        response.then().statusCode(422);
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void createNewCustomXmlFeedWithEmptyBody() {
        SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed(javaFakerXmlFeed.getXmlFeedTitle(), javaFakerXmlFeed.getXmlHeader(), "", javaFakerXmlFeed.getDecodeValue(), javaFakerXmlFeed.getPreselectValue(), javaFakerXmlFeed.getJobLastUpdatedOnLimit());
        String basePath = "custom-xml/save";
        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, saveCustomXmlFeed);
        response.then().statusCode(422);
    }

}
