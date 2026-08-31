package io.rcrm.api.listeners;
import org.testng.*;
import org.testng.xml.XmlTest;

import java.util.HashMap;
import java.util.Map;

public class ExecutionTimeListener implements IExecutionListener, ITestListener {

    private long suiteStartTime;
    private long suiteEndTime;

    private Map<String, Long> testStartTimes = new HashMap<>();
    private Map<String, Long> testExecutionTimes = new HashMap<>();

    @Override
    public void onExecutionStart() {
        suiteStartTime = System.currentTimeMillis();
    }

    @Override
    public void onExecutionFinish() {
        suiteEndTime = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry : testExecutionTimes.entrySet()) {
        }
    }

    @Override
    public void onStart(ITestContext context) {
        testStartTimes.put(context.getName(), System.currentTimeMillis());
    }

    @Override
    public void onFinish(ITestContext context) {
        long endTime = System.currentTimeMillis();
        long startTime = testStartTimes.get(context.getName());
        long executionTime = endTime - startTime;

        testExecutionTimes.put(context.getName(), executionTime);
    }

    @Override
    public void onTestStart(ITestResult result) {

    }

    @Override
    public void onTestSuccess(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {

    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

}

