package io.rcrm.api.pojo.emails;

public class ReceiverEmailsPage {
    private String identifier;
    private String type;

    public ReceiverEmailsPage() {
        super();
    }

    public ReceiverEmailsPage(String identifier, String type) {
        super();
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}