package io.rcrm.api.pojo;

public class UpdateFieldsOnExtension {

	private String key;
	private Value value;
	private int entitytypeid;
	private boolean toggleState;

	public UpdateFieldsOnExtension() {
		super();
	}

	public UpdateFieldsOnExtension(String key, Value value, int entitytypeid, boolean toggleState) {
		this.key = key;
		this.value = value;
		this.entitytypeid = entitytypeid;
		this.toggleState = toggleState;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public int getEntitytypeid() {
		return entitytypeid;
	}

	public void setEntitytypeid(int entitytypeid) {
		this.entitytypeid = entitytypeid;
	}

	public boolean isToggleState() {
		return toggleState;
	}

	public void setToggleState(boolean toggleState) {
		this.toggleState = toggleState;
	}

}
