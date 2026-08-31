package io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.qa.api.util.TestUtil;

import io.rcrm.api.javafaker.ContractStaffing.RuleEngineenFake;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Base test class for Multiple Time Entry tests.
 * Provides helper methods for:
 * - Parsing JSON-driven rule definitions (e.g. "AfterShift:[After,
 * Paymultiplier: 2x, ...]")
 * - Parsing multi-entry actual work times (e.g. "Week1: [Mon: 9:00-11:00;
 * 13:00-15:00, ...]")
 * - Building workTimeDetails payload for the updated time logs endpoint
 * - Full flow: create template → entities → enable timesheet → create → update
 * → approve → evaluate → validate
 */
public abstract class MultipleTimeEntryBaseTest extends ContractStaffingBaseTest {

    protected RuleEngineenFake ruleEngineenFake;

    // ===== Constants =====
    protected static final int HOURS_METHOD = 1;
    protected static final int SHIFTS_LOGGING = 2;
    protected static final int MULTIPLIER_CHARGE = 1;
    protected static final int FIXED_RATE_CHARGE = 2;

    // Rule type IDs
    protected static final int AFTER_SHIFT_RULE = 1;
    protected static final int BEFORE_SHIFT_RULE = 2;
    protected static final int SPECIFIC_RANGE_RULE = 3;
    protected static final int DAILY_OVERTIME_SHIFT_RULE = 4;
    protected static final int WEEKLY_OVERTIME_SHIFT_RULE = 5;
    protected static final int SPECIFIC_HOURS_RANGE_RULE = 6;
    protected static final int DAILY_OVERTIME_HOURS_RULE = 7;
    protected static final int WEEKLY_OVERTIME_HOURS_RULE = 8;

    // Time constants
    protected static final int DEFAULT_WORK_START_TIME = 28800;
    protected static final int DEFAULT_WORK_END_TIME = 61200;
    protected static final int SECONDS_IN_HOUR = 3600;
    protected static final int SECONDS_IN_MINUTE = 60;
    protected static final long SECONDS_IN_DAY = 24L * 60 * 60;

    // HTTP status constants
    protected static final int HTTP_OK = 200;
    protected static final int HTTP_CREATED = 201;
    protected static final double AMOUNT_TOLERANCE = 0.01;
    protected static final int TIMESHEET_APPROVED_STATUS = 4;

    // JSON field constants
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final String WORK_TIME = "workTime";
    private static final String WORK_START_TIME = "workStartTime";
    private static final String WORK_END_TIME = "workEndTime";
    private static final String TIMESHEET_ID = "timesheetId";
    private static final String TOTAL_TIME = "totalTime";
    private static final String RULE_TYPE = "ruleType";
    private static final String WORK_DAY_ID = "workDayId";
    private static final String CHARGE_METHOD = "chargeMethod";
    private static final String START_DURATION = "startDuration";
    private static final String END_DURATION = "endDuration";
    private static final String DAILY_THRESHOLD = "dailyThreshold";
    private static final String WEEKLY_THRESHOLD = "weeklyThreshold";
    private static final String PAY_RATE_MULTIPLIER = "payRateMultiplier";
    private static final String BILL_RATE_MULTIPLIER = "billRateMultiplier";
    private static final String PAY_RATE_PER_HOUR = "payRatePerHour";
    private static final String BILL_RATE_PER_HOUR = "billRatePerHour";
    private static final String CONTRACTOR_IDS = "contractorIds";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String TIMESHEET_DATES = "timesheetDates";
    private static final String TIMESHEET_FREQUENCY_ID = "timesheetFrequencyId";
    private static final String TIMESHEET_START_DAY = "timesheetStartDay";
    private static final String WORK_DAY_IDS = "workDayIds";
    private static final String WORK_LOG_TYPE = "workLogType";
    private static final String CALCULATE_BREAK_TIME = "calculateBreakTime";
    private static final String CUSTOM_RULES = "customRules";
    private static final String TEMPLATE_NAME = "templateName";
    private static final String BREAK_INTERVALS = "breakIntervals";
    private static final String OVER_TIME = "overTime";
    private static final String BREAK_TIME = "breakTime";
    private static final String BREAK_TIME_THRESHOLD = "breakTimeThreshold";
    private static final String ID = "id";
    private static final String RULE_NAME = "ruleName";

    // Day mappings
    private static final String[] DAY_NAMES = { "", "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
    private static final Map<String, Integer> DAY_TO_NUMBER = createDayToNumberMap();
    private static final Map<String, String> DAY_ABBREVIATIONS = createDayAbbreviations();

    private static Map<String, Integer> createDayToNumberMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("monday", 1);
        map.put("mon", 1);
        map.put("tuesday", 2);
        map.put("tue", 2);
        map.put("tues", 2);
        map.put("wednesday", 3);
        map.put("wed", 3);
        map.put("thursday", 4);
        map.put("thu", 4);
        map.put("thurs", 4);
        map.put("friday", 5);
        map.put("fri", 5);
        map.put("saturday", 6);
        map.put("sat", 6);
        map.put("sunday", 7);
        map.put("sun", 7);
        return map;
    }

    private static Map<String, String> createDayAbbreviations() {
        Map<String, String> map = new HashMap<>();
        map.put("monday", "mon");
        map.put("mon", "mon");
        map.put("tuesday", "tue");
        map.put("tue", "tue");
        map.put("tues", "tue");
        map.put("wednesday", "wed");
        map.put("wed", "wed");
        map.put("thursday", "thu");
        map.put("thu", "thu");
        map.put("thurs", "thu");
        map.put("friday", "fri");
        map.put("fri", "fri");
        map.put("saturday", "sat");
        map.put("sat", "sat");
        map.put("sunday", "sun");
        map.put("sun", "sun");
        return map;
    }

    // ===== Parsed Rule POJO =====

    /**
     * Represents a single parsed rule from the JSON rulesApplied string.
     * Example input: "AfterShift:[After, Paymultiplier: 2x, BillMultiplier: 2x,
     * startsFrom:17:00, appliedDay:[mon,tue]]"
     * Parsed into: ruleKey=AfterShift, ruleType=After, payMultiplier=2.0,
     * billMultiplier=2.0,
     * thresholdValue="17:00", appliedDays=[1,2]
     */
    public static class ParsedRule {
        private final String ruleKey; // e.g. "AfterShift", "BeforeShift", "RegularHours", "DailyOT", "WeeklyOT",
                                      // "SpecificRange"
        private final String ruleTypeName; // e.g. "After", "Before", "Regular", "DailyOT", "WeeklyOT", "SpecificRange"
        private final double payMultiplier;
        private final double billMultiplier;
        private final double payFixedRate; // for fixed rate rules
        private final double billFixedRate; // for fixed rate rules
        private final String thresholdValue; // time like "17:00" or hours like ">8"
        private final String rangeStart; // for specific range: start time
        private final String rangeEnd; // for specific range: end time
        private final List<Integer> appliedDays;
        private final boolean isFixedRate;

        public ParsedRule(String ruleKey, String ruleTypeName, double payMultiplier, double billMultiplier,
                double payFixedRate, double billFixedRate,
                String thresholdValue, String rangeStart, String rangeEnd,
                List<Integer> appliedDays, boolean isFixedRate) {
            this.ruleKey = ruleKey;
            this.ruleTypeName = ruleTypeName;
            this.payMultiplier = payMultiplier;
            this.billMultiplier = billMultiplier;
            this.payFixedRate = payFixedRate;
            this.billFixedRate = billFixedRate;
            this.thresholdValue = thresholdValue;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
            this.appliedDays = appliedDays != null ? appliedDays : new ArrayList<>();
            this.isFixedRate = isFixedRate;
        }

        public String getRuleKey() {
            return ruleKey;
        }

        public String getRuleTypeName() {
            return ruleTypeName;
        }

        public double getPayMultiplier() {
            return payMultiplier;
        }

        public double getBillMultiplier() {
            return billMultiplier;
        }

        public double getPayFixedRate() {
            return payFixedRate;
        }

        public double getBillFixedRate() {
            return billFixedRate;
        }

        public String getThresholdValue() {
            return thresholdValue;
        }

        public String getRangeStart() {
            return rangeStart;
        }

        public String getRangeEnd() {
            return rangeEnd;
        }

        public List<Integer> getAppliedDays() {
            return appliedDays;
        }

        public boolean isFixedRate() {
            return isFixedRate;
        }

        @Override
        public String toString() {
            return "ParsedRule{key=" + ruleKey + ", type=" + ruleTypeName +
                    ", payMul=" + payMultiplier + ", billMul=" + billMultiplier +
                    ", threshold=" + thresholdValue + ", days=" + appliedDays + "}";
        }
    }

    /**
     * Represents a single work time entry for a day (one of potentially multiple
     * entries).
     * e.g. Mon: 9:00-11:00 is one entry, 13:00-15:00 is another entry for the same
     * day.
     */
    public static class WorkTimeEntry {
        private final int workStartTime; // in seconds
        private final int workEndTime; // in seconds

        public WorkTimeEntry(int workStartTime, int workEndTime) {
            this.workStartTime = workStartTime;
            this.workEndTime = workEndTime;
        }

        public int getWorkStartTime() {
            return workStartTime;
        }

        public int getWorkEndTime() {
            return workEndTime;
        }

        public int getDuration() {
            return workEndTime - workStartTime;
        }
    }

    /**
     * Represents a single break interval with start and end times.
     */
    public static class BreakEntry {
        private final int breakStartTime;
        private final int breakEndTime;

        public BreakEntry(int breakStartTime, int breakEndTime) {
            this.breakStartTime = breakStartTime;
            this.breakEndTime = breakEndTime;
        }

        public int getBreakStartTime() {
            return breakStartTime;
        }

        public int getBreakEndTime() {
            return breakEndTime;
        }

        public int getDuration() {
            return breakEndTime - breakStartTime;
        }
    }

    /**
     * Holds all parsed work time entries and break entries for a given week,
     * keyed by day name (e.g. "mon", "tue").
     * Each day can have multiple WorkTimeEntry objects (multiple time entries per
     * day).
     * Breaks are mapped to specific work time entries using work time range as key.
     */
    public static class WeekWorkData {
        // day -> list of work time entries (multiple entries per day)
        private final Map<String, List<WorkTimeEntry>> dayWorkEntries;
        // day-qualified work entry key (e.g., "mon_5:00-9:00") -> list of breaks for
        // that work entry
        private final Map<String, List<BreakEntry>> workEntryBreaks;

        public WeekWorkData() {
            this.dayWorkEntries = new LinkedHashMap<>();
            this.workEntryBreaks = new LinkedHashMap<>();
        }

        public void addWorkEntry(String day, WorkTimeEntry entry) {
            dayWorkEntries.computeIfAbsent(day.toLowerCase(), k -> new ArrayList<>()).add(entry);
        }

        /**
         * Maps breaks to a specific work time entry using a day-qualified key.
         * 
         * @param dayName     e.g., "mon"
         * @param workTimeKey e.g., "5:00-9:00" (start-end time range)
         * @param breaks      list of breaks for this work entry
         */
        public void addBreaksForWorkEntry(String dayName, String workTimeKey, List<BreakEntry> breaks) {
            String qualifiedKey = dayName.toLowerCase() + "_" + workTimeKey;
            workEntryBreaks.put(qualifiedKey, breaks != null ? breaks : new ArrayList<>());
        }

        /**
         * Gets breaks for a specific work time entry on a specific day.
         * 
         * @param dayName   e.g., "mon"
         * @param workEntry the work time entry
         * @return list of breaks for this work entry, or empty list if none
         */
        public List<BreakEntry> getBreaksForWorkEntry(String dayName, WorkTimeEntry workEntry) {
            String key = formatWorkTimeKey(dayName, workEntry);
            return workEntryBreaks.getOrDefault(key, new ArrayList<>());
        }

        /**
         * Creates a day-qualified key from work entry for mapping breaks.
         * Format: "mon_5:00-9:00" (day prefix avoids collisions when different days
         * have same time ranges)
         */
        private String formatWorkTimeKey(String dayName, WorkTimeEntry entry) {
            int startHr = entry.getWorkStartTime() / SECONDS_IN_HOUR;
            int startMin = (entry.getWorkStartTime() % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
            int endHr = entry.getWorkEndTime() / SECONDS_IN_HOUR;
            int endMin = (entry.getWorkEndTime() % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
            return String.format("%s_%d:%02d-%d:%02d", dayName.toLowerCase(), startHr, startMin, endHr, endMin);
        }

        public Map<String, List<WorkTimeEntry>> getDayWorkEntries() {
            return dayWorkEntries;
        }

        public List<WorkTimeEntry> getWorkEntriesForDay(String day) {
            return dayWorkEntries.getOrDefault(day.toLowerCase(), new ArrayList<>());
        }
    }

    protected MultipleTimeEntryBaseTest() {
        super();
        this.ruleEngineenFake = new RuleEngineenFake();
    }

    // ========================================================================
    // SECTION 1: RULE PARSING - Parse JSON rulesApplied string into ParsedRule list
    // ========================================================================

    /**
     * Parses the rulesApplied string from JSON into a list of ParsedRule objects.
     * 
     * Supported formats:
     * - "AfterShift:[After, Paymultiplier: 2x, BillMultiplier: 2x,
     * startsFrom:17:00, appliedDay:[mon,tue]]"
     * - "RegularHours:[multiplier: 1x, range:9:00-17:00]"
     * - "BeforeShift:[multiplier: 2x, range:7:00]"
     * - "DailyOT:[multiplier: 1.5x, threshold:>8 hrs]"
     * - "WeeklyOT:[multiplier: 2x, threshold:>40 hrs]"
     * - "SpecificRange:[multiplier: 1.5x, range:16:00-18:00]"
     * - Multiple rules comma-separated at top level
     * 
     * @param rulesApplied      the raw rules string from JSON
     * @param defaultWorkDayIds fallback day IDs if appliedDay is not specified in a
     *                          rule
     * @return list of ParsedRule objects
     */
    protected List<ParsedRule> parseRulesFromJson(String rulesApplied, List<Integer> defaultWorkDayIds) {
        List<ParsedRule> rules = new ArrayList<>();
        if (rulesApplied == null || rulesApplied.trim().isEmpty()) {
            return rules;
        }

        // Split top-level rules: "RuleKey:[...], RuleKey2:[...]"
        // We need to split by comma that is NOT inside brackets
        List<String> ruleStrings = splitTopLevelRules(rulesApplied.trim());

        for (String ruleStr : ruleStrings) {
            ruleStr = ruleStr.trim();
            if (ruleStr.isEmpty())
                continue;

            ParsedRule parsed = parseSingleRule(ruleStr, defaultWorkDayIds);
            if (parsed != null) {
                rules.add(parsed);
            }
        }
        return rules;
    }

    /**
     * Splits the top-level rules string by commas that are NOT inside square
     * brackets.
     * e.g. "AfterShift:[...], BeforeShift:[...]" -> ["AfterShift:[...]",
     * "BeforeShift:[...]"]
     */
    private List<String> splitTopLevelRules(String input) {
        List<String> result = new ArrayList<>();
        int bracketDepth = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '[') {
                bracketDepth++;
                current.append(c);
            } else if (c == ']') {
                bracketDepth--;
                current.append(c);
                // If we just closed the outermost bracket, this rule is complete
                if (bracketDepth == 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                    // Skip any comma/whitespace after closing bracket
                    while (i + 1 < input.length() && (input.charAt(i + 1) == ',' || input.charAt(i + 1) == ' ')) {
                        i++;
                    }
                }
            } else {
                current.append(c);
            }
        }
        // If there's remaining text (shouldn't normally happen with well-formed input)
        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            result.add(remaining);
        }
        return result;
    }

    /**
     * Parses a single rule string like "AfterShift:[After, Paymultiplier: 2x,
     * BillMultiplier: 2x, startsFrom:17:00, appliedDay:[mon,tue]]"
     */
    private ParsedRule parseSingleRule(String ruleStr, List<Integer> defaultWorkDayIds) {
        // Extract rule key (before the colon-bracket)
        int bracketStart = ruleStr.indexOf(":[");
        if (bracketStart == -1) {
            // Try simple format: "RuleKey:[...]"
            bracketStart = ruleStr.indexOf('[');
            if (bracketStart == -1)
                return null;
            String ruleKey = ruleStr.substring(0, bracketStart).trim();
            String content = ruleStr.substring(bracketStart);
            return parseRuleContent(ruleKey, content, defaultWorkDayIds);
        }

        String ruleKey = ruleStr.substring(0, bracketStart).trim();
        // Content is everything inside the outer brackets
        String content = ruleStr.substring(bracketStart + 1); // skip ":"
        return parseRuleContent(ruleKey, content, defaultWorkDayIds);
    }

    private ParsedRule parseRuleContent(String ruleKey, String bracketContent, List<Integer> defaultWorkDayIds) {
        // Strip outer brackets
        String content = bracketContent.trim();
        if (content.startsWith("["))
            content = content.substring(1);
        if (content.endsWith("]"))
            content = content.substring(0, content.length() - 1);
        content = content.trim();

        String normalizedKey = normalizeRuleKey(ruleKey);

        // Parse key-value pairs inside the bracket content
        // Split by comma, but not commas inside nested brackets
        List<String> parts = splitByCommaOutsideBrackets(content);

        // Extract the first element as the rule name (if it doesn't contain a colon,
        // it's the rule name)
        String ruleTypeName = null;
        List<String> remainingParts = new ArrayList<>();
        if (!parts.isEmpty()) {
            String firstPart = parts.get(0).trim();
            // If first part doesn't contain a colon, it's the rule name
            if (!firstPart.contains(":")) {
                ruleTypeName = firstPart;
                remainingParts = parts.subList(1, parts.size());
            } else {
                // First part has a colon, so it's a key-value pair, not the rule name
                ruleTypeName = normalizedKey; // Fallback to normalized key
                remainingParts = parts;
            }
        } else {
            ruleTypeName = normalizedKey;
        }

        // If ruleTypeName is still null or empty, use normalizedKey
        if (ruleTypeName == null || ruleTypeName.trim().isEmpty()) {
            ruleTypeName = normalizedKey;
        }

        double payMultiplier = 1.0;
        double billMultiplier = 1.0;
        double payFixedRate = 0.0;
        double billFixedRate = 0.0;
        boolean isFixedRate = false;
        String thresholdValue = "";
        String rangeStart = "";
        String rangeEnd = "";
        // Start with empty list - appliedDays will be set from appliedDay field if
        // specified
        // Only WeeklyOT will default to all 7 days if appliedDay is not specified
        // Other rules (DailyOT, SpecificRange, BeforeShift, AfterShift, etc.) will use
        // defaultWorkDayIds
        List<Integer> appliedDays = new ArrayList<>();

        for (String part : remainingParts) {
            part = part.trim();
            String lowerPart = part.toLowerCase();

            if (lowerPart.contains("paymultiplier:") || lowerPart.contains("pay_multiplier:")) {
                payMultiplier = extractMultiplierValue(part);
            } else if (lowerPart.contains("billmultiplier:") || lowerPart.contains("billmultipier:")
                    || lowerPart.contains("bill_multiplier:")) {
                billMultiplier = extractMultiplierValue(part);
            } else if (lowerPart.contains("multiplier:") && !lowerPart.contains("pay") && !lowerPart.contains("bill")) {
                // Generic multiplier applies to both pay and bill
                double mul = extractMultiplierValue(part);
                payMultiplier = mul;
                billMultiplier = mul;
            } else if (lowerPart.contains("payrateperhour:") || lowerPart.contains("pay_rate_per_hour:")) {
                payFixedRate = extractNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("billrateperhour:") || lowerPart.contains("bill_rate_per_hour:")) {
                billFixedRate = extractNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("payfixedrate:") || lowerPart.contains("pay_fixed_rate:")) {
                payFixedRate = extractNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("billfixedrate:") || lowerPart.contains("bill_fixed_rate:")) {
                billFixedRate = extractNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("fixed")) {
                // Parse fixed rate pattern like "Fixed $35/$55"
                double[] rates = extractFixedRatePair(part);
                if (rates[0] > 0 || rates[1] > 0) {
                    payFixedRate = rates[0];
                    billFixedRate = rates[1];
                    isFixedRate = true;
                }
            } else if (lowerPart.contains("startsfrom:") || lowerPart.contains("starts_from:")) {
                thresholdValue = extractValueAfterColon(part);
            } else if (lowerPart.contains("threshold:")) {
                thresholdValue = extractValueAfterColon(part);
            } else if (lowerPart.contains("range:") || lowerPart.contains("time:")) {
                String rangeVal = extractValueAfterColon(part);
                if (rangeVal.contains("-")) {
                    String[] rangeParts = rangeVal.split("-");
                    rangeStart = rangeParts[0].trim();
                    rangeEnd = rangeParts.length > 1 ? rangeParts[1].trim() : "";
                    thresholdValue = rangeVal;
                } else {
                    thresholdValue = rangeVal;
                }
            } else if (lowerPart.contains("appliedday:") || lowerPart.contains("applied_day:")) {
                // If appliedDay is specified, use it (overrides default)
                List<Integer> extractedDays = extractAppliedDays(part);
                if (!extractedDays.isEmpty()) {
                    appliedDays = extractedDays;
                }
            }
        }

        // If appliedDays is still empty, set fallback based on rule type
        if (appliedDays.isEmpty()) {
            if (normalizedKey.equals("WeeklyOT")) {
                // Only WeeklyOT defaults to all 7 days if not specified
                appliedDays = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
            } else {
                // All other rules (DailyOT, SpecificRange, BeforeShift, AfterShift, etc.) use
                // defaultWorkDayIds
                appliedDays = new ArrayList<>(defaultWorkDayIds);
            }
        }

        return new ParsedRule(normalizedKey, ruleTypeName, payMultiplier, billMultiplier,
                payFixedRate, billFixedRate, thresholdValue, rangeStart, rangeEnd, appliedDays, isFixedRate);
    }

    private List<String> splitByCommaOutsideBrackets(String input) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '[') {
                depth++;
                current.append(c);
            } else if (c == ']') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0)
            result.add(current.toString());
        return result;
    }

    private String normalizeRuleKey(String ruleKey) {
        String lower = ruleKey.toLowerCase().replaceAll("[\\s_]", "");
        if (lower.contains("aftershift") || lower.equals("after"))
            return "AfterShift";
        if (lower.contains("beforeshift") || lower.contains("beforerule") || lower.equals("before"))
            return "BeforeShift";
        // Removed RegularHours - it's not a rule, just base pay/bill rates
        if (lower.contains("dailyot") || lower.contains("dailyovertime"))
            return "DailyOT";
        if (lower.contains("weeklyot") || lower.contains("weeklyovertime"))
            return "WeeklyOT";
        if (lower.contains("specificrange") || lower.contains("specific"))
            return "SpecificRange";
        if (lower.contains("specifichours"))
            return "SpecificHoursRange";
        return ruleKey.trim();
    }

    // This method is no longer used - rule name is extracted directly in
    // parseRuleContent
    // Keeping for backward compatibility but it's not called anymore
    @Deprecated
    private String extractRuleTypeName(String normalizedKey, String content) {
        // First token in content might be the rule type name
        String firstToken = content.split(",")[0].trim();
        // Return as-is (preserve case) since it's the actual rule name from JSON
        if (!firstToken.contains(":")) {
            return firstToken;
        }
        // Derive from key if first token is a key-value pair
        return normalizedKey;
    }

    private double extractMultiplierValue(String part) {
        Pattern pattern = Pattern.compile("multiplier\\s*:\\s*(\\d+(?:\\.\\d+)?)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(part);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        return 1.0; // default fallback
    }

    private double extractNumericValue(String part) {
        Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(part);
        if (m.find())
            return Double.parseDouble(m.group(1));
        return 0.0;
    }

    private double[] extractFixedRatePair(String part) {
        Matcher m = Pattern.compile("\\$(\\d+(?:\\.\\d+)?)/\\$(\\d+(?:\\.\\d+)?)").matcher(part);
        if (m.find()) {
            return new double[] { Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)) };
        }
        return new double[] { 0.0, 0.0 };
    }

    private String extractValueAfterColon(String part) {
        int colonIdx = part.indexOf(':');
        if (colonIdx >= 0 && colonIdx < part.length() - 1) {
            return part.substring(colonIdx + 1).trim();
        }
        return "";
    }

    private List<Integer> extractAppliedDays(String part) {
        List<Integer> days = new ArrayList<>();
        // Extract content inside brackets: appliedDay:[mon,tue,wed] or appliedDay:[sat]
        // Handle both "appliedDay:[sat]" and "appliedDay: [sat]" (with space)
        Matcher m = Pattern.compile("appliedDay\\s*:\\s*\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE).matcher(part);
        if (m.find()) {
            String bracketContent = m.group(1).trim();
            String[] dayTokens = bracketContent.split(",");
            for (String token : dayTokens) {
                token = token.trim();
                if (token.isEmpty())
                    continue;
                Integer dayNum = DAY_TO_NUMBER.get(token.toLowerCase());
                if (dayNum != null && !days.contains(dayNum)) {
                    days.add(dayNum);
                }
            }
        } else {
            // Fallback: try to find any bracket content if the pattern above didn't match
            Matcher fallback = Pattern.compile("\\[([^\\]]+)\\]").matcher(part);
            if (fallback.find()) {
                String bracketContent = fallback.group(1).trim();
                String[] dayTokens = bracketContent.split(",");
                for (String token : dayTokens) {
                    token = token.trim();
                    if (token.isEmpty())
                        continue;
                    Integer dayNum = DAY_TO_NUMBER.get(token.toLowerCase());
                    if (dayNum != null && !days.contains(dayNum)) {
                        days.add(dayNum);
                    }
                }
            }
        }
        Collections.sort(days);
        return days;
    }

    // ========================================================================
    // SECTION 2: Convert ParsedRule list to API custom rules payload
    // ========================================================================

    /**
     * Converts a list of ParsedRule objects into the custom rules list for the rule
     * template API.
     */
    protected List<Map<String, Object>> buildCustomRulesFromParsedRules(List<ParsedRule> parsedRules,
            List<Integer> defaultWorkDayIds,
            double payRate, double billRate) {
        List<Map<String, Object>> customRules = new ArrayList<>();

        for (ParsedRule parsed : parsedRules) {
            // Skip RegularHours - handled by base pay/bill rates
            if (parsed.getRuleKey().equalsIgnoreCase("RegularHours")) {
                continue;
            }

            Map<String, Object> rule = new HashMap<>();
            rule.put(ID, 0);
            // Use the rule name from the first element in the JSON array
            rule.put(RULE_NAME, parsed.getRuleTypeName());

            // ALWAYS use appliedDays from the rule if specified (from appliedDay field in
            // JSON)
            // This allows rules to apply to any day, not just the default work days from
            // dayPattern
            // If appliedDays is empty, fallback logic: only WeeklyOT defaults to all 7 days
            String ruleKey = parsed.getRuleKey();

            List<Integer> dayIds;
            if (!parsed.getAppliedDays().isEmpty()) {
                // Priority 1: Use appliedDays from the rule (from appliedDay field in JSON)
                // This allows rules like BeforeShift/AfterShift to apply to weekends even if
                // not in dayPattern
                dayIds = new ArrayList<>(parsed.getAppliedDays());
            } else if (ruleKey.equals("WeeklyOT")) {
                // Priority 2: Only WeeklyOT defaults to all 7 days if not specified
                dayIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
            } else {
                // Priority 3: For all other rules (DailyOT, SpecificRange, BeforeShift,
                // AfterShift, etc.),
                // use default work days from dayPattern
                dayIds = new ArrayList<>(defaultWorkDayIds);
            }
            rule.put(WORK_DAY_ID, dayIds);

            rule.put(START_DURATION, 0);
            rule.put(END_DURATION, 0);
            rule.put(DAILY_THRESHOLD, 0);
            rule.put(WEEKLY_THRESHOLD, 0);

            // Configure charge method
            if (parsed.isFixedRate()) {
                rule.put(CHARGE_METHOD, FIXED_RATE_CHARGE);
                rule.put(PAY_RATE_MULTIPLIER, 1.0);
                rule.put(BILL_RATE_MULTIPLIER, 1.0);
                rule.put(PAY_RATE_PER_HOUR, parsed.getPayFixedRate());
                rule.put(BILL_RATE_PER_HOUR, parsed.getBillFixedRate());
            } else {
                rule.put(CHARGE_METHOD, MULTIPLIER_CHARGE);
                rule.put(PAY_RATE_PER_HOUR, 0.0);
                rule.put(BILL_RATE_PER_HOUR, 0.0);
                rule.put(PAY_RATE_MULTIPLIER, parsed.getPayMultiplier());
                rule.put(BILL_RATE_MULTIPLIER, parsed.getBillMultiplier());
            }

            // Configure rule type based on the normalized key
            configureRuleTypeFromParsed(rule, parsed);

            customRules.add(rule);
        }

        return customRules;
    }

    private void configureRuleTypeFromParsed(Map<String, Object> rule, ParsedRule parsed) {
        String key = parsed.getRuleKey();

        switch (key) {
            case "AfterShift":
                rule.put(RULE_TYPE, AFTER_SHIFT_RULE);
                rule.put(START_TIME, convertTimeStringToSeconds(parsed.getThresholdValue()));
                rule.put(END_TIME, 0);
                break;

            case "BeforeShift":
                rule.put(RULE_TYPE, BEFORE_SHIFT_RULE);
                rule.put(START_TIME, convertTimeStringToSeconds(parsed.getThresholdValue()));
                rule.put(END_TIME, 0);
                break;

            case "SpecificRange":
                rule.put(RULE_TYPE, SPECIFIC_RANGE_RULE);
                rule.put(START_TIME, convertTimeStringToSeconds(parsed.getRangeStart()));
                rule.put(END_TIME, convertTimeStringToSeconds(parsed.getRangeEnd()));
                break;

            case "DailyOT":
                rule.put(RULE_TYPE, DAILY_OVERTIME_SHIFT_RULE);
                rule.put(START_TIME, 0);
                rule.put(END_TIME, 0);
                rule.put(DAILY_THRESHOLD, extractThresholdSeconds(parsed.getThresholdValue()));
                break;

            case "WeeklyOT":
                rule.put(RULE_TYPE, WEEKLY_OVERTIME_SHIFT_RULE);
                rule.put(START_TIME, 0);
                rule.put(END_TIME, 0);
                // WORK_DAY_ID is already set in buildCustomRulesFromParsedRules - don't
                // override it here
                // It will use appliedDays from the rule, or default to all 7 days if not
                // specified
                rule.put(WEEKLY_THRESHOLD, extractThresholdSeconds(parsed.getThresholdValue()));
                break;

            case "SpecificHoursRange":
                rule.put(RULE_TYPE, SPECIFIC_HOURS_RANGE_RULE);
                rule.put(START_TIME, 0);
                rule.put(END_TIME, 0);
                rule.put(START_DURATION, extractThresholdSeconds(parsed.getRangeStart()));
                rule.put(END_DURATION, extractThresholdSeconds(parsed.getRangeEnd()));
                break;

            default:
                // Default to after shift
                rule.put(RULE_TYPE, AFTER_SHIFT_RULE);
                rule.put(START_TIME, 0);
                rule.put(END_TIME, 0);
                break;
        }
    }

    // ========================================================================
    // SECTION 3: WORK TIME PARSING - Parse multi-entry actualWorkTime and breakTime
    // ========================================================================

    /**
     * Parses the multi-entry actualWorkTime format into a map of week ->
     * WeekWorkData.
     * 
     * Format: "Week1: [Mon: 9:00-11:00; 13:00-15:00; 18:00-20:00, Tue: 9:00-17:00],
     * Week2: [Mon: 9:00-17:00]"
     * 
     * Each day can have multiple time entries separated by semicolons.
     * 
     * @param actualWorkTime the raw actual work time string
     * @return map of weekKey (e.g. "week1") -> WeekWorkData
     */
    protected Map<String, WeekWorkData> parseMultiEntryWorkTimes(String actualWorkTime) {
        Map<String, WeekWorkData> weekMap = new LinkedHashMap<>();
        if (actualWorkTime == null || actualWorkTime.trim().isEmpty()) {
            return weekMap;
        }

        String input = actualWorkTime.trim();

        // Check if it has Week prefixes
        if (input.toLowerCase().contains("week")) {
            // Pattern: Week1: [...], Week2: [...]
            Pattern weekPattern = Pattern.compile("(?i)week(\\d+)\\s*:\\s*\\[([^\\]]+)\\]");
            Matcher matcher = weekPattern.matcher(input);
            while (matcher.find()) {
                String weekKey = "week" + matcher.group(1);
                String weekContent = matcher.group(2);
                WeekWorkData weekData = parseWeekContent(weekContent);
                weekMap.put(weekKey, weekData);
            }
        } else {
            // No week prefix - treat as single week (week1)
            WeekWorkData weekData = parseWeekContent(input);
            weekMap.put("week1", weekData);
        }

        return weekMap;
    }

    /**
     * Parses the break time string into a map of week -> WeekWorkData (break
     * entries mapped to work entries).
     * 
     * Format: "Week1: [Mon: 9:00-11:00:[9:30-9:40; 10:10-10:40],
     * 13:00-15:00:[13:00-14:00], 18:00-20:00:[], Tue: 9:00-17:00:[10:00-11:00]]"
     * - Each work entry is followed by `:` and then `[break1; break2; ...]`
     * - Empty brackets `[]` if no breaks for that work entry
     * - Multiple breaks within a work entry are separated by semicolons (`;`)
     * - Multiple work entries are separated by commas (`,`)
     */
    protected Map<String, WeekWorkData> parseMultiEntryBreakTimes(String breakTime) {
        Map<String, WeekWorkData> weekMap = new LinkedHashMap<>();
        if (breakTime == null || breakTime.trim().isEmpty() || breakTime.equalsIgnoreCase("None")) {
            return weekMap;
        }

        String input = breakTime.trim();

        // Extract week content using bracket depth tracking
        Pattern weekPattern = Pattern.compile("(?i)week(\\d+)\\s*:\\s*");
        Matcher weekMatcher = weekPattern.matcher(input);
        while (weekMatcher.find()) {
            int weekStart = weekMatcher.end();
            String weekKey = "week" + weekMatcher.group(1);
            // Extract content between brackets, handling nested brackets
            String weekContent = extractBracketContent(input, weekStart);
            if (weekContent != null) {
                WeekWorkData weekData = parseWeekBreakContent(weekContent);
                weekMap.put(weekKey, weekData);
            }
        }

        return weekMap;
    }

    /**
     * Extracts content inside brackets, handling nested brackets.
     * If the outermost closing bracket is missing (e.g., unclosed Week bracket),
     * returns everything from the opening bracket to the end of string.
     */
    private String extractBracketContent(String input, int startPos) {
        if (startPos >= input.length() || input.charAt(startPos) != '[') {
            return null;
        }
        int depth = 0;
        for (int i = startPos; i < input.length(); i++) {
            if (input.charAt(i) == '[') {
                depth++;
            } else if (input.charAt(i) == ']') {
                depth--;
                if (depth == 0) {
                    return input.substring(startPos + 1, i);
                }
            }
        }
        // If we reach end of string without matching close bracket, return what we have
        // This handles cases like "Week1: [Mon:
        // 5:00-9:00:[6:30-7:30];9:00-17:00:[9:30-10:30]"
        // where the outer Week bracket is never closed
        return input.substring(startPos + 1);
    }

    /**
     * Parses content inside a week bracket for work times.
     * e.g. "Mon: 9:00-11:00; 13:00-15:00; 18:00-20:00, Tue: 9:00-17:00"
     */
    private WeekWorkData parseWeekContent(String weekContent) {
        WeekWorkData data = new WeekWorkData();
        // Split by comma to get each day's entries
        String[] dayEntries = weekContent.split(",");

        for (String dayEntry : dayEntries) {
            dayEntry = dayEntry.trim();
            if (dayEntry.isEmpty())
                continue;

            // Extract day name
            Matcher dayMatcher = Pattern.compile("(?i)^(mon|tue|wed|thu|fri|sat|sun)\\w*\\s*:?\\s*(.+)$")
                    .matcher(dayEntry);
            if (dayMatcher.find()) {
                String dayName = normalizeDayName(dayMatcher.group(1));
                String timeRanges = dayMatcher.group(2).trim();

                // Split by semicolon to get multiple time entries for the same day
                String[] ranges = timeRanges.split(";");
                for (String range : ranges) {
                    range = range.trim();
                    if (range.isEmpty())
                        continue;
                    // Parse time range like "9:00-11:00"
                    String[] times = range.split("-");
                    if (times.length == 2) {
                        int start = convertTimeStringToSeconds(times[0].trim());
                        int end = convertTimeStringToSeconds(times[1].trim());
                        if (start >= 0 && end > 0) {
                            data.addWorkEntry(dayName, new WorkTimeEntry(start, end));
                        }
                    }
                }
            }
        }
        return data;
    }

    /**
     * Parses content inside a week bracket for break times with work entry mapping.
     * 
     * Format: "Mon: 5:00-9:00:[6:30-7:30];9:00-17:00:[9:30-10:30], Tue:
     * 9:00-17:00:[10:00-11:00]"
     * - Each work entry (e.g., "5:00-9:00") is followed by `:` and then `[break
     * list]`
     * - Multiple breaks within a work entry are separated by semicolons (`;`)
     * - Empty brackets `[]` if no breaks for that work entry
     * - Multiple work entries on the same day are separated by semicolons (`;`)
     * - Different days are separated by commas (`,`)
     */
    private WeekWorkData parseWeekBreakContent(String weekContent) {
        WeekWorkData data = new WeekWorkData();

        // Split by comma at day level (not inside brackets)
        List<String> dayParts = splitByCommaOutsideBrackets(weekContent);

        for (String dayPart : dayParts) {
            dayPart = dayPart.trim();
            if (dayPart.isEmpty())
                continue;

            // Extract day name
            Matcher dayMatcher = Pattern.compile("(?i)^(mon|tue|wed|thu|fri|sat|sun)\\w*\\s*:?\\s*(.+)$")
                    .matcher(dayPart);
            if (dayMatcher.find()) {
                String dayName = normalizeDayName(dayMatcher.group(1));
                String workEntriesStr = dayMatcher.group(2).trim();

                // Parse work entries with their breaks:
                // "5:00-9:00:[6:30-7:30];9:00-17:00:[9:30-10:30]"
                // Split by semicolon, but not semicolons inside brackets
                List<String> workEntryParts = splitBySemicolonOutsideBrackets(workEntriesStr);

                for (String workEntryPart : workEntryParts) {
                    workEntryPart = workEntryPart.trim();
                    if (workEntryPart.isEmpty())
                        continue;

                    // Pattern: "9:00-11:00:[9:30-9:40; 10:10-10:40]" or "9:00-11:00:[]"
                    Matcher workMatcher = Pattern.compile("(\\d{1,2}:\\d{2})-(\\d{1,2}:\\d{2})\\s*:\\s*\\[(.*?)\\]")
                            .matcher(workEntryPart);
                    if (workMatcher.find()) {
                        String workStart = workMatcher.group(1);
                        String workEnd = workMatcher.group(2);
                        String breaksStr = workMatcher.group(3).trim();

                        int workStartSec = convertTimeStringToSeconds(workStart);
                        int workEndSec = convertTimeStringToSeconds(workEnd);

                        if (workStartSec >= 0 && workEndSec > 0) {
                            WorkTimeEntry workEntry = new WorkTimeEntry(workStartSec, workEndSec);
                            data.addWorkEntry(dayName, workEntry);

                            // Parse breaks for this work entry
                            // Supports both ";" and "," as break separators inside the brackets
                            // e.g. "5:30-5:45;5:45-6:00" or "5:30-5:45,5:45-6:00"
                            List<BreakEntry> breaks = new ArrayList<>();
                            if (!breaksStr.isEmpty()) {
                                String[] breakRanges = breaksStr.split("[;,]");
                                for (String breakRange : breakRanges) {
                                    breakRange = breakRange.trim();
                                    if (breakRange.isEmpty())
                                        continue;
                                    Matcher breakMatcher = Pattern.compile(
                                            "(\\d{1,2}:\\d{2})-(\\d{1,2}:\\d{2})").matcher(breakRange);
                                    if (breakMatcher.find()) {
                                        int breakStart = convertTimeStringToSeconds(breakMatcher.group(1));
                                        int breakEnd = convertTimeStringToSeconds(breakMatcher.group(2));
                                        if (breakStart >= 0 && breakEnd > 0) {
                                            breaks.add(new BreakEntry(breakStart, breakEnd));
                                        }
                                    }
                                }
                            }

                            // Map breaks to this work entry using day-qualified key
                            String workKey = workStart + "-" + workEnd;
                            data.addBreaksForWorkEntry(dayName, workKey, breaks);
                        }
                    }
                }
            }
        }
        return data;
    }

    /**
     * Splits a string by semicolons that are NOT inside square brackets.
     * Used to split work entries within the same day (e.g.,
     * "5:00-9:00:[...];9:00-17:00:[...]")
     */
    private List<String> splitBySemicolonOutsideBrackets(String input) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '[') {
                depth++;
                current.append(c);
            } else if (c == ']') {
                depth--;
                current.append(c);
            } else if (c == ';' && depth == 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }

    // ========================================================================
    // SECTION 4: RULE TEMPLATE CREATION
    // ========================================================================

    /**
     * Creates a rule template using parsed rules from JSON.
     */
    protected Integer createRuleTemplateFromParsedRules(String authToken, String templateName,
            List<Integer> workDayIds, String regularHours,
            List<Map<String, Object>> customRules,
            String breakBillable, int workLogType,
            Integer breakTimeThreshold,
            Integer isUnplannedHoursPayEnabled) {
        try {
            Map<String, Integer> workTimes = parseTimeRange(regularHours);
            Integer workStartTime = workTimes.getOrDefault(START_TIME, DEFAULT_WORK_START_TIME);
            Integer workEndTime = workTimes.getOrDefault(END_TIME, DEFAULT_WORK_END_TIME);

            Map<String, Object> payload = new HashMap<>();
            payload.put(TEMPLATE_NAME, templateName);
            payload.put(WORK_LOG_TYPE, workLogType);

            // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
            int calculateBreakTimeValue = 0;
            payload.put(CALCULATE_BREAK_TIME, calculateBreakTimeValue);

            if (calculateBreakTimeValue == 0 && breakTimeThreshold != null && breakTimeThreshold > 0) {
                payload.put(BREAK_TIME_THRESHOLD, breakTimeThreshold);
            } else {
                payload.put(BREAK_TIME_THRESHOLD, 0);
            }

            payload.put(WORK_DAY_IDS, workDayIds);

            // Build work time arrays for selected work days
            List<Integer> workTimeList = new ArrayList<>();
            List<Integer> workStartTimeList = new ArrayList<>();
            List<Integer> workEndTimeList = new ArrayList<>();

            for (int i = 0; i < workDayIds.size(); i++) {
                if (workLogType == SHIFTS_LOGGING) {
                    workTimeList.add(0);
                    workStartTimeList.add(workStartTime);
                    workEndTimeList.add(workEndTime);
                } else if (workLogType == HOURS_METHOD) {
                    Integer workDuration = workEndTime - workStartTime;
                    workTimeList.add(workDuration);
                    workStartTimeList.add(0);
                    workEndTimeList.add(0);
                }
            }

            payload.put(WORK_TIME, workTimeList);
            payload.put(WORK_START_TIME, workStartTimeList);
            payload.put(WORK_END_TIME, workEndTimeList);
            payload.put(CUSTOM_RULES, customRules != null ? customRules : new ArrayList<>());
            payload.put("isUnplannedHoursPayEnabled",
                    (isUnplannedHoursPayEnabled != null && isUnplannedHoursPayEnabled == 1) ? 1 : 0);

            String jsonPayload = TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/rule-template",
                    authToken, null, true, jsonPayload);
            assertThat("Rule template creation should return 201", response.getStatusCode(), equalTo(HTTP_CREATED));

            return getTemplateIdByName(authToken, templateName);
        } catch (Exception e) {
            throw new AssertionError("Error creating rule template: " + e.getMessage(), e);
        }
    }

    protected Integer getTemplateIdByName(String authToken, String templateName) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            Response response = RestClient.doGet("JSON", timesheetBaseURL, "rule-engine/rule-template/list",
                    authToken, null, null, true);
            if (response.getStatusCode() != HTTP_OK)
                return null;
            JsonPath jsonPath = response.jsonPath();
            List<Map<String, Object>> templates = jsonPath.getList("data");
            if (templates == null || templates.isEmpty())
                return null;
            for (Map<String, Object> template : templates) {
                if (templateName.equals(template.get("templateName"))) {
                    return (Integer) template.get("id");
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    protected List<Map<String, Object>> getRulesFromTemplate(String authToken, Integer ruleTemplateId) {
        try {
            String endpoint = "rule-engine/rule-template/" + ruleTemplateId;
            Response response = RestClient.doGet("JSON", timesheetBaseURL, endpoint, authToken, null, null, true);
            if (response.getStatusCode() == HTTP_OK) {
                List<Map<String, Object>> customRules = response.jsonPath().getList("data.customRules");
                return (customRules != null && !customRules.isEmpty()) ? customRules : null;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // ========================================================================
    // SECTION 5: ENABLE TIMESHEET SETTINGS
    // ========================================================================

    /**
     * Enables timesheet settings with dynamic values from JSON test data.
     */
    protected Response enableTimesheetSettings(String authToken, Integer jobId, Integer candidateId,
            Integer userId, Integer ruleTemplateId,
            String dayPattern, String regularHours,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate,
            Integer timesheetFrequency, Integer timesheetStartDay,
            Integer payCurrencyId, Integer billCurrencyId,
            Integer breakTimeThreshold, int workLogType,
            Integer isUnplannedHoursPayEnabled) {
        try {
            List<Map<String, Object>> templateRules = getRulesFromTemplate(authToken, ruleTemplateId);
            List<Integer> workDayIds = parseWorkDaysFromPattern(dayPattern);
            Map<String, Integer> workTimes = parseTimeRange(regularHours);
            Integer workStartTime = workTimes.getOrDefault(START_TIME, DEFAULT_WORK_START_TIME);
            Integer workEndTime = workTimes.getOrDefault(END_TIME, DEFAULT_WORK_END_TIME);

            Map<String, Object> approvers = new HashMap<>();
            approvers.put("agencyIds", Arrays.asList(userId));
            approvers.put("clientIds", Arrays.asList());

            List<Integer> workTimeList = new ArrayList<>();
            List<Integer> workStartTimeList = new ArrayList<>();
            List<Integer> workEndTimeList = new ArrayList<>();

            for (int i = 0; i < workDayIds.size(); i++) {
                if (workLogType == SHIFTS_LOGGING) {
                    workTimeList.add(0);
                    workStartTimeList.add(workStartTime);
                    workEndTimeList.add(workEndTime);
                } else if (workLogType == HOURS_METHOD) {
                    Integer workDuration = workEndTime - workStartTime;
                    workTimeList.add(workDuration);
                    workStartTimeList.add(0);
                    workEndTimeList.add(0);
                }
            }

            Map<String, Object> settings = new HashMap<>();
            settings.put("jobId", jobId);
            settings.put("contractorIds", Arrays.asList(candidateId));
            settings.put("jobStartDate", jobStartDate);
            settings.put("jobEndDate", jobEndDate);
            settings.put("timesheetFrequency", timesheetFrequency);
            settings.put("timesheetStartDay", timesheetStartDay);
            settings.put("approvers", approvers);
            settings.put("payCurrencyId", payCurrencyId);
            settings.put("payRate", payRate);
            settings.put("billCurrencyId", billCurrencyId);
            settings.put("billRate", billRate);
            settings.put("workDayIds", workDayIds);
            settings.put("workLogType", workLogType);

            // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
            int calculateBreakTime = 0;
            settings.put("calculateBreakTime", calculateBreakTime);

            if (calculateBreakTime == 0 && breakTimeThreshold != null && breakTimeThreshold > 0) {
                settings.put(BREAK_TIME_THRESHOLD, breakTimeThreshold);
            } else {
                settings.put(BREAK_TIME_THRESHOLD, 0);
            }

            settings.put("workTime", workTimeList);
            settings.put("workStartTime", workStartTimeList);
            settings.put("workEndTime", workEndTimeList);
            settings.put("updatedOn", null);
            settings.put("updatedBy", null);
            settings.put("enabledOn", null);
            settings.put("enabledBy", null);
            settings.put("isPreferencesModified", 1);
            settings.put("isReimbursementEnabled", 0);
            settings.put("customRules", templateRules != null ? templateRules : new ArrayList<>());
            settings.put("isUnplannedHoursPayEnabled",
                    (isUnplannedHoursPayEnabled != null && isUnplannedHoursPayEnabled == 1) ? 1 : 0);

            String jsonPayload = TestUtil.getSerializedJSON(settings);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                    authToken, null, true, jsonPayload);
            assertThat("Timesheet settings should return 200", response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error enabling timesheet settings: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // SECTION 6: TIMESHEET CREATION & SLOTS
    // ========================================================================

    protected Response getFreeSlotsForTimesheet(String authToken, Integer candidateId,
            Long startDate, Long endDate,
            Integer timesheetFrequencyId, Integer timesheetStartDay) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put(CONTRACTOR_IDS, Arrays.asList(candidateId));
            payload.put(START_DATE, startDate);
            payload.put(END_DATE, endDate);
            payload.put(TIMESHEET_FREQUENCY_ID, timesheetFrequencyId);
            payload.put(TIMESHEET_START_DAY, timesheetStartDay);

            String jsonPayload = TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheets/free-slots",
                    authToken, null, true, jsonPayload);
            assertThat("Free slots should return 200", response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error getting free slots: " + e.getMessage(), e);
        }
    }

    protected Response createTimesheetFromSlots(String authToken, Integer jobId, Integer candidateId,
            Response freeSlotsResponse) {
        try {
            if (freeSlotsResponse == null || freeSlotsResponse.getStatusCode() != HTTP_OK) {
                throw new AssertionError("Invalid free slots response");
            }
            List<Map<String, Object>> slots = freeSlotsResponse.jsonPath().getList("data");
            if (slots == null || slots.isEmpty()) {
                throw new AssertionError("No available time slots found");
            }

            Map<String, Object> selectedSlot = slots.size() > 1 ? slots.get(1) : slots.get(0);
            Long startDate = ((Number) selectedSlot.get("startDate")).longValue();
            Long endDate = ((Number) selectedSlot.get("endDate")).longValue();

            Map<String, Object> payload = new HashMap<>();
            payload.put(CONTRACTOR_IDS, Arrays.asList(candidateId));
            Map<String, Object> dateRange = new HashMap<>();
            dateRange.put(START_DATE, startDate);
            dateRange.put(END_DATE, endDate);
            payload.put(TIMESHEET_DATES, Arrays.asList(dateRange));

            String endpoint = "timesheets/jobs/" + jobId + "/contractors";
            String jsonPayload = TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, endpoint,
                    authToken, null, true, jsonPayload);
            assertThat("Create timesheet should return 200", response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error creating timesheet: " + e.getMessage(), e);
        }
    }

    protected Response getTimesheetsForContractor(String authToken, Integer jobId, Integer contractorId) {
        try {
            String endpoint = String.format("timesheets/job/contractor/get?jobId=%d&contractorId=%d&page=1&size=100",
                    jobId, contractorId);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sortPriorityList", Arrays.asList());
            String jsonPayload = TestUtil.getSerializedJSON(requestBody);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, endpoint,
                    authToken, null, true, jsonPayload);
            assertThat("Get timesheets should return 200", response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error getting timesheets: " + e.getMessage(), e);
        }
    }

    protected Response getTimeLogsForTimesheet(String authToken, Integer timesheetId) {
        try {
            String endpoint = "timesheets/" + timesheetId + "/time-logs";
            return RestClient.doGet("JSON", timesheetBaseURL, endpoint, authToken, null, null, true);
        } catch (Exception e) {
            throw new AssertionError("Error getting time logs: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // SECTION 7: UPDATE TIME LOGS WITH MULTIPLE TIME ENTRIES (NEW FORMAT)
    // ========================================================================

    /**
     * Updates time logs using the new multiple time entry format with
     * workTimeDetails.
     * 
     * This builds the payload structure:
     * {
     * "isApproved": 0,
     * "save": false,
     * "timeLogs": [
     * {
     * "id": <existing log id>,
     * "timesheetId": <id>,
     * "breakTime": <total break>,
     * "workTimeDetails": [
     * { "workStartTime": ..., "workEndTime": ..., "breakIntervals": [...] },
     * { "workStartTime": ..., "workEndTime": ..., "breakIntervals": [...] }
     * ],
     * "overTime": ...,
     * "totalTime": ...
     * }
     * ]
     * }
     */
    protected Response updateTimeLogsWithMultipleEntries(String authToken, Integer timesheetId,
            Response existingLogsResponse,
            Map<String, WeekWorkData> workTimesByWeek,
            Map<String, WeekWorkData> breakTimesByWeek,
            String regularHours, String breakBillable,
            Integer timesheetFrequency, Long startDate,
            Integer breakTimeThreshold,
            List<Integer> workDayIds,
            String comment) {
        return updateTimeLogsWithMultipleEntries(authToken, timesheetId, existingLogsResponse,
                workTimesByWeek, breakTimesByWeek, regularHours, breakBillable,
                timesheetFrequency, startDate, breakTimeThreshold, workDayIds, comment, null);
    }

    protected Response updateTimeLogsWithMultipleEntries(String authToken, Integer timesheetId,
            Response existingLogsResponse,
            Map<String, WeekWorkData> workTimesByWeek,
            Map<String, WeekWorkData> breakTimesByWeek,
            String regularHours, String breakBillable,
            Integer timesheetFrequency, Long startDate,
            Integer breakTimeThreshold,
            List<Integer> workDayIds,
            String comment,
            Map<Integer, Integer> overtimeByLogId) {
        try {
            JsonPath jsonPath = existingLogsResponse.jsonPath();
            Map<String, Object> data = jsonPath.getMap("data");
            List<Map<String, Object>> existingTimeLogs = (List<Map<String, Object>>) data.get("timeLogs");

            List<Map<String, Object>> updatedTimeLogs = new ArrayList<>();
            int totalWorkTimeSum = 0;
            int totalOvertimeSum = 0;

            for (int i = 0; i < existingTimeLogs.size(); i++) {
                Map<String, Object> timeLog = existingTimeLogs.get(i);
                Integer logId = (Integer) timeLog.get("id");
                String timesheetPeriod = timeLog.get("timesheetPeriod") != null
                        ? timeLog.get("timesheetPeriod").toString()
                        : "";

                int dayIndex = i;
                int weekNumber = (dayIndex / 7) + 1;
                String weekKey = "week" + weekNumber;
                int dayOfWeek = (dayIndex % 7) + 1;
                String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

                WeekWorkData weekWorkData = workTimesByWeek.get(weekKey);
                List<WorkTimeEntry> dayWorkEntries = (weekWorkData != null)
                        ? weekWorkData.getWorkEntriesForDay(dayName)
                        : new ArrayList<>();

                WeekWorkData weekBreakData = breakTimesByWeek != null ? breakTimesByWeek.get(weekKey) : null;

                if (!dayWorkEntries.isEmpty()) {
                    Map<String, Object> log = buildMultiEntryTimeLog(logId, timesheetId,
                            timesheetPeriod, dayName, dayWorkEntries, weekBreakData,
                            breakBillable, comment);

                    int apiOvertime = (overtimeByLogId != null)
                            ? overtimeByLogId.getOrDefault(logId, 0)
                            : 0;
                    log.put(OVER_TIME, apiOvertime);

                    updatedTimeLogs.add(log);
                    totalWorkTimeSum += toInt(log.get(TOTAL_TIME));
                    totalOvertimeSum += toInt(log.get(OVER_TIME));
                } else {
                    Map<String, Object> emptyLog = buildEmptyTimeLog(logId, timesheetId, timesheetPeriod);
                    updatedTimeLogs.add(emptyLog);
                }
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("timeLogs", updatedTimeLogs);

            Map<String, Object> timeDetail = new HashMap<>();
            timeDetail.put(TIMESHEET_ID, timesheetId);
            timeDetail.put("totalWorkTime", totalWorkTimeSum);
            timeDetail.put("totalOvertime", totalOvertimeSum);
            timeDetail.put(TOTAL_TIME, totalWorkTimeSum);
            List<Map<String, Object>> timeDetails = new ArrayList<>();
            timeDetails.add(timeDetail);
            payload.put("timeDetails", timeDetails);

            payload.put("timesheetIdNoLogChanges", new ArrayList<>());
            payload.put("isApproved", 0);
            payload.put("save", 0);

            String jsonPayload = TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPatchOnce("JSON", timesheetBaseURL, "timesheets/bulk/time-logs",
                    authToken, null, true, jsonPayload);
            assertThat("Update time logs should return 200", response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error updating time logs with multiple entries: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a single time log entry with workTimeDetails for multiple time entries
     * on the same day.
     * 
     * @param dayName       the day name (e.g., "mon") used for day-qualified break
     *                      lookup
     * @param weekBreakData contains mapped breaks keyed by day + work time range
     */
    private Map<String, Object> buildMultiEntryTimeLog(Integer logId, Integer timesheetId,
            String timesheetPeriod,
            String dayName,
            List<WorkTimeEntry> workEntries,
            WeekWorkData weekBreakData,
            String breakBillable,
            String comment) {
        Map<String, Object> log = new HashMap<>();
        log.put(ID, logId);
        log.put(TIMESHEET_ID, timesheetId);
        log.put("timesheetPeriod", timesheetPeriod);

        List<Map<String, Object>> workTimeDetails = new ArrayList<>();
        int totalBreakTime = 0;

        for (int w = 0; w < workEntries.size(); w++) {
            WorkTimeEntry workEntry = workEntries.get(w);
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", null);
            detail.put(WORK_START_TIME, workEntry.getWorkStartTime());
            detail.put(WORK_END_TIME, workEntry.getWorkEndTime());
            detail.put("rangeBasedRemark", comment);
            detail.put("rangeBasedBreakTime", null);

            List<BreakEntry> entryBreaks = new ArrayList<>();
            if (weekBreakData != null) {
                entryBreaks = weekBreakData.getBreaksForWorkEntry(dayName, workEntry);
            }

            List<Map<String, Object>> entryBreakIntervals = new ArrayList<>();
            for (int i = 0; i < entryBreaks.size(); i++) {
                BreakEntry brk = entryBreaks.get(i);
                Map<String, Object> breakInterval = new HashMap<>();
                breakInterval.put("id", i + 1);
                breakInterval.put("breakStartTime", brk.getBreakStartTime());
                breakInterval.put("breakEndTime", brk.getBreakEndTime());
                entryBreakIntervals.add(breakInterval);
                totalBreakTime += brk.getDuration();
            }

            detail.put(BREAK_INTERVALS, entryBreakIntervals);
            workTimeDetails.add(detail);
        }

        log.put("workTimeDetails", workTimeDetails);

        int totalWorkDuration = 0;
        for (WorkTimeEntry entry : workEntries) {
            totalWorkDuration += entry.getDuration();
        }

        int totalTime = totalWorkDuration;
        if (breakBillable != null && breakBillable.equalsIgnoreCase("No") && totalBreakTime > 0) {
            totalTime -= totalBreakTime;
        }

        log.put(BREAK_TIME, totalBreakTime);
        log.put(OVER_TIME, 0);
        log.put(TOTAL_TIME, totalTime);

        return log;
    }

    private Map<String, Object> buildEmptyTimeLog(Integer logId, Integer timesheetId,
            String timesheetPeriod) {
        Map<String, Object> emptyLog = new HashMap<>();
        emptyLog.put(ID, logId);
        emptyLog.put(TIMESHEET_ID, timesheetId);
        emptyLog.put("timesheetPeriod", timesheetPeriod);
        emptyLog.put("workTimeDetails", new ArrayList<>());
        emptyLog.put(TOTAL_TIME, 0);
        emptyLog.put(OVER_TIME, -1);
        return emptyLog;
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    // ========================================================================
    // SECTION 7B: EVALUATE OVERTIME VIA API
    // ========================================================================

    /**
     * Parses the overtimeHours data-provider string into a structured map.
     * Format: "week1:[Mon:2, Tue:2], week2:[Mon:3]"
     * Returns: {week1: {mon: 2.0, tue: 2.0}, week2: {mon: 3.0}}
     */
    protected Map<String, Map<String, Double>> parseOvertimeHours(String overtimeHours) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        if (overtimeHours == null || overtimeHours.trim().isEmpty()) {
            return result;
        }

        Pattern weekPattern = Pattern.compile("(week\\d+):\\[([^\\]]+)\\]");
        Matcher weekMatcher = weekPattern.matcher(overtimeHours);
        while (weekMatcher.find()) {
            String weekKey = weekMatcher.group(1).toLowerCase();
            String dayEntries = weekMatcher.group(2);
            Map<String, Double> dayMap = new LinkedHashMap<>();

            Pattern dayPattern = Pattern.compile("(\\w+):(\\d+\\.?\\d*)");
            Matcher dayMatcher = dayPattern.matcher(dayEntries);
            while (dayMatcher.find()) {
                String dayName = dayMatcher.group(1).toLowerCase();
                double hours = Double.parseDouble(dayMatcher.group(2));
                dayMap.put(dayName, hours);
            }
            result.put(weekKey, dayMap);
        }
        return result;
    }

    /**
     * Parses the weeklyOvertimeHours data-provider string into a map of week ->
     * hours.
     * Format: "week1:5, week2:5" or "week1:7.5"
     * Returns: {week1: 5.0, week2: 5.0}
     */
    protected Map<String, Double> parseWeeklyOvertimeHours(String weeklyOvertimeHours) {
        Map<String, Double> result = new LinkedHashMap<>();
        if (weeklyOvertimeHours == null || weeklyOvertimeHours.trim().isEmpty()) {
            return result;
        }
        Pattern pattern = Pattern.compile("(week\\d+):(\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(weeklyOvertimeHours.toLowerCase());
        while (matcher.find()) {
            result.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
        }
        return result;
    }

    /**
     * Builds the payload for the evaluate-overtime API from existing time logs.
     * Mirrors the bulk time-logs PATCH payload structure, including break
     * intervals.
     */
    private List<Map<String, Object>> buildEvaluateOvertimePayload(
            List<Map<String, Object>> existingTimeLogs, Integer timesheetId,
            Map<String, WeekWorkData> workTimesByWeek,
            Map<String, WeekWorkData> breakTimesByWeek, String comment) {

        List<Map<String, Object>> timeLogs = new ArrayList<>();
        for (int i = 0; i < existingTimeLogs.size(); i++) {
            Map<String, Object> timeLog = existingTimeLogs.get(i);
            Integer logId = (Integer) timeLog.get("id");
            String timesheetPeriod = timeLog.get("timesheetPeriod") != null
                    ? timeLog.get("timesheetPeriod").toString()
                    : "";
            Object dateObj = timeLog.get("date");
            long dateEpoch = (dateObj instanceof Number) ? ((Number) dateObj).longValue() : 0L;

            int dayIndex = i;
            int dayOfWeek = (dayIndex % 7) + 1;
            String weekKey = "week" + ((dayIndex / 7) + 1);
            String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

            int dayTypeId = Integer.parseInt(timeLog.get("dayTypeId").toString());

            WeekWorkData weekWorkData = workTimesByWeek.get(weekKey);
            List<WorkTimeEntry> dayWorkEntries = (weekWorkData != null)
                    ? weekWorkData.getWorkEntriesForDay(dayName)
                    : new ArrayList<>();

            WeekWorkData weekBreakData = breakTimesByWeek != null ? breakTimesByWeek.get(weekKey) : null;

            List<Map<String, Object>> workTimeDetails = new ArrayList<>();
            for (WorkTimeEntry entry : dayWorkEntries) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("id", null);
                detail.put(WORK_START_TIME, entry.getWorkStartTime());
                detail.put(WORK_END_TIME, entry.getWorkEndTime());
                detail.put("rangeBasedRemark", comment);
                detail.put("rangeBasedBreakTime", null);

                List<BreakEntry> entryBreaks = new ArrayList<>();
                if (weekBreakData != null) {
                    entryBreaks = weekBreakData.getBreaksForWorkEntry(dayName, entry);
                }

                List<Map<String, Object>> breakIntervals = new ArrayList<>();
                for (int b = 0; b < entryBreaks.size(); b++) {
                    BreakEntry brk = entryBreaks.get(b);
                    Map<String, Object> breakInterval = new HashMap<>();
                    breakInterval.put("id", b + 1);
                    breakInterval.put("breakStartTime", brk.getBreakStartTime());
                    breakInterval.put("breakEndTime", brk.getBreakEndTime());
                    breakIntervals.add(breakInterval);
                }

                detail.put(BREAK_INTERVALS, breakIntervals);
                workTimeDetails.add(detail);
            }

            Map<String, Object> log = new HashMap<>();
            log.put(ID, logId);
            log.put(TIMESHEET_ID, timesheetId);
            log.put("timesheetPeriod", timesheetPeriod);
            log.put("date", dateEpoch);
            log.put("dayTypeId", dayTypeId);
            log.put("workTimeDetails", workTimeDetails);
            timeLogs.add(log);
        }
        return timeLogs;
    }

    /**
     * Calls the evaluate-overtime API and returns the response.
     * POST /v1/rule-engine/evaluate-overtime
     */
    protected Response callEvaluateOvertimeApi(String authToken,
            List<Map<String, Object>> evaluatePayloadTimeLogs) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("timeLogs", evaluatePayloadTimeLogs);
            payload.put("scope", "single_timesheet");

            String jsonPayload = TestUtil.getSerializedJSON(payload);
            return RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/evaluate-overtime",
                    authToken, null, true, jsonPayload);
        } catch (Exception e) {
            throw new AssertionError("Error calling evaluate-overtime API: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the evaluate-overtime API response into a map:
     * {timesheetId -> {timeLogId -> overtimeInSeconds}}
     */
    protected Map<Integer, Map<Integer, Integer>> parseOvertimeApiResponse(Response response) {
        Map<Integer, Map<Integer, Integer>> result = new HashMap<>();
        List<Map<String, Object>> dataList = response.jsonPath().getList("data");
        if (dataList == null) {
            return result;
        }
        for (Map<String, Object> entry : dataList) {
            Integer tsId = ((Number) entry.get("timesheetId")).intValue();
            List<Map<String, Object>> logs = (List<Map<String, Object>>) entry.get("timeLogs");
            Map<Integer, Integer> logMap = new HashMap<>();
            if (logs != null) {
                for (Map<String, Object> logEntry : logs) {
                    Integer timeLogId = ((Number) logEntry.get("timeLogId")).intValue();
                    Integer overtimeInSeconds = ((Number) logEntry.get("overtimeInSeconds")).intValue();
                    logMap.put(timeLogId, overtimeInSeconds);
                }
            }
            result.put(tsId, logMap);
        }
        return result;
    }

    /**
     * Asserts that the overtime from the evaluate-overtime API matches the expected
     * overtimeHours from the data provider.
     * Days not specified in expectedOvertimeHours are asserted as 0 seconds.
     * Hours from data provider are converted to seconds (hours * 3600).
     */
    protected void assertOvertimeFromApi(String testId,
            Map<Integer, Map<Integer, Integer>> overtimeApiMap,
            Map<String, Map<String, Double>> expectedOvertimeHours,
            List<Map<String, Object>> existingTimeLogs,
            Integer timesheetId) {

        Map<Integer, Integer> logOvertimeMap = overtimeApiMap.getOrDefault(timesheetId, new HashMap<>());

        for (int i = 0; i < existingTimeLogs.size(); i++) {
            Integer logId = (Integer) existingTimeLogs.get(i).get("id");
            int dayIndex = i;
            int weekNumber = (dayIndex / 7) + 1;
            String weekKey = "week" + weekNumber;
            int dayOfWeek = (dayIndex % 7) + 1;
            String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

            int expectedSeconds = 0;
            Map<String, Double> weekExpected = expectedOvertimeHours.get(weekKey);
            if (weekExpected != null && weekExpected.containsKey(dayName)) {
                expectedSeconds = (int) (weekExpected.get(dayName) * SECONDS_IN_HOUR);
            }

            int actualSeconds = logOvertimeMap.getOrDefault(logId, 0);
            assertThat("[" + testId + "] Overtime for " + weekKey + ":" + dayName
                    + " (logId=" + logId + ")", actualSeconds, equalTo(expectedSeconds));
        }
    }

    /**
     * Builds the overtime map for use in updateTimeLogsWithMultipleEntries.
     * Maps each timeLogId to its overtime in seconds from the API response.
     * Returns empty map if no data found (all overtime defaults to 0).
     */
    protected Map<Integer, Integer> buildOvertimeMapForTimelogs(
            Map<Integer, Map<Integer, Integer>> overtimeApiMap, Integer timesheetId) {
        if (overtimeApiMap == null || overtimeApiMap.isEmpty()) {
            return new HashMap<>();
        }
        return overtimeApiMap.getOrDefault(timesheetId, new HashMap<>());
    }

    // ========================================================================
    // SECTION 8: APPROVE, EVALUATE, VALIDATE
    // ========================================================================

    protected Response approveTimesheet(String authToken, Integer timesheetId) {
        try {
            String endpoint = "timesheets/" + timesheetId + "/status";
            Map<String, Object> payload = new HashMap<>();
            payload.put("approvalStatus", TIMESHEET_APPROVED_STATUS);
            String jsonPayload = TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, endpoint,
                    authToken, null, true, jsonPayload);
            assertThat("Approve timesheet should return 201", response.getStatusCode(), equalTo(HTTP_CREATED));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error approving timesheet: " + e.getMessage(), e);
        }
    }

    protected Response evaluateTimesheet(String authToken, Integer timesheetId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("timesheetId", timesheetId);
            String jsonPayload = TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/evaluate",
                    authToken, null, true, jsonPayload);
            assertThat("Evaluation should return 200", response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error evaluating timesheet: " + e.getMessage(), e);
        }
    }

    protected void validateTotalPayAndBill(String testId, Response evaluationResponse,
            double expectedPay, double expectedBill) {
        try {
            JsonPath evalJsonPath = evaluationResponse.jsonPath();
            Double actualPay = evalJsonPath.getDouble("data.evaluationSummary.totalPayAmount");
            Double actualBill = evalJsonPath.getDouble("data.evaluationSummary.totalBillAmount");

            boolean payOk = Math.abs(actualPay - expectedPay) <= AMOUNT_TOLERANCE;
            boolean billOk = Math.abs(actualBill - expectedBill) <= AMOUNT_TOLERANCE;

            assertThat(String.format(
                    "Mismatch for %s: expected Pay $%.2f / Bill $%.2f, actual Pay $%.2f / Bill $%.2f",
                    testId, expectedPay, expectedBill, actualPay, actualBill),
                    payOk && billOk, is(true));
        } catch (Exception e) {
            throw new AssertionError("Error validating calculations for " + testId + ": " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // SECTION 8A-COMBINED: COMBINED VALIDATION (Total + Per-Rule + Weekly OT)
    // ========================================================================

    /**
     * Combined validation that checks ALL assertions together:
     * 1. Final total pay & bill (expectedTotalPayRate / expectedTotalBillRate)
     * 2. Per-day per-rule amounts (acceptedPayBillRate)
     * 3. Weekly overtime amounts (weeklyOT)
     *
     * All mismatches are collected into a SINGLE error message so the developer
     * sees the complete picture on failure — both the totals AND the per-rule
     * breakdown — without needing to fix one to discover the next.
     */
    protected void validateAllAmounts(String testId, Response evaluationResponse,
            double expectedPay, double expectedBill,
            String acceptedPayBillRateStr, String weeklyOTStr,
            Integer isUnplannedHoursPayEnabled) {
        StringBuilder allErrors = new StringBuilder();

        try {
            JsonPath evalJsonPath = evaluationResponse.jsonPath();

            // ── 1. Total pay & bill ──────────────────────────────────────────
            Double actualPay = evalJsonPath.getDouble("data.evaluationSummary.totalPayAmount");
            Double actualBill = evalJsonPath.getDouble("data.evaluationSummary.totalBillAmount");

            boolean totalPayOk = Math.abs(actualPay - expectedPay) <= AMOUNT_TOLERANCE;
            boolean totalBillOk = Math.abs(actualBill - expectedBill) <= AMOUNT_TOLERANCE;

            if (!totalPayOk || !totalBillOk) {
                allErrors.append(String.format(
                        "\n\n  ✗ TOTAL MISMATCH: expected Pay=$%.2f / Bill=$%.2f, actual Pay=$%.2f / Bill=$%.2f",
                        expectedPay, expectedBill, actualPay, actualBill));
            } else {
                allErrors.append(String.format(
                        "\n\n  ✓ TOTAL OK: Pay=$%.2f, Bill=$%.2f", actualPay, actualBill));
            }

            // ── 2. Per-day per-rule amounts ──────────────────────────────────
            if (acceptedPayBillRateStr != null && !acceptedPayBillRateStr.trim().isEmpty()) {
                Map<String, Map<String, double[]>> expectedPerDayPerRule = parseAcceptedPayBillRate(
                        acceptedPayBillRateStr);

                // Build actual per-day per-rule map from response
                Map<String, Map<String, double[]>> actualPerDayPerRule = buildActualPerDayPerRuleMap(evalJsonPath);

                boolean anyRuleMismatch = false;
                StringBuilder ruleDetails = new StringBuilder();

                for (Map.Entry<String, Map<String, double[]>> dayEntry : expectedPerDayPerRule.entrySet()) {
                    String day = dayEntry.getKey();
                    Map<String, double[]> expectedRules = dayEntry.getValue();
                    Map<String, double[]> actualRules = actualPerDayPerRule.getOrDefault(day, new LinkedHashMap<>());

                    for (Map.Entry<String, double[]> ruleEntry : expectedRules.entrySet()) {
                        String ruleKey = ruleEntry.getKey();

                        // Skip unallocatedHours validation if isUnplannedHoursPayEnabled is not 1
                        if ("unallocatedHours".equals(ruleKey)
                                && (isUnplannedHoursPayEnabled == null || isUnplannedHoursPayEnabled != 1)) {
                            continue;
                        }

                        double[] expected = ruleEntry.getValue();
                        double[] actual = actualRules.getOrDefault(ruleKey, new double[] { 0.0, 0.0 });

                        boolean payOk = Math.abs(actual[0] - expected[0]) <= AMOUNT_TOLERANCE;
                        boolean billOk = Math.abs(actual[1] - expected[1]) <= AMOUNT_TOLERANCE;

                        if (!payOk || !billOk) {
                            anyRuleMismatch = true;
                            ruleDetails.append(String.format(
                                    "\n      ✗ %s -> %s: expected Pay=%.2f/Bill=%.2f, actual Pay=%.2f/Bill=%.2f",
                                    day.toUpperCase(), ruleKey, expected[0], expected[1], actual[0], actual[1]));
                        } else {
                            ruleDetails.append(String.format(
                                    "\n      ✓ %s -> %s: Pay=%.2f/Bill=%.2f",
                                    day.toUpperCase(), ruleKey, actual[0], actual[1]));
                        }
                    }
                }

                if (anyRuleMismatch) {
                    allErrors.append("\n\n  ✗ PER-RULE BREAKDOWN (has mismatches):");
                } else {
                    allErrors.append("\n\n  ✓ PER-RULE BREAKDOWN (all match):");
                }
                allErrors.append(ruleDetails);
            }

            // ── 3. Weekly overtime ───────────────────────────────────────────
            if (weeklyOTStr != null && !weeklyOTStr.trim().isEmpty()) {
                Map<String, double[]> expectedWeeklyOT = parseWeeklyOT(weeklyOTStr);
                List<Map<String, Object>> weeklyResults = evalJsonPath.getList("data.weeklyResults");

                Map<String, double[]> actualWeeklyOT = new LinkedHashMap<>();
                for (int i = 0; i < weeklyResults.size(); i++) {
                    Map<String, Object> weekResult = weeklyResults.get(i);
                    Map<String, Object> weeklyOvertime = (Map<String, Object>) weekResult.get("weeklyOvertimeResult");
                    double otPay = 0.0;
                    double otBill = 0.0;
                    if (weeklyOvertime != null) {
                        otPay = getDoubleValue(weeklyOvertime.get("weeklyOvertimePayAmount"));
                        otBill = getDoubleValue(weeklyOvertime.get("weeklyOvertimeBillAmount"));
                    }
                    String weekKey = "week" + (i + 1);
                    actualWeeklyOT.put(weekKey, new double[] { otPay, otBill });
                }

                boolean anyWotMismatch = false;
                StringBuilder wotDetails = new StringBuilder();

                for (Map.Entry<String, double[]> entry : expectedWeeklyOT.entrySet()) {
                    String weekKey = entry.getKey();
                    double[] expected = entry.getValue();
                    double[] actual = actualWeeklyOT.getOrDefault(weekKey, new double[] { 0.0, 0.0 });

                    boolean payOk = Math.abs(actual[0] - expected[0]) <= AMOUNT_TOLERANCE;
                    boolean billOk = Math.abs(actual[1] - expected[1]) <= AMOUNT_TOLERANCE;

                    if (!payOk || !billOk) {
                        anyWotMismatch = true;
                        wotDetails.append(String.format(
                                "\n      ✗ %s: expected Pay=%.2f/Bill=%.2f, actual Pay=%.2f/Bill=%.2f",
                                weekKey, expected[0], expected[1], actual[0], actual[1]));
                    } else {
                        wotDetails.append(String.format(
                                "\n      ✓ %s: Pay=%.2f/Bill=%.2f", weekKey, actual[0], actual[1]));
                    }
                }

                if (anyWotMismatch) {
                    allErrors.append("\n\n  ✗ WEEKLY OT (has mismatches):");
                } else {
                    allErrors.append("\n\n  ✓ WEEKLY OT (all match):");
                }
                allErrors.append(wotDetails);
            }

            // ── Final verdict ────────────────────────────────────────────────
            boolean hasAnyFailure = allErrors.toString().contains("✗");
            if (hasAnyFailure) {
                throw new AssertionError("[" + testId + "] Calculation validation FAILED:"
                        + allErrors);
            }

        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError("[" + testId + "] Error during combined validation: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Helper: builds the actual per-day per-rule amounts map from the evaluation
     * response.
     * Reusable by both the combined validator and the standalone per-rule
     * validator.
     *
     * For weekly (single weeklyResult) the key is just the day abbreviation, e.g.
     * "mon".
     * For biweekly (multiple weeklyResults) the key is prefixed with the week
     * index,
     * e.g. "week1:mon", "week2:tue", matching the "Week1:Mon:[...]" format in the
     * JSON.
     */
    private Map<String, Map<String, double[]>> buildActualPerDayPerRuleMap(JsonPath evalJsonPath) {
        Map<String, Map<String, double[]>> actualPerDayPerRule = new LinkedHashMap<>();

        List<Map<String, Object>> weeklyResults = evalJsonPath.getList("data.weeklyResults");
        boolean isMultiWeek = weeklyResults.size() > 1;

        for (int weekIdx = 0; weekIdx < weeklyResults.size(); weekIdx++) {
            Map<String, Object> weekResult = weeklyResults.get(weekIdx);
            List<Map<String, Object>> timeLogEvaluations = (List<Map<String, Object>>) weekResult
                    .get("timeLogRuleEvaluations");
            if (timeLogEvaluations == null)
                continue;

            for (Map<String, Object> timeLogEval : timeLogEvaluations) {
                String dateStr = (String) timeLogEval.get("date");
                String dayName = getDayNameFromDate(dateStr);
                // For biweekly prefix with week number so Week1:Mon and Week2:Mon are distinct
                // keys
                String dayKey = isMultiWeek ? "week" + (weekIdx + 1) + ":" + dayName : dayName;

                List<Map<String, Object>> ruleResults = (List<Map<String, Object>>) timeLogEval
                        .get("ruleEvaluationResults");
                if (ruleResults == null)
                    continue;

                for (Map<String, Object> ruleResult : ruleResults) {
                    String ruleType = (String) ruleResult.get("ruleType");
                    if (ruleType == null)
                        continue;

                    String jsonKey = RULE_TYPE_TO_JSON_KEY.get(ruleType);
                    if (jsonKey == null)
                        continue;

                    Double payAmount = getDoubleValue(ruleResult.get("payAmount"));
                    Double billAmount = getDoubleValue(ruleResult.get("billAmount"));

                    actualPerDayPerRule
                            .computeIfAbsent(dayKey, k -> new LinkedHashMap<>())
                            .merge(jsonKey, new double[] { payAmount, billAmount },
                                    (existing, incoming) -> new double[] {
                                            existing[0] + incoming[0],
                                            existing[1] + incoming[1]
                                    });
                }
            }
        }
        return actualPerDayPerRule;
    }

    // ========================================================================
    // SECTION 8B: PER-DAY PER-RULE VALIDATION (acceptedPayBillRate + weeklyOT)
    // ========================================================================

    /**
     * Maps API ruleType values to JSON acceptedPayBillRate keys.
     * The API returns ruleType like "RANGE_BASED_BEFORE_SHIFT" and we need to match
     * it to the JSON key like "beforeShift".
     */
    private static final Map<String, String> RULE_TYPE_TO_JSON_KEY = new HashMap<>();
    static {
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_BEFORE_SHIFT", "beforeShift");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_AFTER_SHIFT", "afterShift");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_SPECIFIC_TIME_RANGE", "specificHoursRange");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_DAILY_OVERTIME", "dailyOT");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_REGULAR_HOURS", "regularHours");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_BREAK", "Break");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_DEFAULT_PAY", "unallocatedHours");
    }

    /**
     * Parses the acceptedPayBillRate string from JSON into a per-day, per-rule
     * structure.
     * 
     * Format: "Mon:[beforeShift:[8,16], regularHours:[7,14], dailyOT:[3,6]],
     * Tue:[...]"
     * 
     * @param acceptedPayBillRate the raw string from JSON
     * @return Map of dayName (lowercase, e.g. "mon") -> Map of ruleKey (e.g.
     *         "beforeShift") -> double[]{pay, bill}
     */
    protected Map<String, Map<String, double[]>> parseAcceptedPayBillRate(String acceptedPayBillRate) {
        Map<String, Map<String, double[]>> result = new LinkedHashMap<>();
        if (acceptedPayBillRate == null || acceptedPayBillRate.trim().isEmpty()) {
            return result;
        }

        String input = acceptedPayBillRate.trim();

        // Split by day entries: "Mon:[...], Tue:[...]"
        // We need to split at the top level (depth 0) by comma followed by a day name
        List<String> dayEntries = splitDayEntries(input);

        for (String dayEntry : dayEntries) {
            dayEntry = dayEntry.trim();
            if (dayEntry.isEmpty())
                continue;

            // Extract day name and content: "Mon:[beforeShift:[8,16], regularHours:[7,14]]"
            int colonBracket = dayEntry.indexOf(":[");
            if (colonBracket == -1)
                continue;

            String dayName = dayEntry.substring(0, colonBracket).trim().toLowerCase();
            // Normalize day name
            String normalizedDay = DAY_ABBREVIATIONS.getOrDefault(dayName, dayName);

            // Extract content inside outer brackets
            String content = dayEntry.substring(colonBracket + 2); // skip ":["
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }

            Map<String, double[]> ruleAmounts = parseRuleAmountsFromDayContent(content);
            result.put(normalizedDay, ruleAmounts);
        }

        return result;
    }

    /**
     * Splits the acceptedPayBillRate string into individual day entries.
     * Handles nested brackets correctly.
     * e.g. "Mon:[beforeShift:[8,16], reg:[7,14]], Tue:[reg:[8,16]]"
     * -> ["Mon:[beforeShift:[8,16], reg:[7,14]]", "Tue:[reg:[8,16]]"]
     */
    private List<String> splitDayEntries(String input) {
        List<String> entries = new ArrayList<>();
        int bracketDepth = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '[') {
                bracketDepth++;
                current.append(c);
            } else if (c == ']') {
                bracketDepth--;
                current.append(c);
                // If we just closed the outermost bracket for a day, this day entry is complete
                if (bracketDepth == 0) {
                    entries.add(current.toString().trim());
                    current = new StringBuilder();
                    // Skip any comma/whitespace after closing bracket
                    while (i + 1 < input.length() && (input.charAt(i + 1) == ',' || input.charAt(i + 1) == ' ')) {
                        i++;
                    }
                }
            } else {
                current.append(c);
            }
        }
        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            entries.add(remaining);
        }
        return entries;
    }

    /**
     * Parses the rule amounts from inside a day's bracket content.
     * e.g. "beforeShift:[8,16], regularHours:[7,14], dailyOT:[3,6]"
     * -> {"beforeShift" -> [8.0, 16.0], "regularHours" -> [7.0, 14.0], "dailyOT" ->
     * [3.0, 6.0]}
     */
    private Map<String, double[]> parseRuleAmountsFromDayContent(String content) {
        Map<String, double[]> ruleAmounts = new LinkedHashMap<>();
        if (content == null || content.trim().isEmpty())
            return ruleAmounts;

        // Split by comma outside brackets to get individual rule entries
        // e.g. "beforeShift:[8,16]", "regularHours:[7,14]"
        List<String> ruleParts = new ArrayList<>();
        int bracketDepth = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') {
                bracketDepth++;
                current.append(c);
            } else if (c == ']') {
                bracketDepth--;
                current.append(c);
                if (bracketDepth == 0) {
                    ruleParts.add(current.toString().trim());
                    current = new StringBuilder();
                    // Skip comma/whitespace
                    while (i + 1 < content.length() && (content.charAt(i + 1) == ',' || content.charAt(i + 1) == ' ')) {
                        i++;
                    }
                }
            } else {
                current.append(c);
            }
        }
        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            ruleParts.add(remaining);
        }

        for (String rulePart : ruleParts) {
            rulePart = rulePart.trim();
            if (rulePart.isEmpty())
                continue;

            // Format: "beforeShift:[8,16]"
            int colonBracket = rulePart.indexOf(":[");
            if (colonBracket == -1)
                continue;

            String ruleKey = rulePart.substring(0, colonBracket).trim();
            String valuesStr = rulePart.substring(colonBracket + 2); // skip ":["
            if (valuesStr.endsWith("]")) {
                valuesStr = valuesStr.substring(0, valuesStr.length() - 1);
            }

            String[] values = valuesStr.split(",");
            if (values.length >= 2) {
                try {
                    double pay = Double.parseDouble(values[0].trim());
                    double bill = Double.parseDouble(values[1].trim());
                    ruleAmounts.put(ruleKey, new double[] { pay, bill });
                } catch (NumberFormatException e) {
                    // Skip invalid entry
                }
            }
        }

        return ruleAmounts;
    }

    /**
     * Parses the weeklyOT string from JSON.
     * Format: "Week1:[0,0]" or "Week1:[5,10], Week2:[3,6]"
     * Left value = payAmount, Right value = billAmount
     * 
     * @param weeklyOTStr the raw weeklyOT string from JSON
     * @return Map of weekKey (e.g. "week1") -> double[]{pay, bill}
     */
    protected Map<String, double[]> parseWeeklyOT(String weeklyOTStr) {
        Map<String, double[]> result = new LinkedHashMap<>();
        if (weeklyOTStr == null || weeklyOTStr.trim().isEmpty()) {
            return result;
        }

        // Split by entries like "Week1:[0,0], Week2:[3,6]"
        // Use same bracket-aware splitting
        String input = weeklyOTStr.trim();
        List<String> weekEntries = new ArrayList<>();
        int bracketDepth = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '[') {
                bracketDepth++;
                current.append(c);
            } else if (c == ']') {
                bracketDepth--;
                current.append(c);
                if (bracketDepth == 0) {
                    weekEntries.add(current.toString().trim());
                    current = new StringBuilder();
                    while (i + 1 < input.length() && (input.charAt(i + 1) == ',' || input.charAt(i + 1) == ' ')) {
                        i++;
                    }
                }
            } else {
                current.append(c);
            }
        }
        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            weekEntries.add(remaining);
        }

        for (String weekEntry : weekEntries) {
            weekEntry = weekEntry.trim();
            if (weekEntry.isEmpty())
                continue;

            // Format: "Week1:[0,0]"
            int colonBracket = weekEntry.indexOf(":[");
            if (colonBracket == -1)
                continue;

            String weekKey = weekEntry.substring(0, colonBracket).trim().toLowerCase();
            String valuesStr = weekEntry.substring(colonBracket + 2);
            if (valuesStr.endsWith("]")) {
                valuesStr = valuesStr.substring(0, valuesStr.length() - 1);
            }

            String[] values = valuesStr.split(",");
            if (values.length >= 2) {
                try {
                    double pay = Double.parseDouble(values[0].trim());
                    double bill = Double.parseDouble(values[1].trim());
                    result.put(weekKey, new double[] { pay, bill });
                } catch (NumberFormatException e) {
                    // Skip invalid entry
                }
            }
        }

        return result;
    }

    // NOTE: The standalone validatePerDayPerRuleAmounts() and
    // validateWeeklyOvertimeAmounts()
    // methods have been consolidated into validateAllAmounts() above.
    // The combined method collects ALL mismatches (total + per-rule + weekly OT)
    // into one report.

    /**
     * Converts a date string (e.g. "2025-07-01") to a lowercase day abbreviation
     * (e.g. "tue").
     */
    private String getDayNameFromDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty())
            return "mon";
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return date.getDayOfWeek().toString().substring(0, 3).toLowerCase();
        } catch (Exception e) {
            return "mon";
        }
    }

    private Double getDoubleValue(Object value) {
        if (value == null)
            return 0.0;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    // ========================================================================
    // SECTION 9: ENTITY CREATION HELPERS (reuse from ContractStaffingBaseTest)
    // ========================================================================

    protected Integer getRealCandidateId(String authToken, String candidateSlug) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("slug", candidateSlug);
            Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + candidateSlug + "/get",
                    authToken, null, true, requestBody);
            if (response.getStatusCode() == HTTP_OK) {
                Integer realId = response.jsonPath().getInt("data.candidate.id");
                if (realId != null)
                    return realId;
                throw new AssertionError("Could not find candidate ID at data.candidate.id");
            }
            throw new AssertionError("Failed to fetch candidate details. Status: " + response.getStatusCode());
        } catch (Exception e) {
            throw new AssertionError("Error fetching real candidate ID: " + e.getMessage(), e);
        }
    }

    protected Response assignCandidateToJob(String apiAuthToken, String candidateSlug, String jobSlug) {
        try {
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);
            Map<String, String> queryParameters = new HashMap<>();
            queryParameters.put("job_slug", jobSlug);
            Response response = RestClient.doPost1("JSON", baseURL, "candidates/{candidate}/assign",
                    apiAuthToken, queryParameters, pathParameters, true, null);
            assertThat("Assignment should return 200", response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error assigning candidate to job: " + e.getMessage(), e);
        }
    }

    /**
     * Setup method for timesheet preparation.
     * Creates candidate, company, contact, job, assigns candidate to job, and
     * returns all necessary IDs.
     * 
     * @param apiAuthToken       API authentication token
     * @param albatrossAuthToken Albatross authentication token
     * @param function           Common function helper for entity creation
     * @return TimesheetSetupResult containing all created entity slugs and IDs
     */
    protected TimesheetSetupResult setupForTimesheet(String apiAuthToken, String albatrossAuthToken,
            io.rcrm.api.commanfunctions.commanFunction function) {
        try {
            // Create candidate
            JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String candidateSlug = jsonCandidate.getString("slug");
            Integer realCandidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
            assertThat("Real candidate ID should be fetched", realCandidateId, notNullValue());

            // Create company
            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String companySlug = jsonCompany.getString("slug");

            // Create contact
            JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
            String contactSlug = jsonContact.getString("slug");

            // Create job
            JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
            String jobSlug = jsonJob.getString("slug");

            // Get user ID
            Response usersResponse = function.getUsers(baseURL, apiAuthToken);
            int userId = usersResponse.jsonPath().getInt("[0].id");

            // Get job ID
            int jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                    .jsonPath().getInt("data.job.id");

            // Assign candidate to job
            assignCandidateToJob(apiAuthToken, candidateSlug, jobSlug);

            return new TimesheetSetupResult(candidateSlug, realCandidateId, companySlug, contactSlug,
                    jobSlug, jobId, userId);
        } catch (Exception e) {
            throw new AssertionError("Error setting up timesheet entities: " + e.getMessage(), e);
        }
    }

    /**
     * Result class for timesheet setup containing all created entity information.
     */
    public static class TimesheetSetupResult {
        private final String candidateSlug;
        private final Integer candidateId;
        private final String companySlug;
        private final String contactSlug;
        private final String jobSlug;
        private final Integer jobId;
        private final Integer userId;

        public TimesheetSetupResult(String candidateSlug, Integer candidateId, String companySlug,
                String contactSlug, String jobSlug, Integer jobId, Integer userId) {
            this.candidateSlug = candidateSlug;
            this.candidateId = candidateId;
            this.companySlug = companySlug;
            this.contactSlug = contactSlug;
            this.jobSlug = jobSlug;
            this.jobId = jobId;
            this.userId = userId;
        }

        public String getCandidateSlug() {
            return candidateSlug;
        }

        public Integer getCandidateId() {
            return candidateId;
        }

        public String getCompanySlug() {
            return companySlug;
        }

        public String getContactSlug() {
            return contactSlug;
        }

        public String getJobSlug() {
            return jobSlug;
        }

        public Integer getJobId() {
            return jobId;
        }

        public Integer getUserId() {
            return userId;
        }
    }

    // ========================================================================
    // SECTION 10: UTILITY METHODS
    // ========================================================================

    protected List<Integer> parseWorkDaysFromPattern(String dayPattern) {
        if (dayPattern == null || dayPattern.trim().isEmpty())
            return new ArrayList<>();
        String pattern = dayPattern.toLowerCase().trim();

        if (pattern.startsWith("[") && pattern.endsWith("]")) {
            String content = pattern.substring(1, pattern.length() - 1);
            String[] days = content.split(",");
            List<Integer> workDays = new ArrayList<>();
            for (String day : days) {
                Integer dayId = DAY_TO_NUMBER.get(day.trim());
                if (dayId != null && !workDays.contains(dayId))
                    workDays.add(dayId);
            }
            Collections.sort(workDays);
            return workDays;
        } else if (pattern.contains("-")) {
            String[] parts = pattern.split("-");
            if (parts.length == 2) {
                Integer start = DAY_TO_NUMBER.get(parts[0].trim());
                Integer end = DAY_TO_NUMBER.get(parts[1].trim());
                if (start != null && end != null) {
                    List<Integer> workDays = new ArrayList<>();
                    for (int i = start; i <= end; i++)
                        workDays.add(i);
                    return workDays;
                }
            }
        } else if ("all days".equals(pattern)) {
            List<Integer> workDays = new ArrayList<>();
            for (int i = 1; i <= 7; i++)
                workDays.add(i);
            return workDays;
        }
        return new ArrayList<>();
    }

    protected Map<String, Integer> parseTimeRange(String timeRangeStr) {
        Map<String, Integer> times = new HashMap<>();
        if (timeRangeStr == null || timeRangeStr.trim().isEmpty())
            return times;

        String input = timeRangeStr.trim();

        // Check for hourly duration format like "8 hours", "8:30 hours"
        if (input.toLowerCase().contains("hour") || input.toLowerCase().contains("hr")
                || input.toLowerCase().contains("h")) {
            int totalSeconds = parseHourlyDuration(input.toLowerCase());
            if (totalSeconds > 0) {
                times.put(START_TIME, DEFAULT_WORK_START_TIME);
                times.put(END_TIME, DEFAULT_WORK_START_TIME + totalSeconds);
            }
            return times;
        }

        // Time range format: "9:00-17:00"
        if (input.contains("-")) {
            String[] parts = input.split("-");
            if (parts.length == 2) {
                times.put(START_TIME, convertTimeStringToSeconds(parts[0].trim()));
                times.put(END_TIME, convertTimeStringToSeconds(parts[1].trim()));
            }
        }
        return times;
    }

    private int parseHourlyDuration(String input) {
        // Try "8:30 hours" format
        Matcher timeHour = Pattern.compile("^(\\d+):(\\d+)\\s*hours?$", Pattern.CASE_INSENSITIVE).matcher(input.trim());
        if (timeHour.find()) {
            return Integer.parseInt(timeHour.group(1)) * SECONDS_IN_HOUR
                    + Integer.parseInt(timeHour.group(2)) * SECONDS_IN_MINUTE;
        }
        // Try "8 hours" or "8.5 hours"
        Matcher hours = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*h(?:our)?s?", Pattern.CASE_INSENSITIVE).matcher(input);
        if (hours.find()) {
            return (int) Math.round(Double.parseDouble(hours.group(1)) * SECONDS_IN_HOUR);
        }
        return 0;
    }

    protected int convertTimeStringToSeconds(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty())
            return 0;
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0]) * SECONDS_IN_HOUR + Integer.parseInt(parts[1]) * SECONDS_IN_MINUTE;
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return 0;
    }

    /**
     * Extracts threshold in seconds from strings like ">8 hrs", ">40 hrs", ">8.5
     * hrs", "17:00"
     */
    private int extractThresholdSeconds(String thresholdValue) {
        if (thresholdValue == null || thresholdValue.trim().isEmpty())
            return 0;

        // Try ">X hrs" format
        Matcher m = Pattern.compile(">(\\d+(?:\\.\\d+)?)\\s*hrs?").matcher(thresholdValue);
        if (m.find()) {
            return (int) (Double.parseDouble(m.group(1)) * SECONDS_IN_HOUR);
        }

        // Try time format "17:00"
        if (thresholdValue.contains(":")) {
            return convertTimeStringToSeconds(thresholdValue);
        }

        // Try plain number (hours)
        try {
            return (int) (Double.parseDouble(thresholdValue.trim()) * SECONDS_IN_HOUR);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected int getWorkLogTypeFromMethod(String method) {
        return method != null && method.equalsIgnoreCase("Hours") ? HOURS_METHOD : SHIFTS_LOGGING;
    }

    private String normalizeDayName(String day) {
        if (day == null)
            return "mon";
        String abbr = DAY_ABBREVIATIONS.get(day.trim().toLowerCase());
        return abbr != null ? abbr : "mon";
    }

    /**
     * Helper to safely get a double from a scenario map.
     */
    protected double getDouble(Map<String, Object> scenario, String key) {
        Object val = scenario.get(key);
        if (val instanceof Number)
            return ((Number) val).doubleValue();
        if (val instanceof String)
            return Double.parseDouble((String) val);
        return 0.0;
    }

    /**
     * Helper to safely get a long from a scenario map.
     */
    protected long getLong(Map<String, Object> scenario, String key) {
        Object val = scenario.get(key);
        if (val instanceof Number)
            return ((Number) val).longValue();
        if (val instanceof String)
            return Long.parseLong((String) val);
        return 0L;
    }

    /**
     * Helper to safely get an int from a scenario map.
     */
    protected int getInt(Map<String, Object> scenario, String key) {
        Object val = scenario.get(key);
        if (val instanceof Number)
            return ((Number) val).intValue();
        if (val instanceof String)
            return Integer.parseInt((String) val);
        return 0;
    }

    /**
     * Helper to safely get a string from a scenario map.
     */
    protected String getString(Map<String, Object> scenario, String key) {
        Object val = scenario.get(key);
        return val != null ? val.toString() : "";
    }

    // ========================================================================
    // SECTION 11: SCENARIO DATA EXTRACTION AND TEST EXECUTION
    // ========================================================================

    /**
     * Data class to hold all extracted scenario fields from JSON.
     */
    public static class TestScenarioData {
        private final String testId;
        private final String method;
        private final String dayPattern;
        private final String regularHours;
        private final String actualWorkTime;
        private final String rulesApplied;
        private final String breakTime;
        private final String breakBillable;
        private final double payRate;
        private final double billRate;
        private final double expectedTotalPay;
        private final double expectedTotalBill;
        private final String acceptedPayBillRate;
        private final String weeklyOT;
        private final Long jobStartDate;
        private final Long jobEndDate;
        private final Integer timesheetFrequency;
        private final Integer timesheetStartDay;
        private final Integer payCurrencyId;
        private final Integer billCurrencyId;
        private final Integer breakTimeThreshold;
        private final int workLogType;
        private final String comment;
        private final Integer isUnplannedHoursPayEnabled;
        private final String overtimeHours;
        private final String weeklyOvertimeHours;

        public TestScenarioData(String testId, String method, String dayPattern, String regularHours,
                String actualWorkTime, String rulesApplied, String breakTime,
                String breakBillable, double payRate, double billRate,
                double expectedTotalPay, double expectedTotalBill,
                String acceptedPayBillRate, String weeklyOT, Long jobStartDate,
                Long jobEndDate, Integer timesheetFrequency, Integer timesheetStartDay,
                Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold,
                int workLogType, String comment, Integer isUnplannedHoursPayEnabled,
                String overtimeHours, String weeklyOvertimeHours) {
            this.testId = testId;
            this.method = method;
            this.dayPattern = dayPattern;
            this.regularHours = regularHours;
            this.actualWorkTime = actualWorkTime;
            this.rulesApplied = rulesApplied;
            this.breakTime = breakTime;
            this.breakBillable = breakBillable;
            this.payRate = payRate;
            this.billRate = billRate;
            this.expectedTotalPay = expectedTotalPay;
            this.expectedTotalBill = expectedTotalBill;
            this.acceptedPayBillRate = acceptedPayBillRate;
            this.weeklyOT = weeklyOT;
            this.jobStartDate = jobStartDate;
            this.jobEndDate = jobEndDate;
            this.timesheetFrequency = timesheetFrequency;
            this.timesheetStartDay = timesheetStartDay;
            this.payCurrencyId = payCurrencyId;
            this.billCurrencyId = billCurrencyId;
            this.breakTimeThreshold = breakTimeThreshold;
            this.workLogType = workLogType;
            this.comment = comment;
            this.isUnplannedHoursPayEnabled = isUnplannedHoursPayEnabled;
            this.overtimeHours = overtimeHours;
            this.weeklyOvertimeHours = weeklyOvertimeHours;
        }

        // Getters
        public String getTestId() {
            return testId;
        }

        public String getMethod() {
            return method;
        }

        public String getDayPattern() {
            return dayPattern;
        }

        public String getRegularHours() {
            return regularHours;
        }

        public String getActualWorkTime() {
            return actualWorkTime;
        }

        public String getRulesApplied() {
            return rulesApplied;
        }

        public String getBreakTime() {
            return breakTime;
        }

        public String getBreakBillable() {
            return breakBillable;
        }

        public double getPayRate() {
            return payRate;
        }

        public double getBillRate() {
            return billRate;
        }

        public double getExpectedTotalPay() {
            return expectedTotalPay;
        }

        public double getExpectedTotalBill() {
            return expectedTotalBill;
        }

        public String getAcceptedPayBillRate() {
            return acceptedPayBillRate;
        }

        public String getWeeklyOT() {
            return weeklyOT;
        }

        public Long getJobStartDate() {
            return jobStartDate;
        }

        public Long getJobEndDate() {
            return jobEndDate;
        }

        public Integer getTimesheetFrequency() {
            return timesheetFrequency;
        }

        public Integer getTimesheetStartDay() {
            return timesheetStartDay;
        }

        public Integer getPayCurrencyId() {
            return payCurrencyId;
        }

        public Integer getBillCurrencyId() {
            return billCurrencyId;
        }

        public Integer getBreakTimeThreshold() {
            return breakTimeThreshold;
        }

        public int getWorkLogType() {
            return workLogType;
        }

        public String getComment() {
            return comment;
        }

        public Integer getIsUnplannedHoursPayEnabled() {
            return isUnplannedHoursPayEnabled;
        }

        public String getOvertimeHours() {
            return overtimeHours;
        }

        public String getWeeklyOvertimeHours() {
            return weeklyOvertimeHours;
        }
    }

    /**
     * Extracts all scenario data from the JSON scenario map.
     */
    protected TestScenarioData extractScenarioData(Map<String, Object> scenario) {
        String testId = getString(scenario, "testId");
        String method = getString(scenario, "method");
        String dayPattern = getString(scenario, "dayPattern");
        String regularHours = getString(scenario, "regularHours");
        String actualWorkTime = getString(scenario, "actualWorkTime");
        String rulesApplied = getString(scenario, "rulesApplied");
        String breakTime = getString(scenario, "breakTime");
        String breakBillable = getString(scenario, "breakBillable");
        double payRate = getDouble(scenario, "payRate");
        double billRate = getDouble(scenario, "billRate");
        double expectedTotalPay = getDouble(scenario, "expectedTotalPay");
        double expectedTotalBill = getDouble(scenario, "expectedTotalBill");
        String acceptedPayBillRate = getString(scenario, "acceptedPayBillRate");
        String weeklyOT = getString(scenario, "weeklyOT");
        Long jobStartDate = getLong(scenario, "jobStartDate");
        Long jobEndDate = getLong(scenario, "jobEndDate");
        Integer timesheetFrequency = getInt(scenario, "timesheetFrequency");
        Integer timesheetStartDay = getInt(scenario, "timesheetStartDay");
        Integer payCurrencyId = getInt(scenario, "payCurrencyId");
        Integer billCurrencyId = getInt(scenario, "billCurrencyId");
        Integer breakTimeThreshold = getInt(scenario, "breakTimeThreshold");
        int workLogType = getWorkLogTypeFromMethod(method);
        String comment = getString(scenario, "_comment");
        Integer isUnplannedHoursPayEnabled = getInt(scenario, "isUnplannedHoursPayEnabled");

        String overtimeHours = getString(scenario, "overtimeHours");
        String weeklyOvertimeHours = getString(scenario, "weeklyOvertimeHours");

        return new TestScenarioData(testId, method, dayPattern, regularHours, actualWorkTime,
                rulesApplied, breakTime, breakBillable, payRate, billRate, expectedTotalPay,
                expectedTotalBill, acceptedPayBillRate, weeklyOT, jobStartDate, jobEndDate,
                timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId,
                breakTimeThreshold, workLogType, comment, isUnplannedHoursPayEnabled,
                overtimeHours, weeklyOvertimeHours);
    }

    /**
     * Main test execution method that runs the complete flow:
     * 1. Extract scenario data
     * 2. Parse rules and work days
     * 3. Create rule template
     * 4. Setup timesheet entities
     * 5. Enable timesheet settings
     * 6. Create timesheet
     * 7. Update time logs
     * 8. Approve and evaluate
     * 9. Validate results
     */
    protected Integer executeMultipleTimeEntryTest(Map<String, Object> scenario, String albatrossAuthToken,
            String apiAuthToken, io.rcrm.api.commanfunctions.commanFunction function,
            List<Integer> createdTemplateIds) {
        TestScenarioData data = extractScenarioData(scenario);
        String testId = data.getTestId();

        try {
            // 1. Parse rules and work days
            List<Integer> workDayIds = parseWorkDaysFromPattern(data.getDayPattern());
            List<ParsedRule> parsedRules = parseRulesFromJson(data.getRulesApplied(), workDayIds);
            List<Map<String, Object>> customRules = buildCustomRulesFromParsedRules(parsedRules, workDayIds,
                    data.getPayRate(), data.getBillRate());

            // 2. Create rule template
            String templateName = ruleEngineenFake.getTestTemplateName(testId);
            Integer templateId = createRuleTemplateFromParsedRules(albatrossAuthToken, templateName,
                    workDayIds, data.getRegularHours(), customRules, data.getBreakBillable(),
                    data.getWorkLogType(), data.getBreakTimeThreshold(),
                    data.getIsUnplannedHoursPayEnabled());
            assertThat("Template should be created for " + testId, templateId, notNullValue());
            if (createdTemplateIds != null) {
                createdTemplateIds.add(templateId);
            }

            // 3. Setup timesheet entities
            TimesheetSetupResult setup = setupForTimesheet(apiAuthToken, albatrossAuthToken, function);

            // 4. Enable timesheet settings
            Response timesheetResponse = enableTimesheetSettings(albatrossAuthToken, setup.getJobId(),
                    setup.getCandidateId(), setup.getUserId(), templateId, data.getDayPattern(),
                    data.getRegularHours(), data.getPayRate(), data.getBillRate(), data.getBreakBillable(),
                    data.getJobStartDate(), data.getJobEndDate(), data.getTimesheetFrequency(),
                    data.getTimesheetStartDay(), data.getPayCurrencyId(), data.getBillCurrencyId(),
                    data.getBreakTimeThreshold(), data.getWorkLogType(),
                    data.getIsUnplannedHoursPayEnabled());
            assertThat("Timesheet settings should succeed for " + testId,
                    timesheetResponse.getStatusCode(), equalTo(200));

            // 5. Get free slots and create timesheet
            Response freeSlotsResponse = getFreeSlotsForTimesheet(albatrossAuthToken, setup.getCandidateId(),
                    data.getJobStartDate(), data.getJobEndDate(), data.getTimesheetFrequency(),
                    data.getTimesheetStartDay());
            assertThat("Free slots should return 200 for " + testId,
                    freeSlotsResponse.getStatusCode(), equalTo(200));

            Response createTimesheetResponse = createTimesheetFromSlots(albatrossAuthToken, setup.getJobId(),
                    setup.getCandidateId(), freeSlotsResponse);
            assertThat("Create timesheet should return 200 for " + testId,
                    createTimesheetResponse.getStatusCode(), equalTo(200));

            // 6. Get timesheets and time logs
            Response timesheetsResponse = getTimesheetsForContractor(albatrossAuthToken, setup.getJobId(),
                    setup.getCandidateId());
            assertThat("Get timesheets should return 200 for " + testId,
                    timesheetsResponse.getStatusCode(), equalTo(200));

            List<Map<String, Object>> timesheets = timesheetsResponse.jsonPath().getList("data");
            assertThat("Timesheets should not be empty for " + testId, timesheets.isEmpty(), is(false));

            Integer timesheetId = (Integer) timesheets.get(0).get("id");
            assertThat("Timesheet ID should be extracted for " + testId, timesheetId, notNullValue());

            Response timeLogsResponse = getTimeLogsForTimesheet(albatrossAuthToken, timesheetId);
            assertThat("Time logs should return 200 for " + testId,
                    timeLogsResponse.getStatusCode(), equalTo(200));

            // 7. Parse multi-entry work times and break times, then update time logs
            Map<String, WeekWorkData> workTimesByWeek = parseMultiEntryWorkTimes(data.getActualWorkTime());
            Map<String, WeekWorkData> breakTimesByWeek = parseMultiEntryBreakTimes(data.getBreakTime());

            // 7a. Call evaluate-overtime API to get per-timelog overtime from rule engine
            Map<String, Map<String, Double>> expectedOvertimeMap = parseOvertimeHours(data.getOvertimeHours());

            JsonPath tlJsonPath = timeLogsResponse.jsonPath();
            Map<String, Object> tlData = tlJsonPath.getMap("data");
            List<Map<String, Object>> existingTimeLogs = (List<Map<String, Object>>) tlData.get("timeLogs");

            List<Map<String, Object>> evalPayload = buildEvaluateOvertimePayload(
                    existingTimeLogs, timesheetId, workTimesByWeek, breakTimesByWeek,
                    data.getComment());

            Response evalOvertimeResponse = callEvaluateOvertimeApi(albatrossAuthToken, evalPayload);
            assertThat("Evaluate-overtime API should return 200 for " + testId,
                    evalOvertimeResponse.getStatusCode(), equalTo(HTTP_OK));

            Map<Integer, Map<Integer, Integer>> overtimeApiMap = parseOvertimeApiResponse(evalOvertimeResponse);

            assertOvertimeFromApi(testId, overtimeApiMap, expectedOvertimeMap,
                    existingTimeLogs, timesheetId);

            Map<Integer, Integer> overtimeByLogId = buildOvertimeMapForTimelogs(overtimeApiMap, timesheetId);

            updateTimeLogsWithMultipleEntries(albatrossAuthToken, timesheetId, timeLogsResponse,
                    workTimesByWeek, breakTimesByWeek, data.getRegularHours(), data.getBreakBillable(),
                    data.getTimesheetFrequency(), data.getJobStartDate(), data.getBreakTimeThreshold(),
                    workDayIds, data.getComment(), overtimeByLogId);

            // 8. Approve and evaluate
            Response approveResponse = approveTimesheet(albatrossAuthToken, timesheetId);
            assertThat("Approve timesheet should return 201 for " + testId,
                    approveResponse.getStatusCode(), equalTo(201));

            Response evaluationResponse = evaluateTimesheet(albatrossAuthToken, timesheetId);
            assertThat("Rule evaluation should return 200 for " + testId,
                    evaluationResponse.getStatusCode(), equalTo(200));

            // 9. Combined validation: Total pay/bill + Per-day per-rule + Weekly OT
            validateAllAmounts(testId, evaluationResponse,
                    data.getExpectedTotalPay(), data.getExpectedTotalBill(),
                    data.getAcceptedPayBillRate(), data.getWeeklyOT(),
                    data.getIsUnplannedHoursPayEnabled());

            return timesheetId;
        } catch (AssertionError e) {
            // Re-wrap assertion errors to include testId if not already present
            String msg = e.getMessage();
            if (msg != null && msg.contains(testId)) {
                throw e; // Already has testId
            }
            throw new AssertionError("[" + testId + "] " + msg, e);
        } catch (Exception e) {
            throw new AssertionError("[" + testId + "] Test scenario failed: " + e.getMessage(), e);
        }
    }
}
