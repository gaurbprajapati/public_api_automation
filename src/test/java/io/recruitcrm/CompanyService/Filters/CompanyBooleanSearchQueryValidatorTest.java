package io.recruitcrm.CompanyService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CompanyBooleanSearchQueryValidatorTest extends FilterSearchBaseTest {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String albatrossAuthToken;
    String email;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "companyBooleanSearchQueryValidatorTestData")
    public void companyBooleanSearchQueryValidatorTest(String filterValue, String expectedResult) {
        String basePath = ADVANCED_SEARCH_BOOLEAN_VALIDATION_PATH;
        JSONObject payload = new JSONObject();
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logInfo("Filter Value", filterValue);
        FilterSearchReporter.logInfo("Expected Result", expectedResult);
        payload.put("keyword", filterValue);
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, albatrossAuthToken, null, true, payload);
        FilterSearchReporter.logPayload(payload);
        JsonPath validatorResponse = response.jsonPath();
        if (expectedResult.equals("true")) {
            Assert.assertEquals(response.getStatusCode(), 200);
            Assert.assertEquals(validatorResponse.get("meta.message"), "Boolean search query is valid");
            Assert.assertTrue(validatorResponse.get("data.valid"));
        } else {
            Assert.assertEquals(response.getStatusCode(), 400);
            Assert.assertEquals(validatorResponse.get("meta.message"), "Boolean search query validation failed");
            Assert.assertFalse(validatorResponse.get("data.valid"));
        }

    }

    @DataProvider(name = "companyBooleanSearchQueryValidatorTestData")
    public Object[][] companyBooleanSearchQueryValidatorTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyBooleanSearchQueryValidatorDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        JSONArray queries = filterData.getJSONArray("Queries");
        for (int i = 0; i < queries.length(); i++) {
            JSONObject test = queries.getJSONObject(i);
            testData.add(new Object[]{test.getString("query"), test.getString("expectedResult")});
        }
        return testData.toArray(new Object[0][0]);
    }
}
