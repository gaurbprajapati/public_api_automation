package com.qa.api.util.reaper;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;


public final class PortalThreadManager {

    private static final ThreadLocal<Map<String, Object>> THREAD_DATA = ThreadLocal.withInitial(HashMap::new);

    private static final String KEY_JOB_ID = "portalJobId";
    private static final String KEY_CANDIDATE_ID = "portalCandidateId";
    private static final String KEY_RCRM_ACCOUNT_ID = "portalRcrmAccountId";
    private static final String KEY_VMS_USER_ID = "portalVmsUserId";
    private static final String KEY_VMS_ACCOUNT_ID = "portalVmsAccountId";
    private static final String KEY_COMPANY_ID = "portalCompanyId";
    private static final String KEY_BEARER_TOKEN = "portalBearerToken";
    private static final String KEY_REFRESH_TOKEN = "portalRefreshToken";
    private static final String KEY_CLIENT_PORTAL_EMAIL = "clientPortalEmail";
    private static final String KEY_CONTRACTOR_PORTAL_EMAIL = "contractorPortalEmail";

    private PortalThreadManager() {
    }

    private static void put(String key, Object value) {
        THREAD_DATA.get().put(key, value);
    }

    private static Object get(String key) {
        return THREAD_DATA.get().get(key);
    }

    public static void clear() {
        THREAD_DATA.remove();
    }

    public static void setJobId(int jobId) {
        put(KEY_JOB_ID, jobId);
    }

    public static int getJobId() {
        return intOrZero(get(KEY_JOB_ID));
    }

    public static void setCandidateId(int candidateId) {
        put(KEY_CANDIDATE_ID, candidateId);
    }

    public static int getCandidateId() {
        return intOrZero(get(KEY_CANDIDATE_ID));
    }

    public static void setRcrmAccountId(int rcrmAccountId) {
        put(KEY_RCRM_ACCOUNT_ID, rcrmAccountId);
    }

    public static int getRcrmAccountId() {
        return intOrZero(get(KEY_RCRM_ACCOUNT_ID));
    }

    public static void setVmsUserId(int vmsUserId) {
        put(KEY_VMS_USER_ID, vmsUserId);
    }

    public static int getVmsUserId() {
        return intOrZero(get(KEY_VMS_USER_ID));
    }

    public static void setVmsAccountId(int vmsAccountId) {
        put(KEY_VMS_ACCOUNT_ID, vmsAccountId);
    }

    public static int getVmsAccountId() {
        return intOrZero(get(KEY_VMS_ACCOUNT_ID));
    }

    public static void setCompanyId(int companyId) {
        put(KEY_COMPANY_ID, companyId);
    }

    public static int getCompanyId() {
        return intOrZero(get(KEY_COMPANY_ID));
    }

    public static void setBearerToken(String bearerToken) {
        put(KEY_BEARER_TOKEN, bearerToken);
    }

    public static String getBearerToken() {
        Object v = get(KEY_BEARER_TOKEN);
        return v == null ? null : v.toString();
    }

    public static void setRefreshToken(String refreshToken) {
        put(KEY_REFRESH_TOKEN, refreshToken);
    }

    public static String getRefreshToken() {
        Object v = get(KEY_REFRESH_TOKEN);
        return v == null ? null : v.toString();
    }

    public static void setClientPortalEmail(String email) {
        put(KEY_CLIENT_PORTAL_EMAIL, email);
    }

    public static String getClientPortalEmail() {
        Object v = get(KEY_CLIENT_PORTAL_EMAIL);
        return v == null ? null : v.toString();
    }

    public static void setContractorPortalEmail(String email) {
        put(KEY_CONTRACTOR_PORTAL_EMAIL, email);
    }

    public static String getContractorPortalEmail() {
        Object v = get(KEY_CONTRACTOR_PORTAL_EMAIL);
        return v == null ? null : v.toString();
    }
    public static Map<String, String> getBearerAuthHeaderMap() {
        String t = getBearerToken();
        Map<String, String> m = new HashMap<>();
        if (t != null && !t.isEmpty()) {
            m.put("Authorization", "Bearer " + t);
        }
        return m;
    }

    
    public static void applyCreateAccountResponse(Response response, int candidateEntityId, int rcrmCompanyId) {
        if (response == null) {
            return;
        }
        int jobId = response.jsonPath().getInt("jobID");
        int rcrmAccountId = response.jsonPath().getInt("rcrmAccountId");
        int vmsUserId = response.jsonPath().getInt("vmsUserID");
        int vmsAccountId = response.jsonPath().getInt("vmsAccountId");
        String email = response.jsonPath().getString("email");

        setJobId(jobId);
        setCandidateId(candidateEntityId);
        setRcrmAccountId(rcrmAccountId);
        setVmsUserId(vmsUserId);
        setVmsAccountId(vmsAccountId);
        setCompanyId(rcrmCompanyId);
        if (email != null) {
            setClientPortalEmail(email);
        }
    }

    public static void applyCreateContractorAccountResponse(Response response) {
        if (response == null) {
            return;
        }
        String email = response.jsonPath().getString("email");
        if (email != null) {
            setContractorPortalEmail(email);
        }
    }
    
    public static void applyLoginResponse(Response response) {
        if (response == null) {
            return;
        }
        String access = response.jsonPath().getString("data.access_token");
        String refresh = response.jsonPath().getString("data.refresh_token");
        setBearerToken(access);
        setRefreshToken(refresh);
    }

    private static int intOrZero(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
