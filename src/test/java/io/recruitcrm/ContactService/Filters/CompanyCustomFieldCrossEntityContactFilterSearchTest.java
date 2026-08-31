package io.recruitcrm.ContactService.Filters;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.DateUtil;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CompanyCustomFieldCrossEntityContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String accountOwnerAPIKey;
    String email;
    Map<String, Integer> customFieldIds = new HashMap<>();
    Map<String, String> entityCFValueMap = new HashMap<>();
    Map<String, List<JsonPath>> companyDataMap = new HashMap<>();
    Map<String, String> contactSlugToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, String> contactIdToKeyMap = new HashMap<>();
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companySlugToKeyMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> companyIdToKeyMap = new HashMap<>();
    Map<String, Map<String, String>> timestampScenarios;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        customFieldIds = createCustomFields();
        createEntityCFValueMap();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "entityCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Entity Custom Fields (Cross Entity)")
    public void entityCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 12, "Contact");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "textCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Text Custom Fields (Cross Entity)")
    public void textCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createTextFieldFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateTextFieldCrossEntityFilteredData(companyDataByContact, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Number Custom Fields (Cross Entity)")
    public void numberCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createNumberFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateNumberFieldCrossEntityFilteredData(companyDataByContact, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Date Custom Fields (Cross Entity)")
    public void dateCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateDateFieldCrossEntityFilteredData(companyDataByContact, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Dropdown and Multiselect Custom Fields (Cross Entity)")
    public void dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createDropdownAndMultiselectFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 12, "Contact");
    }

    @DataProvider(name = "entityCustomFieldCrossEntityFilterSearchTestData")
    public Object[][] entityCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        List<String> entityTypes = Arrays.asList("candidate", "company", "company", "job", "deal", "user", "team");
        
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                String filterType = test.getString("filterType");
                String filterValue = test.getString("filterValue");
                String dbField = test.getString("dbField");
                String filterValue_TYPE = test.optString("filterValue_TYPE", "STRING_LIST");
                
                if (entityTypes.contains(fieldType)) {
                    testData.add(new Object[]{
                            key, filterType, filterValue, dbField, test.getString("expectedResult"), fieldType, filterValue_TYPE
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "dateCustomFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] dateCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("date") || tests.getJSONObject(i).getString("fieldType").equals("datetime")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "numberCustomFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] numberCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("number")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "textCustomFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] textCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("text") || tests.getJSONObject(i).getString("fieldType").equals("phonenumber") || tests.getJSONObject(i).getString("fieldType").equals("longtext")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTestData")
    public Object[][] dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("multiselect") || tests.getJSONObject(i).getString("fieldType").equals("dropdown")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void validateTextFieldCrossEntityFilteredData(JSONObject companyDataByContact, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(companyDataByContact.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && companyDataByContact.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String contactSlug : companyDataByContact.keySet()) {
            JSONArray contactAssociatedCompanies = companyDataByContact.getJSONArray(contactSlug);
            boolean atLeastOneMatch = false;
            if (filterType.equals("is_not") || filterType.equals("does_not_contain") || filterType.equals("is_empty")) {
                if (contactAssociatedCompanies.length() == 0) {
                    atLeastOneMatch = true;
                    continue;
                }
            }
            for (int i = 0; i < contactAssociatedCompanies.length(); i++) {
                JSONObject company = contactAssociatedCompanies.getJSONObject(i);
                boolean matches = validateTextFieldFilteredDataBoolean(company, filterType, filterValue, fieldName, dbField, expectedResult, "Company", "");
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the filter for contact still coming in the response: " + contactSlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    public void validateNumberFieldCrossEntityFilteredData(JSONObject companyDataByContact, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(companyDataByContact.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && companyDataByContact.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String contactSlug : companyDataByContact.keySet()) {
            JSONArray contactAssociatedCompanies = companyDataByContact.getJSONArray(contactSlug);
            boolean atLeastOneMatch = false;
            for (int i = 0; i < contactAssociatedCompanies.length(); i++) {
                JSONObject company = contactAssociatedCompanies.getJSONObject(i);
                String companyNumber = String.valueOf(company.opt(dbField)).trim();
                if (companyNumber.equals("null") || companyNumber.isEmpty()) {
                    if (filterType.equals("is_empty") || filterType.equals("is_not") || filterType.equals("does_not_contain")) {
                        atLeastOneMatch = true;
                        break;
                    }
                    continue;
                }
                
                if (companyNumber.endsWith(".0")) {
                    companyNumber = companyNumber.substring(0, companyNumber.length() - 2);
                }
                
                boolean matches = validateNumberFieldFilteredDataBoolean(companyNumber, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the filter for contact still coming in the response: " + contactSlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateNumberFieldFilteredDataBoolean(String companyNumber, String filterType, String filterValue, String fieldName) {
        try {
            if (filterType.equals("begins_with")) {
                return companyNumber.startsWith(filterValue);
            }
            if (filterType.equals("ends_with")) {
                return companyNumber.endsWith(filterValue);
            }
            if (filterType.equals("has_any_value")) {
                return !companyNumber.isEmpty() && !companyNumber.equals("null");
            }
            if (filterType.equals("is_empty")) {
                return companyNumber.isEmpty() || companyNumber.equals("null");
            }

            double companyNumberValue = Double.parseDouble(companyNumber);
            
            // Handle is_between separately since filterValue contains comma
            if (filterType.equals("is_between")) {
                String[] rangeParts = filterValue.split(",");
                double startValue = Double.parseDouble(rangeParts[0].trim());
                double endValue = Double.parseDouble(rangeParts[1].trim());
                return companyNumberValue >= startValue && companyNumberValue <= endValue;
            }

            // For other filter types, parse filterValue as double
            double filterValueDouble = Double.parseDouble(filterValue.trim());
            
            switch (filterType) {
                case "is":
                    return companyNumberValue == filterValueDouble;
                case "is_not":
                    return companyNumberValue != filterValueDouble;
                case "is_mt":
                    return companyNumberValue > filterValueDouble;
                case "is_lt":
                    return companyNumberValue < filterValueDouble;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void validateDateFieldCrossEntityFilteredData(JSONObject companyDataByContact, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(companyDataByContact.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && companyDataByContact.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String contactSlug : companyDataByContact.keySet()) {
            JSONArray contactAssociatedCompanies = companyDataByContact.getJSONArray(contactSlug);
            boolean atLeastOneMatch = false;
            if (filterType.equals("is_not") || filterType.equals("does_not_contain") || filterType.equals("is_empty")) {
                if (contactAssociatedCompanies.length() == 0) {
                    atLeastOneMatch = true;
                }
            }
            for (int i = 0; i < contactAssociatedCompanies.length(); i++) {
                JSONObject company = contactAssociatedCompanies.getJSONObject(i);
                String companyDateStr = company.optString(dbField, "").trim();
                boolean matches = validateDateFieldFilteredDataBoolean(companyDateStr, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the filter for contact still coming in the response: " + contactSlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateDateFieldFilteredDataBoolean(String companyDate, String filterType, String filterValue, String fieldName) {
        if (companyDate.isEmpty() || companyDate.equals("null")) {
            return filterType.equals("is_empty");
        }

        try {
            LocalDate companyParsedDate = parseDate(companyDate);
            
            switch (filterType) {
                case "is":
                case "is_equal_to":
                    if (isRelativeDatePeriod(filterValue)) {
                        return isDateInPeriod(companyParsedDate, filterValue);
                    } else {
                        LocalDate filterDate = parseDate(filterValue);
                        return companyParsedDate.equals(filterDate);
                    }
                case "is_not":
                    LocalDate filterDateNot = parseDate(filterValue);
                    return !companyParsedDate.equals(filterDateNot);
                case "is_before":
                    LocalDate filterDateBefore = parseDate(filterValue);
                    return companyParsedDate.isBefore(filterDateBefore);
                case "is_after":
                    LocalDate filterDateAfter = parseDate(filterValue);
                    return companyParsedDate.isAfter(filterDateAfter) || companyParsedDate.isEqual(filterDateAfter);
                case "is_between":
                    String[] dates = filterValue.split(",");
                    if (dates.length != 2) {
                        return false;
                    }
                    LocalDate startDate = parseDate(dates[0].trim());
                    LocalDate endDate = parseDate(dates[1].trim());
                    return (companyParsedDate.isEqual(startDate) || companyParsedDate.isAfter(startDate)) &&
                           (companyParsedDate.isEqual(endDate) || companyParsedDate.isBefore(endDate));
                case "is_mt":
                    int days = Integer.parseInt(filterValue);
                    LocalDate cutoffDate = LocalDate.now().minusDays(days);
                    return companyParsedDate.isBefore(cutoffDate) || companyParsedDate.isEqual(cutoffDate);
                case "is_lt":
                    int daysLt = Integer.parseInt(filterValue);
                    LocalDate cutoffDateLt = LocalDate.now().minusDays(daysLt);
                    return companyParsedDate.isAfter(cutoffDateLt) || companyParsedDate.isEqual(cutoffDateLt);
                case "has_any_value":
                    return !companyDate.isEmpty() && !companyDate.equals("null");
                case "is_empty":
                    return companyDate.isEmpty() || companyDate.equals("null");
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    public JSONObject getCompaniesForContacts(JSONArray contactData) {
        JSONObject companyResult = new JSONObject();
        for (int i = 0; i < contactData.length(); i++) {
            JSONArray contactAssociatedCompanies = new JSONArray();
            JSONObject contact = contactData.getJSONObject(i);
            String contactSlug = contact.getString("slug");
            List<JsonPath> companyList = companyDataMap.get(contactSlug);
            if (companyList != null) {
                for (JsonPath companyJsonPath : companyList) {
                    Map<String, Object> companyMap = companyJsonPath.get("data.company");
                    JSONObject company = new JSONObject(companyMap);
                    contactAssociatedCompanies.put(company);
                }
            }
            companyResult.put(contactSlug, contactAssociatedCompanies);
        }
        return companyResult;
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/contactCompanyCF_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.runAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject payload = companyEntry.getJSONObject("payload");
                    JSONObject processedPayload = processPayloadPlaceholders(payload);
                    Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, processedPayload);
                    int companyId = response.jsonPath().getInt("data.company.id");
                    String companySlug = response.jsonPath().getString("data.company.slug");
                    
                    companyIdToKeyMap.put(String.valueOf(companyId), companyKey);
                    companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                    companyKeyToSlugMap.put(companyKey, companySlug);
                    companySlugToKeyMap.put(companySlug, companyKey);
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            addContactsToCompanies(companyJson);
            updateCompanyCustomDateFieldsTimestamps();
            refreshCompanyDataMap();
        } finally {
            executor.shutdown();
        }
    }

    private void updateCompanyCustomDateFieldsTimestamps() {
        timestampScenarios = createTimestampScenarios();
        String dateCFFieldName = "dateCF";
        
        if (!customFieldIds.containsKey(dateCFFieldName)) {
            return;
        }
        
        String dateCFDbField = "custcolumn" + customFieldIds.get(dateCFFieldName);
        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(timestampScenarios.entrySet());

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            int companyIndex = 0;
            
            for (Map.Entry<String, Map<String, String>> scenario : scenarios) {
                if (companyIndex >= companyKeyToIdMap.size()) {
                    break;
                }
                
                final int idx = companyIndex;
                final Map<String, String> timestamps = scenario.getValue();
                
                futures.add(CompletableFuture.runAsync(() -> {
                    String companyKey = "company" + (idx + 1);
                    String companyIdStr = companyKeyToIdMap.get(companyKey);
                    
                    if (companyIdStr == null) {
                        return;
                    }
                    
                    int companyId = Integer.parseInt(companyIdStr);
                    String dateValue = timestamps.get(dateCFFieldName);
                    
                    if (dateValue != null) {
                        JSONObject fieldsAndTimestamps = new JSONObject();
                        fieldsAndTimestamps.put(dateCFDbField, dateValue);
                        
                        Response updateResponse = ReaperIntegration.updateCompanyFields(companyId, fieldsAndTimestamps);
                        if (updateResponse.getStatusCode() != 200) {
                            Assert.fail("Failed to update custom date field for company: " + companyId);
                        }
                    }
                }, executor));
                
                companyIndex++;
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void addContactsToCompanies(JSONObject companyJson) {
        Map<String, List<String>> contactSlugToCompanySlugsMap = new HashMap<>();
        
        for (int i = 1; i <= 10; i++) {
            Response response = allCrudFunctions.createContact(albatrossURL, albatrossAuthToken);
            String contactSlug = response.jsonPath().getString("data.contact.slug");
            Integer contactId = response.jsonPath().getInt("data.contact.id");
            String contactKey = "contact" + i;
            contactKeyToSlugMap.put(contactKey, contactSlug);
            contactIdToKeyMap.put(String.valueOf(contactId), contactKey);
            contactKeyToIdMap.put(contactKey, String.valueOf(contactId));
            contactSlugToKeyMap.put(contactSlug, contactKey);
            contactSlugToCompanySlugsMap.put(contactSlug, new ArrayList<>());
        }

        for (String companyKey : companyKeyToIdMap.keySet()) {
            if (!companyJson.has(companyKey)) {
                continue;
            }
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String contact = companyEntry.optString("contact", "").trim();
            if (contact.isEmpty()) {
                continue;
            }
            String[] contactKeys = contact.split(",");
            String contactSlugs = Arrays.stream(contactKeys)
                    .map(String::trim)
                    .map(contactKeyToSlugMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            if (contactSlugs.isEmpty()) {
                continue;
            }

            Integer companyId = Integer.parseInt(companyKeyToIdMap.get(companyKey));
            String companySlug = companyKeyToSlugMap.get(companyKey);
            
            UpdateFields updateFields = new UpdateFields();
            updateFields.setKey("contactid");
            updateFields.setValue(contactSlugs);
            updateFields.setTableFlag("company");
            updateFields.setId(Collections.singletonList(companyId));
            updateFields.setAddInValues(true);
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
            linkResponse.then().statusCode(200);
            
            for (String contactKey : contactKeys) {
                String contactSlug = contactKeyToSlugMap.get(contactKey.trim());
                if (contactSlug != null && companySlug != null) {
                    contactSlugToCompanySlugsMap.get(contactSlug).add(companySlug);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : contactSlugToCompanySlugsMap.entrySet()) {
            String contactSlug = entry.getKey();
            List<String> companySlugs = entry.getValue();
            if (!companySlugs.isEmpty()) {
                List<JsonPath> companyList = new ArrayList<>();
                for (String companySlug : companySlugs) {
                    Response companyResponse = getCompany(companySlug);
                    JsonPath companyJsonPath = companyResponse.jsonPath();
                    companyList.add(companyJsonPath);
                }
                synchronized (companyDataMap) {
                    companyDataMap.put(contactSlug, companyList);
                }
            }
        }
    }

    public Response getCompany(String companySlug) {
        String basePath = "/companies/{companySlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companySlug", companySlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    private void refreshCompanyDataMap() {
        synchronized (companyDataMap) {
            for (Map.Entry<String, List<JsonPath>> entry : companyDataMap.entrySet()) {
                String contactSlug = entry.getKey();
                List<JsonPath> existingCompanies = entry.getValue();
                if (existingCompanies == null || existingCompanies.isEmpty()) {
                    continue;
                }

                List<JsonPath> refreshed = new ArrayList<>();
                for (JsonPath companyJsonPath : existingCompanies) {
                    try {
                        String companySlug = companyJsonPath.getString("data.company.slug");
                        if (companySlug == null || companySlug.isEmpty()) {
                            refreshed.add(companyJsonPath);
                            continue;
                        }
                        Response updatedCompanyResponse = getCompany(companySlug);
                        refreshed.add(updatedCompanyResponse.jsonPath());
                    } catch (Exception e) {
                        refreshed.add(companyJsonPath);
                    }
                }
                companyDataMap.put(contactSlug, refreshed);
            }
        }
    }

    private void logContactNameAndCompany(Response response, JSONArray contactData, JSONObject companyDataByContact, String fieldName, String dbField) {
        FilterSearchReporter.logInfo("<b>📋 Contact - Company Custom Field Information:</b>");
        
        if (contactData != null && companyDataByContact != null) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
            logMessage.append("<code>");
            
            for (int i = 0; i < contactData.length(); i++) {
                JSONObject contact = contactData.getJSONObject(i);
                String contactSlug = contact.getString("slug");
                JSONArray associatedCompanies = companyDataByContact.optJSONArray(contactSlug);
                
                String firstName = contact.optString("firstname", "").trim();
                String lastName = contact.optString("lastname", "").trim();
                String contactName = (firstName + " " + lastName).trim();
                if (contactName.isEmpty()) {
                    contactName = "Unknown";
                }
                logMessage.append("Contact: ").append(contactName).append("\n");
                
                if (associatedCompanies != null && associatedCompanies.length() > 0) {
                    for (int j = 0; j < associatedCompanies.length(); j++) {
                        JSONObject company = associatedCompanies.getJSONObject(j);
                        String companyName = company.optString("companyname", "N/A");
                        String companyFieldValue = company.optString(dbField, "").trim();
                        if (companyFieldValue.isEmpty()) {
                            companyFieldValue = "N/A";
                        }
                        logMessage.append("  Company ").append(j + 1).append(": ").append(companyName);
                        logMessage.append(" | ").append(fieldName).append(": ").append(companyFieldValue).append("\n");
                    }
                } else {
                    logMessage.append("  No companies found\n");
                }
                logMessage.append("\n");
            }
            
            logMessage.append("</code></pre>");
            FilterSearchReporter.logInfo(logMessage.toString());
        }
    }

    public void createEntityCFValueMap() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            CompletableFuture<JsonPath> candidateJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> candidateJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            CompletableFuture<JsonPath> companyJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> companyJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);

            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (companyJson1, contactJson1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson1.getString("slug"), contactJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (companyJson2, contactJson2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson2.getString("slug"), contactJson2.getString("slug")).jsonPath(), executor);

            CompletableFuture<JsonPath> dealJson1Future = jobJson1Future.thenApplyAsync(jobJson1 -> {
                String companySlug1 = companyJson1Future.join().getString("slug");
                String contactSlug1 = contactJson1Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug1, contactSlug1, jobJson1.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> dealJson2Future = jobJson2Future.thenApplyAsync(jobJson2 -> {
                String companySlug2 = companyJson2Future.join().getString("slug");
                String contactSlug2 = contactJson2Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug2, contactSlug2, jobJson2.getString("slug")).jsonPath();
            }, executor);

            CompletableFuture<JsonPath> userJsonFuture = CompletableFuture.supplyAsync(() -> {
                return function.getUsers(baseURL, accountOwnerAPIKey).jsonPath();
            }, executor);

            CompletableFuture<JsonPath> teamJsonFuture = userJsonFuture.thenApplyAsync((userJson) -> {
                ArrayList<String> team1UserId = new ArrayList<String>();
                ArrayList<String> team2UserId = new ArrayList<String>();
                team1UserId.add(String.valueOf(userJson.getInt("[1].id")));
                team1UserId.add(String.valueOf(userJson.getInt("[3].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[0].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[2].id")));

                Response team1Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", team1UserId);
                Response team2Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team2", team2UserId);
                team1Response.then().statusCode(200);
                team2Response.then().statusCode(200);
                Response team = function.getTeams(baseURL, accountOwnerAPIKey);
                return team.jsonPath();
            }, executor);

            CompletableFuture.allOf(
                    candidateJson1Future, candidateJson2Future,
                    companyJson1Future, companyJson2Future,
                    contactJson1Future, contactJson2Future,
                    jobJson1Future, jobJson2Future,
                    dealJson1Future, dealJson2Future,
                    userJsonFuture, teamJsonFuture
            ).join();

            JsonPath candidateJson1 = candidateJson1Future.join();
            JsonPath candidateJson2 = candidateJson2Future.join();
            JsonPath companyJson1 = companyJson1Future.join();
            JsonPath companyJson2 = companyJson2Future.join();
            JsonPath contactJson1 = contactJson1Future.join();
            JsonPath contactJson2 = contactJson2Future.join();
            JsonPath jobJson1 = jobJson1Future.join();
            JsonPath jobJson2 = jobJson2Future.join();
            JsonPath dealJson1 = dealJson1Future.join();
            JsonPath dealJson2 = dealJson2Future.join();
            JsonPath userJson = userJsonFuture.join();
            JsonPath teamJson = teamJsonFuture.join();
            entityCFValueMap.put("candidate1", candidateJson1.getString("slug"));
            entityCFValueMap.put("candidate2", candidateJson2.getString("slug"));
            entityCFValueMap.put("company1", companyJson1.getString("slug"));
            entityCFValueMap.put("company2", companyJson2.getString("slug"));
            entityCFValueMap.put("contact1", contactJson1.getString("slug"));
            entityCFValueMap.put("contact2", contactJson2.getString("slug"));
            entityCFValueMap.put("job1", jobJson1.getString("slug"));
            entityCFValueMap.put("job2", jobJson2.getString("slug"));
            entityCFValueMap.put("deal1", dealJson1.getString("slug"));
            entityCFValueMap.put("deal2", dealJson2.getString("slug"));
            entityCFValueMap.put("owner", String.valueOf(userJson.getInt("[0].id")));
            entityCFValueMap.put("admin", String.valueOf(userJson.getInt("[1].id")));
            entityCFValueMap.put("restricted", String.valueOf(userJson.getInt("[2].id")));
            entityCFValueMap.put("teamMember", String.valueOf(userJson.getInt("[3].id")));
            entityCFValueMap.put("team1", teamJson.getString("[0].team_id"));
            entityCFValueMap.put("team2", teamJson.getString("[1].team_id"));
            int entityCompany1Id = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug"));
            int entityCompany2Id = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug"));
            companyKeyToIdMap.put("entityCompany1", String.valueOf(entityCompany1Id));
            companyKeyToIdMap.put("entityCompany2", String.valueOf(entityCompany2Id));
            companyKeyToIdMap.put("entitycompany1", String.valueOf(entityCompany1Id));
            companyKeyToIdMap.put("entitycompany2", String.valueOf(entityCompany2Id));
            companyIdToKeyMap.put(String.valueOf(entityCompany1Id), "entityCompany1");
            companyIdToKeyMap.put(String.valueOf(entityCompany2Id), "entityCompany2");
            
            // Get contact IDs from contactJson1 and contactJson2 for entityContact1/2 mapping
            String contactSlug1 = contactJson1.getString("slug");
            String contactSlug2 = contactJson2.getString("slug");
            int entityContact1Id = function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactSlug1);
            int entityContact2Id = function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactSlug2);
            // Store entity contacts in contactKeyToIdMap as entityContact1/2 for contact filter validation
            contactKeyToIdMap.put("entityContact1", String.valueOf(entityContact1Id));
            contactKeyToIdMap.put("entityContact2", String.valueOf(entityContact2Id));
            contactKeyToIdMap.put("entitycontact1", String.valueOf(entityContact1Id));
            contactKeyToIdMap.put("entitycontact2", String.valueOf(entityContact2Id));
            contactIdToKeyMap.put(String.valueOf(entityContact1Id), "entityContact1");
            contactIdToKeyMap.put(String.valueOf(entityContact2Id), "entityContact2");
            contactSlugToKeyMap.put(contactSlug1, "entityContact1");
            contactSlugToKeyMap.put(contactSlug2, "entityContact2");
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, Integer> createCustomFields() {
        Map<String, Integer> customFieldIds = new HashMap<>();
        commanFunction function = new commanFunction();
        int colId = 1;

        List<String> entityTypes = new ArrayList<>(List.of("candidate", "company", "deals", "job", "contact", "user", "team", "text", "email", "phonenumber", "longtext", "number", "date", "datetime", "social_profile", "dropdown", "multiselect", "checkbox", "file"));
        
        for (String entity : entityTypes) {
            String fieldName = entity + "CF";
            Response response;

            if (entity.equals("dropdown") || entity.equals("multiselect")) {
                response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "company", fieldName, entity, "Option A, Option B, OptionC");
            } else {
                response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "company", fieldName, entity, "", colId);
            }
            colId++;
            
            Assert.assertEquals(response.getStatusCode(), 200, "Failed to create custom field: " + fieldName);
            
            int columnId = response.jsonPath().getInt("data.custumField.columnid");
            customFieldIds.put(fieldName, columnId);
        }
        
        return customFieldIds;
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        String todayDate = DateUtil.getTodayDateString("yyyy-MM-dd");
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("dateCF", todayDate);
        scenarios.put("today_scenario", todayTimestamps);

        String yesterdayDate = DateUtil.getYesterdayDateString("yyyy-MM-dd");
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("dateCF", yesterdayDate);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        String thisWeekDate = DateUtil.getThisWeekDateString();
        LocalDate thisWeekLocalDate = LocalDate.parse(thisWeekDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("dateCF", thisWeekLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        String lastWeekDate = DateUtil.getLastWeekDateString();
        LocalDate lastWeekLocalDate = LocalDate.parse(lastWeekDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("dateCF", lastWeekLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        String thisMonthDate = DateUtil.getThisMonthDateString();
        LocalDate thisMonthLocalDate = LocalDate.parse(thisMonthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("dateCF", thisMonthLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        String lastMonthDate = DateUtil.getLastMonthDateString();
        LocalDate lastMonthLocalDate = LocalDate.parse(lastMonthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("dateCF", lastMonthLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        String thisQuarterDate = DateUtil.getThisQuarterDateString();
        LocalDate thisQuarterLocalDate = LocalDate.parse(thisQuarterDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("dateCF", thisQuarterLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        String lastQuarterDate = DateUtil.getLastQuarterDateString();
        LocalDate lastQuarterLocalDate = LocalDate.parse(lastQuarterDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("dateCF", lastQuarterLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        String thisYearDate = DateUtil.getThisYearDateString();
        LocalDate thisYearLocalDate = LocalDate.parse(thisYearDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("dateCF", thisYearLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_year_scenario", thisYearTimestamps);

        String lastYearDate = DateUtil.getLastYearDateString();
        LocalDate lastYearLocalDate = LocalDate.parse(lastYearDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("dateCF", lastYearLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_year_scenario", lastYearTimestamps);

        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("dateCF", "2022-06-15");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("dateCF", "2023-03-10");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        return scenarios;
    }

    public JSONObject processPayloadPlaceholders(JSONObject payload) {
        JSONObject processedPayload = new JSONObject();
        
        for (String key : payload.keySet()) {
            Object value = payload.get(key);
            
            // Handle nested JSONObject
            if (value instanceof JSONObject) {
                JSONObject nestedObject = (JSONObject) value;
                JSONObject processedNestedObject = processPayloadPlaceholders(nestedObject);
                processedPayload.put(key, processedNestedObject);
            }
            // Handle JSONArray
            else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                JSONArray processedArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    Object arrayItem = array.get(i);
                    if (arrayItem instanceof JSONObject) {
                        processedArray.put(processPayloadPlaceholders((JSONObject) arrayItem));
                    } else {
                        processedArray.put(arrayItem);
                    }
                }
                processedPayload.put(key, processedArray);
            }
            // Handle placeholder keys (custom fields)
            else if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                
                if (customFieldIds.containsKey(trimmedKey)) {
                    String newKey = "custcolumn" + customFieldIds.get(trimmedKey);
                    if (value instanceof String) {
                        String stringValue = (String) value;
                        String processedValue = processEntityPlaceholders(stringValue);
                        processedPayload.put(newKey, processedValue);
                    } else {
                        processedPayload.put(newKey, value);
                    }
                } else {
                    processedPayload.put(key, value);
                }
            }
            // Handle regular keys
            else {
                processedPayload.put(key, value);
            }
        }
        
        return processedPayload;
    }

    public String processEntityPlaceholders(String value) {
        if (value == null) {
            return null;
        }
        
        if (value.startsWith("{") && value.endsWith("}")) {
            String innerValue = value.substring(1, value.length() - 1);
            String[] entityKeys = innerValue.split(",");
            
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < entityKeys.length; i++) {
                String entityKey = entityKeys[i].trim();
                if (entityCFValueMap.containsKey(entityKey)) {
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(entityCFValueMap.get(entityKey));
                } else {
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(entityKey);
                }
            }

            return result.toString();
        }
        return value;
    }


    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);

        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        String[] values = processedFilterValue.split(",");
        JSONArray valueArray = new JSONArray();
        if (filterType.equals("is_empty") || filterType.equals("has_any_value")) {
            filterValueObj.put("value", valueArray);
        } else {
            for (String val : values) {
                if(filterValue_TYPE.equals("INTEGER_LIST")) {
                    valueArray.put(Integer.parseInt(val.trim()));
                } else {
                    valueArray.put(val.trim());
                }
            }
            filterValueObj.put("value", valueArray);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", true);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createTextFieldFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        // Create filterValue object with type and value
        JSONObject filterValueObj;
        if (filterValue_TYPE.equals("STRING_LIST")) {
            filterValueObj = stringListFilterValue(processedFilterValue);
        } else {
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            filterValueObj.put("value", processedFilterValue);
        }

        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();

        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", true);
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

    public JSONObject createNumberFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj;
        if ("NUMERIC_STRING_START_END".equals(filterValue_TYPE) && "is_between".equals(filterType)) {
            filterValueObj = numericStringStartEndFilterValue(filterValue);
        } else if ("STRING_START_END".equals(filterValue_TYPE) && "is_between".equals(filterType)) {
            filterValueObj = stringStartEndFilterValue(filterValue);
        } else if ("STRING".equals(filterValue_TYPE)) {
            filterValueObj = stringFilterValue(filterValue);
        } else {
            filterValueObj = numericStringFilterValue(filterValue);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", true);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createDateFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);
        JSONObject filterValueObj;
        
        if ("LONG_START_END".equals(filterValue_TYPE) && "is_between".equals(filterType)) {
            filterValueObj = dateStartEndFilterValue(processedFilterValue);
        } else if ("DATE_IS".equals(filterValue_TYPE)) {
            filterValueObj = dateIsFilterValue(processedFilterValue);
        } else if ("LONG".equals(filterValue_TYPE)) {
            filterValueObj = longFilterValue(processedFilterValue);
        } else if ("INTEGER".equals(filterValue_TYPE)) {
            filterValueObj = integerFilterValue(processedFilterValue);
        } else if (processedFilterValue == null || processedFilterValue.trim().isEmpty()) {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        } else {
            filterValueObj = dateIsFilterValue(processedFilterValue);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", true);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createDropdownAndMultiselectFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        String[] values = processedFilterValue.split(",");
        JSONArray valueArray = new JSONArray();
        if (filterType.equals("is_empty") || filterType.equals("has_any_value")) {
            filterValueObj.put("value", valueArray);
        } else {    
            for (String val : values) {
                valueArray.put(val.trim());
            }
            filterValueObj.put("value", valueArray);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", true);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }
}
