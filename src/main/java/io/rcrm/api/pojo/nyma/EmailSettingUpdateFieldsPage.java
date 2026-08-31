package io.rcrm.api.pojo.nyma;

public class EmailSettingUpdateFieldsPage {
    private String key;
    private int value;
    private int linked_email_type;

    public EmailSettingUpdateFieldsPage() {
    }

    public EmailSettingUpdateFieldsPage(String key, int value, int linked_email_type) {
        this.key = key;
        this.value = value;
        this.linked_email_type = linked_email_type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getLinked_email_type() {
        return linked_email_type;
    }

    public void setLinked_email_type(int linked_email_type) {
        this.linked_email_type = linked_email_type;
    }
}
