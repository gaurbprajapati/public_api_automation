package io.rcrm.api.pojo.albatross;

public class GetTeam {

	private String sort_by = "updatedon";
	private String sortOrder = "asc";
	private int page = 1;
	private String page_size;
	private TeamFilters teamFilters;

	public GetTeam() {
	}

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

	public TeamFilters getTeamFilters() {
		return teamFilters;
	}

	public void setTeamFilters(TeamFilters teamFilters) {
		this.teamFilters = teamFilters;
	}

	public static class TeamFilters {
		private String teamName;
		private String teamUsers;
		private String teamCreatedBy;

		public TeamFilters() {
		}

		public String getTeamName() {
			return teamName;
		}

		public void setTeamName(String teamName) {
			this.teamName = teamName;
		}

		public String getTeamUsers() {
			return teamUsers;
		}

		public void setTeamUsers(String teamUsers) {
			this.teamUsers = teamUsers;
		}

		public String getTeamCreatedBy() {
			return teamCreatedBy;
		}

		public void setTeamCreatedBy(String teamCreatedBy) {
			this.teamCreatedBy = teamCreatedBy;
		}
	}
}