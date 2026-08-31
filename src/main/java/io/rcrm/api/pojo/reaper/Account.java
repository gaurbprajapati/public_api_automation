package io.rcrm.api.pojo.reaper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Account {
    @JsonProperty("accountId")
    private int accountId;
    
    @JsonProperty("accountName")
    private String accountName;
    
    private OwnerAccount owner;
    private AdminAccount admin;
    private TeamMemberAccount teamMember;
    private RestrictedTeamMemberAccount restrictedTeamMember;
    private CustomRoleTeamOnly customRoleTeamOnly;
    private CustomRoleNothing customRoleNothing;

    public Account() {
    }

    public Account(int accountId, String accountName, OwnerAccount owner, AdminAccount admin, TeamMemberAccount teamMember,
                   RestrictedTeamMemberAccount restrictedTeamMember, CustomRoleTeamOnly customRoleTeamOnly, CustomRoleNothing customRoleNothing) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.owner = owner;
        this.admin = admin;
        this.teamMember = teamMember;
        this.restrictedTeamMember = restrictedTeamMember;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    // Custom setter to handle String accountId from JSON
    @JsonSetter("accountId")
    public void setAccountIdFromJson(String accountId) {
        if (accountId != null && !accountId.isEmpty()) {
            this.accountId = Integer.parseInt(accountId);
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public OwnerAccount getOwner() {
        return owner;
    }

    public void setOwner(OwnerAccount owner) {
        this.owner = owner;
    }

    public AdminAccount getAdmin() {
        return admin;
    }

    public void setAdmin(AdminAccount admin) {
        this.admin = admin;
    }

    public TeamMemberAccount getTeamMember() {
        return teamMember;
    }

    public void setTeamMember(TeamMemberAccount teamMember) {
        this.teamMember = teamMember;
    }

    public RestrictedTeamMemberAccount getRestrictedTeamMember() {
        return restrictedTeamMember;
    }

    public void setRestrictedTeamMember(RestrictedTeamMemberAccount restrictedTeamMember) {
        this.restrictedTeamMember = restrictedTeamMember;
    }

    public CustomRoleTeamOnly getCustomRoleTeamOnly() {
        return customRoleTeamOnly;
    }

    public void setCustomRoleTeamOnly(CustomRoleTeamOnly customRoleTeamOnly) {
        this.customRoleTeamOnly = customRoleTeamOnly;
    }

    public CustomRoleNothing getCustomRoleNothing() {
        return customRoleNothing;
    }

    public void setCustomRoleNothing(CustomRoleNothing customRoleNothing) {
        this.customRoleNothing = customRoleNothing;
    }
}
