package com.qa.api.util.reaper;

import io.rcrm.api.pojo.reaper.*;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;

import java.util.HashMap;
import java.util.Map;

public class ThreadManager {
    private static final ThreadLocal<Map<String, Object>> threadLocalData = ThreadLocal.withInitial(HashMap::new);
    static TestBase testBase = new TestBase();
    private ThreadManager() {
        // private constructor to hide the implicit public one
    }

    public static void setValue(String key, Object value) {
        Map<String, Object> threadData = threadLocalData.get();
        threadData.put(key, value);
    }

    public static Object getValue(String key) {
        Map<String, Object> threadData = threadLocalData.get();
        return threadData.get(key);
    }

    public static Account getAccount() {
        return (Account) getValue("account");
    }

    public static void setAccount(Account account) {
        setValue("account", account);
    }

    public static void clear() {
        threadLocalData.remove();
    }

    public static OwnerAccount getOwner(){
        return ((Account) getValue("account")).getOwner();
    }

    public static AdminAccount getAdmin(){
        return ((Account) getValue("account")).getAdmin();
    }

    public static TeamMemberAccount getTeamMember(){
        return ((Account) getValue("account")).getTeamMember();
    }

    public static RestrictedTeamMemberAccount getRestrictedTeamMember(){
        return ((Account) getValue("account")).getRestrictedTeamMember();
    }

    public static CustomRoleTeamOnly getCustomRoleTeamOnly(){
        return ((Account) getValue("account")).getCustomRoleTeamOnly();
    }

    public static CustomRoleNothing getCustomRoleNothing(){
        return ((Account) getValue("account")).getCustomRoleNothing();
    }

    public static String getAccountApiKey(){
        return ((Account) getValue("account")).getOwner().getApiKey();
    }

    public static Map<String, String> getAccountApiKeyMap(){
        HashMap<String, String> apiKeyMap = new HashMap<>();
        apiKeyMap.put("Authorization", "Bearer " +  ((Account) getValue("account")).getOwner().getApiKey());
        return apiKeyMap;
    }

    public static String getAlbatrossToken(String role){
        if(role.equalsIgnoreCase("Owner")){
            return getOwner().getToken();
        }else if(role.equalsIgnoreCase("Admin")) {
            return getAdmin().getToken();
        }else if(role.equalsIgnoreCase("TeamMember")) {
            return getTeamMember().getToken();
        }else if(role.equalsIgnoreCase("RestrictedTeamMember")) {
            return getRestrictedTeamMember().getToken();
        }else if(role.equalsIgnoreCase("CustomRoleTeamOnly")) {
            return getCustomRoleTeamOnly().getToken();
        }else if(role.equalsIgnoreCase("CustomRoleNothing")) {
            return getCustomRoleNothing().getToken();
        }else {
            return null;
        }
    }

    public static String[] getAlbatrossTokenAndUserId(String role){        
        String token = "";
        int userId = 0;
        switch (role) {
            case "Owner":
                token = getOwner().getToken();
                userId = getOwner().getUserId();
                break;
            case "Admin":
                token = getAdmin().getToken();
                userId = getAdmin().getUserId();
                break;
            case "TeamMember":
                token = getTeamMember().getToken();
                userId = getTeamMember().getUserId();
                break;
            case "RestrictedTeamMember":
                token = getRestrictedTeamMember().getToken();
                userId = getRestrictedTeamMember().getUserId();
                break;
            case "CustomRoleTeamOnly":
                token = getCustomRoleTeamOnly().getToken();
                userId = getCustomRoleTeamOnly().getUserId();
                break;
            case "CustomRoleNothing":
                token = getCustomRoleNothing().getToken();
                userId = getCustomRoleNothing().getUserId();
                break;
            default:
                return null;
        }
        return new String[]{token, userId + ""};
    }

    public static void setOwnerAlbatrossToken(String token){
        setValue("ownerAlbatrossToken", token);
    }

    public static String getOwnerAlbatrossToken(){
        return (String) getValue("ownerAlbatrossToken");
    }

    public static void setOwnerAuthCode(String authCode){
        setValue("ownerAuthCode", authCode);
    }

    public static String getOwnerAuthCode(){
        return (String) getValue("ownerAuthCode");
    }
}
