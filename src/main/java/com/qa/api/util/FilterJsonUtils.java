package com.qa.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;

public class FilterJsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String createFilterJson(
            String groupType,
            String filterName,
            String dbField,
            String filterValue,
            String filterType,
            String fieldType,
            String filterBarLabel) throws IOException {

        // Create the filter request structure
        ObjectNode rootNode = objectMapper.createObjectNode();

        // Add sortPriorityList as empty array
        ArrayNode sortPriorityList = rootNode.putArray("sortPriorityList");

        // Add null fields
        rootNode.putNull("defaultFilterList");
        rootNode.putNull("booleanSearchList");

        // Create filterSearchList
        ObjectNode filterSearchList = rootNode.putObject("filterSearchList");

        // Create groupFilterList array with one item
        ArrayNode groupFilterList = filterSearchList.putArray("groupFilterList");
        ObjectNode groupFilter = groupFilterList.addObject();

        // Set groupFilterJoinOperator
        groupFilter.put("groupFilterJoinOperator", "AND");

        // Create filters array with one filter
        ArrayNode filters = groupFilter.putArray("filters");
        ObjectNode filter = filters.addObject();

        // Set filter properties
        filter.put("groupType", groupType);
        filter.put("filterName", filterName);
        filter.put("dbField", dbField);
        filter.put("filterValue", filterValue);
        filter.put("filterType", filterType);
        filter.put("fieldType", fieldType);
        filter.put("filterBarLabel", filterBarLabel);

        // Set groupJoinOperator
        filterSearchList.put("groupJoinOperator", "AND");

        // Convert to JSON string
        return objectMapper.writeValueAsString(rootNode);
    }

}