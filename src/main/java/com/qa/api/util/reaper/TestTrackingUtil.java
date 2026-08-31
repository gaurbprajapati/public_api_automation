package com.qa.api.util.reaper;

import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for sending test execution data to the reaper test tracking API.
 * Uses the same base URL as {@link ReaperIntegration} for account creation.
 */
public final class TestTrackingUtil {

    private static final String PATH_BUILD_START = "api/test-updates/build/start";
    private static final String TEST_SUITE = "API-Suite";
    private static final String PATH_TEST_RESULT = "api/test-updates/test/result";
    private static final String PATH_BUILD_FINALIZE = "api/test-updates/build/finalize";

    private static volatile String buildId;

    private static final ConcurrentHashMap<String, Long> testStartTimes = new ConcurrentHashMap<>();

    private TestTrackingUtil() {
    }

    /**
     * Returns the reaper base URL (same as used for account creation).
     */
    public static String getBaseUrl() {
        return ReaperIntegration.REAPER_BASE_URL.replaceAll("/+$", "");
    }

    /**
     * Checks if tracking is enabled. Only enabled when running on Jenkins/Linux.
     */
    public static boolean isTrackingEnabled() {
        boolean isJenkins = System.getenv("JENKINS_HOME") != null || System.getenv("JENKINS_URL") != null;
        boolean isLinux = System.getProperty("os.name", "").toLowerCase().contains("linux");
        return isJenkins && isLinux;
    }

    /**
     * Stores the current build ID (set by {@code startBuild}).
     */
    public static void setBuildId(String id) {
        buildId = id;
    }

    /**
     * Returns the current build ID.
     */
    public static String getBuildId() {
        return buildId;
    }

    /**
     * Records the start time for a test (used by listener).
     */
    public static void recordTestStart(String testKey, long startTime) {
        testStartTimes.put(testKey, startTime);
    }

    /**
     * Returns and removes the start time for a test.
     */
    public static Long getAndRemoveTestStartTime(String testKey) {
        return testStartTimes.remove(testKey);
    }

    /**
     * Starts a build by calling POST /api/test-updates/build/start.
     * Expects response body to contain {@code buildId}.
     *
     * @param buildNumber  CI build number (e.g. from CI_BUILD_NUMBER)
     * @param ciUrl       CI URL (e.g. from CI_URL)
     * @param environment Test environment (e.g. from TEST_ENV)
     * @param triggeredBy Build trigger (e.g. from BUILD_TRIGGER)
     * @param suiteType   Maven profile or TestNG group (e.g. candidate, groups:company_service)
     * @return buildId from response, or null if tracking is disabled or request fails
     */
    public static String startBuild(String buildNumber, String ciUrl, String environment, String triggeredBy,
                                    String suiteType) {
        String base = getBaseUrl();
        if (base == null) {
            return null;
        }
        JSONObject body = new JSONObject();
        body.put("buildNumber", nullToEmpty(buildNumber));
        body.put("ciUrl", nullToEmpty(ciUrl));
        body.put("environment", nullToEmpty(environment));
        body.put("triggeredBy", nullToEmpty(triggeredBy));
        body.put("suite_type", nullToEmpty(suiteType));
        body.put("test_suite", TEST_SUITE);
        String baseUri = ReaperIntegration.REAPER_BASE_URL;
        try {
            String responseBody = doPost(baseUri, PATH_BUILD_START, body);
            if (responseBody != null && !responseBody.isEmpty()) {
                JSONObject json = new JSONObject(responseBody);
                if (json.has("buildId")) {
                    buildId = String.valueOf(json.get("buildId"));
                    return buildId;
                }
                if (json.has("id")) {
                    buildId = String.valueOf(json.get("id"));
                    return buildId;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Returns the test name for tracking. Data-provider iterations use a stable label per row:
     * {@code methodName[DataProvider Value1]}, {@code methodName[DataProvider Value2]}, etc.
     * (1-based index from TestNG {@link TestResult#getParameterIndex()}.)
     */
    public static String formatTestName(String methodName, ITestResult result) {
        if (result == null) {
            return methodName;
        }
        Object[] parameters = result.getParameters();
        if (parameters == null || parameters.length == 0) {
            return methodName;
        }
        int rowIndex = dataProviderRowIndex(result);
        return methodName + "[DataProvider Value" + (rowIndex + 1) + "]";
    }

    /** TestNG sets this per data-provider invocation (incl. retries); non-{@link TestResult} → 0. */
    private static int dataProviderRowIndex(ITestResult result) {
        if (result instanceof TestResult) {
            return ((TestResult) result).getParameterIndex();
        }
        return 0;
    }

    /**
     * Records a test result by calling POST /api/test-updates/test/result.
     */
    public static void recordTestResult(String buildIdParam, String testName, String testClass, String testType,
                                        String owner, String status, long startTime, long endTime,
                                        String failureType, String stackTrace, String screenshot) {
        String base = getBaseUrl();
        if (base == null) {
            return;
        }
        String effectiveBuildId = buildIdParam != null ? buildIdParam : buildId;
        if (effectiveBuildId == null || effectiveBuildId.isEmpty()) {
            return;
        }
        JSONObject body = new JSONObject();
        body.put("buildId", effectiveBuildId);
        body.put("testName", nullToEmpty(testName));
        body.put("testClass", nullToEmpty(testClass));
        body.put("testType", nullToEmpty(testType));
        body.put("owner", nullToEmpty(owner));
        body.put("status", nullToEmpty(status));
        body.put("startTime", startTime);
        body.put("endTime", endTime);
        body.put("failureType", failureType);
        body.put("stackTrace", stackTrace);
        body.put("screenshot", screenshot);
        try {
            doPost(ReaperIntegration.REAPER_BASE_URL, PATH_TEST_RESULT, body);
        } catch (Exception e) {
            // Suppress - tracking failures should not affect tests
        }
    }

    /**
     * Finalizes the build by calling POST /api/test-updates/build/finalize.
     */
    public static void finalizeBuild(String buildIdParam) {
        String base = getBaseUrl();
        if (base == null) {
            return;
        }
        String effectiveBuildId = buildIdParam != null ? buildIdParam : buildId;
        if (effectiveBuildId == null || effectiveBuildId.isEmpty()) {
            return;
        }
        JSONObject body = new JSONObject();
        body.put("buildId", effectiveBuildId);
        try {
            doPost(ReaperIntegration.REAPER_BASE_URL, PATH_BUILD_FINALIZE, body);
        } catch (Exception ignored) {
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * POSTs JSON body to the given base URI and path using RestClient.
     *
     * @param baseUri  base URL (e.g. http://localhost:80/)
     * @param basePath path (e.g. api/test-updates/build/start)
     * @param body     JSON request body
     * @return response body as string on success (2xx)
     * @throws RuntimeException if request fails or returns non-2xx
     */
    private static String doPost(String baseUri, String basePath, JSONObject body) {
        Response response = RestClient.doPost("JSON", baseUri, basePath,
                ReaperIntegration.authTokenMap, null, false, body);
        if (response == null) {
            throw new RuntimeException("RestClient.doPost returned null");
        }
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();
        if (statusCode >= 200 && statusCode < 300) {
            return responseBody;
        }
        throw new RuntimeException("HTTP " + statusCode + ": " + responseBody);
    }
}
