package io.recruitcrm.Filters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterValueFactory {

    private static final Map<String, Integer> ENTITY_TYPE_MAP;
    
    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("candidate", 5);
        map.put("company", 3);
        map.put("contact", 2);
        map.put("job", 4);
        map.put("user", 6);
        map.put("team", 52);
        map.put("deal", 11);
        map.put("calllog", 8);
        map.put("note", 14);
        map.put("task", 15);
        map.put("meeting", 16);
        map.put("hotlist", 50);
        map.put("saved_search", 51);
        map.put("company_note", 0);
        map.put("company_last_activities_t", 0);
        ENTITY_TYPE_MAP = Collections.unmodifiableMap(map);
    }
    

    public static JSONObject stringStartEndFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "STRING_START_END");
        JSONObject valueObj = new JSONObject();
        String[] parts = filterValue.split(",");
        valueObj.put("start", parts[0].trim());
        valueObj.put("end", parts[1].trim());
        filterValueObj.put("value", valueObj);
        return filterValueObj;
    }

    public static JSONObject dateStartEndFilterValue(String filterValue, DateConverter dateConverter) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "LONG_START_END");
        JSONObject valueObj = new JSONObject();
        String[] parts = filterValue.split(",");
        valueObj.put("start", dateConverter.toEpochSeconds(parts[0].trim()));
        valueObj.put("end", dateConverter.toEpochSeconds(parts[1].trim()));
        filterValueObj.put("value", valueObj);
        return filterValueObj;
    }

    public static JSONObject stringFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "STRING");
        filterValueObj.put("value", filterValue);
        return filterValueObj;
    }


    public static JSONObject dateIsFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "DATE_IS");
        filterValueObj.put("value", filterValue);
        return filterValueObj;
    }

    public static JSONObject longFilterValue(String filterValue, DateConverter dateConverter) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "LONG");
        long epochValue = dateConverter.toEpochSeconds(filterValue);
        filterValueObj.put("value", epochValue);
        return filterValueObj;
    }

    public static JSONObject integerListFilterValue(String filterValue) {
        List<Integer> integerList = new ArrayList<>();
        if (filterValue == null || filterValue.equals("")) {
            return emptyFilterValue("INTEGER_LIST");
        }
        for (String value : filterValue.split(",")) {
            String trimmedValue = value.trim();
            if (!trimmedValue.isEmpty()) {
                integerList.add(Integer.parseInt(trimmedValue));
            }
        }
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "INTEGER_LIST");
        filterValueObj.put("value", integerList);
        return filterValueObj;
    }

    public static JSONObject stringListFilterValue(String filterValue) {
        JSONArray stringArray = new JSONArray();
        for (String value : filterValue.split(",")) {
            stringArray.put(value.trim());
        }
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "STRING_LIST");
        filterValueObj.put("value", stringArray);
        return filterValueObj;
    }


    public static JSONObject stringListFilterValueWithIgnore(String filterValue) {
        if (filterValue == null) {
            return stringListFilterValueAsIs("");
        }
        String s = filterValue.trim();
        if (s.isEmpty()) {
            return stringListFilterValueAsIs("");
        }
        if (s.startsWith("/")) {
            return stringListFilterValueAsIs(s.substring(1).trim());
        }
        if (s.contains(",/")) {
            String[] segments = s.split(",/", -1);
            JSONArray stringArray = new JSONArray();
            for (int i = 0; i < segments.length; i++) {
                String seg = segments[i].trim();
                if (i == 0 && !seg.isEmpty()) {
                    for (String v : seg.split(",")) {
                        String t = v.trim();
                        if (!t.isEmpty()) stringArray.put(t);
                    }
                } else if (!seg.isEmpty()) {
                    stringArray.put(seg);
                }
            }
            JSONObject filterValueObj = new JSONObject();
            filterValueObj.put("type", "STRING_LIST");
            filterValueObj.put("value", stringArray);
            return filterValueObj;
        }
        return stringListFilterValue(filterValue);
    }

    public static JSONObject stringListFilterValueAsIs(String filterValue) {
        JSONArray stringArray = new JSONArray();
        stringArray.put(filterValue != null ? filterValue.trim() : "");
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "STRING_LIST");
        filterValueObj.put("value", stringArray);
        return filterValueObj;
    }

    public static JSONObject integerFilterValue(String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Filter value cannot be null or empty for INTEGER type");
        }
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "INTEGER");
        filterValueObj.put("value", Integer.parseInt(filterValue.trim()));
        return filterValueObj;
    }

    public static JSONObject integerStartEndFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "INTEGER_START_END");
        JSONObject valueObj = new JSONObject();
        String[] parts = filterValue.split(",");
        valueObj.put("start", Double.parseDouble(parts[0].trim()));
        valueObj.put("end", Double.parseDouble(parts[1].trim()));
        filterValueObj.put("value", valueObj);
        return filterValueObj;
    }

    public static JSONObject numericStringFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "NUMERIC_STRING");
        filterValueObj.put("value", filterValue);
        return filterValueObj;
    }

    public static JSONObject numericStringStartEndFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "NUMERIC_STRING_START_END");
        JSONObject valueObj = new JSONObject();
        String[] parts = filterValue.split(",");
        valueObj.put("start", parts[0].trim());
        valueObj.put("end", parts[1].trim());
        filterValueObj.put("value", valueObj);
        return filterValueObj;
    }

    public static JSONObject doubleFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "DOUBLE");
        filterValueObj.put("value", filterValue != null && !filterValue.trim().isEmpty() ? Double.parseDouble(filterValue.trim()) : 0.0);
        return filterValueObj;
    }

    public static JSONObject doubleStartEndFilterValue(String filterValue) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "DOUBLE_START_END");
        JSONObject valueObj = new JSONObject();
        String[] parts = filterValue.split(",");
        valueObj.put("start", Double.parseDouble(parts[0].trim()));
        valueObj.put("end", Double.parseDouble(parts[1].trim()));
        filterValueObj.put("value", valueObj);
        return filterValueObj;
    }

    public static JSONObject emptyFilterValue(String filterValue_TYPE) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        filterValueObj.put("value", new JSONArray());
        return filterValueObj;
    }

    public static JSONObject entityAssociationFilterValue(String filterValue) {
        // Extract entity type from filterValue : Team:[{team}],User:[{admin}]
        Map<String, List<Integer>> entityMap = parseEntityAssociationFilterValue(filterValue);
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", "ENTITY_ASSOCIATION");
        JSONArray valueArray = new JSONArray();
        for (Map.Entry<String, List<Integer>> entry : entityMap.entrySet()) {
            String entityType = entry.getKey();
            Integer entityTypeId = ENTITY_TYPE_MAP.get(entityType);
            List<Integer> entityIds = entry.getValue();
            if (entityIds.isEmpty()) {
                continue;
            }
            JSONObject valueObj = new JSONObject();
            valueObj.put("entityTypeId", entityTypeId);
            valueObj.put("entityIds", entityIds);
            valueArray.put(valueObj);
        }
        filterValueObj.put("value", valueArray);
        return filterValueObj;
    }

    public static Map<String, List<Integer>> parseEntityAssociationFilterValue(String input) {
        Map<String, List<Integer>> result = new HashMap<>();

        Pattern pattern = Pattern.compile("\"?([\\w]+)\"?\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(input);
    
        while (matcher.find()) {
            String key = matcher.group(1).trim().toLowerCase();
            String valuesPart = matcher.group(2).trim();
    
            List<Integer> values = new ArrayList<>();
    
            if (!valuesPart.isEmpty()) {
                for (String v : valuesPart.split(",")) {
                    String trimmedValue = v.trim();
                    trimmedValue = trimmedValue.replaceAll("^\"|\"$", "");
                    if (!trimmedValue.isEmpty()) {
                        try {
                            values.add(Integer.parseInt(trimmedValue));
                        } catch (NumberFormatException e) {
                            System.out.println("Skipping non-numeric value: " + trimmedValue);
                        }
                    }
                }
            }
    
            result.put(key, values);
        }
    
        return result;
    }
    
    
    
    @FunctionalInterface
    public interface DateConverter {
        long toEpochSeconds(String dateStr);
    }
}

