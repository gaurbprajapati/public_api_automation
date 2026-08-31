package io.rcrm.api.pojo;

public class DealSplit {

	private TeammatesCollaborator[] teammates_collaborator;
	private TeamsCollaborator[] teams_collaborator;
	private String split_type;
	public DealSplit() {
	}

	public TeammatesCollaborator[] getTeammates_collaborator() {
		return teammates_collaborator;
	}
	public void setTeammates_collaborator(TeammatesCollaborator[] teammates_collaborator) {
		this.teammates_collaborator = teammates_collaborator;
	}
	public TeamsCollaborator[] getTeams_collaborator() {
		return teams_collaborator;
	}
	public void setTeams_collaborator(TeamsCollaborator[] teams_collaborator) {
		this.teams_collaborator = teams_collaborator;
	}
	public String getSplit_type() {
		return split_type;
	}
	public void setSplit_type(String split_type) {
		this.split_type = split_type;
	}

	public static class TeammatesCollaborator {
		private int teammate_id;
		private double split_percentage;

		public TeammatesCollaborator() {
		}

		public int getTeammate_id() {
			return teammate_id;
		}
		public void setTeammate_id(int teammate_id) {
			this.teammate_id = teammate_id;
		}
		public double getSplit_percentage() {
			return split_percentage;
		}
		public void setSplit_percentage(double split_percentage) {
			this.split_percentage = split_percentage;
		}
	}

	public static class TeamsCollaborator{

		private int team_id;
		private double split_percentage;

		public TeamsCollaborator() {
		}

		public int getTeam_id() {
			return team_id;
		}
		public void setTeam_id(int team_id) {
			this.team_id = team_id;
		}
		public double getSplit_percentage() {
			return split_percentage;
		}
		public void setSplit_percentage(double split_percentage) {
			this.split_percentage = split_percentage;
		}
	}

}



