package io.rcrm.api.subscriptions.receiveWebhook;

import com.qa.api.util.WebhookHelper;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.WebhookRetryAnalyzer;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.OptOutEmail;
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetEntityOptedOutTest extends TestBase {
    commanFunction function = new commanFunction();
    WebhookHelper webhookHelper;
    JsonPath responseFromWebhook;
    String entitySlug;
    String basePath = "email/opt-out/status";
    OptOutEmail optOutEmail;

    @BeforeClass
    public void setUp() {
        function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        webhookHelper = new WebhookHelper();
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "optOutEvents", retryAnalyzer = WebhookRetryAnalyzer.class)
    public void getEntityOptedOut(String event) {
        function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        // Create new subscription
        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

        // Trigger subscription and extract data
        Response response = null;
        switch (event) {
            case "candidate.optedout":
                String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
                entitySlug = candidateSlug;

                optOutEmail = new OptOutEmail("candidate", candidateSlug, "1");
                response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, optOutEmail);
                break;

            case "contact.optedout":
                String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
                String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
                entitySlug = contactSlug;

                optOutEmail = new OptOutEmail("contact", contactSlug, "1");
                response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, optOutEmail);
                break;

            default:
                break;
        }

        // Add a small delay to allow webhook processing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Fetch data from webhook site
        try {
            responseFromWebhook = new JsonPath(webhookHelper.getData(entitySlug));
        } catch (Exception e) {
            Assert.fail("Failed to fetch Webhook data for Event "+event+", "+e.getMessage());
        }

        //Verify Response
        Assert.assertEquals(responseFromWebhook.get("slug"), entitySlug);
        Assert.assertEquals(responseFromWebhook.get("is_email_opted_out"), "true");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        webhookHelper.clear();
    }

    @DataProvider(name = "optOutEvents")
    public Object[][] dpMethod() {
        return new Object[][]{{"candidate.optedout"}, {"contact.optedout"}};
    }
}
