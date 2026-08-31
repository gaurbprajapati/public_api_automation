package io.recruitcrm.CompanyService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TextFieldCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions function = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }
    
    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "textFieldFilterSearchTestData",description = "Filter Search Test for Text Fields")
    public void textFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200," Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"),"Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        validateTextFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, "Company", "aboutcompany", "about_company");
    }


    @DataProvider(name = "textFieldFilterSearchTestData",parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyTextTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, String> companySlugMap = new HashMap<>();
        
        try {
            //Creating all companies according to payload and storing their slugs in a map
            List<CompletableFuture<Map.Entry<String, String>>> createFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject payload = companyEntry.getJSONObject("payload");
                    Response response = function.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                    response.then().statusCode(200);
                    JsonPath jsonPath = response.jsonPath();
                    String slug = jsonPath.getString("data.company.slug");
                    return Map.entry(companyKey, slug);
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map.Entry<String, String>> future : createFutures) {
                Map.Entry<String, String> entry = future.join();
                companySlugMap.put(entry.getKey(), entry.getValue());
            }
            
            //Linking child companies to parent companies according to payload
            List<CompletableFuture<Void>> linkFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.runAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    String parentCompanyKey = companyEntry.optString("parentCompany", null);
                    
                    if (parentCompanyKey != null && !parentCompanyKey.isEmpty() && !parentCompanyKey.equals("null")) {
                        String parentCompanySlug;
                        if (parentCompanyKey.startsWith("company") && companySlugMap.containsKey(parentCompanyKey)) {
                            parentCompanySlug = companySlugMap.get(parentCompanyKey);
                        } else {
                            return;
                        }
                        if (parentCompanySlug != null) {
                            String childCompanySlug = companySlugMap.get(companyKey);
                            List<String> childCompanySlugs = new ArrayList<>();
                            childCompanySlugs.add(childCompanySlug);
                            
                            Response linkResponse = function.linkCompanyToParentCompany(albatrossURL, albatrossAuthToken, parentCompanySlug, childCompanySlugs);
                            Assert.assertEquals(linkResponse.getStatusCode(), 200, "Failed to link company " + companyKey + " to parent " + parentCompanyKey);
                        }
                    }
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(linkFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Create filterValue object with type and value
        JSONObject filterValueObj;
        if (filterValue_TYPE.equals("STRING_LIST")) {
            filterValueObj = stringListFilterValue(filterValue);
        } else {
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            filterValueObj.put("value", filterValue);
        }
        
        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        
        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);
        
        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);
        
        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");
        
        payload.put("filterSearchList", filterSearchList);

        return payload;
    }



    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "textFieldFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Text Fields")
    public void textFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        textFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "textFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] textFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
