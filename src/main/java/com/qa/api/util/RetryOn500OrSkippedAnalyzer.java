package com.qa.api.util;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import io.restassured.response.Response;

public class RetryOn500OrSkippedAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int maxRetryCount = 1;

    @Override
    public boolean retry(ITestResult result) {
        Object testInstance = result.getInstance();

        try {
            // Check if we should retry based on the error message first
            Throwable throwable = result.getThrowable();
            if (throwable != null && retryCount < maxRetryCount) {
                String errorMessage = throwable.getMessage();
                if (errorMessage != null) {
                    boolean shouldRetry = false;
                    
                    // Check for webhook errors
                    if (errorMessage.contains("Failed to fetch Webhook data") || 
                        errorMessage.contains("Webhook data") ||
                        errorMessage.contains("optedin") ||
                        errorMessage.contains("optedout")) {
                        shouldRetry = true;
                    }
                    
                    // Check for Reaper errors - more robust detection
                    if (errorMessage.contains("Failed to get account from Reaper")) {
                        shouldRetry = true;
                    }
                    
                    if (shouldRetry) {
                        System.out.println("Error detected: " + errorMessage + " - Retrying... Retry count: " + (retryCount + 1));
                        retryCount++;
                        return true;
                    }
                }
            }

            // Case 1: Skipped tests - only retry if they have specific error messages
            if (result.getStatus() == ITestResult.SKIP && retryCount < maxRetryCount) {
                // For skipped tests, check if there's a throwable with specific error messages
                if (throwable != null) {
                    String errorMessage = throwable.getMessage();
                    if (errorMessage != null && (
                        errorMessage.contains("Failed to get account from Reaper") ||
                        errorMessage.contains("Failed to fetch Webhook data") ||
                        errorMessage.contains("Webhook data") ||
                        errorMessage.contains("optedin") ||
                        errorMessage.contains("optedout"))) {
                        System.out.println("Test skipped with retryable error: " + errorMessage + " - Retrying... Retry count: " + (retryCount + 1));
                        retryCount++;
                        return true;
                    }
                }
            }

            // Case 2: Failed due to HTTP 500
            Response response = (Response) testInstance.getClass().getField("response").get(testInstance);
            if (response != null && response.statusCode() == 500 && retryCount < maxRetryCount) {
                System.out.println("HTTP 500 error detected, retrying... Retry count: " + (retryCount + 1));
                retryCount++;
                return true;
            }

        } catch (Exception e) {
            System.out.println("Exception in retry analyzer: " + e.getMessage());
        }

        return false;
    }
}
