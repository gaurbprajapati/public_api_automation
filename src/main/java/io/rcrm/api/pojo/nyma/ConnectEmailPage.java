package io.rcrm.api.pojo.nyma;

public class ConnectEmailPage {
    int linked_email_type;

    public ConnectEmailPage(int linked_email_type) {
        this.linked_email_type = linked_email_type;
    }

    public int getLinked_email_type() {
        return linked_email_type;
    }

    public void setLinked_email_type(int linked_email_type) {
        this.linked_email_type = linked_email_type;
    }
}
