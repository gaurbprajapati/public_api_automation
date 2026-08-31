package io.rcrm.api.util;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;

public class GenericRetryAnalyzer implements IRetryAnalyzer {

    private static final int RETRY_LIMIT = 1;
    private int counter = 0;
    private ITestNGMethod lastMethod;

    @Override
    public boolean retry(ITestResult result) {
        // Reset counter when we move to a different test method (same instance can be reused across methods)
        if (lastMethod != result.getMethod()) {
            lastMethod = result.getMethod();
            counter = 0;
        }
        int status = result.getStatus();
        // Retry if the status is FAILURE or SKIP
        boolean isRetryable = (status == ITestResult.FAILURE || status == ITestResult.SKIP);
        if (isRetryable && counter < RETRY_LIMIT) {
            counter++;
            return true;
        }
        return false;
    }
}