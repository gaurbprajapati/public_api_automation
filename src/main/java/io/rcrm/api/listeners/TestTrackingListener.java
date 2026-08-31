package io.rcrm.api.listeners;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.TestTrackingUtil;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * TestNG listener that reports test execution data to the reaper test tracking API.
 * Implements both ISuiteListener and ITestListener for build lifecycle and per-test reporting.
 * Requires {@code @Owner} annotation on every test method.
 */
public class TestTrackingListener implements ISuiteListener, ITestListener {

    private static final String TEST_TYPE = "API";

    /** When @Owner is missing or "unknown", pick a stable name from rotation (load spread). */
    private static final String[] FALLBACK_OWNERS = {
            "Raj Pandey", "Sampurn Chouksey", "Gaurav Prajapati", "Yash Rampal",
            "Sai Teja SG", "Smit Patel", "Akshaya Uppala",
            "Ajendra Singh", "Harika", "Rahul Shibu"
    };

    /** Contract staffing / VMS tests without @Owner. */
    private static final String[] FALLBACK_CONTRACT_STAFFING = {
            "Gaurav Prajapati", "Yash Rampal"
    };
    private static final String[] ENV_BUILD_NUMBER_CANDIDATES = {
            "CI_BUILD_NUMBER",  // Custom CI convention
            "BUILD_NUMBER",    // Jenkins
            "GITHUB_RUN_NUMBER", // GitHub Actions
            "CI_PIPELINE_ID",  // GitLab
            "TRAVIS_BUILD_NUMBER" // Travis CI
    };
    private static final String ENV_TEST_ENV = System.getProperty("envname");

    @Override
    public void onStart(ISuite suite) {
        if (!TestTrackingUtil.isTrackingEnabled()) {
            return;
        }
        String buildNumber = resolveBuildNumber();
        String ciUrl = resolveCiUrl();
        String environment = ENV_TEST_ENV;
        String triggeredBy = resolveTriggeredBy();
        String suiteType = resolveSuiteType(suite);
        String id = TestTrackingUtil.startBuild(buildNumber, ciUrl, environment, triggeredBy, suiteType);
        if (id != null) {
            TestTrackingUtil.setBuildId(id);
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        if (!TestTrackingUtil.isTrackingEnabled()) {
            return;
        }
        String id = TestTrackingUtil.getBuildId();
        if (id != null) {
            TestTrackingUtil.finalizeBuild(id);
        }
    }

    @Override
    public void onStart(ITestContext context) {
        // No-op; suite-level handling is in ISuiteListener.onStart(ISuite)
    }

    @Override
    public void onFinish(ITestContext context) {
        // No-op; suite-level handling is in ISuiteListener.onFinish(ISuite)
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testKey = getTestKey(result);
        TestTrackingUtil.recordTestStart(testKey, System.currentTimeMillis());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        reportTestResult(result, "PASSED", null, null);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String failureType = null;
        String stackTrace = null;
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            failureType = throwable.getClass().getName();
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
        }
        reportTestResult(result, "FAILED", failureType, stackTrace);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reportTestResult(result, "SKIPPED", null, null);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String failureType = null;
        String stackTrace = null;
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            failureType = throwable.getClass().getName();
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
        }
        reportTestResult(result, "FAILED", failureType, stackTrace);
    }

    private void reportTestResult(ITestResult result, String status,
                                  String failureType, String stackTrace) {
        if (!TestTrackingUtil.isTrackingEnabled()) {
            return;
        }

        String testKey = getTestKey(result);
        Long startTime = TestTrackingUtil.getAndRemoveTestStartTime(testKey);
        long endTime = System.currentTimeMillis();
        if (startTime == null) {
            startTime = result.getStartMillis();
        }

        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String methodName = result.getMethod().getMethodName();
        String testName = TestTrackingUtil.formatTestName(methodName, result);
        String testClass = result.getTestClass() != null
                ? result.getTestClass().getName()
                : "";

        String owner = extractOwner(result, method);
        if (owner == null || owner.trim().isEmpty() || "unknown".equalsIgnoreCase(owner.trim())) {
            String key = testClass + "#" + methodName;
            String[] pool = isContractStaffingOrVmsTest(testClass) ? FALLBACK_CONTRACT_STAFFING : FALLBACK_OWNERS;
            int idx = Math.floorMod(key.hashCode(), pool.length);
            owner = pool[idx];
        }

        String buildId = TestTrackingUtil.getBuildId();
        TestTrackingUtil.recordTestResult(
                buildId,
                testName,
                testClass,
                TEST_TYPE,
                owner,
                status,
                startTime,
                endTime,
                failureType,
                stackTrace,
                null  // not required for API :P
        );
    }

    private String extractOwner(ITestResult result, Method method) {
        if (method == null) {
            return null;
        }
        Owner ownerAnnotation = method.getAnnotation(Owner.class);
        if (ownerAnnotation != null) {
            return ownerAnnotation.value();
        }
        return null;
    }

    private static boolean isContractStaffingOrVmsTest(String testClass) {
        if (testClass == null || testClass.isEmpty()) {
            return false;
        }
        String l = testClass.toLowerCase(Locale.ROOT);
        return l.contains("contractstaffing") || l.contains(".vms.");
    }

    private String getTestKey(ITestResult result) {
        String cls = result.getTestClass() != null ? result.getTestClass().getName() : "";
        String method = result.getMethod().getMethodName();
        String testName = TestTrackingUtil.formatTestName(method, result);
        Object instance = result.getInstance();
        int hash = instance != null ? System.identityHashCode(instance) : 0;
        return cls + "#" + testName + "#" + hash;
    }

    /**
     * Resolves build number from CI environment variables, or generates a local identifier
     * when running outside CI (e.g. locally).
     */
    private static String resolveBuildNumber() {
        for (String envVar : ENV_BUILD_NUMBER_CANDIDATES) {
            String value = System.getenv(envVar);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "local-" + System.currentTimeMillis();
    }

    /**
     * CI dashboard URL for this run. Custom {@code CI_URL} wins; otherwise common CI defaults
     * (Jenkins {@code BUILD_URL}, GitLab {@code CI_PIPELINE_URL}, etc.).
     */
    private static String resolveCiUrl() {
        String v = firstNonEmptyEnv(
                "CI_URL",
                "BUILD_URL",           // Jenkins: link to this build
                "RUN_DISPLAY_URL",     // Jenkins Pipeline (e.g. Blue Ocean)
                "CI_PIPELINE_URL");    // GitLab CI
        return v;
    }

    /**
     * Who triggered the run, sourced from {@code BUILD_USER}.
     */
    private static String resolveTriggeredBy() {
        return System.getenv("BUILD_USER");
    }

    private static String firstNonEmptyEnv(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Resolves suite_type for build tracking. Records which Maven profile was executed
     * or which TestNG group invoked the tests.
     * Priority: 1) -Dsuite_type override, 2) suite_type from pom (Maven profile), 3) -DtestGroup,
     * 4) suite name from XML.
     */
    private static String resolveSuiteType(ISuite suite) {
        String explicit = System.getProperty("suite_type");
        if (explicit != null && !explicit.isEmpty()) {
            return explicit;
        }
        String testGroup = System.getProperty("testGroup");
        if (testGroup != null && !testGroup.isEmpty()) {
            return "groups:" + testGroup;
        }
        if (suite != null) {
            String suiteName = suite.getName();
            if (suiteName != null && !suiteName.isEmpty()) {
                return suiteName;
            }
        }
        return "";
    }
}
