package io.recruitcrm.CompanyService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NumberFieldCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
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
    @Test(groups = {"aries_service"}, dataProvider = "numberFieldFilterCompanySearchTestData", description = "Filter Search Test for Number Fields")
    public void numberFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        // Log payload for the listener to capture
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        Assert.assertEquals(response.getStatusCode(), 200," Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"),"Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        
        validateNumberFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, "Company");
    }
    

    @DataProvider(name = "numberFieldFilterCompanySearchTestData",parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyNumberTypeFilterDataProvider.json");
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

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        JSONObject filterValueObj = new JSONObject();
        
        if (filterType.equals("is_between")) {
            filterValueObj = integerStartEndFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("NUMERIC_STRING")) {
            filterValueObj = numericStringFilterValue(filterValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if (filterType.equals("has_any_value") || filterType.equals("is_empty")) {
                filterValueObj.put("value", JSONObject.NULL);
            } else {
                if (filterValue != null && !filterValue.trim().isEmpty()) {
                    try {
                        if (filterValue.contains(".")) {
                            filterValueObj.put("value", Double.parseDouble(filterValue));
                        } else {
                            filterValueObj.put("value", Integer.parseInt(filterValue));
                        }
                    } catch (NumberFormatException e) {
                        filterValueObj.put("value", filterValue);
                    }
                } else {
                    filterValueObj.put("value", JSONObject.NULL);
                }
            }
        }
        
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
                    Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
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
            
            
            //Updating job count fields for each company using reaper endpoint
            List<CompletableFuture<Void>> updateJobCountFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.runAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    String companySlug = companySlugMap.get(companyKey);
                    
                    if (companySlug != null) {
                        // Get company ID from slug
                        int companyId = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companySlug);
                        
                        // Prepare fieldsAndValues JSON object with job count fields
                        JSONObject fieldsAndValues = new JSONObject();
                        if (companyEntry.has("totalonholdjob")) {
                            fieldsAndValues.put("totalonholdjob", companyEntry.getInt("totalonholdjob"));
                        }
                        if (companyEntry.has("totalcanceledjob")) {
                            fieldsAndValues.put("totalcanceledjob", companyEntry.getInt("totalcanceledjob"));
                        }
                        if (companyEntry.has("totalclosedjob")) {
                            fieldsAndValues.put("totalclosedjob", companyEntry.getInt("totalclosedjob"));
                        }
                        if (companyEntry.has("totalopenjob")) {
                            fieldsAndValues.put("totalopenjob", companyEntry.getInt("totalopenjob"));
                        }
                        
                        // Update company fields using reaper endpoint
                        if (fieldsAndValues.length() > 0) {
                            com.qa.api.util.reaper.ReaperIntegration.updateCompanyFields(companyId, fieldsAndValues);
                        }
                    }
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(updateJobCountFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "numberFieldFilterCompanySearchSmokeTestData", description = "[Smoke] Filter Search Test for Number Fields")
    public void numberFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        numberFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "numberFieldFilterCompanySearchSmokeTestData", parallel = true)
    public Object[][] numberFieldFilterCompanySearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
