package io.rcrm.api.pojo.albatross;

public class DeleteTemplatePage {

	int idsToDelete;
	String tableFlag;

	public DeleteTemplatePage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DeleteTemplatePage( int idsToDelete,String tableFlag) {
		super();
		this.idsToDelete = idsToDelete;
		this.tableFlag = tableFlag;
	}
	public int  getIdsToDelete() {
		return idsToDelete;
	}

	public void setIdsToDelete(int idsToDelete) {
		this.idsToDelete = idsToDelete;
	}
	public String  getTableFlag() {
		return tableFlag;
	}

	public void setTableFlag(String tableFlag) {
		this.tableFlag = tableFlag;
	}
}

