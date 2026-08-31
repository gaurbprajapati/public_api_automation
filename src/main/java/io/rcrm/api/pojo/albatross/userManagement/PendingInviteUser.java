package io.rcrm.api.pojo.albatross.userManagement;

public class PendingInviteUser {
	private String sort_by = "updatedon";
	private String sortOrder = "asc";
	private int page;
	private String page_size;
	private Object columns;

	// Getters and Setters
	public String getSort_by() {
		return sort_by;
	}

	public void setSort_by(String sort_by) {
		this.sort_by = sort_by;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getPage_size() {
		return page_size;
	}

	public void setPage_size(String page_size) {
		this.page_size = page_size;
	}

	public Object getColumns() {
		return columns;
	}

	public void setColumns(Object columns) {
		this.columns = columns;
	}

}
