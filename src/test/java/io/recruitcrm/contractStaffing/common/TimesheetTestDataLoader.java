package io.recruitcrm.contractStaffing.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class TimesheetTestDataLoader {

    private TimesheetTestDataLoader() {
    }

    public enum TestSuite {
        SHIFT_SINGLE_RULE("shiftBasedRuleEngineCalculation", "weeklyCalculations", "SingleRuleTest.json", "singleRule"),
        SHIFT_DAILY_OT("shiftBasedRuleEngineCalculation", "weeklyCalculations", "DailyOvertimeRuleTest.json", "singleRule"),
        SHIFT_WEEKLY_OT("shiftBasedRuleEngineCalculation", "weeklyCalculations", "WeeklyOvertimeTest.json", "singleRule"),
        SHIFT_SPECIFIC_RANGE("shiftBasedRuleEngineCalculation", "weeklyCalculations", "SpecificRangeRuleTest.json", "singleRule"),
        SHIFT_NO_RULE("shiftBasedRuleEngineCalculation", "weeklyCalculations", "NoRuleTest.json", "NoRuleTest"),
        SHIFT_DOUBLE_RULE("shiftBasedRuleEngineCalculation", "weeklyCalculations", "DoubleRuleTest.json", "doubleRule"),
        SHIFT_THREE_RULE("shiftBasedRuleEngineCalculation", "weeklyCalculations", "ThreeRuleTest.json", "threeRule"),
        SHIFT_FOUR_RULE("shiftBasedRuleEngineCalculation", "weeklyCalculations", "FourRuleTest.json", "fourRule"),
        SHIFT_FIVE_RULE("shiftBasedRuleEngineCalculation", "weeklyCalculations", "FiveRuleTest.json", "fiveRule"),
        SHIFT_BIWEEKLY("shiftBasedRuleEngineCalculation", "biweeklyCalculations", "BiweeklyCalculationsTest.json", "biweeklyRule"),
        SHIFT_MONTHLY("shiftBasedRuleEngineCalculation", "monthlyCalculations", "MonthlyCalculationsTest.json", "monthlyRule"),
        SHIFT_UNALLOCATED("shiftBasedRuleEngineCalculation", "UnallocatedHoursCalculations", "UnallocatedHoursTestData.json", "UnallocatedHoursTestData"),

        HOURS_DAILY_OT("hoursBasedRuleEngineCalculation", "weeklyCalculations", "DailyOTHoursTest.json", "dailyOTHours"),
        HOURS_WEEKLY_OT("hoursBasedRuleEngineCalculation", "weeklyCalculations", "WeeklyOTHoursTest.json", "weeklyOTHours"),
        HOURS_SPECIFIC_RANGE("hoursBasedRuleEngineCalculation", "weeklyCalculations", "SpecificHoursRangeTest.json", "specificHoursRange"),
        HOURS_DOUBLE_RULE("hoursBasedRuleEngineCalculation", "weeklyCalculations", "DoubleRuleHoursTest.json", "doubleRuleHours"),
        HOURS_UNALLOCATED("hoursBasedRuleEngineCalculation", "weeklyCalculations", "UnallocatedHoursTest.json", "unallocatedHoursTest"),
        HOURS_BIWEEKLY("hoursBasedRuleEngineCalculation", "biweeklyCalculations", "BiweeklyHoursCalculationsTest.json", "biweeklyHoursRule"),
        HOURS_MONTHLY("hoursBasedRuleEngineCalculation", "monthlyCalculations", "MonthlyHoursCalculationsTest.json", "monthlyHoursRule");

        private final String basePackage;
        private final String subDirectory;
        private final String jsonFileName;
        private final String arrayKey;

        TestSuite(String basePackage, String subDirectory, String jsonFileName, String arrayKey) {
            this.basePackage = basePackage;
            this.subDirectory = subDirectory;
            this.jsonFileName = jsonFileName;
            this.arrayKey = arrayKey;
        }

        public String getBasePackage() { return basePackage; }
        public String getSubDirectory() { return subDirectory; }
        public String getJsonFileName() { return jsonFileName; }
        public String getArrayKey() { return arrayKey; }
    }

    public static List<TimesheetTestConfig> loadConfigs(TestSuite suite) {
        List<Map<String, Object>> maps = loadScenarios(suite);
        List<TimesheetTestConfig> configs = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            configs.add(TimesheetTestConfig.fromMap(map));
        }
        return configs;
    }

    public static List<TimesheetTestConfig> loadConfigs(String basePackage, String subDirectory,
                                                         String jsonFileName, String arrayKey) {
        List<Map<String, Object>> maps = loadScenarios(basePackage, subDirectory, jsonFileName, arrayKey);
        List<TimesheetTestConfig> configs = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            configs.add(TimesheetTestConfig.fromMap(map));
        }
        return configs;
    }

    public static List<TimesheetTestConfig> limitConfigs(List<TimesheetTestConfig> configs, int maxCount) {
        if (configs == null || configs.isEmpty()) return Collections.emptyList();
        int n = Math.min(maxCount, configs.size());
        return new ArrayList<>(configs.subList(0, n));
    }

    public static List<Map<String, Object>> loadScenarios(TestSuite suite) {
        return loadScenarios(suite.getBasePackage(), suite.getSubDirectory(),
                suite.getJsonFileName(), suite.getArrayKey());
    }

    public static List<Map<String, Object>> loadScenarios(String basePackage, String subDirectory,
                                                           String jsonFileName, String arrayKey) {
        String content = loadJsonContent(basePackage, subDirectory, jsonFileName);
        return parseScenarios(content, arrayKey);
    }

    public static List<Map<String, Object>> limitScenarios(List<Map<String, Object>> scenarios, int maxCount) {
        if (scenarios == null || scenarios.isEmpty()) return Collections.emptyList();
        int n = Math.min(maxCount, scenarios.size());
        return new ArrayList<>(scenarios.subList(0, n));
    }

    private static List<Map<String, Object>> parseScenarios(String content, String arrayKey) {
        JSONObject root = new JSONObject(content);
        if (!root.has(arrayKey)) {
            throw new IllegalArgumentException("JSON missing key: " + arrayKey);
        }
        JSONArray array = root.getJSONArray(arrayKey);
        List<Map<String, Object>> scenarios = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            scenarios.add(jsonObjectToMap(array.getJSONObject(i)));
        }
        return scenarios;
    }

    private static Map<String, Object> jsonObjectToMap(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if (value != null && value != JSONObject.NULL) {
                map.put(key, value);
            }
        }
        normalizeExpectedKeys(map);
        return map;
    }

    private static void normalizeExpectedKeys(Map<String, Object> map) {
        if (map.containsKey("expectedTotalPayRate") && !map.containsKey("expectedTotalPay")) {
            map.put("expectedTotalPay", map.get("expectedTotalPayRate"));
        }
        if (map.containsKey("expectedTotalBillRate") && !map.containsKey("expectedTotalBill")) {
            map.put("expectedTotalBill", map.get("expectedTotalBillRate"));
        }
    }

    private static String loadJsonContent(String basePackage, String subDirectory, String jsonFileName) {
        boolean hasTestDataDir = !basePackage.contains("UnallocatedHours") && !subDirectory.contains("UnallocatedHours");
        String testDataSegment = hasTestDataDir ? "/testData/" : "/";

        String resourcePath = basePackage + "/" + subDirectory + testDataSegment + jsonFileName;
        InputStream is = TimesheetTestDataLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            try {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read resource: " + resourcePath, e);
            }
        }

        Path resourcesFilePath = buildPath("src/test/resources", basePackage, subDirectory, hasTestDataDir ? "testData" : null, jsonFileName);
        if (Files.exists(resourcesFilePath)) {
            try {
                return Files.readString(resourcesFilePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + resourcesFilePath, e);
            }
        }

        Path javaTreePath = buildPath("src/test/java/io/recruitcrm/contractStaffing", basePackage, subDirectory, hasTestDataDir ? "testData" : null, jsonFileName);
        if (Files.exists(javaTreePath)) {
            try {
                return Files.readString(javaTreePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + javaTreePath, e);
            }
        }

        throw new RuntimeException("JSON not found: " + jsonFileName
                + " (tried classpath " + resourcePath + ", resources dir, and src/test/java tree)");
    }

    private static Path buildPath(String base, String basePackage, String subDirectory, String extraDir, String fileName) {
        String userDir = System.getProperty("user.dir");
        List<String> segments = new ArrayList<>();
        segments.addAll(Arrays.asList(base.split("/")));
        segments.addAll(Arrays.asList(basePackage.split("/")));
        segments.addAll(Arrays.asList(subDirectory.split("/")));
        if (extraDir != null) segments.add(extraDir);
        segments.add(fileName);

        Path result = Paths.get(userDir);
        for (String s : segments) {
            result = result.resolve(s);
        }
        return result;
    }
}
