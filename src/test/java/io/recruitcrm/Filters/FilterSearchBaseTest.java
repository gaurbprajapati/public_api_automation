package io.recruitcrm.Filters;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.*;
import com.qa.api.util.reaper.ReaperIntegration;

public class FilterSearchBaseTest extends TestBase {

    protected static final String ADVANCED_SEARCH_CANDIDATES_GET_PATH = "advanced-search/candidates/search/get?page=1&size=100";
    protected static final String ADVANCED_SEARCH_CONTACTS_GET_PATH = "advanced-search/contacts/search/get?page=1&size=100";
    protected static final String ADVANCED_SEARCH_COMPANIES_GET_PATH = "advanced-search/companies/search/get?page=1&size=100";
    protected static final String ADVANCED_SEARCH_JOBS_GET_PATH = "advanced-search/jobs/search/get?page=1&size=100";
    protected static final String ADVANCED_SEARCH_CANDIDATES_GET_PATH_BASE = "advanced-search/candidates/search/get";
    protected static final String ADVANCED_SEARCH_CONTACTS_GET_PATH_BASE = "advanced-search/contacts/search/get";
    protected static final String ADVANCED_SEARCH_COMPANIES_GET_PATH_BASE = "advanced-search/companies/search/get";
    protected static final String ADVANCED_SEARCH_JOBS_GET_PATH_BASE = "advanced-search/jobs/search/get";
    protected static final String ADVANCED_SEARCH_BOOLEAN_VALIDATION_PATH = "advanced-search/boolean-search/validation";

    /** TestNG group for reduced Aries smoke runs ({@code -DtestGroup=aries_service_smoke}). */
    public static final String ARIES_SERVICE_SMOKE_GROUP = "aries_service_smoke";

    private static final int DEFAULT_ARIES_SMOKE_ROW_LIMIT = 5; //72 expected

    /** True when Surefire runs with {@code -DtestGroup=aries_service_smoke} (or {@code -Daries.smoke.run=true}). */
    public static boolean isAriesSmokeRun() {
        if ("true".equalsIgnoreCase(System.getProperty("aries.smoke.run"))) {
            return true;
        }
        String testGroup = System.getProperty("testGroup", "");
        if (testGroup.contains(ARIES_SERVICE_SMOKE_GROUP)) {
            return true;
        }
        return System.getProperty("suiteType", "").contains(ARIES_SERVICE_SMOKE_GROUP);
    }

    /** Returns the first N rows of a data provider (N from {@code -Daries.smoke.row.limit}, default 2). */
    protected static Object[][] limitSmokeRows(Object[][] rows) {
        if (rows == null || rows.length == 0) {
            return rows;
        }
        int limit = Integer.getInteger("aries.smoke.row.limit", DEFAULT_ARIES_SMOKE_ROW_LIMIT);
        if (limit <= 0 || rows.length <= limit) {
            return rows;
        }
        Object[][] limited = new Object[limit][];
        System.arraycopy(rows, 0, limited, 0, limit);
        return limited;
    }

    protected Response executeFilterSearch(JSONObject payload, String authToken, String entity) {
        String path;
        switch (entity.toLowerCase()) {
            case "candidates":
                path = ADVANCED_SEARCH_CANDIDATES_GET_PATH;
                break;
            case "contacts":
                path = ADVANCED_SEARCH_CONTACTS_GET_PATH;
                break;
            case "companies":
                path = ADVANCED_SEARCH_COMPANIES_GET_PATH;
                break;
            case "jobs":
                path = ADVANCED_SEARCH_JOBS_GET_PATH;
                break;
            default:
                throw new IllegalArgumentException("Unknown entity for filter search: " + entity);
        }
        return RestClient.doPost("JSON", ariesServiceURL, path, authToken, null, true, payload);
    }

    public void validateMultiselectAndDropdownFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult, Map<String,String> entityKeyToIdMap, Map<String, String> entityIdToKeyMap, int allExpectedCount, String entityType) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected result is empty but response has data");
            return;
        }

        if(expectedResult.equals("All")) {
            Assert.assertEquals(data.length(), allExpectedCount, "Expected result is all but response has data");
            return;
        }

        String[] expectedEntities = expectedResult.split(",");
        List<Integer> expectedEntityIds = new ArrayList<>();
        for (String entityKey : expectedEntities) {
            String normalizedKey = entityKey.toLowerCase().replace(" ", "");
            String entityIdStr = entityKeyToIdMap.get(normalizedKey);
            if (entityIdStr == null) {
                Assert.fail("Expected " + entityType + " key '" + entityKey + "' (normalized: '" + normalizedKey + "') not found in entityKeyToIdMap. Available keys: " + entityKeyToIdMap.keySet());
            }
            expectedEntityIds.add(Integer.parseInt(entityIdStr));
        }

        List<Integer> actualEntityIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject entity = data.getJSONObject(i);
            actualEntityIds.add(entity.getInt("id"));
        }

        
        Assert.assertEquals(data.length(), expectedEntityIds.size(), "All expected " + entityType.toLowerCase() + "s are not present in the response");
        for (int entityId : expectedEntityIds) {
            if (!actualEntityIds.contains(entityId)) {
                Assert.fail(entityType + ": " + entityIdToKeyMap.get(String.valueOf(entityId)) + " is not present in the actual response but was expected to be present");
            }
        }
    }

    public void validateDateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult, String activityType, String albatrossAuthToken) {
        for (int i = 0; i < data.length(); i++) {
            commanFunction function = new commanFunction();
            JSONObject candidate = data.getJSONObject(i);
            String candidateSlug = candidate.getString("slug");
            Response response = function.getActivityBySlug(albatrossURL, albatrossAuthToken, candidateSlug, activityType);
            String responseBody = response.getBody().asString();
            JSONObject jsonObject = new JSONObject(responseBody);

            // Call logs share the same events array as notes; API key is "notes". When validating call log fields, skip type==0 (notes).
            boolean validatingCallLogActivities = "call_log".equals(activityType);
            String eventsArrayKey = validatingCallLogActivities ? "notes" : activityType;

            JSONArray responseData = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray(eventsArrayKey);

            if(expectedResult.equals("Empty")) {
                Assert.assertEquals(responseData.length(), 0, "Wrong candidate data for field: " + fieldName +  " and filterType: " + filterType + " and filterValue: " + filterValue);
                return;
            } else if (expectedResult.isEmpty()){
                Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
            String activityDate = null;
            boolean foundMatchingRecord = false;
            int callLogActivityCount = 0;
            for (int j = 0; j < responseData.length(); j++) {

                JSONObject activity = responseData.getJSONObject(j);

                if (validatingCallLogActivities && activity.getInt("type") == 0) {
                   continue;
                }

                if (validatingCallLogActivities) {
                    callLogActivityCount++;
                }

                activityDate = activity.optString(dbField, "0");
                if (validateDateAgainstFilter(activityDate, filterType, filterValue, fieldName)) {
                    foundMatchingRecord = true;
                    break;
                }

            }
            if (!foundMatchingRecord && "is_empty".equals(filterType) && validatingCallLogActivities && callLogActivityCount == 0) {
                foundMatchingRecord = true;
            }
            String activityDateHint = (activityDate == null || activityDate.trim().isEmpty())
                    ? "n/a (no call log row matched or events list empty)"
                    : activityDate;
            Assert.assertTrue(foundMatchingRecord, "Wrong candidate data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue + " ActivityDate: " + activityDateHint);
        }
    }

    public boolean validateDateAgainstFilter(String activityDate, String filterType, String filterValue, String fieldName) {
        try {
            if (activityDate == null || activityDate.trim().isEmpty() || activityDate.equals("0")) {
                return filterType.equals("is_empty");
            }

            switch (filterType) {
                case "is":
                case "is_equal_to":
                    return validateExactDateMatch(activityDate, filterValue);
                case "is_not":
                    return !validateExactDateMatch(activityDate, filterValue);
                case "is_before":
                    return validateDateBefore(activityDate, filterValue);
                case "is_after":
                    return validateDateAfter(activityDate, filterValue);
                case "is_between":
                    return validateDateBetween(activityDate, filterValue);
                case "is_mt":
                    return validateDateMoreThanDaysAgo(activityDate, filterValue);
                case "is_lt":
                    return validateDateLessThanDaysAgo(activityDate, filterValue);
                case "has_any_value":
                    return !activityDate.equals("0");
                case "is_empty":
                    return activityDate.equals("0");
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateExactDateMatch(String activityDate, String filterValue) {
        try {
            LocalDate activityLocalDate = parseDate(activityDate);
            if (isRelativeDatePeriod(filterValue)) {
                return isDateInPeriod(activityLocalDate, filterValue);
            } else {
                LocalDate filterDate = parseDate(filterValue);
                return activityLocalDate.equals(filterDate);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateDateBefore(String activityDate, String filterValue) {
        try {
            LocalDate activityLocalDate = parseDate(activityDate);
            LocalDate filterDate = parseDate(filterValue);
            return activityLocalDate.isBefore(filterDate);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateDateAfter(String activityDate, String filterValue) {
        try {
            LocalDate activityLocalDate = parseDate(activityDate);
            LocalDate filterDate = parseDate(filterValue);
            return activityLocalDate.isAfter(filterDate);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateDateBetween(String activityDate, String filterValue) {
        try {
            String[] dates = filterValue.split(",");
            if (dates.length != 2) {
                return false;
            }

            LocalDate activityLocalDate = parseDate(activityDate);
            LocalDate startDate = parseDate(dates[0].trim());
            LocalDate endDate = parseDate(dates[1].trim());

            return (activityLocalDate.isEqual(startDate) || activityLocalDate.isAfter(startDate)) &&
                    (activityLocalDate.isEqual(endDate) || activityLocalDate.isBefore(endDate));
        } catch (Exception e) {
            return false;
        }
    }

        public boolean validateDateMoreThanDaysAgo(String activityDate, String daysStr) {
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate activityLocalDate = parseDate(activityDate);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);
            return activityLocalDate.isBefore(cutoffDate) || activityLocalDate.isEqual(cutoffDate);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateDateLessThanDaysAgo(String activityDate, String daysStr) {
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate activityLocalDate = parseDate(activityDate);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);
            return activityLocalDate.isAfter(cutoffDate) || activityLocalDate.isEqual(cutoffDate);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRelativeDatePeriod(String filterValue) {
        return filterValue.equals("all_time") ||
                filterValue.equals("today") ||
                filterValue.equals("yesterday") ||
                filterValue.equals("this_week") ||
                filterValue.equals("last_week") ||
                filterValue.equals("this_month") ||
                filterValue.equals("last_month") ||
                filterValue.equals("this_quarter") ||
                filterValue.equals("last_quarter") ||
                filterValue.equals("this_year") ||
                filterValue.equals("last_year") ||
                filterValue.equals("last_30") ||
                filterValue.equals("last_60") ||
                filterValue.equals("last_90") ||
                filterValue.equals("last_365");
    }

    public boolean isDateInPeriod(LocalDate candidateDate, String period) {
        LocalDate startDate;
        LocalDate endDate;
        LocalDate now = LocalDate.now();

        switch (period) {
            case "all_time":
                return true;
            case "today":
                startDate = endDate = now;
                break;
            case "yesterday":
                startDate = endDate = now.minusDays(1);
                break;
            case "this_week":
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                endDate = startDate.plusDays(6);
                break;
            case "last_week":
                LocalDate lastWeekStart = now.minusDays(now.getDayOfWeek().getValue() + 6);
                startDate = lastWeekStart;
                endDate = lastWeekStart.plusDays(6);
                break;
            case "this_month":
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "last_month":
                LocalDate lastMonth = now.minusMonths(1);
                startDate = lastMonth.withDayOfMonth(1);
                endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                break;
            case "this_quarter":
                int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                int quarterStartMonth = (currentQuarter - 1) * 3 + 1;
                startDate = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                endDate = now.withMonth(quarterStartMonth + 2).withDayOfMonth(now.withMonth(quarterStartMonth + 2).lengthOfMonth());
                break;
            case "last_quarter":
                int lastQuarter = (now.getMonthValue() - 1) / 3;
                if (lastQuarter == 0) {
                    lastQuarter = 4;
                    now = now.minusYears(1);
                }
                int lastQuarterStartMonth = (lastQuarter - 1) * 3 + 1;
                startDate = now.withMonth(lastQuarterStartMonth).withDayOfMonth(1);
                endDate = now.withMonth(lastQuarterStartMonth + 2).withDayOfMonth(now.withMonth(lastQuarterStartMonth + 2).lengthOfMonth());
                break;
            case "this_year":
                startDate = now.withDayOfYear(1);
                endDate = now.withDayOfYear(now.lengthOfYear());
                break;
            case "last_year":
                LocalDate lastYear = now.minusYears(1);
                startDate = lastYear.withDayOfYear(1);
                endDate = lastYear.withDayOfYear(lastYear.lengthOfYear());
                break;
            case "last_30":
                endDate = now;
                startDate = endDate.minusDays(30);
                break;
            case "last_60":
                endDate = now;
                startDate = endDate.minusDays(60);
                break;
            case "last_90":
                endDate = now;
                startDate = endDate.minusDays(90);
                break;
            case "last_365":
                endDate = now;
                startDate = endDate.minusDays(365);
                break;
            default:
                return false;
        }

        return !candidateDate.isBefore(startDate) && !candidateDate.isAfter(endDate);
    }

    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        try {
            long epochSeconds = Long.parseLong(dateStr.trim());
            return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (NumberFormatException e) {
            // Not an epoch value, continue with date string parsing
        }

        DateTimeFormatter[] DATE_FORMATTERS = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                try {
                    return LocalDate.parse(dateStr.trim(), formatter);
                } catch (DateTimeParseException e) {
                    LocalDateTime dateTime = LocalDateTime.parse(dateStr.trim(), formatter);
                    return dateTime.toLocalDate();
                }
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }

        throw new IllegalArgumentException("Unable to parse date: " + dateStr +
                ". Supported formats: yyyy-MM-dd, yy-MM-dd, yyyy-MM-dd HH:mm:ss, MM/dd/yyyy, dd/MM/yyyy, ISO formats, or epoch seconds");
    }


    public Map<String,String> createUserMap(String apiKey) {
        Map<String,String> userMap = new HashMap<>();
        commanFunction function = new commanFunction();
        Response response = function.getUsers(baseURL, apiKey);
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restricted", user.get("[2].id").toString());
        userMap.put("teamMember", user.get("[3].id").toString());
        return userMap;
    }

    public Map<String,String> createUserSlugMap(String apiKey) {
        Map<String,String> userSlugMap = new HashMap<>();
        commanFunction function = new commanFunction();
        Response response = function.getUsers(baseURL, apiKey);
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        userSlugMap.put("userSlugOwner", user.get("[0].first_name")+"_"+user.get("[0].last_name"));
        userSlugMap.put("userSlugAdmin", user.get("[1].first_name")+"_"+user.get("[1].last_name"));
        userSlugMap.put("userSlugRestricted", user.get("[2].first_name")+"_"+user.get("[2].last_name"));
        userSlugMap.put("userSlugTeamMember", user.get("[3].first_name")+"_"+user.get("[3].last_name"));
        return userSlugMap;
    }

    public void processAssociatedEntityField(JSONObject payload, String fieldName, Map<String, String> associatedEntitiesSlugMap) {
        if (payload.has(fieldName)) {
            String fieldValue = payload.getString(fieldName);
            if (fieldValue.startsWith("{" + fieldName + "_")) {
                String entityKeys = fieldValue.replace("{", "").replace("}", "");
                String[] keys = entityKeys.split(",");
                List<String> entityValues = new ArrayList<>();

                for (String key : keys) {
                    String trimmedKey = key.trim();
                    String entityValue = associatedEntitiesSlugMap.get(trimmedKey);
                    if (entityValue != null) {
                        entityValues.add(entityValue);
                    }
                }

                if (!entityValues.isEmpty()) {
                    payload.put(fieldName, String.join(",", entityValues));
                } else {
                    payload.put(fieldName, "");
                }
            }
        }
    }


    public void processCollaboratorField(JSONObject payload, String fieldName, Map<String, String> entityMap) {
        if (payload.has(fieldName) && entityMap != null) {
            String fieldValue = payload.getString(fieldName);
            if (fieldValue.startsWith("{")) {
                String entityKeys = fieldValue.replace("{", "").replace("}", "");
                if (!entityKeys.isEmpty()) {
                    String[] keys = entityKeys.split(",");
                    List<String> entityValues = new ArrayList<>();

                    for (String key : keys) {
                        String trimmedKey = key.trim();
                        String entityValue = entityMap.get(trimmedKey);
                        if (entityValue != null) {
                            entityValues.add(entityValue);
                        }
                    }

                    if (!entityValues.isEmpty()) {
                        payload.put(fieldName, String.join(",", entityValues));
                    } else {
                        payload.put(fieldName, "");
                    }
                } else {
                    payload.put(fieldName, "");
                }
            }
        }
    }

    

    public void createAssociatedEntities(commanFunction function, String accountOwnerAPIKey, String albatrossAuthToken, Map<String, String> associatedEntitiesSlugMap, Map<String, Integer> associatedEntitiesIdMap) {
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

            // Contacts depend on companies
            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);

            // Jobs depend on company + contact
            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (companyJson1, contactJson1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson1.getString("slug"), contactJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (companyJson2, contactJson2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson2.getString("slug"), contactJson2.getString("slug")).jsonPath(), executor);

            // Deals depend on company + contact + job
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

            CompletableFuture.allOf(
                    candidateJson1Future, candidateJson2Future,
                    companyJson1Future, companyJson2Future,
                    contactJson1Future, contactJson2Future,
                    jobJson1Future, jobJson2Future,
                    dealJson1Future, dealJson2Future
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

            associatedEntitiesSlugMap.put("associated_candidates_candidate1", candidateJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_candidates_candidate2", candidateJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_companies_company1", companyJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_companies_company2", companyJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_contacts_contact1", contactJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_contacts_contact2", contactJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_jobs_job1", jobJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_jobs_job2", jobJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_deals_deal1", dealJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_deals_deal2", dealJson2.getString("slug"));

            associatedEntitiesIdMap.put("associated_candidates_candidate1", function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_candidates_candidate2", function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_companies_company1", function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_companies_company2", function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_contacts_contact1", function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_contacts_contact2", function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_jobs_job1", function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_jobs_job2", function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_deals_deal1", function.getDealIdBySlug(albatrossURL, albatrossAuthToken, dealJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_deals_deal2", function.getDealIdBySlug(albatrossURL, albatrossAuthToken, dealJson2.getString("slug")));
        } finally {
            executor.shutdown();
        }
    }


    public void updateActivityWithTimestampScenarios(List<Integer> activityIds, String activityType, Map<String, Map<String, String>> timestampScenarios) {
        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(timestampScenarios.entrySet());
        int limit = Math.min(activityIds.size(), scenarios.size());
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                final int idx = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    Integer activityId = activityIds.get(idx);
                    Map<String, String> timestamps = scenarios.get(idx).getValue();
                    JSONObject fieldsAndTimestamps = new JSONObject();
                    for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                        fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
                    }

                    Response updateResponse = ReaperIntegration.updateActivityTimestamp(activityId, fieldsAndTimestamps, activityType);
                    if (updateResponse.getStatusCode() != 200) {
                        Assert.fail("Failed to update the " + activityType + " timestamps for ID: " + activityId);
                    }
                }, executor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public JSONArray getFilteredData(Response response) {
        String responseBody = response.getBody().asString();
        JSONObject jsonObject = new JSONObject(responseBody);
        if (jsonObject.has("data") && !jsonObject.isNull("data")) {
            return jsonObject.getJSONArray("data");
        } else {
            // Return empty array if data field is missing or null (error response)
            return new JSONArray();
        }
    }

    public void validateNumberFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult, String entityName) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && data.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject entity = data.getJSONObject(i);
            String entityNumber = String.valueOf(entity.get(dbField)).trim();
            if (entityNumber.equals("null")) {
                if (filterType.equals("is_empty") || filterType.equals("is_not")) {
                    continue;
                }
                else {
                    Assert.fail("Entity number is null for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                }
            }

            
            if (entityNumber.endsWith(".0")) {
                entityNumber = entityNumber.substring(0, entityNumber.length() - 2);
            }
            
            // Handle string-based filter types first (no numeric parsing needed)
            if (filterType.equals("begins_with")) {
                Assert.assertTrue(entityNumber.startsWith(filterValue), entityName + " number for field: " + fieldName + " does not start with " + filterValue);
                continue;
            }
            else if (filterType.equals("ends_with")) {
                Assert.assertTrue(entityNumber.endsWith(filterValue), entityName + " number for field: " + fieldName + " does not end with " + filterValue);
                continue;
            }
            else if (filterType.equals("has_any_value")) {
                Assert.assertNotNull(entityNumber, entityName + " number for field: " + fieldName + " should not be null");
                Assert.assertFalse(entityNumber.trim().isEmpty(), entityName + " number for field: " + fieldName + " should not be empty");
                continue;
            }
            else if (filterType.equals("is_empty")) {
                Assert.assertTrue(entityNumber.trim().isEmpty() || entityNumber.equals("0"), entityName + " number for field: " + fieldName + " is not empty");
                continue;
            }
            
            // For numeric filter types, parse as double with error handling
            try {
                double entityNumberInt = Double.parseDouble(entityNumber);
                
                if (filterType.equals("is_between")) {
                    String[] rangeParts = filterValue.split(",");
                    if (rangeParts.length != 2) {
                        Assert.fail("Invalid range format for is_between filter. Expected 'start,end' but got: " + filterValue);
                    }
                    double startValue = Double.parseDouble(rangeParts[0].trim());
                    double endValue = Double.parseDouble(rangeParts[1].trim());
                    Assert.assertTrue(entityNumberInt >= startValue && entityNumberInt <= endValue, 
                        entityName + " number " + entityNumberInt + " for field: " + fieldName + " is not between " + startValue + " and " + endValue);
                    continue;
                }

                double filterValueInt = 0;
                if (!filterValue.isEmpty() && !filterValue.equals(" ")) {
                    filterValueInt = Double.parseDouble(filterValue);
                }
                
                if (filterType.equals("is")) {
                    Assert.assertEquals(filterValueInt, entityNumberInt, entityName + " number " + entityNumberInt + " for field: " + fieldName + " is not equal to " + filterValue);
                }
                else if (filterType.equals("is_not")) {
                    Assert.assertNotEquals(filterValueInt, entityNumberInt, entityName + " number " + entityNumberInt + " for field: " + fieldName + " is equal to " + filterValue + " (should not be equal)");
                }
                else if (filterType.equals("is_mt")) {
                    Assert.assertTrue(entityNumberInt > filterValueInt, entityName + " number " + entityNumberInt + " for field: " + fieldName + " is not greater than " + filterValue + " (expected > " + filterValueInt + ", but got " + entityNumberInt + ")");
                }
                else if (filterType.equals("is_lt")) {  
                    Assert.assertTrue(entityNumberInt < filterValueInt, entityName + " number " + entityNumberInt + " for field: " + fieldName + " is not less than " + filterValue + " (expected < " + filterValueInt + ", but got " + entityNumberInt + ")");
                }
            } catch (NumberFormatException e) {
                Assert.fail("Cannot parse number value '" + entityNumber + "' for field: " + fieldName + 
                    ", filterType: " + filterType + ", filterValue: " + filterValue + 
                    ". Expected numeric value but got: " + entityNumber + ". Error: " + e.getMessage());
            }
        }
    }

    public void validateTextFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult, String entityName, String... htmlFieldsToStrip) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && data.length() == 0){
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject entity = data.getJSONObject(i);
            String entityData = entity.optString(dbField, "").trim();
            String fv = filterValue == null ? "" : filterValue.trim();

            // Check if this field needs HTML tag stripping
            boolean needsHtmlStripping = false;
            for (String htmlField : htmlFieldsToStrip) {
                if (dbField.equals(htmlField)) {
                    needsHtmlStripping = true;
                    break;
                }
            }
            if (needsHtmlStripping) {
                entityData = stripHtmlTags(entityData);
                fv = stripHtmlTags(fv);
            }

            switch (filterType) {
                case "is":
                    if ("skill".equals(dbField)) {
                        Assert.assertEquals(normalizeCommaSeparatedText(entityData), normalizeCommaSeparatedText(fv),
                                "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv + ",");
                    } else {
                        Assert.assertEquals(entityData.toLowerCase(), fv.toLowerCase(), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv + ",");
                    }
                    break;
                case "is_not":
                    if ("skill".equals(dbField)) {
                        Assert.assertNotEquals(normalizeCommaSeparatedText(entityData), normalizeCommaSeparatedText(fv),
                                "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " — candidate skills should not equal filter after normalizing spaces: filter='" + fv + "' entity='" + entityData + "'");
                    } else {
                        Assert.assertNotEquals(entityData.toLowerCase(), fv.toLowerCase(),
                                "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    }
                    break;
                case "contains":
                    Assert.assertTrue(entityData.toLowerCase().contains(fv.toLowerCase()), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    break;
                case "starts_with":
                case "begins_with":
                    Assert.assertTrue(entityData.toLowerCase().startsWith(fv.toLowerCase()), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    break;
                case "ends_with":
                    Assert.assertTrue(entityData.toLowerCase().endsWith(fv.toLowerCase()), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    break;
                case "does_not_contain":
                    Assert.assertFalse(entityData.toLowerCase().contains(fv.toLowerCase()), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    break;
                case "contains_exact_word":
                    Assert.assertTrue(entityData.toLowerCase().contains(fv.toLowerCase()), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    break;
                case "contains_at_least_one":
                    String[] searchValues = fv.split(",");
                    boolean foundMatch = false;

                    for (String searchValue : searchValues) {
                        searchValue = searchValue.trim();
                        if (needsHtmlStripping) {
                            searchValue = stripHtmlTags(searchValue);
                        }
                        if (searchValue.isEmpty()) {
                            continue;
                        }
                        if (foldAccentsForFilterMatch(entityData).contains(foldAccentsForFilterMatch(searchValue))) {
                            foundMatch = true;
                            break;
                        }
                    }

                    Assert.assertTrue(foundMatch, "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv + ". Field value '" + entityData + "' should contain at least one of: " + fv);
                    break;
                case "has_any_value":
                    Assert.assertFalse(entityData.isEmpty() || entityData.equals("") || entityData.equals("null"), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    break;
                case "is_empty":
                    Assert.assertTrue(entityData.isEmpty() || entityData.equals("") || entityData.equals("null"), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + fv);
                    break;
            }
        }
    }


    public boolean validateTextFieldFilteredDataBoolean(JSONObject entity, String filterType, String filterValue, String fieldName, String dbField, String expectedResult, String entityName, String... htmlFieldsToStrip) {
        String entityData = entity.optString(dbField, "").trim();
        String fv = filterValue == null ? "" : filterValue.trim();

        boolean needsHtmlStripping = false;
        for (String htmlField : htmlFieldsToStrip) {
            if (dbField.equals(htmlField)) {
                needsHtmlStripping = true;
                break;
            }
        }
        if (needsHtmlStripping) {
            entityData = stripHtmlTags(entityData);
            fv = stripHtmlTags(fv);
        }

        boolean matches = false;
        switch (filterType) {
            case "is":
                if ("skill".equals(dbField)) {
                    matches = normalizeCommaSeparatedText(entityData).equals(normalizeCommaSeparatedText(fv));
                } else {
                    matches = entityData.toLowerCase().equals(fv.toLowerCase());
                }
                break;
            case "is_not":
                if ("skill".equals(dbField)) {
                    matches = !normalizeCommaSeparatedText(entityData).equals(normalizeCommaSeparatedText(fv));
                } else {
                    matches = !entityData.toLowerCase().equals(fv.toLowerCase());
                }
                break;
            case "contains":
                matches = entityData.toLowerCase().contains(fv.toLowerCase());
                break;
            case "does_not_contain":
                matches = !entityData.toLowerCase().contains(fv.toLowerCase());
                break;
            case "starts_with":
            case "begins_with":
                matches = entityData.toLowerCase().startsWith(fv.toLowerCase());
                break;
            case "ends_with":
                matches = entityData.toLowerCase().endsWith(fv.toLowerCase());
                break;
            case "contains_exact_word":
                matches = entityData.toLowerCase().contains(fv.toLowerCase());
                break;
            case "contains_at_least_one":
                String[] searchValues = fv.split(",");
                for (String searchValue : searchValues) {
                    searchValue = searchValue.trim();
                    if (needsHtmlStripping) {
                        searchValue = stripHtmlTags(searchValue);
                    }
                    if (searchValue.isEmpty()) {
                        continue;
                    }
                    if (foldAccentsForFilterMatch(entityData).contains(foldAccentsForFilterMatch(searchValue))) {
                        matches = true;
                        break;
                    }
                }
                break;
            case "has_any_value":
                matches = !entityData.isEmpty() && !entityData.equals("") && !entityData.equals("null");
                break;
            case "is_empty":
                matches = entityData.isEmpty() || entityData.equals("") || entityData.equals("null");
                break;
            default:
                matches = false;
                break;
        }
        
        return matches;
    }

    /**
     * Normalizes comma-separated text (e.g. skills) so comparisons ignore spaces around commas
     * and only differ by segment content. Segments are trimmed, empty segments dropped, then
     * joined and lowercased for equality checks.
     */
    private static String normalizeCommaSeparatedText(String value) {
        if (value == null) {
            return "";
        }
        String[] parts = value.split(",");
        List<String> segments = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                segments.add(t);
            }
        }
        return String.join(",", segments).toLowerCase();
    }

    /**
     * Lowercase with accent folding (NFD + strip combining marks) for substring-style text
     * filter checks so the validator matches API behavior across fields (e.g. "example" vs "éxample").
     */
    protected static String foldAccentsForFilterMatch(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private String stripHtmlTags(String htmlString) {
        if (htmlString == null || htmlString.isEmpty()) {
            return htmlString;
        }

        String plainText = htmlString.replaceAll("<[^>]+>", "");

        plainText = plainText.replace("&amp;", "&")
                           .replace("&lt;", "<")
                           .replace("&gt;", ">")
                           .replace("&quot;", "\"")
                           .replace("&#39;", "'")
                           .replace("&nbsp;", " ");
        
        return plainText.trim();
    }

    public void validateEntityDateField(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult, String entityName) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (data.isEmpty()) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject entity = data.getJSONObject(i);
            String entityDateStr = entity.optString(dbField, "").trim();

            switch (filterType) {
                case "is":
                case "is_equal_to":
                    validateEntityExactDateMatch(entityDateStr, filterValue, fieldName, filterType, entityName);
                    break;
                case "is_not":
                    validateEntityDateNotEqual(entityDateStr, filterValue, fieldName, filterType, entityName);
                    break;
                case "is_before":
                    validateEntityDateBefore(entityDateStr, filterValue, fieldName, filterType, entityName);
                    break;
                case "is_after":
                    validateEntityDateAfter(entityDateStr, filterValue, fieldName, filterType, entityName);
                    break;
                case "is_between":
                    validateEntityDateBetween(entityDateStr, filterValue, fieldName, filterType, entityName);
                    break;
                case "is_mt":
                    validateEntityDateMoreThanDaysAgo(entityDateStr, filterValue, fieldName, filterType, entityName);
                    break;
                case "is_lt":
                    validateEntityDateLessThanDaysAgo(entityDateStr, filterValue, fieldName, filterType, entityName);
                    break;
                case "has_any_value":
                    Assert.assertNotEquals(entityDateStr, "0", "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - expected non-empty date value");
                    break;
                case "is_empty":
                    Assert.assertTrue(entityDateStr.equals("0") || entityDateStr.isEmpty(), 
                        "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - expected empty date value (0 or empty string), but got: " + entityDateStr);
                    break;
                default:
                    Assert.fail("Unsupported filter type: " + filterType + " for field: " + fieldName);
            }
        }
    }

    private void validateEntityExactDateMatch(String entityDate, String filterValue, String fieldName, String filterType, String entityName) {
        if (entityDate.isEmpty()) {
            Assert.fail(entityName + " date is empty for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        LocalDate entityParsedDate = parseDate(entityDate);
        if (isRelativeDatePeriod(filterValue)) {
            validateEntityDateInPeriod(entityParsedDate, filterValue, fieldName, filterType, entityName);
        } else {
            LocalDate filterParsedDate = parseDate(filterValue);
            Assert.assertEquals(entityParsedDate, filterParsedDate, "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
    }

    private void validateEntityDateInPeriod(LocalDate entityDate, String period, String fieldName, String filterType, String entityName) {
        LocalDate startDate;
        LocalDate endDate;
        LocalDate now = LocalDate.now();

        switch (period) {
            case "all_time":
                Assert.assertNotNull(entityDate, entityName + " date should not be null for field: " + fieldName + " with all_time filter");
                return;
            case "today":
                startDate = endDate = now;
                break;
            case "yesterday":
                startDate = endDate = now.minusDays(1);
                break;
            case "this_week":
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                endDate = startDate.plusDays(6);
                break;
            case "last_week":
                LocalDate lastWeekStart = now.minusDays(now.getDayOfWeek().getValue() + 6);
                startDate = lastWeekStart;
                endDate = lastWeekStart.plusDays(6);
                break;
            case "this_month":
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "last_month":
                LocalDate lastMonth = now.minusMonths(1);
                startDate = lastMonth.withDayOfMonth(1);
                endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                break;
            case "this_quarter":
                int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                int quarterStartMonth = (currentQuarter - 1) * 3 + 1;
                startDate = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                endDate = now.withMonth(quarterStartMonth + 2).withDayOfMonth(now.withMonth(quarterStartMonth + 2).lengthOfMonth());
                break;
            case "last_quarter":
                int lastQuarter = (now.getMonthValue() - 1) / 3;
                if (lastQuarter == 0) {
                    lastQuarter = 4;
                    now = now.minusYears(1);
                }
                int lastQuarterStartMonth = (lastQuarter - 1) * 3 + 1;
                startDate = now.withMonth(lastQuarterStartMonth).withDayOfMonth(1);
                endDate = now.withMonth(lastQuarterStartMonth + 2).withDayOfMonth(now.withMonth(lastQuarterStartMonth + 2).lengthOfMonth());
                break;
            case "this_year":
                startDate = now.withDayOfYear(1);
                endDate = now.withDayOfYear(now.lengthOfYear());
                break;
            case "last_year":
                LocalDate lastYear = now.minusYears(1);
                startDate = lastYear.withDayOfYear(1);
                endDate = lastYear.withDayOfYear(lastYear.lengthOfYear());
                break;
            case "last_30":
                endDate = now;
                startDate = endDate.minusDays(30);
                break;
            case "last_60":
                endDate = now;
                startDate = endDate.minusDays(60);
                break;
            case "last_90":
                endDate = now;
                startDate = endDate.minusDays(90);
                break;
            case "last_365":
                endDate = now;
                startDate = endDate.minusDays(365);
                break;
            default:
                Assert.fail("Unsupported relative date period: " + period);
                return;
        }

        boolean isInPeriod = !entityDate.isBefore(startDate) && !entityDate.isAfter(endDate);
        Assert.assertTrue(isInPeriod,
                "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - " + entityName.toLowerCase() + " date " + entityDate +
                        " should be within period '" + period + "' (between " + startDate + " and " + endDate + ")");
    }

    private void validateEntityDateNotEqual(String entityDate, String filterValue, String fieldName, String filterType, String entityName) {
        if (entityDate.isEmpty()) {
            return;
        }

        LocalDate entityParsedDate = parseDate(entityDate);
        LocalDate filterParsedDate = parseDate(filterValue);

        Assert.assertNotEquals(entityParsedDate, filterParsedDate, "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
    }

    private void validateEntityDateBefore(String entityDate, String filterValue, String fieldName, String filterType, String entityName) {
        if (entityDate.isEmpty()) {
            Assert.fail(entityName + " date is empty for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        LocalDate entityParsedDate = parseDate(entityDate);
        LocalDate filterParsedDate = parseDate(filterValue);

        Assert.assertTrue(entityParsedDate.isBefore(filterParsedDate), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - " + entityName.toLowerCase() + " date " + entityParsedDate + " should be before " + filterValue);
    }

    private void validateEntityDateAfter(String entityDate, String filterValue, String fieldName, String filterType, String entityName) {
        if (entityDate.isEmpty()) {
            Assert.fail(entityName + " date is empty for field: " + fieldName +
                    " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        LocalDate entityParsedDate = parseDate(entityDate);
        LocalDate filterParsedDate = parseDate(filterValue);

        Assert.assertTrue(entityParsedDate.isAfter(filterParsedDate), "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - " + entityName.toLowerCase() + " date " + entityParsedDate + " should be after " + filterValue + " (strictly greater than, not inclusive)");
    }

    private void validateEntityDateBetween(String entityDate, String filterValue, String fieldName, String filterType, String entityName) {
        if (entityDate.isEmpty()) {
            Assert.fail(entityName + " date is empty for field: " + fieldName +
                    " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        String[] dates = filterValue.split(",");
        if (dates.length != 2) {
            Assert.fail("Invalid filter value for is_between: " + filterValue + ". Expected format: 'startDate,endDate'");
        }

        LocalDate entityParsedDate = parseDate(entityDate);
        LocalDate startDate = parseDate(dates[0].trim());
        LocalDate endDate = parseDate(dates[1].trim());

        Assert.assertTrue(
                (entityParsedDate.isEqual(startDate) || entityParsedDate.isAfter(startDate)) &&
                        (entityParsedDate.isEqual(endDate) || entityParsedDate.isBefore(endDate)), 
                "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - " + entityName.toLowerCase() + " date " + entityParsedDate + " should be between " + dates[0] + " and " + dates[1]);
    }

    private void validateEntityDateMoreThanDaysAgo(String entityDate, String daysStr, String fieldName, String filterType, String entityName) {
        if (entityDate.isEmpty()) {
            Assert.fail(entityName + " date is empty for field: " + fieldName +
                    " and filterType: " + filterType + " and filterValue: " + daysStr);
        }
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate entityParsedDate = parseDate(entityDate);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);

            Assert.assertTrue(entityParsedDate.isBefore(cutoffDate) || entityParsedDate.isEqual(cutoffDate),
                    "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - " + entityName.toLowerCase() + " date " + entityParsedDate +
                            " should be more than " + days + " days ago (on or before " + cutoffDate + ")");
        } catch (NumberFormatException e) {
            Assert.fail("Invalid days value for is_mt filter: " + daysStr);
        }
    }

    private void validateEntityDateLessThanDaysAgo(String entityDate, String daysStr, String fieldName, String filterType, String entityName) {
        if (entityDate.isEmpty()) {
            Assert.fail(entityName + " date is empty for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + daysStr);
        }
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate entityParsedDate = parseDate(entityDate);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);

            Assert.assertTrue(entityParsedDate.isAfter(cutoffDate) || entityParsedDate.isEqual(cutoffDate),
                    "Wrong " + entityName.toLowerCase() + " data for field: " + fieldName + " - " + entityName.toLowerCase() + " date " + entityParsedDate +
                            " should be less than " + days + " days ago (on or after " + cutoffDate + ")");
        } catch (NumberFormatException e) {
            Assert.fail("Invalid days value for is_lt filter: " + daysStr);
        }
    }

    public boolean validateTextAgainstFilter(String fieldValue, String filterType, String filterValue, String fieldName) {
        try {
            if (fieldValue == null) {
                fieldValue = "";
            }

            String trimmedFieldValue = fieldValue.trim();
            String trimmedFilterValue = filterValue.trim();

            switch (filterType) {
                case "is":
                    return trimmedFieldValue.equalsIgnoreCase(trimmedFilterValue);
                case "is_not":
                    return !trimmedFieldValue.equalsIgnoreCase(trimmedFilterValue);
                case "contains":
                    return foldAccentsForFilterMatch(trimmedFieldValue).contains(foldAccentsForFilterMatch(trimmedFilterValue));
                case "does_not_contain":
                    return !foldAccentsForFilterMatch(trimmedFieldValue).contains(foldAccentsForFilterMatch(trimmedFilterValue));
                case "begins_with":
                case "starts_with":
                    return foldAccentsForFilterMatch(trimmedFieldValue).startsWith(foldAccentsForFilterMatch(trimmedFilterValue));
                case "ends_with":
                    return foldAccentsForFilterMatch(trimmedFieldValue).endsWith(foldAccentsForFilterMatch(trimmedFilterValue));
                case "contains_exact_word":
                    return containsExactWord(fieldValue, filterValue);
                case "contains_at_least_one":
                    String[] values = filterValue.split(",");
                    for (String value : values) {
                        String token = value.trim();
                        if (!token.isEmpty()
                                && foldAccentsForFilterMatch(trimmedFieldValue).contains(foldAccentsForFilterMatch(token))) {
                            return true;
                        }
                    }
                    return false;
                case "has_any_value":
                    return !trimmedFieldValue.isEmpty();
                case "is_empty":
                    return trimmedFieldValue.isEmpty();
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsExactWord(String text, String word) {
        if (text == null || word == null) {
            return false;
        }

        String searchWord = foldAccentsForFilterMatch(word.trim());
        if (searchWord.isEmpty()) {
            return false;
        }

        String[] words = text.split("[\\s,]+");
        for (String w : words) {
            if (!w.isEmpty() && foldAccentsForFilterMatch(w).equals(searchWord)) {
                return true;
            }
        }
        return false;
    }


    public long dateToEpochSeconds(String dateStr) {
        LocalDate date = parseDate(dateStr);
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    public String formatEpochToDate(long epochSeconds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                .withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochSecond(epochSeconds));
    }

    public JSONObject stringStartEndFilterValue(String filterValue) {
        return FilterValueFactory.stringStartEndFilterValue(filterValue);
    }

    public JSONObject dateStartEndFilterValue(String filterValue) {
        return FilterValueFactory.dateStartEndFilterValue(filterValue, this::dateToEpochSeconds);
    }

    public JSONObject stringFilterValue(String filterValue) {
        return FilterValueFactory.stringFilterValue(filterValue);
    }

    public JSONObject dateIsFilterValue(String filterValue) {
        return FilterValueFactory.dateIsFilterValue(filterValue);
    }

    public JSONObject longFilterValue(String filterValue) {
        return FilterValueFactory.longFilterValue(filterValue, this::dateToEpochSeconds);
    }

    public JSONObject integerFilterValue(String filterValue) {
        return FilterValueFactory.integerFilterValue(filterValue);
    }

    public JSONObject integerListFilterValue(String filterValue) {
        return FilterValueFactory.integerListFilterValue(filterValue);
    }

    public JSONObject stringListFilterValue(String filterValue) {
        return FilterValueFactory.stringListFilterValue(filterValue);
    }

    public JSONObject stringListFilterValueAsIs(String filterValue) {
        return FilterValueFactory.stringListFilterValueAsIs(filterValue);
    }

    public JSONObject stringListFilterValueWithIgnore(String filterValue) {
        return FilterValueFactory.stringListFilterValueWithIgnore(filterValue);
    }

    public JSONObject emptyFilterValue(String filterValue_TYPE) {
        return FilterValueFactory.emptyFilterValue(filterValue_TYPE);
    }

    public JSONObject entityAssociationFilterValue(String filterValue) {
        return FilterValueFactory.entityAssociationFilterValue(filterValue);
    }

    public JSONObject integerStartEndFilterValue(String filterValue) {
        return FilterValueFactory.integerStartEndFilterValue(filterValue);
    }

    public JSONObject numericStringFilterValue(String filterValue) {
        return FilterValueFactory.numericStringFilterValue(filterValue);
    }

    public JSONObject numericStringStartEndFilterValue(String filterValue) {
        return FilterValueFactory.numericStringStartEndFilterValue(filterValue);
    }

    public JSONObject doubleFilterValue(String filterValue) {
        return FilterValueFactory.doubleFilterValue(filterValue);
    }

    public JSONObject doubleStartEndFilterValue(String filterValue) {
        return FilterValueFactory.doubleStartEndFilterValue(filterValue);
    }

    public void waitForDataSync() {
        if (isAriesSmokeRun()) {
            return;
        }
        FilterSearchTimingUtil.waitForFilterSearchDataSync();
    }

    public void waitForDataSyncBooleanSearch() {
        if (isAriesSmokeRun()) {
            waitForDataSync();
            return;
        }
        FilterSearchTimingUtil.waitForBooleanSearchDataSync();
    }

    private void logCompanyNameAndContact(Response response, JSONArray companyData, JSONArray contactData, String fieldName, String dbField) {
        FilterSearchReporter.logInfo("<b>📋 Company Name - Contact Information:</b>");

        if (companyData != null && contactData != null && companyData.length() == contactData.length()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
            logMessage.append("<code>");

            for (int i = 0; i < companyData.length(); i++) {
                JSONObject company = companyData.getJSONObject(i);
                JSONObject contact = contactData.getJSONObject(i);

                String companyName = company.optString("companyname", "N/A");
                String contactFieldValue = contact.optString(dbField, "N/A");

                logMessage.append("Record ").append(i + 1).append(":\n");
                logMessage.append("  Company Name: ").append(companyName).append("\n");
                logMessage.append("  Contact ").append(fieldName).append(" (").append(dbField).append("): ").append(contactFieldValue).append("\n\n");
            }

            logMessage.append("</code></pre>");
            FilterSearchReporter.logInfo(logMessage.toString());
        }
    }

    
}

