package io.rcrm.api.pojo.albatross.offlimit;

public class MarkAsAvailable {

    public MarkAsAvailable() {
        super();
    }

    private int entity_type_id;
    private int[] entity_ids;

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

}
