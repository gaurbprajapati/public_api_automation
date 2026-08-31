package com.qa.api.util;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class WebhookRetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int maxRetryCount = 2; // Allow more retries for webhook tests

    @Override
    public boolean retry(ITestResult result) {
        try {
            // Case 1: Skipped tests
            if (result.getStatus() == ITestResult.SKIP && retryCount < maxRetryCount) {
                System.out.println("Webhook test skipped, retrying... Retry count: " + (retryCount + 1));
                retryCount++;
                return true;
            }

            // Case 2: Failed due to AssertionError related to webhook data fetching or Reaper account errors
            Throwable throwable = result.getThrowable();
            if (throwable instanceof AssertionError && retryCount < maxRetryCount) {
                String errorMessage = throwable.getMessage();
                if (errorMessage != null) {
                    boolean shouldRetry = false;
                    
                    // Check for webhook errors
                    if (errorMessage.contains("Failed to fetch Webhook data") || 
                        errorMessage.contains("Webhook data") ||
                        errorMessage.contains("optedin") ||
                        errorMessage.contains("optedout") ||
                        errorMessage.contains("Webhook Data")) {
                        shouldRetry = true;
                    }
                    
                    // Check for Reaper errors - more robust detection
                    if (errorMessage.contains("Failed to get account from Reaper")) {
                        shouldRetry = true;
                    }
                    
                    if (shouldRetry) {
                        System.out.println("AssertionError detected: " + errorMessage + " - Retrying... Retry count: " + (retryCount + 1));
                        retryCount++;
                        return true;
                    }
                }
            }

            // Case 4: Failed due to any exception in webhook tests (timing issues)
            if (throwable != null && retryCount < maxRetryCount) {
                String errorMessage = throwable.getMessage();
                if (errorMessage != null && 
                    (errorMessage.contains("null") || 
                     errorMessage.contains("NullPointerException") ||
                     errorMessage.contains("IndexOutOfBoundsException"))) {
                    System.out.println("Webhook timing error detected: " + errorMessage + " - Retrying... Retry count: " + (retryCount + 1));
                    retryCount++;
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("Exception in webhook retry analyzer: " + e.getMessage());
        }

        return false;
    }
} 
