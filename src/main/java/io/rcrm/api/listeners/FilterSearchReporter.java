package io.rcrm.api.listeners;

import org.json.JSONArray;
import org.json.JSONObject;
import io.restassured.response.Response;
import com.aventstack.extentreports.ExtentTest;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.ITestListener;
import org.testng.ITestContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterSearchReporter implements ITestListener, IInvokedMethodListener {

    public static final String ITEST_ATTR_FILTER_SEARCH_LOGS = "extent.filter.search.logs";

    private static final ThreadLocal<FilterSearchTestData> testData = new ThreadLocal<>();

    private static class FilterSearchTestData {
        String fieldName;
        String filterType;
        String filterValue;
        JSONObject payload;
        Response response;
        JSONArray responseData;
        List<String> reportLogs = new ArrayList<>();
        boolean filterCriteriaLogged = false;
    }

    @Override
    public void onTestStart(ITestResult result) {
        FilterSearchTestData data = new FilterSearchTestData();
        extractTestParameters(result, data);
        testData.set(data);
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        FilterSearchTestData data = testData.get();
        if (data != null) {
            ExtentTest currentTest = ExtentReportListener.test.get();
            if (currentTest != null) {
                logFilterSearchExecution(currentTest, data);
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        FilterSearchTestData data = testData.get();
        if (data != null) {
            ExtentTest currentTest = ExtentReportListener.test.get();
            if (currentTest != null) {
                if (data.fieldName != null && !data.filterCriteriaLogged) {
                    logFilterCriteria(currentTest, data);
                    data.filterCriteriaLogged = true;
                }
                logTestCompletion(currentTest, data, true);
            }
            persistFilterSearchLogs(result, data);
        }
        testData.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        FilterSearchTestData data = testData.get();
        if (data != null) {
            ExtentTest currentTest = ExtentReportListener.test.get();
            if (currentTest != null) {
                if (data.fieldName != null && !data.filterCriteriaLogged) {
                    logFilterCriteria(currentTest, data);
                    data.filterCriteriaLogged = true;
                }

                if (result.getThrowable() != null) {
                    String stackTrace = getStackTraceAsString(result.getThrowable());
                    currentTest.fail("<pre>" + stackTrace + "</pre>");
                }
                logTestCompletion(currentTest, data, false);
            }
            persistFilterSearchLogs(result, data);
        }
        testData.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        FilterSearchTestData data = testData.get();
        if (data != null) {
            ExtentTest currentTest = ExtentReportListener.test.get();
            if (currentTest != null) {
                if (data.fieldName != null && !data.filterCriteriaLogged) {
                    logFilterCriteria(currentTest, data);
                    data.filterCriteriaLogged = true;
                }
                currentTest.skip("Test Skipped: " + getSkipReason(result));
            }
            persistFilterSearchLogs(result, data);
        }
        testData.remove();
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        FilterSearchTestData data = testData.get();
        if (data != null) {
            ExtentTest currentTest = ExtentReportListener.test.get();
            if (currentTest != null) {
                if (data.fieldName != null && !data.filterCriteriaLogged) {
                    logFilterCriteria(currentTest, data);
                    data.filterCriteriaLogged = true;
                }
                currentTest.warning("Test Failed but within Success Percentage");
            }
            persistFilterSearchLogs(result, data);
        }
        testData.remove();
    }



    private void extractTestParameters(ITestResult result, FilterSearchTestData data) {
        if (!shouldExtractFilterSearchParameters(result)) {
            return;
        }
        Object[] parameters = result.getParameters();
        data.fieldName = (String) parameters[0];
        data.filterType = (String) parameters[1];
        data.filterValue = parameterToString(parameters[2]);
    }

    private static boolean shouldExtractFilterSearchParameters(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters == null || parameters.length < 3) {
            return false;
        }
        if (!(parameters[0] instanceof String) || !(parameters[1] instanceof String)) {
            return false;
        }
        return isFilterSearchTestClass(result.getTestClass().getRealClass());
    }

    private static boolean isFilterSearchTestClass(Class<?> testClass) {
        for (Class<?> c = testClass; c != null && c != Object.class; c = c.getSuperclass()) {
            if ("io.recruitcrm.Filters.FilterSearchBaseTest".equals(c.getName())) {
                return true;
            }
        }
        String className = testClass.getName();
        return className.contains(".Filters.") || className.contains(".BooleanSearch.");
    }

    private static String parameterToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }



    private void logFilterSearchExecution(ExtentTest test, FilterSearchTestData data) {
        if (data.fieldName != null && !data.filterCriteriaLogged) {
            logFilterCriteria(test, data);
            data.filterCriteriaLogged = true;
        }

        if (data.payload != null) {
            logRequestPayload(test, data.payload);
        }
    }

    private void logTestCompletion(ExtentTest test, FilterSearchTestData data, boolean success) {
        if (success) {
            test.pass("Filter Search Test Completed Successfully");
        } else {
            test.fail("Filter Search Test Failed");
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private void logFilterCriteria(ExtentTest test, FilterSearchTestData data) {
        String fieldNameLine = "&nbsp;&nbsp;<b>Field Name:</b> <code>" + htmlEscape(data.fieldName) + "</code>";
        String filterTypeLine = "&nbsp;&nbsp;<b>Filter Type:</b> <code>" + htmlEscape(data.filterType) + "</code>";
        String filterValueLine = "&nbsp;&nbsp;<b>Filter Value:</b> <code>" + htmlEscape(data.filterValue) + "</code>";
        logInfoLine(data, test, fieldNameLine);
        logInfoLine(data, test, filterTypeLine);
        logInfoLine(data, test, filterValueLine);
    }

    private void logRequestPayload(ExtentTest test, JSONObject payload) {
        try {
            String formattedPayload = payload.toString(2);
            String payloadLine = "<details><summary><b>Click to view Request Payload</b></summary>" +
                    "<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>" +
                    "<code>" + htmlEscape(formattedPayload) + "</code></pre></details>";
            logInfoLine(testData.get(), test, payloadLine);
        } catch (Exception e) {
            String payloadFallbackLine = "<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>" +
                    "<code>" + htmlEscape(payload.toString()) + "</code></pre>";
            logInfoLine(testData.get(), test, payloadFallbackLine);
        }
    }





    public static void logPayload(JSONObject payload) {
        FilterSearchTestData data = testData.get();
        if (data != null) {
            data.payload = payload;
        }
    }

    public static void skipFilterCriteriaLogging() {
        FilterSearchTestData data = testData.get();
        if (data != null) {
            data.filterCriteriaLogged = true;
        }
    }

    public static void logInfo(String label, String value) {
        FilterSearchTestData data = testData.get();
        ExtentTest currentTest = ExtentReportListener.test.get();
        if (currentTest != null && label != null && value != null) {
            String line = "&nbsp;&nbsp;<b>" + htmlEscape(label) + ":</b> <code>" + htmlEscape(value) + "</code>";
            logInfoLine(data, currentTest, line);
        } else if (data != null && label != null && value != null) {
            String line = "&nbsp;&nbsp;<b>" + htmlEscape(label) + ":</b> <code>" + htmlEscape(value) + "</code>";
            data.reportLogs.add(line);
        }
    }

    public static void logInfo(String message) {
        FilterSearchTestData data = testData.get();
        ExtentTest currentTest = ExtentReportListener.test.get();
        if (currentTest != null && message != null) {
            logInfoLine(data, currentTest, message);
        } else if (data != null && message != null) {
            data.reportLogs.add(message);
        }
    }

    public static void logResponse(Response response, JSONArray responseData) {
        FilterSearchTestData data = testData.get();
        ExtentTest currentTest = ExtentReportListener.test.get();

        if (data != null) {
            data.response = response;
            data.responseData = responseData;
        }

        // Add null check for currentTest to prevent NullPointerException
        if (response != null) {
            logInfoLine(data, currentTest, "<b>📥 API Response:</b>");
            logInfoLine(data, currentTest, "&nbsp;&nbsp;<b>Status Code:</b> <code>" + response.statusCode() + "</code>");
            logInfoLine(data, currentTest, "&nbsp;&nbsp;<b>Response Time:</b> <code>" + response.time() + " ms</code>");

            try {
                JSONObject responseJson = new JSONObject(response.asString());
                if (responseJson.has("meta") && responseJson.getJSONObject("meta").has("message")) {
                    String messageLine = "&nbsp;&nbsp;<b>Message:</b> <code>" +
                            htmlEscape(responseJson.getJSONObject("meta").getString("message")) + "</code>";
                    logInfoLine(data, currentTest, messageLine);
                }

                if (responseData != null) {
                    logInfoLine(data, currentTest, "&nbsp;&nbsp;<b>Records Returned:</b> <code>" + responseData.length() + "</code>");
                }

                String responseLine = "<details><summary><b>Click to view Full Response JSON</b></summary>" +
                        "<pre style='background-color: #e9ecef; padding: 10px; border-radius: 5px;'>" +
                        "<code>" + htmlEscape(responseJson.toString(2)) + "</code></pre></details>";
                logInfoLine(data, currentTest, responseLine);
            } catch (Exception e) {
                String responseFallbackLine = "<pre style='background-color: #e9ecef; padding: 10px; border-radius: 5px;'>" +
                        "<code>" + htmlEscape(response.asString()) + "</code></pre>";
                logInfoLine(data, currentTest, responseFallbackLine);
            }
        }
    }

    public static void logFieldValues(Response response, JSONArray responseData, String fieldName, String dbField) {
        logFieldValues(response, responseData, fieldName, dbField, null);
    }

    public static void logFieldValues(Response response, JSONArray responseData, String fieldName, String dbField, Map<String, String> contactIdToNameMap) {
        FilterSearchTestData data = testData.get();
        ExtentTest currentTest = ExtentReportListener.test.get();

        if (data != null) {
            data.response = response;
            data.responseData = responseData;
        }

        if (response != null) {
            logInfoLine(data, currentTest, "<b>📥 API Response:</b>");
            logInfoLine(data, currentTest, "&nbsp;&nbsp;<b>Status Code:</b> <code>" + response.statusCode() + "</code>");
            logInfoLine(data, currentTest, "&nbsp;&nbsp;<b>Response Time:</b> <code>" + response.time() + " ms</code>");

            try {
                JSONObject responseJson = new JSONObject(response.asString());
                if (responseJson.has("meta") && responseJson.getJSONObject("meta").has("message")) {
                    String messageLine = "&nbsp;&nbsp;<b>Message:</b> <code>" +
                            responseJson.getJSONObject("meta").getString("message") + "</code>";
                    logInfoLine(data, currentTest, messageLine);
                }

                if (responseData != null) {
                    logInfoLine(data, currentTest, "&nbsp;&nbsp;<b>Records Returned:</b> <code>" + responseData.length() + "</code>");

                    if (responseData.length() > 0) {
                        logInfoLine(data, currentTest, "<b>📋 " + htmlEscape(fieldName) + " Values from Returned Records:</b>");

                        StringBuilder fieldValues = new StringBuilder();
                        fieldValues.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
                        fieldValues.append("<code>");

                        for (int i = 0; i < responseData.length(); i++) {
                            JSONObject record = responseData.getJSONObject(i);
                            String fieldValue = record.optString(dbField, "N/A");
                            if (isEpochTimestamp(fieldValue)) {
                                fieldValue = convertEpochToDate(fieldValue);
                            }

                            // Get contact name from firstname and lastname fields
                            String firstName = record.optString("firstname", "");
                            String lastName = record.optString("lastname", "");
                            String contactName = null;

                            if (!firstName.isEmpty() || !lastName.isEmpty()) {
                                contactName = (firstName + " " + lastName).trim();
                            }

                            if (contactName == null || contactName.isEmpty()) {
                                contactName = record.optString("name", "");
                            }

                            if (contactName == null || contactName.isEmpty()) {
                                contactName = record.optString("slug", "");
                            }

                            if (contactName != null && !contactName.isEmpty()) {
                                if (fieldValue != null && !fieldValue.equals("N/A") && fieldValue.trim().equals(contactName.trim())) {
                                    fieldValues.append("Record ").append(i + 1).append(": ").append(htmlEscape(contactName)).append("\n");
                                } else {
                                    fieldValues.append("Record ").append(i + 1).append(": ").append(htmlEscape(contactName))
                                              .append(" - ").append(htmlEscape(fieldValue)).append("\n");
                                }
                            } else {
                                fieldValues.append("Record ").append(i + 1).append(": ").append(htmlEscape(fieldValue)).append("\n");
                            }
                        }

                        fieldValues.append("</code></pre>");
                        logInfoLine(data, currentTest, fieldValues.toString());
                    }
                }

            } catch (Exception e) {
                String responseFallbackLine = "<pre style='background-color: #e9ecef; padding: 10px; border-radius: 5px;'>" +
                        "<code>" + htmlEscape(response.asString()) + "</code></pre>";
                logInfoLine(data, currentTest, responseFallbackLine);
            }
        }
    }

    private static void logInfoLine(FilterSearchTestData data, ExtentTest currentTest, String line) {
        if (data != null) {
            data.reportLogs.add(line);
        }
        if (currentTest != null) {
            currentTest.info(line);
        }
    }

    private void persistFilterSearchLogs(ITestResult result, FilterSearchTestData data) {
        if (result == null || data == null || data.reportLogs == null || data.reportLogs.isEmpty()) {
            return;
        }
        result.setAttribute(ITEST_ATTR_FILTER_SEARCH_LOGS, new ArrayList<>(data.reportLogs));
    }

    private String getSkipReason(ITestResult result) {
        if (result.getThrowable() != null) {
            return htmlEscape(result.getThrowable().getMessage());
        }
        return "Unknown reason";
    }

    private static String htmlEscape(String input) {
        if (input == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#x27;"); break;
                case '/': sb.append("&#x2F;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }


    private static boolean isEpochTimestamp(String fieldValue) {
        if (fieldValue == null || fieldValue.equals("N/A") || fieldValue.trim().isEmpty()) {
            return false;
        }

        String trimmedValue = fieldValue.trim();

        // Check if it's a numeric string (digits only, optionally with negative sign)
        if (!trimmedValue.matches("^-?\\d+$")) {
            return false;
        }

        try {
            long epochValue = Long.parseLong(trimmedValue);
            return epochValue > 0 && (epochValue > 999999999L || epochValue > 999999999999L);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String convertEpochToDate(String fieldValue) {
        try {
            String trimmedValue = fieldValue.trim();
            long epochValue = Long.parseLong(trimmedValue);

            // Determine if it's in seconds or milliseconds
            long epochMillis = (epochValue > 9999999999L) ? epochValue : epochValue * 1000;

            // Convert epoch to LocalDate
            Instant instant = Instant.ofEpochMilli(epochMillis);
            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            // Format as dd-MM-yyyy
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return date.format(formatter);

        } catch (NumberFormatException | ArithmeticException e) {
            // Return original value if conversion fails
            return fieldValue;
        }
    }


}
