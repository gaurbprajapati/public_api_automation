package io.rcrm.api.pojo.albatross.offlimit;

public class MarkOffLimitCompany {

    public MarkOffLimitCompany() {
        super();
    }

    private int entity_type_id;
    private int[] entity_ids;
    private int status_id;
    private String start_date;
    private String end_date;
    private String reason;
    private boolean mark_contact_off_limit;
    private boolean mark_candidate_off_limit;

    public int getEntity_type_id() {
        return entity_type_id;
    }

    public void setEntity_type_id(int entity_type_id) {
        this.entity_type_id = entity_type_id;
    }

    public int[] getEntity_ids() {
        return entity_ids;
    }

    public void setEntity_ids(int[] entity_ids) {
        this.entity_ids = entity_ids;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isMark_contact_off_limit() {
        return mark_contact_off_limit;
    }

    public void setMark_contact_off_limit(boolean mark_contact_off_limit) {
        this.mark_contact_off_limit = mark_contact_off_limit;
    }

    public boolean isMark_candidate_off_limit() {
        return mark_candidate_off_limit;
    }

    public void setMark_candidate_off_limit(boolean mark_candidate_off_limit) {
        this.mark_candidate_off_limit = mark_candidate_off_limit;
    }
}
