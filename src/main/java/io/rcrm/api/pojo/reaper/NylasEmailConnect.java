package io.rcrm.api.pojo.reaper;

public class NylasEmailConnect {

    private String emailType;
    private String email;
    private String password;
    private int linked_email_type;
    private int is_default;
    private int roleid;

    public NylasEmailConnect() {
    }
    public NylasEmailConnect(String emailType, String email, String password, int linked_email_type, int is_default, int roleid) {
        this.emailType = emailType;
        this.email = email;
        this.password = password;
        this.linked_email_type = linked_email_type;
        this.is_default = is_default;
        this.roleid = roleid;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
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

    public int getLinked_email_type() {
        return linked_email_type;
    }

    public void setLinked_email_type(int linked_email_type) {
        this.linked_email_type = linked_email_type;
    }

    public int getIs_default() {
        return is_default;
    }

    public void setIs_default(int is_default) {
        this.is_default = is_default;
    }

    public int getRoleid() {return roleid;}

    public void setRoleid(int roleid) {this.roleid = roleid;}
}
