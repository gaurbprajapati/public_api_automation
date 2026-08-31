package io.recruitcrm.CompanyService.Filters;

import com.qa.api.util.reaper.ReaperIntegration;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.DateUtil;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CustomFieldCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String accountOwnerAPIKey;
    String email;
    Map<String, Integer> customFieldIds = new HashMap<>();
    Map<String, String> entityCFValueMap = new HashMap<>();
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyIdToKeyMap = new ConcurrentHashMap<>();
    Map<String, List<JsonPath>> contactDataMap = new HashMap<>();
    Map<Integer, String> companyIdToKeyMapForContacts = new HashMap<>();
    Map<String, String> companySlugToKeyMap = new HashMap<>();
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
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "textCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Text Custom Fields (Cross Entity)")
    public void textCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createTextFieldFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateTextFieldCrossEntityFilteredData(contactDataByCompany, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Number Custom Fields (Cross Entity)")
    public void numberCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createNumberFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateNumberFieldCrossEntityFilteredData(contactDataByCompany, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Date Custom Fields (Cross Entity)")
    public void dateCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateDateFieldCrossEntityFilteredData(contactDataByCompany, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Dropdown and Multiselect Custom Fields (Cross Entity)")
    public void dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createDropdownAndMultiselectFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @DataProvider(name = "entityCustomFieldCrossEntityFilterSearchTestData")
    public Object[][] entityCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        List<String> entityTypes = Arrays.asList("candidate", "company", "contact", "job", "deal", "user", "team");
        
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
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("date")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "numberCustomFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] numberCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityCustomFieldFilterDataProvider.json");
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
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("text") || tests.getJSONObject(i).getString("fieldType").equals("phonenumber")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTestData")
    public Object[][] dropdownAndMultiselectCustomFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityCustomFieldFilterDataProvider.json");
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

    public void validateTextFieldCrossEntityFilteredData(JSONObject contactDataByCompany, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(contactDataByCompany.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && contactDataByCompany.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String companySlug : contactDataByCompany.keySet()) {
            JSONArray companyAssociatedContacts = contactDataByCompany.getJSONArray(companySlug);
            boolean atLeastOneMatch = false;
            if (filterType.equals("is_not") || filterType.equals("does_not_contain") || filterType.equals("is_empty")) {
                if (companyAssociatedContacts.length() == 0) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            for (int i = 0; i < companyAssociatedContacts.length(); i++) {
                JSONObject contact = companyAssociatedContacts.getJSONObject(i);
                boolean matches = validateTextFieldFilteredDataBoolean(contact, filterType, filterValue, fieldName, dbField, expectedResult, "Contact", "");
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No contact matched the filter for company still coming in the response: " + companySlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    public void validateNumberFieldCrossEntityFilteredData(JSONObject contactDataByCompany, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(contactDataByCompany.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && contactDataByCompany.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String companySlug : contactDataByCompany.keySet()) {
            JSONArray companyAssociatedContacts = contactDataByCompany.getJSONArray(companySlug);
            boolean atLeastOneMatch = false;
            for (int i = 0; i < companyAssociatedContacts.length(); i++) {
                JSONObject contact = companyAssociatedContacts.getJSONObject(i);
                String contactNumber = String.valueOf(contact.opt(dbField)).trim();
                if (contactNumber.equals("null") || contactNumber.isEmpty()) {
                    if (filterType.equals("is_empty") || filterType.equals("is_not") || filterType.equals("does_not_contain")) {
                        atLeastOneMatch = true;
                        break;
                    }
                    continue;
                }
                
                if (contactNumber.endsWith(".0")) {
                    contactNumber = contactNumber.substring(0, contactNumber.length() - 2);
                }
                
                boolean matches = validateNumberFieldFilteredDataBoolean(contactNumber, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No contact matched the filter for company still coming in the response: " + companySlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateNumberFieldFilteredDataBoolean(String contactNumber, String filterType, String filterValue, String fieldName) {
        try {
            if (filterType.equals("begins_with")) {
                return contactNumber.startsWith(filterValue);
            }
            if (filterType.equals("ends_with")) {
                return contactNumber.endsWith(filterValue);
            }
            if (filterType.equals("has_any_value")) {
                return !contactNumber.isEmpty() && !contactNumber.equals("null");
            }
            if (filterType.equals("is_empty")) {
                return contactNumber.isEmpty() || contactNumber.equals("null");
            }

            double contactNumberValue = Double.parseDouble(contactNumber);
            
            // Handle is_between separately since filterValue contains comma
            if (filterType.equals("is_between")) {
                String[] rangeParts = filterValue.split(",");
                double startValue = Double.parseDouble(rangeParts[0].trim());
                double endValue = Double.parseDouble(rangeParts[1].trim());
                return contactNumberValue >= startValue && contactNumberValue <= endValue;
            }

            // For other filter types, parse filterValue as double
            double filterValueDouble = Double.parseDouble(filterValue.trim());
            
            switch (filterType) {
                case "is":
                    return contactNumberValue == filterValueDouble;
                case "is_not":
                    return contactNumberValue != filterValueDouble;
                case "is_mt":
                    return contactNumberValue > filterValueDouble;
                case "is_lt":
                    return contactNumberValue < filterValueDouble;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void validateDateFieldCrossEntityFilteredData(JSONObject contactDataByCompany, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(contactDataByCompany.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && contactDataByCompany.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String companySlug : contactDataByCompany.keySet()) {
            JSONArray companyAssociatedContacts = contactDataByCompany.getJSONArray(companySlug);
            boolean atLeastOneMatch = false;
            if (filterType.equals("is_not") || filterType.equals("does_not_contain") || filterType.equals("is_empty")) {
                if (companyAssociatedContacts.length() == 0) {
                    atLeastOneMatch = true;
                }
            }
            for (int i = 0; i < companyAssociatedContacts.length(); i++) {
                JSONObject contact = companyAssociatedContacts.getJSONObject(i);
                String contactDateStr = contact.optString(dbField, "").trim();
                boolean matches = validateDateFieldFilteredDataBoolean(contactDateStr, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No contact matched the filter for company still coming in the response: " + companySlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateDateFieldFilteredDataBoolean(String contactDate, String filterType, String filterValue, String fieldName) {
        if (contactDate.isEmpty() || contactDate.equals("null")) {
            return filterType.equals("is_empty");
        }

        try {
            LocalDate contactParsedDate = parseDate(contactDate);
            
            switch (filterType) {
                case "is":
                case "is_equal_to":
                    if (isRelativeDatePeriod(filterValue)) {
                        return isDateInPeriod(contactParsedDate, filterValue);
                    } else {
                        LocalDate filterDate = parseDate(filterValue);
                        return contactParsedDate.equals(filterDate);
                    }
                case "is_not":
                    LocalDate filterDateNot = parseDate(filterValue);
                    return !contactParsedDate.equals(filterDateNot);
                case "is_before":
                    LocalDate filterDateBefore = parseDate(filterValue);
                    return contactParsedDate.isBefore(filterDateBefore);
                case "is_after":
                    LocalDate filterDateAfter = parseDate(filterValue);
                    return contactParsedDate.isAfter(filterDateAfter) || contactParsedDate.isEqual(filterDateAfter);
                case "is_between":
                    String[] dates = filterValue.split(",");
                    if (dates.length != 2) {
                        return false;
                    }
                    LocalDate startDate = parseDate(dates[0].trim());
                    LocalDate endDate = parseDate(dates[1].trim());
                    return (contactParsedDate.isEqual(startDate) || contactParsedDate.isAfter(startDate)) &&
                           (contactParsedDate.isEqual(endDate) || contactParsedDate.isBefore(endDate));
                case "is_mt":
                    int days = Integer.parseInt(filterValue);
                    LocalDate cutoffDate = LocalDate.now().minusDays(days);
                    return contactParsedDate.isBefore(cutoffDate) || contactParsedDate.isEqual(cutoffDate);
                case "is_lt":
                    int daysLt = Integer.parseInt(filterValue);
                    LocalDate cutoffDateLt = LocalDate.now().minusDays(daysLt);
                    return contactParsedDate.isAfter(cutoffDateLt) || contactParsedDate.isEqual(cutoffDateLt);
                case "has_any_value":
                    return !contactDate.isEmpty() && !contactDate.equals("null");
                case "is_empty":
                    return contactDate.isEmpty() || contactDate.equals("null");
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    public JSONObject getContactsForCompanies(JSONArray companyData) {
        JSONObject contactResult = new JSONObject();
        for (int i = 0; i < companyData.length(); i++) {
            JSONArray companyAssociatedContacts = new JSONArray();
            JSONObject company = companyData.getJSONObject(i);
            String companySlug = company.getString("slug");
            List<JsonPath> contactList = contactDataMap.get(companySlug);
            if (contactList != null) {
                for (JsonPath contactJsonPath : contactList) {
                    Map<String, Object> contactMap = contactJsonPath.get("data.contact");
                    JSONObject contact = new JSONObject(contactMap);
                    companyAssociatedContacts.put(contact);
                }
            }
            contactResult.put(companySlug, companyAssociatedContacts);
        }
        return contactResult;
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/companyContactCF_data.json");
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
                    String contactSlug = null;
                    Object contactSlugObj = response.jsonPath().get("data.contact.slug");
                    if (contactSlugObj != null) {
                        contactSlug = contactSlugObj.toString();
                    }
                    
                    companyIdToKeyMap.put(String.valueOf(companyId), companyKey);
                    companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                    
                    synchronized (companyIdToKeyMapForContacts) {
                        companyIdToKeyMapForContacts.put(companyId, companyKey);
                    }
                    synchronized (companySlugToKeyMap) {
                        companySlugToKeyMap.put(companySlug, companyKey);
                    }
                    
                    if (contactSlug != null && !contactSlug.isEmpty()) {
                        Response contactResponse = getContact(contactSlug);
                        JsonPath contactJsonPath = contactResponse.jsonPath();
                        
                        synchronized (contactDataMap) {
                            List<JsonPath> contactList = new ArrayList<>();
                            contactList.add(contactJsonPath);
                            contactDataMap.put(companySlug, contactList);
                        }
                    } else {
                        // Company created without contact (e.g., company7)
                        synchronized (contactDataMap) {
                            contactDataMap.put(companySlug, new ArrayList<>());
                        }
                    }
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // Create additional contacts for companies 2, 4, and 6
            createAdditionalContacts(companyJson);
            updateContactCustomDateFieldsTimestamps();
            refreshContactDataMap();
        } finally {
            executor.shutdown();
        }
    }

    private void updateContactCustomDateFieldsTimestamps() {
        timestampScenarios = createTimestampScenarios();
        String dateCFFieldName = "dateCF";
        
        if (!customFieldIds.containsKey(dateCFFieldName)) {
            return;
        }
        
        String dateCFDbField = "custcolumn" + customFieldIds.get(dateCFFieldName);
        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(timestampScenarios.entrySet());

        List<Map.Entry<String, JsonPath>> contactsWithCompany = new ArrayList<>();
        for (Map.Entry<String, List<JsonPath>> entry : contactDataMap.entrySet()) {
            String companySlug = entry.getKey();
            List<JsonPath> contactList = entry.getValue();
            if (contactList != null && !contactList.isEmpty()) {
                contactsWithCompany.add(new AbstractMap.SimpleEntry<>(companySlug, contactList.get(0)));
            }
        }


        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (int scenarioIndex = 0; scenarioIndex < scenarios.size(); scenarioIndex++) {
                if (scenarioIndex >= contactsWithCompany.size()) {
                    Map.Entry<String, Map<String, String>> scenario = scenarios.get(scenarioIndex);
                    continue;
                }
                
                final int idx = scenarioIndex;
                final Map.Entry<String, Map<String, String>> scenario = scenarios.get(scenarioIndex);
                final Map<String, String> timestamps = scenario.getValue();
                final Map.Entry<String, JsonPath> contactEntry = contactsWithCompany.get(idx);
                final JsonPath contactJsonPath = contactEntry.getValue();

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        int contactId = contactJsonPath.getInt("data.contact.id");
                        String dateValue = timestamps.get(dateCFFieldName);

                        if (dateValue != null) {
                            JSONObject fieldsAndTimestamps = new JSONObject();
                            fieldsAndTimestamps.put(dateCFDbField, dateValue);

                            Response updateResponse = ReaperIntegration.updateContactFields(contactId, fieldsAndTimestamps);
                            if (updateResponse.getStatusCode() != 200) {
                                Assert.fail("Failed to update custom date field for contact: " + contactId);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executor));
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void createAdditionalContacts(JSONObject companyJson) {
        String[] companiesToAddContacts = {"company2", "company4", "company6"};
        
        for (String companyKey : companiesToAddContacts) {
            if (companyJson.has(companyKey)) {
                JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                if (companyEntry.has("additionalContact")) {
                    JSONObject additionalContact = companyEntry.getJSONObject("additionalContact");
                    if (additionalContact.has("contact1")) {
                        JSONObject contact1 = additionalContact.getJSONObject("contact1");
                        JSONObject payload = contact1.getJSONObject("payload");
                        JSONObject processedPayload = processPayloadPlaceholders(payload);
                        
                        String companySlug = null;
                        String companyId = null;
                        
                        for (Map.Entry<String, String> entry : companySlugToKeyMap.entrySet()) {
                            if (entry.getValue().equals(companyKey)) {
                                companySlug = entry.getKey();
                                break;
                            }
                        }
                        
                        for (Map.Entry<String, String> entry : companyKeyToIdMap.entrySet()) {
                            if (entry.getKey().equals(companyKey)) {
                                companyId = entry.getValue();
                                break;
                            }
                        }
                        
                        if (companySlug != null && companyId != null) {
                            JSONArray selectedCompanies = processedPayload.getJSONArray("selectedcompanies");
                            if (selectedCompanies.length() > 0) {
                                JSONObject companyInfo = selectedCompanies.getJSONObject(0);
                                companyInfo.put("slug", companySlug);
                                companyInfo.put("id", companyId);
                            }
                            
                            try {
                                Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", 
                                        albatrossAuthToken, null, true, processedPayload);
                                response.then().statusCode(200);
                                
                                JsonPath contactJsonPath = response.jsonPath();
                                String contactSlug = null;
                                Object contactSlugObj = contactJsonPath.get("data.contact.slug");
                                if (contactSlugObj != null) {
                                    contactSlug = contactSlugObj.toString();
                                }
                                if (contactSlug == null || contactSlug.isEmpty()) {
                                    continue;
                                }
                                
                                Response contactResponse = getContact(contactSlug);
                                JsonPath additionalContactJsonPath = contactResponse.jsonPath();
                                
                                synchronized (contactDataMap) {
                                    List<JsonPath> contactList = contactDataMap.get(companySlug);
                                    if (contactList != null) {
                                        contactList.add(additionalContactJsonPath);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to create additional contact for " + companyKey + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    public Response getContact(String contactSlug) {
        String basePath = "/contacts/{contactSlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactSlug", contactSlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    private void refreshContactDataMap() {
        synchronized (contactDataMap) {
            for (Map.Entry<String, List<JsonPath>> entry : contactDataMap.entrySet()) {
                String companySlug = entry.getKey();
                List<JsonPath> existingContacts = entry.getValue();
                if (existingContacts == null || existingContacts.isEmpty()) {
                    continue;
                }

                List<JsonPath> refreshed = new ArrayList<>();
                for (JsonPath contactJsonPath : existingContacts) {
                    try {
                        String contactSlug = contactJsonPath.getString("data.contact.slug");
                        if (contactSlug == null || contactSlug.isEmpty()) {
                            refreshed.add(contactJsonPath);
                            continue;
                        }
                        Response updatedContactResponse = getContact(contactSlug);
                        refreshed.add(updatedContactResponse.jsonPath());
                    } catch (Exception e) {
                        refreshed.add(contactJsonPath);
                    }
                }
                contactDataMap.put(companySlug, refreshed);
            }
        }
    }

    private void logCompanyNameAndContact(Response response, JSONArray companyData, JSONObject contactDataByCompany, String fieldName, String dbField) {
        FilterSearchReporter.logInfo("<b>📋 Company - Contact Custom Field Information:</b>");
        
        if (companyData != null && contactDataByCompany != null) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
            logMessage.append("<code>");
            
            for (int i = 0; i < companyData.length(); i++) {
                JSONObject company = companyData.getJSONObject(i);
                String companySlug = company.getString("slug");
                JSONArray associatedContacts = contactDataByCompany.optJSONArray(companySlug);
                
                String companyName = company.optString("companyname", "N/A");
                logMessage.append("Company: ").append(companyName).append("\n");
                
                if (associatedContacts != null && associatedContacts.length() > 0) {
                    for (int j = 0; j < associatedContacts.length(); j++) {
                        JSONObject contact = associatedContacts.getJSONObject(j);
                        String firstName = contact.optString("firstname", "").trim();
                        String lastName = contact.optString("lastname", "").trim();
                        String contactName = (firstName + " " + lastName).trim();
                        if (contactName.isEmpty()) {
                            contactName = "Unknown";
                        }
                        String contactFieldValue = contact.optString(dbField, "").trim();
                        if (contactFieldValue.isEmpty()) {
                            contactFieldValue = "N/A";
                        }
                        logMessage.append("  Contact ").append(j + 1).append(": ").append(contactName);
                        logMessage.append(" | ").append(fieldName).append(": ").append(contactFieldValue).append("\n");
                    }
                } else {
                    logMessage.append("  No contacts found\n");
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

            // Add entityCompany contacts to contactDataMap
            String entityCompany1Slug = companyJson1.getString("slug");
            String entityCompany2Slug = companyJson2.getString("slug");
            String entityContact1Slug = contactJson1.getString("slug");
            String entityContact2Slug = contactJson2.getString("slug");

            // Fetch and store contacts for entityCompany1
            try {
                Response contact1Response = getContact(entityContact1Slug);
                JsonPath contact1JsonPath = contact1Response.jsonPath();
                synchronized (contactDataMap) {
                    List<JsonPath> contactList1 = new ArrayList<>();
                    contactList1.add(contact1JsonPath);
                    contactDataMap.put(entityCompany1Slug, contactList1);
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch contact for entityCompany1: " + e.getMessage());
            }

            // Fetch and store contacts for entityCompany2
            try {
                Response contact2Response = getContact(entityContact2Slug);
                JsonPath contact2JsonPath = contact2Response.jsonPath();
                synchronized (contactDataMap) {
                    List<JsonPath> contactList2 = new ArrayList<>();
                    contactList2.add(contact2JsonPath);
                    contactDataMap.put(entityCompany2Slug, contactList2);
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch contact for entityCompany2: " + e.getMessage());
            }
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, Integer> createCustomFields() {
        Map<String, Integer> customFieldIds = new HashMap<>();
        commanFunction function = new commanFunction();

        List<String> entityTypes = new ArrayList<>(List.of("candidate", "company", "deals", "job", "contact", "user", "team", "text", "email", "phonenumber", "longtext", "number", "date", "social_profile", "dropdown", "multiselect"));
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        try {
            List<CompletableFuture<AbstractMap.SimpleEntry<String, Response>>> futures = entityTypes.stream()
                .map(entity -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // Small delay to stagger parallel requests
                        Thread.sleep(1000); // 300–500 ms usually works well
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    String fieldName = entity + "CF";
                    Response response;

                    if (entity.equals("dropdown") || entity.equals("multiselect")) {
                        response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "contact", fieldName, entity, "Option A, Option B, OptionC");
                    } else{
                        response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "contact", fieldName, entity, "");
                    }
                    
                    return new AbstractMap.SimpleEntry<>(fieldName, response);
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            for (CompletableFuture<AbstractMap.SimpleEntry<String, Response>> future : futures) {
                AbstractMap.SimpleEntry<String, Response> result = future.get();
                String fieldName = result.getKey();
                Response response = result.getValue();
                
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to create custom field: " + fieldName);
                
                int columnId = response.jsonPath().getInt("data.custumField.columnid");
                customFieldIds.put(fieldName, columnId);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error creating custom fields in parallel", e);
        } finally {
            executor.shutdown();
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
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "contact");
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
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createTextFieldFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
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
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "contact");
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
        payload.put("advancedSearchContext", "COMPANY");
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
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "contact");
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
        payload.put("advancedSearchContext", "COMPANY");
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
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "contact");
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
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }
}
