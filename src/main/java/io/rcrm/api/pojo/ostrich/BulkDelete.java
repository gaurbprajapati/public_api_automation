package io.rcrm.api.pojo.ostrich;

import java.util.ArrayList;

public class BulkDelete {

    public ArrayList<Integer> idsToDelete;
    public int type;

    public ArrayList<Integer> getIdsToDelete() {
        return idsToDelete;
    }

    public void setIdsToDelete(ArrayList<Integer> idsToDelete) {
        this.idsToDelete = idsToDelete;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
