package io.rcrm.api.pojo.albatross;

public class GlobalDelete {
	
	int idsToDelete;
    String tableFlag;
    String fieldKey;
    
    
	public GlobalDelete() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public int getIdsToDelete() {
		return idsToDelete;
	}
	public void setIdsToDelete(int idsToDelete) {
		this.idsToDelete = idsToDelete;
	}
	public String getTableFlag() {
		return tableFlag;
	}
	public void setTableFlag(String tableFlag) {
		this.tableFlag = tableFlag;
	}
	public String getFieldKey() {
		return fieldKey;
	}
	public void setFieldKey(String fieldKey) {
		this.fieldKey = fieldKey;
	}   

}
