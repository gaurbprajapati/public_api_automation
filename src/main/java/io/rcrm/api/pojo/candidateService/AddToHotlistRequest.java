package io.rcrm.api.pojo.candidateService;

import lombok.Data;

import java.util.Arrays;

@Data
public class AddToHotlistRequest {
    private String entity_name;
    private int[] selectedrows;
    private boolean shared;
    private String[] name;
    private boolean updateUserObj;
    private boolean from_add_to_hotlist_modal;

    @Override
    public String toString() {
        return "AddToHotlistRequest{" +
                "entity_name='" + entity_name + '\'' +
                ", selectedrows=" + Arrays.toString(selectedrows) +
                ", shared=" + shared +
                ", name=" + Arrays.toString(name) +
                ", updateUserObj=" + updateUserObj +
                ", from_add_to_hotlist_modal=" + from_add_to_hotlist_modal +
                '}';
    }
}
