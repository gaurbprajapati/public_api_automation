package io.rcrm.api.pojo.authservice;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TOTP {

    @JsonProperty("TOTP")
    private String totp;

    @JsonProperty("secretKey")
    private String secretKey;

    @JsonProperty("fromSignInpage")
    private boolean fromSignInpage;

    public String getTotp() {
        return totp;
    }

    public void setTotp(String totp) {
        this.totp = totp;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isFromSignInpage() {
        return fromSignInpage;
    }

    public void setFromSignInpage(boolean fromSignInpage) {
        this.fromSignInpage = fromSignInpage;
    }
}