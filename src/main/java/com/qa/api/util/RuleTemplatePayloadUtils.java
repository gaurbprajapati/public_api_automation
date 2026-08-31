package com.qa.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RuleTemplatePayloadUtils {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Map<String, Object>> payloadTemplates = new HashMap<>();

    static {
        loadPayloadTemplates();
    }

    private static void loadPayloadTemplates() {
        loadTemplate("create", "/create-template-payload.json");
        loadTemplate("update", "/update-template-payload.json");
        loadTemplate("list", "/list-template-payload.json");
    }

    private static void loadTemplate(String key, String resourcePath) {
        try (InputStream is = RuleTemplatePayloadUtils.class.getResourceAsStream(resourcePath)) {
            Map<String, Object> templates = mapper.readValue(is, Map.class);
            payloadTemplates.put(key, templates);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + resourcePath, e);
        }
    }

    public static Map<String, String> getListParams(String search, int page, int size) {
        Map<String, String> params = new HashMap<>();
        if (search != null && !search.isEmpty()) {
            params.put("search", search);
        }
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        return params;
    }

}