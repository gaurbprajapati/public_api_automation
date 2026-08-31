package io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation;

import io.rcrm.api.javafaker.ContractStaffing.RuleEngineenFake;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import com.qa.api.util.TestUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RuleEngineCalculationBase extends ContractStaffingBaseTest {
    protected RuleEngineenFake ruleEngineenFake;

    protected static final class RuleEngineConfig {
        private RuleEngineConfig() {
        }

        public static final int HOURS_METHOD = 1;
        public static final int SHIFTS_LOGGING = 2;
        public static final int MULTIPLIER_CHARGE = 1;
        public static final int FIXED_RATE_CHARGE = 2;
        public static final int AFTER_SHIFT_RULE = 1;
        public static final int BEFORE_SHIFT_RULE = 2;
        public static final int SPECIFIC_RANGE_RULE = 3;
        public static final int DAILY_OVERTIME_SHIFT_RULE = 4;
        public static final int WEEKLY_OVERTIME_SHIFT_RULE = 5;
        public static final int SPECIFIC_HOURS_RANGE_RULE = 6;
        public static final int DAILY_OVERTIME_HOURS_RULE = 7;
        public static final int WEEKLY_OVERTIME_HOURS_RULE = 8;
    }

    protected static final class TimesheetConfig {
        public static final int DEFAULT_WORK_START_TIME = 28800;
        public static final int DEFAULT_WORK_END_TIME = 61200;
        public static final int SECONDS_IN_HOUR = 3600;
        public static final int SECONDS_IN_MINUTE = 60;
        public static final long SECONDS_IN_DAY = 24L * 60 * 60;
        public static final int APPROVED_STATUS = 4;
    }

    protected static final class ValidationConfig {
        public static final int HTTP_OK = 200;
        public static final int HTTP_CREATED = 201;
        public static final double AMOUNT_TOLERANCE = 0.01;
    }

    protected static final int HOURS_METHOD = RuleEngineConfig.HOURS_METHOD;
    protected static final int SHIFTS_LOGGING = RuleEngineConfig.SHIFTS_LOGGING;
    protected static final int MULTIPLIER_CHARGE = RuleEngineConfig.MULTIPLIER_CHARGE;
    protected static final int FIXED_RATE_CHARGE = RuleEngineConfig.FIXED_RATE_CHARGE;
    protected static final int DEFAULT_WORK_START_TIME = TimesheetConfig.DEFAULT_WORK_START_TIME;
    protected static final int DEFAULT_WORK_END_TIME = TimesheetConfig.DEFAULT_WORK_END_TIME;
    protected static final int SECONDS_IN_HOUR = TimesheetConfig.SECONDS_IN_HOUR;
    protected static final int SECONDS_IN_MINUTE = TimesheetConfig.SECONDS_IN_MINUTE;
    protected static final long SECONDS_IN_DAY = TimesheetConfig.SECONDS_IN_DAY;
    protected static final double AMOUNT_TOLERANCE = ValidationConfig.AMOUNT_TOLERANCE;
    protected static final int HTTP_OK = ValidationConfig.HTTP_OK;
    protected static final int HTTP_CREATED = ValidationConfig.HTTP_CREATED;
    protected static final int TIMESHEET_APPROVED_STATUS = TimesheetConfig.APPROVED_STATUS;

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

    private static final int AFTER_SHIFT_RULE = RuleEngineConfig.AFTER_SHIFT_RULE;
    private static final int BEFORE_SHIFT_RULE = RuleEngineConfig.BEFORE_SHIFT_RULE;
    private static final int SPECIFIC_RANGE_RULE = RuleEngineConfig.SPECIFIC_RANGE_RULE;
    private static final int DAILY_OVERTIME_SHIFT_RULE = RuleEngineConfig.DAILY_OVERTIME_SHIFT_RULE;
    private static final int WEEKLY_OVERTIME_SHIFT_RULE = RuleEngineConfig.WEEKLY_OVERTIME_SHIFT_RULE;
    private static final int SPECIFIC_HOURS_RANGE_RULE = RuleEngineConfig.SPECIFIC_HOURS_RANGE_RULE;
    private static final int DAILY_OVERTIME_HOURS_RULE = RuleEngineConfig.DAILY_OVERTIME_HOURS_RULE;
    private static final int WEEKLY_OVERTIME_HOURS_RULE = RuleEngineConfig.WEEKLY_OVERTIME_HOURS_RULE;

    private static final String[] DAY_NAMES = { "", "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
    private static final String[] ALL_DAYS = { "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
    private static final String NONE_VALUE = "None";
    private static final String REGULAR_HOURS_RULE = "regular hours";

    private static final Map<String, String> DAY_ABBREVIATIONS = createDayAbbreviations();
    private static final Map<String, Integer> DAY_TO_NUMBER = createDayToNumberMap();

    private static Map<String, String> createDayAbbreviations() {
        Map<String, String> map = new HashMap<>();
        map.put("monday", "mon");
        map.put("tuesday", "tue");
        map.put("wednesday", "wed");
        map.put("thursday", "thu");
        map.put("friday", "fri");
        map.put("saturday", "sat");
        map.put("sunday", "sun");
        map.put("mon", "mon");
        map.put("tue", "tue");
        map.put("tues", "tue");
        map.put("wed", "wed");
        map.put("thu", "thu");
        map.put("thurs", "thu");
        map.put("fri", "fri");
        map.put("sat", "sat");
        map.put("sun", "sun");
        return map;
    }

    private static Map<String, Integer> createDayToNumberMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("monday", 1);
        map.put("tuesday", 2);
        map.put("wednesday", 3);
        map.put("thursday", 4);
        map.put("friday", 5);
        map.put("saturday", 6);
        map.put("sunday", 7);
        map.put("mon", 1);
        map.put("tue", 2);
        map.put("tues", 2);
        map.put("wed", 3);
        map.put("thu", 4);
        map.put("thurs", 4);
        map.put("fri", 5);
        map.put("sat", 6);
        map.put("sun", 7);
        return map;
    }

    private static final Pattern DAY_PREFIX_PATTERN = Pattern.compile(
            "^(?i)(mon(day)?|tue(s|sday)?|wed(nesday)?|thu(r|rs|rsday)?|fri(day)?|sat(urday)?|sun(day)?)\\s*:?");
    private static final Pattern WEEK_DETAILED_PATTERN = Pattern.compile("(?i)week(\\d+):\\s*\\[([^\\]]+)\\]");
    private static final Pattern WEEK_SIMPLE_PATTERN = Pattern.compile("(?i)week(\\d+):\\s*([0-9:]+)-([0-9:]+)");
    private static final Pattern COMPACT_HOUR_PATTERN = Pattern
            .compile("^(?:\\s*)(?:(\\d+)h)?(?:\\s*(\\d+)m)?(?:\\s*)$");
    private static final Pattern TIME_HOUR_PATTERN = Pattern.compile("^(\\d+):(\\d+)\\s*hours?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern HOURS_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*h(?:our)?s?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+)\\s*m(?:in)?s?", Pattern.CASE_INSENSITIVE);

    protected RuleEngineCalculationBase() {
        super();
        this.ruleEngineenFake = new RuleEngineenFake();
    }

    // ========================================================================
    // SECTION: PARSED RULE MODEL + RULE PARSING (ported from shift-based design)
    // ========================================================================

    public static class ParsedRule {
        private final String ruleKey;
        private final String ruleTypeName;
        private final double payMultiplier;
        private final double billMultiplier;
        private final double payFixedRate;
        private final double billFixedRate;
        private final String thresholdValue;
        private final String rangeStart;
        private final String rangeEnd;
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

        public String getRuleKey() { return ruleKey; }
        public String getRuleTypeName() { return ruleTypeName; }
        public double getPayMultiplier() { return payMultiplier; }
        public double getBillMultiplier() { return billMultiplier; }
        public double getPayFixedRate() { return payFixedRate; }
        public double getBillFixedRate() { return billFixedRate; }
        public String getThresholdValue() { return thresholdValue; }
        public String getRangeStart() { return rangeStart; }
        public String getRangeEnd() { return rangeEnd; }
        public List<Integer> getAppliedDays() { return appliedDays; }
        public boolean isFixedRate() { return isFixedRate; }
    }

    protected List<ParsedRule> parseRulesFromJson(String rulesApplied, List<Integer> defaultWorkDayIds) {
        List<ParsedRule> rules = new ArrayList<>();
        if (rulesApplied == null || rulesApplied.trim().isEmpty()) {
            return rules;
        }

        List<String> ruleStrings = splitTopLevelRules(rulesApplied.trim());

        for (String ruleStr : ruleStrings) {
            ruleStr = ruleStr.trim();
            if (ruleStr.isEmpty()) continue;

            ParsedRule parsed = parseSingleRule(ruleStr, defaultWorkDayIds);
            if (parsed != null) {
                rules.add(parsed);
            }
        }
        return rules;
    }

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
                if (bracketDepth == 0) {
                    result.add(current.toString().trim());
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
            result.add(remaining);
        }
        return result;
    }

    private ParsedRule parseSingleRule(String ruleStr, List<Integer> defaultWorkDayIds) {
        int bracketStart = ruleStr.indexOf(":[");
        if (bracketStart == -1) {
            bracketStart = ruleStr.indexOf('[');
            if (bracketStart == -1) return null;
            String ruleKey = ruleStr.substring(0, bracketStart).trim();
            String content = ruleStr.substring(bracketStart);
            return parseRuleContent(ruleKey, content, defaultWorkDayIds);
        }

        String ruleKey = ruleStr.substring(0, bracketStart).trim();
        String content = ruleStr.substring(bracketStart + 1);
        return parseRuleContent(ruleKey, content, defaultWorkDayIds);
    }

    private ParsedRule parseRuleContent(String ruleKey, String bracketContent, List<Integer> defaultWorkDayIds) {
        String content = bracketContent.trim();
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);
        content = content.trim();

        String normalizedKey = normalizeRuleKey(ruleKey);

        List<String> parts = splitByCommaOutsideBrackets(content);

        String ruleTypeName = null;
        List<String> remainingParts = new ArrayList<>();
        if (!parts.isEmpty()) {
            String firstPart = parts.get(0).trim();
            if (!firstPart.contains(":")) {
                ruleTypeName = firstPart;
                remainingParts = parts.subList(1, parts.size());
            } else {
                ruleTypeName = normalizedKey;
                remainingParts = parts;
            }
        } else {
            ruleTypeName = normalizedKey;
        }

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
        List<Integer> appliedDays = new ArrayList<>();

        for (String part : remainingParts) {
            part = part.trim();
            String lowerPart = part.toLowerCase();

            if (lowerPart.contains("paymultiplier:") || lowerPart.contains("pay_multiplier:")) {
                payMultiplier = extractParsedMultiplierValue(part);
            } else if (lowerPart.contains("billmultiplier:") || lowerPart.contains("billmultipier:")
                    || lowerPart.contains("bill_multiplier:")) {
                billMultiplier = extractParsedMultiplierValue(part);
            } else if (lowerPart.contains("multiplier:") && !lowerPart.contains("pay") && !lowerPart.contains("bill")) {
                double mul = extractParsedMultiplierValue(part);
                payMultiplier = mul;
                billMultiplier = mul;
            } else if (lowerPart.contains("payrateperhour:") || lowerPart.contains("pay_rate_per_hour:")) {
                payFixedRate = extractParsedNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("billrateperhour:") || lowerPart.contains("bill_rate_per_hour:")) {
                billFixedRate = extractParsedNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("payfixedrate:") || lowerPart.contains("pay_fixed_rate:")) {
                payFixedRate = extractParsedNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("billfixedrate:") || lowerPart.contains("bill_fixed_rate:")) {
                billFixedRate = extractParsedNumericValue(part);
                isFixedRate = true;
            } else if (lowerPart.contains("fixed")) {
                double[] rates = extractParsedFixedRatePair(part);
                if (rates[0] > 0 || rates[1] > 0) {
                    payFixedRate = rates[0];
                    billFixedRate = rates[1];
                    isFixedRate = true;
                }
            } else if (lowerPart.contains("startsfrom:") || lowerPart.contains("starts_from:")) {
                thresholdValue = extractParsedValueAfterColon(part);
            } else if (lowerPart.contains("threshold:")) {
                thresholdValue = extractParsedValueAfterColon(part);
            } else if (lowerPart.contains("range:") || lowerPart.contains("time:")) {
                String rangeVal = extractParsedValueAfterColon(part);
                if (rangeVal.contains("-")) {
                    String[] rangeParts = rangeVal.split("-");
                    rangeStart = rangeParts[0].trim();
                    rangeEnd = rangeParts.length > 1 ? rangeParts[1].trim() : "";
                    thresholdValue = rangeVal;
                } else {
                    thresholdValue = rangeVal;
                }
            } else if (lowerPart.contains("appliedday:") || lowerPart.contains("applied_day:")) {
                List<Integer> extractedDays = extractParsedAppliedDays(part);
                if (!extractedDays.isEmpty()) {
                    appliedDays = extractedDays;
                }
            }
        }

        if (appliedDays.isEmpty()) {
            if (normalizedKey.equals("WeeklyOT")) {
                appliedDays = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
            } else {
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
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    private String normalizeRuleKey(String ruleKey) {
        String lower = ruleKey.toLowerCase().replaceAll("[\\s_]", "");
        if (lower.contains("aftershift") || lower.equals("after")) return "AfterShift";
        if (lower.contains("beforeshift") || lower.contains("beforerule") || lower.equals("before")) return "BeforeShift";
        if (lower.contains("dailyot") || lower.contains("dailyovertime")) return "DailyOT";
        if (lower.contains("weeklyot") || lower.contains("weeklyovertime")) return "WeeklyOT";
        if (lower.contains("specificrange") || lower.contains("specific")) return "SpecificRange";
        if (lower.contains("specifichours")) return "SpecificHoursRange";
        return ruleKey.trim();
    }

    private double extractParsedMultiplierValue(String part) {
        java.util.regex.Matcher matcher = Pattern.compile("multiplier\\s*:\\s*(\\d+(?:\\.\\d+)?)",
                Pattern.CASE_INSENSITIVE).matcher(part);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 1.0;
    }

    private double extractParsedNumericValue(String part) {
        java.util.regex.Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(part);
        if (m.find()) return Double.parseDouble(m.group(1));
        return 0.0;
    }

    private double[] extractParsedFixedRatePair(String part) {
        java.util.regex.Matcher m = Pattern.compile("\\$(\\d+(?:\\.\\d+)?)/\\$(\\d+(?:\\.\\d+)?)").matcher(part);
        if (m.find()) {
            return new double[] { Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)) };
        }
        return new double[] { 0.0, 0.0 };
    }

    private String extractParsedValueAfterColon(String part) {
        int colonIdx = part.indexOf(':');
        if (colonIdx >= 0 && colonIdx < part.length() - 1) {
            return part.substring(colonIdx + 1).trim();
        }
        return "";
    }

    private List<Integer> extractParsedAppliedDays(String part) {
        List<Integer> days = new ArrayList<>();
        java.util.regex.Matcher m = Pattern.compile("appliedDay\\s*:\\s*\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE)
                .matcher(part);
        if (m.find()) {
            String bracketContent = m.group(1).trim();
            String[] dayTokens = bracketContent.split(",");
            for (String token : dayTokens) {
                token = token.trim();
                if (token.isEmpty()) continue;
                Integer dayNum = DAY_TO_NUMBER.get(token.toLowerCase());
                if (dayNum != null && !days.contains(dayNum)) {
                    days.add(dayNum);
                }
            }
        } else {
            java.util.regex.Matcher fallback = Pattern.compile("\\[([^\\]]+)\\]").matcher(part);
            if (fallback.find()) {
                String bracketContent = fallback.group(1).trim();
                String[] dayTokens = bracketContent.split(",");
                for (String token : dayTokens) {
                    token = token.trim();
                    if (token.isEmpty()) continue;
                    Integer dayNum = DAY_TO_NUMBER.get(token.toLowerCase());
                    if (dayNum != null && !days.contains(dayNum)) {
                        days.add(dayNum);
                    }
                }
            }
        }
        return days;
    }

    private int extractThresholdSeconds(String thresholdValue) {
        if (thresholdValue == null || thresholdValue.trim().isEmpty()) return 0;

        java.util.regex.Matcher m = Pattern.compile(">(\\d+(?:\\.\\d+)?)\\s*hrs?").matcher(thresholdValue);
        if (m.find()) {
            return (int) (Double.parseDouble(m.group(1)) * SECONDS_IN_HOUR);
        }

        if (thresholdValue.contains(":")) {
            return convertTimeToSeconds(thresholdValue);
        }

        try {
            return (int) (Double.parseDouble(thresholdValue.trim()) * SECONDS_IN_HOUR);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ========================================================================
    // SECTION: BUILD CUSTOM RULES FROM PARSED RULES (unified for Hours + Shift)
    // ========================================================================

    protected List<Map<String, Object>> buildCustomRulesFromParsedRules(List<ParsedRule> parsedRules,
            List<Integer> defaultWorkDayIds, double payRate, double billRate, String method) {
        List<Map<String, Object>> customRules = new ArrayList<>();

        for (ParsedRule parsed : parsedRules) {
            if (parsed.getRuleKey().equalsIgnoreCase("RegularHours")) {
                continue;
            }

            Map<String, Object> rule = new HashMap<>();
            rule.put(ID, 0);
            rule.put(RULE_NAME, parsed.getRuleTypeName());

            String ruleKey = parsed.getRuleKey();
            List<Integer> dayIds;
            if (!parsed.getAppliedDays().isEmpty()) {
                dayIds = new ArrayList<>(parsed.getAppliedDays());
            } else if (ruleKey.equals("WeeklyOT")) {
                dayIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
            } else {
                dayIds = new ArrayList<>(defaultWorkDayIds);
            }
            rule.put(WORK_DAY_ID, dayIds);

            rule.put(START_DURATION, 0);
            rule.put(END_DURATION, 0);
            rule.put(DAILY_THRESHOLD, 0);
            rule.put(WEEKLY_THRESHOLD, 0);

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

            configureRuleTypeFromParsed(rule, parsed, method);

            customRules.add(rule);
        }

        return customRules;
    }

    private void configureRuleTypeFromParsed(Map<String, Object> rule, ParsedRule parsed, String method) {
        String key = parsed.getRuleKey();
        boolean isHours = method != null && method.equalsIgnoreCase("Hours");

        switch (key) {
            case "AfterShift":
                rule.put(RULE_TYPE, AFTER_SHIFT_RULE);
                rule.put(START_TIME, convertTimeToSeconds(parsed.getThresholdValue()));
                rule.put(END_TIME, 0);
                break;

            case "BeforeShift":
                rule.put(RULE_TYPE, BEFORE_SHIFT_RULE);
                rule.put(START_TIME, convertTimeToSeconds(parsed.getThresholdValue()));
                rule.put(END_TIME, 0);
                break;

            case "SpecificRange":
                rule.put(RULE_TYPE, SPECIFIC_RANGE_RULE);
                rule.put(START_TIME, convertTimeToSeconds(parsed.getRangeStart()));
                rule.put(END_TIME, convertTimeToSeconds(parsed.getRangeEnd()));
                break;

            case "DailyOT":
                rule.put(RULE_TYPE, isHours ? DAILY_OVERTIME_HOURS_RULE : DAILY_OVERTIME_SHIFT_RULE);
                rule.put(START_TIME, 0);
                rule.put(END_TIME, 0);
                rule.put(START_DURATION, 0);
                rule.put(END_DURATION, 0);
                rule.put(DAILY_THRESHOLD, extractThresholdSeconds(parsed.getThresholdValue()));
                break;

            case "WeeklyOT":
                rule.put(RULE_TYPE, isHours ? WEEKLY_OVERTIME_HOURS_RULE : WEEKLY_OVERTIME_SHIFT_RULE);
                rule.put(START_TIME, 0);
                rule.put(END_TIME, 0);
                rule.put(START_DURATION, 0);
                rule.put(END_DURATION, 0);
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
                rule.put(RULE_TYPE, isHours ? DAILY_OVERTIME_HOURS_RULE : AFTER_SHIFT_RULE);
                rule.put(START_TIME, 0);
                rule.put(END_TIME, 0);
                break;
        }
    }

    protected Integer createRuleTemplate(String authToken, String templateName, List<Integer> workDayIds,
            String regularHours, List<Map<String, Object>> customRules, String breakBillable) {
        return createRuleTemplate(authToken, templateName, workDayIds, regularHours, customRules, breakBillable,
                SHIFTS_LOGGING, null);
    }

    protected Integer createRuleTemplate(String authToken, String templateName, List<Integer> workDayIds,
            String regularHours, List<Map<String, Object>> customRules, String breakBillable, int workLogType) {
        return createRuleTemplate(authToken, templateName, workDayIds, regularHours, customRules, breakBillable,
                workLogType, null);
    }

    protected Integer createRuleTemplate(String authToken, String templateName, List<Integer> workDayIds,
            String regularHours, List<Map<String, Object>> customRules, String breakBillable,
            Integer breakTimeThreshold) {
        return createRuleTemplate(authToken, templateName, workDayIds, regularHours, customRules, breakBillable,
                SHIFTS_LOGGING, breakTimeThreshold);
    }

    protected Integer createRuleTemplate(String authToken, String templateName, List<Integer> workDayIds,
            String regularHours, List<Map<String, Object>> customRules, String breakBillable, int workLogType,
            Integer breakTimeThreshold) {
        return createRuleTemplate(authToken, templateName, workDayIds, regularHours, customRules,
                breakBillable, workLogType, breakTimeThreshold, null);
    }

    protected Integer createRuleTemplate(String authToken, String templateName, List<Integer> workDayIds,
            String regularHours, List<Map<String, Object>> customRules, String breakBillable, int workLogType,
            Integer breakTimeThreshold, Integer isUnplannedHoursPayEnabled) {
        try {
            Map<String, Integer> workTimes = parseTimeRange(regularHours);
            Integer workStartTime = workTimes.getOrDefault(START_TIME, DEFAULT_WORK_START_TIME);
            Integer workEndTime = workTimes.getOrDefault(END_TIME, DEFAULT_WORK_END_TIME);
            Map<String, Object> templatePayload = buildTemplatePayload(templateName, workDayIds, workStartTime,
                    workEndTime, customRules, breakBillable, workLogType, breakTimeThreshold,
                    isUnplannedHoursPayEnabled);
            String jsonPayload = TestUtil.getSerializedJSON(templatePayload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/rule-template", authToken,
                    null, true, jsonPayload);
            assertThat("Rule template creation should return 201",
                    response.getStatusCode(), equalTo(HTTP_CREATED));
            return getTemplateIdByName(authToken, templateName);
        } catch (Exception e) {
            throw new AssertionError("Error creating rule template: " + e.getMessage(), e);
        }
    }

    protected Integer getTemplateIdByName(String authToken, String templateName) {
        try {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Response response = RestClient.doGet("JSON", timesheetBaseURL, "rule-engine/rule-template/list", authToken,
                    null, null, true);
            if (response.getStatusCode() != HTTP_OK) {
                return null;
            }
            JsonPath jsonPath = response.jsonPath();
            List<Map<String, Object>> templates = jsonPath.getList("data");
            if (templates == null || templates.isEmpty()) {
                return null;
            }
            for (Map<String, Object> template : templates) {
                String currentTemplateName = (String) template.get("templateName");
                Integer templateId = (Integer) template.get("id");
                if (templateName.equals(currentTemplateName)) {
                    return templateId;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> buildTemplatePayload(String templateName, List<Integer> workDayIds,
            Integer workStartTime, Integer workEndTime, List<Map<String, Object>> customRules, String breakBillable,
            int workLogType, Integer breakTimeThreshold) {
        return buildTemplatePayload(templateName, workDayIds, workStartTime, workEndTime, customRules,
                breakBillable, workLogType, breakTimeThreshold, null);
    }

    private Map<String, Object> buildTemplatePayload(String templateName, List<Integer> workDayIds,
            Integer workStartTime, Integer workEndTime, List<Map<String, Object>> customRules, String breakBillable,
            int workLogType, Integer breakTimeThreshold, Integer isUnplannedHoursPayEnabled) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(TEMPLATE_NAME, templateName);
        payload.put(WORK_LOG_TYPE, workLogType);

        int calculateBreakTimeValue = calculateBreakTimeValue(breakBillable);
        payload.put(CALCULATE_BREAK_TIME, calculateBreakTimeValue);

        if (calculateBreakTimeValue == 0 && breakTimeThreshold != null && breakTimeThreshold > 0) {
            payload.put(BREAK_TIME_THRESHOLD, breakTimeThreshold);
        } else {
            payload.put(BREAK_TIME_THRESHOLD, 0);
        }

        payload.put(WORK_DAY_IDS, workDayIds);

        WorkTimeArrays workTimeArrays = buildWorkTimeArrays(workDayIds, workStartTime, workEndTime, workLogType);
        payload.put(WORK_TIME, workTimeArrays.workTimeList);
        payload.put(WORK_START_TIME, workTimeArrays.workStartTimeList);
        payload.put(WORK_END_TIME, workTimeArrays.workEndTimeList);
        payload.put(CUSTOM_RULES, customRules != null ? customRules : new ArrayList<>());
        payload.put("isUnplannedHoursPayEnabled",
                (isUnplannedHoursPayEnabled != null && isUnplannedHoursPayEnabled == 1) ? 1 : 0);

        return payload;
    }

    private static class WorkTimeArrays {
        final List<Integer> workTimeList;
        final List<Integer> workStartTimeList;
        final List<Integer> workEndTimeList;

        WorkTimeArrays(List<Integer> workTimeList, List<Integer> workStartTimeList, List<Integer> workEndTimeList) {
            this.workTimeList = workTimeList;
            this.workStartTimeList = workStartTimeList;
            this.workEndTimeList = workEndTimeList;
        }
    }

    private static class BreakThresholdResult {
        final Integer breakThresholdOvertime;

        BreakThresholdResult(Integer breakThresholdOvertime) {
            this.breakThresholdOvertime = breakThresholdOvertime;
        }
    }

    private static class WorkingDayLogParams {
        final Integer logId;
        final Integer timesheetId;
        final Map<String, Integer> dayWorkTimes;
        final List<Map<String, Object>> breakIntervals;
        final Integer totalBreakTime;
        final Map<String, Integer> regularTimes;
        final String breakBillable;
        final Integer workLogType;
        final Integer breakTimeThreshold;

        WorkingDayLogParams(Integer logId, Integer timesheetId, Map<String, Integer> dayWorkTimes,
                List<Map<String, Object>> breakIntervals, Integer totalBreakTime,
                Map<String, Integer> regularTimes, String breakBillable, Integer workLogType,
                Integer breakTimeThreshold) {
            this.logId = logId;
            this.timesheetId = timesheetId;
            this.dayWorkTimes = dayWorkTimes;
            this.breakIntervals = breakIntervals;
            this.totalBreakTime = totalBreakTime;
            this.regularTimes = regularTimes;
            this.breakBillable = breakBillable;
            this.workLogType = workLogType;
            this.breakTimeThreshold = breakTimeThreshold;
        }
    }

    private WorkTimeArrays buildWorkTimeArrays(List<Integer> workDayIds, Integer workStartTime, Integer workEndTime,
            int workLogType) {
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

        return new WorkTimeArrays(workTimeList, workStartTimeList, workEndTimeList);
    }

    private int calculateBreakTimeValue(String breakBillable) {
        // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
        return 0;
    }

    private Map<String, Object> createTimesheetPayload(Integer candidateId, Long startDate, Long endDate) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(CONTRACTOR_IDS, Arrays.asList(candidateId));

        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put(START_DATE, startDate);
        dateRange.put(END_DATE, endDate);

        payload.put(TIMESHEET_DATES, Arrays.asList(dateRange));
        return payload;
    }

    private Map<String, Object> createFreeSlotsPayload(Integer candidateId, Long startDate, Long endDate,
            Integer timesheetFrequencyId, Integer timesheetStartDay) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(CONTRACTOR_IDS, Arrays.asList(candidateId));
        payload.put(START_DATE, startDate);
        payload.put(END_DATE, endDate);
        payload.put(TIMESHEET_FREQUENCY_ID, timesheetFrequencyId);
        payload.put(TIMESHEET_START_DAY, timesheetStartDay);
        return payload;
    }

    protected Response evaluateTimesheet(String authToken, Integer timesheetId) {
        try {
            String endpoint = "rule-engine/evaluate";
            Map<String, Object> payload = new HashMap<>();
            payload.put("timesheetId", timesheetId);
            String jsonPayload = com.qa.api.util.TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, endpoint, authToken, null, true,
                    jsonPayload);
            assertThat("Evaluation should return 200",
                    response.getStatusCode(), equalTo(HTTP_OK));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error evaluating timesheet: " + e.getMessage(), e);
        }
    }

    protected List<Map<String, Object>> getRulesFromTemplate(String authToken, Integer ruleTemplateId) {
        try {
            String endpoint = "rule-engine/rule-template/" + ruleTemplateId;
            Response response = RestClient.doGet("JSON", timesheetBaseURL, endpoint, authToken, null, null, true);
            if (response.getStatusCode() == HTTP_OK) {
                JsonPath jsonPath = response.jsonPath();
                List<Map<String, Object>> customRules = jsonPath.getList("data.customRules");
                if (customRules != null && !customRules.isEmpty()) {
                    return customRules;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    protected Integer getRealCandidateId(String authToken, String candidateSlug) {
        try {
            String endpoint = "candidates/" + candidateSlug + "/get";
            // Use dynamic albatrossURL from TestBase instead of hardcoded URL

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("slug", candidateSlug);

            Response response = RestClient.doPost("JSON", albatrossURL, endpoint,
                    authToken, null, true, requestBody);

            if (response.getStatusCode() == HTTP_OK) {
                JsonPath jsonPath = response.jsonPath();
                Integer realId = jsonPath.getInt("data.candidate.id");

                if (realId != null) {
                    return realId;
                }
                throw new AssertionError("Could not find candidate ID at data.candidate.id");
            } else {
                throw new AssertionError(
                        "Failed to fetch candidate details from Albatross. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new AssertionError("Error fetching real candidate ID: " + e.getMessage(), e);
        }
    }

    /**
     * Enable timesheet settings completely dynamically from CSV data (Weekly)
     */
    protected Response enableWeeklyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                null);
    }

    /**
     * Enable timesheet settings completely dynamically from CSV data (Weekly) with
     * breakTimeThreshold
     */
    protected Response enableWeeklyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                breakTimeThreshold);
    }

    /**
     * Enable monthly timesheet settings completely dynamically from CSV data
     */
    protected Response enableMonthlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, true,
                null);
    }

    /**
     * Enable monthly timesheet settings completely dynamically from CSV data with
     * breakTimeThreshold
     */
    protected Response enableMonthlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, true,
                breakTimeThreshold);
    }

    /**
     * Enable biweekly timesheet settings completely dynamically from CSV data
     */
    protected Response enableBiweeklyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                null);
    }

    /**
     * Enable biweekly timesheet settings completely dynamically from CSV data with
     * breakTimeThreshold
     */
    protected Response enableBiweeklyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                breakTimeThreshold);
    }

    // ===== HOURLY-BASED METHODS =====

    /**
     * Enable weekly timesheet settings for hourly-based logging
     */
    protected Response enableWeeklyHourlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                HOURS_METHOD, null);
    }

    /**
     * Enable weekly timesheet settings for hourly-based logging with
     * breakTimeThreshold
     */
    protected Response enableWeeklyHourlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                HOURS_METHOD, breakTimeThreshold);
    }

    /**
     * Enable monthly timesheet settings for hourly-based logging
     */
    protected Response enableMonthlyHourlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, true,
                HOURS_METHOD, null);
    }

    /**
     * Enable monthly timesheet settings for hourly-based logging with
     * breakTimeThreshold
     */
    protected Response enableMonthlyHourlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, true,
                HOURS_METHOD, breakTimeThreshold);
    }

    /**
     * Enable biweekly timesheet settings for hourly-based logging
     */
    protected Response enableBiweeklyHourlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                HOURS_METHOD, null);
    }

    /**
     * Enable biweekly timesheet settings for hourly-based logging with
     * breakTimeThreshold
     */
    protected Response enableBiweeklyHourlyTimesheetWithDynamicValues(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId, Integer breakTimeThreshold) {

        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId, false,
                HOURS_METHOD, breakTimeThreshold);
    }

    /**
     * Internal shared method for weekly, biweekly, and monthly timesheet settings
     */
    private Response enableTimesheetWithDynamicValuesInternal(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId,
            boolean isMonthly, Integer breakTimeThreshold) {
        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId, ruleTemplateId,
                dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId,
                isMonthly, SHIFTS_LOGGING, breakTimeThreshold);
    }

    /**
     * Internal shared method for weekly, biweekly, and monthly timesheet settings
     * with work log type
     */
    protected Response enableTimesheetWithDynamicValuesInternal(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId,
            boolean isMonthly, int workLogType, Integer breakTimeThreshold) {
        return enableTimesheetWithDynamicValuesInternal(authToken, jobId, candidateId, userId,
                ruleTemplateId, dayPattern, regularHours, rulesApplied, payRate, billRate, breakBillable,
                jobStartDate, jobEndDate, timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId,
                isMonthly, workLogType, breakTimeThreshold, null);
    }

    protected Response enableTimesheetWithDynamicValuesInternal(String authToken, Integer jobId,
            Integer candidateId, Integer userId,
            Integer ruleTemplateId, String dayPattern,
            String regularHours, String rulesApplied,
            double payRate, double billRate, String breakBillable,
            Long jobStartDate, Long jobEndDate, Integer timesheetFrequency,
            Integer timesheetStartDay, Integer payCurrencyId, Integer billCurrencyId,
            boolean isMonthly, int workLogType, Integer breakTimeThreshold,
            Integer isUnplannedHoursPayEnabled) {

        try {
            // Get the actual rules from the created rule template
            List<Map<String, Object>> templateRules = getRulesFromTemplate(authToken, ruleTemplateId);

            if (templateRules == null || templateRules.isEmpty()) {
                List<Integer> workDayIds = parseWorkDaysFromPattern(dayPattern);
                String method = (workLogType == HOURS_METHOD) ? "Hours" : "Shift";
                List<ParsedRule> parsedRules = parseRulesFromJson(rulesApplied, workDayIds);
                templateRules = buildCustomRulesFromParsedRules(parsedRules, workDayIds, payRate, billRate, method);
            }

            // Parse work days and times from CSV
            List<Integer> workDayIds = parseWorkDaysFromPattern(dayPattern);
            Map<String, Integer> workTimes = parseTimeRange(regularHours);
            Integer workStartTime = workTimes.getOrDefault("startTime", 28800);
            Integer workEndTime = workTimes.getOrDefault("endTime", 61200);

            // Create Approvers with the dynamic user ID
            Map<String, Object> approvers = new HashMap<>();
            approvers.put("agencyIds", Arrays.asList(userId));
            approvers.put("clientIds", Arrays.asList());

            // Build work time arrays ONLY for selected work days (not full 7-day array)
            List<Integer> workTimeList = new ArrayList<>();
            List<Integer> workStartTimeList = new ArrayList<>();
            List<Integer> workEndTimeList = new ArrayList<>();

            // Only add times for the selected work days based on work log type
            for (int i = 0; i < workDayIds.size(); i++) {
                if (workLogType == SHIFTS_LOGGING) {
                    // For SHIFTS: workTime is 0, start/end have values
                    workTimeList.add(0);
                    workStartTimeList.add(workStartTime);
                    workEndTimeList.add(workEndTime);
                } else if (workLogType == HOURS_METHOD) {
                    // For HOURS: workTime has duration, start/end are 0
                    Integer workDuration = workEndTime - workStartTime;
                    workTimeList.add(workDuration);
                    workStartTimeList.add(0);
                    workEndTimeList.add(0);
                }
            }

            // Create TimesheetSettings with all dynamic values from CSV
            Map<String, Object> timesheetSettings = new HashMap<>();

            // Basic job and timesheet info - All dynamic from CSV
            timesheetSettings.put("jobId", jobId);
            timesheetSettings.put("contractorIds", Arrays.asList(candidateId));
            timesheetSettings.put("jobStartDate", jobStartDate);
            timesheetSettings.put("jobEndDate", jobEndDate);
            timesheetSettings.put("timesheetFrequency", timesheetFrequency);
            timesheetSettings.put("timesheetStartDay", timesheetStartDay);

            // Approvers
            timesheetSettings.put("approvers", approvers);

            // Currency and rates from CSV - All dynamic
            timesheetSettings.put("payCurrencyId", payCurrencyId);
            timesheetSettings.put("payRate", payRate);
            timesheetSettings.put("billCurrencyId", billCurrencyId);
            timesheetSettings.put("billRate", billRate);

            // Work schedule from CSV
            timesheetSettings.put("workDayIds", workDayIds);
            timesheetSettings.put("workLogType", workLogType); // Dynamic work log type

            // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
            int calculateBreakTime = 0;
            timesheetSettings.put("calculateBreakTime", calculateBreakTime);

            // Always add breakTimeThreshold field
            if (calculateBreakTime == 0 && breakTimeThreshold != null && breakTimeThreshold > 0) {
                // When breaks are not billable and threshold is provided, use the actual
                // threshold
                timesheetSettings.put(BREAK_TIME_THRESHOLD, breakTimeThreshold);
            } else {
                // When breaks are billable or no threshold provided, send 0
                timesheetSettings.put(BREAK_TIME_THRESHOLD, 0);
            }

            // Work time arrays from CSV
            timesheetSettings.put("workTime", workTimeList);
            timesheetSettings.put("workStartTime", workStartTimeList);
            timesheetSettings.put("workEndTime", workEndTimeList);

            // Audit fields
            timesheetSettings.put("updatedOn", null);
            timesheetSettings.put("updatedBy", null);
            timesheetSettings.put("enabledOn", null);
            timesheetSettings.put("enabledBy", null);
            timesheetSettings.put("isPreferencesModified", 1);
            timesheetSettings.put("isReimbursementEnabled", 0);
            // Custom rules from template
            timesheetSettings.put("customRules", templateRules);
            timesheetSettings.put("isUnplannedHoursPayEnabled",
                    (isUnplannedHoursPayEnabled != null && isUnplannedHoursPayEnabled == 1) ? 1 : 0);

            // Send request
            String jsonPayload = TestUtil.getSerializedJSON(timesheetSettings);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                    authToken, null, true, jsonPayload);

            String frequencyType = isMonthly ? "Monthly" : (timesheetFrequency == 3 ? "Biweekly" : "Weekly");
            assertThat(frequencyType + " timesheet settings should return 200",
                    response.getStatusCode(), equalTo(200));

            return response;

        } catch (Exception e) {
            String frequencyType = isMonthly ? "monthly" : (timesheetFrequency == 3 ? "biweekly" : "weekly");
            throw new AssertionError(
                    "Error enabling " + frequencyType + " timesheet with dynamic values: " + e.getMessage(), e);
        }
    }

    protected Response approveTimesheet(String authToken, Integer timesheetId) {

        try {
            String endpoint = "timesheets/" + timesheetId + "/status";

            // Create the approval payload as specified by user
            Map<String, Object> payload = new HashMap<>();
            payload.put("approvalStatus", TIMESHEET_APPROVED_STATUS); // approved status
            String jsonPayload = com.qa.api.util.TestUtil.getSerializedJSON(payload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, endpoint,
                    authToken, null, true, jsonPayload);
            assertThat("Approve timesheet should return 201",
                    response.getStatusCode(), equalTo(201));
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error approving timesheet: " + e.getMessage(), e);
        }
    }

    protected Response createTimesheetFromSlots(String authToken, Integer jobId, Integer candidateId,
            Response freeSlotsResponse) {

        try {
            if (freeSlotsResponse == null || freeSlotsResponse.getStatusCode() != HTTP_OK) {
                throw new AssertionError("Invalid free slots response, cannot create timesheet");
            }

            JsonPath jsonPath = freeSlotsResponse.jsonPath();
            List<Map<String, Object>> slots = jsonPath.getList("data");

            if (slots == null || slots.isEmpty()) {
                throw new AssertionError("No available time slots found");
            }

            // Select the first available slot (as per user example, using index 2:
            // 1753056000 to 1753660799)
            Map<String, Object> selectedSlot = slots.size() > 1 ? slots.get(1) : slots.get(0);
            Long startDate = ((Number) selectedSlot.get("startDate")).longValue();
            Long endDate = ((Number) selectedSlot.get("endDate")).longValue();

            Map<String, Object> timesheetPayload = createTimesheetPayload(candidateId, startDate, endDate);

            // Use RestClient directly with JSON serialization
            String endpoint = "timesheets/jobs/" + jobId + "/contractors";
            String jsonPayload = com.qa.api.util.TestUtil.getSerializedJSON(timesheetPayload);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, endpoint,
                    authToken, null, true, jsonPayload);

            assertThat("Create timesheet should return 200",
                    response.getStatusCode(), equalTo(HTTP_OK));

            return response;

        } catch (Exception e) {
            throw new AssertionError("Error creating timesheet: " + e.getMessage(), e);
        }
    }

    protected Response getFreeSlotsForTimesheet(String authToken, Integer candidateId,
            Long startDate, Long endDate, Integer timesheetFrequencyId, Integer timesheetStartDay) {

        try {
            Map<String, Object> freeSlotsPayload = createFreeSlotsPayload(candidateId, startDate, endDate,
                    timesheetFrequencyId, timesheetStartDay);

            // Convert to JSON string to ensure it's sent as JSON body
            String jsonPayload = com.qa.api.util.TestUtil.getSerializedJSON(freeSlotsPayload);

            Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheets/free-slots",
                    authToken, null, true, jsonPayload);

            assertThat("Free slots should return 200",
                    response.getStatusCode(), equalTo(HTTP_OK));

            return response;

        } catch (Exception e) {
            throw new AssertionError("Error getting free slots: " + e.getMessage(), e);
        }
    }

    protected Response updateTimeEntriesWithCsvData(String authToken, Integer timesheetId,
            Integer workLogType, Response existingLogsResponse,
            String dayPattern, String regularHours, String actualWorkTime,
            String breakTime, String breakBillable, Integer timesheetFrequency, Long startDate,
            Integer breakTimeThreshold, String comment) {
        return updateTimeEntriesWithCsvData(authToken, timesheetId, workLogType, existingLogsResponse,
                dayPattern, regularHours, actualWorkTime, breakTime, breakBillable,
                timesheetFrequency, startDate, breakTimeThreshold, null, comment);
    }

    protected Response updateTimeEntriesWithCsvData(String authToken, Integer timesheetId,
            Integer workLogType, Response existingLogsResponse,
            String dayPattern, String regularHours, String actualWorkTime,
            String breakTime, String breakBillable, Integer timesheetFrequency, Long startDate,
            Integer breakTimeThreshold, Map<Integer, Integer> overtimeByLogId, String comment) {

        try {
            TimeEntryContext context = buildTimeEntryContext(existingLogsResponse, dayPattern, regularHours,
                    actualWorkTime, breakTime, timesheetFrequency);

            List<Map<String, Object>> updatedTimeLogs = new ArrayList<>();
            for (int i = 0; i < context.existingTimeLogs.size(); i++) {
                Map<String, Object> timeLog = context.existingTimeLogs.get(i);
                Integer logId = (Integer) timeLog.get("id");

                boolean shouldLogTime = shouldLogTimeForDay(i, context, startDate);

                if (shouldLogTime) {
                    Map<String, Integer> dayWorkTimes = getDayWorkTimes(i, context, timesheetFrequency, startDate);

                    List<Map<String, Object>> breakIntervals = (workLogType == HOURS_METHOD) ? new ArrayList<>()
                            : context.breakIntervals;

                    Integer dayBreakTime = resolveBreakTimeForDay(i, context, timesheetFrequency, startDate);

                    WorkingDayLogParams params = new WorkingDayLogParams(logId, timesheetId, dayWorkTimes,
                            breakIntervals, dayBreakTime,
                            context.regularTimes, breakBillable, workLogType, breakTimeThreshold);
                    Map<String, Object> workingDayLog = createWorkingDayLog(params);
                    if (workingDayLog != null) {
                        workingDayLog.put("remark", comment);
                        updatedTimeLogs.add(workingDayLog);
                    }
                } else {
                    Map<String, Object> emptyLog = createEmptyDayLog(logId, timesheetId, workLogType);
                    emptyLog.put("remark", comment);
                    updatedTimeLogs.add(emptyLog);
                }
            }

            return submitTimeLogUpdate(authToken, updatedTimeLogs, timesheetId, overtimeByLogId);

        } catch (Exception e) {
            throw new AssertionError("Error updating time entries with CSV data: " + e.getMessage(), e);
        }
    }

    private static class TimeEntryContext {
        final List<Map<String, Object>> existingTimeLogs;
        final List<Integer> workDays;
        final Map<String, Map<String, Integer>> perDayWorkTimes;
        final boolean explicitDayEntries;
        final Map<String, Map<String, Integer>> multiWeekWorkTimes;
        final Map<String, Map<String, Map<String, Integer>>> detailedMultiWeekWorkTimes;
        final Map<String, Integer> regularTimes;
        final List<Map<String, Object>> breakIntervals;
        final Integer totalBreakTime;
        final Map<String, Integer> perDayBreakTimes;
        final Integer timesheetFrequency;

        TimeEntryContext(List<Map<String, Object>> existingTimeLogs, List<Integer> workDays,
                Map<String, Map<String, Integer>> perDayWorkTimes, boolean explicitDayEntries,
                Map<String, Map<String, Integer>> multiWeekWorkTimes,
                Map<String, Map<String, Map<String, Integer>>> detailedMultiWeekWorkTimes,
                Map<String, Integer> regularTimes, List<Map<String, Object>> breakIntervals,
                Integer totalBreakTime, Map<String, Integer> perDayBreakTimes,
                Integer timesheetFrequency) {
            this.existingTimeLogs = existingTimeLogs;
            this.workDays = workDays;
            this.perDayWorkTimes = perDayWorkTimes;
            this.explicitDayEntries = explicitDayEntries;
            this.multiWeekWorkTimes = multiWeekWorkTimes;
            this.detailedMultiWeekWorkTimes = detailedMultiWeekWorkTimes;
            this.regularTimes = regularTimes;
            this.breakIntervals = breakIntervals;
            this.totalBreakTime = totalBreakTime;
            this.perDayBreakTimes = perDayBreakTimes;
            this.timesheetFrequency = timesheetFrequency;
        }
    }

    private TimeEntryContext buildTimeEntryContext(Response existingLogsResponse, String dayPattern,
            String regularHours, String actualWorkTime, String breakTime,
            Integer timesheetFrequency) {
        JsonPath jsonPath = existingLogsResponse.jsonPath();
        Map<String, Object> data = jsonPath.getMap("data");
        List<Map<String, Object>> existingTimeLogs = (List<Map<String, Object>>) data.get("timeLogs");

        List<Integer> workDays = parseWorkDaysFromPattern(dayPattern);
        Map<String, Map<String, Integer>> perDayWorkTimes = parsePerDayWorkTimes(actualWorkTime);
        boolean explicitDayEntries = hasExplicitDayNames(actualWorkTime);

        Map<String, Map<String, Integer>> multiWeekWorkTimes = new HashMap<>();
        Map<String, Map<String, Map<String, Integer>>> detailedMultiWeekWorkTimes = new HashMap<>();
        if (timesheetFrequency == 3 || timesheetFrequency == 4) {
            detailedMultiWeekWorkTimes = parseMultiWeekDetailedWorkTimes(actualWorkTime);
            if (detailedMultiWeekWorkTimes.isEmpty()) {
                multiWeekWorkTimes = parseMultiWeekWorkTimes(actualWorkTime);
            }
        }

        Map<String, Integer> regularTimes = parseTimeRange(regularHours);
        if (regularTimes.isEmpty()) {
            regularTimes.put(START_TIME, DEFAULT_WORK_START_TIME);
            regularTimes.put(END_TIME, DEFAULT_WORK_END_TIME);
        }

        List<Map<String, Object>> breakIntervals = parseBreakIntervals(breakTime);
        Integer totalBreakTime = calculateTotalBreakTime(breakIntervals);
        Map<String, Integer> perDayBreakTimes = parsePerDayBreakTimes(breakTime);

        return new TimeEntryContext(existingTimeLogs, workDays, perDayWorkTimes, explicitDayEntries,
                multiWeekWorkTimes, detailedMultiWeekWorkTimes, regularTimes, breakIntervals,
                totalBreakTime, perDayBreakTimes, timesheetFrequency);
    }

    private boolean shouldLogTimeForDay(int dayIndex, TimeEntryContext context, Long startDate) {
        if (context.timesheetFrequency == 4 && !context.detailedMultiWeekWorkTimes.isEmpty()) {
            return shouldLogTimeForMonthlyDetailedDay(dayIndex, context, startDate);
        } else if (context.timesheetFrequency == 4) {
            int dayOfMonth = dayIndex + 1;
            return shouldLogTimeForMonthlyDay(dayOfMonth, context.workDays, startDate);
        } else if (context.timesheetFrequency == 3 && !context.detailedMultiWeekWorkTimes.isEmpty()) {
            return shouldLogTimeForBiweeklyDetailedDay(dayIndex, context);
        } else {
            int dayOfWeek = (dayIndex % 7) + 1;
            String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";
            boolean hasWorkData = context.perDayWorkTimes.containsKey(dayName);

            if (context.explicitDayEntries) {
                return hasWorkData;
            }
            return context.workDays.contains(dayOfWeek);
        }
    }

    private boolean shouldLogTimeForBiweeklyDetailedDay(int dayIndex, TimeEntryContext context) {
        int dayOfPeriod = dayIndex + 1;
        int weekNumber = ((dayOfPeriod - 1) / 7) + 1;
        String weekKey = "week" + weekNumber;
        int dayOfWeek = ((dayIndex % 7) + 1);
        String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

        Map<String, Map<String, Integer>> weekDayPatterns = context.detailedMultiWeekWorkTimes.get(weekKey);
        return (weekDayPatterns != null && weekDayPatterns.containsKey(dayName) &&
                !weekDayPatterns.get(dayName).isEmpty());
    }

    private boolean shouldLogTimeForMonthlyDetailedDay(int dayIndex, TimeEntryContext context, Long startDate) {
        int dayOfMonth = dayIndex + 1;
        int weekNumber = ((dayOfMonth - 1) / 7) + 1;
        String weekKey = "week" + weekNumber;

        // Get the actual day of week for this day of the month
        int dayOfWeek = getDayOfWeekForMonthlyDay(dayOfMonth, startDate);
        String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

        Map<String, Map<String, Integer>> weekDayPatterns = context.detailedMultiWeekWorkTimes.get(weekKey);
        return (weekDayPatterns != null && weekDayPatterns.containsKey(dayName) &&
                !weekDayPatterns.get(dayName).isEmpty());
    }

    private Map<String, Integer> getDayWorkTimes(int dayIndex, TimeEntryContext context,
            Integer timesheetFrequency, Long startDate) {
        if ((timesheetFrequency == 3 || timesheetFrequency == 4) && !context.detailedMultiWeekWorkTimes.isEmpty()) {
            return getDetailedMultiWeekWorkTimes(dayIndex, context, timesheetFrequency, startDate);
        } else if ((timesheetFrequency == 3 || timesheetFrequency == 4) && !context.multiWeekWorkTimes.isEmpty()) {
            return getSimpleMultiWeekWorkTimes(dayIndex, context);
        } else {
            return getRegularDayWorkTimes(dayIndex, context, timesheetFrequency, startDate);
        }
    }

    private Map<String, Integer> getDetailedMultiWeekWorkTimes(int dayIndex, TimeEntryContext context,
            Integer timesheetFrequency, Long startDate) {
        int dayOfPeriod = dayIndex + 1;
        int weekNumber = ((dayOfPeriod - 1) / 7) + 1;
        String weekKey = "week" + weekNumber;

        int dayOfWeek = (timesheetFrequency == 4) ? getDayOfWeekForMonthlyDay(dayOfPeriod, startDate)
                : ((dayIndex % 7) + 1);
        String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

        Map<String, Map<String, Integer>> weekDayPatterns = context.detailedMultiWeekWorkTimes.get(weekKey);
        if (weekDayPatterns != null) {
            Map<String, Integer> dayWorkTimes = weekDayPatterns.get(dayName);
            if (dayWorkTimes != null && !dayWorkTimes.isEmpty()) {
                return dayWorkTimes;
            }
        }
        return context.regularTimes;
    }

    private Map<String, Integer> getSimpleMultiWeekWorkTimes(int dayIndex, TimeEntryContext context) {
        int dayOfPeriod = dayIndex + 1;
        int weekNumber = ((dayOfPeriod - 1) / 7) + 1;
        String weekKey = "week" + weekNumber;

        Map<String, Integer> dayWorkTimes = context.multiWeekWorkTimes.get(weekKey);
        if (dayWorkTimes != null && !dayWorkTimes.isEmpty()) {
            return dayWorkTimes;
        }
        return context.multiWeekWorkTimes.values().stream().findFirst().orElse(context.regularTimes);
    }

    private Map<String, Integer> getRegularDayWorkTimes(int dayIndex, TimeEntryContext context,
            Integer timesheetFrequency, Long startDate) {
        int dayOfWeek = (timesheetFrequency == 4) ? getDayOfWeekForMonthlyDay(dayIndex + 1, startDate)
                : (dayIndex % 7) + 1;

        String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

        Map<String, Integer> dayWorkTimes = context.perDayWorkTimes.get(dayName);
        if (dayWorkTimes != null && !dayWorkTimes.isEmpty()) {
            return dayWorkTimes;
        }
        return context.regularTimes;
    }

    private Map<String, Object> createWorkingDayLog(WorkingDayLogParams params) {
        Integer dayStartTime = params.dayWorkTimes.get(START_TIME);
        Integer dayEndTime = params.dayWorkTimes.get(END_TIME);

        if (dayStartTime == null || dayEndTime == null) {
            return null;
        }

        // Calculate break threshold overtime and adjusted values
        BreakThresholdResult breakResult = calculateBreakThresholdLogic(
                params.totalBreakTime, params.breakTimeThreshold, params.breakBillable);

        Integer totalTime = calculateTotalTimeWithBreakThreshold(dayStartTime, dayEndTime,
                params.totalBreakTime, params.breakBillable, breakResult.breakThresholdOvertime);
        Integer overTime = calculateOvertime(dayStartTime, dayEndTime,
                params.regularTimes.get(START_TIME), params.regularTimes.get(END_TIME));

        // Add break threshold overtime to regular overtime
        overTime += breakResult.breakThresholdOvertime;

        Map<String, Object> log = new HashMap<>();
        log.put(ID, params.logId);
        log.put(TIMESHEET_ID, params.timesheetId);
        log.put(BREAK_INTERVALS, params.breakIntervals);

        addWorkTimeFields(log, params.workLogType, dayStartTime, dayEndTime, totalTime, overTime);
        addBreakTimeField(log, params.breakBillable, params.totalBreakTime, params.workLogType);

        return log;
    }

    private Integer calculateTotalTime(Integer dayStartTime, Integer dayEndTime, Integer totalBreakTime,
            String breakBillable) {
        Integer totalTime = dayEndTime - dayStartTime;
        if (breakBillable.equalsIgnoreCase("No") && totalBreakTime > 0) {
            totalTime -= totalBreakTime;
        }
        return totalTime;
    }

    private BreakThresholdResult calculateBreakThresholdLogic(Integer actualBreakTime,
            Integer breakTimeThreshold, String breakBillable) {

        // Break threshold logic only applies when breaks are not billable and threshold
        // is set
        if (breakBillable.equalsIgnoreCase("No") && breakTimeThreshold != null && breakTimeThreshold > 0) {
            if (actualBreakTime < breakTimeThreshold) {
                // If actual break is less than threshold, the difference becomes overtime
                Integer breakThresholdOvertime = breakTimeThreshold - actualBreakTime;
                return new BreakThresholdResult(breakThresholdOvertime);
            }
        }

        // Normal case: no break threshold overtime
        return new BreakThresholdResult(0);
    }

    private Integer calculateTotalTimeWithBreakThreshold(Integer dayStartTime, Integer dayEndTime,
            Integer totalBreakTime, String breakBillable, Integer breakThresholdOvertime) {
        Integer totalTime = dayEndTime - dayStartTime;

        if (breakBillable.equalsIgnoreCase("No") && totalBreakTime > 0) {
            totalTime -= totalBreakTime;
        }

        // Add break threshold overtime to total time
        totalTime += breakThresholdOvertime;

        return totalTime;
    }

    private void addWorkTimeFields(Map<String, Object> log, Integer workLogType, Integer dayStartTime,
            Integer dayEndTime, Integer totalTime, Integer overTime) {
        if (workLogType == HOURS_METHOD) {
            // For hourly: workTime is the gross time, totalTime is net billable time
            Integer workTime = dayEndTime - dayStartTime; // Gross work time including breaks
            log.put(WORK_TIME, workTime);
            log.put(TOTAL_TIME, totalTime); // Net time after break deduction
        } else {
            log.put(WORK_START_TIME, dayStartTime);
            log.put(WORK_END_TIME, dayEndTime);
            log.put(TOTAL_TIME, totalTime);
            log.put(OVER_TIME, overTime);
        }
    }

    private void addBreakTimeField(Map<String, Object> log, String breakBillable, Integer totalBreakTime,
            Integer workLogType) {
        if (workLogType == HOURS_METHOD) {
            log.put(BREAK_TIME, totalBreakTime > 0 ? totalBreakTime : -1);
        } else {
            log.put(BREAK_TIME, totalBreakTime > 0 ? totalBreakTime : 0);
        }
    }

    private Map<String, Object> createEmptyDayLog(Integer logId, Integer timesheetId, Integer workLogType) {
        Map<String, Object> emptyLog = new HashMap<>();
        emptyLog.put(ID, logId);
        emptyLog.put(TIMESHEET_ID, timesheetId);
        emptyLog.put(BREAK_INTERVALS, new ArrayList<>());

        if (workLogType == HOURS_METHOD) {
            // For hourly-based: empty logs don't need workTime, totalTime, or breakTime
            // Just breakIntervals is sufficient
        } else {
            emptyLog.put(WORK_START_TIME, 0);
            emptyLog.put(WORK_END_TIME, 0);
            emptyLog.put(TOTAL_TIME, 0);
            emptyLog.put(OVER_TIME, 0);
        }

        return emptyLog;
    }

    private Response submitTimeLogUpdate(String authToken, List<Map<String, Object>> updatedTimeLogs) {
        return submitTimeLogUpdate(authToken, updatedTimeLogs, null, null);
    }

    private Response submitTimeLogUpdate(String authToken, List<Map<String, Object>> updatedTimeLogs,
            Integer timesheetId, Map<Integer, Integer> overtimeByLogId) {

        int totalWorkTimeSum = 0;
        int totalOvertimeSum = 0;

        for (Map<String, Object> log : updatedTimeLogs) {
            if (overtimeByLogId != null && log.get(ID) != null) {
                int apiOvertime = overtimeByLogId.getOrDefault((Integer) log.get(ID), 0);
                log.put(OVER_TIME, apiOvertime);
                totalOvertimeSum += apiOvertime;
            }
            Object totalTimeObj = log.get(TOTAL_TIME);
            if (totalTimeObj instanceof Number) {
                totalWorkTimeSum += ((Number) totalTimeObj).intValue();
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("timeLogs", updatedTimeLogs);

        if (timesheetId != null) {
            Map<String, Object> timeDetail = new HashMap<>();
            timeDetail.put(TIMESHEET_ID, timesheetId);
            timeDetail.put("totalWorkTime", totalWorkTimeSum);
            timeDetail.put("totalOvertime", totalOvertimeSum);
            timeDetail.put(TOTAL_TIME, totalWorkTimeSum);
            List<Map<String, Object>> timeDetails = new ArrayList<>();
            timeDetails.add(timeDetail);
            payload.put("timeDetails", timeDetails);
        }

        payload.put("timesheetIdNoLogChanges", new ArrayList<>());
        payload.put("isApproved", 0);
        payload.put("save", 0);

        String jsonPayload = TestUtil.getSerializedJSON(payload);
        Response response = RestClient.doPatchOnce("JSON", timesheetBaseURL, "timesheets/bulk/time-logs",
                authToken, null, true, jsonPayload);

        assertThat("Update time logs should return 200",
                response.getStatusCode(), equalTo(HTTP_OK));
        return response;
    }

    private boolean shouldLogTimeForMonthlyDay(int dayOfMonth, List<Integer> workDays, Long startDate) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(startDate * 1000);
            cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
            int javaDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int ourDayOfWeek;
            if (javaDayOfWeek == java.util.Calendar.SUNDAY) {
                ourDayOfWeek = 7;
            } else {
                ourDayOfWeek = javaDayOfWeek - 1;
            }
            return workDays.contains(ourDayOfWeek);

        } catch (Exception e) {
            int dayOfWeek = ((dayOfMonth - 1) % 7) + 1;
            return workDays.contains(dayOfWeek);
        }
    }

    protected Response getTimesheetsForContractor(String authToken, Integer jobId, Integer contractorId) {

        try {
            String endpoint = String.format("timesheets/job/contractor/get?jobId=%d&contractorId=%d&page=1&size=100",
                    jobId, contractorId);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sortPriorityList", Arrays.asList());
            String jsonPayload = com.qa.api.util.TestUtil.getSerializedJSON(requestBody);
            Response response = RestClient.doPost("JSON", timesheetBaseURL, endpoint, authToken, null, true,
                    jsonPayload);
            assertThat("Get timesheets should return 200",
                    response.getStatusCode(), equalTo(HTTP_OK));
            return response;

        } catch (Exception e) {
            throw new AssertionError("Error getting timesheets: " + e.getMessage(), e);
        }
    }

    protected List<Integer> parseWorkDaysFromPattern(String dayPattern) {
        if (isNullOrEmpty(dayPattern)) {
            return new ArrayList<>();
        }

        String pattern = dayPattern.toLowerCase().trim();

        if (isListFormat(pattern)) {
            return parseListFormat(pattern);
        } else if (isRangeFormat(pattern)) {
            return parseRangeFormat(pattern);
        } else if (isAllDaysPattern(pattern)) {
            return parseAllDays();
        }

        return new ArrayList<>();
    }

    private boolean isListFormat(String pattern) {
        return pattern.startsWith("[") && pattern.endsWith("]");
    }

    private boolean isRangeFormat(String pattern) {
        return pattern.contains("-");
    }

    private boolean isAllDaysPattern(String pattern) {
        return "all days".equals(pattern);
    }

    private List<Integer> parseListFormat(String pattern) {
        String content = pattern.substring(1, pattern.length() - 1);
        String[] days = content.split(",");
        List<Integer> workDays = new ArrayList<>();

        for (String day : days) {
            Integer dayId = getDayNumber(day.trim());
            if (dayId != null && !workDays.contains(dayId)) {
                workDays.add(dayId);
            }
        }

        Collections.sort(workDays);
        return workDays;
    }

    private List<Integer> parseRangeFormat(String pattern) {
        String[] parts = pattern.split("-");
        if (parts.length != 2) {
            return new ArrayList<>();
        }

        Integer startId = getDayNumber(parts[0].trim());
        Integer endId = getDayNumber(parts[1].trim());

        if (startId == null || endId == null) {
            return new ArrayList<>();
        }

        List<Integer> workDays = new ArrayList<>();
        for (int i = startId; i <= endId; i++) {
            workDays.add(i);
        }

        return workDays;
    }

    private List<Integer> parseAllDays() {
        List<Integer> workDays = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            workDays.add(i);
        }
        return workDays;
    }

    private Integer getDayNumber(String dayName) {
        return DAY_TO_NUMBER.get(dayName.toLowerCase());
    }

    private int convertTimeToSeconds(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty())
            return 0;

        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 2) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                return hours * SECONDS_IN_HOUR + minutes * SECONDS_IN_MINUTE;
            }
        } catch (NumberFormatException e) {
            System.err.println("Warning: Invalid time format '" + timeStr + "': " + e.getMessage());
        }
        return 0;
    }

    protected Map<String, Map<String, Map<String, Integer>>> parseMultiWeekDetailedWorkTimes(String actualWorkTime) {
        Map<String, Map<String, Map<String, Integer>>> weekPatterns = new HashMap<>();
        if (isNullOrEmpty(actualWorkTime)) {
            return weekPatterns;
        }
        String input = actualWorkTime.trim();
        if (isDetailedWeekPattern(input)) {
            parseDetailedWeekEntries(input, weekPatterns);
        }
        return weekPatterns;
    }

    private boolean isDetailedWeekPattern(String input) {
        return input.toLowerCase().contains("week") && input.contains("[");
    }

    private void parseDetailedWeekEntries(String input, Map<String, Map<String, Map<String, Integer>>> weekPatterns) {
        Matcher matcher = WEEK_DETAILED_PATTERN.matcher(input);
        while (matcher.find()) {
            String weekKey = "week" + matcher.group(1);
            String weekContent = matcher.group(2);
            Map<String, Map<String, Integer>> weekDayTimes = parsePerDayWorkTimes(weekContent);
            if (!weekDayTimes.isEmpty()) {
                weekPatterns.put(weekKey, weekDayTimes);
            }
        }
    }

    private Map<String, Map<String, Integer>> parseMultiWeekWorkTimes(String actualWorkTime) {
        Map<String, Map<String, Integer>> weekPatterns = new HashMap<>();
        if (isNullOrEmpty(actualWorkTime)) {
            return weekPatterns;
        }
        String input = actualWorkTime.trim();
        if (isSimpleWeekPattern(input)) {
            parseSimpleWeekEntries(input, weekPatterns);
        }
        return weekPatterns;
    }

    private boolean isSimpleWeekPattern(String input) {
        return input.toLowerCase().contains("week") && !input.contains("[");
    }

    private void parseSimpleWeekEntries(String input, Map<String, Map<String, Integer>> weekPatterns) {
        String[] weekEntries = input.split(",");
        for (String weekEntry : weekEntries) {
            parseSimpleWeekEntry(weekEntry.trim(), weekPatterns);
        }
    }

    private void parseSimpleWeekEntry(String entry, Map<String, Map<String, Integer>> weekPatterns) {
        Matcher matcher = WEEK_SIMPLE_PATTERN.matcher(entry);
        if (matcher.find()) {
            String weekKey = "week" + matcher.group(1);
            String timeRange = matcher.group(2) + "-" + matcher.group(3);
            Map<String, Integer> weekTimes = parseTimeRange(timeRange);
            if (!weekTimes.isEmpty()) {
                weekPatterns.put(weekKey, weekTimes);
            }
        }
    }

    protected Map<String, Map<String, Integer>> parsePerDayWorkTimes(String actualWorkTime) {
        if (isNullOrEmpty(actualWorkTime)) {
            return new HashMap<>();
        }
        String input = actualWorkTime.trim();

        // Strip "Week1: [...]" wrapper if present (unified format for weekly frequency)
        Matcher weekWrapper = Pattern.compile("(?i)week\\d+\\s*:\\s*\\[(.+)\\]").matcher(input);
        if (weekWrapper.matches()) {
            input = weekWrapper.group(1).trim();
        }

        String[] entries = input.contains(",") ? input.split(",") : new String[] { input };
        Map<String, Map<String, Integer>> perDayTimes = new HashMap<>();
        for (String entry : entries) {
            parseSingleDayEntry(entry.trim(), perDayTimes);
        }
        if (perDayTimes.isEmpty()) {
            return applyTimeToAllDays(parseTimeRange(input));
        }
        return perDayTimes;
    }

    private void parseSingleDayEntry(String entry, Map<String, Map<String, Integer>> perDayTimes) {
        Matcher matcher = DAY_PREFIX_PATTERN.matcher(entry);
        if (matcher.find()) {
            String dayKey = normalizeDayKey(matcher.group(1));
            String timeRange = entry.substring(matcher.end()).trim();
            Map<String, Integer> dayTimes = parseTimeRange(timeRange);
            if (!dayTimes.isEmpty()) {
                perDayTimes.put(dayKey, dayTimes);
            }
        }
    }

    private Map<String, Map<String, Integer>> applyTimeToAllDays(Map<String, Integer> times) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        if (!times.isEmpty()) {
            for (String day : ALL_DAYS) {
                result.put(day, new HashMap<>(times));
            }
        }
        return result;
    }

    private String normalizeDayKey(String rawDay) {
        if (rawDay == null)
            return "mon";
        return DAY_ABBREVIATIONS.getOrDefault(rawDay.trim().toLowerCase(), "mon");
    }

    private static final Pattern DAY_NAME_ANYWHERE_PATTERN = Pattern.compile(
            "(?i)(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s*:");

    private boolean hasExplicitDayNames(String actualWorkTime) {
        if (actualWorkTime == null || actualWorkTime.trim().isEmpty()) {
            return false;
        }
        return DAY_NAME_ANYWHERE_PATTERN.matcher(actualWorkTime).find();
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty() || value.equalsIgnoreCase(NONE_VALUE);
    }

    private int getDayOfWeekForMonthlyDay(int dayOfMonth, Long startDate) {
        try {
            long dailySeconds = SECONDS_IN_DAY; // seconds in a day
            long targetDate = startDate + ((dayOfMonth - 1) * dailySeconds);

            // Convert to Java date and get day of week
            java.util.Date date = new java.util.Date(targetDate * 1000);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);

            int javaDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            // Convert Java's Sunday=1 to our Monday=1 system
            return (javaDayOfWeek == 1) ? 7 : javaDayOfWeek - 1;
        } catch (Exception e) {
            return 1; // Default to Monday
        }
    }

    private Integer calculateOvertime(Integer workStartTime, Integer workEndTime,
            Integer regularStartTime, Integer regularEndTime) {
        if (workStartTime == null || workEndTime == null ||
                regularStartTime == null || regularEndTime == null) {
            return 0;
        }

        Integer totalWorkTime = workEndTime - workStartTime;
        Integer regularWorkTime = regularEndTime - regularStartTime;

        // Overtime is any time beyond regular working hours
        return Math.max(0, totalWorkTime - regularWorkTime);
    }

    private Map<String, Integer> parseTimeRange(String timeRangeStr) {
        if (timeRangeStr == null || timeRangeStr.trim().isEmpty()) {
            return new HashMap<>();
        }
        String input = timeRangeStr.trim();
        if (isHourlyDurationFormat(input.toLowerCase())) {
            return parseHourlyDuration(input.toLowerCase());
        }
        if (input.contains("-")) {
            return parseTimeRangeFormat(input);
        }
        return new HashMap<>();
    }

    private boolean isHourlyDurationFormat(String input) {
        return input.contains("hour") || input.contains("hr") || input.contains("h") ||
                input.contains("min") || input.contains("m");
    }

    private Map<String, Integer> parseHourlyDuration(String input) {
        int totalSeconds = parseTimeHourFormat(input);
        if (totalSeconds < 0) {
            totalSeconds = parseCompactHourFormat(input);
        }
        if (totalSeconds < 0) {
            totalSeconds = parseVerboseHourFormat(input);
        }
        Map<String, Integer> times = new HashMap<>();
        if (totalSeconds > 0) {
            times.put(START_TIME, DEFAULT_WORK_START_TIME);
            times.put(END_TIME, DEFAULT_WORK_START_TIME + totalSeconds);
        }
        return times;
    }

    private int parseTimeHourFormat(String input) {
        Matcher timeHour = TIME_HOUR_PATTERN.matcher(input.trim());
        if (timeHour.find()) {
            int hours = Integer.parseInt(timeHour.group(1));
            int minutes = Integer.parseInt(timeHour.group(2));
            return hours * SECONDS_IN_HOUR + minutes * SECONDS_IN_MINUTE;
        }
        return -1;
    }

    private int parseCompactHourFormat(String input) {
        Matcher compact = COMPACT_HOUR_PATTERN.matcher(input);
        if (compact.find() && (compact.group(1) != null || compact.group(2) != null)) {
            int hours = compact.group(1) != null ? Integer.parseInt(compact.group(1)) : 0;
            int minutes = compact.group(2) != null ? Integer.parseInt(compact.group(2)) : 0;
            return hours * SECONDS_IN_HOUR + minutes * SECONDS_IN_MINUTE;
        }
        return -1;
    }

    private int parseVerboseHourFormat(String input) {
        double hoursPart = extractHours(input);
        int minutesPart = extractMinutes(input);
        boolean hasHours = hoursPart > 0;
        boolean hasMinutes = minutesPart > 0;
        if (hasHours || hasMinutes) {
            return (int) Math.round(hoursPart * SECONDS_IN_HOUR) + minutesPart * SECONDS_IN_MINUTE;
        }
        return -1;
    }

    private double extractHours(String input) {
        Matcher hoursMatcher = HOURS_PATTERN.matcher(input);
        return hoursMatcher.find() ? Double.parseDouble(hoursMatcher.group(1)) : 0.0;
    }

    private int extractMinutes(String input) {
        Matcher minutesMatcher = MINUTES_PATTERN.matcher(input);
        return minutesMatcher.find() ? Integer.parseInt(minutesMatcher.group(1)) : 0;
    }

    private Map<String, Integer> parseTimeRangeFormat(String input) {
        Map<String, Integer> times = new HashMap<>();
        String[] parts = input.split("-");
        if (parts.length == 2) {
            times.put(START_TIME, convertTimeToSeconds(parts[0].trim()));
            times.put(END_TIME, convertTimeToSeconds(parts[1].trim()));
        }
        return times;
    }

    protected List<Map<String, Object>> buildCustomRulesFromDescription(String rulesApplied,
            List<Integer> workDayIds,
            double payRate, double billRate, String method) {
        List<Map<String, Object>> customRules = new ArrayList<>();

        if (rulesApplied == null || rulesApplied.equals(NONE_VALUE)) {
            return customRules;
        }

        // Split rules by comma and process each
        String[] ruleDescriptions = rulesApplied.split(",");
        int ruleCounter = 1;

        for (String ruleDesc : ruleDescriptions) {
            ruleDesc = ruleDesc.trim();

            // Skip "Regular Hours" rules - they should be handled by base pay/bill rates
            if (ruleDesc.toLowerCase().contains(REGULAR_HOURS_RULE)) {
                continue; // Skip creating a rule for regular hours
            }

            Map<String, Object> rule = createRuleFromDescription(ruleDesc, workDayIds, payRate, billRate, ruleCounter,
                    method);
            customRules.add(rule);
            ruleCounter++;
        }

        return customRules;
    }

    private Map<String, Object> createRuleFromDescription(String ruleDesc, List<Integer> workDayIds,
            double payRate, double billRate, int ruleCounter, String method) {
        Map<String, Object> rule = createBaseRule(ruleCounter, workDayIds);
        configureRuleCharging(rule, ruleDesc, payRate, billRate);
        configureRuleType(rule, ruleDesc, method);
        return rule;
    }

    private Map<String, Object> createBaseRule(int ruleCounter, List<Integer> workDayIds) {
        Map<String, Object> rule = new HashMap<>();
        rule.put(ID, 0);
        rule.put(RULE_NAME, "Rule " + ruleCounter);
        rule.put(WORK_DAY_ID, new ArrayList<>(workDayIds));
        rule.put(START_DURATION, 0);
        rule.put(END_DURATION, 0);
        rule.put(DAILY_THRESHOLD, 0);
        rule.put(WEEKLY_THRESHOLD, 0);
        return rule;
    }

    private void configureRuleCharging(Map<String, Object> rule, String ruleDesc, double payRate, double billRate) {

        if (ruleDesc.toLowerCase().contains("fixed")) {
            configureFixedRateCharging(rule, ruleDesc, payRate, billRate);
        } else {
            configureMultiplierCharging(rule, ruleDesc);
        }
    }

    private void configureFixedRateCharging(Map<String, Object> rule, String ruleDesc, double payRate,
            double billRate) {
        rule.put(CHARGE_METHOD, FIXED_RATE_CHARGE);
        rule.put(PAY_RATE_MULTIPLIER, 1.0);
        rule.put(BILL_RATE_MULTIPLIER, 1.0);

        String[] rates = extractFixedRates(ruleDesc);
        if (rates.length >= 2) {
            rule.put(PAY_RATE_PER_HOUR, Double.parseDouble(rates[0]));
            rule.put(BILL_RATE_PER_HOUR, Double.parseDouble(rates[1]));
        } else {
            rule.put(PAY_RATE_PER_HOUR, payRate);
            rule.put(BILL_RATE_PER_HOUR, billRate);
        }
    }

    private void configureMultiplierCharging(Map<String, Object> rule, String ruleDesc) {
        rule.put(CHARGE_METHOD, MULTIPLIER_CHARGE);
        rule.put(PAY_RATE_PER_HOUR, 0.0);
        rule.put(BILL_RATE_PER_HOUR, 0.0);

        double multiplier = extractMultiplierFromRule(ruleDesc);
        rule.put(PAY_RATE_MULTIPLIER, multiplier);
        rule.put(BILL_RATE_MULTIPLIER, multiplier);
    }

    private void configureRuleType(Map<String, Object> rule, String ruleDesc, String method) {

        if (method.equalsIgnoreCase("Hours")) {
            configureHourlyRuleType(rule, ruleDesc);
        } else {
            configureShiftRuleType(rule, ruleDesc);
        }
    }

    private void configureHourlyRuleType(Map<String, Object> rule, String ruleDesc) {
        String lowerDesc = ruleDesc.toLowerCase().trim();

        if (lowerDesc.contains("daily ot") || lowerDesc.contains("daily overtime")) {
            rule.put(RULE_TYPE, DAILY_OVERTIME_HOURS_RULE);
            rule.put(START_TIME, 0);
            rule.put(END_TIME, 0);
            rule.put(START_DURATION, 0);
            rule.put(END_DURATION, 0);
            rule.put(DAILY_THRESHOLD, extractThresholdFromRule(ruleDesc));
        } else if (lowerDesc.contains("weekly ot") || lowerDesc.contains("weekly overtime")) {
            rule.put(RULE_TYPE, WEEKLY_OVERTIME_HOURS_RULE);
            rule.put(START_TIME, 0);
            rule.put(END_TIME, 0);
            rule.put(START_DURATION, 0);
            rule.put(END_DURATION, 0);
            rule.put(WORK_DAY_ID, Arrays.asList(1, 2, 3, 4, 5, 6, 7));
            rule.put(WEEKLY_THRESHOLD, extractThresholdFromRule(ruleDesc));
        } else if (lowerDesc.contains("specific") && lowerDesc.contains("range")) {
            configureSpecificHoursRange(rule, ruleDesc);
        } else {
            rule.put(RULE_TYPE, DAILY_OVERTIME_HOURS_RULE);
            rule.put(START_TIME, 0);
            rule.put(END_TIME, 0);
            rule.put(START_DURATION, 0);
            rule.put(END_DURATION, 0);
            rule.put(DAILY_THRESHOLD, 10 * SECONDS_IN_HOUR); // 10 hours default
        }
    }

    private void configureSpecificHoursRange(Map<String, Object> rule, String ruleDesc) {
        rule.put(RULE_TYPE, SPECIFIC_HOURS_RANGE_RULE);
        rule.put(START_TIME, 0);
        rule.put(END_TIME, 0);

        int startHours = 1;
        int endHours = 2;
        try {
            Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*-\\s*(\\d+(?:\\.\\d+)?)\\s*hr")
                    .matcher(ruleDesc.toLowerCase());
            if (m.find()) {
                startHours = (int) Math.floor(Double.parseDouble(m.group(1)));
                endHours = (int) Math.floor(Double.parseDouble(m.group(2)));
                if (endHours < startHours) {
                    int tmp = startHours;
                    startHours = endHours;
                    endHours = tmp;
                }
            }
        } catch (Exception e) {
            System.err
                    .println("Warning: Failed to parse specific hours range from rule description: " + e.getMessage());
        }

        rule.put(START_DURATION, startHours * SECONDS_IN_HOUR);
        rule.put(END_DURATION, endHours * SECONDS_IN_HOUR);
    }

    private void configureShiftRuleType(Map<String, Object> rule, String ruleDesc) {
        String lowerDesc = ruleDesc.toLowerCase();

        if (lowerDesc.contains("before shift")) {
            rule.put(RULE_TYPE, BEFORE_SHIFT_RULE);
            rule.put(START_TIME, extractTimeFromRule(ruleDesc));
            rule.put(END_TIME, 0);
        } else if (lowerDesc.contains("after shift")) {
            rule.put(RULE_TYPE, AFTER_SHIFT_RULE);
            rule.put(START_TIME, extractTimeFromRule(ruleDesc));
            rule.put(END_TIME, 0);
        } else if (lowerDesc.contains("specific range")) {
            rule.put(RULE_TYPE, SPECIFIC_RANGE_RULE);
            Map<String, Integer> timeRange = extractTimeRangeFromRule(ruleDesc);
            rule.put(START_TIME, timeRange.get(START_TIME));
            rule.put(END_TIME, timeRange.get(END_TIME));
        } else if (lowerDesc.trim().contains("daily ot") || lowerDesc.trim().contains("daily overtime")) {
            rule.put(RULE_TYPE, DAILY_OVERTIME_SHIFT_RULE);
            rule.put(START_TIME, 0);
            rule.put(END_TIME, 0);
            rule.put(DAILY_THRESHOLD, extractThresholdFromRule(ruleDesc));
        } else if (lowerDesc.contains("weekly ot") || lowerDesc.contains("weekly overtime")) {
            rule.put(RULE_TYPE, WEEKLY_OVERTIME_SHIFT_RULE);
            rule.put(START_TIME, 0);
            rule.put(END_TIME, 0);
            rule.put(WORK_DAY_ID, Arrays.asList(1, 2, 3, 4, 5, 6, 7));
            rule.put(WEEKLY_THRESHOLD, extractThresholdFromRule(ruleDesc));
        } else {
            rule.put(RULE_TYPE, AFTER_SHIFT_RULE);
            rule.put(START_TIME, 0);
            rule.put(END_TIME, 0);
        }
    }

    private String[] extractFixedRates(String ruleDesc) {
        try {
            // Look for pattern like "$35/$55"
            Pattern pattern = Pattern.compile("\\$(\\d+(?:\\.\\d+)?)/\\$(\\d+(?:\\.\\d+)?)");
            Matcher matcher = pattern.matcher(ruleDesc);
            if (matcher.find()) {
                return new String[] { matcher.group(1), matcher.group(2) };
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to extract fixed rates from rule description: " + e.getMessage());
        }
        return new String[] { "0", "0" };
    }

    private Map<String, Integer> extractTimeRangeFromRule(String ruleDesc) {
        Map<String, Integer> result = new HashMap<>();
        try {
            // Look for pattern like "(16:00-18:00)"
            Pattern pattern = Pattern.compile("\\((\\d{1,2}:\\d{2})-(\\d{1,2}:\\d{2})\\)");
            Matcher matcher = pattern.matcher(ruleDesc);
            if (matcher.find()) {
                result.put("startTime", convertTimeToSeconds(matcher.group(1)));
                result.put("endTime", convertTimeToSeconds(matcher.group(2)));
            } else {
                result.put("startTime", 0);
                result.put("endTime", 0);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to extract time range from rule description: " + e.getMessage());
            result.put("startTime", 0);
            result.put("endTime", 0);
        }
        return result;
    }

    private Integer extractTimeFromRule(String ruleDesc) {
        try {
            // Look for pattern like "(8:00)" or "(18:00)"
            Pattern pattern = Pattern.compile("\\((\\d{1,2}:\\d{2})\\)");
            Matcher matcher = pattern.matcher(ruleDesc);
            if (matcher.find()) {
                return convertTimeToSeconds(matcher.group(1));
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to extract time from rule description: " + e.getMessage());
        }
        return 0;
    }

    private double extractMultiplierFromRule(String ruleDesc) {
        try {
            // Look for pattern like "1x", "1.5x", "2x", "1.25x", "1.75x"
            Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)x");
            Matcher matcher = pattern.matcher(ruleDesc);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to extract multiplier from rule description: " + e.getMessage());
        }
        return 1.0;
    }

    private Integer extractThresholdFromRule(String ruleDesc) {
        try {
            // pattern like ">8 hrs", ">40 hrs", ">8.5 hrs"
            Pattern pattern = Pattern.compile(">(\\d+(?:\\.\\d+)?)\\s*hrs?");
            Matcher matcher = pattern.matcher(ruleDesc);
            if (matcher.find()) {
                double hours = Double.parseDouble(matcher.group(1));
                return (int) (hours * SECONDS_IN_HOUR); // Convert hours to seconds
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to extract threshold from rule description: " + e.getMessage());
        }
        return 0;
    }

    protected List<Map<String, Object>> parseBreakIntervals(String breakTime) {
        List<Map<String, Object>> breakIntervals = new ArrayList<>();

        if (breakTime == null || breakTime.equals(NONE_VALUE) || breakTime.trim().isEmpty()) {
            return breakIntervals;
        }

        try {
            // Split by comma to get individual break intervals
            String[] intervals = breakTime.split(",");

            for (String interval : intervals) {
                interval = interval.trim();
                if (interval.contains("-")) {
                    // Handle time range format: "12:00-13:00"
                    String[] times = interval.split("-");
                    if (times.length == 2) {
                        Integer breakStartTime = convertTimeToSeconds(times[0].trim());
                        Integer breakEndTime = convertTimeToSeconds(times[1].trim());

                        Map<String, Object> breakInterval = new HashMap<>();
                        breakInterval.put("timelogId", 0); // Will be set when used
                        breakInterval.put("breakStartTime", breakStartTime);
                        breakInterval.put("breakEndTime", breakEndTime);

                        breakIntervals.add(breakInterval);
                    }
                } else if (interval.matches("\\d{1,2}:\\d{2}")) {
                    Integer breakDuration = convertTimeToSeconds(interval);
                    if (breakDuration > 0) {
                        Map<String, Object> breakInterval = new HashMap<>();
                        breakInterval.put("timelogId", 0);
                        breakInterval.put("breakStartTime", 0);
                        breakInterval.put("breakEndTime", breakDuration);
                        breakIntervals.add(breakInterval);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to parse break intervals: " + e.getMessage());
        }

        return breakIntervals;
    }

    protected Integer calculateTotalBreakTime(List<Map<String, Object>> breakIntervals) {
        int totalBreakTime = 0;

        for (Map<String, Object> interval : breakIntervals) {
            Integer startTime = (Integer) interval.get("breakStartTime");
            Integer endTime = (Integer) interval.get("breakEndTime");
            if (startTime != null && endTime != null) {
                totalBreakTime += (endTime - startTime);
            }
        }

        return totalBreakTime;
    }

    protected Response getTimeLogsForTimesheet(String authToken, Integer timesheetId) {
        try {
            String endpoint = "timesheets/" + timesheetId + "/time-logs";
            return RestClient.doGet("JSON", timesheetBaseURL, endpoint, authToken, null, null, true);
        } catch (Exception e) {
            throw new AssertionError("Error getting time logs: " + e.getMessage(), e);
        }
    }

    protected void validateRuleEngineCalculations(String testId, Response evaluationResponse,
            double expectedPay, double expectedBill) {
        try {
            JsonPath evalJsonPath = evaluationResponse.jsonPath();
            Double actualPayAmount = evalJsonPath.getDouble("data.evaluationSummary.totalPayAmount");
            Double actualBillAmount = evalJsonPath.getDouble("data.evaluationSummary.totalBillAmount");

            validateAmountCalculations(testId, actualPayAmount, actualBillAmount, expectedPay, expectedBill);

        } catch (Exception e) {
            throw new AssertionError("Error validating calculations for " + testId + ": " + e.getMessage(), e);
        }
    }

    private void validateAmountCalculations(String testId, Double actualPay, Double actualBill,
            double expectedPay, double expectedBill) {
        boolean payWithinTolerance = Math.abs(actualPay - expectedPay) <= AMOUNT_TOLERANCE;
        boolean billWithinTolerance = Math.abs(actualBill - expectedBill) <= AMOUNT_TOLERANCE;
        assertThat(String.format(
                "Mismatch for %s: expected Pay $%.2f / Bill $%.2f, actual Pay $%.2f / Bill $%.2f",
                testId, expectedPay, expectedBill, actualPay, actualBill),
                payWithinTolerance && billWithinTolerance, is(true));
    }

    // ========================================================================
    // SECTION: EVALUATE-OVERTIME API (Hours-based)
    // ========================================================================

    private List<Map<String, Object>> buildEvaluateOvertimePayloadForHours(
            List<Map<String, Object>> existingTimeLogs, Integer timesheetId,
            Map<String, Map<String, Integer>> perDayWorkTimes,
            Map<String, Map<String, Map<String, Integer>>> detailedMultiWeekWorkTimes,
            Integer timesheetFrequency, Long startDate, String breakTimeStr) {

        Map<String, Integer> perDayBreaks = parsePerDayBreakTimes(breakTimeStr);
        Integer fallbackBreakSeconds = parseBreakTimeToSeconds(breakTimeStr);

        List<Map<String, Object>> timeLogs = new ArrayList<>();
        for (int i = 0; i < existingTimeLogs.size(); i++) {
            Map<String, Object> timeLog = existingTimeLogs.get(i);
            Integer logId = (Integer) timeLog.get("id");
            String timesheetPeriod = timeLog.get("timesheetPeriod") != null
                    ? timeLog.get("timesheetPeriod").toString() : "";
            Object dateObj = timeLog.get("date");
            long dateEpoch = (dateObj instanceof Number) ? ((Number) dateObj).longValue() : 0L;
            int dayTypeId = Integer.parseInt(timeLog.get("dayTypeId").toString());

            int dayIndex = i;
            int weekNumber = (dayIndex / 7) + 1;
            String weekKey = "week" + weekNumber;
            int dayOfWeek;
            if (timesheetFrequency == 4) {
                dayOfWeek = getDayOfWeekForMonthlyDay(dayIndex + 1, startDate);
            } else {
                dayOfWeek = (dayIndex % 7) + 1;
            }
            String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

            int workTimeSeconds = resolveWorkTimeForDay(dayName, weekKey, perDayWorkTimes,
                    detailedMultiWeekWorkTimes);

            Integer dayBreakSeconds = null;
            if (workTimeSeconds > 0) {
                if (!perDayBreaks.isEmpty()) {
                    dayBreakSeconds = perDayBreaks.getOrDefault(dayName, null);
                } else {
                    dayBreakSeconds = fallbackBreakSeconds;
                }
            }

            Map<String, Object> log = new HashMap<>();
            log.put(ID, logId);
            log.put("date", dateEpoch);
            log.put("dayTypeId", dayTypeId);
            log.put(WORK_TIME, workTimeSeconds);
            log.put(WORK_START_TIME, null);
            log.put(WORK_END_TIME, null);
            log.put(BREAK_TIME, dayBreakSeconds);
            log.put(BREAK_INTERVALS, null);
            log.put("workTimeDetails", null);
            log.put(OVER_TIME, null);
            log.put("remark", null);
            log.put(TOTAL_TIME, workTimeSeconds);
            log.put(TIMESHEET_ID, timesheetId);
            log.put("timesheetPeriod", timesheetPeriod);
            timeLogs.add(log);
        }
        return timeLogs;
    }

    private Integer parseBreakTimeToSeconds(String breakTimeStr) {
        if (breakTimeStr == null || breakTimeStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = breakTimeStr.trim();
        if (trimmed.matches("\\d{1,2}:\\d{2}")) {
            String[] parts = trimmed.split(":");
            return Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60;
        }
        return null;
    }

    private Map<String, Integer> parsePerDayBreakTimes(String breakTimeStr) {
        Map<String, Integer> perDayBreaks = new HashMap<>();
        if (breakTimeStr == null || breakTimeStr.trim().isEmpty()) {
            return perDayBreaks;
        }
        java.util.regex.Pattern weekPattern = java.util.regex.Pattern.compile(
                "(?i)Week\\d+\\s*:\\s*\\[(.+?)\\]");
        java.util.regex.Matcher weekMatcher = weekPattern.matcher(breakTimeStr);
        if (weekMatcher.find()) {
            String weekContent = weekMatcher.group(1);
            java.util.regex.Pattern dayPattern = java.util.regex.Pattern.compile(
                    "(?i)(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s*:\\s*([\\d.]+)\\s*hours?");
            java.util.regex.Matcher dayMatcher = dayPattern.matcher(weekContent);
            while (dayMatcher.find()) {
                String dayName = dayMatcher.group(1).toLowerCase().substring(0, 3);
                double hours = Double.parseDouble(dayMatcher.group(2));
                int seconds = (int) (hours * 3600);
                perDayBreaks.put(dayName, seconds);
            }
        }
        return perDayBreaks;
    }

    private Integer resolveBreakTimeForDay(int dayIndex, TimeEntryContext context,
            Integer timesheetFrequency, Long startDate) {
        int dayOfWeek;
        if (timesheetFrequency == 4) {
            dayOfWeek = getDayOfWeekForMonthlyDay(dayIndex + 1, startDate);
        } else {
            dayOfWeek = (dayIndex % 7) + 1;
        }
        String dayName = (dayOfWeek > 0 && dayOfWeek < DAY_NAMES.length) ? DAY_NAMES[dayOfWeek] : "mon";

        if (!context.perDayBreakTimes.isEmpty()) {
            return context.perDayBreakTimes.getOrDefault(dayName, 0);
        }
        return context.totalBreakTime;
    }

    private int resolveWorkTimeForDay(String dayName, String weekKey,
            Map<String, Map<String, Integer>> perDayWorkTimes,
            Map<String, Map<String, Map<String, Integer>>> detailedMultiWeekWorkTimes) {

        if (detailedMultiWeekWorkTimes != null && !detailedMultiWeekWorkTimes.isEmpty()) {
            Map<String, Map<String, Integer>> weekData = detailedMultiWeekWorkTimes.get(weekKey);
            if (weekData != null && weekData.containsKey(dayName)) {
                Map<String, Integer> dayTimes = weekData.get(dayName);
                Integer start = dayTimes.get(START_TIME);
                Integer end = dayTimes.get(END_TIME);
                if (start != null && end != null) {
                    return end - start;
                }
            }
            return 0;
        }
        if (perDayWorkTimes != null && !perDayWorkTimes.isEmpty()) {
            Map<String, Integer> dayTimes = perDayWorkTimes.get(dayName);
            if (dayTimes != null) {
                Integer start = dayTimes.get(START_TIME);
                Integer end = dayTimes.get(END_TIME);
                if (start != null && end != null) {
                    return end - start;
                }
            }
        }
        return 0;
    }

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
     * Parses weeklyOvertimeHours from test data. Format: "week1:5, week2:10"
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

    protected void assertOvertimeFromApi(String testId,
            Map<Integer, Map<Integer, Integer>> overtimeApiMap,
            Map<String, Map<String, Double>> expectedOvertimeHours,
            List<Map<String, Object>> existingTimeLogs,
            Integer timesheetId, Integer timesheetFrequency, Long startDate) {

        Map<Integer, Integer> logOvertimeMap = overtimeApiMap.getOrDefault(timesheetId, new HashMap<>());

        for (int i = 0; i < existingTimeLogs.size(); i++) {
            Integer logId = (Integer) existingTimeLogs.get(i).get("id");
            int dayIndex = i;
            int weekNumber = (dayIndex / 7) + 1;
            String weekKey = "week" + weekNumber;
            int dayOfWeek;
            if (timesheetFrequency == 4) {
                dayOfWeek = getDayOfWeekForMonthlyDay(dayIndex + 1, startDate);
            } else {
                dayOfWeek = (dayIndex % 7) + 1;
            }
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

    protected Map<Integer, Integer> buildOvertimeMapForTimelogs(
            Map<Integer, Map<Integer, Integer>> overtimeApiMap, Integer timesheetId) {
        if (overtimeApiMap == null || overtimeApiMap.isEmpty()) {
            return new HashMap<>();
        }
        return overtimeApiMap.getOrDefault(timesheetId, new HashMap<>());
    }

    // ========================================================================
    // SECTION: COMBINED VALIDATION (Total + Per-Rule + Weekly OT)
    // ========================================================================

    private static final Map<String, String> RULE_TYPE_TO_JSON_KEY = new HashMap<>();
    static {
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_BEFORE_SHIFT", "beforeShift");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_AFTER_SHIFT", "afterShift");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_SPECIFIC_TIME_RANGE", "specificHoursRange");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_DAILY_OVERTIME", "dailyOT");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_REGULAR_HOURS", "regularHours");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_BREAK", "Break");
        RULE_TYPE_TO_JSON_KEY.put("RANGE_BASED_DEFAULT_PAY", "unallocatedHours");
        RULE_TYPE_TO_JSON_KEY.put("DURATION_BASED_REGULAR_HOURS", "regularHours");
        RULE_TYPE_TO_JSON_KEY.put("DURATION_BASED_DAILY_OVERTIME", "dailyOT");
        RULE_TYPE_TO_JSON_KEY.put("DURATION_BASED_WEEKLY_OVERTIME", "weeklyOT");
        RULE_TYPE_TO_JSON_KEY.put("DURATION_BASED_SPECIFIC_HOURS_RANGE", "specificHoursRange");
        RULE_TYPE_TO_JSON_KEY.put("DURATION_BASED_DEFAULT_PAY", "unallocatedHours");
    }

    protected void validateAllAmounts(String testId, Response evaluationResponse,
            double expectedPay, double expectedBill,
            String acceptedPayBillRateStr, String weeklyOTStr) {
        validateAllAmounts(testId, evaluationResponse, expectedPay, expectedBill,
                acceptedPayBillRateStr, weeklyOTStr, null);
    }

    protected void validateAllAmounts(String testId, Response evaluationResponse,
            double expectedPay, double expectedBill,
            String acceptedPayBillRateStr, String weeklyOTStr,
            Integer isUnplannedHoursPayEnabled) {
        StringBuilder allErrors = new StringBuilder();

        try {
            JsonPath evalJsonPath = evaluationResponse.jsonPath();

            Double actualPay = evalJsonPath.getDouble("data.evaluationSummary.totalPayAmount");
            Double actualBill = evalJsonPath.getDouble("data.evaluationSummary.totalBillAmount");

            boolean totalPayOk = Math.abs(actualPay - expectedPay) <= AMOUNT_TOLERANCE;
            boolean totalBillOk = Math.abs(actualBill - expectedBill) <= AMOUNT_TOLERANCE;

            if (!totalPayOk || !totalBillOk) {
                allErrors.append(String.format(
                        "\n\n  TOTAL MISMATCH: expected Pay=$%.2f / Bill=$%.2f, actual Pay=$%.2f / Bill=$%.2f",
                        expectedPay, expectedBill, actualPay, actualBill));
            } else {
                allErrors.append(String.format(
                        "\n\n  TOTAL OK: Pay=$%.2f, Bill=$%.2f", actualPay, actualBill));
            }

            boolean anyRuleMismatch = false;
            if (acceptedPayBillRateStr != null && !acceptedPayBillRateStr.trim().isEmpty()) {
                Map<String, Map<String, double[]>> expectedPerDayPerRule = parseAcceptedPayBillRate(
                        acceptedPayBillRateStr);
                Map<String, Map<String, double[]>> actualPerDayPerRule = buildActualPerDayPerRuleMap(evalJsonPath);

                StringBuilder ruleDetails = new StringBuilder();
                for (Map.Entry<String, Map<String, double[]>> dayEntry : expectedPerDayPerRule.entrySet()) {
                    String day = dayEntry.getKey();
                    Map<String, double[]> expectedRules = dayEntry.getValue();
                    Map<String, double[]> actualRules = actualPerDayPerRule.getOrDefault(day, new LinkedHashMap<>());

                    for (Map.Entry<String, double[]> ruleEntry : expectedRules.entrySet()) {
                        String ruleKey = ruleEntry.getKey();

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
                                    "\n      MISMATCH %s -> %s: expected Pay=%.2f/Bill=%.2f, actual Pay=%.2f/Bill=%.2f",
                                    day.toUpperCase(), ruleKey, expected[0], expected[1], actual[0], actual[1]));
                        } else {
                            ruleDetails.append(String.format(
                                    "\n      OK %s -> %s: Pay=%.2f/Bill=%.2f",
                                    day.toUpperCase(), ruleKey, actual[0], actual[1]));
                        }
                    }
                }
                allErrors.append(anyRuleMismatch
                        ? "\n\n  PER-RULE BREAKDOWN (has mismatches):"
                        : "\n\n  PER-RULE BREAKDOWN (all match):");
                allErrors.append(ruleDetails);
            }

            boolean anyWotMismatch = false;
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
                        otPay = getDoubleFromObj(weeklyOvertime.get("weeklyOvertimePayAmount"));
                        otBill = getDoubleFromObj(weeklyOvertime.get("weeklyOvertimeBillAmount"));
                    }
                    String weekKey = "week" + (i + 1);
                    actualWeeklyOT.put(weekKey, new double[] { otPay, otBill });
                }

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
                                "\n      MISMATCH %s: expected Pay=%.2f/Bill=%.2f, actual Pay=%.2f/Bill=%.2f",
                                weekKey, expected[0], expected[1], actual[0], actual[1]));
                    } else {
                        wotDetails.append(String.format(
                                "\n      OK %s: Pay=%.2f/Bill=%.2f", weekKey, actual[0], actual[1]));
                    }
                }
                allErrors.append(anyWotMismatch
                        ? "\n\n  WEEKLY OT (has mismatches):"
                        : "\n\n  WEEKLY OT (all match):");
                allErrors.append(wotDetails);
            }

            boolean hasAnyFailure = (!totalPayOk || !totalBillOk || anyRuleMismatch || anyWotMismatch);
            if (hasAnyFailure) {
                throw new AssertionError("[" + testId + "] Calculation validation FAILED:" + allErrors);
            }

        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError("[" + testId + "] Error during combined validation: "
                    + e.getMessage(), e);
        }
    }

    private Map<String, Map<String, double[]>> buildActualPerDayPerRuleMap(JsonPath evalJsonPath) {
        Map<String, Map<String, double[]>> actualPerDayPerRule = new LinkedHashMap<>();

        List<Map<String, Object>> weeklyResults = evalJsonPath.getList("data.weeklyResults");
        boolean isMultiWeek = weeklyResults.size() > 1;

        for (int weekIdx = 0; weekIdx < weeklyResults.size(); weekIdx++) {
            Map<String, Object> weekResult = weeklyResults.get(weekIdx);
            List<Map<String, Object>> timeLogEvaluations = (List<Map<String, Object>>) weekResult
                    .get("timeLogRuleEvaluations");
            if (timeLogEvaluations == null) continue;

            for (Map<String, Object> timeLogEval : timeLogEvaluations) {
                String dateStr = (String) timeLogEval.get("date");
                String dayName = getDayNameFromDate(dateStr);
                String dayKey = isMultiWeek ? "week" + (weekIdx + 1) + ":" + dayName : dayName;

                List<Map<String, Object>> ruleResults = (List<Map<String, Object>>) timeLogEval
                        .get("ruleEvaluationResults");
                if (ruleResults == null) continue;

                for (Map<String, Object> ruleResult : ruleResults) {
                    String ruleType = (String) ruleResult.get("ruleType");
                    if (ruleType == null) continue;

                    String jsonKey = RULE_TYPE_TO_JSON_KEY.get(ruleType);
                    if (jsonKey == null) continue;

                    Double payAmount = getDoubleFromObj(ruleResult.get("payAmount"));
                    Double billAmount = getDoubleFromObj(ruleResult.get("billAmount"));

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

    protected Map<String, Map<String, double[]>> parseAcceptedPayBillRate(String acceptedPayBillRate) {
        Map<String, Map<String, double[]>> result = new LinkedHashMap<>();
        if (acceptedPayBillRate == null || acceptedPayBillRate.trim().isEmpty()) {
            return result;
        }
        String input = acceptedPayBillRate.trim();
        List<String> dayEntries = splitDayEntries(input);

        for (String dayEntry : dayEntries) {
            dayEntry = dayEntry.trim();
            if (dayEntry.isEmpty()) continue;

            int colonBracket = dayEntry.indexOf(":[");
            if (colonBracket == -1) continue;

            String dayName = dayEntry.substring(0, colonBracket).trim().toLowerCase();
            String normalizedDay = DAY_ABBREVIATIONS.getOrDefault(dayName, dayName);

            String content = dayEntry.substring(colonBracket + 2);
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }

            Map<String, double[]> ruleAmounts = parseRuleAmountsFromDayContent(content);
            result.put(normalizedDay, ruleAmounts);
        }
        return result;
    }

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
                if (bracketDepth == 0) {
                    entries.add(current.toString().trim());
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
            entries.add(remaining);
        }
        return entries;
    }

    private Map<String, double[]> parseRuleAmountsFromDayContent(String content) {
        Map<String, double[]> ruleAmounts = new LinkedHashMap<>();
        if (content == null || content.trim().isEmpty()) return ruleAmounts;

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
            if (rulePart.isEmpty()) continue;

            int colonBracket = rulePart.indexOf(":[");
            if (colonBracket == -1) continue;

            String ruleKey = rulePart.substring(0, colonBracket).trim();
            String valuesStr = rulePart.substring(colonBracket + 2);
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
                    // skip invalid
                }
            }
        }
        return ruleAmounts;
    }

    protected Map<String, double[]> parseWeeklyOT(String weeklyOTStr) {
        Map<String, double[]> result = new LinkedHashMap<>();
        if (weeklyOTStr == null || weeklyOTStr.trim().isEmpty()) {
            return result;
        }
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
            if (weekEntry.isEmpty()) continue;

            int colonBracket = weekEntry.indexOf(":[");
            if (colonBracket == -1) continue;

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
                    // skip invalid
                }
            }
        }
        return result;
    }

    private String getDayNameFromDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "mon";
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return date.getDayOfWeek().toString().substring(0, 3).toLowerCase();
        } catch (Exception e) {
            return "mon";
        }
    }

    private Double getDoubleFromObj(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    protected int getWorkLogTypeFromMethod(String method) {
        return method.equalsIgnoreCase("Hours") ? HOURS_METHOD : SHIFTS_LOGGING;
    }

    protected Response assignCandidateToJob(String authToken, String candidateSlug, String jobSlug) {
        try {
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);
            String basePath = "candidates/{candidate}/assign";
            Map<String, String> queryParameters = new HashMap<>();
            queryParameters.put("job_slug", jobSlug);
            Response response = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParameters,
                    pathParameters, true, null);
            assertThat("Assignment should return 200",
                    response.getStatusCode(), equalTo(HTTP_OK));
            if (response.getStatusCode() == HTTP_OK) {
                JsonPath jsonPath = response.jsonPath();
                if (jsonPath.getString("candidate_slug") != null) {
                    // Success - no logging needed
                } else {
                    throw new AssertionError("Failed to assign candidate to job");
                }
            } else {
                throw new AssertionError("Failed to assign candidate to job. Status: " + response.getStatusCode());
            }
            return response;
        } catch (Exception e) {
            throw new AssertionError("Error assigning candidate to job: " + e.getMessage(), e);
        }
    }

    // ========== JSON-DRIVEN TEST EXECUTION (unified flow) ==========

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
                                String acceptedPayBillRate, String weeklyOT,
                                Long jobStartDate, Long jobEndDate,
                                Integer timesheetFrequency, Integer timesheetStartDay,
                                Integer payCurrencyId, Integer billCurrencyId,
                                Integer breakTimeThreshold, int workLogType, String comment,
                                Integer isUnplannedHoursPayEnabled,
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

        public String getTestId() { return testId; }
        public String getMethod() { return method; }
        public String getDayPattern() { return dayPattern; }
        public String getRegularHours() { return regularHours; }
        public String getActualWorkTime() { return actualWorkTime; }
        public String getRulesApplied() { return rulesApplied; }
        public String getBreakTime() { return breakTime; }
        public String getBreakBillable() { return breakBillable; }
        public double getPayRate() { return payRate; }
        public double getBillRate() { return billRate; }
        public double getExpectedTotalPay() { return expectedTotalPay; }
        public double getExpectedTotalBill() { return expectedTotalBill; }
        public String getAcceptedPayBillRate() { return acceptedPayBillRate; }
        public String getWeeklyOT() { return weeklyOT; }
        public Long getJobStartDate() { return jobStartDate; }
        public Long getJobEndDate() { return jobEndDate; }
        public Integer getTimesheetFrequency() { return timesheetFrequency; }
        public Integer getTimesheetStartDay() { return timesheetStartDay; }
        public Integer getPayCurrencyId() { return payCurrencyId; }
        public Integer getBillCurrencyId() { return billCurrencyId; }
        public Integer getBreakTimeThreshold() { return breakTimeThreshold; }
        public int getWorkLogType() { return workLogType; }
        public String getComment() { return comment; }
        public Integer getIsUnplannedHoursPayEnabled() { return isUnplannedHoursPayEnabled; }
        public String getOvertimeHours() { return overtimeHours; }
        public String getWeeklyOvertimeHours() { return weeklyOvertimeHours; }
    }

    protected TestScenarioData extractScenarioData(Map<String, Object> scenario) {
        String testId = getStringValue(scenario, "testId");
        String method = getStringValue(scenario, "method");
        String dayPattern = getStringValue(scenario, "dayPattern");
        String regularHours = getStringValue(scenario, "regularHours");
        String actualWorkTime = getStringValue(scenario, "actualWorkTime");
        String rulesApplied = getStringValue(scenario, "rulesApplied");
        String breakTime = getStringValue(scenario, "breakTime");
        String breakBillable = getStringValue(scenario, "breakBillable");
        double payRate = getDoubleValue(scenario, "payRate");
        double billRate = getDoubleValue(scenario, "billRate");
        double expectedTotalPay = getDoubleValue(scenario, "expectedTotalPay");
        double expectedTotalBill = getDoubleValue(scenario, "expectedTotalBill");
        String acceptedPayBillRate = getStringValue(scenario, "acceptedPayBillRate");
        String weeklyOT = getStringValue(scenario, "weeklyOT");
        Long jobStartDate = getLongValue(scenario, "jobStartDate");
        Long jobEndDate = getLongValue(scenario, "jobEndDate");
        Integer timesheetFrequency = getIntValue(scenario, "timesheetFrequency");
        Integer timesheetStartDay = getIntValue(scenario, "timesheetStartDay");
        Integer payCurrencyId = getIntValue(scenario, "payCurrencyId");
        Integer billCurrencyId = getIntValue(scenario, "billCurrencyId");
        Integer breakTimeThreshold = getIntValue(scenario, "breakTimeThreshold");
        int workLogType = getWorkLogTypeFromMethod(method);
        String comment = getStringValue(scenario, "_comment");
        Integer isUnplannedHoursPayEnabled = getIntValue(scenario, "isUnplannedHoursPayEnabled");
        String overtimeHours = getStringValue(scenario, "overtimeHours");
        String weeklyOvertimeHours = getStringValue(scenario, "weeklyOvertimeHours");

        return new TestScenarioData(testId, method, dayPattern, regularHours, actualWorkTime,
                rulesApplied, breakTime, breakBillable, payRate, billRate, expectedTotalPay,
                expectedTotalBill, acceptedPayBillRate, weeklyOT, jobStartDate, jobEndDate,
                timesheetFrequency, timesheetStartDay, payCurrencyId, billCurrencyId,
                breakTimeThreshold, workLogType, comment, isUnplannedHoursPayEnabled,
                overtimeHours, weeklyOvertimeHours);
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (NumberFormatException e) { return 0.0; }
        }
        return 0.0;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            try { return Long.parseLong((String) val); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    /**
     * Unified test execution method for hours-based rule engine tests.
     * Runs the complete flow:
     * 1. Extract scenario data from JSON map
     * 2. Parse rules and work days
     * 3. Create rule template
     * 4. Setup entities (candidate, company, contact, job)
     * 5. Enable timesheet settings (auto-selects hourly/shift and frequency)
     * 6. Create timesheet from free slots
     * 7. Get time logs and call evaluate-overtime API
     * 7a. Assert overtime from API matches expected
     * 7b. Update time logs with overtime from API
     * 8. Approve and evaluate
     * 9. Combined validation: Total pay/bill + Per-day per-rule + Weekly OT
     */
    protected Integer executeHoursBasedTest(Map<String, Object> scenario, String albatrossAuthToken,
                                            String apiAuthToken, io.rcrm.api.commanfunctions.commanFunction function,
                                            List<Integer> createdTemplateIds) {
        TestScenarioData data = extractScenarioData(scenario);
        String testId = data.getTestId();

        try {
            List<Integer> workDayIds = parseWorkDaysFromPattern(data.getDayPattern());
            List<ParsedRule> parsedRules = parseRulesFromJson(data.getRulesApplied(), workDayIds);
            List<Map<String, Object>> customRules = buildCustomRulesFromParsedRules(parsedRules,
                    workDayIds, data.getPayRate(), data.getBillRate(), data.getMethod());
            int workLogType = data.getWorkLogType();

            String templateName = ruleEngineenFake.getTestTemplateName(testId);
            Integer templateId = createRuleTemplate(albatrossAuthToken, templateName, workDayIds,
                    data.getRegularHours(), customRules, data.getBreakBillable(), workLogType,
                    data.getBreakTimeThreshold(), data.getIsUnplannedHoursPayEnabled());
            assertThat("Template should be created for " + testId, templateId, notNullValue());
            if (createdTemplateIds != null) {
                createdTemplateIds.add(templateId);
            }

            JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String candidateSlug = jsonCandidate.getString("slug");
            Integer realCandidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
            assertThat("Real candidate ID should be fetched for " + testId, realCandidateId, notNullValue());

            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String companySlug = jsonCompany.getString("slug");
            JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
            String contactSlug = jsonContact.getString("slug");
            JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
            String jobSlug = jsonJob.getString("slug");

            Response usersResponse = function.getUsers(baseURL, apiAuthToken);
            int userId = usersResponse.jsonPath().getInt("[0].id");
            int jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                    .jsonPath().getInt("data.job.id");
            assignCandidateToJob(apiAuthToken, candidateSlug, jobSlug);

            boolean isMonthly = data.getTimesheetFrequency() == 4;
            Response timesheetResponse = enableTimesheetWithDynamicValuesInternal(albatrossAuthToken, jobId,
                    realCandidateId, userId, templateId, data.getDayPattern(), data.getRegularHours(),
                    data.getRulesApplied(), data.getPayRate(), data.getBillRate(), data.getBreakBillable(),
                    data.getJobStartDate(), data.getJobEndDate(), data.getTimesheetFrequency(),
                    data.getTimesheetStartDay(), data.getPayCurrencyId(), data.getBillCurrencyId(),
                    isMonthly, workLogType, data.getBreakTimeThreshold(),
                    data.getIsUnplannedHoursPayEnabled());
            assertThat("Timesheet settings should succeed for " + testId,
                    timesheetResponse.getStatusCode(), equalTo(200));

            Response freeSlotsResponse = getFreeSlotsForTimesheet(albatrossAuthToken, realCandidateId,
                    data.getJobStartDate(), data.getJobEndDate(), data.getTimesheetFrequency(),
                    data.getTimesheetStartDay());
            assertThat("Free slots should return 200 for " + testId,
                    freeSlotsResponse.getStatusCode(), equalTo(200));

            Response createTimesheetResponse = createTimesheetFromSlots(albatrossAuthToken, jobId,
                    realCandidateId, freeSlotsResponse);
            assertThat("Create timesheet should return 200 for " + testId,
                    createTimesheetResponse.getStatusCode(), equalTo(200));

            Response timesheetsResponse = getTimesheetsForContractor(albatrossAuthToken, jobId, realCandidateId);
            assertThat("Get timesheets should return 200 for " + testId,
                    timesheetsResponse.getStatusCode(), equalTo(200));

            List<Map<String, Object>> timesheets = timesheetsResponse.jsonPath().getList("data");
            assertThat("Timesheets should not be empty for " + testId, timesheets.isEmpty(), is(false));

            Integer timesheetId = (Integer) timesheets.get(0).get("id");
            assertThat("Timesheet ID should be extracted for " + testId, timesheetId, notNullValue());

            Response timeLogsResponse = getTimeLogsForTimesheet(albatrossAuthToken, timesheetId);
            assertThat("Time logs should return 200 for " + testId,
                    timeLogsResponse.getStatusCode(), equalTo(200));

            // 7a. Call evaluate-overtime API
            Map<String, Map<String, Double>> expectedOvertimeMap = parseOvertimeHours(data.getOvertimeHours());

            JsonPath tlJsonPath = timeLogsResponse.jsonPath();
            Map<String, Object> tlData = tlJsonPath.getMap("data");
            List<Map<String, Object>> existingTimeLogs = (List<Map<String, Object>>) tlData.get("timeLogs");

            Map<String, Map<String, Integer>> perDayWorkTimes = parsePerDayWorkTimes(data.getActualWorkTime());
            Map<String, Map<String, Map<String, Integer>>> detailedMultiWeekWorkTimes = new HashMap<>();
            if (data.getTimesheetFrequency() == 3 || data.getTimesheetFrequency() == 4) {
                detailedMultiWeekWorkTimes = parseMultiWeekDetailedWorkTimes(data.getActualWorkTime());
            }

            List<Map<String, Object>> evalPayload = buildEvaluateOvertimePayloadForHours(
                    existingTimeLogs, timesheetId, perDayWorkTimes, detailedMultiWeekWorkTimes,
                    data.getTimesheetFrequency(), data.getJobStartDate(), data.getBreakTime());

            Response evalOvertimeResponse = callEvaluateOvertimeApi(albatrossAuthToken, evalPayload);
            assertThat("Evaluate-overtime API should return 200 for " + testId,
                    evalOvertimeResponse.getStatusCode(), equalTo(HTTP_OK));

            Map<Integer, Map<Integer, Integer>> overtimeApiMap = parseOvertimeApiResponse(evalOvertimeResponse);

            assertOvertimeFromApi(testId, overtimeApiMap, expectedOvertimeMap,
                    existingTimeLogs, timesheetId, data.getTimesheetFrequency(), data.getJobStartDate());

            Map<Integer, Integer> overtimeByLogId = buildOvertimeMapForTimelogs(overtimeApiMap, timesheetId);

            // 7b. Update time logs with overtime from evaluate-overtime API
            int updateWorkLogType = data.getMethod().equalsIgnoreCase("Hours") ? HOURS_METHOD : SHIFTS_LOGGING;
            updateTimeEntriesWithCsvData(albatrossAuthToken, timesheetId, updateWorkLogType, timeLogsResponse,
                    data.getDayPattern(), data.getRegularHours(), data.getActualWorkTime(),
                    data.getBreakTime(), data.getBreakBillable(), data.getTimesheetFrequency(),
                    data.getJobStartDate(), data.getBreakTimeThreshold(), overtimeByLogId,data.getComment());

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
            String msg = e.getMessage();
            if (msg != null && msg.contains(testId)) {
                throw e;
            }
            throw new AssertionError("[" + testId + "] " + msg, e);
        } catch (Exception e) {
            throw new AssertionError("[" + testId + "] Test scenario failed: " + e.getMessage(), e);
        }
    }
}