# JSON-Driven DataProvider Design for Multiple Time Entry Tests

## 1. Design Overview

**Goal**: Store all test combination data in JSON files (e.g. `AfterShiftRuleTest.json`) and load them in the test class via `@DataProvider`. The test class contains only the test logic; data lives in JSON.

**Benefits**:
- **Separation of concerns**: Data vs. code
- **Easy to add/change scenarios**: Edit JSON without changing Java
- **Reusable across tests**: Same JSON structure for different rule types
- **Version control friendly**: Clear diffs when adding scenarios

---

## 2. Recommended Structure

### 2.1 File Placement

| Approach | Path | Pros | Cons |
|----------|------|------|------|
| **A. Resources folder** | `src/test/resources/multipleTimeEntry/AfterShiftRuleTest.json` | Standard Maven layout; packaged in JAR | Path from test class different |
| **B. Same package** | `src/test/java/.../shiftBaseRuleCalculation/AfterShiftRuleTest.json` | Colocated with test | JSON in `src` is non-standard |

**Recommendation**: Use ** `src/test/resources/multipleTimeEntry/`** – aligns with existing tests (e.g. `taskSideBarFilterDataProvider.json`).

### 2.2 JSON Structure

Align with the DataProvider parameters from `RuleEngineBiweeklyCalculationsTest`:

```json
{
  "scenarios": [
    {
      "testId": "AFTER_001",
      "dayPattern": "[mon,tue,wed,thu,fri]",
      "regularHours": "9:00-17:00",
      "actualWorkTime": "Week1: [Mon: 9:00-11:00; 13:00-15:00; 18:00-20:00, Tue: 9:00-17:00], Week2: [Mon: 9:00-17:00]",
      "method": "Shift",
      "rulesApplied": "Regular Hours: 1x, After Shift (17:00): 1.5x",
      "breakTime": "None",
      "breakBillable": "Yes",
      "payRate": 25.0,
      "billRate": 40.0,
      "expectedPay": 550.0,
      "expectedBill": 880.0,
      "jobStartDate": 1751328000,
      "jobEndDate": 1753920000,
      "timesheetFrequency": 3,
      "timesheetStartDay": 1,
      "payCurrencyId": 53,
      "billCurrencyId": 53,
      "breakTimeThreshold": 0
    },
    {
      "testId": "AFTER_002",
      "dayPattern": "[mon,tue,wed,thu,fri]",
      "regularHours": "9:00-17:00",
      "actualWorkTime": "Week1: [Mon: 9:00-11:00; 12:00-13:00; 18:00-20:00], Week2: [Mon: 9:00-17:00]",
      "method": "Shift",
      "rulesApplied": "Regular Hours: 1x, After Shift (17:00): 1.5x",
      "breakTime": "11:00-12:00",
      "breakBillable": "No",
      "payRate": 25.0,
      "billRate": 40.0,
      "expectedPay": 475.0,
      "expectedBill": 760.0,
      "jobStartDate": 1751328000,
      "jobEndDate": 1753920000,
      "timesheetFrequency": 3,
      "timesheetStartDay": 1,
      "payCurrencyId": 53,
      "billCurrencyId": 53,
      "breakTimeThreshold": 0
    }
  ]
}
```

**Key points**:
- Root key `scenarios` (or `testCases`) – array of scenario objects
- Each scenario = one row in DataProvider
- Field names match (or can map to) DataProvider parameters

### 2.3 Optional: Group by Break Variant

To organize by break type:

```json
{
  "Break Not Taken": [
    { "testId": "AFTER_001", "dayPattern": "[mon,tue,wed,thu,fri]", ... }
  ],
  "Break Paid and Billed": [
    { "testId": "AFTER_002", ... }
  ],
  "Break Not Paid or Billed": [
    { "testId": "AFTER_003", ... }
  ]
}
```

Then iterate over keys and arrays (like `TaskSideBarFilterTest`).

---

## 3. Test Class Design

### 3.1 Class Structure

```java
package io.recruitcrm.contractStaffing.multipleTimeEntry.shiftBaseRulesCalculations;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@TestBase.AccountType("Business|AlbatrossTkn")
public class AfterShiftRuleTest extends RuleEngineCalculationBase {

    private List<Integer> createdTemplateIds = new ArrayList<>();
    String albatrossAuthToken;
    String apiAuthToken;
    commanFunction function;

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
    }

    @DataProvider(name = "afterShiftRuleDataProvider", parallel = true)
    public Object[][] provideAfterShiftRuleTestData() {
        JSONObject json = loadJsonFromResources("multipleTimeEntry/AfterShiftRuleTest.json");
        JSONArray scenarios = json.getJSONArray("scenarios");
        List<Object[]> rows = new ArrayList<>();

        for (int i = 0; i < scenarios.length(); i++) {
            JSONObject s = scenarios.getJSONObject(i);
            rows.add(new Object[]{
                    s.getString("testId"),
                    s.getString("dayPattern"),
                    s.getString("regularHours"),
                    s.getString("actualWorkTime"),
                    s.getString("method"),
                    s.getString("rulesApplied"),
                    s.optString("breakTime", "None"),
                    s.getString("breakBillable"),
                    s.getDouble("payRate"),
                    s.getDouble("billRate"),
                    s.getDouble("expectedPay"),
                    s.getDouble("expectedBill"),
                    s.getLong("jobStartDate"),
                    s.getLong("jobEndDate"),
                    s.getInt("timesheetFrequency"),
                    s.getInt("timesheetStartDay"),
                    s.getInt("payCurrencyId"),
                    s.getInt("billCurrencyId"),
                    s.optInt("breakTimeThreshold", 0)
            });
        }
        return rows.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "afterShiftRuleDataProvider")
    public void verifyAfterShiftRuleCalculation(String testId, String dayPattern, String regularHours,
                                                String actualWorkTime, String method, String rulesApplied, String breakTime,
                                                String breakBillable, double payRate, double billRate, double expectedPay, double expectedBill,
                                                Long jobStartDate, Long jobEndDate, Integer timesheetFrequency, Integer timesheetStartDay,
                                                Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold) {
        // Same flow as RuleEngineBiweeklyCalculationsTest.verifyBiweeklyTimesheetCalculation
        // ... (create template, setup, create timesheet, update time entries, evaluate, validate)
    }

    private JSONObject loadJsonFromResources(String relativePath) {
        try {
            String basePath = System.getProperty("user.dir") + "/src/test/resources/";
            String content = Files.readString(Paths.get(basePath + relativePath));
            return new JSONObject(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON: " + relativePath, e);
        }
    }
}
```

### 3.2 JSON Loader Utility

Add a shared utility (e.g. in `RuleEngineCalculationBase` or a new helper):

```java
protected JSONObject loadJsonFromResources(String relativePath) {
    try {
        String basePath = System.getProperty("user.dir") + "/src/test/resources/";
        String content = Files.readString(Paths.get(basePath + relativePath));
        return new JSONObject(content);
    } catch (Exception e) {
        throw new RuntimeException("Failed to load JSON: " + relativePath, e);
    }
}
```

**Alternative – classpath** (if running from IDE):

```java
protected JSONObject loadJsonFromResources(String relativePath) {
    try (InputStream is = getClass().getClassLoader()
            .getResourceAsStream(relativePath)) {
        if (is == null) throw new RuntimeException("Resource not found: " + relativePath);
        String content = new String(is.readAllBytes());
        return new JSONObject(content);
    } catch (Exception e) {
        throw new RuntimeException("Failed to load JSON: " + relativePath, e);
    }
}
```

Use `"multipleTimeEntry/AfterShiftRuleTest.json"` for classpath (no leading slash).

---

## 4. Per-Rule JSON Files

| Rule Type | JSON File | Test Class |
|-----------|-----------|------------|
| After Shift | `AfterShiftRuleTest.json` | `AfterShiftRuleTest.java` |
| Before Shift | `BeforeShiftRuleTest.json` | `BeforeShiftRuleTest.java` |
| Specific Hours | `SpecificHoursRuleTest.json` | `SpecificHoursRuleTest.java` |
| Daily OT | `DailyOvertimeRuleTest.json` | `DailyOvertimeRuleTest.java` |
| Weekly OT | `WeeklyOvertimeRuleTest.json` | `WeeklyOvertimeRuleTest.java` |
| ... | ... | ... |

**Shared base**: All extend `RuleEngineCalculationBase`; only DataProvider and JSON path differ.

---

## 5. How to Proceed

### Step 1: Create JSON Loader

Add `loadJsonFromResources(String relativePath)` to `RuleEngineCalculationBase` (or a `MultipleTimeEntryTestBase` that extends it).

### Step 2: Create `AfterShiftRuleTest.json`

- Path: `src/test/resources/multipleTimeEntry/AfterShiftRuleTest.json`
- Structure: `{ "scenarios": [ {...}, {...} ] }`
- Add 2–3 scenarios initially (e.g. break not taken, break paid, break not paid)

### Step 3: Implement `AfterShiftRuleTest.java`

- Extend `RuleEngineCalculationBase`
- Add `@DataProvider` that loads JSON and returns `Object[][]`
- Add `@Test` method with same signature as `verifyBiweeklyTimesheetCalculation`
- Call the same setup/update/evaluate/validate flow

### Step 4: Adapt for Multiple Entries

When the API supports multiple entries per day:

- Extend `actualWorkTime` format in JSON (e.g. `Mon: 9-11; 13-15; 18-20`)
- Use `updateMultipleTimeEntriesWithCsvData()` instead of `updateTimeEntriesWithCsvData()` in the test

### Step 5: Replicate for Other Rules

- Copy `AfterShiftRuleTest.json` → `BeforeShiftRuleTest.json`, etc.
- Adjust `rulesApplied` and expected values
- Create corresponding test classes with their own DataProvider pointing to their JSON

---

## 6. Example: Minimal `AfterShiftRuleTest.json`

```json
{
  "scenarios": [
    {
      "testId": "AFTER_001",
      "dayPattern": "[mon,tue,wed,thu,fri]",
      "regularHours": "9:00-17:00",
      "actualWorkTime": "Week1: [Mon: 9:00-11:00; 13:00-15:00; 18:00-20:00, Tue: 9:00-17:00], Week2: [Mon: 9:00-17:00]",
      "method": "Shift",
      "rulesApplied": "Regular Hours: 1x, After Shift (17:00): 1.5x",
      "breakTime": "None",
      "breakBillable": "Yes",
      "payRate": 25.0,
      "billRate": 40.0,
      "expectedPay": 550.0,
      "expectedBill": 880.0,
      "jobStartDate": 1751328000,
      "jobEndDate": 1753920000,
      "timesheetFrequency": 3,
      "timesheetStartDay": 1,
      "payCurrencyId": 53,
      "billCurrencyId": 53,
      "breakTimeThreshold": 0
    }
  ]
}
```

---

## 7. Existing `AfterShiftRule.json` Note

The current `AfterShiftRule.json` in the package has a different structure (workTimeDetails, ruleSettings, etc.). For the **DataProvider approach**, use the new structure above so it maps 1:1 to the test method parameters. You can keep the old file for other uses or migrate its scenarios into the new format.
