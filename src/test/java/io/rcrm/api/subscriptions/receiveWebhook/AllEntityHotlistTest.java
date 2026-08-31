package io.rcrm.api.subscriptions.receiveWebhook;

import com.qa.api.util.WebhookHelper;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.RetryOn500OrSkippedAnalyzer;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Hotlist;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import io.rcrm.api.testbase.TestBase.AccountType;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEntityHotlistTest extends TestBase {
    commanFunction function=new commanFunction();
    WebhookHelper webhookHelper;
    JsonPath responseFromWebhook;
    @BeforeMethod
    public void setUp(){
        function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        webhookHelper = new WebhookHelper();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "entities", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class, groups = "nightly-build")
    public void eventHotlistCreated(String entity){
        String event="hotlist.created";
        function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

        Response response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
        String entitySlug=response.jsonPath().getString("id");
        try {
            responseFromWebhook = new JsonPath(webhookHelper.getData(entitySlug));
        } catch (Exception e) {
            Assert.fail("Failed to fetch Webhook Data for "+entity+" entity "+"for event "+event);
        }
        //Verify Response
        Assert.assertEquals(responseFromWebhook.getString("id"), entitySlug);

    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "entities", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class, groups = "nightly-build")
    public void eventHotlistUpdated(String entity){
        String event="hotlist.updated";
        function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

        Response response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        int hotlistId=response.jsonPath().get("id");
        String entitySlug=response.jsonPath().getString("id");

        //Update Hotlist
        Hotlist hotlist=new Hotlist();
        hotlist.setFirst_name("Updated Hotlist "+entity);
        hotlist.setShared(1);
        hotlist.setRelated_to_type(entity);
        String path="hotlists/{hotlist}";
        Map<String,String> pathParameter=new HashMap<>();
        pathParameter.put("hotlist",String.valueOf(hotlistId));
        Response responseUpdate = RestClient.doPost1("JSON", baseURL, path, ThreadManager.getAccountApiKey(),null, pathParameter,false,hotlist );

        try {
            responseFromWebhook = new JsonPath(webhookHelper.getData(entitySlug));
        } catch (Exception e) {
            Assert.fail("Failed to fetch Webhook Data for "+entity+" entity "+"for event "+event);
        }
        //Verify Response
        Assert.assertEquals(responseFromWebhook.getString("id"), entitySlug);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "entities", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class, groups = "nightly-build")
    public void eventRecordAddedToHotlist(String entity){
        String entitySlug="";
        Response response=null;
        Response response1=null;
        Map<String, String> pathParamters = new HashMap<String, String>();
        String basePath="";
        String hotlistId="";
        String event="hotlist.record.added";
        function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);
        switch (entity){
            case "candidate": {
                entitySlug=function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                int id=response.jsonPath().get("id");
                hotlistId = String.valueOf(id);
                HotlistRelated hotlistRelated = new HotlistRelated();
                hotlistRelated.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/add-record";

                response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false,
                        hotlistRelated);
                break;
            }
            case "company": {
                entitySlug=function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                hotlistId = response.jsonPath().getString("id");
                HotlistRelated hotlistRelatedCompany = new HotlistRelated();
                hotlistRelatedCompany.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/add-record";

                response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false,
                        hotlistRelatedCompany);
                break;
            }
            case "contact": {
                String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
                entitySlug=function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                hotlistId = response.jsonPath().getString("id");
                HotlistRelated hotlistRelatedContact = new HotlistRelated();
                hotlistRelatedContact.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/add-record";
                response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false,
                        hotlistRelatedContact);
                break;
            }
            case "job": {
                String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
                String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
                entitySlug = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                hotlistId = response.jsonPath().getString("id");
                HotlistRelated hotlistRelatedContact = new HotlistRelated();
                hotlistRelatedContact.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/add-record";
                response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false,
                        hotlistRelatedContact);
                break;
            }
        }
        try {
            responseFromWebhook = new JsonPath(webhookHelper.getData(hotlistId));
        } catch (Exception e) {
            Assert.fail("Failed to fetch Webhook Data for "+entity+" entity "+"for event "+event);
        }
        //Verify Response
        Assert.assertEquals(responseFromWebhook.getString("id"), hotlistId);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "entities", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class, groups = "nightly-build")
    public void eventHotlistRecordRemoved(String entity){
        String entitySlug="";
        Response response=null;
        String basePath="";
        String hotlistId="";
        Map<String, String> pathParamters = new HashMap<String, String>();
        String event="hotlist.record.removed";
        function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);
        switch (entity){
            case "candidate": {
                entitySlug=function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                int id=response.jsonPath().get("id");
                hotlistId = String.valueOf(id);
                addRecordToHotlist(hotlistId, entitySlug);

                HotlistRelated hotlistRelated = new HotlistRelated();
                hotlistRelated.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/remove-record";

                Response response2 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
                        hotlistRelated);
                break;
            }
            case "company": {
                entitySlug=function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                hotlistId = response.jsonPath().getString("id");
                addRecordToHotlist(hotlistId,entitySlug);
                HotlistRelated hotlistRelated = new HotlistRelated();
                hotlistRelated.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/remove-record";

                Response response2 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
                        hotlistRelated);

                break;
            }
            case "contact": {
                String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
                entitySlug=function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                hotlistId = response.jsonPath().getString("id");
                addRecordToHotlist(hotlistId,entitySlug);
                HotlistRelated hotlistRelated = new HotlistRelated();
                hotlistRelated.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/remove-record";

                Response response2 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
                        hotlistRelated);


                break;
            }
            case "job": {
                String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
                String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
                entitySlug = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath().getString("slug");
                response = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), entity);
                hotlistId = response.jsonPath().getString("id");
                addRecordToHotlist(hotlistId,entitySlug);
                HotlistRelated hotlistRelated = new HotlistRelated();
                hotlistRelated.setRelated(entitySlug);

                pathParamters.put("hotlist", hotlistId);
                basePath = "hotlists/{hotlist}/remove-record";

                Response response2 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
                        hotlistRelated);

                break;
            }
        }
        try {
            responseFromWebhook = new JsonPath(webhookHelper.getData(hotlistId));
        } catch (Exception e) {
            Assert.fail("Failed to fetch Webhook Data for "+entity+" entity "+"for event "+event);
        }
        //Verify Response
        Assert.assertEquals(responseFromWebhook.getString("id"), hotlistId);
    }

    public void addRecordToHotlist(String hotlistId, String entitySlug){
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(entitySlug);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/add-record";

        Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false,
                hotlistRelated);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        webhookHelper.clear();
    }

    @DataProvider(name = "entities")
    public Object[][] dpMethod() {
        return new Object[][] { { "candidate" },{ "company" },{"contact"},{"job"} };
    }
}
