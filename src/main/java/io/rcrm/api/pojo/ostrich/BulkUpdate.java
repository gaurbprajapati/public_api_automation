package io.rcrm.api.pojo.ostrich;

import java.util.ArrayList;

public class BulkUpdate {
    public String key;
    public String value;
    public ArrayList<Integer> ids;
    public int type;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ArrayList<Integer> getIds() {
        return ids;
    }

    public void setIds(ArrayList<Integer> ids) {
        this.ids = ids;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}