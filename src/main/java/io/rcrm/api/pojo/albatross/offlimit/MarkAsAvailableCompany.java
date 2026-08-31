package io.rcrm.api.pojo.albatross.offlimit;

public class MarkAsAvailableCompany {

    public MarkAsAvailableCompany() {
        super();
    }

    private int entity_type_id;
    private int[] entity_ids;
    private boolean mark_contact_available;
    private boolean mark_candidate_available;

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

    public boolean isMark_contact_available() {
        return mark_contact_available;
    }

    public void setMark_contact_available(boolean mark_contact_available) {
        this.mark_contact_available = mark_contact_available;
    }

    public boolean isMark_candidate_available() {
        return mark_candidate_available;
    }

    public void setMark_candidate_available(boolean mark_candidate_available) {
        this.mark_candidate_available = mark_candidate_available;
    }

}
