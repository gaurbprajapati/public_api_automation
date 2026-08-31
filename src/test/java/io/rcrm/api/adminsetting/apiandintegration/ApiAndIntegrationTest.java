package io.rcrm.api.adminsetting.apiandintegration;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.reaper.Account;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("NA")
public class ApiAndIntegrationTest extends TestBase {

    public ApiAndIntegrationTest() {
        super();
    }

    JavaFakerCompany faker = new JavaFakerCompany();
    String companyName = faker.getCompanyName();
    String companyWebsite = faker.getUrl();
    String contactNumber = faker.getContactNumber();

    final String INSUFFICIENT_ACCESS_MGS = "INSUFFICIENT_ACCESS - Please Upgrade Your Plan To “Business” or “Enterprise” to " +
            "gain access to the Open API";

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void verifyPublicApiRestrictionOnFreeAndProAccounts() {
        //adding company using public api to get the unauthorized access message
        Company company = new Company(companyName, companyWebsite, contactNumber, "");

        //verify for free plan
        Account freeAccount = getAccounts("Free","", 1)[0];
        ThreadManager.setAccount(freeAccount);
        Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);
        Assert.assertEquals(response.getStatusCode(), 401);

        String responseBody = response.getBody().asString();
        response.then().body("message", Matchers.is(INSUFFICIENT_ACCESS_MGS));

        //verify for pro plan
        Account proAccount = getAccounts("Pro","", 1)[0];
        ThreadManager.setAccount(proAccount);
        Response response2 = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);
        Assert.assertEquals(response2.getStatusCode(), 401);

        String responseBody2 = response2.getBody().asString();
        response2.then().body("message", Matchers.is(INSUFFICIENT_ACCESS_MGS));
    }
}
