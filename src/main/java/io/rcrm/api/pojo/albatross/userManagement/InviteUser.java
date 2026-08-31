package io.rcrm.api.pojo.albatross.userManagement;

import java.util.List;

public class InviteUser {
	private List<Invitation> invitation;

	public List<Invitation> getInvitation() {
		return invitation;
	}

	public void setInvitation(List<Invitation> invitation) {
		this.invitation = invitation;
	}

	public static class Invitation {
		private String email;
		private Role role;
		private List<Team> selectedTeam;
		private boolean savedTeam;
		private List<Team> tempTeams;

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public Role getRole() {
			return role;
		}

		public void setRole(Role role) {
			this.role = role;
		}

		public List<Team> getSelectedTeam() {
			return selectedTeam;
		}

		public void setSelectedTeam(List<Team> selectedTeam) {
			this.selectedTeam = selectedTeam;
		}

		public boolean isSavedTeam() {
			return savedTeam;
		}

		public void setSavedTeam(boolean savedTeam) {
			this.savedTeam = savedTeam;
		}

		public List<Team> getTempTeams() {
			return tempTeams;
		}

		public void setTempTeams(List<Team> tempTeams) {
			this.tempTeams = tempTeams;
		}
	}

	public static class Role {
		private int id;
		private String role;
		private String folder;
		private String useraccesjson;
		private int accountid;
		private int createdby;
		private long createdon;
		private int updatedby;
		private long updatedon;

		public Role(int id, String role, String useraccesjson, int accountid, int createdby, long createdon,
				int updatedby, long updatedon) {
			this.id = id;
			this.role = role;
			this.useraccesjson = useraccesjson;
			this.accountid = accountid;
			this.createdby = createdby;
			this.createdon = createdon;
			this.updatedby = updatedby;
			this.updatedon = updatedon;
		}

		public int getId() {
			return id;
		}

		public String getRole() {
			return role;
		}

		public String getFolder() {
			return folder;
		}

		public String getUseraccesjson() {
			return useraccesjson;
		}
		
		public int getAccountid() {
			return accountid;
		}

		public int getCreatedby() {
			return createdby;
		}

		public long getCreatedon() {
			return createdon;
		}

		public int getUpdatedby() {
			return updatedby;
		}

		public long getUpdatedon() {
			return updatedon;
		}

	}

	public static class Team {
		private int teamid;
		private String name;
		private String userids;
		private String id;

		public Team(int teamid, String name, String userids, String id) {
			this.teamid = teamid;
			this.name = name;
			this.userids = userids;
			this.id = id;
		}

		public int getTeamid() {
			return teamid;
		}
		public String getName() {
			return name;
		}
		
		public String getUserids() {
			return userids;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
}
