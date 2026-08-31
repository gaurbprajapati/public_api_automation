package io.recruitcrm.ContactService.Filters;

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
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NumberFieldContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String email;
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        albatrossAuthToken = ownerAlbatrossAuthToken;
        email = ThreadManager.getAccount().getOwner().getEmail();
        createCompanies();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberFieldFilterContactSearchTestData", description = "Filter Search Test for Number Fields")
    public void numberFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        // Log payload for the listener to capture
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        JSONArray data = getFilteredData(response);
        Assert.assertEquals(response.getStatusCode(), 200," Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"),"Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        
        validateNumberFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, "Contact");
    }
    

    @DataProvider(name = "numberFieldFilterContactSearchTestData",parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactNumberTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        JSONObject filterValueObj = new JSONObject();
        
        if (filterType.equals("is_between")) {
            // For is_between, use INTEGER_START_END type
            filterValueObj.put("type", "INTEGER_START_END");
            JSONObject rangeValue = new JSONObject();
            String startValue = filterValue.split(",")[0].trim();
            String endValue = filterValue.split(",")[1].trim();
            // Parse as double first to handle decimal strings like "15.0", then convert to int
            rangeValue.put("start", Double.parseDouble(startValue));
            rangeValue.put("end",  Double.parseDouble(endValue));
            filterValueObj.put("value", rangeValue);
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
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "contact");
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

    public void createCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            // Create only companies 1-5
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String companyKey = "company" + i;
                if (companyJson.has(companyKey)) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, authToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        
                        if (companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - company ID is null");
                            return;
                        }
                        
                        Integer companyId = Integer.parseInt(companyIdStr);
                        
                        synchronized (companyKeyToSlugMap) {
                            companyKeyToSlugMap.put(companyKey, slug);
                        }
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                        }
                    }, executor));
                }
            }

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return ownerAlbatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return ownerAlbatrossAuthToken;
        }
    }

    public void createTestData() {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/contact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        try {
            //Creating all contacts according to payload
            List<CompletableFuture<Void>> createFutures = contactJson.keySet().stream()
                .filter(key -> key.startsWith("contact"))
                .map(contactKey -> CompletableFuture.runAsync(() -> {
                    JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                    JSONObject payload = contactEntry.getJSONObject("payload");
                    String createdBy = contactEntry.has("createdBy") ? contactEntry.getString("createdBy") : "admin";
                    String authToken = getAlbatrossAuthToken(createdBy);
                    
                    // Add delay to avoid contacts getting created with same ID
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Replace company placeholders in selectedcompanies
                    if (payload.has("selectedcompanies")) {
                        JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                        for (int i = 0; i < selectedCompanies.length(); i++) {
                            JSONObject companyInfo = selectedCompanies.getJSONObject(i);
                            String slugPlaceholder = companyInfo.optString("slug", "");
                            String idPlaceholder = companyInfo.optString("id", "");
                            
                            // Extract company key from placeholder like {company1_slug} or {company1_id}
                            String companyKey = null;
                            if (slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                                companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6); // Remove { and _slug}
                            } else if (idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                                companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4); // Remove { and _id}
                            }
                            
                            if (companyKey != null) {
                                String actualSlug = companyKeyToSlugMap.get(companyKey);
                                String actualId = companyKeyToIdMap.get(companyKey);
                                
                                if (actualSlug != null && actualId != null) {
                                    companyInfo.put("slug", actualSlug);
                                    companyInfo.put("id", actualId);
                                }
                            }
                        }
                    }
                    
                    Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", authToken, null, true, payload);
                    response.then().statusCode(200);
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "numberFieldFilterContactSearchSmokeTestData", description = "[Smoke] Filter Search Test for Number Fields")
    public void numberFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        numberFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE, testCaseId);
    }

    @DataProvider(name = "numberFieldFilterContactSearchSmokeTestData", parallel = true)
    public Object[][] numberFieldFilterContactSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
