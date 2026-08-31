package io.rcrm.api.listeners;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

public class ContractStaffingReporter {

    public boolean isContractStaffingTest(ITestResult result) {
        String className = result.getTestClass().getName();
        return className.contains("contractStaffing") ||
                className.contains("ContractStaffing") ||
                className.contains("contractStaffing.shiftBasedTimesheets") ||
                className.contains("contractStaffing.ruleEngineCalculation") ||
                className.contains("contractStaffing.TimeSheet") ||
                className.contains("contractStaffing.RuleEngine");
    }

    public void addContractStaffingTestDetails(ExtentTest test, ITestResult result, Status status) {
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();

        test.log(Status.INFO, "<b>Test Class:</b> " + className);
        test.log(Status.INFO, "<b>Test Method:</b> " + methodName);

        addOwnerAccountId(test, result);

        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            test.log(Status.INFO, "<b>Data Provider Parameters:</b>");
            String[] parameterNames = getParameterNames(result);
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] != null) {
                    String paramName = (i < parameterNames.length && parameterNames[i] != null)
                            ? parameterNames[i]
                            : "Parameter " + (i + 1);
                    test.log(Status.INFO, "&nbsp;&nbsp;<b>" + paramName + ":</b> " + parameters[i].toString());
                }
            }
        }

        if (status == Status.FAIL) {
            addFailureDetails(test, result);
        }
    }

    private void addOwnerAccountId(ExtentTest test, ITestResult result) {
        try {
            Class<?> testClass = result.getTestClass().getRealClass();
            Object testInstance = result.getInstance();

            try {
                java.lang.reflect.Field ownerAccountField = testClass.getDeclaredField("ownerAccountID");
                ownerAccountField.setAccessible(true);
                Object ownerAccountValue = ownerAccountField.get(testInstance);
                if (ownerAccountValue != null) {
                    test.log(Status.INFO, "<b>Owner Account ID:</b> " + ownerAccountValue.toString());
                    return;
                }
            } catch (NoSuchFieldException e) {
            }

            try {
                Class<?> threadManagerClass = Class.forName("com.qa.api.util.reaper.ThreadManager");
                java.lang.reflect.Method getAccountMethod = threadManagerClass.getMethod("getAccount");
                Object account = getAccountMethod.invoke(null);
                if (account != null) {
                    java.lang.reflect.Method getAccountIdMethod = account.getClass().getMethod("getAccountId");
                    Object accountId = getAccountIdMethod.invoke(account);
                    if (accountId != null) {
                        test.log(Status.INFO, "<b>Owner Account ID:</b> " + accountId.toString());
                        return;
                    }
                }
            } catch (Exception e) {
                test.log(Status.FAIL, "<b>Error:</b> " + e.getMessage());
            }

            String accountId = System.getProperty("accountId");
            if (accountId == null) {
                accountId = System.getenv("ACCOUNT_ID");
            }
            if (accountId != null) {
                test.log(Status.INFO, "<b>Owner Account ID:</b> " + accountId);
            } else {
                test.log(Status.WARNING, "<b>Owner Account ID:</b> Not available");
            }

        } catch (Exception e) {
            test.log(Status.WARNING, "<b>Owner Account ID:</b> Could not retrieve - " + e.getMessage());
        }
    }

    private void addFailureDetails(ExtentTest test, ITestResult result) {
        test.log(Status.FAIL, "<b>Contract Staffing Test Failure Details:</b>");

        long executionTime = result.getEndMillis() - result.getStartMillis();
        test.log(Status.FAIL, "&nbsp;&nbsp;<b>Execution Time:</b> " + executionTime + " ms");

        String[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            test.log(Status.FAIL, "&nbsp;&nbsp;<b>Test Groups:</b> " + String.join(", ", groups));
        }

        String description = result.getMethod().getDescription();
        if (description != null && !description.trim().isEmpty()) {
            test.log(Status.FAIL, "&nbsp;&nbsp;<b>Test Description:</b> " + description);
        }

        String environment = System.getProperty("envname");
        if (environment != null) {
            test.log(Status.FAIL, "&nbsp;&nbsp;<b>Environment:</b> " + environment);
        }

        test.log(Status.FAIL, "&nbsp;&nbsp;<b>Failure Time:</b> " + new Date().toString());
    }

    private String[] getParameterNames(ITestResult result) {
        try {
            Class<?> testClass = result.getTestClass().getRealClass();
            String methodName = result.getMethod().getMethodName();
            Object[] parameters = result.getParameters();

            Method[] methods = testClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Parameter[] methodParams = method.getParameters();
                    String[] parameterNames = new String[methodParams.length];

                    for (int i = 0; i < methodParams.length; i++) {
                        Parameter param = methodParams[i];
                        String paramName = param.getName();

                        // Check if parameter name is present (compiled with -parameters flag)
                        if (param.isNamePresent()) {
                            parameterNames[i] = paramName;
                        } else {
                            // Fallback to generic names if parameter names not available
                            parameterNames[i] = "Parameter " + (i + 1);
                        }
                    }

                    return parameterNames;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not extract parameter names: " + e.getMessage(), e);
        }

        return new String[0];
    }

}
