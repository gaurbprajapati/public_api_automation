package io.recruitcrm.albatross.global;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerUser;
import io.rcrm.api.pojo.albatross.globalsearch.GlobalSearchDeal;
import io.rcrm.api.pojo.albatross.jobs.SearchEntity;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GlobalSearchTest extends TestBase {

    commanFunction function = new commanFunction();
    JavaFakerUser javaFakerUser = new JavaFakerUser();
    Object accountAPIKey;
    Object albatrossAuthToken;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        accountAPIKey = ThreadManager.getAccountApiKey();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "globalSearchDealTestData", groups = "nightly-build")
    public void globalSearchDeal(boolean validQuery, String query, JsonPath jsonDeal) {
        GlobalSearchDeal globalSearchDeal = new GlobalSearchDeal();
        globalSearchDeal.setSearch(query);
        String basePath = "global/deal-global-search";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, globalSearchDeal);
        
        Assert.assertEquals(response.getStatusCode(), 200, "Response status code is not 200");
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("message"), "Deals retrieved successfully.", "Deal message is not matching");
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Message type is not success");
        
        if (validQuery) {
            Assert.assertEquals(jsonPath.getString("data[0].title"), jsonDeal.getString("name"), "Deal name is not matching");
            Assert.assertEquals(jsonPath.getString("data[0].srno"), jsonDeal.getString("id"), "Deal Serial Number is not matching");
            Assert.assertEquals(Double.parseDouble(jsonPath.getString("data[0].dealvalue")), Double.parseDouble(jsonDeal.getString("deal_value")), "Deal Value is not matching");
        } else {
            Assert.assertEquals(jsonPath.getList("data").size(), 0, "Data should be empty for invalid query");
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//global//globalSearchDeal.json"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "searchCompanyData", groups = "nightly-build")
    public void globalSearchCompany(Response companyResponse) {
        JsonPath companyJson = companyResponse.jsonPath();
        String companyName = companyJson.getString("company_name");
        String companySlug = companyJson.getString("slug");
        SearchEntity searchCompany = new SearchEntity(companyName, false, true, false, false, false, false);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", albatrossAuthToken, null, true, searchCompany);
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200, "Response status code is not 200");
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Message type is not success");
        Assert.assertEquals(jsonPath.getString("data[0].title"), companyName, "Company name is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].slug"), companySlug, "Company slug is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].entitytype"), "3", "Entity type is not matching");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "searchContactData", groups = "nightly-build")
    public void globalSearchContact(Response contactResponse) {
        JsonPath contactJson = contactResponse.jsonPath();
        String contactFirstName = contactJson.getString("first_name");
        String contactLastName = contactJson.getString("last_name");
        String contactSlug = contactJson.getString("slug");
        String contactEmail = contactJson.getString("email");
        String fullName = contactFirstName + " " + contactLastName;
        SearchEntity searchContact = new SearchEntity(fullName, false, false, false, true, false, false);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", albatrossAuthToken, null, true, searchContact);
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200, "Response status code is not 200");
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Message type is not success");
        Assert.assertEquals(jsonPath.getString("data[0].title"), fullName, "Contact name is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].slug"), contactSlug, "Contact slug is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].entitytype"), "2", "Entity type is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].email"), contactEmail, "Contact email is not matching");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "searchCandidateData", groups = "nightly-build")
    public void globalSearchCandidate(Response candidateResponse) {
        JsonPath candidateJson = candidateResponse.jsonPath();
        String candidateFirstName = candidateJson.getString("first_name");
        String candidateLastName = candidateJson.getString("last_name");
        String candidateSlug = candidateJson.getString("slug");
        String candidateEmail = candidateJson.getString("email");
        String fullName = candidateFirstName + " " + candidateLastName;
        SearchEntity searchCandidate = new SearchEntity(fullName, true, false, false, false, false, false);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", albatrossAuthToken, null, true, searchCandidate);
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200, "Response status code is not 200");
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Message type is not success");
        Assert.assertEquals(jsonPath.getString("data[0].title"), fullName, "Candidate name is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].slug"), candidateSlug, "Candidate slug is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].entitytype"), "5", "Entity type is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].email"), candidateEmail, "Candidate email is not matching");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "searchJobData", groups = "nightly-build")
    public void globalSearchJob(Response jobResponse) {
        JsonPath jobJson = jobResponse.jsonPath();
        String jobName = jobJson.getString("name");
        String jobSlug = jobJson.getString("slug");
        SearchEntity searchJob = new SearchEntity(jobName, false, false, true, false, false, false);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", albatrossAuthToken, null, true, searchJob);
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200, "Response status code is not 200");
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Message type is not success");
        Assert.assertEquals(jsonPath.getString("data[0].title"), jobName, "Job name is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].slug"), jobSlug, "Job slug is not matching");
        Assert.assertEquals(jsonPath.getString("data[0].entitytype"), "4", "Entity type is not matching");
    }

    @DataProvider
    public Object[][] searchCompanyData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey);
        return new Object[][] { { companyResponse } };
    }

    @DataProvider
    public Object[][] searchContactData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey);
        String companySlug = companyResponse.jsonPath().getString("slug");
        Response contactResponse = function.createNewContact_POST(baseURL, accountAPIKey, companySlug);
        return new Object[][] { { contactResponse } };
    }

    @DataProvider
    public Object[][] searchCandidateData() {
        Response candidateResponse = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey);
        return new Object[][] { { candidateResponse } };
    }

    @DataProvider
    public Object[][] searchJobData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey);
        String companySlug = companyResponse.jsonPath().getString("slug");
        Response contactResponse = function.createNewContact_POST(baseURL, accountAPIKey, companySlug);
        String contactSlug = contactResponse.jsonPath().getString("slug");
        Response jobResponse = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug);
        return new Object[][] { { jobResponse } };
    }

    @DataProvider
    public Object[][] globalSearchDealTestData() {
        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
        JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, accountAPIKey, jsonCompany.getString("slug"), "", "").jsonPath();
        return new Object[][]{
                {true, jsonDeal.getString("name"), jsonDeal},
                {true, "ID - " + jsonDeal.getInt("id"), jsonDeal},
                {true, jsonCompany.getString("company_name"), jsonDeal},
                {false, javaFakerUser.getUserAccountName(), jsonDeal}
        };
    }
}