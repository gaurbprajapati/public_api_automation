package io.rcrm.api.pojo.albatross;

import java.util.List;

public class CustumField {

	private String extrafieldname;
	private String extrafieldtype;
	private int entitytypeid;
	private int columnid;
	private String defaultvalue;
    private List<DefaultOptionsValue> defaultoptionsvalue;

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

	public String getDefaultvalue() {
		return defaultvalue;
	}

	public void setDefaultvalue(String defaultvalue) {
		this.defaultvalue = defaultvalue;
	}

    public List<DefaultOptionsValue> getDefaultoptionsvalue() {
        return defaultoptionsvalue;
    }

    public void setDefaultoptionsvalue(List<DefaultOptionsValue> defaultoptionsvalue) {
        this.defaultoptionsvalue = defaultoptionsvalue;
    }

}