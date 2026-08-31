package io.rcrm.api.pojo.reaper;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountList {
    @JsonProperty("accountsList")
    private Account[] accountsList;

    public AccountList() {
    }

    public AccountList(Account[] accountsList) {
        this.accountsList = accountsList;
    }

    public Account[] getAccountsList() {
        return accountsList;
    }

    public void setAccountsList(Account[] accountsList) {
        this.accountsList = accountsList;
    }
}
