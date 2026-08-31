package io.rcrm.api.pojo.albatross;

public class SSOConfiguration {
    private String client_id;
    private String client_secret;
    private int is_google_idp;

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public int getIs_google_idp() {
        return is_google_idp;
    }

    public void setIs_google_idp(int is_google_idp) {
        this.is_google_idp = is_google_idp;
    }
}
