package io.rcrm.api.pojo.albatross;

import java.util.List;

public class ExtraField {

	private String extrafieldname;
	private String extrafieldtype;
	private int entitytypeid;
	private int columnid;
	private boolean deleted = false;
	private Object defaultvalue = null;
	private List<DefaultOptionsValue> defaultoptionsvalue;
	private int accountid;
	private int id;
	private String description;
	private Boolean is_parser_enabled;

	// Getters and setters for all fields
	public String getExtrafieldname() {
		return extrafieldname;
	}

	public void setExtrafieldname(String extrafieldname) {
		this.extrafieldname = extrafieldname;
	}

	public String getExtrafieldtype() {
		return extrafieldtype;
	}

	public void setExtrafieldtype(String extrafieldtype) {
		this.extrafieldtype = extrafieldtype;
	}

	public int getEntitytypeid() {
		return entitytypeid;
	}

	public void setEntitytypeid(int entitytypeid) {
		this.entitytypeid = entitytypeid;
	}

	public int getColumnid() {
		return columnid;
	}

	public void setColumnid(int columnid) {
		this.columnid = columnid;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Object getDefaultvalue() {
		return defaultvalue;
	}

	public void setDefaultvalue(Object defaultvalue) {
		this.defaultvalue = defaultvalue;
	}
	
	public List<DefaultOptionsValue> getDefaultoptionsvalue() {
		return defaultoptionsvalue;
	}

	public void setDefaultoptionsvalue(List<DefaultOptionsValue> defaultoptionsvalue) {
		this.defaultoptionsvalue = defaultoptionsvalue;
	}

	public int getAccountid() {
		return accountid;
	}

	public void setAccountid(int accountid) {
		this.accountid = accountid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIs_parser_enabled() {
		return is_parser_enabled;
	}

	public void setIs_parser_enabled(Boolean is_parser_enabled) {
		this.is_parser_enabled = is_parser_enabled;
	}

}
