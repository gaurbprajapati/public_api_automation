package io.rcrm.api.pojo.chromeExtension;

import java.util.List;

public class Hotlist {
	private List<Integer> selectedrows;
	private String entity_name;
	private boolean shared = true;
	private String name;

	// Getters and Setters
	public List<Integer> getSelectedrows() {
		return selectedrows;
	}

	public void setSelectedrows(List<Integer> selectedrows) {
		this.selectedrows = selectedrows;
	}

	public String getEntity_name() {
		return entity_name;
	}

	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}