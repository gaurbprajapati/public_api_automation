package io.rcrm.api.pojo.reaper;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestrictedTeamMemberAccount {

    private String email;
    private String password;
    private String apiKey;
    private String connectedEmail_1;
    private String connectedEmail_2;
    
    @JsonProperty("userId")
    private int userId;
    
    @JsonProperty("fullName")
    private String fullName;
    
    private String token;
    
    @JsonProperty("authCode")
    private String authCode;

    // Default constructor required for Jackson deserialization
    public RestrictedTeamMemberAccount() {
    }

    // Custom constructors for specific parameter combinations
    public RestrictedTeamMemberAccount(String email, String password, String apiKey) {
        this.email = email;
        this.password = password;
        this.apiKey = apiKey;
    }

    public RestrictedTeamMemberAccount(String email, String password, String apiKey, int userId, String fullName, String token, String authCode) {
        this.email = email;
        this.password = password;
        this.apiKey = apiKey;
        this.userId = userId;
        this.fullName = fullName;
        this.token = token;
        this.authCode = authCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getConnectedEmail_1() {
        return connectedEmail_1;
    }

    public void setConnectedEmail_1(String connectedEmail_1) {
        this.connectedEmail_1 = connectedEmail_1;
    }

    public String getConnectedEmail_2() {
        return connectedEmail_2;
    }

    public void setConnectedEmail_2(String connectedEmail_2) {
        this.connectedEmail_2 = connectedEmail_2;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
}
