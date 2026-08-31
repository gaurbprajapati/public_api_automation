package io.rcrm.api.pojo.authservice;

public class DisableTwoFactorAuthentication {

    private boolean passwordFlag;

    private String password;

    // Getters
    public boolean isPasswordFlag() {
        return passwordFlag;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setPasswordFlag(boolean passwordFlag) {
        this.passwordFlag = passwordFlag;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
