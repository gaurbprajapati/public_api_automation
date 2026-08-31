package io.rcrm.api.pojo.albatross;

import java.util.List;

public class Team {
	
	private String label;
    private List<String> userids;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getUserids() {
        return userids;
    }

    public void setUserids(List<String> userids) {
        this.userids = userids;
    }

}
