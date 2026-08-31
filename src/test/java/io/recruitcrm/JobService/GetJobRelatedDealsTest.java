package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetJobRelatedDealsTest extends TestBase {

    String accountApiKey;
    String albatrossTkn;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    @BeforeClass
    public void setUp() {
        accountApiKey = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "jobRelatedDealsTestData", groups = {"job_service", "nightly-build"})
    public void testGetJobRelatedDealsSuccess(String jobSlug, int dealId, String jobName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", jobName);

        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        JSONObject sort2 = new JSONObject();
        sort2.put("field", "dealvalue");
        sort2.put("order", "asc");
        sortOrder.put(sort1);
        sortOrder.put(sort2);
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("jobSlug", jobSlug);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Related deals fetched successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Deals array should not be null", jp.get("data.deals"), notNullValue());

        if (jp.get("data.deals.size()") != null && (Integer) jp.get("data.deals.size()") > 0) {
            assertThat("Deal ID should not be null", jp.get("data.deals[0].id"), notNullValue());
            assertThat("Deal name should not be null", jp.get("data.deals[0].name"), notNullValue());
            assertThat("Deal value should not be null", jp.get("data.deals[0].dealvalue"), notNullValue());
            if (jp.get("data.deals[0].id") != null) {
                assertThat("Deal ID should be positive", jp.get("data.deals[0].id"), greaterThan(0));
            }
            if (jp.get("data.deals[0].dealvalue") != null) {
                assertThat("Deal value should be non-negative", jp.get("data.deals[0].dealvalue"), greaterThanOrEqualTo(0.0f));
            }
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/job/jobRelatedDeals.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "jobRelatedDealsTestData", groups = {"job_service", "nightly-build"})
    public void testGetJobRelatedDealsWithoutAuth(String jobSlug, int dealId, String jobName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", jobName);

        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("jobSlug", jobSlug);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
                "", queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Meta message should be Unauthorised access", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Meta responseType context should be Warning", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("Data should indicate missing bearer token", jp.get("data"), equalTo("Missing bearer token in header"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "jobRelatedDealsTestData", groups = {"job_service", "nightly-build"})
    public void testGetJobRelatedDealsInvalidAuth(String jobSlug, int dealId, String jobName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");
        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", jobName);
        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("jobSlug", jobSlug);
        Response response = RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
                albatrossTkn + "invalid_token", queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobRelatedDealsInvalidJobSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "TestJob");

        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("jobSlug", "invalid-job-slug-12345");

        Response response = RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Errors should not be null", jp.get("errors"), notNullValue());
        assertThat("Error message should contain job not found", jp.get("errors[0].message"), containsString("Job not found for slug"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "jobRelatedDealsTestData", groups = {"job_service", "nightly-build"})
    public void testGetJobRelatedDealsEmptyRequestBody(String jobSlug, int dealId, String jobName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("jobSlug", jobSlug);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
                albatrossTkn, queryParams, pathParams, true, null);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "jobRelatedDealsSearchTestData", groups = {"job_service", "nightly-build"})
    public void testGetJobRelatedDealsSearch(String jobSlug, String jobName, String searchTerm, String searchType) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", searchTerm);

        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("jobSlug", jobSlug);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for searchType: " + searchType,
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Related deals fetched successfully"));
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Deals array should not be null", jp.get("data.deals"), notNullValue());

        int dealsSize = jp.get("data.deals.size()");
        switch (searchType) {
            case "exact_match":
                assertThat("Exact match search should return at least one deal", dealsSize, greaterThanOrEqualTo(1));
                break;
            case "partial_match":
                assertThat("Partial match search should return at least one deal", dealsSize, greaterThanOrEqualTo(1));
                break;
            case "case_insensitive":
                assertThat("Case insensitive search should return at least one deal", dealsSize, greaterThanOrEqualTo(1));
                break;
            case "no_results":
                assertThat("No results search should return empty deals array", dealsSize, equalTo(0));
                break;
            default:
                assertThat("Deals array should not be null for search type: " + searchType, jp.get("data.deals"), notNullValue());
                assertThat("Deals size should be non-negative for search type: " + searchType, dealsSize, greaterThanOrEqualTo(0));
                break;
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/job/jobRelatedDeals.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "jobRelatedDealsSortOrderTestData", groups = {"job_service", "nightly-build"})
    public void testGetJobRelatedDealsWithSortOrder(String jobSlug, String jobName, String sortOrderName, JSONArray sortOrder) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", jobName);
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("jobSlug", jobSlug);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "jobs/{jobSlug}/related-deals",
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for sortOrder: " + sortOrderName,
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Deals array should not be null", jp.get("data.deals"), notNullValue());
        int dealsSize = jp.get("data.deals.size()");
        assertThat("Should have at least 2 deals to verify sorting for " + sortOrderName, dealsSize, greaterThanOrEqualTo(2));
        verifySortOrder(jp, sortOrder, dealsSize, sortOrderName);
    }

    private void verifySortOrder(JsonPath jp, JSONArray sortOrder, int dealsSize, String sortOrderName) {
        if (sortOrder.length() == 0) return;

        JSONObject firstSort = sortOrder.getJSONObject(0);
        String field = firstSort.getString("field");
        String order = firstSort.getString("order");

        for (int i = 0; i < dealsSize - 1; i++) {
            Object currentValueObj = jp.get("data.deals[" + i + "]." + field);
            Object nextValueObj = jp.get("data.deals[" + (i + 1) + "]." + field);

            if (currentValueObj != null && nextValueObj != null) {
                if (currentValueObj instanceof Number && nextValueObj instanceof Number) {
                    double currentNum = ((Number) currentValueObj).doubleValue();
                    double nextNum = ((Number) nextValueObj).doubleValue();
                    if (order.equalsIgnoreCase("asc")) {
                        assertThat("Sort order should be ascending for numeric field: " + field + " in " + sortOrderName,
                                currentNum, lessThanOrEqualTo(nextNum));
                    } else if (order.equalsIgnoreCase("desc")) {
                        assertThat("Sort order should be descending for numeric field: " + field + " in " + sortOrderName,
                                currentNum, greaterThanOrEqualTo(nextNum));
                    }
                } else {
                    String currentValue = String.valueOf(currentValueObj);
                    String nextValue = String.valueOf(nextValueObj);
                    if (order.equalsIgnoreCase("asc")) {
                        assertThat("Sort order should be ascending for field: " + field + " in " + sortOrderName,
                                currentValue.compareToIgnoreCase(nextValue), lessThanOrEqualTo(0));
                    } else if (order.equalsIgnoreCase("desc")) {
                        assertThat("Sort order should be descending for field: " + field + " in " + sortOrderName,
                                currentValue.compareToIgnoreCase(nextValue), greaterThanOrEqualTo(0));
                    }
                }
            }
        }
    }

    @DataProvider(name = "jobRelatedDealsSortOrderTestData")
    public Object[][] getJobRelatedDealsSortOrderTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
                assertThat("Failed to create test company", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);

            CompletableFuture<Response> candidateFuture = CompletableFuture.supplyAsync(() -> {
                Response response = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
                assertThat("Failed to create test candidate", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);

            Response companyResponse = companyFuture.join();
            Response candidateResponse = candidateFuture.join();

            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");

            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");

            Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
            assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
            JsonPath contactJp = contactResponse.jsonPath();
            String contactSlug = contactJp.get("slug");

            Response jobResponse = function.createNewJob(baseURL, accountApiKey, companySlug, contactSlug);
            assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
            JsonPath jobJp = jobResponse.jsonPath();
            String jobSlug = jobJp.get("slug");
            final String jobDisplayName = (jobJp.get("job_title") != null && !jobJp.get("job_title").toString().isEmpty())
                    ? jobJp.get("job_title").toString() : "Job " + jobSlug;

            Integer ownerUserId = function.getOwnerUserId(baseURL, accountApiKey);
            CompletableFuture<Response> deal1Future = CompletableFuture.supplyAsync(() -> {
                Deal deal1 = new Deal();
                deal1.setName("Deal A - " + jobDisplayName);
                deal1.setDeal_value(5000);
                deal1.setClose_date("2025-06-30");
                deal1.setDeal_stage("1");
                deal1.setDeal_type("1");
                deal1.setCompany_slug(companySlug);
                deal1.setJob_slug(jobSlug);
                deal1.setContact_slugs(contactSlug);
                deal1.setCandidate_slug(candidateSlug);
                if (ownerUserId != null) {
                    deal1.setCreated_by(ownerUserId);
                    deal1.setOwner_id(String.valueOf(ownerUserId));
                }
                Response response = RestClient.doPost("JSON", baseURL, "deals", accountApiKey, null, true, deal1);
                assertThat("Failed to create first test deal", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);

            CompletableFuture<Response> deal2Future = CompletableFuture.supplyAsync(() -> {
                Deal deal2 = new Deal();
                deal2.setName("Deal B - " + jobDisplayName);
                deal2.setDeal_value(15000);
                deal2.setClose_date("2025-12-31");
                deal2.setDeal_stage("2");
                deal2.setDeal_type("1");
                deal2.setCompany_slug(companySlug);
                deal2.setJob_slug(jobSlug);
                deal2.setContact_slugs(contactSlug);
                deal2.setCandidate_slug(candidateSlug);
                if (ownerUserId != null) {
                    deal2.setCreated_by(ownerUserId);
                    deal2.setOwner_id(String.valueOf(ownerUserId));
                }
                Response response = RestClient.doPost("JSON", baseURL, "deals", accountApiKey, null, true, deal2);
                assertThat("Failed to create second test deal", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);

            deal1Future.join();
            deal2Future.join();

            JSONObject sortOrderJson = readJsonFileFromPath("src/test/resources/privateApi/job/jobRelatedDealsSortOrder.json");
            JSONArray configurations = sortOrderJson.getJSONArray("sortOrderConfigurations");

            Object[][] testData = new Object[configurations.length()][4];
            for (int i = 0; i < configurations.length(); i++) {
                JSONObject config = configurations.getJSONObject(i);
                String sortOrderName = config.getString("name");
                JSONArray sortOrder = config.getJSONArray("sortOrder");
                testData[i][0] = jobSlug;
                testData[i][1] = jobDisplayName;
                testData[i][2] = sortOrderName;
                testData[i][3] = sortOrder;
            }
            return testData;
        } finally {
            executor.shutdown();
        }
    }

    @DataProvider(name = "jobRelatedDealsSearchTestData")
    public Object[][] getJobRelatedDealsSearchTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
            assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            String companyName = companyJp.get("company_name");

            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");

            Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
            assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
            JsonPath contactJp = contactResponse.jsonPath();
            String contactSlug = contactJp.get("slug");

            Response jobResponse = function.createNewJob(baseURL, accountApiKey, companySlug, contactSlug);
            assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
            JsonPath jobJp = jobResponse.jsonPath();
            String jobSlug = jobJp.get("slug");
            String jobName = jobJp.get("job_title");
            if (jobName == null) jobName = "SearchTestJob";

            Deal deal = new Deal();
            deal.setName("SearchTest Deal - " + companyName + " " + jobName);
            deal.setDeal_value(10000);
            deal.setClose_date("2025-12-31");
            deal.setDeal_stage("1");
            deal.setDeal_type("1");
            deal.setCompany_slug(companySlug);
            deal.setJob_slug(jobSlug);
            deal.setContact_slugs(contactSlug);
            deal.setCandidate_slug(candidateSlug);
            Integer ownerUserId = function.getOwnerUserId(baseURL, accountApiKey);
            if (ownerUserId != null) {
                deal.setCreated_by(ownerUserId);
                deal.setOwner_id(String.valueOf(ownerUserId));
            }
            Response dealResponse = RestClient.doPost("JSON", baseURL, "deals", accountApiKey, null, true, deal);
            assertThat("Failed to create test deal", dealResponse.getStatusCode(), equalTo(200));

            return new Object[][]{
                    {jobSlug, jobName, jobName, "exact_match"},
                    {jobSlug, jobName, jobName.length() > 3 ? jobName.substring(0, 3) : jobName, "partial_match"},
                    {jobSlug, jobName, jobName.toUpperCase(), "case_insensitive"},
                    {jobSlug, jobName, "NonExistentJobName12345", "no_results"}
            };
        } finally {
            executor.shutdown();
        }
    }

    @DataProvider(name = "jobRelatedDealsTestData")
    public Object[][] getJobRelatedDealsTestData() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        String companyName = companyJp.get("company_name");

        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        String candidateSlug = candidateJp.get("data.candidate.slug");

        Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");

        Response jobResponse = function.createNewJob(baseURL, accountApiKey, companySlug, contactSlug);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");
        String jobName = jobJp.get("job_title");
        if (jobName == null) jobName = "Test Job";

        Deal deal = new Deal();
        deal.setName("Test Deal for " + companyName + " Job " + jobName);
        deal.setDeal_value(10000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setCompany_slug(companySlug);
        deal.setJob_slug(jobSlug);
        deal.setContact_slugs(contactSlug);
        deal.setCandidate_slug(candidateSlug);
        Integer ownerUserId = function.getOwnerUserId(baseURL, accountApiKey);
        if (ownerUserId != null) {
            deal.setCreated_by(ownerUserId);
            deal.setOwner_id(String.valueOf(ownerUserId));
        }

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals", accountApiKey, null, true, deal);
        assertThat("Failed to create test deal", dealResponse.getStatusCode(), equalTo(200));
        JsonPath dealJp = dealResponse.jsonPath();
        int dealId = dealJp.get("id");
        assertThat("Deal ID should not be null", dealId, notNullValue());

        return new Object[][]{{jobSlug, dealId, jobName}};
    }
}
