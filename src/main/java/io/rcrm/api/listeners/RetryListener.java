package io.rcrm.api.listeners;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import io.rcrm.api.util.GenericRetryAnalyzer;
import com.qa.api.util.RetryOn500OrSkippedAnalyzer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryListener implements IAnnotationTransformer {
    private static final String RETRY_PROPERTY = "retry";

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        // Only enable retry analyzer when -Dretry=true is passed
        if (!Boolean.parseBoolean(System.getProperty(RETRY_PROPERTY, "false"))) {
            return;
        }
        // Assign the GenericRetryAnalyzer to all tests to enable retries for any failures or skips
        annotation.setRetryAnalyzer(GenericRetryAnalyzer.class);
    }
}
