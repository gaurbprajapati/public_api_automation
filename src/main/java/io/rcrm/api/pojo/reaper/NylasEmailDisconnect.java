package io.rcrm.api.pojo.reaper;

public class NylasEmailDisconnect {
    private int linked_email_type;
    private int notify;

    public NylasEmailDisconnect() {
    }

    public NylasEmailDisconnect(int linked_email_type, int notify) {
        this.linked_email_type = linked_email_type;
        this.notify = notify;
    }

    public int getLinked_email_type() {
        return linked_email_type;
    }

    public void setLinked_email_type(int linked_email_type) {
        this.linked_email_type = linked_email_type;
    }

    public int getNotify() {
        return notify;
    }

    public void setNotify(int notify) {
        this.notify = notify;
    }
}
